package com.gtcafe.openapi;

import java.util.Map;
import java.util.Set;

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
            processSchemas(key, s);
        }
    }

    private static void processSchemas(String key, Schema s) {

        System.out.println(String.format("  - %s: %s", key, s.getType()));

        String type = s.getType();

        if ("object".equals(type)) {
            Map<String, Schema> p = s.getProperties();
            Set<String> kk = p.keySet();
            for (String kk2 : kk) {
                Schema ss = p.get(kk2);
                processSchemas(kk2, ss);
            }
        } else if ("number".equals(type)) {
            System.out.println("type=" + type);
        } else if ("string".equals(type)) {
            System.out.println("type=" + type);
        } else if ("integer".equals(type)) {
            System.out.println("type=" + type);
        } else if ("boolean".equals(type)) {
            System.out.println("type=" + type);
        } else if ("array".equals(type)) {
            System.out.println("type=" + type);
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
