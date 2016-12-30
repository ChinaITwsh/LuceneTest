package com.wsh.score;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.CorruptIndexException;
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
import com.wsh.score.MyNameScoreQuery;

/**
 * Lucene高级搜索之评分
 * Created by 王书汉 on 2016/12/30.
 */
public class AdvancedSearchByScore {
    private Directory directory;
    private IndexReader reader;

    public AdvancedSearchByScore(){
        /** 文件大小 */
        int[] sizes = {90, 10, 20, 10, 60, 50};
        /** 文件名 */
        String[] names = {"Michael.java", "Scofield.ini", "Tbag.txt", "Jack", "Jade", "Jadyer"};
        /** 文件内容 */
        String[] contents = {"my java blog is http://blog.highcom/highcom",
                             "my Java Website is http://www.highcom.cn",
                             "my name is chinaitwsh",
                             "I am a Java Developer",
                             "I am from Haerbin",
                             "I like java of Lucene"};
        IndexWriter writer = null;
        Document doc = null;
        try {
            directory = FSDirectory.open(new File("myExample/01_index/"));
            writer = new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_36, new StandardAnalyzer(Version.LUCENE_36)));
            writer.deleteAll();
            for(int i=0; i<sizes.length; i++){
                doc = new Document();
                doc.add(new NumericField("size", Field.Store.YES, true).setIntValue(sizes[i]));
                doc.add(new Field("name", names[i], Field.Store.YES, Field.Index.ANALYZED_NO_NORMS));
                doc.add(new Field("content", contents[i], Field.Store.NO, Field.Index.ANALYZED));
                //添加一个评分域，专门在自定义评分时使用（此时默认为Field.Store.NO和Field.Index.ANALYZED_NO_NORMS）
                doc.add(new NumericField("fileScore").setIntValue(new Random().nextInt(600)));
                writer.addDocument(doc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
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
     * 获取IndexReader实例
     */
    private IndexReader getIndexReader(){
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
            return reader;
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null; //发生异常则返回null
    }

    /**
     * 自定义评分搜索
     */
    public void searchByCustomScoreQuery(){
        IndexSearcher searcher = new IndexSearcher(this.getIndexReader());
        ////创建一个评分域
        //FieldScoreQuery fsq = new FieldScoreQuery("fileScore", FieldScoreQuery.Type.INT);
        ////创建自定义的CustomScoreQuery对象
        //Query query = new MyCustomScoreQuery(new TermQuery(new Term("content", "java")), fsq);
        Query query = new MyNameScoreQuery(new TermQuery(new Term("content", "java")));
        try {
            TopDocs tds = searcher.search(query, 10);
            for(ScoreDoc sd : tds.scoreDocs){
                Document doc = searcher.doc(sd.doc);
                System.out.print("文档编号=" + sd.doc + "  文档权值=" + doc.getBoost() + "  文档评分=" + sd.score + "   ");
                System.out.println("size=" + doc.get("size") + "  name=" + doc.get("name"));
            }
        } catch (CorruptIndexException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(searcher != null){
                try {
                    searcher.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 测试一下评分效果
     */
    public static void main(String[] args) {
        new AdvancedSearchByScore().searchByCustomScoreQuery();
    }
}
