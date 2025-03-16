package mini.data.area;

import mini.cl.MiniClass;

import java.util.HashMap;
import java.util.Map;

/**
 * 元空间
 * 这里模拟了 JDK 1.8 及以上在【本地内存】中的【元空间】
 */
public class MiniMetaSpace {
    public final static Map<String, MiniClass> CLASS_CACHE = new HashMap<>();
    private static Map<MiniClass, MiniConstantPool> constantPool = new HashMap<>();

    public static MiniConstantPool getConstantPool(MiniClass clazz) {
        return constantPool.get(clazz);
    }

    public static void putConstantPool(MiniClass clazz, MiniConstantPool pool) {
        constantPool.put(clazz, pool);
    }
}
