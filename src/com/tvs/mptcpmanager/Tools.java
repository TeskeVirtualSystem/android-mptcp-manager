package com.tvs.mptcpmanager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.tvs.mptcptypes.NetworkInterface;

/**
 * Auxiliary tools for Reading Files, Executing Commands and get stuff from
 * system calls.
 * 
 * @author Lucas Teske
 * 
 */
public class Tools {
	
	/**
	 * Writes a variable in sysctl
	 * 
	 * @param var
	 *          Variable
	 * @param val
	 *          Value
	 * @return true if ok, false if not
	 */
	public static boolean WriteSysctl(String var, String val) {
		try {
			String ret = Tools.ExecuteCMD("sysctl -w " + var + "=" + val);
			
			/**
			 * sysctl -w should return the same data. Example: $ sysctl -w
			 * net.ipv4.ip_forward=1 net.ipv4.ip_forward = 1
			 */
			return ret.contentEquals(var + " = " + val);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Read an sysctl variable
	 * 
	 * @param var
	 *          The variable to read
	 * @return Variable Value
	 */
	public static String ReadSysctl(String var) {
		try {
			String ret = Tools.ExecuteCMD("sysctl " + var);
			/**
			 * It should ALWAYS return something like: var = value Example:
			 * net.ipv4.ip_forward = 1 But just in case, lets make an exception
			 */
			return (ret.split("=", 1).length > 1) ? ret.split("=", 1)[1].trim() : ret.trim();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * Gets MPTCP Version <BR>
	 * Example: <B>Stable release v0.89.2</B>
	 * 
	 * @return MPTCP Version
	 */
	public String GetMPTCPVersion() {
		try {
			String version = ExecuteCMD("cat /var/log/dmesg | grep MPTCP");
			return version.length() > 22 ? version.substring(22) : "No MPTCP";
		} catch (Exception e) {
			return "No MPTCP";
		}
	}
	
	/**
	 * Gets a Short String for MPTCP Version <BR>
	 * Example: <B>v0.89.2</B>
	 * 
	 * @return MPTCP Version
	 */
	public String GetShortMPTCPVersion() {
		String version = GetMPTCPVersion();
		if (version.length() > 14)
			return version.substring(15);
		else
			return version;
	}
	
	/**
	 * Gets a list of the network interfaces available
	 * 
	 * @return String array with devices names
	 */
	public static String[] GetNetworkInterfacesList() {
		String data;
		String[] tmp;
		try {
			data = ExecuteCMD("getifaces");
			String[] devs = data.split("\n");
			List<String> dev_list = new ArrayList<String>();
			for (int i = 0; i < devs.length; i++) {
				tmp = devs[i].split("/");
				devs[i] = tmp[tmp.length - 1];
				if (CheckNetworkDevice(devs[i]))
					dev_list.add(devs[i]);
			}
			devs = new String[dev_list.size()];
			for (int i = 0; i < dev_list.size(); i++) {
				devs[i] = dev_list.get(i);
			}
			data = null;
			tmp = null;
			return devs;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Updates Network Interface Class with System Calls Data
	 * 
	 * @param iface
	 *          The NetworkInterface Instance to be Updated
	 */
	public static void UpdateNetworkInterface(NetworkInterface iface) {
		String dev = iface.Device;
		if (CheckNetworkDevice(dev)) {
			iface.IPAddress = GetIPAddress(dev);
			iface.Broadcast = GetBroadcast(dev);
			iface.NetworkMask = GetNetworkMask(dev);
			iface.Address = GetMAC(dev);
			
			// | Receive | Transmit
			// |bytes packets errs drop fifo frame compressed multicast|bytes packets
			// errs drop fifo colls carrier compressed
			
			int[] netdev = GetProcNetDev(dev);
			if (netdev != null && netdev.length >= 16) {
				iface.TXBytes = netdev[0];
				iface.TXPackets = netdev[1];
				iface.TXErrors = netdev[2];
				iface.TXDrops = netdev[3];
				iface.TXFifo = netdev[4];
				iface.TXFrame = netdev[5];
				iface.TXCompressed = netdev[6];
				iface.TXMulticast = netdev[7];
				
				iface.RXBytes = netdev[8];
				iface.RXPackets = netdev[9];
				iface.RXErrors = netdev[10];
				iface.RXDrops = netdev[11];
				iface.RXFifo = netdev[12];
				iface.RXColls = netdev[13];
				iface.RXCarrier = netdev[14];
				iface.RXCompressed = netdev[15];
			}
		} else {
			iface.Blank();
			iface.Device = dev;
		}
	}
	
	/**
	 * Returns specified Processor Core Clock
	 * 
	 * @param core
	 *          CoreID
	 * @return Frequency in Hz
	 */
	public static int GetFrequency(int core) {
		try {
			String output = ExecuteCMD("cat /sys/devices/system/cpu/cpu" + core + "/cpufreq/cpuinfo_cur_freq");
			return Integer.parseInt(output);
		} catch (Exception e) {
			return -1;
		}
	}
	
	/**
	 * Returns Processor Core Average Clock
	 * 
	 * <br>
	 * <b>(Core0F + Core1F ... CoreNF) / NumCores</b>
	 * 
	 * @return Average Clock
	 */
	public static int GetFrequency() {
		try {
			String output = ExecuteCMD("cat /sys/devices/system/cpu/present");
			String[] c = output.split("-"); // This will return [ FirstCore, LastCore
			                                // ]
			int firstcore = Integer.parseInt(c[0]), lastcore = Integer.parseInt(c[1]), numcores = lastcore - firstcore + 1, frequency = 0;
			for (int i = firstcore; i <= lastcore; i++) {
				frequency += GetFrequency(i);
			}
			frequency /= numcores;
			return frequency;
		} catch (Exception e) {
			return -1;
		}
	}
	
	/**
	 * Returns the System Temperature
	 * 
	 * @return System Temperature
	 */
	public static int GetTemperature() {
		try {
			String output = ExecuteCMD("cat /sys/devices/platform/tmu/temperature");
			return Integer.parseInt(output);
		} catch (Exception e) {
			return -1;
		}
	}
	
	/**
	 * Gets a <B>/proc/net/dev</B> entry data
	 * 
	 * @param device
	 *          String array with columns
	 * @return
	 */
	private static int[] GetProcNetDev(String device) {
		// | Receive | Transmit
		// |bytes packets errs drop fifo frame compressed multicast|bytes packets
		// errs drop fifo colls carrier compressed
		try {
			String output = ExecuteCMD(new String[] { "getprocnetdev", device });
			String[] vals = output.trim().split("\\s+");
			
			int[] data = new int[vals.length];
			for (int i = 0; i < vals.length; i++) {
				if (!vals[i].isEmpty())
					data[i] = Integer.parseInt(vals[i]);
			}
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns the <B>ifconfig</B> output field id for device.
	 * 
	 * @param device
	 *          The Linux Device Name
	 * @param id
	 *          The Field ID
	 * @return Field Value
	 */
	private static String GetIFconfigField(String device, int id) {
		try {
			String output = ExecuteCMD(new String[] { "getifconfigfield", device, String.valueOf(id) });
			return output;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Gets the device MAC Address from system.
	 * 
	 * @param device
	 *          The Linux Device Name
	 * @return The device MAC or FF:FF:FF:FF:FF:FF if not available/exists
	 */
	public static String GetMAC(String device) {
		try {
			String mac = ReadFile("/sys/class/net/" + device + "/address");
			mac = mac.replaceAll("\n", "").replaceAll("\r", "").trim();
			return mac.isEmpty() ? "FF:FF:FF:FF:FF:FF" : mac;
		} catch (IOException e) {
			return "FF:FF:FF:FF:FF:FF";
		}
	}
	
	/**
	 * Gets the device Network Mask from system.
	 * 
	 * @param device
	 *          The Linux Device Name
	 * @return The device IP or 255.0.0.0 if not available/exists
	 */
	public static String GetNetworkMask(String device) {
		String output = GetIFconfigField(device, 4);
		output = output.replaceAll("\n", "").replaceAll("\r", "").trim();
		return output == null || output.isEmpty() ? "255.0.0.0" : output;
	}
	
	/**
	 * Gets the device Broadcast Address from system.
	 * 
	 * @param device
	 *          The Linux Device Name
	 * @return The device IP or 0.0.0.0 if not available/exists
	 */
	public static String GetBroadcast(String device) {
		String output = GetIFconfigField(device, 3);
		output = output.replaceAll("\n", "").replaceAll("\r", "").trim();
		return output == null || output.isEmpty() ? "0.0.0.0" : output;
	}
	
	/**
	 * Gets the device IP Address from system.
	 * 
	 * @param device
	 *          The Linux Device Name
	 * @return The device IP or 0.0.0.0 if not available/exists
	 */
	public static String GetIPAddress(String device) {
		String output = GetIFconfigField(device, 2);
		output = output.replaceAll("\n", "").replaceAll("\r", "").trim();
		return output == null || output.isEmpty() ? "0.0.0.0" : output;
	}
	
	/**
	 * Checks if network device exists.
	 * 
	 * @param device
	 *          The Linux Device Name
	 * @return True if device exists
	 */
	public static boolean CheckNetworkDevice(String device) {
		try {
			String output = ExecuteCMD(new String[] { "checkiface", device });
			return output.replaceAll("\n", "").equalsIgnoreCase("true");
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Executes an Shell Command and returns the output
	 * 
	 * @param cmd
	 *          The Command
	 * @return Output String
	 * @throws IOException
	 */
	public static String ExecuteCMD(String[] cmd) throws Exception {
		StringBuilder data = new StringBuilder();
		BufferedReader buffered_reader = null;
		try {
			String[] fullcmd = new String[2 + cmd.length];
			fullcmd[0] = "/bin/sh";
			fullcmd[1] = "-c";
			for (int i = 0; i < cmd.length; i++)
				fullcmd[i + 2] = cmd[i];
			Process p = Runtime.getRuntime().exec(cmd);
			InputStream istream = p.getInputStream();
			InputStreamReader istream_reader = new InputStreamReader(istream);
			buffered_reader = new BufferedReader(istream_reader);
			String line;
			while ((line = buffered_reader.readLine()) != null) {
				data.append(line + "\n");
			}
			p.waitFor();
		} catch (Exception e) {
			throw (e);
		} finally {
			try {
				if (buffered_reader != null)
					buffered_reader.close();
			} catch (IOException ex) {
				throw (ex);
			}
		}
		return data.toString();
	}
	
	/**
	 * Executes an Shell Command and returns the output
	 * 
	 * @param cmd
	 *          The Command
	 * @return Output String
	 * @throws IOException
	 */
	public static String ExecuteCMD(String cmd) throws Exception {
		StringBuilder data = new StringBuilder();
		BufferedReader buffered_reader = null;
		try {
			// Runtime.getRuntime().exec("su");
			Process p = Runtime.getRuntime().exec("/bin/sh -c " + cmd);
			InputStream istream = p.getInputStream();
			InputStreamReader istream_reader = new InputStreamReader(istream);
			buffered_reader = new BufferedReader(istream_reader);
			String line;
			while ((line = buffered_reader.readLine()) != null) {
				data.append(line + "\n");
			}
			p.waitFor();
		} catch (Exception e) {
			throw (e);
		} finally {
			try {
				if (buffered_reader != null)
					buffered_reader.close();
			} catch (IOException ex) {
				throw (ex);
			}
		}
		return data.toString();
	}
	
	/**
	 * Executes an Shell Command as Super User and returns the output
	 * 
	 * @param cmd
	 *          The Command
	 * @return Output String
	 * @throws IOException
	 */
	public static String ExecuteCMD_SU(String cmd) throws Exception {
		StringBuilder data = new StringBuilder();
		BufferedReader buffered_reader = null;
		try {
			Process su = Runtime.getRuntime().exec("su");
			DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
			outputStream.writeBytes(cmd + "\n");
			outputStream.flush();
			
			outputStream.writeBytes("exit\n");
			outputStream.flush();
			
			InputStream istream = su.getInputStream();
			InputStreamReader istream_reader = new InputStreamReader(istream);
			buffered_reader = new BufferedReader(istream_reader);
			String line;
			while ((line = buffered_reader.readLine()) != null) {
				data.append(line + "\n");
			}
			su.waitFor();
		} catch (Exception e) {
			throw (e);
		} finally {
			try {
				if (buffered_reader != null)
					buffered_reader.close();
			} catch (IOException ex) {
				throw (ex);
			}
		}
		return data.toString();
	}
	
	/**
	 * Reads a file and return its contents.
	 * 
	 * @param file
	 *          The File
	 * @return File Contents
	 * @throws IOException
	 */
	public static String ReadFile(String file) throws IOException {
		StringBuilder data = new StringBuilder();
		BufferedReader buffered_reader = null;
		try {
			buffered_reader = new BufferedReader(new FileReader(file));
			String line;
			
			while ((line = buffered_reader.readLine()) != null) {
				data.append(line);
			}
		} catch (IOException e) {
			throw (e);
		} finally {
			try {
				if (buffered_reader != null)
					buffered_reader.close();
			} catch (IOException ex) {
				throw (ex);
			}
		}
		return data.toString();
	}
	
	/**
	 * Calls getprop on android shell and return system properties
	 * 
	 * @param property
	 * @return property value
	 */
	public static String GetProp(String property) {
		try {
			return Tools.ExecuteCMD(new String[] { "getprop", property }).replaceAll("\n", "").replaceAll("\r", "").trim();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * Calculates Subnet ID using Network Mask
	 * 
	 * @param NetworkMask
	 *          Network Mask
	 * @return Subnet ID
	 */
	public static int GetMaskID(String NetworkMask) {
		String[] splitted = NetworkMask.replaceAll("\n", "").replaceAll("\r", "").split("\\.");
		if (splitted.length != 4)
			return -1;
		
		int count = 0;
		for (int i = 0; i < 4; i++)
			count += Integer.bitCount(Integer.parseInt(splitted[i]));
		
		return count;
	}
	
	/**
	 * Does an ping to the <b>address</b> using interface <b>iface</b>
	 * 
	 * @param address Target Address
	 * @param iface Interface to use
	 * @return True if goes OK
	 */
	public static boolean CheckConnection(String address, String iface) {
		try {
			return Tools.ExecuteCMD(new String[] { "testping", address, iface }).contains("true");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Calculates the Network Address having one IP Address from it and the
	 * Network Mask
	 * 
	 * @param IP
	 *          One IP Address. <B>Ex:</B> 10.0.5.12
	 * @param NetworkMask
	 *          The NetworkMask <B>Ex:</B> 255.255.255.0
	 * @return
	 */
	public static String GetNetworkAddress(String IP, String NetworkMask) {
		String[] ipspllited = IP.replaceAll("\n", "").replaceAll("\r", "").trim().split("\\.");
		String[] maskspllited = NetworkMask.replaceAll("\n", "").replaceAll("\r", "").trim().split("\\.");
		StringBuilder NetAddr = new StringBuilder();
		
		if (ipspllited.length != 4 || maskspllited.length != 4)
			return "0.0.0.0";
		
		for (int i = 0; i < 4; i++) {
			NetAddr.append(Integer.parseInt(ipspllited[i]) & Integer.parseInt(maskspllited[i]));
			if (i != 3)
				NetAddr.append(".");
		}
		
		return NetAddr.toString();
	}
}
