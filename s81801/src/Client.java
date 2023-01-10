import java.net.InetAddress;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import java.util.zip.CRC32;

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

        /* variable delaration/initialization */
        String hostname, filepath, protocol;
        int port = 0;
        long fileSize = 0;
        InetAddress address = null;
        DatagramSocket socket = null;
        byte[] startPacket = null;
        
        /* get hostname, exception handling */
        hostname = args[0];
        try {
            address = InetAddress.getByName(hostname);
        } catch(UnknownHostException ex) {
            System.out.println("\'" + hostname + "\' is not a valid ip address/hostname.");
            System.exit(1);
        }
        
        /* get port, exception handling */ 
        try {
            port = Integer.parseInt(args[1]);
        } catch(NumberFormatException ex) {
            System.out.println("\'" + args[1] + "\' is not a valid number.");
            System.exit(1);
        } 
        
        /* get filepath, exception handling */
        filepath = args[2];
        File file = new File(filepath);
        if(!file.exists()) {
            System.out.println("\'" + filepath + "\' is not a valid file path.");
            System.exit(1);
        }
        
        /* get filesize */
        fileSize = file.length();

        /* get protocol, exception handling */
        protocol = args[3];
        if(!protocol.equals("sw") && !protocol.equals("gbn")) {
            System.out.println("\'" + protocol + "\' is not valid, it must be either sw or gbn.");
            System.exit(1);
        }
        
        startPacket = createStartPacket(file);

        /*try {
            socket = new DatagramSocket();

            /*while(true) {
                DatagramPacket request = new DatagramPacket(new byte[1], 1, address, port);
                socket.send(request);

                byte[] buffer = new byte[512];
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                socket.receive(response);

                String quote = new String(buffer, 0, response.getLength());

                System.out.println(quote + "\n");

                Thread.sleep(10000);
            }
        } catch(SocketTimeoutException ex) {
            System.out.println("Timeout error: " + ex.getMessage());
            ex.printStackTrace();
        } catch(IOException ex) {
            System.out.println("Client error: " + ex.getMessage());
            ex.printStackTrace();
        } catch(InterruptedException ex) {
            ex.printStackTrace();
        }*/
    }

    public static byte[] createStartPacket(File file) {
        int sessionNumber = 0, packetNumber = 0;
        long fileSize = 0, fileNameSize = 0;
        String strStartID = "Start";
        byte[] startID = new byte[5], fileNameByte = null;
        Random random = null;
        ByteBuffer startPacketData = null;
        CRC32 crc32 = null;
        
        random = new Random();
        sessionNumber = random.nextInt(65536);

        try {
            startID = strStartID.getBytes("US-ASCII");
        } catch(UnsupportedEncodingException ex) {
            System.out.println("Problem using charset \'US-ASCII\'.");
            System.exit(1);
        }

        fileSize = file.length();
        fileNameSize = file.getName().length();

        if(file.getName().toLowerCase().matches("^[a-z0-9äöüß_.-]")) {
            System.out.println("File name \'" + file.getName() + "\' does contain invalid characters.");
            System.exit(1);
        }

        try {
            fileNameByte = file.getName().getBytes("UTF-8");
        } catch(UnsupportedEncodingException ex) {
            System.out.println("Problem using charset \'UTF-8\'.");
            System.exit(1);
        }

        if(fileNameByte.length > 255) {
            fileNameByte = Arrays.copyOfRange(fileNameByte, 1, 255);
        }
        
        startPacketData = ByteBuffer.allocate(37 + fileNameByte.length);
        startPacketData.putInt(sessionNumber);
        startPacketData.putInt(packetNumber);
        startPacketData.put(startID);
        startPacketData.putLong(fileSize);
        startPacketData.putLong(fileNameSize);
        startPacketData.put(fileNameByte);

        crc32 = new CRC32();
        crc32.update(Arrays.copyOfRange(startPacketData.array(), 4, startPacketData.position()));
        startPacketData.putLong(crc32.getValue());
    
        byte[] startPacket = startPacketData.array();

        return startPacket;
    }

    public static void sendStartPacket() {}
}
