package org.openbaton.integration.test.rest;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.openbaton.integration.test.interfaces.WaiterInterface;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.sdk.NFVORequestor;
import org.openbaton.sdk.api.exception.SDKException;
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
    public boolean subscribe(EventEndpoint eventEndpoint) {
        if(launchServer())
        {
            if(eventEndpoint!=null)
            {
                String url = "http://localhost:" + server.getAddress().getPort() + "/"+name;
                eventEndpoint.setEndpoint(url);
                EventEndpoint response;
                try
                {
                    response = this.requestor.getEventAgent().create(eventEndpoint);
                } catch (SDKException e)
                {
                    log.error("Waiter ("+name+") problems during subscription");
                    e.printStackTrace();
                    return false;
                }
                if(response==null)
                    throw new NullPointerException("Response is null");
                unsubscriptionId=response.getId();
            }
            else throw new NullPointerException("EventEndpoint is null");
        }
        else return false;
        ee=eventEndpoint;
        return true;
    }

    @Override
    public boolean unSubscribe() {
        if(ee!=null)
        {
            try
            {
                this.requestor.getEventAgent().requestDelete(unsubscriptionId);
            } catch (SDKException e)
            {
                log.error("Waiter (" + name + ") problems during unSubscription");
                e.printStackTrace();
                stopServer();
                return false;
            }
        }
        else
        {
            throw new NullPointerException("EventEndpoint is null");
        }
        stopServer();
        return true;
    }

    private void stopServer() {
        server.stop(10);
    }
    @Override
    public boolean waitForEvent(int timeOut) {
        return myHandler.await(timeOut);
    }

    private boolean launchServer(){
        try {
            server = HttpServer.create(new InetSocketAddress(0), 1);
        } catch (IOException e) {
            log.error("Waiter cannot launch the server");
            e.printStackTrace();
            return false;
        }
        myHandler=new MyHandler();
        server.createContext("/" + name, myHandler);
        server.setExecutor(null);
        server.start();
        return true;
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
        public synchronized boolean await(int timeOut) {
            try {
                wait(timeOut * 1000);
            } catch (InterruptedException e) {
                log.error("Waiter ("+name+") was interrupted",e.getMessage());
                e.printStackTrace();
                return false;
            }
            return true;
        }
        private synchronized void wakeUp() {
            notify();
        }
    }
}
