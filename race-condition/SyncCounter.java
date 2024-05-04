public class SyncCounter implements ICounter {

    private int c = 0;

    public SyncCounter() {
        System.out.printf("Init counter, value: [%s]\n", this.getValue());
    }

    public void increment() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Auto-generated catch block
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

    @Override
    public void run() {
        synchronized(this) {
            this.increment();
            this.decrement();
    
        }
    }
}
