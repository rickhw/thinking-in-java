public class Main {

    static int COUNT = 10;
    static int STEP = 1000;

    static int tempo = 60;

    public static void main(String[] args) {
        for (int i = 0; i < COUNT; i++) {
            try {
                System.out.println("click: " + 1);
                Thread.sleep(STEP);
            } catch (Exception ex) {
            }
        }
    }
}

public class TimeSignature {
    private int FULL_NOTE = 4;
    private int BEATS_PER_MEASURE = 4;

    


}






