package com.javainuse.book.service;

import org.springframework.stereotype.Service;

import com.javainuse.employee.BookRequest;
import com.javainuse.employee.BookResponse;
import com.javainuse.employee.BookServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

@Service
public class BookService {

	public void getBook() {

		ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8090).usePlaintext().build();

		BookServiceGrpc.BookServiceBlockingStub stub = BookServiceGrpc.newBlockingStub(channel);

		BookResponse bookResponse = stub.getBook(BookRequest.newBuilder().setBookId("1").build());

		System.out.println(bookResponse);

		channel.shutdown();
	}
}
