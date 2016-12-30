package com.wsh.custom;

import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

/**
 * Lucene自定义停用词和同义词分词器
 * Created by 王书汉 on 2016/12/30.
 */
public class LuceneCustomAnalyzer {
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
}
