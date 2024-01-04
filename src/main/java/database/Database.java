package database;

import model.EnumaraGradova;
import model.Korisnik;
import model.Menza;
import model.Student;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.io.FileUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.*;
import java.util.*;

import static org.apache.commons.codec.binary.Hex.decodeHex;
import static org.apache.commons.io.FileUtils.copyInputStreamToFile;
import static org.apache.commons.io.FileUtils.readFileToByteArray;

public class Database {
    private static MessageDigest md;



    static {
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private static SecureRandom random = new SecureRandom();

    private static Connection connectToDatabase() throws SQLException, IOException {
        Properties configuration = new Properties();
        configuration.load(new FileReader("dat/database.properties"));
        String databaseURL = configuration.getProperty("databaseURL");
        String databaseUsername = configuration.getProperty("databaseUsername");
        String databasePassword = configuration.getProperty("databasePassword");
        Connection connection = DriverManager
                .getConnection(databaseURL, databaseUsername, databasePassword);
        return connection;
    }

    public static Korisnik getKorisnik(String user, String pass) throws SQLException, IOException, NoSuchAlgorithmException {
        Connection connection = connectToDatabase();

        PreparedStatement korisnikStatement = connection.prepareStatement("SELECT pristup FROM korisnici where username = ?");
        korisnikStatement.setString(1, user);
        ResultSet rsKorisnik = korisnikStatement.executeQuery();
        rsKorisnik.next();
        String pPristup = rsKorisnik.getString("pristup");

        /*byte[] salt = new byte[16];
        random.nextBytes(salt);
        md.update(salt);*/
        PreparedStatement statement;

        if(pPristup.equals("student"))
        {
            String salt = "NaCl";

            md.update(salt.getBytes());

            statement = connection.prepareStatement("SELECT id, username, pristup FROM korisnici " +
                "WHERE korisnici.username = ? AND  korisnici.password = ?");
            statement.setString(1, user);
            statement.setString(2, new String(md.digest(pass.getBytes(StandardCharsets.UTF_8))));
        }
        else
        {
            statement = connection.prepareStatement("SELECT id, username, pristup FROM korisnici " +
                    "WHERE korisnici.username = ? AND  korisnici.password = ?");
            statement.setString(1, user);
            statement.setString(2, pass);
        }
        ResultSet rs = statement.executeQuery();
        Integer id;
        String username, pristup;
        rs.next();
        id = rs.getInt("id");
        username = rs.getString("username");
        pristup = rs.getString("pristup");
        disconnectFromDatabase(connection);

        Korisnik k = new Korisnik(id, username, pristup);

        FileOutputStream fos = new FileOutputStream("dat/last_session.bin");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(k);
        oos.flush();
        oos.close();
        return k;
    }

    public static Student getStudentFromKorisnik(Korisnik korisnik) throws SQLException, IOException{
        File privateKeyFile = new File("dat/private.key");
        PrivateKey privateKey;
        Cipher decryptCipher = null;
        byte[] publicKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
        try{
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(publicKeyBytes);
            privateKey = keyFactory.generatePrivate(privateKeySpec);
            decryptCipher = Cipher.getInstance("RSA");
            decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
        Connection connection= connectToDatabase();
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM studenti WHERE studenti.username = ?");
        stmt.setString(1, korisnik.getUsername());
        ResultSet rs = stmt.executeQuery();
        String ime, prezime = "", jmbag, fakultet, eAdresa, encIme = "";
        rs.next();
        jmbag = rs.getString("jmbag");
        ime = rs.getString("ime");

        try{
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, getKey());
            byte[] plainText = cipher.doFinal(Base64.getDecoder()
                    .decode(ime));
            encIme = new String(plainText);
        } catch (InvalidKeyException | NoSuchPaddingException |
                 IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException e) {
            e.printStackTrace();
        }


        byte[] decodedPrezimeBytes = Base64.getDecoder().decode(rs.getString("prezime"));
        try{
            byte[] decrytedPrezimeBytes = decryptCipher.doFinal(decodedPrezimeBytes);
            prezime = new String(decrytedPrezimeBytes, StandardCharsets.UTF_8);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }

        eAdresa = rs.getString("eadresa");
        fakultet = rs.getString("fakultet");
        Blob blob = rs.getBlob("slika");
        File file = new File("dat/slika.jpg");
        copyInputStreamToFile(blob.getBinaryStream(), file);

        InputStream slika = new FileInputStream(file);
        //InputStream slika = rs.getBinaryStream("slika");
        disconnectFromDatabase(connection);



        return new Student(korisnik.getId(), korisnik.getUsername(), korisnik.getPristup(), jmbag, encIme, prezime,
                eAdresa, fakultet, slika);
    }

    public static Menza getMenzaFromKorisnik(Korisnik korisnik) throws  SQLException, IOException{
        Connection connection = connectToDatabase();
        PreparedStatement stmt = connection.prepareStatement("select * from menze where menze.id_korisnika = ?");
        stmt.setString(1, korisnik.getId().toString());
        ResultSet rs = stmt.executeQuery();
        String naziv, adresa, grad, info, radnoVrijeme, jelovnik;
        rs.next();
        naziv = rs.getString("naziv");
        adresa = rs.getString("adresa");
        grad = rs.getString("grad");
        info = rs.getString("info");
        jelovnik = rs.getString("jelovnik");
        radnoVrijeme = rs.getString("radno_vrijeme");
        disconnectFromDatabase(connection);

        return new Menza(korisnik.getId(), korisnik.getUsername(), korisnik.getPristup(), naziv, adresa, grad, info,
                jelovnik, radnoVrijeme, false);

    }


    public static List<Menza> getSveMenze()throws SQLException, IOException{
        Connection connection = connectToDatabase();
        List<Menza> sveMenze = new ArrayList<>();
        Integer id;
        String naziv, adresa, jelovnik, info, radnoVrijeme;
        Optional<EnumaraGradova> grad;
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM menze");
        while(rs.next()){
            id = rs.getInt("id");
            naziv = rs.getString("naziv");
            adresa = rs.getString("adresa");
            grad = EnumaraGradova.get(rs.getString("grad"));
            info = rs.getString("info");
            jelovnik = rs.getString("jelovnik");
            radnoVrijeme = rs.getString("radno_vrijeme");
            sveMenze.add(new Menza(id, null, null, naziv, adresa, grad.get().getIme(), info, jelovnik,
                    radnoVrijeme, false));
        }
        disconnectFromDatabase(connection);

        return sveMenze;
    }

    public static void updateTextField(String text, String field, Korisnik k) throws SQLException, IOException{
        Connection connection = connectToDatabase();
        PreparedStatement stmt;
        if(field.equals("dnevni_meni"))
            stmt = connection.prepareStatement("UPDATE menze SET jelovnik = ? WHERE id_korisnika = '" + k.getId() +"';");
        else if(field.equals("info"))
            stmt = connection.prepareStatement("UPDATE menze SET info = ? WHERE id_korisnika = '" + k.getId() +"';");
        else
            stmt = connection.prepareStatement("UPDATE menze SET radno_vrijeme = ? WHERE id_korisnika = '" + k.getId() +"';");

        stmt.setString(1, text);
        stmt.executeUpdate();
        disconnectFromDatabase(connection);

    }

    public static void newStudent(Student student, String pass) throws SQLException, IOException{
        String encIme = student.getIme();
        try{
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, getKey());
            byte[] cipherText = cipher.doFinal(student.getIme().getBytes());
            encIme = Base64.getEncoder().encodeToString(cipherText);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        File publicKeyFile = new File("dat/public.key");
        PublicKey publicKey;
        Cipher encryptCipher = null;
        byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
        try{
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            publicKey = keyFactory.generatePublic(publicKeySpec);
            encryptCipher = Cipher.getInstance("RSA");
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }

        Connection connection = connectToDatabase();

        /*byte[] salt = new byte[16];
        random.nextBytes(salt);*/
        String salt = "NaCl";
        md.update(salt.getBytes());
        PreparedStatement stmtK = connection.prepareStatement("INSERT INTO korisnici(username, password, pristup)" +
                "VALUES (?,?,?)");
        stmtK.setString(1, student.getUsername());
        stmtK.setString(2, new String(md.digest(pass.getBytes(StandardCharsets.UTF_8))));
        stmtK.setString(3, student.getPristup());
        stmtK.executeUpdate();

        byte[] prezimeBytes = student.getPrezime().getBytes(StandardCharsets.UTF_8);
        byte[] encryptedPrezimeBytes;
        String encodedPrezime = student.getPrezime();
        try{
            encryptedPrezimeBytes = encryptCipher.doFinal(prezimeBytes);
            encodedPrezime = Base64.getEncoder().encodeToString(encryptedPrezimeBytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        PreparedStatement stmtS = connection.prepareStatement("INSERT  INTO studenti VALUES (?,?,?,?,?,null,?,?)");
        stmtS.setString(1, student.getJmbag());
        stmtS.setString(2, encIme);
        stmtS.setString(3, encodedPrezime);
        stmtS.setString(4, student.geteAdresa());
        stmtS.setString(5, student.getFakultet());
        //InputStream in = new FileInputStream("dat/profile_photo.jpg");

        stmtS.setBlob(6, student.getSlika());
        stmtS.setString(7, student.getUsername());
        stmtS.executeUpdate();
    }

    public static void updateStudent(String jmbag, String eadresa, String fakultet, FileInputStream slika) throws SQLException, IOException {
        Connection connection = connectToDatabase();
        PreparedStatement stmt = connection.prepareStatement("UPDATE studenti " +
                "SET eadresa = ?, fakultet = ?, slika = ? WHERE jmbag = ?;");
        stmt.setString(1, eadresa);
        stmt.setString(2, fakultet);
        stmt.setBlob(3, slika);
        stmt.setString(4, jmbag);
        stmt.executeUpdate();
    }

    public static void deleteStudent(String username) throws SQLException, IOException {
        Connection connection = connectToDatabase();
        PreparedStatement stmt = connection.prepareStatement("DELETE FROM Studenti WHERE username = ?");
        stmt.setString(1, username);
        stmt.executeUpdate();
        PreparedStatement stmtk = connection.prepareStatement("DELETE FROM Korisnici WHERE username = ?");
        stmtk.setString(1, username);
        stmtk.executeUpdate();
    }

    public static void insertDnevniMeni(String dnevniMeni, Korisnik k) throws SQLException, IOException {
        Connection connection = connectToDatabase();
        PreparedStatement stmt = connection.prepareStatement("UPDATE menze SET jelovnik = ? WHERE id_korisnika = ?;");
        stmt.setString(1, dnevniMeni);
        stmt.setString(2, k.getId().toString());
        stmt.executeUpdate();
    }

    public static String getJelovnik(Korisnik k) throws SQLException, IOException {
        Connection connection = connectToDatabase();
        PreparedStatement stmt = connection.prepareStatement("SELECT jelovnik FROM menze where id_korisnika = ?;");
        stmt.setInt(1, k.getId());
        ResultSet rs = stmt.executeQuery();
        rs.next();
        return rs.getString("jelovnik");
    }

    public static List<String> getGradovi() throws SQLException, IOException {
        Connection connection = connectToDatabase();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT DISTINCT(grad) FROM menze");
        List<String> gradovi = new ArrayList<>();
        while(rs.next()){
            gradovi.add(rs.getString("grad"));
        }
        return gradovi;
    }

    private static void disconnectFromDatabase(Connection connection){
        try{
            if(!connection.equals(null) && !connection.isClosed())
                connection.close();
        }catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static SecretKey getKey() throws IOException {
        String data = new String(readFileToByteArray(new File("dat/aes_key.key")));
        byte[] encoded = null;
        try {
            encoded = decodeHex(data.toCharArray());
        } catch (DecoderException e) {
            e.printStackTrace();
        }
        return new SecretKeySpec(encoded, "AES");

    }





}
