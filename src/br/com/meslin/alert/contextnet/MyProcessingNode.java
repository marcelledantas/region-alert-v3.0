/**
 * 
 */
package br.com.meslin.alert.contextnet;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.meslin.alert.interSCity.InterSCityData;
import br.com.meslin.alert.util.Debug;
import br.com.meslin.alert.util.StaticLibrary;
import br.com.meslin.thread.ReceiveData;
import br.com.meslin.thread.SubscriberListener;
import ckafka.data.Swap;
import ckafka.data.SwapData;
import lac.cnet.sddl.objects.ApplicationObject;
import main.java.application.ModelApplication;


/**
 * @author meslin<br>
 * A Processing Node Implementation.<br>
 * A PN class must implements UDIDataReaderListener interface.<br>
 */
public class MyProcessingNode extends ModelApplication {
	
	private Swap swap;
	private ObjectMapper objectMapper;
	
	/**
	 * Constructor
	 */
	public MyProcessingNode() {
		this.objectMapper = new ObjectMapper();
		this.swap = new Swap(objectMapper);
	}
	
	/**
	 * Main method
	 * @param args 
	 */
		
	public static void main(String[] args){

        //Unicast messages: set environment variables
        Map<String,String> env = new HashMap<String, String>();
        env.putAll(System.getenv());
        if(System.getenv("app.consumer.topics") == null)
            env.put("app.consumer.topics", "AppModel");
        if(System.getenv("app.consumer.auto.offset.reset") == null)
            env.put("app.consumer.auto.offset.reset", "latest");
        if(System.getenv("app.consumer.bootstrap.servers") == null)
            env.put("app.consumer.bootstrap.servers", "127.0.0.1:9092");
        if(System.getenv("app.consumer.group.id") == null)
            env.put("app.consumer.group.id", "gw-consumer");
        if(System.getenv("app.producer.retries") == null)
            env.put("app.producer.retries", "3");
        if(System.getenv("app.producer.enable.idempotence") == null)
            env.put("app.producer.enable.idempotence", "true");
        if(System.getenv("app.producer.linger.ms") == null)
            env.put("app.producer.linger.ms", "1");
        if(System.getenv("app.producer.acks") == null)
            env.put("app.producer.acks", "all");
        try {
            StaticLibrary.setEnv(env);
        } catch (Exception e) {
            e.printStackTrace();
        }

        
        /**
         * Groupcast messages
         */
        
        Scanner keyboardGroup = new Scanner(System.in);
        MyProcessingNode PNGroup = new MyProcessingNode();

            while(true) {
            	PNGroup.sendGroupcastMessage(keyboardGroup);
        }

    }
	
	
	/** TODO: adjust Contructor
	 * Constructor
	 * @param interSCityDataQueue
	 * @throws Exception 
	 */
	public MyProcessingNode(String interSCityIPAddress, ConcurrentLinkedQueue<InterSCityData> interSCityDataQueue) throws Exception {
		this();

//		this.processingNode = UniversalDDSLayerFactory.getInstance();
//		this.processingNode.createParticipant(UniversalDDSLayerFactory.CNET_DOMAIN);
//		
//		this.processingNode.createPublisher();
//		this.processingNode.createSubscriber();
//		
//		Object receiveMessageTopic = this.processingNode.createTopic(Message.class, Message.class.getSimpleName());
//		this.processingNode.createDataReader(this, receiveMessageTopic);
//		
//		Object toMobileNodeTopic = this.processingNode.createTopic(PrivateMessage.class, PrivateMessage.class.getSimpleName());
//		this.processingNode.createDataWriter(toMobileNodeTopic);
		
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
	
	/**
     * @param ConsumerRecord 
     */
	@SuppressWarnings("rawtypes")
	@Override
	public void recordReceived(ConsumerRecord record) {
		// value possui o conteúdo transmitido e recebido em byte[]
        this.logger.debug("Record Received " + record.value().toString());
        System.out.println(String.format("Mensagem recebida de %s", record.key()));	// String com UUID do remetente
        
        try {
			SwapData data = swap.SwapDataDeserialization((byte[]) record.value());
			String text = new String(data.getMessage(), StandardCharsets.UTF_8);
	        System.out.println("Mensagem recebida = " + text);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
	private void sendGroupcastMessage(Scanner keyboard) {
		System.out.print("Mensagem groupcast. Entre com o número do grupo: ");
		String group = keyboard.nextLine();
		System.out.print("Entre com a mensagem: ");
		String messageText = keyboard.nextLine();
		System.out.println(String.format("Enviando mensagem %s para o grupo %s.", messageText, group));
		
		try {
			sendRecord(createRecord("GroupMessageTopic", group, swap.SwapDataSerialization(createSwapData(messageText))));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error SendGroupCastMessage", e);
		}
	}
	
	
	

	public void onNewData(ApplicationObject nodeMessage) {
		// Auto-generated method stub
	}
	
}
