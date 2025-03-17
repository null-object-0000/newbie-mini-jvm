package mini.cl;

import mini.data.area.MiniMetaSpace;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class MiniBootstrapClassLoaderTest {

    @Test
    public void loadClass() throws IOException {
        MiniClass clazz = MiniMetaSpace.APPLICATION_CLASS_LOADER.loadClass("demo.HelloStackVM");
        // 验证类名
        assertEquals("demo.HelloStackVM", clazz.getName());
        assertEquals(15, (Integer) clazz.getStaticVariables().get("k"));

        MiniClass.MiniMemberInfo init = clazz.getMethod("<init>");
        MethodCaller.call(clazz, init, new HashMap<>());

        MiniClass.MiniMemberInfo main = clazz.getMethod("main");
        MiniStackFrame stackFrame = MethodCaller.call(clazz, main, new HashMap<>());

        assertEquals(7, stackFrame.getLocalVariableTable().get(3));
    }

}