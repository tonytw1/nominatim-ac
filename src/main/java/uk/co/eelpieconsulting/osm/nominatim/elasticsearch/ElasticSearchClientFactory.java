package uk.co.eelpieconsulting.osm.nominatim.elasticsearch;

import org.apache.log4j.Logger;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class ElasticSearchClientFactory {
	
	private static Logger log = Logger.getLogger(ElasticSearchClientFactory.class);
	
	private String clusterName;
	private String unicastHost;
	
	private Client client;
	
	@Autowired
	public ElasticSearchClientFactory(@Value("${elasticsearch.cluster}") String clusterName,
			@Value("${elasticsearch.unicasthost}") String unicastHost) {
		this.clusterName = clusterName;
		this.unicastHost = unicastHost;
	}
	
	public synchronized Client getClient() {
		if (client == null) {
			try {
				client = connectToCluster();
			} catch (UnknownHostException e) {
				throw new RuntimeException(e);
			}
		}
		return client;
	}

	private Client connectToCluster() throws UnknownHostException {
		if (client == null) {			
			log.info("Connecting to elastic search cluster: " + clusterName + ", unicast hosts: " + unicastHost);
			final Settings settings = Settings.settingsBuilder().put("cluster.name", "elasticsearch").build();
			client = TransportClient.builder().settings(settings).build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(unicastHost), 9300));
		}
		return client;
	}
	
}
