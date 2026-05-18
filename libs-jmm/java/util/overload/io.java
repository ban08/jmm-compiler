package util.overload;

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
                        //System.out.println(tokens[i]);

                        int value = Integer.parseInt(tokens[i]);

                        //System.out.println("valor lido: "+value);

                        if (!init) {
                            Buffer.addElement(value);
                        } else {
                            a = value;
                            init = false;
                        }
                    }
                }
            } catch (java.io.IOException e) {
//                System.out.println(e);
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

	/* final static int readln() {
		int a=0;
		try {
			BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
			a = Integer.parseInt(teclado.readLine());
		} catch (java.io.IOException e) {
			System.out.println(e);
			System.exit(1);
		}
		return a;
	} */

    public static void print(String c, int a) {
        System.out.print(c + a);
    }

    public static void print(int a) {
        System.out.print(a);
    }

    public static void print(String a) {
        System.out.print(a);
    }

    public static void print(boolean a) {
        System.out.print(a);
    }

    public static void println() {
        System.out.println();
    }

    public static void println(String c, int a) {
        System.out.println(c + a);
    }

    public static void println(int a) {
        System.out.println(a);
    }

    public static void println(String a) {
        System.out.println(a);
    }

    public static void println(boolean a) {
        System.out.println(a);
    }
}
