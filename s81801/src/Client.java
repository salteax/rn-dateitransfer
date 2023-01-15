import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import java.util.zip.CRC32;
import java.nio.charset.StandardCharsets;

/*
 * 
 * 
 * 
 */

public class Client {
    public static int socketTimeout = 1000;
    public static double alpha = 0.9;
    public static int maxBytes = 1400;
    public static double totalDuration = 0.0, avgSpeed = 0.0, minSpeed = 0.0, maxSpeed = 0.0;

    public static void main(String args[]) {
        if(args.length != 4) {
            System.out.println("Expected parameters (in order): <ipaddress/hostname> <port> <filepath> <protocol>");
            System.exit(1);
        }

        /* variable delaration/initialization */
        String hostname, filepath, protocol;
        int port = 0, sessionNumber = 0, remainingBytes = 0, sendBytes = 0, crc32Int = 0, dataPacketNumber = 0;
        byte packetNumber = (byte) 0;
        long fileSize = 0;
        InetAddress address = null;
        DatagramSocket socket = null;
        byte[] startPacket = null, dataPacket = null, data = null, fileDataByte = null;
        Random random = null;
        CRC32 crc32 = null;
        FileInputStream fileInputStream = null;
        
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
        try {
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(fileDataByte);
            fileInputStream.close();
        } catch(FileNotFoundException ex) {
            System.out.println("Could not find file.");
            System.exit(1);
        } catch(IOException ex) {
            System.out.println("Could not read file.");
            System.exit(1);
        }
        
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
        startPacket = createStartPacket(file, sessionNumber, packetNumber);

        /* send start packet */
        try {
            socket = new DatagramSocket();
            System.out.println("Trying to send start packet with sessionnumber \'" + sessionNumber + "\' and packetnumber \'" + packetNumber + "\'.");
            if(sendDataPacket(socket, startPacket, address, port)) {
                System.out.println("Start packet send.");
            } else {
                System.out.println("Start packet could not be send.");
                System.exit(1);
            }
        } catch (SocketException ex) {
            System.out.println("Could not open socket.");
            System.exit(1);
        }
        
        /* send data packets */
        remainingBytes = (int) fileSize;
        while(sendBytes < remainingBytes+8) {
            if(packetNumber == (byte) 0) {
                packetNumber = (byte) 1;
            } else {
                packetNumber = (byte) 0;
            }

            if(remainingBytes < maxBytes) {
                maxBytes = remainingBytes;
            }
            remainingBytes = remainingBytes - maxBytes;

            data = new byte[maxBytes];
            data = Arrays.copyOfRange(fileDataByte, sendBytes, sendBytes+maxBytes);

            packetNumber++;
            if(remainingBytes == 0) {
                if(packetNumber == (byte) 0) {
                    packetNumber = (byte) 1;
                } else {
                    packetNumber = (byte) 0;
                }
                crc32 = new CRC32();
                crc32.update(fileDataByte);
                crc32Int = (int) crc32.getValue();
            }
            
            dataPacketNumber++;

            dataPacket = createDataPacket(file, sessionNumber, packetNumber, data, crc32Int, dataPacketNumber);

            System.out.println("Trying to send data packet with sessionnumber \'" + sessionNumber + "\' and packetnumber \'" + packetNumber + "\'.");
            if(sendDataPacket(socket, dataPacket, address, port)) {
                System.out.println("Successfully send data packet with number \'" + dataPacketNumber + "\'.");
            } else {
                System.out.println("Data packet with number \'" + dataPacketNumber + "\' could not be send.");
                System.exit(1);
            }

            if(remainingBytes == 0) {
                avgSpeed = fileSize/(1000*totalDuration);
                System.out.println("File transfered.");
                System.out.println("Total duration: " + String.format("%.5f", totalDuration) + "s");
                System.out.println("Minimal speed: " + String.format("%.5f", minSpeed) + "kb/s");
                System.out.println("Maximal speed: " + String.format("%.5f", maxSpeed) + "kb/s");
                System.out.println("Average speed: " + String.format("%.5f", avgSpeed) + "kb/s");
                break;
            }
        }
    }

    public static byte[] createStartPacket(File file, int sessionNumber, byte packetNumber) {
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
    
        byte[] startPacket = startPacketData.array();

        return startPacket;
    }

    public static byte[] createDataPacket(File file, int sessionNumber, byte packetNumber, byte[] data, long crc32Long, int dataPacketNumber) {
        ByteBuffer packetDataBuffer = null;

        if(crc32Long != 0) {
            packetDataBuffer = packetDataBuffer.allocate(8+maxBytes+8);
        } else {
            packetDataBuffer = packetDataBuffer.allocate(8+maxBytes);
        }
        packetDataBuffer.putInt(sessionNumber);
        packetDataBuffer.putInt(packetNumber);
        packetDataBuffer.put(data);
        if(crc32Long != 0) {
            packetDataBuffer.putLong(crc32Long);
        }

        System.out.println("Successfully created packet with number \'" + dataPacketNumber + "\'.");

        return packetDataBuffer.array();
    }

    public static boolean sendDataPacket(DatagramSocket socket, byte[] dataPacket, InetAddress address, int port) {
        DatagramPacket packet = null;
        byte[] returnDataPacket = new byte[4];
        int i = 0;
        long startTime = 0, stopTime = 0;
        double duration = 0, speed = 0;

        packet = new DatagramPacket(dataPacket, dataPacket.length, address, port);

        startTime = System.nanoTime();
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

            if(!Arrays.equals(Arrays.copyOfRange(returnDataPacket, 0, 4), Arrays.copyOfRange(dataPacket, 0, 4))) {
                System.out.println("Received packet with wrong sessionnumber.");
                continue;
            }

            if(!Arrays.equals(Arrays.copyOfRange(returnDataPacket, 4, 8), Arrays.copyOfRange(dataPacket, 4, 8))) {
                System.out.println("Received packet with wrong packetnumber.");
                continue;
            }
            
            stopTime = System.nanoTime();
            duration = (stopTime - startTime)/1000000000.0;
            speed = dataPacket.length/(1000*duration);
            
            totalDuration = totalDuration + duration;

            if(minSpeed == 0.0 || minSpeed > speed) {
                minSpeed = speed;
            }
            if(maxSpeed == 0.0 || maxSpeed < speed) {
                maxSpeed = speed;
            }

            System.out.println("Time passed for response: " + String.format("%.5f", duration) + "s");
            System.out.println("Speed: " + String.format("%.5f", speed) + "kb/s");
            return true;
        }
        stopTime = System.nanoTime();
        duration = (stopTime - startTime)/1000000000.0;
        System.out.println("Time passed with no response: " + String.format("%.5f", duration) + "s");
        return false;
    }
}
