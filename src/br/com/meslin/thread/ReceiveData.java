package br.com.meslin.thread;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.meslin.alert.contextnet.MyNodeConnection;
import br.com.meslin.alert.dao.UserDAO;
import br.com.meslin.alert.interSCity.InterSCity;
import br.com.meslin.alert.main.MyContextNetCore;
import br.com.meslin.alert.model.Alert;
import br.com.meslin.alert.model.PersonSituation;
import br.com.meslin.alert.util.Debug;
import br.com.meslin.alert.util.StaticLibrary;

public class ReceiveData extends Thread {
	private MyNodeConnection nodeConnection;

	public ReceiveData() {
		// create a new connection to send message to mobile-hub
		nodeConnection = new MyNodeConnection(StaticLibrary.contextNetIPAddress, StaticLibrary.contextNetPortNumber);
	}

	/**
	 * Receives a message when a person enters a new region (group)<br>
	 * Queries InterSCity for an alert<br>
	 * Send an alert to a person who enter the region<br>
	 */
	@Override
	public void run() {
		InterSCity interSCity = new InterSCity();
		Object data;
		List<Alert> alerts = new ArrayList<Alert>();
		
		while(true) {
			while(MyContextNetCore.dataToPNQueue.isEmpty()) {
				synchronized (MyContextNetCore.dataToPNQueue) {
					try {
						MyContextNetCore.dataToPNQueue.wait();
					} catch (InterruptedException e) {
						Debug.warning("Could not wait for dataToPN", e);
					}
				}
			}
			// dataToPNQueue is ConcurrentLinkedQueue thread safe linked queue, so, does NOT need to be synchronized
			while ((data = MyContextNetCore.dataToPNQueue.poll()) != null) {
				Debug.info("Data received: " + data + " object");
				if(data instanceof PersonSituation) {
					PersonSituation personSituation = (PersonSituation) data;
					Debug.info("This person is in the area #" + personSituation.getDifGroups().toString());
					// now it is time to lookup at the database for an alert at this new person areas of interested
					if(personSituation.isNewPerson()) {
						// if this is a new person, lookup areas of interest in the database by username
						try {
							// get all areas this person is interested (these areas are not the same area where this person is in)
							List<Integer> areas = UserDAO.getInstance().getAreasByUsername(personSituation.getId());
							Debug.info(personSituation.getId() + " has " + areas.size() + " areas of interest");
							personSituation.addToDifGroups(areas);
						} catch (ClassNotFoundException | SQLException e) {
							Debug.warning("Could not generate the alert list for this new person.", e);
						}
					}
					// lookup for alerts where this person has entered now
					alerts.addAll(interSCity.getAlertListByArea(personSituation.getDifGroups()));
					// check if there is alerts to this person
					if(alerts != null) {
						Debug.info("There is " + alerts.size() + " alert(s) for " + personSituation.getId());
						// send each alert to this person
						for(Alert alert : alerts) {
							this.nodeConnection.sendMessageToMobileHub(personSituation.getUuid(), alert.getText());
						}
					}
				}
			}
		}
	}

}
