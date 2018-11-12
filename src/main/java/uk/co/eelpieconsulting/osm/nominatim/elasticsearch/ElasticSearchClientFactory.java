package uk.co.eelpieconsulting.osm.nominatim.elasticsearch;

import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.common.settings.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;

@Component
public class ElasticSearchClientFactory {
	
	private static Logger log = Logger.getLogger(ElasticSearchClientFactory.class);
	
	private String clusterName;
	private String unicastHost;
	
	private RestClient client;
	
	@Autowired
	public ElasticSearchClientFactory(@Value("${elasticsearch.cluster}") String clusterName,
			@Value("${elasticsearch.unicasthost}") String unicastHost) {
		this.clusterName = clusterName;
		this.unicastHost = unicastHost;
	}
	
	public synchronized RestClient getClient() {
		if (client == null) {
			try {
				client = connectToCluster();
			} catch (UnknownHostException e) {
				throw new RuntimeException(e);
			}
		}
		return client;
	}

	private RestClient connectToCluster() throws UnknownHostException {
		if (client == null) {			
			log.info("Connecting to elastic search cluster: " + clusterName + ", unicast hosts: " + unicastHost);
			final Settings settings = Settings.builder().put("cluster.name", "elasticsearch").build();
			client = RestClient.builder(new HttpHost(unicastHost, 9200, "http")).build();	// TODO clustername
		}

		return client;
	}
	
}
