import java.util.concurrent.atomic.AtomicInteger;

public class OptimisticLockDemo {
    private static int stock = 100;
    private static final AtomicInteger version = new AtomicInteger(0);

    public static void main(String[] args) {
        Runnable task = new OptimisticTask();

        for (int i = 0; i < 110; i++) {
            new Thread(task).start();
        }
    }

    static class OptimisticTask implements Runnable {
        public void run() {
            int retries = 5;
            while (retries-- > 0) {
                int currentVersion = version.get();
                if (stock > 0) {
                    try {
                        Thread.sleep((long) (Math.random() * 10));
                    } catch (InterruptedException ignored) {}
                    if (version.compareAndSet(currentVersion, currentVersion + 1)) {
                        stock--;
                        System.out.println(Thread.currentThread().getName() + " 成功扣庫存，剩餘：" + stock);
                        break;
                    } else {
                        System.out.println(Thread.currentThread().getName() + " 發現版本衝突，重試...");
                    }
                }
            }
        }
    }
}
