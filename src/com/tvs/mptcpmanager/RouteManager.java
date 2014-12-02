package com.tvs.mptcpmanager;

/**
 * Linux Route Manager using shell IP commands
 * 
 * <BR>
 * This assumes that you have rt_tables with: <i> <BR>
 * 255 local <BR>
 * 254 main <BR>
 * 253 default <BR>
 * 0 unspec <BR>
 * 2 ethernet <BR>
 * 3 wireless <BR>
 * 4 modem </i> <BR>
 * On Interface up: <BR>
 * ip route add table $TABLE to $NETNUM/$SUBNET dev $IFACE scope link <BR>
 * ip route add table $TABLE to via $GATEWAY dev $IFACE
 * 
 * @author Lucas Teske
 * 
 */
public class RouteManager {
	enum ROUTE_TABLES {
		ETHERNET(2), WIRELESS(3), MODEM(4);
		
		public int num;
		
		/**
		 * Initializes ROUTE_TABLES Enum with a value
		 * 
		 * @param value
		 */
		ROUTE_TABLES(int value) {
			num = value;
		}
	}
	
	public static void AddDefaultHop(String address, String iface) {
		try {
			CallIP("route add default scope global nexthop via " + address + " dev " + iface);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds Network to a Route Table
	 * 
	 * @param Table
	 *          Network Routing Table (ethernet, wireless or modem)
	 * @param Interface
	 *          Network Interface
	 * @param NetworkAddress
	 *          Network Address
	 * @param Subnet
	 *          Sub Network Address
	 */
	public static void AddNetworkToTable(String Table, String Interface, String NetworkAddress, String Subnet) {
		try {
			CallIP("route add table " + Table + " to " + NetworkAddress + "/" + Subnet + " dev " + Interface + " scope link");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Add Gateway to Route Table
	 * 
	 * @param Table
	 *          Network Routing Table (ethernet, wireless or modem)
	 * @param Interface
	 *          Network Interface
	 * @param Gateway
	 *          Network Gateway
	 */
	public static void AddNetworkGatewayToTable(String Table, String Interface, String Gateway) {
		try {
			CallIP("route add table" + Table + " default via " + Gateway + " dev " + Interface );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Clear the Gateway Routes
	 */
	public static void ClearRoutes() {
		try {
			CallIP("route del 0/0");
			CallIP("route del 0/0");
			CallIP("route del 0/0");
			CallIP("route del 0/0");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets an interface Gateway Address
	 * 
	 * @param iface
	 *          Network Address
	 * @return Gateway Address
	 */
	public static String GetIFaceGateway(String iface) {
		return Tools.GetProp("dhcp." + iface + ".gateway");
	}
	
	/**
	 * Gets an interface IP Address
	 * 
	 * @param iface
	 *          Network Address
	 * @return IP Address
	 */
	public static String GetIFaceIP(String iface) {
		return Tools.GetProp("dhcp." + iface + ".ipaddress");
	}
	
	/**
	 * Cleans an specific routing table
	 * 
	 * @param table
	 *          The routing table name or number
	 */
	public static void CleanRouteTable(String table) {
		try {
			CallIP("route flush table " + table );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Calls ip command
	 * 
	 * @param args
	 *          Arguments
	 * @return String Output
	 * @throws Exception
	 */
	public static String CallIP(String args) throws Exception {
		return Tools.ExecuteCMD_SU("ip " + args);
	}
	
	/**
	 * Calls ip command
	 * 
	 * @param args
	 *          Arguments
	 * @return String Output
	 * @throws Exception
	 */
	public static String CallIP(String[] args) throws Exception {
		String[] tmp = new String[args.length + 1];
		tmp[0] = "ip";
		for (int i = 0; i < args.length; i++)
			tmp[i + 1] = args[i];
		return Tools.ExecuteCMD(tmp);
	}
}
