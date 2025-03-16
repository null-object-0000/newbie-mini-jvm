package demo;

public class HelloStackVM {
    private static final int i = 5;
    private static final int j;

    static {
        j = 10;
        int k = i + j;
        System.out.println("Hello JVM (static)：" + k);
    }

    public static void main(String[] args) {
        int a = 3;
        int b = 4;
        int c = a + b;
        System.out.println("Hello JVM (main)：" + c);
    }
}