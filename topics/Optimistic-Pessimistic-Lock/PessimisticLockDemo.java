public class PessimisticLockDemo {
    private static int stock = 100;

    public static void main(String[] args) {
        Runnable task = new PessimisticTask();

        for (int i = 0; i < 110; i++) {
            new Thread(task).start();
        }
    }

    static class PessimisticTask implements Runnable {
        public void run() {
            synchronized (PessimisticLockDemo.class) {
                if (stock > 0) {
                    try {
                        Thread.sleep((long) (Math.random() * 10));
                    } catch (InterruptedException ignored) {}
                    stock--;
                    System.out.println(Thread.currentThread().getName() + " 扣庫存成功，剩餘：" + stock);
                } else {
                    System.out.println(Thread.currentThread().getName() + " 沒有庫存了");
                }
            }
        }
    }
}
