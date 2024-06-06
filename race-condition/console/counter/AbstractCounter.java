package pure.counter;

public abstract class AbstractCounter implements ICounter {

    protected int c = 0;

    public AbstractCounter() {
        System.out.printf("Init counter, value: [%s]\n", this.getValue());
    }

    public void increment() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        c++;

        System.out.printf("After increment: Thread: [%s], Value: [%s]\n", Thread.currentThread().getName(),  this.getValue());
    }

    public void decrement() {
        c--;
        System.out.printf("After decrement: Thread: [%s], Value: [%s]\n", Thread.currentThread().getName(),  this.getValue());
    }

    public int getValue() {
        return c;
    }

    public abstract void run();
    
}
