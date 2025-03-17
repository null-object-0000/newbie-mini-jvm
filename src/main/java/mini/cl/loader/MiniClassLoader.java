package mini.cl.loader;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mini.cl.MethodCaller;
import mini.cl.MiniClass;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;

@Getter
@AllArgsConstructor
public abstract class MiniClassLoader {
    private MiniClassLoader parent;

    public abstract MiniClass loadClass(String className) throws IOException;

    public MiniClass defineClass(byte[] classData) throws IOException {
        // 第一阶段：加载 Loading
        MiniClass clazz = _load(classData);

        // 第二阶段：链接 Linking
        // 1. 验证 Verify
        clazz._linking_verify();
        // 2. 准备 Prepare
        clazz._linking_prepare();
        // 3. 解析 Resolve
        clazz._linking_resolve();

        // 第三阶段：初始化 Initialization
        _initialization(clazz);

        return clazz;
    }

    /**
     * 1. 通过全类名获取定义此类的二进制字节流。
     * 2. 将字节流所代表的静态存储结构转换为方法区的运行时数据结构。
     * 3. 在内存中生成一个代表该类的 Class 对象，作为方法区这些数据的访问入口。
     */
    private static MiniClass _load(byte[] classData) {
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(classData));
        return new MiniClass(input);
    }

    private static void _initialization(MiniClass clazz) {
        // 先执行 <clinit> 方法
        MiniClass.MiniMemberInfo clinit = clazz.getMethod("<clinit>");
        if (clinit == null) return;
        MethodCaller.call(clazz, clinit, new HashMap<>());
    }
}
