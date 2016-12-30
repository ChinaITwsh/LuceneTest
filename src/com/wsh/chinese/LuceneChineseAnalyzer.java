package com.wsh.chinese;

import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.Version;
import com.chenlb.mmseg4j.solr.MMseg4jHandler;
import com.chenlb.mmseg4j.analysis.ComplexAnalyzer;
import com.chenlb.mmseg4j.analysis.MMSegAnalyzer;

/**
 * Lucene中文分词器
 * Created by 王书汉 on 2016/12/29.
 */
public class LuceneChineseAnalyzer {
    /**
     * 查看分词信息
     * -----------------------------------------------------------------------------------
     * TokenStream还有两个属性，分别为FlagsAttribute和PayloadAttribute，都是开发时用的
     * FlagsAttribute----标注位属性
     * PayloadAttribute--做负载的属性，用来检测是否已超过负载，超过则可以决定是否停止搜索等等
     * -----------------------------------------------------------------------------------
     * @param txt        待分词的字符串
     * @param analyzer   所使用的分词器
     * @param displayAll 是否显示所有的分词信息
     */
    public static void displayTokenInfo(String txt, Analyzer analyzer, boolean displayAll){
        //第一个参数没有任何意义，可以随便传一个值，它只是为了显示分词
        //这里就是使用指定的分词器将'txt'分词，分词后会产生一个TokenStream（可将分词后的每个单词理解为一个Token）
        TokenStream stream = analyzer.tokenStream("此参数无意义", new StringReader(txt));
        //用于查看每一个语汇单元的信息，即分词的每一个元素
        //这里创建的属性会被添加到TokenStream流中，并随着TokenStream而增加（此属性就是用来装载每个Token的，即分词后的每个单词）
        //当调用TokenStream.incrementToken()时，就会指向到这个单词流中的第一个单词，即此属性代表的就是分词后的第一个单词
        //可以形象的理解成一只碗，用来盛放TokenStream中每个单词的碗，每调用一次incrementToken()后，这个碗就会盛放流中的下一个单词
        CharTermAttribute cta = stream.addAttribute(CharTermAttribute.class);
        //用于查看位置增量（指的是语汇单元之间的距离，可理解为元素与元素之间的空格，即间隔的单元数）
        PositionIncrementAttribute pia = stream.addAttribute(PositionIncrementAttribute.class);
        //用于查看每个语汇单元的偏移量
        OffsetAttribute oa = stream.addAttribute(OffsetAttribute.class);
        //用于查看使用的分词器的类型信息
        TypeAttribute ta = stream.addAttribute(TypeAttribute.class);
        try {
            if(displayAll){
                //等价于while(stream.incrementToken())
                for(; stream.incrementToken() ;){
                    System.out.print(ta.type() + " " + pia.getPositionIncrement());
                    System.out.println(" [" + oa.startOffset() + "-" + oa.endOffset() + "] [" + cta + "]");
                }
            }else{
                System.out.println();
                while(stream.incrementToken()){
                    System.out.print("[" + cta + "]");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试一下中文分词的效果
     */
    public static void main(String[] args) {
        String txt = "My name is wangshuhan. I am a Javadeveloper.";
        
        //标准分词，它有一些较智能的分词操作，诸如将’jadyer@yeah.net’中的’yeah.net’当作一个分词流
        displayTokenInfo(txt, new StandardAnalyzer(Version.LUCENE_36), false);
        System.out.println("---原始分词方式---");
        
        displayTokenInfo(txt, new StopAnalyzer(Version.LUCENE_36), false);
        System.out.println("---StopAnalyzer分词方式---");
        
        //Simple和Complex都是基于正向最大匹配
        displayTokenInfo(txt, new SimpleAnalyzer(Version.LUCENE_36), false);
        System.out.println("---SimpleAnalyzer分词方式---");
        
        displayTokenInfo(txt, new ComplexAnalyzer(), false);
        System.out.println("---ComplexAnalyzer分词方式---");
        
        //使用空格进行分词，诸如将’Thank you,I am jadyer’会被分为4个词
        displayTokenInfo(txt, new WhitespaceAnalyzer(Version.LUCENE_36), false);
        System.out.println("---WhitespaceAnalyzer分词方式---");
        
        // max-word 分词：“很好听” -> "很好|好听"; “中华人民共和国” -> "中华|华人|共和|国"; “中国人民银行” -> "中国|人民|银行"。
        displayTokenInfo(txt, new MMSegAnalyzer(), false); //等价于new com.chenlb.mmseg4j.analysis.MaxWordAnalyzer()
        System.out.println("---MMSegAnalyzer分词方式(max-word)---");
        
        displayTokenInfo(txt, new com.chenlb.mmseg4j.analysis.SimpleAnalyzer(), false);
        System.out.println("--- com.chenlb.mmseg4j.analysis.SimpleAnalyzer分词方式 ---");
        
        displayTokenInfo(txt, new com.chenlb.mmseg4j.analysis.MaxWordAnalyzer(), false);
        System.out.println("---new com.chenlb.mmseg4j.analysis.MaxWordAnalyzer()分词方式---");
    }
}
