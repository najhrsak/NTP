package DRETVE;

import com.example.menze.MenzaViewController;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class DretvaPosjecenosti implements Runnable{
    @Override
    public void run() {
        Long max = 12L;
        Long brojNarudzbiUProteklojMin = ThreadLocalRandom.current().nextLong(0, max);
        Long rez = brojNarudzbiUProteklojMin*100/max;
        System.out.println(rez);
        MenzaViewController.setPosjecenost(brojNarudzbiUProteklojMin*100/max);
    }
}
