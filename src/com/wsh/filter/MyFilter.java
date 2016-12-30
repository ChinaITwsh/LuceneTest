package com.wsh.filter;

import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;

/**
 * 自定义Filter
 * -----------------------------------------------------------------------------------------------
 * 本例的应用场景
 * 假设很多的数据，然后删除了其中的某几条数据，此时在接受搜索请求时为保证不会搜索到已删除的数据
 * 那么可以更新索引，但更新索引会消耗很多时间（因为数据量大），而又要保证已删除的数据不会被搜索到
 * 此时就可以自定义Filter，原理即搜索过程中，当发现此记录为已删除记录，则不添加到返回的搜索结果集中
 * -----------------------------------------------------------------------------------------------
 * 自定义Filter步骤如下
 * 1)继承Filter类并重写getDocIdSet()方法
 * 2)根据实际过滤要求返回新的DocIdSet对象
 * -----------------------------------------------------------------------------------------------
 * DocIdSet小解
 * 这里Filter干的活其实就是创建一个DocIdSet，而DocIdSet其实就是一个数组，可以理解为其中只存放0或1的值
 * 每个搜索出来的Document都有一个文档编号，所以搜索出来多少个Document，那么DocIdSet中就会有多少条记录
 * 而DocIdSet中每一条记录的索引号与文档编号是一一对应的
 * 所以当DocIdSet中的记录为1时，则对应文档编号的Document就会被添加到TopDocs中，为0就会被过滤掉
 * -----------------------------------------------------------------------------------------------
 * Created by 王书汉 on 2016/12/30.
 */
public class MyFilter extends Filter {
    private static final long serialVersionUID = -8955061358165068L;

    //假设这是已删除记录的fileID值的集合
    private String[] deleteFileIDs = {"1", "3"};

    @Override
    public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
        //创建一个DocIdSet的子类OpenBitSet（创建之后默认所有元素都是0），传的参数就是本次"搜索到的"元素数目
        OpenBitSet obs = new OpenBitSet(reader.maxDoc());
        //先把元素填满，即全部设置为1
        obs.set(0, reader.maxDoc());
        //用于保存已删除元素的文档编号
        int[] docs = new int[1];
        for(String deleteDataID : deleteFileIDs){
            //获取已删除元素对应的TermDocs
            TermDocs tds = reader.termDocs(new Term("fileID", deleteDataID));
            //将已删除元素的文档编号放到docs中，将其出现的频率放到freqs中，最后返回查询出来的元素数目
            int count = tds.read(docs, new int[1]);
            if(count == 1){
                //将这个位置docs[0]的元素删除
                obs.clear(docs[0]);
            }
        }
        return obs;
    }
}
