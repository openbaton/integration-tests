/* Tiziano Cecamore - 2015*/

/**
 * Error codes:
 * 	1) NFVO not started correctly
 * 	2) VNFM not started correctly
 *	3) Parameters in configuration file (ini) not setted correctly
 * 	*	status > 200:
 *	*	*	7XX) NetworkServiceRecord Test failed
 *  *   *   *	701) create test
 *	*	*	8XX) NetworkServiceDescriptor Test failed
 *  *   *   *	801) create test
 *	*	*	9XX) Vim Test failed
 *  *   *   *	901) create test
 */

package org.project.openbaton.integration.test;

import org.ini4j.Profile;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.integration.test.testers.*;
import org.project.openbaton.integration.test.utils.SubTask;
import org.project.openbaton.integration.test.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.*;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class MainIntegrationTest {


	private static Logger log = LoggerFactory.getLogger(MainIntegrationTest.class);

	private static String nfvoIp;
	private static String nfvoPort;
	private static String nfvoUsr;
	private static String nfvoPsw;
	private static String dbUri;
	private static String dbUsr;
	private static String dbPsw;
	private final static String CONF_FILE_PATH = "/etc/openbaton/integration-test-scenarios";

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
			if (!val) {
				log.debug("vnfm endpoint not present yet");
				return false;
			} else {
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
			if (i > 50) {
				return false;
			}

		}
		return true;
	}

	private static boolean isVnfmReady() {
		int i = 0;
		while (!vnfmReady()) {
			i++;
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (i > 50) {
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


		File f = loadFileIni(args);
		IntegrationTestManager itm = new IntegrationTestManager("org.project.openbaton.integration.test.testers") {
			@Override
			protected void configureSubTask(SubTask subTask, Profile.Section currentSection) {
				if (subTask instanceof VimInstanceCreate)
					configureVimInstanceCreate(subTask, currentSection);
				else if (subTask instanceof NetworkServiceDescriptorCreate)
					configureNetworkServiceDescriptorCreate(subTask, currentSection);
				else if (subTask instanceof NetworkServiceDescriptorDelete)
					configureNetworkServiceDescriptorDelete(subTask, currentSection);
				else if (subTask instanceof NetworkServiceDescriptorWait)
					configureNetworkServiceDescriptorWaiterWait(subTask, currentSection);
				else if (subTask instanceof NetworkServiceRecordDelete)
					configureNetworkServiceRecordDelete(subTask, currentSection);
				else if (subTask instanceof NetworkServiceRecordCreate)
					configureNetworkServiceRecordCreate(subTask, currentSection);
				else if (subTask instanceof NetworkServiceRecordWait)
					configureNetworkServiceRecordWait(subTask, currentSection);
			}
		};
		itm.setLogger(log);
		long startTime, stopTime;
		if (f.isDirectory()) {
			if(f.listFiles().length==0)
				log.info("The directory " + f.getPath() + " is empty");
			for (File file : f.listFiles()) {
				if (file.getName().endsWith(".ini")) {
					startTime = System.currentTimeMillis();
					if (itm.runTestScenario(properties, file)) {
						stopTime = System.currentTimeMillis() - startTime;
						log.info("Test: " + file.getName() + " finished correctly :) in " +
								String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(stopTime), TimeUnit.MILLISECONDS.toSeconds(stopTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(stopTime))));
					} else log.info("Test: " + file.getName() + " completed with errors :(");
				}
			}
		} else {
			if (f.getName().endsWith(".ini")) {
				startTime = System.currentTimeMillis();
				if (itm.runTestScenario(properties, f)) {
					stopTime = System.currentTimeMillis() - startTime;
					log.info("Test: " + f.getName() + " finished correctly :) in " +
							String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(stopTime), TimeUnit.MILLISECONDS.toSeconds(stopTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(stopTime))));
				} else log.info("Test: " + f.getName() + " completed with errors :(");
			}
		}
		System.exit(0);
	}

	private static File loadFileIni(String[] args) throws FileNotFoundException {
		File f;
		if (args.length > 1) {
			f = new File(args[1]);
			if (f.exists()) {
				return f;
			}
		}
		log.info("the name file passed is incorrect");
		f = new File(CONF_FILE_PATH);
		if (f.exists())
			return f;
		if (!f.exists()) {
			log.info("the name file " + CONF_FILE_PATH + " is incorrect");
			return new File(MainIntegrationTest.class.getResource("/integration-test-scenarios").getPath());
		}
		throw new FileNotFoundException();
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
		if (action == null) {
			log.error("action for NetworkServiceRecordWait not setted");
			exit(3);
		}
		w.setAction(Action.valueOf(action));
	}

	private static void configureNetworkServiceRecordCreate(SubTask instance, Profile.Section currentSection) {
		//cast and get specific properties
	}

	private static void configureNetworkServiceRecordDelete(SubTask instance, Profile.Section currentSection) {
		//cast and get specific properties
	}

	private static void configureNetworkServiceDescriptorCreate(SubTask instance, Profile.Section currentSection) {
		NetworkServiceDescriptorCreate w = (NetworkServiceDescriptorCreate) instance;
		w.setFileName(currentSection.get("name-file"));
	}

	private static void configureVimInstanceCreate(SubTask instance, Profile.Section currentSection) {
		VimInstanceCreate w = (VimInstanceCreate) instance;
		w.setFileName(currentSection.get("name-file"));
	}

	private static void exit(int i) {
		System.exit(i);
	}
}