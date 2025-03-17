package mini.cl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mini.data.structure.MiniStack;

import java.util.Map;

/**
 * 栈帧
 */
@Getter
@AllArgsConstructor
public class MiniStackFrame {
    private final MiniClass clazz;
    private final String methodName;

    /**
     * 局部变量表
     */
    private final Map<Integer, Integer> localVariableTable;
    /**
     * 操作数栈
     */
    private final MiniStack<Integer> operandStack = new MiniStack<>();
    /**
     * 栈顶缓存变量（模拟寄存器）
     */
    private Integer tosCache;
}
