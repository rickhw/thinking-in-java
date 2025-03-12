package com.gtcafe.asimov;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Configuration
public class AppConfig {
    @Value("${app.timezone.default:UTC}")
    private String defaultTimeZone;

    @Value("${app.timeformat.default:ISO8601}")
    private String defaultTimeFormat;

    public ZoneId getDefaultZoneId() {
        return ZoneId.of(defaultTimeZone);
    }

    public DateTimeFormatter getDefaultDateTimeFormatter() {
        return switch (defaultTimeFormat) {
            case "ISO8601" -> DateTimeFormatter.ISO_DATE_TIME;
            // 可以在這裡添加更多的時間格式
            default -> DateTimeFormatter.ISO_DATE_TIME;
        };
    }
}