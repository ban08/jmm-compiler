package util;

import java.util.Scanner;

public class ioPlus {
    public static void printResult(int var0) {
        System.out.println("Result: " + var0);
    }

    public static void printHelloWorld() {
        System.out.println("Hello, World!");
    }

    public static void sayHello() {
        System.out.println("Who are you? ");
        Scanner sc = new Scanner(System.in);
        System.out.println("Hello, " + sc.nextLine() + "!");
    }

    public static int requestNumber() {
        System.out.print("Insert number: ");
        Scanner var0 = new Scanner(System.in);
        int var1 = var0.nextInt();
        return var1;
    }

    public static void printDashedLine(int numDashes) {
        System.out.println("-".repeat(numDashes));
    }
}
