package com.gtcafe.lab;

import java.io.StringWriter;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {
        System.out.println( "Hello World!" );

        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache m = mf.compile("todo.mustache");

        Todo todo = new Todo("Todo 1", "Description");

        StringWriter writer = new StringWriter();

        m.execute(writer, todo).flush();
        
        String html = writer.toString();

        System.out.println(html);
    }
}
