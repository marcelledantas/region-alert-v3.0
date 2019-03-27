package br.com.meslin.alert.interSCity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import br.com.meslin.alert.connection.HTTPConnection;
import br.com.meslin.alert.connection.HTTPException;
import br.com.meslin.alert.model.Alert;
import br.com.meslin.alert.model.MyGroup;
import br.com.meslin.alert.model.Person;
import br.com.meslin.alert.util.Debug;
import br.com.meslin.alert.util.Sequencial;

/**
 * 
 * @author meslin
 *
 */
public class InterSCity {
	private HTTPConnection connection;
//	private Map<String, String> ordemUUIDMap;

	/**
	 * Constructor
	 * @param connection an HTTP connection to the InterSCity platform
	 */
	public InterSCity(HTTPConnection connection) {
		this.connection = connection;

//		ordemUUIDMap = new HashMap<String, String>();
	}
	/**
	 * Constructor<br>
	 * Creates an HTTPConnection HTTP connction object with default parameters<br>
	 */
	public InterSCity() {
		this(new HTTPConnection());
	}
	
	/**
	 * Constructor<br>
	 * @param interSCityIPAddress
	 */
	public InterSCity(String interSCityIPAddress) {
		this(new HTTPConnection(interSCityIPAddress));
	}

	/**
	 * Creates a new resource<br>
	 * Register new resources<br>
	 * /adaptor/resources<br>
	 * Method: POST<br>
	 *<br>
	 *  {<br>
	 *  	"data": {<br> 
	 *  		"description": "A city alert",<br> 
	 *  		"capabilities": [<br>
	 *  			"city_alerting"
	 *  		],<br> 
	 *  		"status": "active",<br> 
	 *  		"lat": -23.559616,<br> 
	 *  		"lon": -46.731386<br> 
	 *  	}<br> 
	 *  }<br>
	 *<br>
	 *  Answer:<br>
	 * 	{<br>
  	 *		"data": {<br>
     *			"uuid": "45b7d363-86fd-4f81-8681-663140b318d4",<br>
     *			"description": "A city alert",<br>
     *			"capabilities": [<br>
	 *  			"ordem",<br>
	 *  			"linha",<br> 
	 *  			"velocidade" <br>
     *			],<br>
     *  		"status": "active",<br>
     *  		"lat": -23.559616,<br>
     *  		"lon": -46.731386,<br>
     *  		"country": "Brazil",<br>
     *  		"state": "São Paulo",<br>
     *  		"city": "São Paulo",<br>
     *  		"neighborhood": "Butantã",<br>
     *  		"postal_code": null,<br>
     *  		"created_at": "2017-12-27T13:25:07.176Z",<br>
     *  		"updated_at": "2017-12-27T13:25:07.176Z",<br>
     *  		"id": 10<br>
     * 		}<br>
     * 	}<br>
     *<br>
	 * @param interSCityData
	 * @param group
	 * @return UUID String
	 * @throws Exception 
	 */
	private String createNewResource(InterSCityData interSCityData, MyGroup group) throws Exception {
		String uuid =null;
		String response = null;

		if(interSCityData instanceof Alert) {
			// create a new Alert
			JSONObject data = new JSONObject();
			data.put("description", "An alert");
			data.accumulate("capabilities", "city_alerting");
			data.accumulate("capabilities", "uv");	// workaround for the one postion JSON array bug - capability not used
			data.put("status", "active");
			data.put("lat", group.getLatitude());
			data.put("lon", group.getLongitude());

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("data", data);

			try {
				response = connection.sendPost("adaptor/resources", jsonObject.toString());
			} catch (IOException | HTTPException e) {
				Debug.warning("resource not created", e);
				throw new Exception(e.getMessage());
			}
			jsonObject = new JSONObject(response);
			uuid = jsonObject.getJSONObject("data").getString("uuid");
			
			if(uuid == null) {
				Debug.warning("Null UUID for " + data.toString());
			}
			return uuid;
		}
		else if(interSCityData instanceof Person) {
			// create a new Person
			JSONObject data = new JSONObject();
			data.put("description", "A person");
			data.accumulate("capabilities", "person");
			data.accumulate("capabilities", "uv");	// workaround for the one postion JSON array bug - capability not used
			data.put("status", "active");

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("data", data);

			try {
				response = connection.sendPost("adaptor/resources", jsonObject.toString());
			} catch (IOException | HTTPException e) {
				Debug.warning("resource not created");
			}
			jsonObject = new JSONObject(response);
			uuid = jsonObject.getJSONObject("data").getString("uuid");
			
			if(uuid == null) {
				Debug.warning("Null UUID for " + data.toString());
			}
			return uuid;
		}
		else {
			Debug.warning("unknown resource type to create");
			return null;
		}
	}

	/**
	 * Update InterSCity database<br>
	 * InterSCity commands<br>
	 * <ul>
	 * <li>
	 * <li>curl -X GET  "http://localhost:8000/catalog/capabilities"
	 * <li>curl -X POST "http://localhost:8000/adaptor/resources/" + uuid + "/data" -H "Content-Type: application/json" -d jsonobject
	 * </ul>
	 * @param interSCityData
	 * @throws Exception 
	 */
	public void updateDB(InterSCityData interSCityData) throws Exception {
		String uuid = null;
		@SuppressWarnings("unused")
		String response = null;
		
		if(interSCityData instanceof Alert) {
			Alert alert = (Alert) interSCityData;
			JSONArray cityAlerting = new JSONArray();
			for(MyGroup group : alert.getGroups()) {
				uuid = createNewResource(interSCityData, group);
				if(uuid == null) {
					Debug.warning("Resource alert not created");
					return;
				}
				
				// if everything is ok, create the alert in this format
				// an alert for every group (area)
				/*
				 * {
				 * 		"data": {
				 * 			"city_alerting": [
				 * 				{
				 * 					"seq": 123,
				 * 					"text": "Danger! Danger! Danger!",
				 * 					"timestamp": "2017-06-14T17:52:25.428Z",
				 * 					"endTimestamp": "2017-06-14T19:52:25.428Z",
				 * 					"group": 12,
				 * 					"lat": -12,
				 * 					"lon": -32
				 * 				}, ...
				 * 				...
				 * 			]
				 * 		}
				 * }
				 */
				
				// create the JSON string
				JSONObject capabilities = new JSONObject();
				capabilities.put("seq", Sequencial.getInstance().getSeq());
				capabilities.put("text", alert.getText());
				capabilities.put("endTimestamp", alert.getEndTimestamp());
				capabilities.put("group", group.getGroup());
				capabilities.put("timestamp", alert.getStartTimestamp());
				cityAlerting.put(capabilities);
			}
			JSONObject data = new JSONObject();
			data.put("city_alerting", cityAlerting);
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("data", data);
			String jsonString = jsonObject.toString(2).replace("[[", "[").replace("]]", "]");
			connection = new HTTPConnection();
			try {
				response = connection.sendPost("adaptor/resources/" + uuid + "/data", jsonString);
			} catch (IOException | HTTPException e) {
				Debug.warning("Could not update resource\n" + jsonString, e) ;
			}
		}
		else if(interSCityData instanceof Person) {
			// TODO InterSCity received an instance of PERSON! Do something (I don't know what to do by now!!!)
		}
	}



	
	
	
	/**
	 * Checks the InterSCity:<br>
	 * <ul>
	 * <li>Availability</li>
	 * <li>Existence of the required capabilities (creates if not available)</li>
	 * </ul>
	 * InterSCity commands:
	 * <ul>
	 * <li>curl -X GET  "http://localhost:8000/catalog/capabilities"</li>
	 * <li>curl -X POST "http://localhost:8000/catalog/capabilities" -H "Content-Type: application/json" -d '{"name": "city_alerting", "description": "City alert", "capability_type": "sensor"}'</li>
	 * </ul>
	 * @throws HTTPException 
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public void checkInterSCity() throws MalformedURLException, IOException, HTTPException {
		String response;
		Boolean found;
		
		// the availability is checked using the services
		
		// check for the existence of the city_alerting capability
		//connection = new HTTPConnection();
		try {
			response = connection.sendGet("catalog/capabilities", "");
		} catch (Exception e) {
			Debug.warning("Error while checking InterSCity connection (shutted down?)", e);
			return;
		}
		
		if(response == null) return;
		
		JSONArray capabilities = new JSONObject(response).getJSONArray("capabilities");
		
		// check for alert capability
		found = false;
		for(int i=0; i<capabilities.length(); i++) {
			if(((String)((JSONObject)capabilities.get(i)).get("name")).equals("city_alerting")) {
				found = true;
			}
		}
		if(!found) {
			// alert capability not found, need to be created now!
			Debug.info("Capability alert not found, creating one");
			String jsonString = "{ \"name\": \"city_alerting\", \"description\": \"City alert\", \"capability_type\": \"sensor\" }";
			Debug.warning("jsonString = " + jsonString);
			response = connection.sendPost("catalog/capabilities", jsonString);
		}
		else {
			Debug.info("Capability alert found!");
		}
		// check for person capability
		found = false;
		for(int i=0; i<capabilities.length(); i++) {
			if(((String)((JSONObject)capabilities.get(i)).get("name")).equals("person")) {
				found = true;
			}
		}
		if(!found) {
			// person capability not found, need to be created now!
			Debug.info("Capability person not found, creating one");
			String data = "{ \"name\": \"person\", \"description\": \"Person\", \"capability_type\": \"sensor\" }";
			response = connection.sendPost("catalog/capabilities", data);
		}
		else {
			Debug.info("Capability person found!");
		}
	}

	/**
	 * 
	 * @return
	 */
	public String getIpAddress() {
		return connection.getIpAddress();
	}
	
	/**
	 * Lookup InterSCity for alerts by areas<br>
	 * @param groups
	 * @return
	 */
	public List<Alert> getAlertListByArea(HashSet<Integer> groups) {
		// no areas => nothing to do, just return
		if(groups == null || groups.size() == 0) return null;
		List<Integer> areas = new ArrayList<Integer>();
		for(int area : groups) {
			areas.add(area);
		}
		return getAlertListByArea(areas);
	}
	/**
	 * Lookup InterSCity for alerts by areas<br>
	 * @param areas area List
	 * @return an Alert List
	 */
	public List<Alert> getAlertListByArea(List<Integer> areas) {
		// no areas => nothing to do, just return
		if(areas == null || areas.size() == 0) return null;
		
		String response;
		List<Alert> alerts = new ArrayList<Alert>();
		
		String qs = "capability=city_alerting";
		for(int area : areas) {
			qs += "&group.in[]=" + area;
		}
		
		/*
		 * Connection: alert:8000/discovery/resources?capability=city_alerting&group.in[]=73&group.in[]=60
		 * Response:
		 * {
		 *     "resources": [
		 *         {
		 *             "id": 10,
		 *             "uri": null,
		 *             "created_at": "2019-03-15T19:14:22.333Z",
		 *             "updated_at": "2019-03-15T19:14:22.333Z",
		 *             "lat": -22.9631480868568,
		 *             "lon": -43.3693979286854,
		 *             "status": "active",
		 *             "collect_interval": null,
		 *             "description": "An alert",
		 *             "uuid": "3a4acbf2-3e60-4a4e-a27e-a0072ff2ce87",
		 *             "city": null,
		 *             "neighborhood": null,
		 *             "state": null,
		 *             "postal_code": null,
		 *             "country": null,
		 *             "capabilities": [
		 *                 "city_alerting",
		 *                 "uv"
		 *             ]
		 *         },...
		 *         ...
		 *     ]
		 * }
		 */
		try {
			Debug.info("Getting sensors using discovery/resources?" + qs);
			response = connection.sendGet("discovery/resources", qs);
		} catch (Exception e) {
			Debug.warning("Could not get resources by area: ", e);
			return null;
		}
		
		/*
		 * This is an example of response:
		 * {
		 *     "resources": [
		 *         {
		 *             "uuid": "12a36493-eddc-4fd6-bc3f-903f4458e2a3",
		 *             "capabilities": {
		 *                 "city_alerting": [
		 *                     {
		 *                         "text": "Danger at Grajau!",
		 *                         "endTimestamp": "Sun Mar 31 14:02:00 BRT 2019",
		 *                         "seq": 6,
		 *                         "group": 60,
		 *                         "date": "2019-03-15T19:10:24.809Z"
		 *                     }
		 *                 ]
		 *             }
		 *         }
		 *     ]
		 * }
		 */
		JSONArray jsonResources = new JSONObject(response).getJSONArray("resources");
		Debug.info("There are " + jsonResources.length() + " resources with alert for this user\nbecause the answer was " + response);
		
		for(int i=0; i< jsonResources.length(); i++) {
			JSONObject jsonResource = getResourceDataByUUID(jsonResources.getJSONObject(i).getString("uuid"));
			Alert alert = new Alert();
			try {
				// this came from the previous query
				alert.setStartTimestamp(jsonResources.getJSONObject(i).getString("updated_at"));
				// and these came from this query
				JSONArray resources = jsonResource.getJSONArray("resources"); 
				alert.setEndTimestamp(resources.getJSONObject(0).getJSONObject("capabilities").getJSONArray("city_alerting").getJSONObject(0).getString("endTimestamp"));
				alert.setText(resources.getJSONObject(0).getJSONObject("capabilities").getJSONArray("city_alerting").getJSONObject(0).getString("text"));
				alerts.add(alert);
			} catch (Exception e) {
				Debug.warning("Unknown error (I do NOT know how to react to it)", e);
			}
		}
		
		return alerts;
	}
	
	/**
	 * Returns the resource data by UUID
	 * @param uuid
	 * @return the resource data in JSON format or null in case of error
	 */
	private JSONObject getResourceDataByUUID(String uuid) {
		JSONObject response;
		try {
			response = new JSONObject(connection.sendGet("collector/resources/" + uuid + "/data", ""));
		} catch (Exception e) {
			Debug.warning("Could not get resource data by UUID " + uuid, e);
			return null;
		}
		return response;
	}
}
