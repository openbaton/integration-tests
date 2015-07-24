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
import org.project.openbaton.integration.test.exceptions.IntegrationTestException;
import org.project.openbaton.integration.test.testers.NetworkServiceDescriptorTest;
import org.project.openbaton.integration.test.testers.VimInstanceCreateTest;
import org.project.openbaton.integration.test.utils.SubTask;
import org.project.openbaton.integration.test.utils.Utils;
import org.project.openbaton.sdk.api.exception.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class MainIntegrationTest {

	private static String NFVO_VERSION = "0.5-SNAPSHOT";
	private static String VNFM_VERSION = "0.3-SNAPSHOT";

	private static Logger log = LoggerFactory.getLogger(MainIntegrationTest.class);

	private static String nfvoIp;
	private static String nfvoPort;
	private static String nfvoUsr;
	private static String nfvoPsw;
	private static String dbUri;
	private static String dbUsr;
	private static String dbPsw;

	private static final String VNFM_DUMMY_PATH = "../dummy-vnfm-jms/build/libs/";
	private static String NFVO_FILE_NAME = "openbaton-" + NFVO_VERSION + ".jar";
	private static final String VNFM_FILE_NAME = "dummy-vnfm-jms-" + VNFM_VERSION + ".jar";

	private static Nfvo nfvo;
	private static Vnfm vnfm;

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
			Integer exitVal = null;
			try{
				exitVal=nfvo.getProcess().exitValue();
			}catch (IllegalThreadStateException e){
				log.debug("waiting the server to start");
			}
			if (exitVal != null && exitVal != 0){
				log.error("NFVO not started correctly");
				exit(2);
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
			Integer exitVal=null;
			try{
				exitVal=vnfm.getProcess().exitValue();
			}catch (IllegalThreadStateException e){
				log.debug("waiting for vnfm registration...");
			}
			if (exitVal!=null && exitVal != 0){
				log.error("VNFM not started correctly: exitval="+exitVal);
				exit(2);
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

		nfvo = new Nfvo();
		try {
			nfvo.start();
		} catch (IntegrationTestException e) {
			e.printStackTrace();
			exit(1);
		}

		if (!isNfvoStarted(nfvoIp, nfvoPort)) {
			log.error("After 150 sec the Nfvo is not started yet. Is there an error?");
			System.exit(1); // 1 stands for the error in running nfvo TODO define error codes (doing)
		}

		log.info("Nfvo is started");

		/******************************
		 * Running VNFM				  *
		 ******************************/

		vnfm = new Vnfm();
		try {
			vnfm.start();
		} catch (IntegrationTestException e) {
			e.printStackTrace();
			exit(2);
		}

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
		NetworkServiceDescriptorTest networkServiceDescriptorTest2 = new NetworkServiceDescriptorTest(properties);

		vimInstanceCreateTest.addSuccessor(networkServiceDescriptorTest);
		vimInstanceCreateTest.addSuccessor(networkServiceDescriptorTest2);

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
		vimInstanceCreateTest.shutdownAndAwaitTermination();

		log.info("Test finished correctly :)");
		vnfm.getProcess().destroy();
		nfvo.getProcess().destroy();
		System.exit(0);
	}

	private static class Vnfm {
		
		private Process process;

		public void start() throws IntegrationTestException {
			try {
				log.info("Starting Vnfm");
				String pathVnfm = VNFM_DUMMY_PATH + VNFM_FILE_NAME;
				File f = new File(pathVnfm);
				if (!f.exists() || f.isDirectory()) {
					throw new IntegrationTestException("File " + pathVnfm + " doesn't exist. Have you compiled the VNFM v" + VNFM_VERSION);
				}
				ProcessBuilder processBuilder = new ProcessBuilder().command("java", "-jar", pathVnfm);
				processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
				process = processBuilder.start();
				log.info("Vnfm started!");
			} catch (IOException e) {
				log.error("Vnfm class: Vnfm not started correctly");
				e.printStackTrace();
				exit(2);
			}
		}

		public Process getProcess() {
			return process;
		}

		public void setProcess(Process process) {
			this.process = process;
		}
	}


	private static class Nfvo {

		private Process process;

		public void start() throws IntegrationTestException {
			try {
				//TODO need to be downloaded from git
//				log.info("Compiling Nfvo");
//				process = new ProcessBuilder().command ("../nfvo/gradlew", "clean", "build", "-x", "test", "install").start();
//				process.waitFor();
				log.info("Starting Nfvo");
				String pathNFVO = "../nfvo/build/libs/" + NFVO_FILE_NAME;
				File f = new File(pathNFVO);
				if (!f.exists() || f.isDirectory()) {
					throw new IntegrationTestException("File " + pathNFVO + " doesn't exist. Have you compiled the NFVO v" + NFVO_VERSION);
				}
				ProcessBuilder processBuilder = new ProcessBuilder().command("java", "-jar", pathNFVO);
//				processBuilder.inheritIO();
				processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
				process = processBuilder.start();
//				process.waitFor();
			} catch (IOException e) {
				log.error("Nfvo not started correctly");
				e.printStackTrace();
				System.exit(2);
			}
		}

		public Process getProcess() {
			return process;
		}

		public void setProcess(Process process) {
			this.process = process;
		}

	}

	private static void exit(int i) {
		if (nfvo.getProcess() != null)
			nfvo.getProcess().destroy();
		if (vnfm.getProcess() != null)
			vnfm.getProcess().destroy();
		System.exit(i);
	}
}