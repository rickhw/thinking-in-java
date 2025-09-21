import java.lang.ScopedValue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScopedUserExample {
    static final ScopedValue<String> USER = ScopedValue.newInstance();

    public static void main(String[] args) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> ScopedValue.where(USER, "Alice").run(() -> {
                System.out.println("Thread: " + Thread.currentThread());
                System.out.println("User: " + USER.get());
            }));

            executor.submit(() -> ScopedValue.where(USER, "Bob").run(() -> {
                System.out.println("Thread: " + Thread.currentThread());
                System.out.println("User: " + USER.get());
            }));

            // Optional delay to ensure output appears before main exits
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
