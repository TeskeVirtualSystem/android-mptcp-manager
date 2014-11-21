package com.tvs.mptcpmanager;

import com.tvs.mptcptypes.NetworkInterface;


/**
 * Network Manager Static Methods
 * 
 * This should be used to manage network interfaces and MPTCP
 * @author Lucas Teske
 */
public class NetworkManager {
	
	/**
	 * Sets the MPTCP Enable Checksum Flag
	 * @param flag
	 */
	public static void MPTCP_SetChecksum(boolean flag)	{
		Tools.WriteSysctl("net.mptcp.mptcp_checksum", (flag?"1":"2"));
	}

	/**
	 * Sets the MPTCP Enabled Flag
	 * @param flag
	 */
	public static void MPTCP_SetEnabled(boolean flag)	{
		Tools.WriteSysctl("net.mptcp.mptcp_enabled", (flag?"1":"2"));
	}

	/**
	 * Gets the MPTCP Enable Checksum Flag
	 * @return True if enabled, false if not
	 */
	public static boolean MPTCP_GetChecksum()	{
		return Tools.ReadSysctl("net.mptcp.mptcp_checksum").contentEquals("1");
	}

	/**
	 * Gets the MPTCP Enabled Flag
	 * @return True if enabled, false if not
	 */
	public static boolean MPTCP_GetEnabled()	{
		return Tools.ReadSysctl("net.mptcp.mptcp_enabled").contentEquals("1");
	}
	
	/**
	 * Gets a list of NetworkInterface Objects filled with all network
	 * interfaces data.
	 * @return NetworkInterface Array
	 */
	public static NetworkInterface[] GetInterfaces()	{
		String[] devs = Tools.GetNetworkInterfacesList();
		NetworkInterface[] ifaces = new NetworkInterface[devs.length];
		for(int i=0;i<devs.length;i++)	{
			ifaces[i] = new NetworkInterface(devs[i]);
			ifaces[i].Update();
		}
		return ifaces;
	}
}
