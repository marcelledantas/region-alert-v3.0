/**
 * 
 */
package br.com.meslin.alert.contextnet;

import java.awt.GraphicsEnvironment;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import main.java.ckafka.GroupDefiner;
import main.java.ckafka.GroupSelection;

//import org.json.JSONObject;
import org.openstreetmap.gui.jmapviewer.Coordinate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import br.com.meslin.alert.main.MyContextNetCore;
import br.com.meslin.alert.model.MobileObject;
import br.com.meslin.alert.model.PersonSituation;
import br.com.meslin.alert.model.Region;
import br.com.meslin.alert.util.Debug;
import br.com.meslin.alert.util.StaticLibrary;
import ckafka.data.Swap;

/**
 * @author meslin
 *
 */
public class MyGroupSelector implements GroupSelection {
	
	private List<Region> regionList;
	/** maps persons (string id) to a hashset of groups (list of integer) */
	private Map<String, HashSet<Integer>> personGroup;

	/**
	 * Creates a Group Definer based on ContextNet IP address, ContextNet port number and region description filename 
	 * @param filename
	 */
	public MyGroupSelector(String workdir, String filename) {
		this.personGroup = new HashMap<String, HashSet<Integer>>();
		this.regionList = StaticLibrary.readFilenamesFile(workdir, filename);
		
		/*
		 * reads each region file
		 */
		for(Region region : regionList) {
			region.setPoints(StaticLibrary.readRegion(workdir, region.getFilename()));
		}
		
		/*
		 * Create GroupDefiner 
		 */
        ObjectMapper objectMapper = new ObjectMapper();
        Swap swap = new Swap(objectMapper);
        new GroupDefiner(this, swap);

	}
	
	/* (non-Javadoc)
	 * @see 
	 */
	public Set<Integer> getNodesGroupByContext(ObjectNode contextInfo) {
		boolean isNewPerson;
		MobileObject mobileObject = null;
		HashSet<Integer> difGroups;
		
		// how to handle message from a real Mobile-Hub? Details at http://wiki.lac.inf.puc-rio.br/doku.php?id=m_hub
		try {
			/*
			 * if a nodeMessage cames from M-Hub, its is a JSON string coded as byte[] like this:
			 * 	{
			 * 		"uuid":"9509494b-b270-4cd7-a5a2-08cc6bb998d1",
			 * 		"source":"1-34947689C447",
			 * 		"action":"found",
			 * 		"signal":-67,
			 * 		"latitude":-22.938382,
			 * 		"longitude":-43.192847,
			 * 		"tag":"SensorData",
			 * 		"timestamp":1551904249,
			 * 
			 * 		"username": "username fake"
			 * }
			 */
			
		// TODO: verify if MobileObject needs to be adjusted
			
			//mobileObject = new MobileObject(contextInfo);
			
		} catch (Exception e) {
			Debug.warning("Could not create a mobile object", e);
		}

		HashSet<Integer> newGroups = new HashSet<Integer>(2, 1);
		// search the regions where the mobileObject may be
		
		for(Region region : regionList) {
			
			Coordinate coordinate = new Coordinate(mobileObject.getLatitude(), mobileObject.getLongitude());
			
			if (region.contains(coordinate)) {
			newGroups.add(region.getNumber());
		}
	}
		// TODO: check how to extract ID without using mobileObject
		// check if this mobile object enter a new group
		difGroups = (HashSet<Integer>) newGroups.clone();
		if(this.personGroup.containsKey(mobileObject.getId())) {
			// gets the all new groups removing old groups from the new groups
			difGroups.removeAll(this.personGroup.get(mobileObject.getId()));
			isNewPerson = false;
		}
		else {
			isNewPerson = true;
		}
		if(difGroups.size() > 0) {
			// send a message to PN with difGroups
			PersonSituation personSituation = new PersonSituation(mobileObject, isNewPerson, difGroups);
			MyContextNetCore.dataToPNQueue.add(personSituation);
			synchronized (MyContextNetCore.dataToPNQueue) {
				MyContextNetCore.dataToPNQueue.notify();
			}
		}
		this.personGroup.put(mobileObject.getId(), newGroups);
		
		return newGroups;
	}
	
	/*
	 * Groups definition
	 */
	public Set<Integer> groupsIdentification()
	{
		Set<Integer> setOfGroups = new HashSet<Integer>();
		for(Region region : regionList)
		{
			setOfGroups.add(region.getNumber());
		}
		
		return setOfGroups;
	}

	@Override
	public String kafkaConsumerPrefix() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String kafkaProducerPrefix() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
