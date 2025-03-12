import java.lang.reflect.Method;

public class AnnotationProcessor {

    public void processAnnotations(Object obj, String methodName) throws Exception {
        Method method = obj.getClass().getMethod(methodName);

        ExecMode execMode = method.getAnnotation(ExecMode.class);

        // 根据 @ExecMode 的值决定执行模式
        if (execMode != null && execMode.mode() == ExecMode.Mode.ASYNC) {
            System.out.println("Executing asynchronously.");
            handleAsync(method, obj);
        } else {
            System.out.println("Executing synchronously.");
            method.invoke(obj);  // 同步执行
        }
    }


    private void handleAsync(Method method, Object obj) throws Exception {
        // 异步执行
        System.out.println("Executing asynchronously: " + method.getName());
        new Thread(() -> {
            try {
                method.invoke(obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
