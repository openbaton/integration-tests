/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
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
package org.openbaton.integration.test.parser;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

/**
 * Created by mob on 27.07.15.
 *
 * The Parser is used to obtain a new json file from a json file passed as a parameter to the method
 * randomize. The new file has different values accordingly to the Parser configuration file.
 *
 */
public class Parser {

  private static Properties properties;
  private static Logger log = LoggerFactory.getLogger(Parser.class);
  private static Gson mapper = new GsonBuilder().create();
  private static Map<String, String> namesAlreadyReplicated;

  /**
   * This method takes as input: json: the old json file we want to modify
   *
   * Output: A new json file with some changed parameters accordingly to the Parser configuration
   * file
   *
   */
  public synchronized static String randomize(String json, String path) throws IOException {
    if (json == null || path == null) throw new NullPointerException("json or path is null!");
    properties = new Properties();
    properties.load(new FileInputStream(new File(path)));
    namesAlreadyReplicated = new HashMap<>();
    JsonElement jsonRoot = mapper.fromJson(json, JsonElement.class);
    return mapper.toJson(parse(jsonRoot));
  }

  private static JsonElement parse(JsonElement jsonRoot) {
    if (jsonRoot instanceof JsonObject) {
      JsonObject jsonRootO = (JsonObject) jsonRoot;
      for (Map.Entry<String, JsonElement> entry : jsonRootO.entrySet()) {
        JsonElement elementValue = entry.getValue();
        if (checkCompatibility(elementValue)) {
          String nameToReplace = getNameToReplace(elementValue.getAsString());
          //log.debug("Replicated: " + entry.toString() + " with: "+entry.getKey()+"=\""+nameToReplace+"\"");
          jsonRootO.addProperty(entry.getKey(), nameToReplace);
        } else parse(elementValue);
      }
    } else if (jsonRoot instanceof JsonArray) {
      JsonArray jsonRootA = (JsonArray) jsonRoot;
      for (JsonElement jsonElement : jsonRootA) {
        parse(jsonElement);
      }
    }
    return jsonRoot;
  }

  private static boolean checkCompatibility(JsonElement elementValue) {
    if (elementValue instanceof JsonPrimitive) {
      JsonPrimitive jsonPrimitive = (JsonPrimitive) elementValue;
      if (jsonPrimitive.isString())
        return jsonPrimitive.getAsString().startsWith("<::")
            && jsonPrimitive.getAsString().endsWith("::>");
    }
    return false;
  }

  private static String getNameToReplace(String nameToReplace) {

    String name = nameToReplace.replace("<::", "").replace("::>", "");
    if (namesAlreadyReplicated.containsKey(name)) return namesAlreadyReplicated.get(name);

    String nameFromProperties = (String) properties.get(name);
    if (nameFromProperties == null) {
      log.error("A properties doesn't exist");
      return "";
    }
    StringBuilder valueFromProperties = new StringBuilder(nameFromProperties);
    Random r = new Random();
    for (int i = 0; i < valueFromProperties.length(); i++) {
      if (valueFromProperties.charAt(i) == '*') {
        valueFromProperties.setCharAt(i, (char) (r.nextInt(26) + 'a'));
      }
    }
    namesAlreadyReplicated.put(name, valueFromProperties.toString());
    return valueFromProperties.toString();
  }
}
