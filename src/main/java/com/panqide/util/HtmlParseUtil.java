package com.panqide.util;

import com.panqide.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @title: 爬取在京东搜索出来的数据
 * @description: TODO
 * @date: 2020/8/27 11:48
 */
public class HtmlParseUtil {

//    public static void main(String[] args) throws IOException {
//        parseJD("哲学").forEach(System.out::println);
//    }

    public static List<Content> parseJD(String keywords) throws IOException {
        List<Content> list = new ArrayList<>();
        //获取url
        String url="https://search.jd.com/Search?keyword="+keywords;
        //根据url发起请求并解析页面
        Document document = Jsoup.parse(new URL(url), 30000);
        //可以在document使用js的方法
        Element elementGoods = document.getElementById("J_goodsList");
        //根据京东的前端页面，获取所有的li元素，li元素里面包含了商品信息
        Elements elementsLi = elementGoods.getElementsByTag("li");
        //从每个li元素获取商品标题、价格、图片
        for (Element element : elementsLi) {
            String img = element.getElementsByTag("img").eq(0).attr("src");
            String price = element.getElementsByClass("p-price").eq(0).text();
            String title = element.getElementsByClass("p-name").eq(0).text();
            Content content = new Content();
            content.setTitle(title).setPrice(price).setImg(img);
            list.add(content);
        }
        return list;
    }
}
