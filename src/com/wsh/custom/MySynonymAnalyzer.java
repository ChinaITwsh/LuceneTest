package com.wsh.custom;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;
import com.chenlb.mmseg4j.ComplexSeg;
import com.chenlb.mmseg4j.Dictionary;
import com.chenlb.mmseg4j.analysis.MMSegTokenizer;

/**
 * 自定义的同义词分词器
 * Created by 王书汉 on 2016/12/30.
 */
public class MySynonymAnalyzer extends Analyzer {
    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        //借助MMSeg4j实现自定义分词器，写法参考MMSegAnalyzer类的tokenStream()方法
        //但为了过滤并处理分词后的各个语汇单元，以达到同义词分词器的功能，故自定义一个TokenFilter
        //实际执行流程就是字符串的Reader首先进入MMSegTokenizer，由其进行分词，分词完毕后进入自定义的MySynonymTokenFilter
        //然后在MySynonymTokenFilter中添加同义词
        return new MySynonymTokenFilter(new MMSegTokenizer(new ComplexSeg(Dictionary.getInstance()), reader));
    }
}

/**
 * 自定义的TokenFilter
 * Created by 王书汉 on 2016/12/30
 */
class MySynonymTokenFilter extends TokenFilter {
    private CharTermAttribute cta;            //用于获取TokenStream中的语汇单元
    private PositionIncrementAttribute pia;   //用于获取TokenStream中的位置增量
    private AttributeSource.State tokenState; //用于保存语汇单元的状态
    private Stack<String> synonymStack;       //用于保存同义词

    protected MySynonymTokenFilter(TokenStream input) {
        super(input);
        this.cta = this.addAttribute(CharTermAttribute.class);
        this.pia = this.addAttribute(PositionIncrementAttribute.class);
        this.synonymStack = new Stack<String>();
    }

    /**
     * 判断是否存在同义词
     */
    private boolean isHaveSynonym(String name){
        //先定义同义词的词典
        Map<String, String[]> synonymMap = new HashMap<String, String[]>();
        synonymMap.put("我", new String[]{"咱", "俺"});
        synonymMap.put("中国", new String[]{"兲朝", "大陆"});
        if(synonymMap.containsKey(name)){
            for(String str : synonymMap.get(name)){
                this.synonymStack.push(str);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean incrementToken() throws IOException {
        while(this.synonymStack.size() > 0){
            restoreState(this.tokenState); //将状态还原为上一个元素的状态
            cta.setEmpty();
            cta.append(this.synonymStack.pop()); //获取并追加同义词
            pia.setPositionIncrement(0);         //设置位置增量为0
            return true;
        }
        if(input.incrementToken()){
            //注意：当发现当前元素存在同义词之后，不能立即追加同义词，即不能在目标元素上直接处理
            if(this.isHaveSynonym(cta.toString())){
                //存在同义词时，则捕获并保存当前状态
                this.tokenState = captureState();
            }
            return true;
        }else {
            //只要TokenStream中没有元素，就返回false
            return false;
        }
    }
}
