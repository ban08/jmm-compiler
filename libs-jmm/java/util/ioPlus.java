package util;

import java.util.Scanner;

public class ioPlus {
    public static void printResult(int var0) {
        System.out.println("Result: " + var0);
    }

    public static void printHelloWorld() {
        System.out.println("Hello, World!");
    }

    public static int requestNumber() {
        System.out.print("Insert number: ");
        Scanner var0 = new Scanner(System.in);
        int var1 = var0.nextInt();
        return var1;
    }
}
