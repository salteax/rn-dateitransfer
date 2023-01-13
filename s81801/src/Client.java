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
    public static int socketTimeout = 1000;
    public static double alpha = 0.9;

    public static void main(String args[]) {
        if(args.length != 4) {
            System.out.println("Expected parameters (in order): <ipaddress/hostname> <port> <filepath> <protocol>");
            System.exit(1);
        }

        /* variable delaration/initialization */
        String hostname, filepath, protocol;
        int port = 0, sessionNumber = 0, packetNumber = 0, remainingBytes = 0, sendBytes = 0, maxBytes = 1400;
        long fileSize = 0;
        InetAddress address = null;
        DatagramSocket socket = null;
        byte[] startPacket = null, dataPacket = null, data = null, fileDataByte = null;
        Random random = null;
        
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

        /* create filedatabyte */
        fileDataByte = new byte[(int)fileSize];

        /* get protocol, exception handling */
        protocol = args[3];
        if(!protocol.equals("sw") && !protocol.equals("gbn")) {
            System.out.println("\'" + protocol + "\' is not valid, it must be either sw or gbn.");
            System.exit(1);
        }
        
        /* create sessionnumber */
        random = new Random();
        sessionNumber = random.nextInt(65536);

        /* create start packet */
        startPacket = createStartPacket(file, sessionNumber); // lesen crc32, bytebuffer, byte[]

        /* send start packet */
        try {
            socket = new DatagramSocket();
            sendStartPacket(socket, startPacket, address, port);
        } catch (SocketException ex) {
            System.out.println("Could not open socket.");
            System.exit(1);
        }
        
        remainingBytes = (int) fileSize;
        while(sendBytes < remainingBytes) {
            if(remainingBytes < maxBytes) {
                maxBytes = remainingBytes;
            }
            remainingBytes = remainingBytes - maxBytes;

            data = new byte[maxBytes];
            data = Arrays.copyOfRange(fileDataByte, sendBytes, sendBytes+maxBytes);
            
            packetNumber++;
            if(remainingBytes == maxBytes) {
                // letztes Packet, CRC32 senden
                dataPacket = createDataPacket(file, sessionNumber, packetNumber, data, 0); // CRC anstelle von null
            } else {
                dataPacket = createDataPacket(file, sessionNumber, packetNumber, data, 0);
            }

            
            
        }
    }

    public static byte[] createStartPacket(File file, int sessionNumber) {
        int packetNumber = 0;
        long fileSize = 0, fileNameSize = 0;
        String strStartID = "Start";
        byte[] startID = new byte[5], fileNameByte = null;
        
        ByteBuffer startPacketData = null;
        CRC32 crc32 = null;
        
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
        crc32.update(Arrays.copyOfRange(startPacketData.array(), 8, startPacketData.position()));
        startPacketData.putLong(crc32.getValue());
        System.out.println(crc32.getValue());
    
        byte[] startPacket = startPacketData.array();

        System.out.println(Arrays.toString(startPacket));

        return startPacket;
    }

    public static void sendStartPacket(DatagramSocket socket, byte[] dataPacket, InetAddress address, int port) {
        DatagramPacket packet = null;
        byte[] returnDataPacket = new byte[2];
        int i = 0;

        packet = new DatagramPacket(dataPacket, dataPacket.length, address, port);

        while(i < 10) {
            i++;
            try {
                socket.setSoTimeout(socketTimeout);
                socket.send(packet);
            } catch(IOException ex) {
                System.out.println("Could not send packet.");
                continue;
            }
            try {
                socket.receive(packet);
            } catch(IOException ex) {
                System.out.println("Could not receive packet.");
                socketTimeout = (int) ((alpha * socketTimeout) + ((1-alpha) * socketTimeout));
                continue;
            }

            returnDataPacket = packet.getData();

            if(!Arrays.equals(Arrays.copyOfRange(returnDataPacket, 0, 2), Arrays.copyOfRange(dataPacket, 0, 2))) {
                System.out.println("Received packet with wrong sessionnumber.");
                continue;
            }
            if(!Arrays.equals(Arrays.copyOfRange(returnDataPacket, 2, 4), Arrays.copyOfRange(dataPacket, 2, 4))) {
                System.out.println("Received packet with wrong packetnumber.");
                continue;
            }
            System.out.println("Received response.");
            break;
        }
    }

    public static byte[] createDataPacket(File file, int sessionNumber, int packetNumber, byte[] data, long crc32Long) {
        ByteBuffer packetDataBuffer = null;

        packetData.allocate(4+maxBytes);
        packetData.putInt(sessionNumber);
        packetData.putInt(packetNumber);
        packetData.put(data);
        if(crc32Long != 0) {
            packetData.putLong(crc32Long);
        }

        return packetData.array();
    }
}
