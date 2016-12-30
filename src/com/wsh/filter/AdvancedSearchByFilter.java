package com.wsh.filter;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import com.wsh.filter.MyFilter;

/**
 * Lucene高级搜索之Filter
 * Created by 王书汉 on 2016/12/30.
 */
public class AdvancedSearchByFilter {
    private Directory directory;
    private IndexReader reader;

    public AdvancedSearchByFilter(){
        /** 文件大小 */
        int[] sizes = {90, 10, 20, 10, 60, 50};
        /** 文件名 */
        String[] names = {"Michael.java", "Scofield.ini", "Tbag.txt", "Jack", "Jade", "Jadyer"};
        /** 文件内容 */
        String[] contents = {"my java blog is http://blog.csdn.net/jadyer",
                             "my Java Website is http://www.jadyer.cn",
                             "my name is jadyer",
                             "I am a Java Developer",
                             "I am from Haerbin",
                             "I like java of Lucene"};
        /** 文件日期 */
        Date[] dates = new Date[sizes.length];
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        IndexWriter writer = null;
        Document doc = null;
        try {
            dates[0] = sdf.parse("20130407 15:25:30");
            dates[1] = sdf.parse("20130407 16:30:45");
            dates[2] = sdf.parse("20130213 11:15:25");
            dates[3] = sdf.parse("20130808 09:30:55");
            dates[4] = sdf.parse("20130526 13:54:22");
            dates[5] = sdf.parse("20130701 17:35:34");
            directory = FSDirectory.open(new File("myExample/01_index/"));
            writer = new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_36, new StandardAnalyzer(Version.LUCENE_36)));
            writer.deleteAll();
            for(int i=0; i<sizes.length; i++){
                doc = new Document();
                doc.add(new NumericField("size",Field.Store.YES, true).setIntValue(sizes[i]));
                doc.add(new Field("name", names[i], Field.Store.YES, Field.Index.ANALYZED_NO_NORMS));
                doc.add(new Field("content", contents[i], Field.Store.NO, Field.Index.ANALYZED));
                doc.add(new NumericField("date", Field.Store.YES, true).setLongValue(dates[i].getTime()));
                //为每个文档添加一个fileID（与ScoreDoc.doc不同），其专门在自定义Filter时使用
                doc.add(new Field("fileID", String.valueOf(i), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
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
     * 搜索过滤
     */
    public void searchByFilter(String expr, Filter filter){
        IndexSearcher searcher = new IndexSearcher(this.getIndexReader());
        QueryParser parser = new QueryParser(Version.LUCENE_36, "content", new StandardAnalyzer(Version.LUCENE_36));
        TopDocs tds = null;
        try {
            if(null == filter){
                tds = searcher.search(parser.parse(expr), 10);
            }else{
                tds = searcher.search(parser.parse(expr), filter, 10);
            }
            for(ScoreDoc sd : tds.scoreDocs){
                Document doc = searcher.doc(sd.doc);
                System.out.print("文档编号=" + sd.doc + "  文档权值=" + doc.getBoost() + "  文档评分=" + sd.score + "   ");
                System.out.print("fileID=" + doc.get("fileID") + "  size=" + doc.get("size") + "  date=");
                System.out.print(new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date(Long.parseLong(doc.get("date")))));
                System.out.println("  name=" + doc.get("name"));
            }
        } catch (Exception e) {
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
     * 测试一下过滤效果
     */
    public static void main(String[] args) throws ParseException {
        AdvancedSearchByFilter advancedSearch = new AdvancedSearchByFilter();
        ////过滤文件名首字母从'h'到'n'的记录（注意hn要小写）
        //advancedSearch.searchByFilter("Java", new TermRangeFilter("name", "h", "n", true, true));
        ////过滤文件大小在30到80以内的记录
        //advancedSearch.searchByFilter("Java", NumericRangeFilter.newIntRange("size", 30, 80, true, true));
        ////过滤文件日期在20130701 00:00:00到20130808 23:59:59之间的记录
        //Long min = Long.valueOf(new SimpleDateFormat("yyyyMMdd").parse("20130701").getTime());
        //Long max = Long.valueOf(new SimpleDateFormat("yyyyMMdd HH:mm:ss").parse("20130808 23:59:59").getTime());
        //advancedSearch.searchByFilter("Java", NumericRangeFilter.newLongRange("date", min, max, true, true));
        ////过滤文件名以'ja'打头的（注意ja要小写）
        //advancedSearch.searchByFilter("Java", new QueryWrapperFilter(new WildcardQuery(new Term("name", "ja*"))));
        //自定义Filter
        advancedSearch.searchByFilter("Java", new MyFilter());
    }
}
