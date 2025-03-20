package mini.cl;

import mini.MiniVirtualMachine;
import mini.data.area.MiniVirtualMachineMemory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class MiniBootstrapClassLoaderTest {

    @Test
    public void loadClass() throws IOException {
        MiniVirtualMachine.start(null);

        MiniClass clazz = MiniVirtualMachineMemory.METHOD_AREA.APP_CLASS_LOADER.loadClass("demo.HelloStackVM");
        // 验证类名
        assertEquals("demo.HelloStackVM", clazz.getName());
        assertEquals(15, (Integer) clazz.getStaticVariables().get("k"));

        MiniClass.MiniMemberInfo main = clazz.getMethod("main");
        MiniStackFrame stackFrame = MethodCaller.call(clazz, main, new HashMap<>());

        assertEquals(7, stackFrame.getLocalVariableTable().get(3));
    }

    /**
     * 1. 加载类的元信息
     * 2. 计算对象的大小并为其分配内存空间（内存规整-指针碰撞、内存不规整-空闲链表）
     * 3. 处理并发安全问题（CAS、TLAB）
     * 4. 初始化分配到的空间（属性的默认初始化）
     * 5. 设置对象头的信息
     * 6. 执行 <init> 进行初始化对象（属性的显示初始化、代码块与构造器初始化）
     */
    @Test
    public void instanceOf() throws IOException {
        MiniVirtualMachine.start(null);

        MiniClass clazz = MiniVirtualMachineMemory.METHOD_AREA.APP_CLASS_LOADER.loadClass("demo.TestObject");
        // 验证类名
        assertEquals("demo.TestObject", clazz.getName());

        MiniClass.MiniMemberInfo main = clazz.getMethod("main");
        MiniStackFrame stackFrame = MethodCaller.call(clazz, main, new HashMap<>());

        System.out.println("==============================================================================");
        System.out.println(MiniVirtualMachineMemory.HEAP_AREA.YOUNG_GEN.EDEN.toString().substring(0, 1024));
    }

}