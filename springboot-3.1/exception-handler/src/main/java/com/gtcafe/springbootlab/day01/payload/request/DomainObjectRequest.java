package com.gtcafe.springbootlab.day01.payload.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DomainObjectRequest {

	@NotBlank(message = "The kind is required.")
    @Size(max = 10, message = "kind max lenght is 10.")
    private String kind;

    @Valid
    @NotNull(message = "The metadata is required.")
    private Metadata metadata;

    // @Valid
    // @NotNull(message = "The spec is required.")
    private Spec spec;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public Spec getSpec() {
        return spec;
    }

    public void setSpec(Spec spec) {
        this.spec = spec;
    }

}
