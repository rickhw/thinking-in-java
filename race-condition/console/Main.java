package pure;

import pure.counter.ICounter;
import pure.counter.impl.*;

public class Main {

    static int sleep = 2000;
    static int opsCount = 100;

    public static void main(String args[]) {
        ICounter counter = new NormalCounter();
        // ICounter counter = new SyncCounter();
        // ICounter counter = new ReentrantLockCounter();

        for(int i=0; i<opsCount;i ++) {
            Thread t = new Thread(counter, "T" + i);
            t.start();
        }
        
        // expect result: 0

        try {
            Thread.sleep(sleep);
            System.out.printf("value: %s\n", counter.getValue());
        } catch (Exception e) {

        }
    }
}
