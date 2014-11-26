package com.tvs.mptcpmanager;

/**
 * Linux Route Manager using shell IP commands
 * 
 *  <BR>This assumes that you have rt_tables with:
 *  <i>
 *  	<BR>255	local
 *  	<BR>254	main
 *  	<BR>253	default
 *  	<BR>0	unspec
 *  	<BR>2	ethernet
 *  	<BR>3	wireless
 *  	<BR>4	modem
 *  </i>
 *  <BR> On Interface up:
 *  <BR> ip route add table $IFACE to $NETNUM/$SUBNET dev $IFACE scope link
 *  <BR> ip route add table $IFACE to via $GATEWAY dev $IFACE
 * @author Lucas Teske
 *
 */
public class RouteManager {
	enum ROUTE_TABLES	{
		ETHERNET(2),
		WIRELESS(3),
		MODEM(4);
		
		public int num;
		
		/**
		 * Initializes ROUTE_TABLES Enum with a value
		 * @param value
		 */
		ROUTE_TABLES(int value)	{
			num = value;
		}
	}
	
	public static void AddNetworkToTable(String Interface, String NetworkAddress, String Subnet)	{
		try {
			CallIP("route add table "+Interface+" to "+NetworkAddress+"/"+Subnet+" dev "+Interface+" scope link");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void AddNetworkGatewayToTable(String Interface, String Gateway)	{
		try {
			CallIP("route add table "+Interface+" default via "+Gateway+" dev "+Interface);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public static String CallIP(String args) throws Exception	{
		return Tools.ExecuteCMD("ip "+args);
	}
}
