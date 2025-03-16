package mini;

import mini.data.area.MiniMetaSpace;
import mini.data.structure.MiniStack;

import java.util.HashMap;
import java.util.Map;

/**
 * 模拟栈式虚拟机执行字节码指令
 */
public class MiniStackVM {
    /**
     * 基于栈的指令
     */
    private final static MiniStack<Integer> INSTRUCTION_STACK = new MiniStack<>();
    /**
     * 局部变量表
     */
    private final static Map<Integer, Integer> LOCAL_VARIABLES = new HashMap<>();

    private MiniStackVM() {
    }

    public final static MiniStackVM INSTANCE = new MiniStackVM();

    public Integer getLocalVariable(int index) {
        return LOCAL_VARIABLES.get(index);
    }

    /**
     * 执行指令
     */
    public void execute(int pc, String instruction) {
        String[] parts = instruction.split("_");
        String command = parts[0];

        // 通过反射调用对应的指令类，如果是 i 开头的指令，去掉 i 前缀
        if (command.startsWith("i")) {
            command = command.substring(1);
        }
        String className = command.substring(0, 1).toUpperCase() + command.substring(1) + "Instruction";
        try {
            // 为了便于测试和维护，指令类放在当前类的内部并通过反射调用
            Class<?> clazz = Class.forName(String.format("%s.%s$%s", this.getClass().getPackageName(), this.getClass().getSimpleName(), className));
            Instruction instructionInstance = (Instruction) clazz.getDeclaredConstructor().newInstance();
            instructionInstance.execute(pc, parts);
        } catch (Exception e) {
            System.out.printf("    %4d: [error] 执行 %s 指令失败: %s%n", pc, instruction, e.getMessage());
        }
    }

    public void reset() {
        INSTRUCTION_STACK.clear();
        LOCAL_VARIABLES.clear();
    }

    /**
     * 模拟栈式虚拟机的指令集
     * <p>
     * 该接口定义了一个方法，用于执行给定的指令。实现该接口的类可以根据具体的需求来执行不同的指令。
     * </p>
     */
    public interface Instruction {
        /**
         * 执行指令
         *
         * @param pc
         * @param parts 指令的各个部分
         */
        void execute(int pc, String[] parts);
    }

    /**
     * nop
     * 空指令，不执行任何操作
     */
    public static class NopInstruction implements Instruction {
        @Override
        public void execute(int pc, String[] parts) {
            System.out.printf("    %4d: [nop] 执行空指令%n", pc);
        }
    }

    /**
     * bipush
     * 将常量压入栈
     */
    public static class BipushInstruction implements Instruction {
        @Override
        public void execute(int pc, String[] parts) {
            int value = Integer.parseInt(parts[1]);
            INSTRUCTION_STACK.push(value);
            System.out.printf("    %4d: [bipush] 将常量 %d 压入栈%n", pc, value);
        }
    }

    /**
     * iconst
     * 将常量压入栈
     */
    public static class ConstInstruction implements Instruction {
        @Override
        public void execute(int pc, String[] parts) {
            int constant = Integer.parseInt(parts[1]);
            INSTRUCTION_STACK.push(constant);
            System.out.printf("    %4d: [iconst] 将常量 %s 压入栈%n", pc, constant);
        }
    }

    /**
     * istore
     * 将栈顶值存储到局部变量
     */
    public static class StoreInstruction implements Instruction {
        @Override
        public void execute(int pc, String[] parts) {
            int index = Integer.parseInt(parts[1]);
            int value = INSTRUCTION_STACK.pop();
            LOCAL_VARIABLES.put(index, value);
            System.out.printf("    %4d: [istore] 将栈顶值 %d 存储到局部变量 %d%n", pc, value, index);
        }
    }

    /**
     * iload
     * 将局部变量压入栈
     */
    public static class LoadInstruction implements Instruction {
        @Override
        public void execute(int pc, String[] parts) {
            int loadIndex = Integer.parseInt(parts[1]);
            Integer loadValue = LOCAL_VARIABLES.get(loadIndex);
            INSTRUCTION_STACK.push(loadValue);
            System.out.printf("    %4d: [iload] 将局部变量 " + loadIndex + " 的值 " + loadValue + " 压入栈%n", pc);
        }
    }

    /**
     * getstatic
     * 将静态变量压入栈
     */
    public static class GetstaticInstruction implements Instruction {
        @Override
        public void execute(int pc, String[] parts) {
            int index = Integer.parseInt(parts[1]);
            Integer value = (Integer) MiniMetaSpace.constantPool.getConstant(index);
            INSTRUCTION_STACK.push(value);
            System.out.printf("    %4d: [getstatic] 将静态变量 " + index + " 的值 " + value + " 压入栈%n", pc);
        }
    }

    /**
     * putstatic
     * 将栈顶值存储到静态变量
     */
    public static class PutstaticInstruction implements Instruction {
        @Override
        public void execute(int pc, String[] parts) {
            int index = Integer.parseInt(parts[1]);
            int value = INSTRUCTION_STACK.pop();
            MiniMetaSpace.constantPool.setConstant(index, value);
            System.out.printf("    %4d: [putstatic] 将栈顶值 %d 存储到静态变量 %d%n", pc, value, index);
        }
    }

    /**
     * iadd
     * 弹出栈顶两个值相加，结果压入栈
     */
    public static class AddInstruction implements Instruction {
        @Override
        public void execute(int pc, String[] parts) {
            int value1 = INSTRUCTION_STACK.pop();
            int value2 = INSTRUCTION_STACK.pop();
            int result = value1 + value2;
            INSTRUCTION_STACK.push(result);
            System.out.printf("    %4d: [iadd] 将栈顶两个值 " + value1 + " 和 " + value2 + " 相加，结果 " + result + " 压入栈%n", pc);
        }
    }

    /**
     * return
     * 返回指令，结束执行
     */
    public static class ReturnInstruction implements Instruction {
        @Override
        public void execute(int pc, String[] parts) {
            System.out.printf("    %4d: [return] 执行结束%n", pc);
        }
    }
}