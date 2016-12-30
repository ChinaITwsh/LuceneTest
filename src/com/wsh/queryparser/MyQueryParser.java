package com.wsh.queryparser;

import java.text.SimpleDateFormat;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

/**
 * 自定义QueryParser
 * --------------------------------------------------------------------------------------------------
 * 实际使用QueryParser的过程中，通常会考虑两个问题
 * 1)限制性能低的QueryParser--对于某些QueryParser在搜索时会使得性能降低，故考虑禁用这些搜索以提升性能
 * 2)扩展基于数字和日期的搜索---有时需要进行一个数字的范围搜索，故需扩展原有的QueryParser才能实现此搜索
 * --------------------------------------------------------------------------------------------------
 * 限制性能低的QueryParser
 * 继承QueryParser类并重载相应方法，比如getFuzzyQuery和getWildcardQuery
 * 这样造成的结果就是，当输入普通的搜索表达式时，如'I AND Haerbin'可以正常搜索
 * 但输入'name:Jadk~'或者'name:Ja??er'时，就会执行到重载方法中，这时就可以自行处理了，比如本例中禁止该功能
 * --------------------------------------------------------------------------------------------------
 * 扩展基于数字和日期的查询
 * 思路就是继承QueryParser类后重载getRangeQuery()方法
 * 再针对数字和日期的'域'，做特殊处理（使用NumericRangeQuery.newIntRange()方法来搜索）
 * --------------------------------------------------------------------------------------------------
 * Created by 王书汉 on 2016/12/30.
 */
public class MyQueryParser extends QueryParser {
    public MyQueryParser(Version matchVersion, String f, Analyzer a) {
        super(matchVersion, f, a);
    }

    @Override
    protected Query getWildcardQuery(String field, String termStr) throws ParseException {
        throw new ParseException("由于性能原因，已禁用通配符搜索，请输入更精确的信息进行搜索 ^_^ ^_^");
    }

    @Override
    protected Query getFuzzyQuery(String field, String termStr, float minSimilarity) throws ParseException {
        throw new ParseException("由于性能原因，已禁用模糊搜索，请输入更精确的信息进行搜索 ^_^ ^_^");
    }

    @Override
    protected Query getRangeQuery(String field, String part1, String part2, boolean inclusive) throws ParseException {
        if(field.equals("size")){
            //默认的QueryParser.parse(String query)表达式中并不支持'size:[20 TO 80]'数字的域值
            //这样一来，针对数字的域值进行特殊处理，那么QueryParser表达式就支持数字了
            return NumericRangeQuery.newIntRange(field, Integer.parseInt(part1), Integer.parseInt(part2), inclusive, inclusive);
        }else if(field.equals("date")){
            String regex = "\\d{8}";
            String dateType = "yyyyMMdd";
            if(Pattern.matches(regex, part1) && Pattern.matches(regex, part2)){
                SimpleDateFormat sdf = new SimpleDateFormat(dateType);
                try {
                    long min = sdf.parse(part1).getTime();
                    long max = sdf.parse(part2).getTime();
                    //使之支持日期的检索，应用时直接QueryParser.parse("date:[20130407 TO 20130701]")
                    return NumericRangeQuery.newLongRange(field, min, max, inclusive, inclusive);
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }
            }else{
                throw new ParseException("Unknown date format, please use '" + dateType + "'");
            }
        }
        //如没找到匹配的Field域，那么返回默认的TermRangeQuery
        return super.getRangeQuery(field, part1, part2, inclusive);
    }
}
