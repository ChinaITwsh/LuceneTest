#初学Lucene总结
This is one of my learning Lucene project, very suitable for friends interested in Lucene. Project notes rich in detail, all of the code can run. Operating environment: JDK1.7, Lucene - 3.6.2, mmseg4j - all - 1.8.5 -- with dic, tika - app - 1.4
##一、Lucene的基本用法
####本学习项目采用的都是`Lucene-3.6.2`
---
##简介
对于全文搜索工具：都是由索引、分词、搜索三部分组成

并且需要认清一点：被存储和被索引，是两个独立的概念

关于域，要介绍一下

域的存储选项，有以下两个

* Field.Store.YES：会把该域中的内容存储到文件中，方便进行文本的还原  
* Field.Store.NO ：表示该域中的内容不存储到文件中，但允许被索引，且内容无法完全还原（doc.get(##)）  

域的索引选项，有以下几个

* Field.Index.ANALYZED ：进行分词和索引,适用于标题、内容等
* Field.Index.NOT_ANALYZED ：进行索引但不分词（如身份证号、姓名、ID等）,适用于精确搜索
* Field.Index.ANALYZED_NOT_NORMS ：分词但不存储norms信息（norms包含了索引和排序评分规则权值等信息）
* Field.Index.NOT_ANALYZED_NOT_NORMS：即不进行分词也不存储norms信息
* Field.Index.NO ：不进行索引  
---
##代码
注意：测试时，要在/myExample/01_file/文件夹中准备几个包含内容的文件（比如txt格式的）然后先执行createIndex()方法，再执行searchFile()方法，最后观看控制台输出即可。项目代码详细：`LuceneTest\src\com\wsh\lucenefir`  
```java
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
```  
---
##二、Lucene操作索引
##Luke
---
使用Luke可以查看分词信息，其下载地址为:http://code.google.com/p/luke/

注意：每一个Lucene版本都会有一个相应的Luke文件

用法为双击lukeall-3.5.0.jar或命令执行java -jar lukeall-3.5.0.jar

然后选择索引的存放目录，再点击OK即可

如果我们的索引有改变，可以点击右侧的Re-open按钮重新载入索引

Luke界面右下角的Top ranking terms窗口中显示的就是分词信息（其中Rank列表示出现频率）

Luke菜单下的Documents选项卡中显示的就是文档信息，我们可以根据文档序号来浏览（点击向左和向右的方向箭头）

Luke菜单下的Search选项卡中可以根据我们输入的表达式来查文档内容

比如在Enter search expression here:输入content:my，再在右侧点击一个黑色粗体字的Search大按钮即可

---
##代码
下面演示的是`Lucene-3.6.2`中针对索引文件增删改查的操作方式,项目代码详细:`LuceneTest\src\com\wsh\lucene`
```java
package com.wsh.lucene;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
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

/**
 * Lucene之索引的操作
 * Created by 王书汉 on 2016/12/28.
 */
public class Lucene {
    /*
     * 定义一组数据，用来演示搜索（这里有一封邮件为例）
     * 假设每一个变量代表一个Document，这里就定义了6个Document
     */
    //邮件编号
    private String[] ids = {"1", "2", "3", "4", "5", "6"};
    //邮件主题
    private String[] names = {"Michael", "Scofield", "Tbag", "Jack", "Jade", "Jadyer"};
    //邮件地址
    private String[] emails = {"aa@iCloud.com", "bb@highcom.com", "cc@163.com", "dd@gmail.com", "ee@qq.com", "ff@baidu.com"};
    //邮件内容
    private String[] contents = {"my Mac", "my company", "my email", "I am JavaDeveloper", "I am from Haerbin", "I like Lucene"};
    //邮件附件（为数字和日期加索引，与，字符串加索引的方式不同）
    private int[] attachs = {9,3,5,4,1,2};
    //邮件日期
    private Date[] dates = new Date[ids.length];
    //它的创建是比较耗时耗资源的，所以这里只让它创建一次，此时reader处于整个生命周期中，实际应用中也可能直接放到ApplicationContext里面
    private static IndexReader reader = null;
    private Directory directory = null;

    public Lucene(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        try {
            dates[0] = sdf.parse("20161228");
            dates[1] = sdf.parse("20161228");
            dates[2] = sdf.parse("20161228");
            dates[3] = sdf.parse("20161228");
            dates[4] = sdf.parse("20161228");
            dates[5] = sdf.parse("20161228");
            directory = FSDirectory.open(new File("myExample/02_index/"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取IndexReader实例
     */
    private IndexReader getIndexReader(){
        try {
            if(null == reader){
                reader = IndexReader.open(directory);
            }else{
                //if the index was changed since the provided reader was opened, open and return a new reader; else,return null
                //如果当前reader在打开期间index发生改变，则打开并返回一个新的IndexReader，否则返回null
                IndexReader ir = IndexReader.openIfChanged(reader);
                if(null != ir){
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
     * 通过IndexReader获取文档数量
     */
    public void getDocsCount(){
        System.out.println("maxDocs:" + this.getIndexReader().maxDoc());
        System.out.println("numDocs:" + this.getIndexReader().numDocs());
        System.out.println("deletedDocs:" + this.getIndexReader().numDeletedDocs());
    }

    /**
     * 创建索引
     */
    public void createIndex(){
        IndexWriter writer = null;
        Document doc = null;
        try{
            writer = new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_36, new StandardAnalyzer(Version.LUCENE_36)));
            //创建索引之前，先把文档清空掉
            writer.deleteAll();
            //遍历ID来创建文档
            for(int i=0; i<ids.length; i++){
                doc = new Document();
                doc.add(new Field("id", ids[i], Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
                doc.add(new Field("name", names[i], Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
                doc.add(new Field("email", emails[i], Field.Store.YES, Field.Index.NOT_ANALYZED));
                doc.add(new Field("content", contents[i], Field.Store.NO, Field.Index.ANALYZED));
                //为数字加索引（第三个参数指定是否索引）
                doc.add(new NumericField("attach", Field.Store.YES, true).setIntValue(attachs[i]));
                //为日期加索引
                doc.add(new NumericField("date", Field.Store.YES, true).setLongValue(dates[i].getTime()));
                 //建立索引时加权（定义排名规则，即加权，这里是为指定邮件名结尾的emails加权）
                if(emails[i].endsWith("highcom.com")){
                    doc.setBoost(2.0f);
                }else if(emails[i].endsWith("gmail.com")){
                    //为文档加权（注意它的参数类型是Float，默认为1.0f，权值越高则排名越高，显示得就越靠前）
                    doc.setBoost(1.5f);
                }else{
                    doc.setBoost(0.5f);
                }
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
     * 搜索文件
     */
    public void searchFile(){
        IndexSearcher searcher = new IndexSearcher(this.getIndexReader());
        //精确搜索：搜索"content"中包含"my"的文档
        Query query = new TermQuery(new Term("content", "my"));
        try{
            TopDocs tds = searcher.search(query, 10);
            for(ScoreDoc sd : tds.scoreDocs){
                //sd.doc得到的是文档的序号
                Document doc = searcher.doc(sd.doc);
                //doc.getBoost()得到的权值与创建索引时设置的权值之间是不相搭的，创建索引时的权值的查看需要使用Luke工具
                //              之所以这样，是因为这里的Document对象（是获取到的）与创建索引时的Document对象，不是同一个对象
                //sd.score得到的是该文档的评分，该评分规则的公式是比较复杂的，它主要与文档的权值和出现次数成正比
                System.out.print("("+sd.doc+"|"+doc.getBoost()+"|"+sd.score+")"+doc.get("name")+"["+doc.get("email")+"]-->");
                System.out.print(doc.get("id")+","+doc.get("attach")+",");
                System.out.println(new SimpleDateFormat("yyyyMMdd").format(new Date(Long.parseLong(doc.get("date")))));
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(null != searcher){
                try {
                    searcher.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 更新索引
     * ----------------------------------------------------------------------
     * Lucene其实并未提供更新索引的方法，这里的更新操作内部是先删除再添加的方式
     * 因为Lucene认为更新索引的代价，与删除后重建索引的代价，二者是差不多的
     * ----------------------------------------------------------------------
     */
    public void updateIndex(){
        IndexWriter writer = null;
        Document doc = new Document();
        try{
            writer = new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_36, new StandardAnalyzer(Version.LUCENE_36)));
            doc.add(new Field("id", "1111", Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
            doc.add(new Field("name", names[0], Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
            doc.add(new Field("email", emails[0], Field.Store.YES, Field.Index.NOT_ANALYZED));
            doc.add(new Field("content", contents[0], Field.Store.NO, Field.Index.ANALYZED));
            doc.add(new NumericField("attach", Field.Store.YES, true).setIntValue(attachs[0]));
            doc.add(new NumericField("date", Field.Store.YES, true).setLongValue(dates[0].getTime()));
            //其实它会先删除索引文档中id为1的文档，然后再将这里的doc对象重新索引，所以即便这里的1!=1111，但它并不会报错
            //所以在执行完该方法后：maxDocs=7,numDocs=6,deletedDocs=1，就是因为Lucene会先删除再添加
            writer.updateDocument(new Term("id","1"), doc);
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
     * 删除索引
     * ----------------------------------------------------------------------------------------------
     * 在执行完该方法后，再执行本类的searchFile()方法，得知numDocs=5,maxDocs=6,deletedDocs=1
     * 这说明此时删除的文档并没有被完全删除，而是存储在一个回收站中，它是可以恢复的
     * ----------------------------------------------------------------------------------------------
     * 从回收站中清空索引IndexWriter
     * 对于清空索引，Lucene3.5之前叫做优化，调用的是IndexWriter.optimize()方法，但该方法已被禁用
     * 因为optimize时它会全部更新索引，这一过程所涉及到的负载是很大的，于是弃用了该方法，使用forceMerge代替
     * 使用IndexWriter.forceMergeDeletes()方法可以强制清空回收站中的内容
     * 另外IndexWriter.forceMerge(3)方法会将索引合并为3段，这3段中的被删除的数据也会被清空
     * 但其在Lucene3.5之后不建议使用，因为其会消耗大量的开销，而Lucene会根据情况自动处理的
     * ----------------------------------------------------------------------------------------------
     */
    public void deleteIndex(){
        IndexWriter writer = null;
        try{
            writer = new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_36, new StandardAnalyzer(Version.LUCENE_36)));
            //其参数可以传Query或Term（Query指的是可以查询出一系列的结果并将其全部删掉，而Term属于精确查找）
            //删除索引文档中id为1的文档
            writer.deleteDocuments(new Term("id", "1"));
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
     * 恢复索引（建议弃用）
     */
    @Deprecated
    public void unDeleteIndex(){
        IndexReader reader = null;
        try {
            //IndexReader.open(directory)此时该IndexReader默认的readOnly=true，而在恢复索引时应该指定其为非只读的
            reader = IndexReader.open(directory, false);
            //Deprecated. Write support will be removed in Lucene 4.0. There will be no replacement for this method.
            reader.undeleteAll();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(null != reader){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
```
####LuceneTest
```java
package com.wsh.lucene;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.wsh.lucene.Lucene;

public class LuceneTest {
    private Lucene lucene;

    @Before
    public void init(){
        lucene = new Lucene();
    }

    @After
    public void destroy(){
    	lucene.getDocsCount();
    }

    @Test
    public void createIndex(){
    	lucene.createIndex();
    }

    @Test
    public void searchFile(){
    	lucene.searchFile();
    }

    @Test
    public void updateIndex(){
    	lucene.updateIndex();
    }

    @Test
    public void deleteIndex(){
    	lucene.deleteIndex();
    }

    @Test
    @SuppressWarnings("deprecation")
    public void unDeleteIndex(){
    	lucene.unDeleteIndex();
    }
}
```
---
##三、Lucene常见搜索
###下面演示的内容，包括了Lucene-3.6.2的以下几种常见搜索

* 精确搜索
* 范围搜索
* 针对数字的搜索
* 基于前缀的搜索
* 基于通配符的搜索
* 模糊搜索
* 多条件搜索
* 短语搜索
* 基于QueryParser的搜索
* 普通的分页搜索（适用于lucene3.5之前）
* 基于searchAfter的分页搜索（适用于Lucene3.5）
###示例代码如下,项目代码详细:`LuceneTest\src\com\wsh\search`

```java
package com.wsh.search;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * Lucene常见搜索
 * Created by 王书汉 on 2016/12/29.
 */
public class LuceneSearch {
    private Directory directory;
    private IndexReader reader;
    private String[] ids = {"1", "2", "3", "4", "5", "6"};
    private String[] names = {"Michael", "Scofield", "Tbag", "Jack", "Jade", "Jadyer"};
    private String[] emails = {"aa@highcom.us", "bb@highcom.cn", "cc@highcom.cc", "dd@highcom.tw", "ee@highcom.hk", "ff@highcom.me"};
    private String[] contents = {"my java blog is http://blog.highcom.net/highcom", "my website is http://www.highcom.cn", "my name is highcom", "I am JavaDeveloper", "I am from Haerbin", "I like Lucene"};
    private int[] attachs = {9,3,5,4,1,2};
    private Date[] dates = new Date[ids.length];

    public LuceneSearch(){
        IndexWriter writer = null;
        Document doc = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        try {
        	dates[0] = sdf.parse("20161228");
            dates[1] = sdf.parse("20161228");
            dates[2] = sdf.parse("20161228");
            dates[3] = sdf.parse("20161228");
            dates[4] = sdf.parse("20161228");
            dates[5] = sdf.parse("20161228");
            directory = FSDirectory.open(new File("myExample/03_index/"));
            writer = new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_36, new StandardAnalyzer(Version.LUCENE_36)));
            //创建索引之前，先把文档清空掉
            writer.deleteAll();
            //遍历ID来创建文档
            for(int i=0; i<ids.length; i++){
                doc = new Document();
                doc.add(new Field("id", ids[i], Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
                doc.add(new Field("name", names[i], Field.Store.YES, Field.Index.ANALYZED_NO_NORMS));
                doc.add(new Field("email", emails[i], Field.Store.YES, Field.Index.NOT_ANALYZED));
                doc.add(new Field("email", "test"+i+""+i+"@jadyer.com", Field.Store.YES, Field.Index.NOT_ANALYZED));
                doc.add(new Field("content", contents[i], Field.Store.NO, Field.Index.ANALYZED));
                //为数字加索引（第三个参数指定是否索引）
                doc.add(new NumericField("attach", Field.Store.YES, true).setIntValue(attachs[i]));
                //假设有多个附件
                doc.add(new NumericField("attach", Field.Store.YES, true).setIntValue((i+1)*100));
                //为日期加索引
                doc.add(new NumericField("date", Field.Store.YES, true).setLongValue(dates[i].getTime()));
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
     * 针对分页搜索创建索引
     */
    public LuceneSearch(boolean pageFlag){
        String[] myNames = new String[50];
        String[] myContents = new String[50];
        for(int i=0; i<50; i++){
            myNames[i] = "file(" + i + ")";
            myContents[i] = "I love Java, also love Lucene(" + i + ")";
        }
        IndexWriter writer = null;
        Document doc = null;
        try {
            directory = FSDirectory.open(new File("myExample/03_index/"));
            writer = new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_36, new StandardAnalyzer(Version.LUCENE_36)));
            writer.deleteAll();
            for(int i=0; i<myNames.length; i++){
                doc = new Document();
                doc.add(new Field("myname", myNames[i], Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
                doc.add(new Field("mycontent", myContents[i], Field.Store.YES, Field.Index.ANALYZED));
                writer.addDocument(doc);
            }
        } catch (IOException e) {
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
     * @param query 搜索的Query对象
     */
    private void doSearch(Query query){
        IndexSearcher searcher = this.getIndexSearcher();
        try {
            //第二个参数指定搜索后显示的最多的记录数，其与tds.totalHits没有联系
            TopDocs tds = searcher.search(query, 10);
            System.out.println("本次搜索到[" + tds.totalHits + "]条记录");
            for(ScoreDoc sd : tds.scoreDocs){
                Document doc = searcher.doc(sd.doc);
                System.out.print("文档编号="+sd.doc+"  文档权值="+doc.getBoost()+"  文档评分="+sd.score+"    ");
                System.out.print("id="+doc.get("id")+"  email="+doc.get("email")+"  name="+doc.get("name")+"  ");
                //获取多个同名域的方式
                String[] attachValues = doc.getValues("attach");
                for(String attach : attachValues){
                    System.out.print("attach=" + attach + "  ");
                }
                System.out.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(null != searcher){
                try {
                    //记得关闭IndexSearcher
                    searcher.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 精确匹配搜索
     * @param fieldName 域名（相当于表的字段名）
     * @param keyWords  搜索的关键字
     */
    public void searchByTerm(String fieldName, String keyWords){
        Query query = new TermQuery(new Term(fieldName, keyWords));
        this.doSearch(query);
    }

    /**
     * 基于范围的搜索
     * @param fieldName 域名（相当于表的字段名）
     * @param start     开始字符
     * @param end       结束字符
     */
    public void searchByTermRange(String fieldName, String start, String end){
        //后面两个参数用于指定开区间或闭区间
        Query query = new TermRangeQuery(fieldName, start, end, true, true);
        this.doSearch(query);
    }

    /**
     * 针对数字的搜索
     */
    public void searchByNumericRange(String fieldName, int min, int max){
        Query query = NumericRangeQuery.newIntRange(fieldName, min, max, true, true);
        this.doSearch(query);
    }

    /**
     * 基于前缀的搜索（它是对Field分词后的结果进行前缀查找的结果）
     */
    public void searchByPrefix(String fieldName, String prefix){
        Query query = new PrefixQuery(new Term(fieldName, prefix));
        this.doSearch(query);
    }

    /**
     * 基于通配符的搜索
     * @param wildcard *：任意多个字符，?：一个字符
     */
    public void searchByWildcard(String fieldName, String wildcard){
        Query query = new WildcardQuery(new Term(fieldName, wildcard));
        this.doSearch(query);
    }

    /**
     * 模糊搜索（与通配符搜索不同）
     */
    public void searchByFuzzy(String fieldName, String fuzzy){
        Query query = new FuzzyQuery(new Term(fieldName, fuzzy));
        this.doSearch(query);
    }

    /**
     * 多条件搜索（本例中搜索name值中以Ja开头，且content中包含am的内容）
     */
    public void searchByBoolean(){
        BooleanQuery query = new BooleanQuery();
        //Occur.MUST表示此条件必须为true，Occur.MUST_NOT表示此条件必须为false，Occur.SHOULD表示此条件非必须
        query.add(new WildcardQuery(new Term("name", "Ja*")), Occur.MUST);
        query.add(new TermQuery(new Term("content", "am")), Occur.MUST);
        this.doSearch(query);
    }

    /**
     * 短语搜索（很遗憾的是短语查询对中文搜索没有太大的作用，但对英文搜索是很好用的，但它的开销比较大，尽量少用）
     */
    public void searchByPhrase(){
        PhraseQuery query = new PhraseQuery();
        query.setSlop(1);                          //设置跳数
        query.add(new Term("content", "am"));      //第一个Term
        query.add(new Term("content", "Haerbin")); //产生距离之后的第二个Term
        this.doSearch(query);
    }

    /**
     * 基于QueryParser的搜索
     */
    public void searchByQueryParse(){
        QueryParser parser = new QueryParser(Version.LUCENE_36, "content", new StandardAnalyzer(Version.LUCENE_36));
        Query query = null;
        try {
            //query = parser.parse("Haerbin");           //搜索content中包含[Haerbin]的记录
            //query = parser.parse("I AND Haerbin");     //搜索content中包含[I]和[Haerbin]的记录
            //query = parser.parse("Lucene OR Haerbin"); //搜索content中包含[Lucene]或者[Haerbin]的记录
            //query = parser.parse("Lucene Haerbin");    //搜索content中包含[Lucene]或者[Haerbin]的记录
            //parser.setDefaultOperator(Operator.AND);   //将空格的默认操作OR修改为AND
            ////1)如果name域在索引时，不进行分词，那么无论这里写成[name:Jadyer]还是[name:jadyer]，最后得到的都是0条记录
            ////2)由于name原值为大写[J]，若索引时不对name分词，除非修改name原值为小写[j]，并且搜索[name:jadyer]才能得到记录
            //query = parser.parse("name:Jadyer");       //修改搜索域为name=Jadyer的记录
            //query = parser.parse("name:Ja*");          //支持通配符
            //query = parser.parse("\"I am\"");          //搜索content中包含[I am]的记录（注意不能使用parse("content:'I am'")）
            //parser.setAllowLeadingWildcard(true);      //设置允许[*]或[?]出现在查询字符的第一位，即[name:*de]，否则[name:*de]会报异常
            //query = parser.parse("name:*de");          //Lucene默认的第一个字符不允许为通配符，因为这样效率比较低
            ////parse("+am +name:Jade")--------------------搜索content中包括[am]的，并且name=Jade的记录
            ////parse("am AND NOT name:Jade")--------------搜索content中包括[am]的，并且nam不是Jade的记录
            ////parse("(blog OR am) AND name:Jade")--------搜索content中包括[blog]或者[am]的，并且name=Jade的记录
            //query = parser.parse("-name:Jack +I");     //搜索content中包括[I]的，并且name不是Jack的记录（加减号要放到域说明的前面）
            //query = parser.parse("id:[1 TO 3]");       //搜索id值从1到3的记录（TO必须大写，且这种方式没有办法匹配数字）
            //query = parser.parse("id:{1 TO 3}");       //搜索id=2的记录
            query = parser.parse("name:high~");          //模糊搜索
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.doSearch(query);
    }

    /**
     * 普通的分页搜索（适用于lucene3.5之前）
     * @param expr      搜索表达式
     * @param pageIndex 页码
     * @param pageSize  分页大小
     */
    public void searchPage(String expr, int pageIndex, int pageSize){
        IndexSearcher searcher = this.getIndexSearcher();
        QueryParser parser = new QueryParser(Version.LUCENE_36, "mycontent", new StandardAnalyzer(Version.LUCENE_36));
        try {
            Query query = parser.parse(expr);
            TopDocs tds = searcher.search(query, pageIndex*pageSize);
            ScoreDoc[] sds = tds.scoreDocs;
            for(int i=(pageIndex-1)*pageSize; i<pageIndex*pageSize; i++){
                Document doc = searcher.doc(sds[i].doc);
                System.out.println("文档编号:" + sds[i].doc + "-->" + doc.get("myname") + "-->" + doc.get("mycontent"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(null != searcher){
                try {
                    searcher.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 基于searchAfter的分页搜索（适用于Lucene3.5）
     * @param expr      搜索表达式
     * @param pageIndex 页码
     * @param pageSize  分页大小
     */
    public void searchPageByAfter(String expr, int pageIndex, int pageSize){
        IndexSearcher searcher = this.getIndexSearcher();
        QueryParser parser = new QueryParser(Version.LUCENE_36, "mycontent", new StandardAnalyzer(Version.LUCENE_36));
        try {
            Query query = parser.parse(expr);
            TopDocs tds = searcher.search(query, (pageIndex-1)*pageSize);
            //使用IndexSearcher.searchAfter()搜索，该方法第一个参数为上一页记录中的最后一条记录
            if(pageIndex > 1){
                tds = searcher.searchAfter(tds.scoreDocs[(pageIndex-1)*pageSize-1], query, pageSize);
            }else{
                tds = searcher.searchAfter(null, query, pageSize);
            }
            for(ScoreDoc sd : tds.scoreDocs){
                Document doc = searcher.doc(sd.doc);
                System.out.println("文档编号:" + sd.doc + "-->" + doc.get("myname") + "-->" + doc.get("mycontent"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(null != searcher){
                try {
                    searcher.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
```
####LuceneTest
```java
package com.wsh.search;

import java.io.File;
import org.junit.Before;
import org.junit.Test;
import com.wsh.search.LuceneSearch;

public class LuceneTest {
    private LuceneSearch search;

    @Before
    public void init(){
    	search = new LuceneSearch();
    }

    @Test
    public void searchByTerm(){
    	System.out.println("按词条搜索------searchByTerm");
    	search.searchByTerm("content", "my");
    }

    @Test
    public void searchByTermRange(){
    	System.out.println("按范围搜索------searchByTermRange");
    	search.searchByTermRange("name", "M", "o");
    }

    @Test
    public void searchByNumericRange(){
    	System.out.println("针对数字搜索------searchByNumericRange");
    	search.searchByNumericRange("attach", 2, 5);
    }

    @Test
    public void searchByPrefix(){
    	System.out.println("基于前缀的搜索------searchByPrefix");
    	search.searchByPrefix("content", "b");
    }

    @Test
    public void searchByWildcard(){
    	System.out.println("基于通配符的搜索------searchByWildcard");
    	search.searchByWildcard("name", "hi??om");
    }

    @Test
    public void searchByFuzzy(){
    	System.out.println("模糊搜索------searchByFuzzy");
    	search.searchByFuzzy("name", "high");
    }

    @Test
    public void searchByBoolean(){
    	System.out.println("多条件搜索------searchByBoolean");
    	search.searchByBoolean();
    }

    @Test
    public void searchByPhrase(){
    	System.out.println("短语搜索------searchByPhrase");
    	search.searchByPhrase();
    }

    @Test
    public void searchByQueryParse(){
    	System.out.println("基于QueryParse的搜索------searchByQueryParse");
    	search.searchByQueryParse();
    }

    @Test
    public void searchPage(){
    	System.out.println("普通的分页搜索------searchPage");
        for(File file : new File("myExample/03_index/").listFiles()){
            file.delete();
        }
        search = new LuceneSearch(true);
        search.searchPage("mycontent:java", 2, 10);
    }

    @Test
    public void searchPageByAfter(){
    	System.out.println("基于searchAfter的分页搜索------searchAfter");
        for(File file : new File("myExample/03_index/").listFiles()){
            file.delete();
        }
        search = new LuceneSearch(true);
        search.searchPageByAfter("mycontent:java", 3, 10);
    }
}
```
