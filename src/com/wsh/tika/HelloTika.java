package com.wsh.tika;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

/**
 * Tika-1.4使用示例
 * Created by 王书汉 on 2016/12/30.
 */
public class HelloTika {
    public static String parseToStringByTikaParser(File file){
        //创建解析器，使用AutoDetectParser可以自动检测一个最合适的解析器
        Parser parser = new AutoDetectParser();
        //指定解析文件中的文档内容
        ContentHandler handler = new BodyContentHandler();
        //指定元数据存放位置，并自己添加一些元数据
        Metadata metadata = new Metadata();
        metadata.set("MyAddPropertyName", "我叫玄玉");
        metadata.set(Metadata.RESOURCE_NAME_KEY, file.getAbsolutePath());
        //指定最基本的变量信息（即存放一个所使用的解析器对象）
        ParseContext context = new ParseContext();
        context.set(Parser.class, parser);
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            //InputStream-----指定文件输入流
            //ContentHandler--指定要解析文件的哪一个内容，它有一个实现类叫做BodyContentHandler，即专门用来解析文档内容的
            //Metadata--------指定解析文件时，存放解析出来的元数据的Metadata对象
            //ParseContext----该对象用于存放一些变量信息，该对象最少也要存放所使用的解析器对象，这也是其存放的最基本的变量信息
            parser.parse(is, handler, metadata, context);
            //打印元数据
            for(String name : metadata.names()){
                System.out.println(name + "=" + metadata.get(name));
            }
            //返回解析到的文档内容
            return handler.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static String parseToStringByTika(File file){
        //据Tika文档上说，org.apache.tika.Tika的效率没有org.apache.tika.parser.Parser的高
        Tika tika = new Tika();
        //可以指定是否获取元数据，也可自己添加元数据
        Metadata metadata = new Metadata();
        metadata.set("MyAddPropertyName", "我叫玄玉");
        metadata.set(Metadata.RESOURCE_NAME_KEY, file.getAbsolutePath());
        try {
            String fileContent = tika.parseToString(file);
            //String fileContent = tika.parseToString(new FileInputStream(file), metadata);
            //打印元数据
            for(String name : metadata.names()){
                System.out.println(name + "=" + metadata.get(name));
            }
            return fileContent;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println(parseToStringByTikaParser(new File("myExample/myFile/Java安全.doc")));
        System.out.println(parseToStringByTika(new File("myExample/myFile/Oracle_SQL语句优化.pdf")));
        System.out.println(parseToStringByTika(new File("myExample/myFile/")));
    }
}
