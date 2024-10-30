package com.gtcafe;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

public class CustomErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().isError();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        HttpStatus statusCode = response.getStatusCode();
        switch (statusCode.series()) {
            case CLIENT_ERROR:
                System.out.println("4XX Client Error: " + statusCode);
                break;
            case SERVER_ERROR:
                System.out.println("5XX Server Error: " + statusCode);
                break;
            default:
                System.out.println("Unexpected error: " + statusCode);
        }
    }
}
