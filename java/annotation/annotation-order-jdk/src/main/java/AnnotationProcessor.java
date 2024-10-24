import java.lang.reflect.Method;

public class AnnotationProcessor {

    // 處理方法注解的邏輯
    public void processAnnotations(Object obj, String methodName) throws Exception {
        Method method = obj.getClass().getMethod(methodName);

        // 檢查是否有 @DryRun 和 @Async 注解
        DryRun dryRun = method.getAnnotation(DryRun.class);
        Async async = method.getAnnotation(Async.class);

        // 如果有 @DryRun，優先執行 DryRun 邏輯，並忽略 @Async
        if (dryRun != null) {
            System.out.println("Executing in DryRun mode. Async will be ignored.");
            handleDryRun(method);
            return;
        }

        // 如果有 @Async，執行 Async 的邏輯
        if (async != null) {
            System.out.println("Executing in Async mode.");
            handleAsync(method, obj);
        } else {
            // 如果沒有注解，執行原始方法
            method.invoke(obj);
        }
    }

    private void handleDryRun(Method method) {
        // 模擬執行方法，不執行實際邏輯
        System.out.println("Simulating method execution for: " + method.getName());
    }

    private void handleAsync(Method method, Object obj) throws Exception {
        // 在這裡執行異步邏輯
        System.out.println("Executing asynchronously: " + method.getName());
        method.invoke(obj);  // 實際執行方法
    }
}
