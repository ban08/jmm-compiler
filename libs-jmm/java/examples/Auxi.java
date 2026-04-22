package examples;

import util.io;

public class Auxi {


    public static void staticCall(int a) {
        io.println(a);
    }

    public void instanceCallVoid() {
        io.println(10);
    }

    public int instanceCallInt() {
        return 20;
    }

    public boolean instanceCallBool() {
        return true;
    }

    public int instanceCallIntWithArgs(int a, int b) {
        return a + b;
    }

    public int instanceExpectingAuxi(Auxi auxi) {
        return auxi.instanceCallInt();
    }

    public int instanceExpectingObject(Object obj) {
        return obj.hashCode();
    }

}