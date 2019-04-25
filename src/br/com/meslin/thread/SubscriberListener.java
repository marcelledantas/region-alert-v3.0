/**
 * 
 */
package br.com.meslin.thread;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import br.com.meslin.alert.contextnet.MyNodeConnection;
import br.com.meslin.alert.interSCity.InterSCity;
import br.com.meslin.alert.model.Alert;
import br.com.meslin.alert.util.Debug;
import br.com.meslin.alert.util.StaticLibrary;

/**
 * @author meslin
 *
 */
public class SubscriberListener extends Thread {
	private ServerSocket server; 

	/**
	 * Constructor<br>
	 * @throws Exception when could not create the listener
	 */
	public SubscriberListener() throws Exception {
		String ip = null;
		String uuid;
		
		// get the local IP address to subscribe to receive actuation commands (information about new messages)
		try (final DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			ip = socket.getLocalAddress().getHostAddress();
		} catch (SocketException | UnknownHostException e) {
			throw(e);
		}
		
		server = new ServerSocket(0);
		// TODO testar se conseguiu subscrever (subscribe)
		InterSCity interSCity = new InterSCity();
		String[] uuids = interSCity.alertListenerDiscover(-23.559616, -46.731386);
		if(uuids == null) {
			Debug.info("creating a new alert listener");
			uuid = interSCity.createNewResource("A city alert listener", "alertListener", -23.559616, -46.731386);
		}
		uuid = uuids[0];
		interSCity.subscribe(uuid, new String[]{"alertListener"}, "http://" + ip + ":" + server.getLocalPort());

		Debug.info("Listening at " + ip + ":" + server.getLocalPort());
	}

	/**
	 * Listen for the publication of an alert<br>
	 * Upon receive the publication, sends the alert to all users in the corresponding areas<br> 
	 */
	@Override
	public void run() {
		InterSCity interSCity = new InterSCity();
		while (true) {
			try {
				// wait for a new alert
				Socket client = server.accept();
				Debug.warning("Publisher connected at " + client.getInetAddress().getHostAddress());
				/*
				 * read the published text
				 * data should be a JSON text like this:
				 * 
				 * {
				 *   "data": [
				 *     {
				 *       "uuid": "b0ae6f76-521d-4199-9595-f52c99361052",
				 *       "capabilities": {
				 *         "alertListener": [<area list>]
				 *       }
				 *     }
				 *   ]
				 * }
				 */
				Scanner scanner = new Scanner(client.getInputStream());
				String jsonText = "";
				while(scanner.hasNextLine()) {
					jsonText += scanner.nextLine();
				}
				scanner.close();
				JSONObject jsonObject = new JSONObject(jsonText);
				JSONArray alertAreas = jsonObject.getJSONArray("data").getJSONObject(0).getJSONObject("capabilities").getJSONArray("alertListerner");
				List<Integer> areas = new ArrayList<Integer>();
				for(int i=0; i<alertAreas.length(); i++) {
					areas.add(alertAreas.getInt(i));
				}
				// TODO testar se conseguiu enviar o alerta
				List<Alert> alerts = interSCity.getAlertListByArea(areas);
				// send alert to everybody connected
				for(Alert alert : alerts) {
					MyNodeConnection connection = new MyNodeConnection(StaticLibrary.contextNetIPAddress, StaticLibrary.contextNetPortNumber);
					connection.sendMessageToGroups(alert.getGroups(), alert.getText());
				}

				client.close();
			} catch (IOException e) {
				Debug.error("Could not start listening", e);
			}
		}
	}
	
	/**
	 * Closes the server
	 */
	@Override
	public void finalize() throws IOException {
		Debug.warning("Closing subscriber thread");
		server.close();
	}

	/**
	 * Constructor<br>
	 * @param target
	 */
	public SubscriberListener(Runnable target) {
		super(target);
		// Auto-generated constructor stub
	}

	/**
	 * Constructor<br>
	 * @param name
	 */
	public SubscriberListener(String name) {
		super(name);
		// Auto-generated constructor stub
	}

	/**
	 * Constructor<br>
	 * @param group
	 * @param target
	 */
	public SubscriberListener(ThreadGroup group, Runnable target) {
		super(group, target);
		// Auto-generated constructor stub
	}

	/**
	 * Constructor<br>
	 * @param group
	 * @param name
	 */
	public SubscriberListener(ThreadGroup group, String name) {
		super(group, name);
		// Auto-generated constructor stub
	}

	/**
	 * Constructor<br>
	 * @param target
	 * @param name
	 */
	public SubscriberListener(Runnable target, String name) {
		super(target, name);
		// Auto-generated constructor stub
	}

	/**
	 * Constructor<br>
	 * @param group
	 * @param target
	 * @param name
	 */
	public SubscriberListener(ThreadGroup group, Runnable target, String name) {
		super(group, target, name);
		// Auto-generated constructor stub
	}

	/**
	 * Constructor<br>
	 * @param group
	 * @param target
	 * @param name
	 * @param stackSize
	 */
	public SubscriberListener(ThreadGroup group, Runnable target, String name,
			long stackSize) {
		super(group, target, name, stackSize);
		// Auto-generated constructor stub
	}
}
