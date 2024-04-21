import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class FileUploadClient {
    private static final String DEFAULT_CONFIG_FILE = "config.properties";
    private static final long DEFAULT_CHUNK_SIZE = 500 * 1024; // 500KB
    private static final int DEFAULT_CONCURRENT_UPLOADS = 1;
    private static final int DEFAULT_MAX_TIME = 86400; // 一天的秒數

    public static void main(String[] args) {
        // 讀取參數
        String filePath = parseArguments(args);
        if (filePath == null) {
            // 如果未指定路徑參數，從配置文件中讀取
            filePath = getConfigProperty("file.path", DEFAULT_CONFIG_FILE, ".");
        }

        // 讀取配置並設置其他參數
        String chunkSizeStr = getConfigProperty("file.chunkSize", DEFAULT_CONFIG_FILE, Long.toString(DEFAULT_CHUNK_SIZE));
        long chunkSize = parseSize(chunkSizeStr);

        String concurrentUploadsStr = getConfigProperty("file.concurrentUploads", DEFAULT_CONFIG_FILE, Integer.toString(DEFAULT_CONCURRENT_UPLOADS));
        int concurrentUploads = Integer.parseInt(concurrentUploadsStr);

        String maxTimeStr = getConfigProperty("file.maxTime", DEFAULT_CONFIG_FILE, Integer.toString(DEFAULT_MAX_TIME));
        int maxTime = Integer.parseInt(maxTimeStr);

        // 1. Initializer
        try {
            FileInitializer fileInitializer = new FileInitializer(filePath, chunkSize);
            fileInitializer.initialize();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private static String parseArguments(String[] args) {
        if (args.length > 0) {
            return args[0];
        }
        return null;
    }

    private static String getConfigProperty(String key, String configFile, String defaultValue) {
        Properties properties = new Properties();
        try {
            FileInputStream fileInputStream = new FileInputStream(configFile);
            properties.load(fileInputStream);
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties.getProperty(key, defaultValue);
    }

    private static long parseSize(String sizeStr) {
        // 將 KB 或 MB 轉換為位元組
        long size = Long.parseLong(sizeStr.replaceAll("\\D+", ""));
        if (sizeStr.toLowerCase().contains("kb")) {
            return size * 1024;
        } else if (sizeStr.toLowerCase().contains("mb")) {
            return size * 1024 * 1024;
        }
        return size;
    }
}
