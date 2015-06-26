package org.project.neutrino.integration.test.utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.project.neutrino.integration.test.exceptions.IntegrationTestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * Created by lto on 24/06/15.
 */
public class Utils {

    private static final String PROPERTIES_FILE = "/integration-test.properties";
    private static Logger log = LoggerFactory.getLogger(Utils.class);

    public static Properties getProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(Utils.class.getResourceAsStream(PROPERTIES_FILE));
        log.debug("Loaded properties: " + properties);
        return properties;
    }

    public static JSONObject executePostCall(String nfvoIp, String nfvoPort, String path) throws URISyntaxException, IOException, IntegrationTestException {
        return executePostCall(nfvoIp,nfvoPort,null,path);
    }
    public static JSONObject executePostCall(String nfvoIp, String nfvoPort, String body, String path) throws URISyntaxException, IOException, IntegrationTestException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        URI uri = new URI("http://" + nfvoIp + ":" + nfvoPort, "/api/v1/" + path, null);
        HttpPost request = new HttpPost(uri);
        request.addHeader("content-type", "application/json");
        request.addHeader("accept", "application/json");
        if(body == null) {
            body = "{}";
        }
        StringEntity params = new StringEntity(body);
        request.setEntity(params);
        HttpResponse response = httpClient.execute(request);
        if (response.getEntity().getContentLength() != 0) {
            return new JSONObject(getStringFromInputStream(response.getEntity().getContent()));
        }
        else
            throw new IntegrationTestException();
    }

    public static String getStringFromInputStream(InputStream stream) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream), 65728);
            String line = null;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        catch (IOException e) { e.printStackTrace(); }
        catch (Exception e) { e.printStackTrace(); }


        return sb.toString();
    }

    public static boolean isNfvoStarted(String nfvoIp, String nfvoPort) {
        int i = 0;
        while (!available(nfvoIp, nfvoPort)) {
            log.debug("waiting the server to start");
            i++;
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (i > 50){
                return false;
            }
        }
        return true;
    }

    public static boolean available(String host, String port) {
        try {
            Socket s = new Socket(host, Integer.parseInt(port));
            log.info("Server is listening on port " + port + " of " + host);
            s.close();
            return true;
        } catch (IOException ex) {
            // The remote host is not listening on this port
            log.warn("Server is not listening on port " + port	+ " of " + host);
            return false;
        }
    }

    public static void executeDeleteCall(String nfvoIp, String nfvoPort, String path) throws URISyntaxException, IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        URI uri = new URI("http://" + nfvoIp + ":" + nfvoPort, "/api/v1/" + path, null);
        HttpDelete request = new HttpDelete(uri);
        HttpResponse response = httpClient.execute(request);
    }
    
    
	public static void evaluateJSONObject(JSONObject jsonObject) {
		
		for (Object obj : jsonObject.keySet()) {
			String key = (String) obj;

			Object value = jsonObject.get(key);
			if (value instanceof JSONArray) {
				JSONArray array = (JSONArray) value;
				evaluateJSONArray(array);

			} else if (value instanceof JSONObject) {
				JSONObject obj2 = (JSONObject) value;
				evaluateJSONObject(obj2);

			} else 
			{
				log.info("key:" + key + " - value:" + value.toString());
			}
		}

	}

	private static void evaluateJSONArray(JSONArray array) {

		for (int i = 0; i < array.length(); i++) {
			Object obj = array.get(i);
			if (obj instanceof JSONArray) {
				JSONArray array2 = (JSONArray) obj;
				evaluateJSONArray(array2);
			} else if (obj instanceof JSONObject) {

				JSONObject obj2 = (JSONObject) obj;
				evaluateJSONObject(obj2);
			}

		}
		  
    
}
}
