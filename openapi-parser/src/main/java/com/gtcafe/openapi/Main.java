package com.gtcafe.openapi;

import java.util.Map;
import java.util.Set;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

public class Main {

    // static String OPENAPI_PATH = "spec/openapi.yaml";
    static String OPENAPI_PATH = "spec/petstore.yaml";

    public static void main(String[] args) {

        SwaggerParseResult result = new OpenAPIParser().readLocation(OPENAPI_PATH, null, null);

        // validation errors and warnings
        if (result.getMessages() != null)
            result.getMessages().forEach(System.err::println); 

        // the parsed POJO
        OpenAPI openAPI = result.getOpenAPI();

        if (openAPI != null) {
            System.out.println(openAPI.getInfo().getTitle());
            // openAPI.getComponents().getSchemas().forEach((k, v) -> System.out.println(k));

            // processPaths(openAPI);

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

    private static void processSchemas(String propName, Schema s) {

        String type = s.getType();

        if ("object".equals(type)) {
            Map<String, Schema> p = s.getProperties();
            Set<String> ss = p.keySet();
            for (String name : ss) {
                processSchemas(name, p.get(name));
            }
        } else if ("array".equals(type)) {
            System.out.println(String.format("%s:", propName));
            System.out.println(String.format("  - type: [%s]", s.getType()));
            System.out.println(String.format("  - items: %s", s.getItems()));

        } else {
            System.out.println(String.format("%s:", propName));
            System.out.println(String.format("  - type: [%s]", s.getType()));
            System.out.println(String.format("  - example: [%s]", s.getExample()));
            System.out.println(String.format("  - default: [%s]", s.getDefault()));
            System.out.println(String.format("  - description: [%s]", s.getDescription()));
            System.out.println(String.format("  - format: [%s]", s.getFormat()));
            System.out.println(String.format("  - enum: [%s]", s.getEnum()));
        }
        
        // if ("number".equals(type)) {
        //     System.out.println("type=" + type);

        // } else if ("string".equals(type)) {
        //     System.out.println("type=" + type);
        // } else if ("integer".equals(type)) {
        //     System.out.println("type=" + type);
        // } else if ("boolean".equals(type)) {
        //     System.out.println("type=" + type);
        // } else if ("array".equals(type)) {
        //     System.out.println("type=" + type);
        // }
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
