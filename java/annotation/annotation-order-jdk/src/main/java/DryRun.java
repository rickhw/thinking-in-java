import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 定義 @DryRun 注解
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DryRun {
}

