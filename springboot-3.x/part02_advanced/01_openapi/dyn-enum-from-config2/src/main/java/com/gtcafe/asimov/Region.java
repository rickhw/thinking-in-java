package com.gtcafe.asimov;

public enum Region {
    // 列舉保持基本結構，具體內容將由配置決定
    UNKNOWN;

    private String displayName;
    private String endpoint;

    // Getter 方法
    public String getDisplayName() {
        return displayName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    // 在載入後可以由外部設置
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}