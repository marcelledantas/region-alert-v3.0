/**
 * 
 */
package br.com.meslin.alert.contextnet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.meslin.alert.model.MyGroup;
import br.com.meslin.alert.util.Debug;
import lac.cnclib.net.NodeConnection;
import lac.cnclib.net.NodeConnectionListener;
import lac.cnclib.net.groups.Group;
import lac.cnclib.net.groups.GroupCommunicationManager;
import lac.cnclib.net.mrudp.MrUdpNodeConnection;
import lac.cnclib.sddl.message.ApplicationMessage;
import lac.cnclib.sddl.message.Message;

/**
 * @author meslin
 *
 */
public class MyNodeConnection implements NodeConnectionListener {
	/** a connection objet to send message to mobile-hub */
	private MrUdpNodeConnection connection;


	/**
	 * 
	 * @param gatewayIP
	 * @param gatewayPort
	 */
	public MyNodeConnection(String gatewayIP, int gatewayPort) {
		// create a new connection to send message to mobile-hub
		InetSocketAddress address = new InetSocketAddress(gatewayIP, gatewayPort);
		try {
			connection = new MrUdpNodeConnection(UUID.randomUUID());
			connection.addNodeConnectionListener(this);
			connection.connect(address);
		} catch (IOException e) {
			Debug.warning("Could not create a connection to " + gatewayIP + ":" + gatewayPort, e);
		}
	}

	/**
	 * Sends a message to a mobile-hub<br>
	 * @param uuid
	 * @param text
	 */
	public void sendMessageToMobileHub(String uuid, String text) {
		ApplicationMessage message = new ApplicationMessage();
		message.setContentObject(text);
		message.setRecipientID(UUID.fromString(uuid));
		try {
			sendMessage(message);
		} catch (IOException e) {
			Debug.warning("Could not send message " + new String(message.getContent()) + " to " + message.getRecipientID(), e);
		}
	}

	/* (non-Javadoc)
	 * @see lac.cnclib.net.NodeConnectionListener#connected(lac.cnclib.net.NodeConnection)
	 */
	@Override
	public void connected(NodeConnection arg0) {
		// Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see lac.cnclib.net.NodeConnectionListener#disconnected(lac.cnclib.net.NodeConnection)
	 */
	@Override
	public void disconnected(NodeConnection arg0) {
		// Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see lac.cnclib.net.NodeConnectionListener#internalException(lac.cnclib.net.NodeConnection, java.lang.Exception)
	 */
	@Override
	public void internalException(NodeConnection arg0, Exception arg1) {
		// Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see lac.cnclib.net.NodeConnectionListener#newMessageReceived(lac.cnclib.net.NodeConnection, lac.cnclib.sddl.message.Message)
	 */
	@Override
	public void newMessageReceived(NodeConnection arg0, Message arg1) {
		// Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see lac.cnclib.net.NodeConnectionListener#reconnected(lac.cnclib.net.NodeConnection, java.net.SocketAddress, boolean, boolean)
	 */
	@Override
	public void reconnected(NodeConnection arg0, SocketAddress arg1,
			boolean arg2, boolean arg3) {
		// Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see lac.cnclib.net.NodeConnectionListener#unsentMessages(lac.cnclib.net.NodeConnection, java.util.List)
	 */
	@Override
	public void unsentMessages(NodeConnection arg0, List<Message> arg1) {
		// Auto-generated method stub
	}

	public void sendMessage(ApplicationMessage message) throws IOException {
		this.connection.sendMessage(message);
	}

	/**
	 * Sends a message to groups<br> 
	 * @param groups list of group
	 * @param text message text
	 * @throws IOException 
	 */
	public void sendMessageToGroups(List<MyGroup> groups, String text) throws IOException {
		GroupCommunicationManager groupManager;
		groupManager = new GroupCommunicationManager(connection);
		ApplicationMessage applicationMessage = new ApplicationMessage();
		applicationMessage.setContentObject(text);
		List<Group> theGroups = new ArrayList<Group>();
		for(MyGroup group : groups) {
			theGroups.add(group);
		}
		groupManager.sendGroupcastMessage(applicationMessage, theGroups);
	}
}
