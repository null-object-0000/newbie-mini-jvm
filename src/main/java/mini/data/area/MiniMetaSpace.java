package mini.data.area;

import mini.cl.MiniClass;
import mini.cl.loader.MiniApplicationClassLoader;
import mini.cl.loader.MiniExtensionClassLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * 元空间
 * 这里模拟了 JDK 1.8 及以上在【本地内存】中的【元空间】
 */
public class MiniMetaSpace {
    public final static Map<String, MiniClass> CLASS_CACHE = new HashMap<>();
    private static final Map<MiniClass, MiniConstantPool> CONSTANT_POOLS = new HashMap<>();

    public static MiniConstantPool getConstantPool(MiniClass clazz) {
        return CONSTANT_POOLS.get(clazz);
    }

    public static void putConstantPool(MiniClass clazz, MiniConstantPool pool) {
        CONSTANT_POOLS.put(clazz, pool);
    }

    public final static MiniExtensionClassLoader EXT_CLASS_LOADER = new MiniExtensionClassLoader();
    public final static MiniApplicationClassLoader APP_CLASS_LOADER = new MiniApplicationClassLoader();
}
