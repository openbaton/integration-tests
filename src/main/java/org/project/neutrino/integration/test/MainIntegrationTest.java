/* Tiziano Cecamore - 2015*/

/**
 * Error codes:
 * 	1) NFVO not started correctly
 * 	2) VNFM not started correctly
 *
 * 	*	status > 200:
 *	*	*	800) NetworkServiceDescriptor Test failed
 *  *   *   *	801) create test
 *	*	*	900) Vim Test failed
 *  *   *   *	901) create test
 */

package org.project.neutrino.integration.test;

import org.project.neutrino.integration.test.utils.Utils;
import org.project.neutrino.nfvo.main.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class MainIntegrationTest {

	private static Logger log = LoggerFactory.getLogger(MainIntegrationTest.class);

	private static String nfvoIp;
	private static String nfvoPort;
	private static String nfvoUsr;
	private static String nfvoPsw;
	private static String dbUri;
	private static String dbUsr;
	private static String dbPsw;

	private static void loadProperties() throws IOException {
		Properties properties = Utils.getProperties();
		nfvoIp = properties.getProperty("nfvo-ip");
		nfvoPort = properties.getProperty("nfvo-port");
		nfvoUsr = properties.getProperty("nfvo-usr");
		nfvoPsw = properties.getProperty("nfvo-psw");
		dbUsr = properties.getProperty("db-usr");
		dbPsw = properties.getProperty("db-psw");
		dbUri = properties.getProperty("db-uri");
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
				return false;
			}else
			{
				return true;
			}

		} catch (SQLException ex) {
			log.debug("error db");
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


	public static void main(String[] args) throws ClassNotFoundException, SQLException, FileNotFoundException, IOException,
			URISyntaxException, InterruptedException, ExecutionException {

		loadProperties();

		Nfvo nfvo = new Nfvo();
		nfvo.start();

		if (!Utils.isNfvoStarted(nfvoIp, nfvoPort)){
			log.error("After 150 sec the Nfvo is not started yet. Is there an error?");
			System.exit(1); // 1 stands for the error in running nfvo TODO define error codes (doing)
		}

		log.info("Nfvo is started");

		Vnfm vnfm = new Vnfm();
		vnfm.start();

		int i = 0;
		while (!vnfmReady()) {
			log.debug("waiting for vnfm registration...");
			i++;
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (i > 50){
				log.error("After 150 sec the Nfvo is not started yet. Is there an error?");
				System.exit(2); // 1 stands for the error in running nfvo TODO define error codes (doing)
			}
		}

		boolean vimCreateResult = VimInstanceTest.create();

		log.debug("Received vim create: " + vimCreateResult);
		try {
			assert vimCreateResult;
		}catch (Exception e){
			log.error("The vim create test was unsuccessful. Exit now...");
			System.exit(901);
		}

		boolean nsdCreateResult = NetworkServiceDescriptorTest.create();

		log.debug("Received NetworkServiceDescriptor create: " + nsdCreateResult);
		try {
			assert nsdCreateResult;
		}catch (Exception e){
			log.error("The NetworkServiceDescriptor create test was unsuccessful. Exit now...");
			System.exit(801);
		}

		log.info("Test finished correctly :)");

	}

	private static class Vnfm extends Thread{
		@Override
		public void run() {
			try {
				log.info("Starting Vnfm");
//				SpringApplication.run(DummyJMSVNFManager.class);
				Runtime.getRuntime().exec("java -jar ../vnfm/dummy-vnfm/build/libs/dummy-vnfm-0.3-SNAPSHOT.jar" );
			} catch (IOException e) {
				log.error("Vnfn not started correctly");
				e.printStackTrace();
				System.exit(2);
			}
		}
	}

	private static class Nfvo extends Thread{
		@Override
		public void run() {

			log.info("Starting Nfvo");
			SpringApplication.run(Application.class);
		}
	}

}
