package com.wsh.lucenefir;

import org.junit.Before;
import org.junit.Test;
import com.wsh.lucenefir.LuceneHelloWorld;

public class LuceneTest {
    private LuceneHelloWorld lucene;

    @Before
    public void init(){
        lucene = new LuceneHelloWorld();
    }

    @Test
    public void createIndex(){
    	lucene.createIndex();
    }

    @Test
    public void searchFile(){
    	lucene.searchFile();
    }

}
