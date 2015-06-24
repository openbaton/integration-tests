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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainIntegrationTest {
	
	private static Logger log = LoggerFactory
			.getLogger(MainIntegrationTest.class);

	static ConfigurableApplicationContext context;

	public static void main(String[] args) throws ClassNotFoundException,
			SQLException, FileNotFoundException, IOException,
			URISyntaxException, InterruptedException, ExecutionException {
		testConfiguration();
	}

	public static boolean available(String host, int port) {
		try {
			Socket s = new Socket(host, port);
			log.warn("Server is listening on port " + port + " of "
					+ host);
			s.close();
			return true;
		} catch (IOException ex) {
			// The remote host is not listening on this port
			log.error("Server is not listening on port " + port
					+ " of " + host);
			return false;
		}
	}
	
   public static boolean vnfmReady()
   {
    Connection con = null;
    Statement st = null;
    ResultSet rs = null;

    String url = "jdbc:mysql://localhost:3306/openbaton";
    String user = "root";
    String password = "root";

    try {
        con = DriverManager.getConnection(url, user, password);
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


	public static void testConfiguration() throws ClassNotFoundException,
			SQLException, FileNotFoundException, IOException,
			URISyntaxException, InterruptedException, ExecutionException {
		

	  Runnable vnfm = new Runnable() {
			public void run() {
			
			try {
				Runtime.getRuntime().exec("java -jar /home/tce/neutrino/vnfm/dummy-vnfm/build/libs/dummy-vnfm.jar" );
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				log.error(" VNFN NOT STARTED");
				e1.printStackTrace();
			}
            			

			}
		};
				
		Runnable openbaton = new Runnable() {
			public void run() {
				

				log.info("################################# TEST CONFIGURATION #############################################");
				context = SpringApplication.run(Application.class);
			}
		};

		new Thread(openbaton).start();

		while (!available("127.0.0.1", 8080)) {
			log.debug("waiting the server to start");
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		log.info("OPENBATON STARTED!!!");
		//log.info("Working Directory = " +
		//log.info("user.dir"));
		
		new Thread(vnfm).start();
		
		while (!vnfmReady()) {
			log.debug("waiting for vnfm registration...");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		log.info("VNFN STARTED!!!");
		
        
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
