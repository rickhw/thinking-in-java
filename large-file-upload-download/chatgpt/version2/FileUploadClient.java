import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class FileUploadClient {
    public static void main(String[] args) throws IOException {
        // 文件路径
        String filePath = "path/to/your/file";
        // 服务器地址
        String serverUrl = "http://example.com/upload";
        // Chunk大小
        int chunkSize = 1024 * 1024; // 1MB

        // 读取文件
        Path file = Path.of(filePath);
        byte[] fileBytes = Files.readAllBytes(file);
        int fileSize = fileBytes.length;

        // 计算MD5
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        md5.update(fileBytes);
        byte[] md5Bytes = md5.digest();
        String md5Hex = javax.xml.bind.DatatypeConverter.printHexBinary(md5Bytes);

        // 设置压缩参数
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);

        // 初始化连接
        URL url = new URL(serverUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        // 发送初始请求
        String initialRequest = String.format("{\"filename\": \"%s\", \"md5\": \"%s\", \"fileSize\": %d, \"compression\": \"deflate\", \"chunkSize\": %d}", file.getFileName(), md5Hex, fileSize, chunkSize);
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write(initialRequest);
        writer.flush();

        // 接收服务器响应
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String response = reader.readLine();
        if (!response.equals("Ready")) {
            System.out.println("Server is not ready.");
            return;
        }

        // 获取并发数量
        int concurrent = Integer.parseInt(reader.readLine());

        // 分片并发送
        int start = 0;
        while (start < fileSize) {
            int end = Math.min(start + chunkSize, fileSize);
            byte[] chunk = new byte[end - start];
            System.arraycopy(fileBytes, start, chunk, 0, end - start);

            ByteArrayOutputStream compressedStream = new ByteArrayOutputStream();
            DeflaterOutputStream deflaterStream = new DeflaterOutputStream(compressedStream, deflater);
            deflaterStream.write(chunk);
            deflaterStream.close();
            byte[] compressedChunk = compressedStream.toByteArray();

            // 发送chunk
            // 这里应该使用多线程或异步机制以实现并发上传
            sendChunkToServer(compressedChunk);

            start += chunkSize;
        }

        // 发送完成信号
        sendCompletionSignalToServer();
    }

    private static void sendChunkToServer(byte[] chunk) {
        // 实现向服务器发送chunk的代码
    }

    private static void sendCompletionSignalToServer() {
        // 实现向服务器发送完成信号的代码
    }
}
