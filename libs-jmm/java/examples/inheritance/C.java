package examples.inheritance;

import examples.inheritance.B;
import examples.inheritance.A;

public class C {
    public A method(B b) {
        A a;
        a = b.self();
        return a;
    }
}
