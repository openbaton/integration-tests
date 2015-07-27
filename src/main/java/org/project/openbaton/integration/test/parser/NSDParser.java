package org.project.openbaton.integration.test.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.project.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.openbaton.integration.test.utils.Tester;
import org.project.openbaton.integration.test.utils.Utils;

import java.util.*;

/**
 * Created by mob on 27.07.15.
 */
public class NSDParser {

    private static Map<String, String> vnfdMap = new HashMap<>();

    public static String randomize(String json, String param, String value){
        String result=null;
        GsonBuilder builder = new GsonBuilder();
        Gson mapper = builder.create();
        Map<String, Object> javaRootMapObject = mapper.fromJson(json, Map.class);


        //
        for(String element : javaRootMapObject.keySet()){
            if(element.equals(param))
                javaRootMapObject.get(param);
        }

        //


        ArrayList vnfdList = (ArrayList) javaRootMapObject.get("vnfd");

        for(Object vnfdOb : vnfdList){
            Map<String, Object> vnfd = (Map<String, Object>) vnfdOb;
            String vnfdName = (String) vnfd.get("name");
            vnfdMap.put(vnfdName, vnfdName + Math.random());
            System.out.println(vnfdMap.get(vnfdName));
        }

        ArrayList listOfDependencies = (ArrayList)javaRootMapObject.get("vnf_dependency");
        for(Object dependencies : listOfDependencies){

        Map<String, Object> mapDependencies = (Map<String, Object>)dependencies;

        String sourceName = (String)((Map<String, Object>)mapDependencies.get("source")).get("name");
        ((Map<String, Object>)mapDependencies.get("source")).put("name", vnfdMap.get(sourceName));

        String target= (String)((Map<String, Object>)mapDependencies.get("target")).get("name");
        ((Map<String, Object>)mapDependencies.get("target")).put("name",vnfdMap.get(target));
        System.out.println("Source: " + ((Map<String, Object>) mapDependencies.get("source")).get("name") + "\nTarget:" + ((Map<String, Object>) mapDependencies.get("target")).get("name"));
        }

        return result;
    }

    public static void main(String[] args){
        String body = Utils.getStringFromInputStream(Tester.class.getResourceAsStream("/etc/json_file/network_service_descriptors/NetworkServiceDescriptor-with-dependencies-without-allacation.json"));
        NSDParser.randomize(body,"name","vimdummy");
    }
}
