package capacityUnit.impl;

import capacityUnit.AbstractCapacityUnit;

public class NativeCapacityUnit extends AbstractCapacityUnit {

    @Override
    public void operate(int value) {
        synchronized(this) {
            _currentCapacityUnit += value;
        }
    }
}
