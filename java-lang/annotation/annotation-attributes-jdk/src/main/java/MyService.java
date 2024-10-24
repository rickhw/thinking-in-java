public class MyService {

    @ExecMode(mode = ExecMode.Mode.ASYNC)
    public void myMethodAsync() {
        System.out.println("Original method execution");
    }

    @ExecMode(mode = ExecMode.Mode.SYNC)  // 明确声明同步执行
    public void myMethodAync() {
        System.out.println("Another method execution");
    }

    @ExecMode // default sync mode.
    public void myMethodDefault() {
        System.out.println("default method execution");
    }

}
