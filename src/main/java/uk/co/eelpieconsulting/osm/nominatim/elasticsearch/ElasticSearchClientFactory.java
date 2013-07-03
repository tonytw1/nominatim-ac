package uk.co.eelpieconsulting.osm.nominatim.elasticsearch;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import org.apache.log4j.Logger;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ElasticSearchClientFactory {
	
	private static Logger log = Logger.getLogger(ElasticSearchClientFactory.class);
	
	private String clusterName;
	private String unicastHosts;
	
	private Client client;
	
	@Autowired
	public ElasticSearchClientFactory(@Value("#{autoComplete['elasticsearch.cluster']}") String clusterName,
			@Value("#{autoComplete['elasticsearch.unicasthosts']}") String unicastHosts) {		
		this.clusterName = clusterName;
		this.unicastHosts = unicastHosts;
	}
	
	public synchronized Client getClient() {
		if (client == null) {
			client = connectToCluster();
		}
		return client;
	}

	private Client connectToCluster() {
		if (client == null) {			
			log.info("Connecting to elastic search cluster: " + clusterName + ", unicast hosts: " + unicastHosts);
			final Settings settings = ImmutableSettings.settingsBuilder()
					.put("discovery.zen.ping.multicast.enabled", false)
					.put("discovery.zen.ping.unicast.enabled", true)
					.put("discovery.zen.ping.unicast.hosts", unicastHosts)
					.build();
			
			Node node = nodeBuilder().client(true).clusterName(clusterName).settings(settings).node();
			client = node.client();
		}
		return client;
	}
	
}
