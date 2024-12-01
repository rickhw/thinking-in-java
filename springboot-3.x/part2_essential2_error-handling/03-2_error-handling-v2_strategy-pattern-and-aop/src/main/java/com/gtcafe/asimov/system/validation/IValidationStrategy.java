package com.gtcafe.asimov.system.validation;

public interface IValidationStrategy<T> {
    void validatePayload(T request);
    void validateQuota(T request);
    void validateAssociations(T request);
}