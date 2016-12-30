package com.wsh.search;

import java.io.File;
import org.junit.Before;
import org.junit.Test;
import com.wsh.search.LuceneSearch;

public class LuceneTest {
    private LuceneSearch search;

    @Before
    public void init(){
    	search = new LuceneSearch();
    }

    @Test
    public void searchByTerm(){
    	System.out.println("按词条搜索------searchByTerm");
    	search.searchByTerm("content", "my");
    }

    @Test
    public void searchByTermRange(){
    	System.out.println("按范围搜索------searchByTermRange");
    	search.searchByTermRange("name", "M", "o");
    }

    @Test
    public void searchByNumericRange(){
    	System.out.println("针对数字搜索------searchByNumericRange");
    	search.searchByNumericRange("attach", 2, 5);
    }

    @Test
    public void searchByPrefix(){
    	System.out.println("基于前缀的搜索------searchByPrefix");
    	search.searchByPrefix("content", "b");
    }

    @Test
    public void searchByWildcard(){
    	System.out.println("基于通配符的搜索------searchByWildcard");
    	search.searchByWildcard("name", "hi??om");
    }

    @Test
    public void searchByFuzzy(){
    	System.out.println("模糊搜索------searchByFuzzy");
    	search.searchByFuzzy("name", "high");
    }

    @Test
    public void searchByBoolean(){
    	System.out.println("多条件搜索------searchByBoolean");
    	search.searchByBoolean();
    }

    @Test
    public void searchByPhrase(){
    	System.out.println("短语搜索------searchByPhrase");
    	search.searchByPhrase();
    }

    @Test
    public void searchByQueryParse(){
    	System.out.println("基于QueryParse的搜索------searchByQueryParse");
    	search.searchByQueryParse();
    }

    @Test
    public void searchPage(){
    	System.out.println("普通的分页搜索------searchPage");
        for(File file : new File("myExample/03_index/").listFiles()){
            file.delete();
        }
        search = new LuceneSearch(true);
        search.searchPage("mycontent:java", 2, 10);
    }

    @Test
    public void searchPageByAfter(){
    	System.out.println("基于searchAfter的分页搜索------searchAfter");
        for(File file : new File("myExample/03_index/").listFiles()){
            file.delete();
        }
        search = new LuceneSearch(true);
        search.searchPageByAfter("mycontent:java", 3, 10);
    }
}
