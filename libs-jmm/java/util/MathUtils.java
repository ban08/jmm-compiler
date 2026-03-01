package util;

public class MathUtils {
    public static int random(int var0, int var1) {
        int var2 = var1 - var0 + 1;
        return (int)(Math.random() * (double)var2 + (double)var0);
    }
}
