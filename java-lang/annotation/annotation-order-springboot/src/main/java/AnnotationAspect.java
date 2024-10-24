import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
// import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

@Aspect
// @Component
public class AnnotationAspect {

    @Around("@annotation(async) || @annotation(dryRun)")
    public Object handleAnnotations(ProceedingJoinPoint joinPoint, Async async, DryRun dryRun) throws Throwable {
        // 如果 DryRun 存在，優先執行 DryRun 的邏輯，並忽略 Async
        if (dryRun != null) {
            System.out.println("Executing in DryRun mode. Async will be ignored.");
            return handleDryRun(joinPoint); // 處理 DryRun 的邏輯
        }

        // 如果 DryRun 不存在且 Async 存在，執行 Async 的邏輯
        if (async != null) {
            System.out.println("Executing in Async mode.");
            return handleAsync(joinPoint); // 處理 Async 的邏輯
        }

        // 如果兩者都不存在，執行原始方法
        return joinPoint.proceed();
    }

    private Object handleDryRun(ProceedingJoinPoint joinPoint) {
        // DryRun 的邏輯，這裡可以模擬實際執行而不真正執行方法
        System.out.println("Simulating method execution for: " + joinPoint.getSignature().getName());
        return null;  // 模擬結果
    }

    private Object handleAsync(ProceedingJoinPoint joinPoint) throws Throwable {
        // Async 的邏輯，這裡可以執行異步調用
        System.out.println("Executing asynchronously: " + joinPoint.getSignature().getName());
        // 實際執行方法
        return joinPoint.proceed();
    }
}
