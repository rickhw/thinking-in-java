package capacityUnit;

public abstract class AbstractCapacityUnit implements ICapacityUnit {

    protected int _currentCapacityUnit = 0;
    protected int _initCapacityUnit = 0;

    public AbstractCapacityUnit() {
        System.out.printf("Init counter, value: [%s]\n", this.getCapacityUnit());
    }

    public void setMaxCapacityUnit(int cu) {
        this._initCapacityUnit = cu;
    }

    public int getCapacityUnit() {
        return _currentCapacityUnit;
    }

    public void reset() {
        _currentCapacityUnit = _initCapacityUnit;
    }

    public abstract void operate(int value) ;
}
