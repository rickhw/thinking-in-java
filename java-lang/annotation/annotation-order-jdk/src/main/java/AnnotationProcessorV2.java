import java.lang.reflect.Method;

public class AnnotationProcessorV2 {

    public void processAnnotations(Object obj, String methodName) throws Exception {
        Method method = obj.getClass().getMethod(methodName);

        // 检查是否有 @DryRun 和 @ExecMode 注解
        DryRun dryRun = method.getAnnotation(DryRun.class);
        ExecMode execMode = method.getAnnotation(ExecMode.class);

        // 如果有 @DryRun，优先执行 DryRun 逻辑
        if (dryRun != null) {
            System.out.println("Executing in DryRun mode. Any execution mode will be ignored.");
            handleDryRun(method);
            return;
        }

        // 根据 @ExecMode 的值决定执行模式
        if (execMode != null && execMode.mode() == ExecMode.Mode.ASYNC) {
            System.out.println("Executing asynchronously.");
            handleAsync(method, obj);
        } else {
            System.out.println("Executing synchronously.");
            method.invoke(obj);  // 同步执行
        }
    }

    private void handleDryRun(Method method) {
        // 模拟执行
        System.out.println("Simulating method execution for: " + method.getName());
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
