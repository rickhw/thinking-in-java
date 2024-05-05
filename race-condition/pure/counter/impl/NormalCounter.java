package pure.counter.impl;

import pure.counter.AbstractCounter;

public class NormalCounter extends AbstractCounter {

    public NormalCounter() {
        System.out.printf("Init counter, value: [%s]\n", this.getValue());
    }

    @Override
    public void run() {
        this.increment();
        this.decrement();
    }
}
