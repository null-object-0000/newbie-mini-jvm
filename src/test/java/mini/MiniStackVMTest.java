package mini;

import demo.HelloStackVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MiniStackVMTest {

    @BeforeEach
    public void setUp() {
        // 清空局部变量表和栈
        MiniStackVM.INSTANCE.reset();
    }

    /**
     * 测试执行一段简单的字节码指令
     *
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
                "getstatic",
                "iload_3",
                "invokedynamic",
                "invokevirtual",
                "return"
        };

        int pc = 0;
        for (String instruction : instructions) {
            MiniStackVM.INSTANCE.execute(null, pc++, instruction);
        }
    }

    @Test
    public void full() {
        String[] instructions = {
                "bipush",
                "putstatic",
                "iconst_5",
                "getstatic",
                "iadd",
                "istore_0",
                "getstatic",
                "iload_0",
                "invokedynamic",
                "invokevirtual",
                "return",
        };

        int pc = 0;
        for (String instruction : instructions) {
            MiniStackVM.INSTANCE.execute(null, pc++, instruction);
        }
    }
}