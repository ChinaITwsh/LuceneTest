package com.wsh.custom;

import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import com.wsh.custom.MyStopAnalyzer;
import com.wsh.custom.MySynonymAnalyzer;
import com.wsh.custom.LuceneCustomAnalyzer;

public class LuceneTest {
    @Test
    public void stopAnalyzer(){
        String txt = "This is my house, I`m come from Haerbin, My email is ChinaITwsh@iCloud.com";
        LuceneCustomAnalyzer.displayTokenInfo(txt, new StandardAnalyzer(Version.LUCENE_36), false);
        LuceneCustomAnalyzer.displayTokenInfo(txt, new StopAnalyzer(Version.LUCENE_36), false);
        LuceneCustomAnalyzer.displayTokenInfo(txt, new MyStopAnalyzer(new String[]{"I", "EMAIL", "you"}), false);
    }

    @Test
    public void synonymAnalyzer(){
        String txt = "我来自中国黑龙江省哈尔滨市";
        IndexWriter writer = null;
        IndexSearcher searcher = null;
        Directory directory = new RAMDirectory();
        try {
            writer = new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_36, new MySynonymAnalyzer()));
            Document doc = new Document();
            doc.add(new Field("content", txt, Field.Store.YES, Field.Index.ANALYZED));
            writer.addDocument(doc);
            writer.close();
            //搜索前要确保IndexWriter已关闭，否则会报告org.apache.lucene.index.IndexNotFoundException: no segments* file found
            searcher = new IndexSearcher(IndexReader.open(directory));
            TopDocs tds = searcher.search(new TermQuery(new Term("content", "咱")), 10);
            for(ScoreDoc sd : tds.scoreDocs){
                System.out.println(searcher.doc(sd.doc).get("content"));
            }
            searcher.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LuceneCustomAnalyzer.displayTokenInfo(txt, new MySynonymAnalyzer(), true);
    }
}
