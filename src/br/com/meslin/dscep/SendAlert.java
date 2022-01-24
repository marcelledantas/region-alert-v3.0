/**
 * 
 */
package br.com.meslin.dscep;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.meslin.alert.connection.HTTPConnection;
import br.com.meslin.alert.connection.HTTPException;
import br.com.meslin.alert.model.MyGroup;

/**
 * @author meslin
 *
 */
public class SendAlert {
	/** List of regions involved in the alert */
	private List<MyGroup> regions;
	/** alert message */
	private String msg;
	/** start timestamp (date and time) */ 
	private Date startTimestamp;
	/** end timestamp (date and time) */
	private Date endTimestamp;
	/** event duration in milliseconds */
	private long duration;
	/** endpoint URL + TCP port (IP or FQDN) */
	private String endPoint;
	/** an object representing the HTTP connection */
	private HTTPConnection connection;
	
	/**
	 * Constructor
	 */
	public SendAlert() {
		regions = new ArrayList<MyGroup>();
	}
	/**
	 * Constructor
	 * @param regions a list of regions (MyGroup)
	 * @param msg
	 * @param start
	 * @param duration
	 * @param endPoint
	 * @throws Exception 
	 */
	public SendAlert(List<MyGroup> regions, String msg, Date startTimestamp, long duration, String endPoint) throws Exception {
		this();
		this.regions = regions;
		this.msg = msg;
		this.startTimestamp = startTimestamp;
		this.duration = duration;
		endTimestamp = new Date(startTimestamp.getTime() + duration);
		
		connection = new HTTPConnection(endPoint);
	}
	
	public int sendAlert() throws MalformedURLException, IOException, HTTPException {
		// create the query string
		String qs = "startTimestamp=" + this.startTimestamp;
		qs += "&endTimestamp=" + this.endTimestamp;
		qs += "&text=" + msg;
		for(MyGroup region : regions) {
			qs += "&areas=" + region.getGroup() + "#" 
		       + region.getLatitude() + "#" 
			   + region.getLongitude();
		}
		connection.sendPost("PutAlert", qs);
		
		return 0;
	}


	/**
	 * @return the region list
	 */
	public List<MyGroup> getRegions() {
		return regions;
	}

	/**
	 * @param regions the region list to set
	 */
	public void setRegions(List<MyGroup> regions) {
		this.regions = regions;
	}
	/**
	 * Adds a region to the region list
	 * @param region a region to add to the region list
	 */
	public void addRegion(MyGroup region) {
		this.regions.add(region);
	}

	/**
	 * @return the message
	 */
	public String getMsg() {
		return msg;
	}

	/**
	 * @param msg the message to set
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}

	/**
	 * @return the duration
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * Set alert duration. Stat time must be set first, otherwise, a null pointer exception will be throw.
	 * @param duration the duration to set
	 * @throws NullPointerException if startTimestamp not set
	 */
	public void setDuration(long duration) throws NullPointerException {
		this.duration = duration;
		endTimestamp = new Date(startTimestamp.getTime() + duration);
	}

	/**
	 * @return the start date/time
	 */
	public Date getStartTimestamp() {
		return startTimestamp;
	}

	/**
	 * @param start the start date/time to set
	 */
	public void setStartTimestamp(Date startTimestamp) {
		this.startTimestamp = startTimestamp;
	}
	/**
	 * @return the endPoint IP or FQDN address and TCP port 
	 */
	public String getEndPoint() {
		return endPoint;
	}
	/**
	 * @param endPoint the endPoint (IP or FQDN) to set including TCP port (please, *DO NOT* include protocol)
	 */
	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}
}
