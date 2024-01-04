package model;

import java.util.Comparator;

public class GradoviComparator implements Comparator<Menza> {
    @Override
    public int compare(Menza o1, Menza o2) {
        return o1.getGrad().compareTo(o2.getGrad());
    }
}
