import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FullLoadCSV {
    public static void main(String[] args) {
        String filePath = "data.csv";
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            for (String line : lines) {
                // Process each line
                String[] data = line.split(",");
                // System.out.println("ID: " + data[0] + ", Name: " + data[1] + ", Value: " + data[2]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
