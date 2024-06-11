package capacityUnit.impl;

import capacityUnit.AbstractCapacityUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockCapacityUnit extends AbstractCapacityUnit {

    private ReentrantLock locker = new ReentrantLock();

    @Override
    public void operate(int value) {
        locker.lock();

        try {
            _currentCapacityUnit += value;
        } catch (Exception ex) {
        } finally {
            locker.unlock();
        }
    }
}
