package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class Student extends Korisnik{
    private String jmbag;
    private String ime, prezime, eAdresa, fakultet;
    private InputStream slika;


    public Student(Integer id, String username, String pristup, String jmbag, String ime, String prezime,
                   String eAdresa, String fakultet, InputStream slika) {
        super(id, username, pristup);
        this.jmbag = jmbag;
        this.ime = ime;
        this.prezime = prezime;
        this.eAdresa = eAdresa;
        this.fakultet = fakultet;
        this.slika = slika;
    }

    public String getJmbag() {
        return jmbag;
    }

    public void setJmbag(String jmbag) {
        this.jmbag = jmbag;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public String geteAdresa() {
        return eAdresa;
    }

    public void seteAdresa(String eAdresa) {
        this.eAdresa = eAdresa;
    }

    public String getFakultet() {
        return fakultet;
    }

    public void setFakultet(String fakultet) {
        this.fakultet = fakultet;
    }

    public InputStream getSlika() {
        return slika;
    }

    public void setSlika(InputStream slika) {
        this.slika = slika;
    }
}
