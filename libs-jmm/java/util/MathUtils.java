package util;

import java.util.Random;

public class MathUtils {

    private static Random rand = new Random();

    public static void setSeed(int seed) {
        rand = new Random(seed);
    }

    public static int random(int var0, int var1) {
        int var2 = var1 - var0 + 1;
        return (int) (rand.nextDouble() * (double) var2 + (double) var0);
    }
}
