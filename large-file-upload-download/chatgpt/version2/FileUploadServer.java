import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.zip.DeflaterInputStream;

public class FileUploadServer {
    public static void main(String[] args) throws IOException {
        // 服务器端口
        int port = 8080;
        // 存储路径
        String storagePath = "path/to/your/storage";

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server is running on port " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress().getHostName());

            // 处理客户端请求
            new Thread(() -> {
                try {
                    handleClientRequest(clientSocket, storagePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private static void handleClientRequest(Socket clientSocket, String storagePath) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

        // 读取客户端请求
        String request = reader.readLine();
        System.out.println("Received request from client: " + request);

        // 解析请求
        // 这里需要根据实际情况解析客户端发送的请求内容
        // 然后根据请求内容做相应的处理
        // 比如解析文件名、MD5、文件大小、压缩算法、chunked数量等信息
        // 然后根据chunked数量循环接收并保存文件片段，进行重组和验证

        // 发送准备就绪响应
        writer.write("Ready\n");
        writer.flush();

        // 接收文件片段并保存
        receiveAndSaveChunks(reader, storagePath);

        // 发送完成信号
        // 这里可以根据需要实现对客户端的确认机制
        writer.write("Complete\n");
        writer.flush();

        clientSocket.close();
    }

    private static void receiveAndSaveChunks(BufferedReader reader, String storagePath) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            // 解析并处理文件片段数据
            // 这里需要根据实际情况处理接收到的文件片段数据
            // 可以将片段数据保存到临时文件中，也可以直接保存到最终文件中
            // 例如，可以将接收到的片段数据写入文件并追加到之前的数据后面
        }
    }
}
