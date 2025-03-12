package com.gtcafe.asimov.utils;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

@Slf4j
@Service
public class JsonUtils {

    private final ObjectMapper objectMapper;

    public JsonUtils() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 將 JSON 字串轉換為物件
     * @param jsonString JSON 字串
     * @param clazz 目標類別
     * @return Optional 包裝的目標物件
     */
    public <T> Optional<T> jsonStringToModelSafe(String jsonString, Class<T> clazz) {
        if (jsonString == null) {
            return Optional.empty();
        }
        
        try {
            T result = objectMapper.readValue(jsonString, clazz);
            return Optional.ofNullable(result);
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON string to {}: {}", clazz.getSimpleName(), e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 將 JSON 字串轉換為物件（保留原有方法以維持相容性）
     * @deprecated 建議使用 jsonStringToModelSafe 方法
     */
    @Deprecated
    public <T> T jsonStringToModel(String jsonString, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON string to {}: {}", clazz.getSimpleName(), e.getMessage());
            return null;
        }
    }

    /**
     * 將物件轉換為 JSON 字串
     * @param object 要轉換的物件
     * @return Optional 包裝的 JSON 字串
     */
    public Optional<String> modelToJsonStringSafe(Object object) {
        if (object == null) {
            return Optional.empty();
        }
        
        try {
            String result = objectMapper.writeValueAsString(object);
            return Optional.ofNullable(result);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to JSON string: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 將物件轉換為 JSON 字串（保留原有方法以維持相容性）
     * @deprecated 建議使用 modelToJsonStringSafe 方法
     */
    @Deprecated
    public String modelToJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to JSON string: {}", e.getMessage());
            return null;
        }
    }
}