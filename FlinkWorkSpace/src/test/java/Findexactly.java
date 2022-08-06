import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;

public class Findexactly {
    public static void main(String[] args) throws IOException {
        // 创建客户端对象
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("hadoop101", 9200, "http"))
        );
        // 创建搜索请求对象
        SearchRequest request = new SearchRequest();
        request.indices("item");
// 构建查询的请求体
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
       // sourceBuilder.query(QueryBuilders.matchQuery( "id","1451").fuzziness(Fuzziness.ONE));
       // boolQueryBuilder.must(QueryBuilders.matchQuery("name","RTX2060"));
        //boolQueryBuilder.must(QueryBuilders.matchQuery("name","512GSSD"));
        //boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gte(3000.0).lte(5000.0));
        //boolQueryBuilder.must(QueryBuilders.matchQuery("keyword","电脑"));
        sourceBuilder.query(boolQueryBuilder);
        sourceBuilder.size(1200);
        //sourceBuilder.query(QueryBuilders.matchQuery("name","戴尔 MX330 联想 "));
        //sourceBuilder.query(QueryBuilders.fuzzyQuery( "name","人脸识别").fuzziness(Fuzziness.ONE));
        request.source(sourceBuilder);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
// 查询匹配
        SearchHits hits = response.getHits();
        System.out.println("took:" + response.getTook());
        System.out.println("timeout:" + response.isTimedOut());
        System.out.println("total:" + hits.getTotalHits());
        System.out.println("MaxScore:" + hits.getMaxScore());
        System.out.println("hits========>>");
        for (SearchHit hit : hits) {
//输出每条查询的结果信息
            System.out.println("数据为： "+hit.getSourceAsString());
        }
        System.out.println("<<========");
        client.close();
    }
}
