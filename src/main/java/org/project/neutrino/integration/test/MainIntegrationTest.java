/* Tiziano Cecamore - 2015*/

package org.project.neutrino.integration.test;

import org.project.neutrino.nfvo.main.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainIntegrationTest {
	
	private static Logger log = LoggerFactory.getLogger(MainIntegrationTest.class);

	private static final String PROPERTIES_FILE = "/integration-test.properties";
	static ConfigurableApplicationContext context;

	private static String nfvoIp;
	private static String nfvoPort;
	private static String nfvoUsr;
	private static String nfvoPsw;

	private static void loadProperties() throws IOException {

		Properties properties = new Properties();
		properties.load(MainIntegrationTest.class.getResourceAsStream(PROPERTIES_FILE));
		log.debug("Loaded properties: " + properties);
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

		if (!isNfvoStarted()){
			log.error("After 150 sec the Nfvo is not started yet. Is there an error?");
			System.exit(1); // 1 stands for the error in running nfvo TODO define error codes
		}

		log.info("Nfvo is started");

//		testConfiguration();
	}

	private static class Nfvo extends Thread{
		@Override
		public void run() {

			log.info("Starting Nfvo");
//			context = SpringApplication.run(Application.class);
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

	private static boolean isNfvoStarted() {
		int i = 0;
		while (!available(nfvoIp, nfvoPort)) {
			log.debug("waiting the server to start");
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

	public static boolean available(String host, String port) {
		try {
			Socket s = new Socket(host, Integer.parseInt(port));
			log.info("Server is listening on port " + port + " of " + host);
			s.close();
			return true;
		} catch (IOException ex) {
			// The remote host is not listening on this port
			log.warn("Server is not listening on port " + port	+ " of " + host);
			return false;
		}
	}

}
