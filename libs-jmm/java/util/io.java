package util;

import java.io.*;
import java.lang.*;
import java.util.*;


public class io {

    // buffer to emulate the buffer of the keyboard
    static Vector<Integer> Buffer = new Vector<Integer>();
    static boolean init;

    public final static int read() {
        int a = 0;

        if (Buffer.isEmpty()) {
            init = true;
            BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
            String line;
            try {
                if ((line = teclado.readLine()) != null) {

                    String[] tokens = line.split("\\s");

                    for (int i = 0; i < tokens.length; i++) {

                        int value = Integer.parseInt(tokens[i]);

                        if (!init) {
                            Buffer.addElement(value);
                        } else {
                            a = value;
                            init = false;
                        }
                    }
                }
            } catch (java.io.IOException e) {
                e.printStackTrace(System.err);
                System.exit(1);
            }

        } else {
            Integer top = (Integer) Buffer.firstElement();
            Buffer.removeElementAt(0);
            a = top;
        }
        return a;
    }

    public static void printSI(String c, int a) {
        System.out.print(c + a);
    }

    public static void print(int a) {
        System.out.print(a);
    }

    public static void printS(String a) {
        System.out.print(a);
    }

    public static void printB(boolean a) {
        System.out.print(a);
    }

    public static void printLine() {
        System.out.println("");
    }

    public static void printSIln(String c, int a) {
        System.out.println(c + a);
    }

    public static void println(int a) {
        System.out.println(a);
    }

    public static void printSln(String a) {
        System.out.println(a);
    }

    public static void printBln(boolean a) {
        System.out.println(a);
    }
}
