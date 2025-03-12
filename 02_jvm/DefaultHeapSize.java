public class DefaultHeapSize {
    public static void main(String[] args) {
        long initialHeapSize = Runtime.getRuntime().totalMemory();
        long maxHeapSize = Runtime.getRuntime().maxMemory();
        System.out.println("Initial Heap Size (-Xms): " + initialHeapSize / (1024 * 1024) + " MB");
        System.out.println("Max Heap Size (-Xmx): " + maxHeapSize / (1024 * 1024) + " MB");
    }
}

