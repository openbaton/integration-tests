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

import org.project.openbaton.catalogue.nfvo.VimInstance;
import org.project.openbaton.integration.test.testers.NetworkServiceDescriptorTest;
import org.project.openbaton.integration.test.testers.NetworkServiceRecordTest;
import org.project.openbaton.integration.test.testers.VimInstanceCreateTest;
import org.project.openbaton.integration.test.utils.SubTask;
import org.project.openbaton.integration.test.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
			exit(2); // 1 stands for the error in running nfvo TODO define error codes (doing)
		}

		log.info("Vnfm is started correctly");

		/******************************
		 * Now create the VIM		  *
		 ******************************/

		log.debug("Properties: " + properties);

		SubTask vimInstanceCreateTest = new VimInstanceCreateTest(properties);

		NetworkServiceDescriptorTest networkServiceDescriptorTest = new NetworkServiceDescriptorTest(properties);
//		NetworkServiceDescriptorTest networkServiceDescriptorTest2 = new NetworkServiceDescriptorTest(properties);

		int nsrCreator = 5;
		for (int i=0; i< nsrCreator;i++){

			networkServiceDescriptorTest.addSuccessor(new NetworkServiceRecordTest(properties));
		}

		vimInstanceCreateTest.addSuccessor(networkServiceDescriptorTest);

		VimInstance vimInstanceReceived=null;
		try {
			vimInstanceReceived = (VimInstance) vimInstanceCreateTest.call();
		} catch (Exception e) {
			e.printStackTrace();
		}


		log.debug("Received vim create: " + vimInstanceReceived);
		try {
			assert vimInstanceReceived != null;
		} catch (Exception e) {
			log.error("The vim create test was unsuccessful. Exit now...");
			System.exit(901);
		}
		log.info("Waiting for successors....");
//		vimInstanceCreateTest.shutdownAndAwaitTermination();
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		log.info("Test finished correctly :)");

		System.exit(0);
	}


	private static void exit(int i) {
		System.exit(i);
	}
}