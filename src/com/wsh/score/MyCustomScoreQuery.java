package com.wsh.score;

import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.CustomScoreProvider;
import org.apache.lucene.search.function.CustomScoreQuery;
import org.apache.lucene.search.function.ValueSourceQuery;

/**
 * 自定义评分的步骤
 * ---------------------------------------------------------------------------------------
 * 1)创建一个类继承于CustomScoreQuery
 * 2)覆盖CustomScoreQuery.getCustomScoreProvider()方法
 * 3)创建一个类继承于CustomScoreProvider
 * 4)覆盖CustomScoreProvider.customScore()方法：我们的自定义评分主要就是在此方法中完成的
 * ---------------------------------------------------------------------------------------
 * Created by 王书汉 on 2016/12/30.
 */
public class MyCustomScoreQuery extends CustomScoreQuery {
    private static final long serialVersionUID = -2373017691291184609L;

    public MyCustomScoreQuery(Query subQuery, ValueSourceQuery valSrcQuery) {
        //ValueSourceQuery参数就是指专门用来做评分的Query，即评分域的FieldScoreQuery
        super(subQuery, valSrcQuery);
    }

    @Override
    protected CustomScoreProvider getCustomScoreProvider(IndexReader reader) throws IOException {
        //如果直接返回super的，就表示使用原有的评分规则，即通过[原有的评分*传入的评分域所获取的评分]来确定最终评分
        //return super.getCustomScoreProvider(reader);
        return new MyCustomScoreProvider(reader);
    }

    private class MyCustomScoreProvider extends CustomScoreProvider {
        public MyCustomScoreProvider(IndexReader reader) {
            super(reader);
        }
        @Override
        public float customScore(int doc, float subQueryScore, float valSrcScore) throws IOException {
            //subQueryScore--表示默认文档的打分，valSrcScore--表示评分域的打分
            //该方法的返回值就是文档评分，即ScoreDoc.score获取的结果
            System.out.println("subQueryScore=" + subQueryScore + "    valSrcScore=" + valSrcScore);
            return subQueryScore/valSrcScore;
        }
    }
}
