package examples;

public class Pair {
    int left;
    int right;

    public Pair(int left, int right) {
        this.left = left;
        this.right = right;
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public int setLeft(int left) {
        int old = this.left;
        this.left = left;
        return old;
    }

    public int setRight(int right) {
        int old = this.right;
        this.right = right;
        return old;
    }
}