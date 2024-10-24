public class Main {
    public static void main(String[] args) throws Exception {
        MyService myService = new MyService();
        AnnotationProcessor processor = new AnnotationProcessor();

        // 處理 myMethod 方法上的注解
        processor.processAnnotations(myService, "myMethod");
    }
}
