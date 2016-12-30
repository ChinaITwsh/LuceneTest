package com.wsh.custom;

import java.io.Reader;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LetterTokenizer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Version;

/**
 * 自定义的停用词分词器（这里主要用来过滤忽略大小写的指定的字符串）
 * Created by 王书汉 on 2016/12/30.
 */
public class MyStopAnalyzer extends Analyzer {
    //存放停用的分词信息
    private Set<Object> stopWords;

    /**
     * 自定义的用于过滤指定字符串的分词器
     * @param _stopWords 用于指定所要过滤的忽略大小写的字符串
     */
    public MyStopAnalyzer(String[] _stopWords){
        //会自动将字符串数组转换为Set
        stopWords = StopFilter.makeStopSet(Version.LUCENE_36, _stopWords, true);
        //将原有的停用词加入到现在的停用词中
        stopWords.addAll(StopAnalyzer.ENGLISH_STOP_WORDS_SET);
    }

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        //为这个分词器设定过滤器链和Tokenizer
        return new StopFilter(Version.LUCENE_36,
            //这里就可以存放很多的TokenFilter
            new LowerCaseFilter(Version.LUCENE_36, new LetterTokenizer(Version.LUCENE_36, reader)),
            stopWords);
    }
}
