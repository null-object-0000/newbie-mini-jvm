package mini;

import demo.HelloStackVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StackVMTest {

    @BeforeEach
    public void setUp() {
        // 清空局部变量表和栈
        StackVM.INSTANCE.reset();
    }

    /**
     * 测试执行一段简单的字节码指令
     * @see HelloStackVM
     */
    @Test
    public void execute() {
        String[] instructions = {
                "iconst_3",
                "istore_1",
                "iconst_4",
                "istore_2",
                "iload_1",
                "iload_2",
                "iadd",
                "istore_3",
                "return"
        };

        for (String instruction : instructions) {
            StackVM.INSTANCE.execute(instruction);
        }

        // 验证局部变量表中的值
        assertEquals(7, StackVM.INSTANCE.getLocalVariable(3));
    }
}