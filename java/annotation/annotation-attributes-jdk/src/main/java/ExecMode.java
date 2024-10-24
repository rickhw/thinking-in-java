import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExecMode {

    enum Mode {
        SYNC, ASYNC
    }

    // 默认为同步模式
    Mode mode() default Mode.SYNC;
}
