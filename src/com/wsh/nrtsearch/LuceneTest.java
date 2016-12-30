package com.wsh.nrtsearch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.wsh.nrtsearch.HelloNRTSearch;

public class LuceneTest {
    @Before
    public void init(){
        HelloNRTSearch.createIndex();
    }

    @After
    public void destroy(){
        HelloNRTSearch.getDocsCount();
    }

    @Test
    public void searchFile(){
        HelloNRTSearch hello = new HelloNRTSearch();
        for(int i=0; i<5; i++){
            hello.searchFile();
            System.out.println("-----------------------------------------------------------");
            hello.deleteIndex();
            if(i == 2){
                hello.updateIndex();
            }
            try {
                System.out.println(".........开始休眠5s(模拟近实时搜索情景)");
                Thread.sleep(5000);
                System.out.println(".........休眠结束");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //不能单独去new HelloNRTSearch，要保证它们是同一个对象，否则所做的delete和update不会被commit
        hello.commitIndex();
    }
}
