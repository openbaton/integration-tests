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
package org.openbaton.integration.test.example1;

import org.ini4j.Profile;
import org.openbaton.integration.test.example1.Tasks.Task3;
import org.openbaton.integration.test.IntegrationTestManager;
import org.openbaton.integration.test.utils.SubTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by mob on 18.08.15.
 */
public class MainTest {

    public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger(MainTest.class);
        Properties properties=new Properties();
        File file= new File(MainTest.class.getResource("/file.ini").getPath());
        URL url = MainTest.class.getResource("/file.ini");
        IntegrationTestManager itm = new IntegrationTestManager("org.openbaton.integration.test.example1.Tasks") {
            @Override
            protected void configureSubTask(SubTask subTask, Profile.Section currentSection) {
                if(subTask instanceof Task3){
                    Task3 t3 = (Task3) subTask;
                    // set a parameter specificated in the ini file in the section [it/task1-1/task2-1/task3-1]
                    // If "specific-parameter" is not present in the ini file, "5" will be the default
                    t3.setSpecificParameter(Double.parseDouble(currentSection.get("specific-parameter", "5")));
                }
            }};
        itm.setLogger(log);
        long startTime = System.currentTimeMillis();
        boolean result=false;
        try {
            System.out.println(url);
            result = itm.runTestScenario(properties, url, "file.ini");
        } catch (IOException e) {
            e.printStackTrace();
        }
        long stopTime = System.currentTimeMillis() - startTime;
        if(result)
            System.out.println("Test: " + file.getName() + " finished correctly :) in " +
                    String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(stopTime), TimeUnit.MILLISECONDS.toSeconds(stopTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(stopTime))));
        else
            System.out.println("Test: " + file.getName() + " completed with errors :(");
    }
}
