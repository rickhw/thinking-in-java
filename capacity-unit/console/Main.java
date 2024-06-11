
import capacityUnit.ICapacityUnit;
import capacityUnit.impl.*;

public class Main {

    static int sleep = 2000;
    static int opsCount = 10;

    public static void main(String args[]) {
        // ICapacityUnit cu = new NormalCapacityUnit();
        // ICapacityUnit cu = new NativeCapacityUnit();
        ICapacityUnit cu = new ReentrantLockCapacityUnit();

        for (int i = 0; i < opsCount; i++) {
            int value = (int) (Math.random() * 100);
            ClientThread t = new ClientThread(cu, "T" + i, value);
            t.start();
        }

        // expect result: 0

        try {
            Thread.sleep(sleep);
            System.out.printf("value: %s\n", cu.getCapacityUnit());
        } catch (Exception e) {

        }
    }
}

class ClientThread extends Thread {

    private ICapacityUnit cu;
    private int value;

    ClientThread(ICapacityUnit cu, String name, int value) {
        this.cu = cu;
        this.value = value;
        this.setName(name);
    }

    @Override
    public void run() {
        cu.operate(value);
    }

}