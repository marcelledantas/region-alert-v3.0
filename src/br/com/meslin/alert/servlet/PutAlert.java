package br.com.meslin.alert.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lac.cnclib.net.groups.Group;
import br.com.meslin.alert.connection.HTTPException;
import br.com.meslin.alert.interSCity.InterSCity;
import br.com.meslin.alert.model.Alert;
import br.com.meslin.alert.model.MyGroup;
import br.com.meslin.alert.util.Debug;

/**
 * Servlet implementation class PutAlert
 */
@WebServlet(description = "Insert a new Alert in InterSCity platform", urlPatterns = { "/PutAlert" })
public class PutAlert extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String responseMessage;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	ServletContext application = getServletContext();
    	/** ContextNet IP address */
		//String gatewayIP = application.getInitParameter("gatewayIP");
		/** Contextnet UDP port */
		//int gatewayPort = Integer.parseInt(application.getInitParameter("gatewayPort"));
		/** InterSCity ip address */
    	String interSCityIPAddress = application.getInitParameter("interSCityIPAddress");
		InterSCity interSCity = new InterSCity(interSCityIPAddress);

		Alert alert = new Alert();
		
		// text
		alert.setText(request.getParameter("text"));
		
		// start date
		try {
			alert.setStartTimestamp(request.getParameter("startTimestamp"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// end date
		if(request.getParameter("endTimestamp") != null) {
			try {
				alert.setEndTimestamp(request.getParameter("endTimestamp"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// convert minutes to milliseconds and add to the start date in milliseconds,
			// then, converto do Date and set endTimestamp
			
			DateFormat format = new SimpleDateFormat("");
			Date date;
			try {
				date = ((Date) format.parse(alert.getStartTimestamp()));
				alert.setEndTimestamp(new Date(date.getTime() + Integer.parseInt(request.getParameter("deltaTime"))* 60 * 1000));
			} catch (ParseException e) {
				Debug.warning("Wrong date format at date " + alert.getStartTimestamp(), e);
			}			
		}
		
		// area, latitude & longitude
		for(String aux : request.getParameterValues("areas")) {
			// area, latitude & longitude came in a single HTTP parameter separated by #
			String[] areaLatLon = aux.split("#");
			MyGroup group = new MyGroup(areaLatLon[0], areaLatLon[1], areaLatLon[2]);
			alert.addGroup(group);
		}

		responseMessage = "Data stored in InterSCity: " + alert;
		
		try {
			interSCity.updateDB(alert);
		} catch (Exception e) {
			Debug.warning("Alert not created", e);
			responseMessage = "Alert not created. Is InterSCity UP at " + interSCity.getIpAddress() + "?";
		}
		
		List<String> areas = new ArrayList<String>();
		for(Group group : alert.getGroups()) {
			areas.add("" + group.getGroupID());
		}
		try {
			interSCity.sendActuatorCommand("alertListener", areas.toArray(new String[areas.size()]));
		} catch (HTTPException e) {
			responseMessage += " but could not publish that there are new alerts";
		}

		PrintWriter out = response.getWriter();
		response.setContentType("text/plain");
		out.println(responseMessage);
	}
}
