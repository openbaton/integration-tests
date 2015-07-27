package org.project.openbaton.integration.test.parser;

import com.google.gson.*;
import org.project.openbaton.integration.test.utils.Tester;
import org.project.openbaton.integration.test.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Created by mob on 27.07.15.
 */
public class Parser {

    private Map<String, String> propertyMap = new HashMap<>();
    private static Properties properties;
    private static Logger log = LoggerFactory.getLogger(Parser.class);
    private static Map<String,String> namesAlreadyReplicated;

    public static JsonElement randomize(String json, String path){

        GsonBuilder builder = new GsonBuilder();
        Gson mapper = builder.create();
        JsonElement jsonRoot = mapper.fromJson(json, JsonElement.class);

        properties = new Properties();
        try {
            properties.load(Parser.class.getResourceAsStream(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        namesAlreadyReplicated=new HashMap<>();

        return parse(jsonRoot);
    }

    private static JsonElement parse(JsonElement jsonRoot) {
        if(jsonRoot instanceof JsonObject)
        {
            JsonObject jsonRootO = (JsonObject) jsonRoot;
            for (Map.Entry<String,JsonElement> entry : jsonRootO.entrySet()) {
                JsonElement elementValue = entry.getValue();
                if(checkCompatibility(elementValue))
                {
                    String nameToReplace = getNameToReplace(elementValue.getAsString());
                    log.debug("Replicating: " + elementValue.getAsString() + " with: " + nameToReplace);

                    jsonRootO.addProperty(entry.getKey(), nameToReplace);
                }
                else
                    parse(elementValue);
            }
        }
        else if(jsonRoot instanceof JsonArray)
        {
            JsonArray jsonRootA = (JsonArray) jsonRoot;
            for(JsonElement jsonElement : jsonRootA)
            {
                parse(jsonElement);
            }
        }
        return jsonRoot;
    }

    private static boolean checkCompatibility(JsonElement elementValue) {
        if(elementValue instanceof JsonPrimitive)
        {
            JsonPrimitive jsonPrimitive = (JsonPrimitive) elementValue;
            if(jsonPrimitive.isString())
                if(jsonPrimitive.getAsString().startsWith("<::") && jsonPrimitive.getAsString().endsWith("::>")){
                    return true;
                }
        }
        return false;
    }

    public static String getNameToReplace(String nameToReplace){

        String name=nameToReplace.replace("<::","").replace("::>","");
        if(namesAlreadyReplicated.containsKey(name))
            return namesAlreadyReplicated.get(name);

        String nameFromProperties=(String) properties.get(name);
        if(nameFromProperties==null) {
            log.error("A properties doesn't exist");
            return "";
        }
        StringBuilder valueFromProperties = new StringBuilder(nameFromProperties);

        for(int i=0 ; i<valueFromProperties.length(); i++)
        {
            if(valueFromProperties.charAt(i)=='*'){
                valueFromProperties.setCharAt(i, ("" + ((int) (Math.random() * 10000) % 9)).charAt(0));
            }
        }
        namesAlreadyReplicated.put(name,valueFromProperties.toString());
        return valueFromProperties.toString();
    }
/*
    public static void main(String[] args){
        String body = Utils.getStringFromInputStream(Tester.class.getResourceAsStream("/etc/json_file/network_service_descriptors/NetworkServiceDescriptor-with-dependencies-without-allacation.json"));
        System.out.println(Parser.randomize(body, "/etc/json_file/network_service_descriptors/nsd1.properties"));
    }*/
}
