package com.panqide.service;

import com.alibaba.fastjson.JSON;
import com.panqide.pojo.Content;
import com.panqide.util.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContentService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    //解析页面数据放入es索引中
    public Boolean parseContent(String keywords) throws Exception{
        List<Content> contents = HtmlParseUtil.parseJD(keywords);
        //把查询到的数据放入es中
        //创建批量请求
        BulkRequest bulkRequest = new BulkRequest();
        //设置超时时间
        bulkRequest.timeout("2m");
        for(int i=0;i<contents.size();i++){
          bulkRequest.add(new IndexRequest("jd_goods")
                  .source(JSON.toJSONString(contents.get(i)), XContentType.JSON));
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        return !bulk.hasFailures();
    }
}
