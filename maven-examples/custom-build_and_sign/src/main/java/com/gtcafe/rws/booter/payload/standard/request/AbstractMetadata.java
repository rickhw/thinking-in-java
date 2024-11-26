package com.gtcafe.rws.booter.payload.standard.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public abstract class AbstractMetadata {

    @NotBlank(message = "[name] The city is required.")
    @Size(min = 3, max = 20, message = "The name must be from 3 to 20 characters.")
    private String name;

    @NotBlank(message = "[label] The city is required.")
    @Size(max = 50, message = "label max lenght is 50.")
    private String label;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
