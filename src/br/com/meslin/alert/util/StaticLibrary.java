package br.com.meslin.alert.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import lac.cnclib.net.groups.GroupCommunicationManager;

import org.openstreetmap.gui.jmapviewer.Coordinate;

import br.com.meslin.alert.model.Region;

/**
 * 
 * @author meslin
 *
 */
public class StaticLibrary {
	/*
	 * constants
	 */
	public static final int DATAHORA = 0;
	public static final int ORDEM = 1;
	public static final int LINHA = 2;
	public static final int LATITUDE = 3;
	public static final int LONGITUDE = 4;
	public static final int VELOCIDADE = 5;

	public static final String USER_AGENT = "Mozilla/5.0";

	
	/*
	 * global command line configuration
	 */
	/** run as in a headless environment */
	public static boolean forceHeadless = false; 
	/** ContextNet IP address */
	public static String contextNetIPAddress;
	/** ContextNet TCP port number */
	public static int contextNetPortNumber;
	
	
	
	/** interval in ms (interval to create a thread */
	public static final long interval = 5000;
	public static long intervalBetweenThreads = 500;
	/** in % (interval variance to create a thread) */
	public static final long variance = 20;		// 

	
	
	/*
	 * statistics
	 */
	public static long nMessages = 0;
	/** start time - negative value means that there is no start time setted yet */
	public static long startTime = -1;
	/** stop time */
	public static long stopTime;
	
	
	
	/*
	 * Global data
	 */
	/** Core UUID */
	public static UUID coreUUID;
	/** número da mensagem */
	public static long sequencial;
	/** passenger group type */
	public static final int PASSENGER_GROUP = 0;
	public static GroupCommunicationManager groupManager;


	
	/**
	 * Constructor<br>
	 */
	public StaticLibrary() {
		nMessages = 0;
		startTime = -1;
	}
	
	

	/**
	 * Handles files, jar entries, and deployed jar entries in a zip file (EAR).
	 * 
	 * @return The date if it can be determined, or null if not.
	 */
	public static Date getClassBuildTime() {
		Date d = null;
		Class<?> currentClass = new Object() {}.getClass().getEnclosingClass();
		URL resource = currentClass.getResource(currentClass.getSimpleName() + ".class");
		if (resource != null) {
			if (resource.getProtocol().equals("file")) {
				try {
					d = new Date(new File(resource.toURI()).lastModified());
				} catch (URISyntaxException ignored) {
				}
			} else if (resource.getProtocol().equals("jar")) {
				String path = resource.getPath();
				d = new Date(new File(path.substring(5, path.indexOf("!"))).lastModified());
			} else if (resource.getProtocol().equals("zip")) {
				String path = resource.getPath();
				File jarFileOnDisk = new File(path.substring(0, path.indexOf("!")));
				// long jfodLastModifiedLong = jarFileOnDisk.lastModified ();
				// Date jfodLasModifiedDate = new Date(jfodLastModifiedLong);
				try (JarFile jf = new JarFile(jarFileOnDisk)) {
					ZipEntry ze = jf.getEntry(path.substring(path.indexOf("!") + 2));	// Skip the ! and the /
					long zeTimeLong = ze.getTime();
					Date zeTimeDate = new Date(zeTimeLong);
					d = zeTimeDate;
				} catch (IOException | RuntimeException ignored) {
				}
			}
		}
		return d;
	}

	
	
	/**
	 * Reads the filenames file<br>
	 * This file has a filename per line<br>
	 * Each filename represents a region (group) on the map<br>
	 * @param workdir work directory ending with a slash
	 * @param filename name of the file with filenames
	 * @return list of regions (without coordinates)
	 */
	public static List<Region> readFilenamesFile(String workdir, String filename) {
		// read the file composed by a filename per line
		BufferedReader br = null;
		List<Region> regions = new ArrayList<Region>();
		
		try {
			br = new BufferedReader(new FileReader(workdir + filename));
		} catch (IOException e0) {
			try {
				workdir = "/media/meslin/643CA9553CA92352/Users/meslin/Google Drive/workspace-desktop-ubuntu/RegionAlert/";
				br = new BufferedReader(new FileReader(workdir + filename));
				String line;
				while((line = br.readLine()) != null)
				{
					String[] data = line.split(",");
					Region region = new Region();
					region.setNumber(Integer.parseInt(data[0]));
					region.setFilename(data[1].trim());
					regions.add(region);
				}
			}
			catch (IOException e) {
				Debug.warning("Error while reading " + workdir + filename, e);
				Debug.warning("Available files are:");
				File directory = new  File(".");
				File[] files = directory.listFiles();
				for(File file: files) {
					Debug.warning(file.getAbsolutePath());
				}
			}
			finally {
				if(br != null) {
					try {
						br.close();
					}
					catch (IOException e) {
						Debug.warning("Could not close " + workdir + filename, e);
					}
				}
			}
		}
		return regions;
	}
	
	
	
	/**
	 * Reads a region from a given file<br>
	 * @param filename	name of the file describing a region
	 * @return a region
	 */
	public static List<Coordinate> readRegion(String workdir, String filename) {
		// reads a region. A region is described by an X, Y coordinate per line
		List<Coordinate> points = new ArrayList<Coordinate>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(workdir + filename));
		} catch (IOException e0) {
			try {
				workdir = "/media/meslin/643CA9553CA92352/Users/meslin/Google Drive/workspace-desktop-ubuntu/RegionAlert/";
				br = new BufferedReader(new FileReader(workdir + filename));
				String line;
				while((line = br.readLine()) != null) {
					Coordinate coordinate = new Coordinate(
							Double.parseDouble(line.substring(0, line.indexOf(" ")).trim()),
							Double.parseDouble(line.substring(line.indexOf(" ")).trim())
							);
					points.add(coordinate);
				}
			}
			catch (IOException e) {
				Debug.warning("Could not read region file " + workdir + filename, e);
				Debug.warning("Available files are:");
				File directory = new  File(".");
				File[] files = directory.listFiles();
				for(File file: files) {
					Debug.warning(file.getAbsolutePath());
				}
			}
			finally {
				if(br != null) {
					try {
						br.close();
					}
					catch (IOException e) {
						System.err.println("Date = " + new Date());
						e.printStackTrace();
					}
				}
			}
		}
		return points;
	}

	
	
	/**
	 * Reads and returns a text file
	 * @param filename
	 * @return text file content
	 */
	public static String readFile(String filename) {
		Debug.warning("Reading file " + filename);
		
		// read the file composed by a filename per line
		BufferedReader br = null;
		String buffer = "";
		
		try
		{
			br = new BufferedReader(new FileReader(filename));
			String line;
			while((line = br.readLine()) != null) {
				buffer += line.trim();
			}
		}
		catch (IOException e)
		{
			System.err.println("Date = " + new Date());
			e.printStackTrace();
		}
		finally {
			if(br != null) {
				try {
					br.close();
				}
				catch (IOException e) {
					System.err.println("Date = " + new Date());
					e.printStackTrace();
				}
			}
		}
		return buffer;
	}
}
