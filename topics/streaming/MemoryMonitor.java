public class MemoryMonitor {

    public static void main(String[] args) throws InterruptedException {
        // Memory monitoring in a separate thread
        Thread monitor = new Thread(() -> {
            Runtime runtime = Runtime.getRuntime();
            while (true) {
                long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
                System.out.println("Used Memory (MB): " + usedMemory);
                try {
                    Thread.sleep(1000); // Check memory every second
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        monitor.setDaemon(true);
        monitor.start();

        // Run either FullLoadCSV or StreamCSV to observe memory usage
        FullLoadCSV.main(null); // or 
        // StreamCSV.main(null);
    }
}
