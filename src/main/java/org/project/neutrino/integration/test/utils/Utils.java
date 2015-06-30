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
//TODO create singleton
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
        if(body == null) {
            body = "{}";
        }
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        log.trace("Invoking POST on URL: " + "http://" + nfvoIp + ":" + nfvoPort + "/api/v1/" + path);
        URI uri = new URI("http://" + nfvoIp + ":" + nfvoPort + "/api/v1/" + path);
        HttpPost request = new HttpPost("http://" + nfvoIp + ":" + nfvoPort + "/api/v1/" + path);
        request.addHeader("content-type", "application/json");
        request.addHeader("accept", "application/json");
        body = body.replaceAll("\\t", "");
        body = body.replaceAll("\\n", "");
        body = body.replaceAll(" ", "");
        log.trace("With body: " + body);
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
            log.warn("Server is not listening on port " + port + " of " + host);
            return false;
        }
    }

    public static void executeDeleteCall(String nfvoIp, String nfvoPort, String path) throws URISyntaxException, IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        URI uri = new URI("http://" + nfvoIp + ":" + nfvoPort, "/api/v1/" + path, null);
        HttpDelete request = new HttpDelete(uri);
        HttpResponse response = httpClient.execute(request);
    }
    
    
	public static boolean evaluateJSONObject(JSONObject expected, JSONObject obtained) {
		
		for (Object obj : expected.keySet()) {
			String key = (String) obj;

			Object valueExp = expected.get(key);
            Object valueObt = obtained.get(key);

            if (valueExp instanceof JSONArray) {
                JSONArray array = (JSONArray) valueExp;
                log.trace("array - " + key);
                evaluateJSONArray(array, (JSONArray) valueObt, key);

            } else {
                if (valueExp instanceof JSONObject) {
                    JSONObject obj2 = (JSONObject) valueExp;
                    log.trace("object key - " + key);
                    evaluateJSONObject(obj2, (JSONObject) valueObt);

                } else
                {
                    log.trace("key:" + key + " - valueExp expected:" + valueExp.toString() + ", value obtained: " + valueObt);
                    if(!valueExp.equals(valueObt))
                    {
                    	log.trace("FALSE");
                    	return false;
                    }
                }
            }
		}

        return true;
    }
	
	
	private static boolean evaluateJSONArray(JSONArray expected, JSONArray obtained,String key) {

        boolean res = false;

		for (int i = 0; i < expected.length(); i++) {
			
            Object obj = expected.get(i);

			if (obj instanceof JSONObject) {
				JSONObject jsonObjectExp = (JSONObject) obj;
				log.trace("- object key - " + key);
				for(int j = 0; j < expected.length(); j++)
                    res = res || evaluateJSONObject(jsonObjectExp, (JSONObject) obtained.get(j));
				
			}
            else {
            	log.trace("- key:" + key + " valueExp expected:"+ obj.toString()+ ", value obtained: " + obtained.getString(0));
            	if(!obj.toString().equals(obtained.getString(0)))
                {
                	log.trace("FALSE");
                	return false;
                }
            }

		}
       return res;
    }
	

//print json object
	
	public static boolean printJSONObject(JSONObject expected) {

		for (Object obj : expected.keySet()) {

			String key = (String) obj;

			Object valueExp = expected.get(key);

			if (valueExp instanceof JSONObject) {
				JSONObject obj2 = (JSONObject) valueExp;
				log.trace("object key - " + key);
				printJSONObject(obj2);

			} else if (valueExp instanceof JSONArray) {
				JSONArray array = (JSONArray) valueExp;
				log.trace("array - " + key);

				printJSONArray(array, key);

			} else {
				log.trace("key:" + key + " - valueExp expected:"
						+ valueExp.toString());

			}
		}

		return true;
	}

	private static boolean printJSONArray(JSONArray expected, String key) {

		boolean res = true;
		for (int i = 0; i < expected.length(); i++) {

			Object obj = expected.get(i);

			if (obj instanceof JSONObject) {
				log.trace("- object key - " + key);
				JSONObject jsonObjectExp = (JSONObject) obj;
				printJSONObject(jsonObjectExp);

			} else {
				log.trace("- key:" + key + " valueExp expected:"
						+ obj.toString());
			}

		}

		return res;
	}
	
	
}
