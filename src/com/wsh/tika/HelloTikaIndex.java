package com.wsh.tika;

import java.io.File;
import java.io.IOException;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.tika.Tika;
import com.chenlb.mmseg4j.analysis.ComplexAnalyzer;

/**
 * Lucene之Tika
 * Created by 王书汉 on 2016/12/30.
 */
public class HelloTikaIndex {
    private Directory directory;
    private IndexReader reader;

    public HelloTikaIndex(){
        try {
            directory = FSDirectory.open(new File("myExample/myIndex/"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建索引
     */
    public void createIndex(){
        Document doc = null;
        IndexWriter writer = null;
        File myFile = new File("myExample/myFile/");
        try{
            //这里的分词器使用的是MMSeg4j（记得引入mmseg4j-all-1.8.5-with-dic.jar）
            //详见https://jadyer.github.io/2013/08/18/lucene-chinese-analyzer/中对MMSeg4j的介绍
            writer = new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_36, new ComplexAnalyzer()));
            writer.deleteAll();
            for(File file : myFile.listFiles()){
                doc = new Document();
                ////当保存文件的Metadata时，要过滤掉文件夹，否则会报告文件夹无法访问的异常
                //if(file.isDirectory()){
                //    continue;
                //}
                //Metadata metadata = new Metadata();
                //doc.add(new Field("filecontent", new Tika().parse(new FileInputStream(file), metadata)));
                doc.add(new Field("filecontent", new Tika().parse(file)));
                doc.add(new Field("filename", file.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
                writer.addDocument(doc);
            }
        }catch(Exception e) {
            e.printStackTrace();
        }finally{
            if(null != writer){
                try {
                    writer.close();
                } catch (IOException ce) {
                    ce.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取IndexSearcher实例
     */
    private IndexSearcher getIndexSearcher(){
        try {
            if(reader == null){
                reader = IndexReader.open(directory);
            }else{
                //if the index was changed since the provided reader was opened, open and return a new reader; else,return null
                //如果当前reader在打开期间index发生改变，则打开并返回一个新的IndexReader，否则返回null
                IndexReader ir = IndexReader.openIfChanged(reader);
                if(ir != null){
                    reader.close(); //关闭原reader
                    reader = ir;    //赋予新reader
                }
            }
            return new IndexSearcher(reader);
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null; //发生异常则返回null
    }

    /**
     * 执行搜索操作
     * @param fieldName 域名（相当于表的字段名）
     * @param keyWords  搜索的关键字
     */
    public void searchFile(String fieldName, String keyWords){
        IndexSearcher searcher = this.getIndexSearcher();
        Query query = new TermQuery(new Term(fieldName, keyWords));
        try {
            TopDocs tds = searcher.search(query, 50);
            for(ScoreDoc sd : tds.scoreDocs){
                Document doc = searcher.doc(sd.doc);
                System.out.print("文档编号=" + sd.doc + "  文档权值=" + doc.getBoost() + "  文档评分=" + sd.score + "   ");
                System.out.println("filename=" + doc.get("filename"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(null != searcher){
                try {
                    searcher.close(); //记得关闭IndexSearcher
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 测试一下效果（测试前记得在myExample/myFile/目录下预先准备几个doc,pdf,html,txt等文件）
     */
    public static void main(String[] args) {
        HelloTikaIndex hello = new HelloTikaIndex();
        hello.createIndex();
        hello.searchFile("filecontent", "java");
    }
}
