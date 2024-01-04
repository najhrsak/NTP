package model;

import java.util.Arrays;
import java.util.Optional;

public enum EnumaraGradova {
    SISAK("Sisak", 44000),
    ZAGREB("Zagreb", 10000),
    SPLIT("Split", 21000),
    OSIJEK("Osijek", 31000),
    RIJEKA("Rijeka", 51000),
    VARAZDIN("Varazdin", 42000),
    ZADAR("Zadar", 23000),
    DUBROVNIK("Dubrovnik", 20000);

    private String ime;
    private Integer pbr;

    EnumaraGradova(String ime, Integer pbr){
        this.ime = ime;
        this.pbr = pbr;
    }

    public String getIme() {
        return ime;
    }

    public Integer getPbr() {
        return pbr;
    }
    public static Optional<EnumaraGradova> get(String imeGrada) {
        return Arrays.stream(EnumaraGradova.values())
                .filter(grad -> grad.ime.equals(imeGrada))
                .findFirst();
    }
}
