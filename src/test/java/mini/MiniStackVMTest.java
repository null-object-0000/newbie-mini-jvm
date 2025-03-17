package mini;

import demo.HelloStackVM;
import mini.cl.MiniStackFrame;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class MiniStackVMTest {

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

        MiniStackFrame stackFrame = new MiniStackFrame(null, "main", new HashMap<>());
        int pc = 0;
        for (String instruction : instructions) {
            MiniStackVM.INSTANCE.execute(stackFrame, pc++, instruction);
        }
    }

    @Test
    public void full() {
        String[] instructions = {
                "bipush 10",
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

        MiniStackFrame stackFrame = new MiniStackFrame(null, "main", new HashMap<>());
        int pc = 0;
        for (String instruction : instructions) {
            MiniStackVM.INSTANCE.execute(stackFrame, pc++, instruction);
        }
    }
}