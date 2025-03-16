package mini.cl;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class MiniStackFrame {
    @Getter
    private final MiniClass clazz;
    @Getter
    private final String methodName;
    /**
     * 局部变量表
     */
    @Getter
    private final Map<Integer, Integer> localVariables = new HashMap<>();
}
