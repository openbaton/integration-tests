package org.project.neutrino.integration.test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.support.util.FileUtils;

@SuppressWarnings("rawtypes")
public class Configuration implements Callable{
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public Boolean call() throws Exception {

		Boolean output = false;
        //output =  ConfigurationCreate();
		output =  ConfigurationFindAll();
		
		return output;
	}
	
	private boolean ConfigurationCreate() throws URISyntaxException
	{
		String body = FileUtils.read(new File("./src/main/resources/configuration.json"));
		 
		 System.out.println("SEND REQUEST CREATE");
		 try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			    URI uri = new URI(
					    "http://127.0.0.1:8080", 
					    "/api/v1/configurations",
					    null);
	            HttpPost request = new HttpPost(uri);
	            StringEntity params = new StringEntity(body);
	            request.addHeader("content-type", "application/json");
	            request.setEntity(params);
	            HttpResponse result = httpClient.execute(request);
	            log.info("RESPONSE RECIVED");
	     
	            
	            log.info(result.toString());

	            
	             } catch (IOException ex) {
	        }
		
		return true;
		
	}
	
	private boolean ConfigurationFindAll() throws URISyntaxException
	{
		 
		 log.info("SEND REQUEST FIND ALL");
		 try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			    URI uri = new URI(
					    "http://127.0.0.1:8080", 
					    "/api/v1/configurations",
					    null);
	            HttpGet request = new HttpGet(uri);
	            HttpResponse httpResponse = httpClient.execute(request);
	            log.info("RESPONSE RECIVED");
	            
	            HttpEntity entity = httpResponse.getEntity();
	            
	            log.info("----------------------------------------");
	            log.info(httpResponse.getStatusLine().toString());
	            Header[] headers = httpResponse.getAllHeaders();
	            for (int i = 0; i < headers.length; i++) {
	            	log.info(headers[i].toString());
	            }
	            log.info("----------------------------------------");
	       
	            if (entity != null) {
	            	log.info(EntityUtils.toString(entity));
	            }
	     

	            
	             } catch (IOException ex) {
	        }
		
		return true;
		
	}
	
	

}
