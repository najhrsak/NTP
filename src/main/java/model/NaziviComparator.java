package model;

import java.util.Comparator;

public class NaziviComparator implements Comparator<Menza> {
    @Override
    public int compare(Menza o1, Menza o2) {
        return o1.getNaziv().compareTo(o2.getNaziv());
    }
}
