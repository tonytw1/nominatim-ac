package uk.co.eelpieconsulting.osm.nominatim.psql;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OSMDAOFactory implements FactoryBean {

  private String username;
  private String password;
  private String host;

  @Autowired
  public OSMDAOFactory(@Value("${database.username}") String username,
                       @Value("${database.password}") String password,
                       @Value("${database.host}") String host) {
    this.username = username;
    this.password = password;
    this.host = host;
  }

  @Override
  public Object getObject() throws Exception {
    return new OsmDAO(username, password, host);
  }

  @Override
  public Class<?> getObjectType() {
    return OsmDAO.class;
  }
}
