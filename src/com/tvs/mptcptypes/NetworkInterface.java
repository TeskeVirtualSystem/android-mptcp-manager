package com.tvs.mptcptypes;

import com.tvs.mptcpmanager.Tools;

/**
 * This is a base class for Network Interfaces.
 * It should contain most information that a network interface can have.
 * 
 * @author Lucas Teske
 */

public class NetworkInterface {
	public String Device;		//	Device
	public String Address;		//	MAC Address
	public String IPAddress;	//	IP Address
	public String NetworkMask;	//	Network Mask
	public String Broadcast;	//	Network Broadcast
	
	//	TX	Stuff
	public int 	TXBytes, 		//	Transmitted Bytes
				TXPackets, 		//	Transmitted Packets
				TXErrors, 		//	Transmission Errors
				TXDrops, 		//	Transmission Drops
				TXFifo, 		//	Transmission FIFO
				TXFrame, 		//	Transmitted Frames
				TXCompressed, 	//	Transmitted Compressed Packets
				TXMulticast;	//	Transmission Multicast Packets 
	
	//	RX Stuff
	public int 	RXBytes, 		//	Received Bytes
				RXPackets, 		//	Received Packets
				RXErrors, 		//	Receiver Errors
				RXDrops, 		//	Receiver Drops
				RXFifo, 		//	Receiver FIFO
				RXColls, 		//	Received Colls
				RXCarrier, 		//	Received Carrier
				RXCompressed;	//	Received Compressed Packets
	
	/**
	 * Initializes a blank NetworkInterface Instance
	 */
	public NetworkInterface(){
		Blank();
	}
	
	/**
	 * Initializes a Blank NetworkInterface with device field.
	 * After initializing you can call Update Method to fetch device data.
	 * 
	 * @param device The Linux Device Name
	 */
	public NetworkInterface(String device)	{
		Blank();
		Device = device;
	}
	
	/**
	 * Empty all fields and initialize with default values
	 */
	public void Blank()	{
		Device = "none";
		Address = "FF:FF:FF:FF:FF:FF";
		IPAddress = "0.0.0.0";
		Broadcast = "0.0.0.0";
		NetworkMask = "255.0.0.0";

		TXBytes = 0;
		TXPackets = 0;
		TXErrors = 0;
		TXDrops = 0;
		TXFifo = 0;
		TXFrame = 0;
		TXCompressed = 0;
		TXMulticast = 0;
		
		RXBytes = 0;
		RXPackets = 0;
		RXErrors = 0;
		RXDrops = 0;
		RXFifo = 0;
		RXFrame = 0;
		RXCompressed = 0;
		RXMulticast = 0;		
	}
	
	/**
	 * Updates Interface Related Information using System Calls
	 * Device name (field device) must be setted.
	 * 
	 * @see UpdateNetworkInterface
	 */
	public void Update()	{
		Tools.UpdateNetworkInterface(this);
	}
}
