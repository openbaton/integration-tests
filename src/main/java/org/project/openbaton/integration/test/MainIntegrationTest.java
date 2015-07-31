/* Tiziano Cecamore - 2015*/

/**
 * Error codes:
 * 	1) NFVO not started correctly
 * 	2) VNFM not started correctly
 *
 * 	*	status > 200:
 *	*	*	7XX) NetworkServiceRecord Test failed
 *  *   *   *	701) create test
 *	*	*	8XX) NetworkServiceDescriptor Test failed
 *  *   *   *	801) create test
 *	*	*	9XX) Vim Test failed
 *  *   *   *	901) create test
 */

package org.project.openbaton.integration.test;

import org.ini4j.Ini;
import org.ini4j.Profile;
import org.project.openbaton.integration.test.testers.*;
import org.project.openbaton.integration.test.utils.SubTask;
import org.project.openbaton.integration.test.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.Properties;

public class MainIntegrationTest {


	private static Logger log = LoggerFactory.getLogger(MainIntegrationTest.class);

	private static String nfvoIp;
	private static String nfvoPort;
	private static String nfvoUsr;
	private static String nfvoPsw;
	private static String dbUri;
	private static String dbUsr;
	private static String dbPsw;
	private final static String CONF_FILE_PATH = "/etc/openbaton/integration-test.ini";;


	private static Properties loadProperties() throws IOException {
		Properties properties = Utils.getProperties();
		nfvoIp = properties.getProperty("nfvo-ip");
		nfvoPort = properties.getProperty("nfvo-port");
		nfvoUsr = properties.getProperty("nfvo-usr");
		nfvoPsw = properties.getProperty("nfvo-psw");
		dbUsr = properties.getProperty("db-usr");
		dbPsw = properties.getProperty("db-psw");
		dbUri = properties.getProperty("db-uri");
		return properties;
	}

	public static boolean vnfmReady() {
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = DriverManager.getConnection(dbUri, dbUsr, dbPsw);

			st = con.createStatement();
			rs = st.executeQuery("select * from vnfm_manager_endpoint");

			boolean val = rs.next(); //next() returns false if there are no-rows retrieved
			if(val==false){
				log.debug("vnfm endpoint not present yet");
				return false;
			}else
			{
				return true;
			}

		} catch (SQLException ex) {
			log.debug("error db");
			ex.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
				if (con != null) {
					con.close();
				}

			} catch (SQLException ex) {
				log.debug("error db");
			}
		}
		return false;
	}

	private static boolean isNfvoStarted(String nfvoIp, String nfvoPort) {
		int i = 0;
		while (!Utils.available(nfvoIp, nfvoPort)) {
			i++;
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (i > 50){
				return false;
			}

		}
		return true;
	}

	private static boolean isVnfmReady(){
		int i = 0;
		while (!vnfmReady()) {
			i++;
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (i > 50){
				return false;
			}

		}
		return true;
	}

	// TODO move to test
	public static void main(String[] args){

		System.out.println(log.getClass());
		Properties properties = null;
		try {
			properties = loadProperties();
		} catch (IOException e) {
			e.printStackTrace();
		}

		/******************************
		 * Running NFVO				  *
		 ******************************/

		if (!isNfvoStarted(nfvoIp, nfvoPort)) {
			log.error("After 150 sec the Nfvo is not started yet. Is there an error?");
			System.exit(1); // 1 stands for the error in running nfvo TODO define error codes (doing)
		}

		log.info("Nfvo is started");

		/******************************
		 * Running VNFM				  *
		 ******************************/

		if (!isVnfmReady()) {
			log.error("After 150 sec the Vnfm is not started yet. Is there an error?");
			exit(2); // 2 stands for the error in running vnfm TODO define error codes (doing)
		}

		log.info("Vnfm is started correctly");

		/******************************
		 * Now create the VIM		  *
		 ******************************/

		log.debug("Properties: " + properties);

		Ini ini=loadFileIni(args);

		Ini.Section root = ini.get("it");
		SubTask rootSubTask = loadTesters(properties, root);
		try {
			rootSubTask.call();
		} catch (Exception e) {
			e.printStackTrace();
		}
		rootSubTask.awaitTermination();
		log.info("Test finished correctly :)");
		System.exit(0);
	}

	private static Ini loadFileIni(String[] args) {
		File f=null;
		if(args.length>1) {
			f = new File(args[1]);
		}
		InputStream is=null;
		if(f==null || !f.exists() || f.isDirectory()){
			log.info("the name file passed is incorrect");

			f=new File(CONF_FILE_PATH);
			if(!f.exists() || f.isDirectory())
			{
				log.info("the name file " + CONF_FILE_PATH + " is incorrect");
				is =MainIntegrationTest.class.getResourceAsStream("/integration-test_properties.ini");
			}
		}
		Ini ini = new Ini();
		try {
			if(is==null)
				ini.load(new FileReader(f));
			else{
				ini.load(is);
				is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ini;
	}

	private static SubTask loadTesters(Properties properties, Profile.Section root) {

		SubTask instance = null;

		for (String child : root.childrenNames()) {
			String[] splittedName = child.split("-");
			String nameClass = splittedName[0] + splittedName[1].substring(0, 1).toUpperCase() + splittedName[1].substring(1);

			try {

				String className = "org.project.openbaton.integration.test.testers." + nameClass;
				log.debug("Classname is:" + className);
				Class<?> currentClass = MainIntegrationTest.class.getClassLoader().loadClass(className);
				instance = (SubTask) currentClass.getConstructor(Properties.class).newInstance(properties);
				log.debug("Class is:" + instance.getClass());

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}

			//If there are specific properties for a type of a tester in the configuration file (.ini)
			configureTester(instance,root.getChild(child));

			for(String subChild : root.getChild(child).childrenNames()){
				log.debug("SubChild is:" + subChild);
				int instances=Integer.parseInt(root.getChild(child).getChild(subChild).get("num_instances", "1"));
				log.debug("Num instances is:" + instances);
				for(int i=0; i<instances;i++)
					instance.addSuccessor(loadTesters(properties, root.getChild(child)));
			}

		}
		return instance;
	}

	private static void configureTester(SubTask instance, Profile.Section currentSection) {
		if(instance instanceof VimInstanceCreate)
			configureVimInstanceCreate(instance,currentSection);
		else if (instance instanceof NetworkServiceDescriptorCreate)
			configureNetworkServiceDescriptorCreate(instance,currentSection);
		else if (instance instanceof NetworkServiceDescriptorDelete)
			configureNetworkServiceDescriptorDelete(instance, currentSection);
		else if (instance instanceof NetworkServiceRecordDelete)
			configureNetworkServiceRecordDelete(instance, currentSection);
		else if (instance instanceof NetworkServiceRecordCreate)
			configureNetworkServiceRecordCreate(instance, currentSection);
		else if (instance instanceof NetworkServiceRecordWaiterWait)
			configureWaiterWait(instance,currentSection);
	}

	private static void configureNetworkServiceDescriptorDelete(SubTask instance, Profile.Section currentSection) {
		NetworkServiceDescriptorDelete nSDD= (NetworkServiceDescriptorDelete) instance;
		nSDD.setNSRCreated(Integer.parseInt(currentSection.getParent().getParent().getParent().get("num_instances")));
	}

	private static void configureWaiterWait(SubTask instance, Profile.Section currentSection) {
		NetworkServiceRecordWaiterWait w = (NetworkServiceRecordWaiterWait) instance;
		w.setTimeout(Integer.parseInt(currentSection.get("timeout","5")));
	}

	private static void configureNetworkServiceRecordCreate(SubTask instance, Profile.Section currentSection) {
		//cast and get specific properties
	}

	private static void configureNetworkServiceRecordDelete(SubTask instance, Profile.Section currentSection) {
		//cast and get specific properties
	}

	private static void configureNetworkServiceDescriptorCreate(SubTask instance, Profile.Section currentSection) {
		//cast and get specific properties
	}

	private static void configureVimInstanceCreate(SubTask subTask, Profile.Section currentSection) {
		//cast and get specific properties
	}


	private static void exit(int i) {
		System.exit(i);
	}
}