package capacityUnit.impl;

import capacityUnit.AbstractCapacityUnit;

public class NormalCapacityUnit extends AbstractCapacityUnit {

    @Override
    public void operate(int value) {
        _currentCapacityUnit += value;
    }

}
