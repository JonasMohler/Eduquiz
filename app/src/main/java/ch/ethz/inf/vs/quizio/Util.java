package ch.ethz.inf.vs.quizio;

import android.net.wifi.WifiManager;
import android.util.Base64;

class Util {

    // Extract the hostpart of the IP, flip endianness (to save space) & encode
    static String encode(WifiManager wifiManager) {
        int ip = wifiManager.getConnectionInfo().getIpAddress();
        int netmask = wifiManager.getDhcpInfo().netmask;
        int suffix = flipEndianness(~netmask & ip);

        return Base64.encodeToString(String.valueOf(suffix).getBytes(), 0);
    }

    // Decode code, flip endianness & "append" it to the network prefix
    static int decode(WifiManager wifiManager, String code) throws IllegalArgumentException {
        int ip = wifiManager.getConnectionInfo().getIpAddress();
        int netmask = wifiManager.getDhcpInfo().netmask;
        int suffix = Integer.parseInt(new String(Base64.decode(code, 0)).trim());
        suffix = flipEndianness(suffix);

        return ip & netmask | suffix;
    }

    private static int flipEndianness(int ip) {
        int a = ip       & 0xFF;
        int b = ip >>  8 & 0xFF;
        int c = ip >> 16 & 0xFF;
        int d = ip >> 24 & 0xFF ;

        return (a << 24) | (b << 16) | (c << 8) | d;
    }

    // Pretty print an IP address given as int
    static String ipFormat(int ip) {
        return String.format("%d.%d.%d.%d",
                ip & 0xFF, ip >> 8 & 0xFF, ip >> 16 & 0xFF, ip >> 24 & 0xFF);
    }
}
