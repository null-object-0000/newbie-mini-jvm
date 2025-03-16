package mini;

import cn.hutool.core.io.FileUtil;
import cn.hutool.system.SystemUtil;
import mini.cl.MiniClass;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class MiniClassTest {

    @Test
    public void parse() throws IOException {
        // 读取字节码文件
        byte[] classData = FileUtil.readBytes(SystemUtil.getUserInfo().getCurrentDir() + "\\src\\main\\java\\demo\\HelloStackVM.class");
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(classData));

        MiniClass clazz = new MiniClass(input);
        clazz._linking_verify();
        // 验证类名
        assertEquals("demo.HelloStackVM", clazz.getName());
    }
}