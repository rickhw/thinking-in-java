package com.javainuse.book;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.javainuse.book.service.BookService;

@SpringBootApplication
public class SpringBootGrpcClientExampleApplication {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(SpringBootGrpcClientExampleApplication.class, args);
		BookService bookService = context.getBean(BookService.class);
		bookService.getBook();
	}

}
