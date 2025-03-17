package demo;

public class HelloStackVM {
    static int i = 5;
    static int j;
    static int k;

    static {
        j = 10;
        k = i + j;
    }

    public static void main(String[] args) {
        int a = 3;
        int b = 4;
        int c = add(a, b);
        System.out.println("Hello JVM (main)：" + c);
    }

    public static int add(int a, int b) {
        return a + b;
    }
}