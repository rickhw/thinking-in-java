import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.io.IOException;

public class FileUploadClient {

    public static void main(String[] args) throws IOException {
        String filePath = "path/to/your/file"; // Replace with the path to your file

        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://localhost:8080/upload");

        File file = new File(filePath);

        HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName())
                .build();

        httpPost.setEntity(entity);

        HttpResponse response = httpClient.execute(httpPost);

        System.out.println("Response status: " + response.getStatusLine());
    }
}
