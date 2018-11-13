package uk.co.eelpieconsulting.osm.nominatim.elasticsearch;

import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;

@Component
public class ElasticSearchClientFactory {

  private static Logger log = Logger.getLogger(ElasticSearchClientFactory.class);

  private final String host;
  private final Integer port;

  private RestHighLevelClient client;

  @Autowired
  public ElasticSearchClientFactory(
          @Value("${elasticsearch.host}") String host,
          @Value("${elasticsearch.port}") Integer port) {
    this.host = host;
    this.port = port;
  }

  public synchronized RestHighLevelClient getClient() {
    if (client == null) {
      try {
        client = connectToCluster();
      } catch (UnknownHostException e) {
        throw new RuntimeException(e);
      }
    }
    return client;
  }

  private RestHighLevelClient connectToCluster() throws UnknownHostException {
    if (client == null) {
      log.info("Setting up elastic rest client with host and port: " + host + ":" + port);
      RestClientBuilder restClient = RestClient.builder(new HttpHost(host, port, "http"));
      client = new RestHighLevelClient(restClient);
    }

    return client;
  }

}
