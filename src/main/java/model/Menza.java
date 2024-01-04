package model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Menza extends Korisnik{
    private String naziv, adresa, grad, jelovnik, radnoVrijeme, info;
    private final BooleanProperty favorit = new SimpleBooleanProperty();


    public Menza(Integer id, String username, String pristup, String naziv, String adresa, String grad, String info,
                 String jelovnik, String radnoVrijeme, boolean favorit) {
        super(id, username, pristup);
        this.naziv = naziv;
        this.adresa = adresa;
        this.grad = grad;
        this.info = info;
        this.jelovnik = jelovnik;
        this.radnoVrijeme = radnoVrijeme;
        this.favorit.set(favorit);
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getAdresa() {
        return adresa;
    }

    public void setAdresa(String adresa) {
        this.adresa = adresa;
    }

    public String getGrad() {
        return grad;
    }

    public void setGrad(String grad) {
        this.grad = grad;
    }

    public String getJelovnik() {
        return jelovnik;
    }

    public void setJelovnik(String jelovnik) {
        this.jelovnik = jelovnik;
    }

    public String getRadnoVrijeme() {
        return radnoVrijeme;
    }

    public void setRadnoVrijeme(String radnoVrijeme) {
        this.radnoVrijeme = radnoVrijeme;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public BooleanProperty getFavorit(){return favorit;}
}
