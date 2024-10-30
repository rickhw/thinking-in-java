
public class MyService {

    @DryRun
    @Async
    public void myMethod() {
        System.out.println("Original method execution");
    }

    @DryRun
    @ExecMode(mode = ExecMode.Mode.ASYNC)
    public void myMethodAsync() {
        System.out.println("Original method execution");
    }

    @ExecMode(mode = ExecMode.Mode.SYNC)  // 明确声明同步执行
    public void anotherMethod() {
        System.out.println("Another method execution");
    }
}
