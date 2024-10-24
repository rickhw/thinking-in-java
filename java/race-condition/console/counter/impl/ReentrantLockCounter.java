package pure.counter.impl;
import java.util.concurrent.locks.ReentrantLock;

import pure.counter.AbstractCounter;

public class ReentrantLockCounter extends AbstractCounter {

    private ReentrantLock locker = new ReentrantLock();

    public ReentrantLockCounter() {
        System.out.printf("Init counter, value: [%s]\n", this.getValue());
    }

    @Override
    public void run() {
        locker.lock();

        try {
            this.increment();
            this.decrement();
        } catch (Exception ex) {
        } finally {
            locker.unlock();    // 解鎖
        }

    }
}
