package com.panqide.service;

import com.alibaba.fastjson.JSON;
import com.panqide.pojo.Content;
import com.panqide.util.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ContentService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 解析页面数据放入es索引中
     */
    public Boolean parseContent(String keywords) throws Exception{
        //解析页面并获取页面数据，如果是从数据库获取的数据也是同理，用List来接收
        List<Content> contents = HtmlParseUtil.parseJD(keywords);

        //创建批量请求对象
        BulkRequest bulkRequest = new BulkRequest();
        //设置超时时间为2分钟
        bulkRequest.timeout("2m");
        //将页面数据添加到请求对象中
        for(int i=0;i<contents.size();i++){
          //在请求对象中创建索引库，在索引的source中添加页面数据，并且转换为json格式
          bulkRequest.add(new IndexRequest("jd_goods")
                  .source(JSON.toJSONString(contents.get(i)), XContentType.JSON));
        }
        //按默认配置利用restHighLevelClient发起批量请求
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        return !bulk.hasFailures();
    }

    /**
     * 获取es索引库的数据实现搜索功能
     */
    public List<Map<String,Object>> searchPage(String keyword,int pageNo,int pageSize) throws IOException {
        if(pageNo<=1) pageNo=1;
        //创建索引中source的查询构造器
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //分页
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);
        //实现标题的关键词匹配matchQuery(temQuery属于精装匹配）
        MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("title", keyword);
        sourceBuilder.query(matchQuery);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //实现搜索词的高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        //>>设置高亮的前缀标签和后缀标签
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        highlightBuilder.requireFieldMatch(false);  //多个高亮显示
        sourceBuilder.highlighter(highlightBuilder);

        //创建执行搜索的请求对象，并指定索引库
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        //将source放置到请求对象中执行查询，获取查询结果
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        //解析结果
        ArrayList<Map<String,Object>> list = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            //获取查询结果
            Map<String,Object> sourceAsMap= hit.getSourceAsMap();
            //解析高亮字段，将原来的字段换为高亮的字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            if(title!=null){
                Text[] fragments = title.fragments();
                String n_title="";
                for(Text text:fragments){
                    n_title += text;
                }
                sourceAsMap.put("title",n_title);
            }
            list.add(sourceAsMap);
        }
        return list;
    }
}
