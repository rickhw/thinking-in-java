import java.util.concurrent.atomic.AtomicInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OptimisticLockDemoV2 {
    public static void main(String[] args) {
        Inventory inventory = new Inventory("樂觀資源", 100);
        Runnable task = new OptimisticTask(inventory);

        for (int i = 0; i < 110; i++) {
            new Thread(task).start();
        }

        // 等待執行緒完成 (簡化做法)
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        inventory.printSummary();
    }

    static class Inventory {
        private final String name;
        private int stock;
        private final AtomicInteger version = new AtomicInteger(0);
        private final AtomicInteger success = new AtomicInteger(0);
        private final AtomicInteger failure = new AtomicInteger(0);

        public Inventory(String name, int stock) {
            this.name = name;
            this.stock = stock;
        }

        public boolean tryDecrement() {
            int retries = 5;
            while (retries-- > 0) {
                int currentVersion = version.get();
                if (stock > 0) {
                    simulateDelay();
                    if (version.compareAndSet(currentVersion, currentVersion + 1)) {
                        stock--;
                        success.incrementAndGet();
                        log("成功扣庫存，剩餘：" + stock);
                        return true;
                    } else {
                        log("版本衝突，重試...");
                    }
                }
            }
            failure.incrementAndGet();
            return false;
        }

        public void printSummary() {
            System.out.println("\n===== 結果統計 for " + name + " =====");
            System.out.println("成功次數：" + success.get());
            System.out.println("失敗次數：" + failure.get());
            System.out.println("剩餘庫存：" + stock);
        }

        private void log(String message) {
            String timestamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
            System.out.println("[" + timestamp + "] [" + Thread.currentThread().getName() + "] " + message);
        }

        private void simulateDelay() {
            try { Thread.sleep((long)(Math.random() * 10)); } catch (InterruptedException ignored) {}
        }
    }

    static class OptimisticTask implements Runnable {
        private final Inventory inventory;

        public OptimisticTask(Inventory inventory) {
            this.inventory = inventory;
        }

        public void run() {
            inventory.tryDecrement();
        }
    }
}
