public class FileInitiationPayload {
    private long fileSize;
    private String contentType;
    private String hashCode;
    private String hashAlgorithm;
    private long chunkSize;
    private int chunkCount;

    public FileInitiationPayload(long fileSize, String contentType, String hashCode, String hashAlgorithm, long chunkSize, int chunkCount) {
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.hashCode = hashCode;
        this.hashAlgorithm = hashAlgorithm;
        this.chunkSize = chunkSize;
        this.chunkCount = chunkCount;
    }

    // Getters and setters
    // 如果需要的話
}
