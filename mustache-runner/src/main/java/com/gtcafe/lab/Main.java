package com.gtcafe.lab;

import java.io.StringWriter;
import java.util.*;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;


public class Main {
    public static void main(String[] args) throws Exception {

        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache m = mf.compile("todo.mustache");

        List<Todo> todo = new LinkedList<Todo>();
        for (int i = 0; i<5; i++) {
            todo.add(new Todo("Todo " + Integer.toString(i), "Description"));
        }

        Map<String, Object> context = new HashMap<>();
        context.put("todo", todo);

        StringWriter writer = new StringWriter();

        m.execute(writer, context).flush();

        String html = writer.toString();
        System.out.println("result:");
        System.out.println(html);
    }
}
