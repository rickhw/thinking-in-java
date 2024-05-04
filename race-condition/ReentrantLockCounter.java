import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockCounter implements ICounter {

    private int c = 0;

    private ReentrantLock locker = new ReentrantLock();

    public ReentrantLockCounter() {
        
        System.out.printf("Init counter, value: [%s]\n", this.getValue());
    }

    public void increment() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        c++;

        System.out.printf("After increment: Thread: [%s], Value: [%s]\n", Thread.currentThread().getName(),
                this.getValue());
    }

    public void decrement() {
        c--;
        System.out.printf("After decrement: Thread: [%s], Value: [%s]\n", Thread.currentThread().getName(),
                this.getValue());
    }

    public int getValue() {
        return c;
    }

    @Override
    public void run() {
        locker.lock();

        try {
            this.increment();
            this.decrement();
        } catch (Exception ex) {
            // ...
        } finally {
            // 解锁操作
            locker.unlock();
        }

    }
}
