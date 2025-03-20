# newbie-mini-jvm

此项目将试图让工作多年的 Java 程序员通过自己动手写 Java 虚拟机的方式深入理解 JVM 原理。本人在开启这个项目时对于 JVM 也无深入了解，希望在不久的将来可以通过此种方式有所学习。

本人将主要依照 [尚硅谷宋红康 JVM 全套教程（详解 java 虚拟机）](https://www.bilibili.com/video/BV1PJ411n7xZ) 视频为基础，并结合 [《自己动手写 Java 虚拟机》](https://github.com/zxh0/jvmgo-book) 来逐步学习与完善。下面是一些编码约定，了解过后我们就可以开始动手了：

- 使用 Java 21 编写代码，JUnit 5 编写单元测试，Maven 作为构建工具
- 所有的核心类都以 `mini` 开头，表示这是一个迷你版的 JVM

## 一、前置知识准备

### 1. JVM 体系结构总览
> Java 是一种跨平台的编程语言，JVM 是 Java 的运行时环境，负责执行 Java 字节码。JVM 使得 Java 程序可以在不同的平台上运行，而不需要重新编译。
> JVM 实现有很多种，最常用的是 HotSpot JVM（Oracle JDK 和 OpenJDK 使用的 JVM），还有其他实现如 OpenJ9、GraalVM 等。

#### JVM 体系结构
- 类加载器（Class Loader）：负责加载 Java 类文件，并将其转换为 JVM 可以理解的格式。
- 运行时数据区（Runtime Data Area）：JVM 在运行时使用的内存区域，包括方法区、堆、虚拟机栈、本地方法栈和程序计数器。
- 执行引擎（Execution Engine）：负责执行字节码指令，包括解释器和即时编译器（JIT）。
- 本地接口（Native Interface）：用于与其他编程语言（如 C/C++）进行交互。
- 垃圾回收器（Garbage Collector）：负责自动管理内存，回收不再使用的对象。

### 2. 字节码执行引擎
> 把这一章节提前主要是因为我的个人兴趣，对能先实现这一步感到很开心（嗯，也因为这部分在这里只是先实现了指令的映射，相对简单），之后我们将会基于这个“字节码执行引擎”逐步完善我们的 JVM。

我们编写了一个简单的 Java 程序，源码见：[HelloStackVM.java](./src/main/java/demo/HelloStackVM.java) （未来我们的目标就是用自己的 JVM 来执行这个 Java 程序。）

然后我们通过 `javac` 和 `javap` 命令来查看编译后的字节码指令。

```bash
$ javac ./src/main/java/demo/HelloStackVM.java
$ javap -c ./src/main/java/demo/HelloStackVM.class
```

<details>
<summary>点击展开查看详情</summary>

通过 `javap` 我们可以看到 `HelloStackVM` 类的字节码指令集，我们重点关注 `main` 方法的字节码指令集 ，下面以表格的形式对应字节码指令和
Java 源码的关系：

| Java 源码                | 字节码指令                                            |
| ------------------------ | ----------------------------------------------------- |
| `int a = 3;`             | `iconst_3` `istore_1`                                 |
| `int b = 4;`             | `iconst_4` `istore_2`                                 |
| `int c = a + b;`         | `iload_1` `iload_2` `iadd` `istore_3`                 |
| `System.out.println(c);` | `getstatic` `iload_3` `invokedynamic` `invokevirtual` |
| `return;`                | `return`                                              |

我们来分析一下字节码指令（只需要了解大致意思即可）：

- `iconst_3`：将整数常量 3 压入操作数栈。
- `istore_1`：将操作数栈顶的整数值存入局部变量表的第 1 个位置。
- `iload_1`：将局部变量表第 1 个位置的整数值压入操作数栈。
- `iadd`：将操作数栈顶的两个整数值相加，并将结果压入操作数栈。
- `istore_3`：将操作数栈顶的整数值存入局部变量表的第 3 个位置。
- `getstatic`：获取静态字段的值，并将其压入操作数栈。
- `invokedynamic`：动态调用方法。
- `invokevirtual`：调用实例方法。
- `return`：返回。
</details>

由于 JVM 使用的指令集架构是`栈的指令集架构`（还有一种是`寄存器的指令集架构`），所以在开始之前，我们需要先来了解一种数据结构：栈。

<details>
<summary>点击展开查看详情</summary>

#### 栈（Stack）

栈是一种后进先出（LIFO）的数据结构，具有以下特点：

- 只能在栈顶进行插入和删除操作。
- 栈顶元素是最后插入的元素，栈底元素是最先插入的元素。

有两种常见的栈实现方式：

| 实现方式 | 关键说明                               | 优点                   | 缺点           |
| -------- | -------------------------------------- | ---------------------- | -------------- |
| 数组     | 通过索引直接访问元素，无需遍历链表节点 | 内存连续，CPU 缓存友好 | 扩容需复制数据 |
| 链表     | 动态分配内存，插入/删除效率高          | 无固定大小限制         | 内存碎片化     |

我们使用链表来实现栈，链表的每个节点包含一个数据域和一个指向下一个节点的指针。源码见：[MiniStack.java](src/main/java/mini/data/structure/MiniStack.java)

</details>

为什么要使用栈呢？因为 Java 虚拟机的字节码指令集是基于栈的，所有的操作都是在操作数栈上进行的。

#### 基于栈的虚拟机

接下来我们来实现一个可以执行字节码指令的虚拟机，源码见：[MiniExecutionEngine.java](./src/main/java/mini/MiniExecutionEngine.java)

另外我们还在单元测试中模拟了 `HelloStackVM` 类的 `main`
方法的字节码指令集，来验证我们实现的虚拟机是否正确。源码见：[MiniExecutionEngine.java](./src/test/java/mini/MiniExecutionEngine.java)

## 二、类加载器

类加载器是 Java 虚拟机的核心组件之一，负责将 Java 类文件加载到内存中，并将其转换为 JVM 可以理解的格式。

## 三、执行引擎

实现字节码指令的执行引擎，支持基本的算术运算、类型转换、控制流等指令。

## 四、运行时数据区

- 程序计数器（线程私有，无OOM）
- 堆（线程共享，Old-2/3:Young-1/3）
    - 老年代
    - 新生代（Eden-8:S0-1:S1-1）
        - Eden（对象优先分配区）
        - Survivor 0（From）
        - Survivor 1（To）
- 元空间（JDK 8+，本地内存）
    - 类元数据（方法、字段、父类等）
    - 运行时常量池（符号引用、字面量）
- JVM 栈（线程私有）
    - 栈帧
        - 本地变量表
        - 操作数栈
        - 动态链接
        - 方法返回地址
        - 附加信息
- 本地方法栈（线程私有，Native方法）

## 五、垃圾回收

实现垃圾回收机制，包括标记-清除算法和分代回收策略。

## 六、JVM 参数

- -Xms：初始堆大小
- -Xmx：最大堆大小
- -Xss：每个线程的栈大小
- -XX:+UseSerialGC：使用串行垃圾回收器
- -XX:+UseParallelGC：使用并行垃圾回收器
- -XX:+UseG1GC：使用 G1 垃圾回收器
- -XX:+UseConcMarkSweepGC：使用 CMS 垃圾回收器
- -XX:+UseZGC：使用 ZGC 垃圾回收器