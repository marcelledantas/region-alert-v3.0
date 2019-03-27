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

import lac.cnet.groupdefiner.components.groupselector.GroupSelector;
import lac.cnet.sddl.objects.GroupRegion;
import lac.cnet.sddl.objects.Message;

import org.json.JSONObject;

import br.com.meslin.alert.main.BenchmarkMyCore;
import br.com.meslin.alert.model.MobileObject;
import br.com.meslin.alert.model.PersonSituation;
import br.com.meslin.alert.model.Region;
import br.com.meslin.alert.util.Debug;
import br.com.meslin.alert.util.GeographicMap;
import br.com.meslin.alert.util.StaticLibrary;

/**
 * @author meslin
 *
 */
public class BenchmarkMyGroupSelector implements GroupSelector {
	private GeographicMap map;
	private List<Region> regionList;
	/** maps persons (string id) to a hashset of groups (list of integer) */
	private Map<String, HashSet<Integer>> personGroup;

	/**
	 * Creates a Group Definer based on ContextNet IP address, ContextNet port number and region description filename 
	 * @param filename
	 */
	public BenchmarkMyGroupSelector(String filename) {
		this.personGroup = new HashMap<String, HashSet<Integer>>();
		this.regionList = StaticLibrary.readFilenamesFile(filename);
		
		/*
		 * reads each region file
		 */
		for(Region region : regionList) {
			region.setPoints(StaticLibrary.readRegion(region.getFilename()));
		}

		/*
		 * checks if there is an graphic environment available (true if not, otherwise, false)
		 */
		if(!GraphicsEnvironment.isHeadless() && !StaticLibrary.forceHeadless) {
			map = new GeographicMap(regionList);
			map.setVisible(true);
		}
	}

	/* (non-Javadoc)
	 * @see lac.cnet.groupdefiner.components.groupselector.GroupSelector#getGroupType()
	 */
	@Override
	public int getGroupType() {
		return 3;
	}

	/* (non-Javadoc)
	 * @see lac.cnet.groupdefiner.components.groupselector.GroupSelector#processGroups(lac.cnet.sddl.objects.Message)
	 */
	@Override
	public Set<Integer> processGroups(Message nodeMessage) {
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
			mobileObject = new MobileObject(new JSONObject(new String(nodeMessage.getContent())));
		} catch (Exception e) {
			Debug.warning("Could not create a mobile object", e);
		}

		if(!GraphicsEnvironment.isHeadless() && !StaticLibrary.forceHeadless) {
			map.removeItem(mobileObject);
			map.addItem(mobileObject);
		}

		HashSet<Integer> newGroups = new HashSet<Integer>(2, 1);
		// search the regions where the mobileObject may be
		for(Region region : regionList) {
			if (region.contains(mobileObject)) {
				newGroups.add(region.getNumber());
			}
		}
		
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
			BenchmarkMyCore.dataToPNQueue.add(personSituation);
			synchronized (BenchmarkMyCore.dataToPNQueue) {
				BenchmarkMyCore.dataToPNQueue.notify();
			}
		}
		this.personGroup.put(mobileObject.getId(), newGroups);
		
		return newGroups;
	}

	/* (non-Javadoc)
	 * @see lac.cnet.groupdefiner.components.groupselector.GroupSelector#createGroup(lac.cnet.sddl.objects.GroupRegion)
	 */
	@Override
	public void createGroup(GroupRegion arg0) {}
}
