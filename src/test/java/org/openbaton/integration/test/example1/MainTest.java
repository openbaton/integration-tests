package org.openbaton.integration.test.example1;

import org.ini4j.Profile;
import org.openbaton.integration.test.example1.Tasks.Task3;
import org.openbaton.integration.test.IntegrationTestManager;
import org.openbaton.integration.test.utils.SubTask;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by mob on 18.08.15.
 */
public class MainTest {

    public static void main(String[] args) {
        Properties properties=new Properties();
        File file= new File(MainTest.class.getResource("/file.ini").getPath());
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
        long startTime = System.currentTimeMillis();
        boolean result=false;
        try {
            result = itm.runTestScenario(properties, file);
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
