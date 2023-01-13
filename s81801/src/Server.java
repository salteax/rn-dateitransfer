import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import java.util.zip.CRC32;

public class Server {
    public static void main(String args[]) {
        if(args.length != 1  && args.length != 3) {
            System.out.println("Expected parameters (in order): <port> [<loss rate> <delay>]");
            System.exit(1);
        }
    
        int port = 0, delay = 0, fileNameSizeInt = 0, receiveCrc32Int = 0, crc32Int = 0, ack = 0, packetNumber = 0;
        double lossrate = 0;
        DatagramSocket serverSocket = null;
        InetAddress address = null;
        ByteBuffer byteBuffer = null;
        byte[] receiveData = new byte[1400], dataPacketByte = null, sessionNumberByte = new byte[2], startID = new byte[5], fileSize = new byte[8], fileNameSize = new byte[8], fileName = null, receiveCrc32 = new byte[8], packetNumberByte = new byte[4], returnData = new byte[3];
        CRC32 crc32 = null;
    
        try {
            port = Integer.parseInt(args[0]);
        } catch(NumberFormatException ex) {
            System.out.println("\'" + args[0] + "\' is not a valid number.");
            System.exit(1);
        } 

        if(args.length == 3) {
            try {
                lossrate = Double.parseDouble(args[1]);
            } catch(NumberFormatException ex) {
                System.out.println("\'" + args[1] + "\' is not a valid number.");
                System.exit(1);
            }
            if(lossrate < 0 || lossrate >= 1) {
                System.out.println("\'" + args[1] + "\' must be a number between 0 and 1.");
                System.exit(1);
            }

            try {
                delay = Integer.parseInt(args[2]);
            } catch(NumberFormatException ex) {
                System.out.println("\'" + args[2] + "\' is not a valid number.");
                System.exit(1);
            }
            if(delay < 0) {
                System.out.println("\'" + args[2] + "\' must be a positve number.");
                System.exit(1);
            }
        }

        try {
            serverSocket = new DatagramSocket(port);
        } catch(SocketException ex) {
            System.out.println("Could not open socket.");
        }

        while(true) { 
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length); 

            try {
                serverSocket.setSoTimeout(0);
                serverSocket.receive(receivePacket);
            } catch(SocketException ex) {
                System.out.println("Problem with socket.");
            } catch(IOException ex) {
                System.out.println("Could not receive data.");
            }
            
            port = receivePacket.getPort();
            address = receivePacket.getAddress();

            dataPacketByte = Arrays.copyOfRange(receivePacket.getData(), 0, receivePacket.getLength());
            sessionNumberByte = Arrays.copyOfRange(dataPacketByte, 0, 4);

            packetNumberByte = Arrays.copyOfRange(dataPacketByte, 4, 8);
            byteBuffer = ByteBuffer.wrap(packetNumberByte);
            packetNumber = byteBuffer.getInt();
            if(packetNumber != ack) {
                System.out.print("Packetnumber \'" + packetNumber + "\' of startpacket is not equal to expected ACK \'"+ ack + "\'.");
                continue;
            }

            startID = Arrays.copyOfRange(dataPacketByte, 8, 13);
            try {
                if(!Arrays.equals(startID, "Start".getBytes("US-ASCII"))) {
                    System.out.println("Startpacket contains wrong startID.");
                    continue;
                }
            } catch(UnsupportedEncodingException ex) {
                System.out.println("Problem using charset \'US-ASCII\'.");
                System.exit(1);
            }
            
            fileSize = Arrays.copyOfRange(dataPacketByte, 13, 21);
            fileNameSize = Arrays.copyOfRange(dataPacketByte, 21, 29);

            byteBuffer = ByteBuffer.wrap(fileNameSize);
            fileNameSizeInt = (int) byteBuffer.getLong();
            fileName = new byte[fileNameSizeInt];
            fileName = Arrays.copyOfRange(dataPacketByte, 29, 29+fileNameSizeInt);

            receiveCrc32 = Arrays.copyOfRange(dataPacketByte, 29+fileNameSizeInt, 29+fileNameSizeInt+8);
            byteBuffer = ByteBuffer.wrap(receiveCrc32);
            receiveCrc32Int = (int) byteBuffer.getLong();

            crc32 = new CRC32();
            crc32.update(Arrays.copyOfRange(dataPacketByte, 8, 29+fileNameSizeInt));
            crc32Int = (int) crc32.getValue();

            if(crc32Int != receiveCrc32Int) {
                System.out.println("The calculated CRC32 \'" + crc32Int + "\' does not match the received CRC32 \'" + receiveCrc32Int + "\'.");
                continue;
            }

            returnData = Arrays.copyOfRange(receiveData, 0, 4);
            DatagramPacket returnPacket = new DatagramPacket(returnData, returnData.length, address, port);

            try {
                serverSocket.send(returnPacket);
            } catch(IOException ex) {
                System.out.println("Could not send data.");
                System.exit(1);
            }
        }
    }
}
