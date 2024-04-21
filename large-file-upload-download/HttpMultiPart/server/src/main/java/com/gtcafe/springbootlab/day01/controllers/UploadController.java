import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


@RestController
@RequestMapping("/upload")
class FileUploadController {

    private static final String UPLOAD_DIRECTORY = "./upload";
    private static final int BUFFER_SIZE = 8192; // 8KB buffer size

    @PostMapping
    public ResponseEntity<String> handleFileUpload(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            File directory = new File(UPLOAD_DIRECTORY);
            if (!directory.exists()) {
                directory.mkdir();
            }

            File newFile = new File(directory.getAbsolutePath() + "/" + file.getOriginalFilename());
            try (InputStream inputStream = file.getInputStream();
                 BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(newFile))) {

                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            return ResponseEntity.ok("File uploaded successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file");
        }
    }
}
