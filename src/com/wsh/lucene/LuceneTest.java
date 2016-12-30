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
