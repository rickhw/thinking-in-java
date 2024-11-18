package com.gtcafe.asimov.system.validation;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class ValidationStrategyFactory {
    
    private final Map<Class<?>, ValidationStrategy<?>> strategies;
    
    public ValidationStrategyFactory(List<ValidationStrategy<?>> strategies) {
        this.strategies = strategies.stream()
            .collect(Collectors.toMap(
                strategy -> getStrategyType(strategy.getClass()),
                strategy -> strategy
            ));
    }
    
    @SuppressWarnings("unchecked")
    public <T> ValidationStrategy<T> getStrategy(Class<T> requestType) {
        ValidationStrategy<?> strategy = strategies.get(requestType);
        if (strategy == null) {
            throw new IllegalArgumentException("No validation strategy found for type: " + requestType);
        }
        return (ValidationStrategy<T>) strategy;
    }
    
    private Class<?> getStrategyType(Class<?> strategyClass) {
        return Arrays.stream(strategyClass.getGenericInterfaces())
            .filter(type -> type instanceof ParameterizedType)
            .map(type -> (ParameterizedType) type)
            .filter(type -> type.getRawType().equals(ValidationStrategy.class))
            .map(type -> (Class<?>) type.getActualTypeArguments()[0])
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Cannot determine strategy type"));
    }
}