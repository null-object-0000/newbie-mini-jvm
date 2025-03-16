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
        assertEquals(15, (Integer) clazz.getStaticVariables().get("k"));

        MiniClass.MiniMemberInfo init = clazz.getMethod("<init>");
        MethodCaller.call(clazz, init);

        MiniClass.MiniMemberInfo main = clazz.getMethod("main");
        MethodCaller.call(clazz, main);

    }

}