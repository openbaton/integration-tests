package org.openbaton.integration.test.rest;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.integration.test.exceptions.SubscriptionException;
import org.openbaton.integration.test.interfaces.WaiterInterface;
import org.openbaton.sdk.NFVORequestor;
import org.openbaton.sdk.api.exception.SDKException;
import org.slf4j.Logger;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by mob on 31.07.15.
 */
public class RestWaiter implements WaiterInterface {

    private HttpServer server;
    private MyHandler myHandler;
    private String name;
    private Logger log;
    private final Lock lock = new ReentrantLock();
    private final Condition eventOccurred  = lock.newCondition();
    private NFVORequestor requestor=null;
    private Gson mapper;
    private EventEndpoint ee;
    private String unsubscriptionId;
    private Action action;
    private String payload;

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
    public boolean waitForEvent(int timeOut) throws InterruptedException {
        return myHandler.await(timeOut);
    }

    @Override
    public Action getAction() {
        if(action==null)
            throw new NullPointerException("Action is null. This method must be invoked after waitForEvent");
        return action;
    }

    @Override
    public String getPayload() {
        if(payload==null)
            throw new NullPointerException("Payload is null. This method must be invoked after waitForEvent");
        return payload;
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
            //log.debug("Action received: " + actionReceived);
            action=Action.valueOf(actionReceived);
            payload= jsonElement.getAsJsonObject().get("payload").toString();
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
        //false if the waiting time detectably elapsed before return from the method, else true
        public boolean await(int timeOut) throws InterruptedException {
            lock.lock();
            try {
               return eventOccurred.await(timeOut, TimeUnit.SECONDS);
            } finally {
                lock.unlock();
            }
        }
        private void wakeUp() {
            lock.lock();
            try {
               eventOccurred.signal();
            } finally {
                lock.unlock();
            }

        }
    }
}
