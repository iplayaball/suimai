package com.study.suimai.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SuimaiEsConfig {

  // @Bean
  // public RestHighLevelClient esRestClient(){
  //     RestHighLevelClient client = new RestHighLevelClient(
  //             RestClient.builder(new HttpHost("192.168.137.14", 9200, "http")));
  //     return  client;
  // }

  public static final RequestOptions COMMON_OPTIONS;

  static {
    RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
//    builder.addHeader("Cookie", "CloudShellAuthorization=\"Bearer ya29.a0ARrdaM_YCT3YUzeUsLqwK-BazrKH2M_zlKmgPdXMHow9knueTFvUDvPbwRJ92UorOHbWR0qgDwK4kVuIwwrgp1YvM3Vyo-dRHEwcgx3_-Bi_dVrhIOtMUpdprUf-XQOp56kS9RyzVZtCNe0fYd5IZtIPrS2dnbcjU36YEud58jkK9xiGLPoY3MUHZKk1NZ_xTHw1-8FJYP7vmYD8SiMHV0iZR5A9FP0u88M\"");
//    builder.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36");
    // builder.addHeader("Authorization", "Bearer " + TOKEN);
    // builder.setHttpAsyncResponseConsumerFactory(
    //         new HttpAsyncResponseConsumerFactory
    //                 .HeapBufferedResponseConsumerFactory(30 * 1024 * 1024 * 1024));
    COMMON_OPTIONS = builder.build();
  }

  @Bean
  public RestHighLevelClient esRestClient() {
    RestClientBuilder builder = RestClient.builder(new HttpHost("172.18.0.1", 9200, "http"));
//    RestClientBuilder builder = RestClient.builder(new HttpHost("9200-cs-252558529935-default.cs-asia-east1-jnrc.cloudshell.dev", 9200, "https"));
//
//    Header[] defaultHeaders = new Header[]{new BasicHeader("Cookie", "CloudShellAuthorization=\"Bearer ya29.a0ARrdaM_YCT3YUzeUsLqwK-BazrKH2M_zlKmgPdXMHow9knueTFvUDvPbwRJ92UorOHbWR0qgDwK4kVuIwwrgp1YvM3Vyo-dRHEwcgx3_-Bi_dVrhIOtMUpdprUf-XQOp56kS9RyzVZtCNe0fYd5IZtIPrS2dnbcjU36YEud58jkK9xiGLPoY3MUHZKk1NZ_xTHw1-8FJYP7vmYD8SiMHV0iZR5A9FP0u88M\"")};
//    builder.setDefaultHeaders(defaultHeaders);
    RestHighLevelClient client = new RestHighLevelClient(builder);
    return client;
  }

}
