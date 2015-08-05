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
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.integration.test.testers.*;
import org.project.openbaton.integration.test.utils.SubTask;
import org.project.openbaton.integration.test.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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
	private final static String CONF_FILE_PATH = "/etc/openbaton/integration-test-scenarios";;


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
	public static void main(String[] args) throws IOException {

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



		Ini ini=new Ini();

		File f = loadFileIni(args);
		if (f.isDirectory())
			for (File file : f.listFiles())
				runTestScenario(ini, properties, file);
		else
			runTestScenario(ini,properties,f);
		log.info("Test finished correctly :)");
		System.exit(0);
	}

	private static void runTestScenario(Ini ini, Properties properties, File file) throws IOException {
		ini.load(new FileReader(file));

		Ini.Section root = ini.get("it");
		SubTask rootSubTask = loadTesters(properties, root);
		try {
			rootSubTask.call();
		} catch (Exception e) {
			e.printStackTrace();
			exit(8);
		}
		// set the maximum time (in seconds) of the Integration tests. e.g. 10 min = 600 seconds
		rootSubTask.awaitTermination(600);
	}

	private static File loadFileIni(String[] args) throws FileNotFoundException {
		File f;
		if(args.length>1) {
			f = new File(args[1]);
			if (f != null && f.exists()) {
				return f;
			}
		}
		log.info("the name file passed is incorrect");
		f=new File(CONF_FILE_PATH);
		if(f.exists())
			return f;
		if(!f.exists())
		{
			log.info("the name file " + CONF_FILE_PATH + " is incorrect");
			return new File(MainIntegrationTest.class.getResource("/integration-test-scenarios").getPath());
		}
		throw new FileNotFoundException();
	}

	private static SubTask loadTesters(Properties properties, Profile.Section root) {
		/**Get some global properties**/

		/****************************/

		String firstChildName=root.childrenNames()[0];
		return loadEntity(properties, root.getChild(firstChildName));
	}
	private static SubTask loadInstance (Properties properties, Profile.Section currentChild){
		String[] splittedName = currentChild.getSimpleName().split("-");
		String nameClass = getNameClass(splittedName);
		SubTask instance=null;
		try {
			String classNamePath = "org.project.openbaton.integration.test.testers." + nameClass;
			Class<?> currentClass = MainIntegrationTest.class.getClassLoader().loadClass(classNamePath);
			instance = (SubTask) currentClass.getConstructor(Properties.class).newInstance(properties);
			log.debug("Class is:" + instance.getClass().getName());
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
		return instance;
	}

	private static String getNameClass(String[] splittedName) {
		String nameClass=null;
		switch(splittedName[0]) {
			case "vim":	nameClass = getNameClassWithAction("VimInstance", splittedName[1]);break;
			case "nsd":	nameClass = getNameClassWithAction("NetworkServiceDescriptor", splittedName[1]);break;
			case "nsr":	nameClass = getNameClassWithAction("NetworkServiceRecord", splittedName[1]);break;
		}
		return nameClass;
	}

	private static String getNameClassWithAction(String nameClass, String action) {
		String nameClassWithAction=null;
		switch (action){
			case "c": nameClassWithAction=nameClass+"Create";break;
			case "w": nameClassWithAction=nameClass+"Wait";break;
			case "d": nameClassWithAction=nameClass+"Delete";break;
		}
		return nameClassWithAction;
	}

	private static SubTask loadEntity(Properties properties, Profile.Section currentChild) {

		SubTask instance = loadInstance(properties, currentChild);
		//If there are specific properties for a type of a tester in the configuration file (.ini)
		configureTester(instance, currentChild);
		String successorRemover = getSuccessorRemover(currentChild);

		for (String subChild : currentChild.childrenNames()) {
			log.debug("SubChild is:" + subChild);
			int instances = Integer.parseInt(currentChild.getChild(subChild).get("num_instances", "1"));
			log.debug("Num instances is:" + instances);
			if(!successorRemover.equals("false") && successorRemover.equals(subChild))
			{
				instance.setSuccessorRemover(loadEntity(properties,currentChild.getChild(subChild)));
			}
			else
			{
				for (int i = 0; i < instances; i++)
					instance.addSuccessor(loadEntity(properties, currentChild.getChild(subChild)));
			}
		}
		return instance;
	}
	private static String getSuccessorRemover(Profile.Section currentSection) {
		return currentSection.get("successor-remover","false");
	}
	private static void configureTester(SubTask instance, Profile.Section currentSection) {
		if(instance instanceof VimInstanceCreate)
			configureVimInstanceCreate(instance,currentSection);
		else if (instance instanceof NetworkServiceDescriptorCreate)
			configureNetworkServiceDescriptorCreate(instance, currentSection);
		else if (instance instanceof NetworkServiceDescriptorDelete)
			configureNetworkServiceDescriptorDelete(instance, currentSection);
		else if (instance instanceof NetworkServiceDescriptorWait)
			configureNetworkServiceDescriptorWaiterWait(instance, currentSection);
		else if (instance instanceof NetworkServiceRecordDelete)
			configureNetworkServiceRecordDelete(instance, currentSection);
		else if (instance instanceof NetworkServiceRecordCreate)
			configureNetworkServiceRecordCreate(instance, currentSection);
		else if (instance instanceof NetworkServiceRecordWait)
			configureNetworkServiceRecordWait(instance, currentSection);
	}

	private static void configureNetworkServiceDescriptorWaiterWait(SubTask instance, Profile.Section currentSection) {
		//cast and get specific properties
	}

	private static void configureNetworkServiceDescriptorDelete(SubTask instance, Profile.Section currentSection) {
		//cast and get specific properties
	}

	private static void configureNetworkServiceRecordWait(SubTask instance, Profile.Section currentSection) {
		NetworkServiceRecordWait w = (NetworkServiceRecordWait) instance;
		w.setTimeout(Integer.parseInt(currentSection.get("timeout", "5")));
		String action = currentSection.get("action");
		w.setAction(Action.valueOf(action));
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