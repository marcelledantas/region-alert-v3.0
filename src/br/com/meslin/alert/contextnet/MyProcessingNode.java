/**
 * 
 */
package br.com.meslin.alert.contextnet;

import java.util.concurrent.ConcurrentLinkedQueue;

import lac.cnet.sddl.objects.ApplicationObject;
import lac.cnet.sddl.objects.Message;
import lac.cnet.sddl.objects.PrivateMessage;
import lac.cnet.sddl.udi.core.SddlLayer;
import lac.cnet.sddl.udi.core.UniversalDDSLayerFactory;
import lac.cnet.sddl.udi.core.listener.UDIDataReaderListener;
import br.com.meslin.alert.interSCity.InterSCityData;
import br.com.meslin.alert.util.Debug;
import br.com.meslin.thread.ReceiveData;
import br.com.meslin.thread.SubscriberListener;

/**
 * @author meslin<br>
 * A Processing Node Implementation.<br>
 * A PN class must implements UDIDataReaderListener interface.<br>
 */
public class MyProcessingNode implements UDIDataReaderListener<ApplicationObject> {
	private SddlLayer processingNode;

	/**
	 * Constructor
	 */
	public MyProcessingNode() {
	}
	
	/**
	 * Constructor
	 * @param interSCityDataQueue
	 */
	public MyProcessingNode(String interSCityIPAddress, ConcurrentLinkedQueue<InterSCityData> interSCityDataQueue) {
		this();

		this.processingNode = UniversalDDSLayerFactory.getInstance();
		this.processingNode.createParticipant(UniversalDDSLayerFactory.CNET_DOMAIN);
		
		this.processingNode.createPublisher();
		this.processingNode.createSubscriber();
		
		Object receiveMessageTopic = this.processingNode.createTopic(Message.class, Message.class.getSimpleName());
		this.processingNode.createDataReader(this, receiveMessageTopic);
		
		Object toMobileNodeTopic = this.processingNode.createTopic(PrivateMessage.class, PrivateMessage.class.getSimpleName());
		this.processingNode.createDataWriter(toMobileNodeTopic);
		
		// a thread to receive data from ContextNet
		Thread receiveData = new ReceiveData(interSCityIPAddress);
		receiveData.start();
		
		// a thread to act as an actuator and receive messages whenever a new alert is created
		Thread subscriberListener;
		try {
			subscriberListener = new SubscriberListener(interSCityIPAddress);
			subscriberListener.start();
		} catch (Exception e) {
			Debug.error("Could not subscribe to receive news about alerts", e);
		}
	}

	@Override
	public void onNewData(ApplicationObject nodeMessage) {
		// Auto-generated method stub
	}
}
