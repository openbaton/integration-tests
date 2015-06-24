/* Tiziano Cecamore - 2015*/

/**
 * Error codes:
 * 	1) NFVO not started correctly
 *
 * 	*	status > 200:
 *	*	*	800) NetworkServiceDescriptor Test failed
 *  *   *   *	801) create test
 *	*	*	900) Vim Test failed
 *  *   *   *	901) create test
 */

package org.project.neutrino.integration.test;

import org.hsqldb.lib.StringInputStream;
import org.project.neutrino.integration.test.utils.Utils;
import org.project.neutrino.nfvo.main.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainIntegrationTest {
	
	private static Logger log = LoggerFactory.getLogger(MainIntegrationTest.class);

	private static String nfvoIp;
	private static String nfvoPort;
	private static String nfvoUsr;
	private static String nfvoPsw;

	private static void loadProperties() throws IOException {
		Properties properties = Utils.getProperties();
		nfvoIp = properties.getProperty("nfvo-ip");
		nfvoPort = properties.getProperty("nfvo-port");
		nfvoUsr = properties.getProperty("nfvo-usr");
		nfvoPsw = properties.getProperty("nfvo-psw");
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
		System.setIn(new StringInputStream("exit"));
	}

	private static class Nfvo extends Thread{
		@Override
		public void run() {

			log.info("Starting Nfvo");
			SpringApplication.run(Application.class);
		}
	}

	public static void testConfiguration() throws ClassNotFoundException, SQLException, IOException, URISyntaxException, InterruptedException, ExecutionException {

		//log.info("Working Directory = " +
		//log.info("user.dir"));


		ExecutorService threadpool = Executors.newFixedThreadPool(1);

		//Configuration task = new Configuration();
		NetworkServiceRecord task = new NetworkServiceRecord();

		log.debug("Submitting Task ...");

		@SuppressWarnings({ "unchecked", "rawtypes" })
		Future future = threadpool.submit(task);
		log.debug("Task is submitted");

		while (!future.isDone()) {
			log.debug("Task is not completed yet....");
			Thread.sleep(1); // sleep for 1 millisecond before checking again
		}

		log.info("Task is completed, let's check result");
		Boolean result = (Boolean) future.get();
		if (result.TRUE) {
			log.info("TRUE");
		} else {
			log.info("FALSE");
		}

		threadpool.shutdown();



	}

}
