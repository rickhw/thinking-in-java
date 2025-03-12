import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newBuilder().build();
        for (int i = 0; i < 1000; i++) {
            var thread = Thread.ofVirtual().start(() -> {
                HttpRequest request = HttpRequest.newBuilder(URI.create("http://example.com")).build();
                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    System.out.println(response.body());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}