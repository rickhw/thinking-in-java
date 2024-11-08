import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class StreamCSV {
    public static void main(String[] args) {
        String filePath = "data.csv";
        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            lines.forEach(line -> {
                String[] data = line.split(",");
                // System.out.println("ID: " + data[0] + ", Name: " + data[1] + ", Value: " + data[2]);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
