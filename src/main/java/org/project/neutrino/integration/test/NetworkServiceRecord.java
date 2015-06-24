package org.project.neutrino.integration.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;

import net.minidev.json.parser.JSONParser;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.support.util.FileUtils;

@SuppressWarnings("rawtypes")
public class NetworkServiceRecord implements Callable {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public Boolean call() throws Exception {

		Boolean output = false;
		output = NetworkServiceRecordCreate();

		return output;
	}

	private boolean NetworkServiceRecordCreate() throws URISyntaxException {
		String body = FileUtils.read(new File(
				"./src/main/resources/vim-instance.json"));

		log.info("SEND REQUEST CREATE VIM");
		try (CloseableHttpClient httpClient = HttpClientBuilder.create()
				.build()) {
			URI uri = new URI("http://127.0.0.1:8080", "/api/v1/datacenters",
					null);
			HttpPost request = new HttpPost(uri);
			StringEntity params = new StringEntity(body);
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse result = httpClient.execute(request);
			log.info("RESPONSE VIM RECIVED");

			log.info(result.toString());

		} catch (IOException ex) {
			return false;
		}

		String body2 = FileUtils.read(new File(
				"./src/main/resources/NetworkServiceDescriptor.json"));

		log.info("SEND REQUEST CREATE NSD");
		try (CloseableHttpClient httpClient = HttpClientBuilder.create()
				.build()) {
			URI uri = new URI("http://127.0.0.1:8080",
					"/api/v1/ns-descriptors", null);
			HttpPost request = new HttpPost(uri);
			StringEntity params = new StringEntity(body2);
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse response = httpClient.execute(request);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "UTF-8"));
			
			

		} catch (IOException ex) {
			return false;
		}

		/*
		 * String body3 = FileUtils.read(new
		 * File("./src/main/resources/NetworkServiceDescriptor.json"));
		 * 
		 * log.info("SEND REQUEST CREATE NSR"); try (CloseableHttpClient
		 * httpClient = HttpClientBuilder.create().build()) { URI uri = new URI(
		 * "http://127.0.0.1:8080", "/api/v1/ns-records/", null); HttpPost
		 * request = new HttpPost(uri); StringEntity params = new
		 * StringEntity(body3); request.addHeader("content-type",
		 * "application/json"); request.setEntity(params); HttpResponse result =
		 * httpClient.execute(request); log.info("RESPONSE NSR RECIVED");
		 * 
		 * log.info(result.toString());
		 * 
		 * 
		 * } catch (IOException ex) { return false; }
		 */

		return true;

	}

}
