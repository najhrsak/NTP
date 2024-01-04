package model;

import java.io.Serializable;

public class Jelo implements Serializable{
    private Integer id;
    private String naziv;
    private Double cijena;

    public Jelo(Integer id, String naziv, Double cijena) {
        this.id = id;
        this.naziv = naziv;
        this.cijena = cijena;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public Double getCijena() {
        return cijena;
    }

    public void setCijena(Double cijena) {
        this.cijena = cijena;
    }

    @Override
    public String toString() {
        return "Jelo{" +
                "id=" + id +
                ", naziv='" + naziv + '\'' +
                ", cijena=" + cijena +
                '}';
    }
}
