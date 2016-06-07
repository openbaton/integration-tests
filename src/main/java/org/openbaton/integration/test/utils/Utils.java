/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openbaton.integration.test.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.openbaton.sdk.api.exception.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;


import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Created by lto on 24/06/15.
 */
public class Utils {

    private static final String PROPERTIES_FILE = "/integration-test.properties";
    private static Logger log = LoggerFactory.getLogger(Utils.class);

    public static Properties getProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(Utils.class.getResourceAsStream(PROPERTIES_FILE));
        if (properties.getProperty("external-properties-file") != null) {
            File externalPropertiesFile = new File(properties.getProperty("external-properties-file"));
            if (externalPropertiesFile.exists()) {
                log.debug("Loading properties from external-properties-file: " + properties.getProperty("external-properties-file"));
                InputStream is = new FileInputStream(externalPropertiesFile);
                properties.load(is);
            } else {
                log.debug("external-properties-file: " + properties.getProperty("external-properties-file") + " doesn't exist");
            }
        }
        log.debug("Loaded properties: " + properties);
        return properties;
    }

//	public static JSONObject executePostCall(String nfvoIp, String nfvoPort, String path) throws URISyntaxException, IOException, IntegrationTestException {
//		return executePostCall(nfvoIp,nfvoPort,null,path);
//	}
//	public static JSONObject executePostCall(String nfvoIp, String nfvoPort, String body, String path) throws URISyntaxException, IOException, IntegrationTestException {
//		if(body == null) {
//			body = "{}";
//		}
//		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
//		log.trace("Invoking POST on URL: " + "http://" + nfvoIp + ":" + nfvoPort + "/api/v1/" + path);
//		URI uri = new URI("http://" + nfvoIp + ":" + nfvoPort + "/api/v1/" + path);
//		HttpPost request = new HttpPost("http://" + nfvoIp + ":" + nfvoPort + "/api/v1/" + path);
//		request.addHeader("content-type", "application/json");
//		request.addHeader("accept", "application/json");
////		body = body.replaceAll("\\t", "");
////		body = body.replaceAll("\\n", "");
////		body = body.replaceAll(" ", "");
//		log.trace("With body: " + body);
//		StringEntity params = new StringEntity(body);
//		request.setEntity(params);
//		HttpResponse response = httpClient.execute(request);
//		if (response.getEntity().getContentLength() != 0) {
//			return new JSONObject(getStringFromInputStream(response.getEntity().getContent()));
//		}
//		else
//			throw new IntegrationTestException();
//	}

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


    public static List<URL> getFilesAsURL(String location) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = {};
        try {
            resources = resolver.getResources(location);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        List<URL> urls = new LinkedList<>();
        for (Resource resource : resources) {
            try {
                urls.add(resource.getURL());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }

    // taken from the sdks RestRequest class and slightly modified
    public static String getAccessToken(String nfvoIp, String nfvoPort, String username, String password) throws IOException, SDKException {
        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpPost httpPost = new HttpPost("http://" + nfvoIp + ":" + nfvoPort + "/oauth/token");

        httpPost.setHeader("Authorization", "Basic " + Base64.encodeBase64String("openbatonOSClient:secret".getBytes()));
        List<BasicNameValuePair> parametersBody = new ArrayList<>();
        parametersBody.add(new BasicNameValuePair("grant_type", "password"));
        parametersBody.add(new BasicNameValuePair("username", username));
        parametersBody.add(new BasicNameValuePair("password", password));

        log.debug("Username is: " + username);
        log.debug("Password is: " + password);

        httpPost.setEntity(new UrlEncodedFormEntity(parametersBody, StandardCharsets.UTF_8));

        org.apache.http.HttpResponse response = null;
        log.debug("httpPost is: " + httpPost.toString());
        response = httpClient.execute(httpPost);

        String responseString = null;
        responseString = EntityUtils.toString(response.getEntity());
        int statusCode = response.getStatusLine().getStatusCode();
        log.trace(statusCode + ": " + responseString);

        if (statusCode != 200) {
            ParseComError error = new Gson().fromJson(responseString, ParseComError.class);
            log.error("Status Code [" + statusCode + "]: Error signing-in [" + error.error + "] - " + error.error_description);
            throw new SDKException("Status Code [" + statusCode + "]: Error signing-in [" + error.error + "] - " + error.error_description);
        }
        JsonObject jobj = new Gson().fromJson(responseString, JsonObject.class);
        log.trace("JsonTokeAccess is: " + jobj.toString());
        String bearerToken = null;
        try {
            String token = jobj.get("value").getAsString();
            log.trace(token);
            bearerToken = "Bearer " + token;
        } catch (NullPointerException e) {
            String error = jobj.get("error").getAsString();
            if (error.equals("invalid_grant")) {
                throw new SDKException("Error during authentication: " + jobj.get("error_description").getAsString(), e);
            }
        }
        return bearerToken;
    }

    private class ParseComError implements Serializable {
        String error_description;
        String error;
    }

//	public static void executeDeleteCall(String nfvoIp, String nfvoPort, String path) throws URISyntaxException, IOException {
//		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
//		URI uri = new URI("http://" + nfvoIp + ":" + nfvoPort, "/api/v1/" + path, null);
//		HttpDelete request = new HttpDelete(uri);
//		HttpResponse response = httpClient.execute(request);
//	}


//	public static boolean evaluateJSONObject(JSONObject expected,JSONObject obtained) throws IntegrationTestException {
//
//		boolean res = false;
//		for (Object obj : expected.keySet()) {
//
//			String key = (String) obj;
//
//			Object valueExp = expected.get(key);
//			Object valueObt = null;
//			try{
//				valueObt = obtained.get(key);
//			}catch(JSONException e){
//				return false;
//			}
//
//			if (valueExp instanceof JSONObject) {
//				JSONObject obj2 = (JSONObject) valueExp;
//				log.trace("object key - " + key);
//				if (!evaluateJSONObject(obj2,(JSONObject) valueObt))
//					return false;
//
//			} else if (valueExp instanceof JSONArray) {
//				JSONArray array = (JSONArray) valueExp;
//				log.trace("array - " + key);
//				if (valueObt instanceof JSONArray)
//					res = evaluateJSONArray(array, (JSONArray) valueObt);
//				else
//					throw new IntegrationTestException();
//				if (res == false)
//					return res;
//
//			} else {
//				log.trace("key:" + key + " - valueExp expected:"
//						+ valueExp.toString() + ", value obtained: "
//						+ valueObt);
//				if (!valueExp.toString().equalsIgnoreCase(valueObt.toString())) {
//					log.trace("FALSE");
//					log.trace("key:" + key + " - valueExp expected: "
//							+ valueExp.toString() + ", value obtained: "
//							+ valueObt.toString());
//
//					return false;
//				}
//
//			}
//		}
//
//		return true;
//	}
//
//	private static boolean evaluateJSONArray(JSONArray expected,JSONArray obtained) throws JSONException, IntegrationTestException {
//
//		for (int i = 0; i < expected.length(); i++) {
//			boolean tmpRes = false;
//			Object expVal = expected.get(i);
//
//			if (expVal instanceof JSONObject) {
//				log.trace("array of objects");
//				JSONObject jsonObjectExp = (JSONObject) expVal;
//				for (int j = 0; j < obtained.length(); j++) {
//					tmpRes = tmpRes|| evaluateJSONObject(jsonObjectExp,(JSONObject) obtained.get(j));
//				}
//				if(!tmpRes){
//					return tmpRes;
//				}
//			} else {
//				log.trace(" valueExp expected:"
//						+ expVal.toString() + "  value obtained: "
//						+ obtained.getString(0));
//				for (int j = 0; j < obtained.length(); j++) {
//					tmpRes = tmpRes||obtained.get(j).toString().equals(expVal.toString());
//				}
//				if(!tmpRes){
//					return tmpRes;
//				}
//			}
//		}
//		return true;
//	}
//
//
//	//print json object
//
//	public static boolean printJSONObject(JSONObject expected) {
//
//		for (Object obj : expected.keySet()) {
//
//			String key = (String) obj;
//
//			Object valueExp = expected.get(key);
//
//			if (valueExp instanceof JSONObject) {
//				JSONObject obj2 = (JSONObject) valueExp;
//				log.debug("object key - " + key);
//				printJSONObject(obj2);
//
//			} else if (valueExp instanceof JSONArray) {
//				JSONArray array = (JSONArray) valueExp;
//				log.debug("array - " + key);
//
//				printJSONArray(array, key);
//
//			} else {
//				log.debug("key:" + key + " - valueExp expected:"
//						+ valueExp.toString());
//
//			}
//		}
//
//		return true;
//	}
//
//	private static boolean printJSONArray(JSONArray expected, String key) {
//
//		boolean res = true;
//		for (int i = 0; i < expected.length(); i++) {
//
//			Object obj = expected.get(i);
//
//			if (obj instanceof JSONObject) {
//				log.debug("- object key - " + key);
//				JSONObject jsonObjectExp = (JSONObject) obj;
//				printJSONObject(jsonObjectExp);
//
//			} else {
//				log.debug("- key:" + key + " valueExp expected:"
//						+ obj.toString());
//			}
//
//		}
//
//		return res;
//	}

    public static boolean evaluateObjects(Object expected, Object obtained) {



        return true;
    }

}