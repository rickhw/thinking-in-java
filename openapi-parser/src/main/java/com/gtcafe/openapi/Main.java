package com.gtcafe.openapi;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

public class Main {
    public static void main(String[] args) {

        // or from a file
        SwaggerParseResult result = new OpenAPIParser().readLocation("spec/openapi.yaml", null, null);

        if (result.getMessages() != null)
            result.getMessages().forEach(System.err::println); // validation errors and warnings

        // the parsed POJO
        OpenAPI openAPI = result.getOpenAPI();

        if (openAPI != null) {
            System.out.println(openAPI.getInfo().getTitle());
            // openAPI.getComponents().getSchemas().forEach((k, v) -> System.out.println(k));

            processPaths(openAPI);

            processComponents(openAPI);
        }
    }

    private static void processComponents(OpenAPI openAPI) {
        System.out.println("## Components/Schemas");
        // openAPI.getComponents().getSchemas().forEach((k, v) -> System.out.println(k));

        for ( String key : openAPI.getComponents().getSchemas().keySet()) {
            Schema s = openAPI.getComponents().getSchemas().get(key);
            System.out.println(String.format("  - %s: %s", key, s.getType()));
        }
    }

    private static void processPaths(OpenAPI openAPI) {
        System.out.println("## Paths");
        // openAPI.getComponents().getPathItems().forEach(System.out::println);
        openAPI.getPaths().forEach((k, v) -> System.out.println(k));

        // white (openAPI.getPaths().for) {
        //     Schema s = openAPI.getPaths().get(key);
        //     System.out.println(String.format("  - %s: %s", key, s.getType()));
        // }
    }


}
