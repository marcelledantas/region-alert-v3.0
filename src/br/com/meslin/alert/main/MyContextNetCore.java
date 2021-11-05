/**
 * 
 */
package br.com.meslin.alert.main;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import lac.cnet.groupdefiner.components.GroupDefiner;
import lac.cnet.groupdefiner.components.groupselector.GroupSelector;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import br.com.meslin.alert.connection.Constants;
import br.com.meslin.alert.connection.HTTPException;
import br.com.meslin.alert.contextnet.MyGroupSelector;
import br.com.meslin.alert.contextnet.MyProcessingNode;
import br.com.meslin.alert.interSCity.InterSCity;
import br.com.meslin.alert.interSCity.InterSCityConsumer;
import br.com.meslin.alert.interSCity.InterSCityData;
import br.com.meslin.alert.util.Debug;
import br.com.meslin.alert.util.StaticLibrary;

/**
 * @author meslin
 * 
 * This application creates a core environment with a Processing Node thread and
 * It also creates a consumer to send data to a InterSCity 
 *
 */
public class MyContextNetCore {
	// From: https://stackoverflow.com/questions/56397206/gluon-maps-doesnt-load-the-map-and-throws-an-exception
	static {
	    System.setProperty("http.agent", "Gluon Mobile/1.0.3");
	}
	/*
	 * Configuration parameters
	 */
	/** InterSCity IP address */
	private static String interSCityIPAddress;
	/** group description region file */
	private static String workdir;
	/** group description file name */
	private static String filename;

	/** An interface to InterSCity */
	private static InterSCity interSCity;
//	public static Logger log = Logger.getLogger(MyContextNetCore.class);
	/** stores a queue of bus data to be sent to the InterSCity */
	public static ConcurrentLinkedQueue<InterSCityData> mobileObjectQueue = new ConcurrentLinkedQueue<InterSCityData>();
	
	/** A queue to send information (objects) to the ContextNet Processing Node */
	public static ConcurrentLinkedQueue<Object> dataToPNQueue = new ConcurrentLinkedQueue<Object>();

	/**
	 * 
	 */
	public MyContextNetCore() {
		// HTTP agent to request map tiles
		String httpAgent = System.getProperty("http.agent");
		if (httpAgent == null) {
		    httpAgent = "(" + System.getProperty("os.name") + " / " + System.getProperty("os.version") + " / " + System.getProperty("os.arch") + ")";
		}
		System.setProperty("http.agent", "RegionAlert/1.0 " + httpAgent);
	}

	/**
	 * @param args
	 * @throws HTTPException 
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) {
		// Build date
		final Date buildDate = StaticLibrary.getClassBuildTime();
		System.out.println("BenchmarMyCore builed at " + buildDate);
		
		// get command line options
		Options options = new Options();
		Option option;
		
		option = new Option("a", "address", true, "ContextNet Gateway IP address");
		option.setRequired(false);
		options.addOption(option);

		option = new Option("f", "groupfilename", true, "Group description filename");
		option.setRequired(true);
		options.addOption(option);
		
		option = new Option("h", "force-headless", false, "Run as in a headless environment");
		option.setRequired(false);
		options.addOption(option);

		option = new Option("i", "InterSCity", true, "InterSCity IP address");
		option.setRequired(true);
		options.addOption(option);
		
		option = new Option("p", "port", true, "ContextNet Gateway TCP port number");
		option.setRequired(false);
		options.addOption(option);

		option = new Option("w", "workdir", true, "Directory where the group description filename is");
		option.setRequired(true);
		options.addOption(option);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;
		
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println("Date = " +  new Date());
			formatter.printHelp("MyContextNetCore", options);
			e.printStackTrace();
			return;
		}
		
		// getting command line options
		// ContextNet IP address
		if((StaticLibrary.contextNetIPAddress = cmd.getOptionValue("address")) == null) {
			StaticLibrary.contextNetIPAddress = Constants.GATEWAY_IP;
		}
		// group description filename
		workdir = cmd.getOptionValue("workdir");
		filename = cmd.getOptionValue("groupfilename");
		// ContextNet TCP port number
		try {
			StaticLibrary.contextNetPortNumber = Integer.parseInt(cmd.getOptionValue("port"));
		} catch(Exception e) {
			StaticLibrary.contextNetPortNumber = Constants.GATEWAY_PORT;
		}
		// InterSCity IP address
		interSCityIPAddress = cmd.getOptionValue("InterSCity");	// null if not available, but, by now, it is mandatory, so never NULL
		
		StaticLibrary.forceHeadless = cmd.hasOption("force-headless");

		StaticLibrary.nMessages = 0;		// for statistics

		/*
		 * Catch Ctrl+C
		 */
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
		    	long elapsedTime = StaticLibrary.stopTime - StaticLibrary.startTime;
		    	System.err.println("CTRL+C");
		    	System.err.println("Time: " +  elapsedTime + " (" + StaticLibrary.stopTime + " - " + StaticLibrary.startTime + ") with " + StaticLibrary.nMessages + " messages");
		    }
		});

		System.out.println("\n\nStarting ContextNet Core using gateway at " + StaticLibrary.contextNetIPAddress + ":" + StaticLibrary.contextNetPortNumber + "\n\n");
		System.out.println("Ready, set...");

		// check and set InterSCity capabilities
		interSCity = new InterSCity(interSCityIPAddress);
		try {
			interSCity.checkInterSCity();
		} catch (IOException | HTTPException e) {
			Debug.warning("Please, start InterSCity platform using this command:\n"
					+ "( cd /opt/InterSCity/dev-env ; ./project start )\n", e);
		}

		/*
		 * Creating GroupSelector
		 */
		GroupSelector groupSelector = new MyGroupSelector(workdir, filename);
		new GroupDefiner(groupSelector);
		
		/*
		 * Create Processing Node
		 */
		new MyProcessingNode(interSCityIPAddress, mobileObjectQueue);

		/*
		 * Create a thread to send user data to the InterSCity
		 */
		Thread consumer = new Thread(new InterSCityConsumer(interSCity, mobileObjectQueue));
		consumer.start();
		
		System.out.println("\nGO!");
		while(true) {}
	}
}
