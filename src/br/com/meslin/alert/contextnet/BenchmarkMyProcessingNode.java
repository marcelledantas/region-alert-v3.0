/**
 * 
 */
package br.com.meslin.alert.contextnet;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import lac.cnet.sddl.objects.ApplicationObject;
import lac.cnet.sddl.objects.Message;
import lac.cnet.sddl.objects.PrivateMessage;
import lac.cnet.sddl.udi.core.SddlLayer;
import lac.cnet.sddl.udi.core.UniversalDDSLayerFactory;
import lac.cnet.sddl.udi.core.listener.UDIDataReaderListener;
import br.com.meslin.alert.dao.UserDAO;
import br.com.meslin.alert.interSCity.InterSCity;
import br.com.meslin.alert.interSCity.InterSCityData;
import br.com.meslin.alert.main.BenchmarkMyCore;
import br.com.meslin.alert.model.Alert;
import br.com.meslin.alert.model.PersonSituation;
import br.com.meslin.alert.util.Debug;

/**
 * @author meslin<br>
 * A Processing Node Implementation.<br>
 * A PN class must implements UDIDataReaderListener interface.<br>
 */
public class BenchmarkMyProcessingNode extends Thread implements UDIDataReaderListener<ApplicationObject> {
	private SddlLayer processingNode;
	private String GATEWAY_IP = "127.0.0.1";
	private int GATEWAY_PORT = 5500;
	private MyNodeConnection nodeConnection;
	/**
	 * Constructor
	 */
	public BenchmarkMyProcessingNode() {
		// create a new connection to send message to mobile-hub
		nodeConnection = new MyNodeConnection(GATEWAY_IP, GATEWAY_PORT);
	}
	
	/**
	 * Constructor
	 * @param interSCityDataQueue
	 */
	public BenchmarkMyProcessingNode(ConcurrentLinkedQueue<InterSCityData> interSCityDataQueue) {
		this();

		this.processingNode = UniversalDDSLayerFactory.getInstance();
		this.processingNode.createParticipant(UniversalDDSLayerFactory.CNET_DOMAIN);
		
		this.processingNode.createPublisher();
		this.processingNode.createSubscriber();
		
		Object receiveMessageTopic = this.processingNode.createTopic(Message.class, Message.class.getSimpleName());
		this.processingNode.createDataReader(this, receiveMessageTopic);
		
		Object toMobileNodeTopic = this.processingNode.createTopic(PrivateMessage.class, PrivateMessage.class.getSimpleName());
		this.processingNode.createDataWriter(toMobileNodeTopic);
		
		Thread receiveData = new BenchmarkMyProcessingNode();
		receiveData.start();
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
			while(BenchmarkMyCore.dataToPNQueue.isEmpty()) {
				synchronized (BenchmarkMyCore.dataToPNQueue) {
					try {
						BenchmarkMyCore.dataToPNQueue.wait();
					} catch (InterruptedException e) {
						Debug.warning("Could not wait for dataToPN", e);
					}
				}
			}
			// dataToPNQueue is ConcurrentLinkedQueue thread safe linked queue, so, does NOT need to be synchronized
			while ((data = BenchmarkMyCore.dataToPNQueue.poll()) != null) {
				Debug.info("Data received: " + data + " object");
				if(data instanceof PersonSituation) {
					PersonSituation personSituation = (PersonSituation) data;
					Debug.info("This person is in the area #" + personSituation.getDifGroups().toString());
					// now it is time to lookup at the database for an alert at this new person areas of interested
					if(personSituation.isNewPerson()) {
						// if this is a new person:
						// - lookup areas of interest in the database by username
						// - get all alerts in those areas
						try {
							// get all areas this person is interested (these areas are not the same area where this person is in)
							List<Integer> areas = UserDAO.getInstance().getAreasByUsername(personSituation.getId());
							Debug.info(personSituation.getId() + " has " + areas.size() + " areas of interest");
							
							// get all alert in the areas this person is interested in
							/*
							 * TODO
							 * Essas áreas poderiam ser acrescentadas em difGroups para que a pesquisa abaixo no InterSCity não fosse necessária.
							 * A pesquisa seria feita juntamente com as novas áreas do usuário logo a seguir. 
							 */
							alerts.addAll(interSCity.getAlertListByArea(areas));
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

	@Override
	public void onNewData(ApplicationObject nodeMessage) {
		// Auto-generated method stub
	}
}
