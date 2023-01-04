import java.net.InetAddress;
import java.io.*;
import java.net.*;
import java.nio.*;

/*
 * 
 * 
 * 
 */

public class Client {
    public static void main(String args[]) {
        if(args.length != 4) {
            System.out.println("Expected parameters (in order): <ipaddress/hostname> <port> <filepath> <protocol>");
            System.exit(1);
        }

        String hostname, filepath, protocol;
        int port;

        hostname = args[0];
        try {
            InetAddress address = InetAddress.getByName(hostname);
        } catch(UnknownHostException e) {
            System.out.println("\'" + hostname + "\' is not a valid ip address/hostname.");
            System.exit(1);
        }
        
        port = 0;
        try {
            port = Integer.parseInt(args[1]);
        } catch(NumberFormatException e) {
            System.out.println("\'" + Integer.toString(port) + "\' is not a valid number.");
            System.exit(1);
        } 
        
        filepath = args[2];
        File file = new File(filepath);
        if(!file.exists()) {
            System.out.println("\'" + filepath + "\' is not a valid path.");
            System.exit(1);
        }
        
        protocol = args[3];
        if(!protocol.equals("sw") || !protocol.equals("gbn")) {
            System.out.println("\'" + protocol + "\' is not valid, it must be either sw or gbn.");
            System.exit(1);
        }
    
    }
}
