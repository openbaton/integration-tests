package org.openbaton.integration.test;

import org.ini4j.Ini;
import org.ini4j.Profile;
import org.openbaton.integration.test.utils.SubTask;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * Created by mob on 18.08.15.
 */
public abstract class IntegrationTestManager {
    private int maxIntegrationTestTime;
    private int maxConcurrentSuccessors;
    private Logger log=null;
    private String classPath;

    public IntegrationTestManager(String classPath){
        this.classPath=classPath;
    }
    public boolean runTestScenario(Properties properties, File file) throws IOException {
        Ini ini=new Ini();
        ini.load(new FileReader(file));
        SubTask rootSubTask = loadTesters(properties, ini.get("it"));
        try {
            rootSubTask.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootSubTask.awaitTermination();
    }

    public void setLogger(Logger log){
        this.log=log;
    }

    private SubTask loadInstance (Properties properties, Profile.Section currentChild){
        String nameClass = currentChild.get("class-name");
        SubTask instance=null;
        try {
            String classNamePath = classPath +"."+ nameClass;
            Class<?> currentClass = MainIntegrationTest.class.getClassLoader().loadClass(classNamePath);
            instance = (SubTask) currentClass.getConstructor(Properties.class).newInstance(properties);
        } catch (ClassNotFoundException e) {
            log("Problem during class loading: " + e.getMessage(), "error");
        } catch (InstantiationException e) {
            log("Problem during class loading: " + e.getMessage(),"error");
        } catch (IllegalAccessException e) {
            log("Problem during class loading: " + e.getMessage(),"error");
        } catch (NoSuchMethodException e) {
            log("Problem during class loading: " + e.getMessage(),"error");
        } catch (InvocationTargetException e) {
            log("Problem during class loading: " + e.getMessage(),"error");
        }
        return instance;
    }

    private SubTask loadEntity(Properties properties, Profile.Section currentChild) {

        SubTask instance = loadInstance(properties, currentChild);
        if(instance==null)
            throw new NullPointerException();
        //If there are specific properties for a type of a tester in the configuration file (.ini)
        configureSubTask(instance, currentChild);
        String successorRemover = getSuccessorRemover(currentChild);
        instance.setMaxIntegrationTestTime(maxIntegrationTestTime);
        instance.setMaxConcurrentSuccessors(maxConcurrentSuccessors);

        for (String subChild : currentChild.childrenNames()) {
            //log("SubChild is:" + subChild, "debug");
            int numInstances = Integer.parseInt(currentChild.getChild(subChild).get("num_instances", "1"));
            if(!successorRemover.equals("false") && successorRemover.equals(subChild))
            {
                instance.setSuccessorRemover(loadEntity(properties,currentChild.getChild(subChild)));
            }
            else
            {
                for (int i = 0; i < numInstances; i++)
                    instance.addSuccessor(loadEntity(properties, currentChild.getChild(subChild)));
            }
        }
        return instance;
    }

    protected abstract void configureSubTask(SubTask subTask, Profile.Section currentSection);

    private SubTask loadTesters(Properties properties, Profile.Section root) {
        /**Get some global properties**/
        maxIntegrationTestTime = Integer.parseInt(root.get("max-integration-test-time","600"));
        maxConcurrentSuccessors = Integer.parseInt(root.get("max-concurrent-successors","10"));

        log("maxIntegrationTestTime = " + maxIntegrationTestTime, "info");
        log("maxConcurrentSuccessors = " + maxConcurrentSuccessors, "info");
        /****************************/

        return loadEntity(properties, root.getChild(root.childrenNames()[0]));
    }

    private String getSuccessorRemover(Profile.Section currentSection) {
        return currentSection.get("successor-remover", "false");
    }

    private void log(String message, String level){
        if(log!=null){
            switch (level){
                case "error": log.error(message);break;
                case "info" : log.info(message);break;
                case "warn" : log.warn(message);break;
                case "debug" : log.debug(message);break;
            }
        }
        else System.out.println(message);
    }
}
