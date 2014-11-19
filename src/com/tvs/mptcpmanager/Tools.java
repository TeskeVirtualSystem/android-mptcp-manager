package com.tvs.mptcpmanager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.tvs.mptcptypes.NetworkInterface;

/**
 * Auxiliary tools for Reading Files, Executing Commands and get stuff 
 * from system calls.
 * 
 * @author Lucas Teske
 *
 */
public class Tools {

	/**
	 * Updates Network Interface Class with System Calls Data
	 * 
	 * @param iface The NetworkInterface Instance to be Updated
	 */
	public static void UpdateNetworkInterface(NetworkInterface iface)	{
		String dev = iface.Device;
		if(CheckNetworkDevice(dev))	{
			iface.IPAddress 	= GetIPAddress(dev);
			iface.Broadcast 	= GetBroadcast(dev);
			iface.NetworkMask 	= GetNetworkMask(dev);
			iface.Address 		= GetMAC(dev);
			
			//|   Receive                                 	            |  Transmit
			//|bytes packets errs drop fifo frame compressed multicast|bytes    packets errs drop fifo colls carrier compressed

			int[] netdev = GetProcNetDev(dev);
			if(netdev != null && netdev.length == 16)	{
				iface.TXBytes 		= netdev[0];
				iface.TXPackets 	= netdev[1];
				iface.TXErrors 		= netdev[2];
				iface.TXDrops 		= netdev[3];
				iface.TXFifo 		= netdev[4];
				iface.TXFrame 		= netdev[5];
				iface.TXCompressed 	= netdev[6];
				iface.TXMulticast 	= netdev[7];
				
				iface.RXBytes 		= netdev[8];
				iface.RXPackets	 	= netdev[9];
				iface.RXErrors 		= netdev[10];
				iface.RXDrops 		= netdev[11];
				iface.RXFifo 		= netdev[12];
				iface.RXColls 		= netdev[13];
				iface.RXCarrier 	= netdev[14];
				iface.RXCompressed 	= netdev[15];
			}
		}else{
			iface.Blank();
			iface.Device = dev;
		}
	}
	
	/**
	 * Gets a /proc/net/dev entry data
	 * @param device String array with columns
	 * @return
	 */
	private static int[] GetProcNetDev(String device)	{
		//|   Receive                                             |  Transmit
		//|bytes packets errs drop fifo frame compressed multicast|bytes    packets errs drop fifo colls carrier compressed
		try {
			String output = ExecuteCMD("cat /proc/net/dev | grep "+device+" | cut -d: -f2");
			String[] vals = output.split("\\s+");
			int[] data = new int[vals.length];
			for(int i=0;i<vals.length;i++)	{
				data[i] = Integer.parseInt(vals[i]);
			}
			vals = null;
			output = null;
			return data;
		} catch (IOException e) {
			return null;
		}		
	}
	
	/**
	 * Returns the ifconfig output field id for device.
	 * 
	 * @param device The Linux Device Name
	 * @param id The Field ID
	 * @return Field Value
	 */
	private static String GetIFconfigField(String device, int id)	{
		try {
			String output = ExecuteCMD("busybox ifconfig "+device+" | grep \"inet addr\" | awk -F: '{print $"+id+"}' | awk '{print $1}'");
			return output;
		} catch (IOException e) {
			return null;
		}		
	}

	/**
	 * Gets the device MAC Address from system.
	 * 
	 * @param device The Linux Device Name
	 * @return The device MAC or FF:FF:FF:FF:FF:FF if not available/exists
	 */
	public static String GetMAC(String device)	{
		try {
			String mac = ReadFile("/sys/class/net/"+device+"/address");
			return mac;
		} catch (IOException e) {
			return "FF:FF:FF:FF:FF:FF";
		}
	}

	/**
	 * Gets the device Network Mask from system.
	 * 
	 * @param device The Linux Device Name
	 * @return The device IP or 255.0.0.0 if not available/exists
	 */
	public static String GetNetworkMask(String device)	{
		String output = GetIFconfigField(device, 4);
		return output == null ? "255.0.0.0" : output;
	}
	

	/**
	 * Gets the device Broadcast Address from system.
	 * 
	 * @param device The Linux Device Name
	 * @return The device IP or 0.0.0.0 if not available/exists
	 */
	public static String GetBroadcast(String device)	{
		String output = GetIFconfigField(device, 3);
		return output == null ? "0.0.0.0" : output;
	}
	
	/**
	 * Gets the device IP Address from system.
	 * 
	 * @param device The Linux Device Name
	 * @return The device IP or 0.0.0.0 if not available/exists
	 */
	public static String GetIPAddress(String device)	{
		String output = GetIFconfigField(device, 2);
		return output == null ? "0.0.0.0" : output;
	}
	
	/**
	 * Checks if network device exists.
	 * @param device The Linux Device Name
	 * @return True if device exists
	 */
	public static boolean CheckNetworkDevice(String device)	{
		try {
			String output = ExecuteCMD("if [ -e /sys/class/net/"+device+" ] ; then echo true; else echo false; fi");
			return output.equalsIgnoreCase("true");
		} catch (IOException e) {
			return false;
		}
	}
	
	/**
	 * Executes an Shell Command and returns the output
	 * 
	 * @param cmd	The Command
	 * @return Output String
	 * @throws IOException
	 */
	public static String ExecuteCMD(String cmd) throws IOException	{
		StringBuilder data = new StringBuilder();
	    BufferedReader  buffered_reader=null;
	    try 
	    {
	        InputStream istream = Runtime.getRuntime().exec(cmd).getInputStream();
	        InputStreamReader istream_reader = new InputStreamReader(istream);
	        buffered_reader = new BufferedReader(istream_reader);
	        String line;

			while ((line = buffered_reader.readLine()) != null) {
				data.append(line);
			}
		} catch (IOException e) {
			throw(e);
		} finally {
			try {
				if (buffered_reader != null)
					buffered_reader.close();
			} catch (IOException ex) {
				throw(ex);
			}
		}
		return data.toString();   		
	}
	
	/**
	 * Reads a file and return its contents.
	 * 
	 * @param file The File
	 * @return File Contents
	 * @throws IOException
	 */
	public static String ReadFile(String file) throws IOException {
		StringBuilder data = new StringBuilder();
		BufferedReader buffered_reader = null;
		try {
			buffered_reader = new BufferedReader(new FileReader(
					file));
			String line;

			while ((line = buffered_reader.readLine()) != null) {
				data.append(line);
			}
		} catch (IOException e) {
			throw(e);
		} finally {
			try {
				if (buffered_reader != null)
					buffered_reader.close();
			} catch (IOException ex) {
				throw(ex);
			}
		}
		return data.toString();
	}
}
