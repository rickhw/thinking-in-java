package pure.counter.impl;

import pure.counter.AbstractCounter;

public class SyncCounter extends AbstractCounter {

    public SyncCounter() {
        System.out.printf("Init counter, value: [%s]\n", this.getValue());
    }

    @Override
    public void run() {
        synchronized(this) {
            this.increment();
            this.decrement();
        }
    }
}
