package com.wsh.lucenefir;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * Lucene创建索引与查询
 * 这里只需用到一个lucene-core-3.6.2.jar
 * Created by 王书汉 on 2016/12/28.
 */
public class LuceneHelloWorld {
    private static final String PATH_OF_FILE = "luceneExample/01_file/";   //待索引文件的目录
    private static final String PATH_OF_INDEX = "luceneExample/01_index/"; //存放索引文件的目录

    /**
     * 创建索引
     * ------------------------------------------------------------------------------------------------------
     * 1、创建Directory--------指定索引被保存的位置
     * 2、创建IndexWriter------通过IndexWriter写索引
     * 3、创建Document对象-----我们索引的有可能是一段文本or数据库中的一张表
     * 4、为Document添加Field--相当于Document的标题、大小、内容、路径等等，二者类似于数据库表中每条记录和字段的关系
     * 5、通过IndexWriter添加文档到索引中
     * 6、关闭IndexWriter------用完IndexWriter之后，必须关闭之
     * ------------------------------------------------------------------------------------------------------
     * _0.fdt和_0.fdx文件--保存域中所存储的数据(Field.Store.YES条件下的)
     * _0.fnm文件----------保存域选项的数据(即new Field(name, value)中的name)
     * _0.frq文件----------记录相同的文件(或查询的关键字)出现的次数，它是用来做评分和排序的
     * _0.nrm文件----------存储一些评分信息
     * _0.prx文件----------记录偏移量
     * _0.tii和_0.tis文件--存储索引里面的所有内容信息
     * segments_1文件------它是段文件，Lucene首先会到段文件中查找相应的索引信息
     * ------------------------------------------------------------------------------------------------------
     */
    public void createIndex(){
        Directory directory = null;
        IndexWriter writer = null;
        Document doc = null;
        try{
            //FSDirectory会根据运行环境打开一个合理的基于File的Directory（若在内存中创建索引则使用RAMDirectory）
            //这里是在硬盘上"C/index01/"文件夹中创建索引
            directory = FSDirectory.open(new File(PATH_OF_INDEX));
            //由于Lucene2.9之后，其索引的格式就不会再兼容Lucene的所有版本了,所以创建索引前要指定其所匹配的Lucene版本号
            //这里使用了Lucene的标准分词器
            writer = new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_36, new StandardAnalyzer(Version.LUCENE_36)));
            for(File file : new File(PATH_OF_FILE).listFiles()){
                doc = new Document();
                //把内容添加到索引域中，即为该文档存储信息，供将来搜索时使用（下面的写法，其默认为Field.Store.NO和Field.Index.ANALYZED）
                //若想把content的内容也存储到硬盘上，那就需要先把file转换成字符串，然后按照"fileName"的存储方式加到Field中
                //doc.add(new Field("content", FileUtils.readFileToString(file), Field.Store.YES, Field.Index.ANALYZED));
                doc.add(new Field("content", new FileReader(file)));
                //Field.Store.YES-----------这里是将文件的全名存储到硬盘中
                //Field.Index.NOT_ANALYZED--这里是不对文件名进行分词
                doc.add(new Field("fileName", file.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
                doc.add(new Field("filePath", file.getAbsolutePath(), Field.Store.YES, Field.Index.NOT_ANALYZED));
                //通过IndexWriter添加文档到索引中
                writer.addDocument(doc);
            }
        }catch(Exception e){
            System.out.println("创建索引的过程中遇到异常,堆栈轨迹如下");
            e.printStackTrace();
        }finally{
            if(null != writer){
                try {
                    //IndexWriter在用完之后一定要关闭
                    writer.close();
                } catch (IOException ce) {
                    System.out.println("关闭IndexWriter时遇到异常,堆栈轨迹如下");
                    ce.printStackTrace();
                }
            }
        }
    }

    /**
     * 搜索文件
     * 1、创建Directory
     * 2、创建IndexReader
     * 3、根据IndexReader创建IndexSearcher
     * 4、创建搜索的Query
     * 5、根据searcher搜索并返回TopDocs
     * 6、根据TopDocs获取ScoreDoc对象
     * 7、根据searcher和ScoreDoc对象获取具体的Document对象
     * 8、根据Document对象获取需要的值
     * 9、关闭IndexReader
     */
    public void searchFile(){
        IndexReader reader = null;
        try{
            reader = IndexReader.open(FSDirectory.open(new File(PATH_OF_INDEX)));
            IndexSearcher searcher = new IndexSearcher(reader);
            //创建基于Parser搜索的Query，创建时需指定其"搜索的版本,默认搜索的域,分词器"...这里的域指的是创建索引时Field的名字
            QueryParser parser = new QueryParser(Version.LUCENE_36, "content", new StandardAnalyzer(Version.LUCENE_36));
            Query query = parser.parse("java");       //指定搜索域为content（即上一行代码指定的"content"）中包含"java"的文档
            TopDocs tds = searcher.search(query, 10); //第二个参数指定搜索后显示的条数，若查到5条则显示为5条，查到15条则只显示10条
            ScoreDoc[] sds = tds.scoreDocs;           //TopDocs中存放的并不是我们的文档，而是文档的ScoreDoc对象
            for(ScoreDoc sd : sds){                   //ScoreDoc对象相当于每个文档的ID号，我们就可以通过ScoreDoc来遍历文档
                Document doc = searcher.doc(sd.doc);  //sd.doc得到的是文档的序号
                System.out.println(doc.get("fileName") + "["+doc.get("filePath")+"]"); //输出该文档所存储的信息
            }
        }catch(Exception e){
            System.out.println("搜索文件的过程中遇到异常,堆栈轨迹如下");
            e.printStackTrace();
        }finally{
            if(null != reader){
                try {
                    reader.close();
                } catch (IOException e) {
                    System.out.println("关闭IndexReader时遇到异常,堆栈轨迹如下");
                    e.printStackTrace();
                }
            }
        }
    }
}

