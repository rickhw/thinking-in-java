public interface ICounter extends Runnable {

    public void increment();
    public void decrement();
    public int getValue();
    public void run();
}
