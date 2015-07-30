package org.project.openbaton.integration.test.testers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.project.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.EndpointType;
import org.project.openbaton.catalogue.nfvo.EventEndpoint;
import org.project.openbaton.integration.test.utils.Tester;
import org.project.openbaton.sdk.api.exception.SDKException;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.Properties;

/**
 * Created by mob on 28.07.15.
 */
public class NetworkServiceRecordWaiterWait extends Tester {

    private int timeout;
    private final static String name="NetworkServiceRecordWaiterWait";
    private HttpServer server;
    private MyHandler myHandler;

    public NetworkServiceRecordWaiterWait(Properties properties) {
        super(properties, NetworkServiceRecordWaiterWait.class, "" , "");
    }

    @Override
    protected Serializable prepareObject() {
        return null;
    }

    @Override
    protected Object doWork() throws Exception {

        launchServer();

        sendRestRegistration();
        //log.debug(name+": registration complete");


        myHandler.await();

        stopServer();
        sendRestUnregistration();
        //log.debug(name+": unsubscribed");
        log.debug(name+": ended the wait and forward the param: " + param.toString());
        return param;
    }

    private void sendRestUnregistration() throws SDKException {
        this.requestor.getEventAgent().requestDelete(name);
    }

    private void stopServer() {
        server.stop(10);
    }

    private void launchServer() throws IOException, InterruptedException {
        server = HttpServer.create(new InetSocketAddress(0), 1);
        myHandler=new MyHandler();
        server.createContext("/"+name, myHandler);
        server.setExecutor(null);
        server.start();
    }

    private String getNSRId() {
        NetworkServiceRecord nsr = (NetworkServiceRecord) param;
        return nsr.getId();
    }

    private void sendRestRegistration() throws IOException, SDKException {

        EventEndpoint eventEndpoint = new EventEndpoint();
        eventEndpoint.setEvent(Action.INSTANTIATE_FINISH);
        eventEndpoint.setNetworkServiceId(getNSRId());
        String url = "http://localhost:" + server.getAddress().getPort() + "/"+name;
        eventEndpoint.setEndpoint(url);
        eventEndpoint.setName(name);
        eventEndpoint.setType(EndpointType.REST);
        this.requestor.getEventAgent().requestPost(eventEndpoint);
    }

    class MyHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            InputStream is = t.getRequestBody();
            String message = read(is);

            if(checkRequest(message))
                try
                {
                    wakeUp();
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            String response = "";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private boolean checkRequest(String message) {
            JsonElement jsonElement=mapper.fromJson(message, JsonElement.class);

            String actionReceived= jsonElement.getAsJsonObject().get("action").getAsString();
            //log.debug(name+": action received: " + actionReceived);
            if(actionReceived.equals(Action.INSTANTIATE_FINISH.toString()))
                return true;
            return false;
        }

        private String read(InputStream is) throws UnsupportedEncodingException {

            BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;

            try
            {
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //log.debug(name+": something received:" + responseStrBuilder.toString());
            return responseStrBuilder.toString();
        }
        public synchronized void await() throws InterruptedException {
            //log.debug(name+": waits... (max "+getTimeout()+" seconds)");
            wait(getTimeout()*1000);
        }
        private synchronized void wakeUp() throws InterruptedException {
            notify();
        }
    }

    @Override
    protected void handleException(Exception e) {
        log.error("Exception "+name+" : there was an exception: " + e.getMessage());
        e.printStackTrace();
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
