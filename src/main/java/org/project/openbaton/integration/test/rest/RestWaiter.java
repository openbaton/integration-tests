package org.project.openbaton.integration.test.rest;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.project.openbaton.catalogue.nfvo.EventEndpoint;
import org.project.openbaton.integration.test.exceptions.SubscriptionException;
import org.project.openbaton.integration.test.interfaces.WaiterInterface;
import org.project.openbaton.sdk.NFVORequestor;
import org.project.openbaton.sdk.api.exception.SDKException;
import org.slf4j.Logger;
import java.io.*;
import java.net.InetSocketAddress;

/**
 * Created by mob on 31.07.15.
 */
public class RestWaiter implements WaiterInterface {

    private HttpServer server;
    private MyHandler myHandler;
    private String name;
    private Logger log;
    private NFVORequestor requestor=null;
    private Gson mapper;
    private EventEndpoint ee;
    private String unsubscriptionId;

    public RestWaiter(String waiterName,NFVORequestor nfvoRequestor,Gson gsonMapper,Logger logger) {
        name=waiterName;
        requestor=nfvoRequestor;
        mapper=gsonMapper;
        log=logger;
        ee=null;
        unsubscriptionId=null;
    }

    @Override
    public void subscribe(EventEndpoint eventEndpoint) throws SDKException, SubscriptionException {
        try {
            launchServer();
        } catch (IOException e) {
           throw new SubscriptionException("Problems during the launch of the server",e);
        }
        if (eventEndpoint != null) {
            String url = "http://localhost:" + server.getAddress().getPort() + "/" + name;
            eventEndpoint.setEndpoint(url);
            EventEndpoint response=null;
            response = this.requestor.getEventAgent().create(eventEndpoint);
            if (response == null)
                throw new NullPointerException("Response is null");
            unsubscriptionId = response.getId();
        } else throw new NullPointerException("EventEndpoint is null");
        ee = eventEndpoint;
    }

    @Override
    public void unSubscribe() throws SDKException {
        if (ee == null)
            throw new NullPointerException("EventEndpoint is null");
        this.requestor.getEventAgent().requestDelete(unsubscriptionId);
        stopServer();
    }

    private void stopServer() {
        server.stop(10);
    }
    @Override
    public void waitForEvent(int timeOut) throws InterruptedException {
        myHandler.await(timeOut);
    }

    private void launchServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 1);
        myHandler=new MyHandler();
        server.createContext("/" + name, myHandler);
        server.setExecutor(null);
        server.start();
    }
    class MyHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            InputStream is = t.getRequestBody();
            String message = read(is);

            if(checkRequest(message))
                wakeUp();
            String response = "";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private boolean checkRequest(String message) {
            JsonElement jsonElement = mapper.fromJson(message, JsonElement.class);

            String actionReceived= jsonElement.getAsJsonObject().get("action").getAsString();
            log.debug("Action received: " + actionReceived);
            String payload= jsonElement.getAsJsonObject().get("payload").getAsString();
            //log.debug("Payload received: "+payload);
            if(actionReceived.equals(ee.getEvent().toString()))
                return true;
            else
                log.error("Received wrong action: "+ actionReceived);
            return false;
        }

        private String read(InputStream is) throws IOException {

            BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;

            try
            {
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                streamReader.close();
            }
            return responseStrBuilder.toString();
        }

        public synchronized void await(int timeOut) throws InterruptedException {
            wait(timeOut * 1000);
        }
        private synchronized void wakeUp() {
            notify();
        }
    }
}
