
public class MyService {

    @DryRun
    @Async
    public void myMethod() {
        System.out.println("Original method execution");
    }

    public static void main(String[] args) {
        new MyService().myMethod();
    }
}
