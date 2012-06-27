package fileinfo.impl;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zoli
 */
public class ServerUtil {
    
    public static List<String> createSupportedServerList(String name, String[] versions) {
        List<String> servers = new ArrayList<String>();
        for (String ver : versions) {
            servers.add(name + "/" + ver);
        }
        return servers;
    }
        
    public static boolean equal(Object x, Object y) {
        return ( x == null ? true : x.equals(y) );
    }
    
}
