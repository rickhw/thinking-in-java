import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileInitializer {
    private String filePath;
    private long chunkSize;
    private String contentType;
    private String hashAlgorithm;

    public FileInitializer(String filePath, long chunkSize) {
        this.filePath = filePath;
        this.chunkSize = chunkSize;
    }

    public void initialize() throws IOException, NoSuchAlgorithmException {
        // 讀取檔案屬性
        File file = new File(filePath);
        long fileSize = file.length();
        contentType = "application/octet-stream"; // 預設為二進位檔案

        // 計算檔案的雜湊值
        hashAlgorithm = "MD5"; // 使用 MD5 雜湊算法
        String hashCode = calculateFileHash(file, hashAlgorithm);

        // 計算切割區塊數量
        int chunkCount = (int) Math.ceil((double) fileSize / chunkSize);

        // 封裝 payload
        FileInitiationPayload payload = new FileInitiationPayload(fileSize, contentType, hashCode, hashAlgorithm, chunkSize, chunkCount);

        // 發送初始化通訊
        sendInitializationRequest(payload);
    }

    private String calculateFileHash(File file, String algorithm) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        try (DigestInputStream dis = new DigestInputStream(new FileInputStream(file), digest)) {
            while (dis.read() != -1) ; // 讀取整個檔案以計算雜湊值
            digest = dis.getMessageDigest();
        }
        byte[] hashBytes = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte hashByte : hashBytes) {
            String hex = Integer.toHexString(0xff & hashByte);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private void sendInitializationRequest(FileInitiationPayload payload) {
        // 實際發送初始化請求至伺服器
        // 這裡應該是使用 HTTP 客戶端庫向伺服器發送 POST 請求
        // 然後處理伺服器的回應
        System.out.println("Sending initialization request with payload: " + payload);
    }
}
