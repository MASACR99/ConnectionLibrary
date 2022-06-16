# ConnectionLibrary

This library was made with the idea to simplify the socket usage in Java, not only that, since we also added a few new features to ensure scalability and stability, learn more on "Insides of how it works".

### Usage

To use the library simply download the library files (Communications folder in the source code) and put it in a package, for example, com.'name-of-project'.connections, and you are ready to start coding. To start coding you will need a class that implements ConnectionInterface since that'll be the interface used by the library to send any messages or updates via events, and add a new CommunicationController variable and call the constructor of your liking. You're now ready to start using the library.

##### First steps

1. Download the project source code inside the Communications folder.
2. Put the downloaded code in a new package.
3. Create a class that implements ConectionInterface, implement all the needed methods
4. And finally create a CommunicationController variable. Optionally use the addListeners methods (addOnPacketListener, addOnConnectionListener, addOnDisconnectListener, addOnLookupUpdate or add them all addAllListeners).

##### How to connect

Use the CommunicationController variable to call the method "connectToIp(String ip);". That's it

##### How to disconnect

Use the CommunicationController variable to call the method "disconnect(String macAddress);".

##### Send a package

Use the CommunicationController variable to call the "sendMessage(ProtocolDataPacket packet);" and use the method "createPacket(String targetMac, int packetId, Object data);" to create the needed ProtocolDataPacket.

##### Need to know the connected peers

Use the CommunicationController variable to call the "getConnectedMacs();" method

##### Need to broadcast to everyone or my inmediate neighbors

Use the CommunicationController variable to call "sendBroadcastMessage(int Command, Object data);" to send a broadcast message to all connected peers or sendToNeighbors(int Command, Object data);" to send a message to your inmediatly connected peers (1 jump away).

##### Need to close connections

You can use "closeAllConnections();" to close pc and mobile connections or use "closePcConnections();" or "closeMobileConnections;" to close the pc or mobile connections respectively.

##### Check if an id for a protocol is valid to use

Use the "addNewProtocol(int id, String description);" and it will return true or false, true if it's valid, flase otherwise.

### Insides of how it works

Now that the how to use it is out for the people that only want to read about that we'll begin to explain how the program works.

The program uses Java default sockets to establish connections between different peers on a peer to peer basis, where everyone has a server on when starting the library and a new thread starts for each connection made to keep listening, so it's a P2P multithreaded network. Inside the library there are a few "watchers" whose job is to make sure 2 peers stay connected without errors. Those are:
  - ServerHealth: One of this starts each time a new connection is made, basically every socket has one and it sends a randomly generated 4-byte int and checks that the received integer sent by the connected peer is the same integer it sent. So it makes sure that after a delay the connection hasn't fallen without previous warning. We are aware that Java already has some checks of this kind but our teacher made us make this one anyway.
  - ClientConnector: One of this starts as a new thread when the library starts and it's job is to watch the made connections so that if they have a few characteristics they are re-made. So if a ServerHealth closes a connection becuase of bad health the ClientConnector will restart that same connection to keep it alive and hopefully make it be healthier than before. It will do a few attempts before giving up and resetting everything to null on that connection.

We use MAC addresses to know to whom send the messages, and to have that data we must do a handshake when we start a new connection, that handshake follows the following steps:
1. Client connects to the server
2. Server responds by accepting the socket connection, starting a new Connection thread and sending an "askDeviceType" message.
3. Client receives the accept and the first message of the handshake after which stores the MAC address of the sender in a variable and sends it's device type, either mobile or pc connection
4. Server also stores the mac of the sender as the connected MAC address and based on the type of device the handshake changes so followign first the mobile connection:
5. The mobile connections are always accepted so the connection is added to the ArrayList on the controller
6. The pc connections are only accepter if there's space in the controller, this limit is user defined to define specific network structures, like star-connections, circle connections...
7. After ACCEPTING the connection an askLookup message is sent with the data as the lookup table of the server.
8. Client receives that message, updates it's lookup (we will get into lookups a bit later) and sends it's lookup back
9. Server also updates it's lookups and sends the final message of the handshake to approve the connection as valid.
10. In case the connection CANNOT be ACCEPTED a new protocol starts, a new message starts being sent to all already connected and accepted peers asking if they have space for a new connection giving them the IP of the connection, this message will, in the worst case scenario, go fully around all peers and die. The server also sends a close message to the connected peer.
11. After a connection receives the message asking to accept a new connection, that peer connects to the IP given and the handshake starts again.

Our protocol is also expandable giving the user space to put their codes and definitions so they can use the librayr and get an event when one of their packages is received.

### Lookup tables
Each socket has a lookup table in itself, that lookup table tells the library or a user how many jumps there's for a packet to a target mac address if that packet was sent via that socket, it doesn't do anything with timings so a package will always prefer fewer jumps since we have not implemented any other way of prioritizing.

When the handshake is made or a lookup table is updated locally, the neighbors also update their lookups based on the new data, that keeps the tables updated when a connection falls, a disconnect happens or a new connection is made. The logic behind it is pretty simple but I'd recommend checking the code behind that.

