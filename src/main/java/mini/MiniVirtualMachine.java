package mini;

import mini.cl.loader.MiniBootstrapClassLoader;

import java.io.IOException;

public final class MiniVirtualMachine {
    public static void main(String[] args) throws IOException {
        MiniVirtualMachine.start(args);
    }

    public static void start(String[] args) throws IOException {
        // 引导类加载器默认加载 java.lang 包下的类
        String[] classNames = {
                "demo/java/lang/MiniObject",
                "demo/java/lang/MiniSystem",
                "demo/java/io/MiniPrintStream",
        };
        for (String className : classNames) {
            MiniBootstrapClassLoader.loadClass(className);
        }
        System.out.println("================================================== MiniJVM started ==================================================");
    }
}
