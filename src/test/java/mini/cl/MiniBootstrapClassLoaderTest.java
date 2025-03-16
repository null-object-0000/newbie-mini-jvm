package mini.cl;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class MiniBootstrapClassLoaderTest {

    @Test
    public void loadClass() throws IOException {
        MiniClass clazz = MiniBootstrapClassLoader.loadClass("HelloStackVM");
        // 验证类名
        assertEquals("demo.HelloStackVM", clazz.getName());
        System.out.println(clazz.getSuperClass());
    }

}