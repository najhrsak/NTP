package model;

import java.io.Serializable;

public class Korisnik implements Serializable {
    private Integer id;
    private String username, pristup;

    public Korisnik(Integer id, String username, String pristup) {
        this.id = id;
        this.username = username;
        this.pristup = pristup;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPristup() {
        return pristup;
    }

    public void setPristup(String pristup) {
        this.pristup = pristup;
    }
}
