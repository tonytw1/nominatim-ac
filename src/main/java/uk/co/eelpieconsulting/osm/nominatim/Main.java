package uk.co.eelpieconsulting.osm.nominatim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@EnableAutoConfiguration
@ComponentScan(basePackages="uk.co.eelpieconsulting")
@Configuration
@EnableScheduling
@EnableWebMvc
public class Main extends WebMvcConfigurerAdapter {
	
	private static ApplicationContext ctx;
    
    public static void main(String[] args) throws Exception {
    	ctx = SpringApplication.run(Main.class, args);
    }
    
}