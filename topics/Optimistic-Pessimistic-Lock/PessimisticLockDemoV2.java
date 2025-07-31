import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class PessimisticLockDemoV2 {
    public static void main(String[] args) {
        Inventory inventory = new Inventory("悲觀資源", 100);
        Runnable task = new PessimisticTask(inventory);

        for (int i = 0; i < 110; i++) {
            new Thread(task).start();
        }

        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        inventory.printSummary();
    }

    static class Inventory {
        private final String name;
        private int stock;
        private final AtomicInteger success = new AtomicInteger(0);
        private final AtomicInteger failure = new AtomicInteger(0);

        public Inventory(String name, int stock) {
            this.name = name;
            this.stock = stock;
        }

        public void decrementWithLock() {
            synchronized (this) {
                if (stock > 0) {
                    simulateDelay();
                    stock--;
                    success.incrementAndGet();
                    log("成功扣庫存，剩餘：" + stock);
                } else {
                    failure.incrementAndGet();
                    log("沒有庫存了");
                }
            }
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

    static class PessimisticTask implements Runnable {
        private final Inventory inventory;

        public PessimisticTask(Inventory inventory) {
            this.inventory = inventory;
        }

        public void run() {
            inventory.decrementWithLock();
        }
    }
}
