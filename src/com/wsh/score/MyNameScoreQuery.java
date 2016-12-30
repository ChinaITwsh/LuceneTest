package com.wsh.score;

import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.CustomScoreProvider;
import org.apache.lucene.search.function.CustomScoreQuery;

/**
 * 采用特殊文件名作为评分标准的评分类
 * Created by 王书汉 on 2016/12/30.
 */
public class MyNameScoreQuery extends CustomScoreQuery {
    private static final long serialVersionUID = -2813985445544972520L;

    public MyNameScoreQuery(Query subQuery) {
        //由于这里是打算根据文件名来自定义评分，所以重写构造方法时不必传入评分域的ValueSourceQuery
        super(subQuery);
    }

    @Override
    protected CustomScoreProvider getCustomScoreProvider(IndexReader reader) throws IOException {
        return new FilenameScoreProvider(reader);
    }

    private class FilenameScoreProvider extends CustomScoreProvider {
        String[] filenames;
        public FilenameScoreProvider(IndexReader reader) {
            super(reader);
            try {
                //在IndexReader没有关闭之前，所有的数据都会存储到一个预缓存中（缺点是占用大量内存）
                //所以我们可以通过预缓存获取name域的值（获取到的是name域所有值，故使用数组）
                this.filenames = FieldCache.DEFAULT.getStrings(reader, "name");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public float customScore(int doc, float subQueryScore, float valSrcScore) throws IOException {
            //由于FilenameScoreQuery构造方法没有传入ValueSourceQuery，故此处ValueSourceQuery默认为1.0
            System.out.println("subQueryScore=" + subQueryScore + "    valSrcScore=" + valSrcScore);
            if(filenames[doc].endsWith(".java") || filenames[doc].endsWith(".ini")){
                //只加大java文件和ini文件的评分
                return subQueryScore*1.5f;
            }else{
                return subQueryScore/1.5f;
            }
        }
    }
}
