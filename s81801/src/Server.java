import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import java.util.zip.CRC32;
import java.nio.charset.StandardCharsets;

public class Server {
    public static void main(String args[]) {
        if(args.length != 1  && args.length != 3) {
            System.out.println("Expected parameters (in order): <port> [<loss rate> <delay>]");
            System.exit(1);
        }
    
        int port = 0, delay = 0, fileNameSizeInt = 0, receiveCrc32Int = 0, crc32Int = 0, ack = 0, sessionNumber = 0, packetNumber = 0, i = 0, receivedBytes = 0, sessionNumberData = 0, packetNumberData = 0;
        long fileSizeLong = 0;
        double lossrate = 0;
        String fileNameString;
        DatagramSocket serverSocket = null;
        InetAddress address = null;
        ByteBuffer byteBuffer = null, fileDataBuffer = null;
        byte[] receiveData = new byte[1408], dataPacketByte = null, sessionNumberByte = new byte[2], startID = new byte[5], fileSize = new byte[8], fileNameSize = new byte[8], fileName = null, receiveCrc32 = new byte[8], packetNumberByte = new byte[4], returnData = new byte[3], sessionNumberDataByte = new byte[4], packetNumberDataByte = new byte[4], dataPacketByteClean = null, fileDataByte = null;
        CRC32 crc32 = null;
        File file = null;
        FileOutputStream fileOutputStream = null;
    
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
            System.out.println("Could not open socket on port \'" + port + "\'.");
            System.exit(1);
        }
        System.out.println("Socket started on port \'" + port + "\'.");

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
            System.out.println("Received start packet.");
            
            port = receivePacket.getPort();
            address = receivePacket.getAddress();

            dataPacketByte = Arrays.copyOfRange(receivePacket.getData(), 0, receivePacket.getLength());

            sessionNumberByte = Arrays.copyOfRange(dataPacketByte, 0, 4);
            byteBuffer = ByteBuffer.wrap(sessionNumberByte);
            sessionNumber = byteBuffer.getInt();

            packetNumberByte = Arrays.copyOfRange(dataPacketByte, 4, 8);
            byteBuffer = ByteBuffer.wrap(packetNumberByte);
            packetNumber = byteBuffer.getInt();
            if(packetNumber != ack) {
                System.out.println("Packetnumber \'" + packetNumber + "\' of startpacket is not equal to expected ACK \'"+ ack + "\'.");
                continue;
            }

            System.out.println("Received start packet with sessionnumber \'" + sessionNumber+ "\' and packetnumber \'" + packetNumber+ "\'.");

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
            byteBuffer = ByteBuffer.wrap(fileSize);
            fileSizeLong = byteBuffer.getLong();

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

            System.out.println("Trying to return start packet.");
            try {
                serverSocket.send(returnPacket);
            } catch(IOException ex) {
                System.out.println("Could not send data.");
                System.exit(1);
            }
            System.out.println("Returned start packet.");

            //ack++;

            try {
                serverSocket.setSoTimeout(1000);
            } catch(SocketException ex) {
                System.out.println("Problem with socket.");
            }

            while((receivedBytes < fileSizeLong) && (i < 10)) {
                receivePacket = new DatagramPacket(receiveData, receiveData.length); 

                System.out.println("Trying to receive data packet.");
                try {
                    serverSocket.receive(receivePacket);
                } catch(SocketTimeoutException ex) {
                    System.out.println("Could not receive data in time.");
                } catch(IOException ex) {
                    System.out.println("Could not receive data.");
                } 

                port = receivePacket.getPort();
                address = receivePacket.getAddress();


                dataPacketByte = new byte[receivePacket.getLength()];
                dataPacketByte = Arrays.copyOfRange(receivePacket.getData(), 0, receivePacket.getLength());
           
                sessionNumberDataByte = Arrays.copyOfRange(dataPacketByte, 0, 4);
                byteBuffer = ByteBuffer.wrap(sessionNumberDataByte);
                sessionNumberData = byteBuffer.getInt();

                packetNumberDataByte = Arrays.copyOfRange(dataPacketByte, 4, 8);
                byteBuffer = ByteBuffer.wrap(packetNumberDataByte);
                packetNumberData = byteBuffer.getInt();

                System.out.println("Received start packet with sessionnumber \'" + sessionNumberData + "\' and packetnumber \'" + packetNumberData + "\'");

                receiveData = new byte[dataPacketByte.length - 8];
                receiveData = Arrays.copyOfRange(receivePacket.getData(), 8, receivePacket.getLength());

                if(!Arrays.equals(sessionNumberDataByte, sessionNumberByte)) {
                    System.out.println("Sessionnumbers do not equal.");
                    i++;
                    continue;
                }

                /*if(packetNumberData != ack) {
                    // TODO
                }*/

                receivedBytes = receivedBytes + receiveData.length;

                returnData = new byte[4];
                returnData = Arrays.copyOfRange(dataPacketByte, 0, 4);
                returnPacket = new DatagramPacket(returnData, returnData.length, address, port);

                // TODO: Datei abspeichern

                if(receivedBytes < (int) fileSizeLong) {
                    fileDataBuffer = ByteBuffer.allocate(receivedBytes);
                    fileDataBuffer.put(receiveData);
                    fileDataByte = fileDataBuffer.array();

                    System.out.println("Trying to return data packet.");
                    try {
                        serverSocket.send(returnPacket);
                    } catch(IOException ex) {
                        System.out.println("Could not send data.");
                        System.exit(1);
                    }
                    System.out.println("Returned data packet.");
                } else {
                    System.out.println("test");

                    dataPacketByteClean = new byte[receiveData.length - 8];
                    dataPacketByteClean = Arrays.copyOfRange(receiveData, 0, receiveData.length - 8);

                    fileDataBuffer = ByteBuffer.allocate(receivedBytes-8);
                    fileDataBuffer.put(dataPacketByteClean);
                    fileDataByte = fileDataBuffer.array();

                    crc32 = new CRC32();
                    crc32.update(fileDataByte);
                    
                    receiveCrc32 = Arrays.copyOfRange(receiveData, receiveData.length-8, receiveData.length);
                    byteBuffer = ByteBuffer.wrap(receiveCrc32);
                    receiveCrc32Int = (int) byteBuffer.getLong();

                    if(receiveCrc32Int != (int) crc32.getValue()) {
                        System.out.println("File CRC32 does not match.");
                        System.exit(1);
                    } else {
                        fileNameString = new String(fileName, StandardCharsets.UTF_8);
                        fileNameString = fileNameString.substring(0, fileNameString.indexOf('.')) + "1" + fileNameString.substring(fileNameString.indexOf('.'), fileNameString.length());

                        try {
                            file = new File(fileNameString);

                            fileOutputStream = new FileOutputStream(fileNameString);
                            fileOutputStream.write(fileDataByte);
                            fileOutputStream.close();
                        } catch(FileNotFoundException ex) {
                            System.out.println("File not found.");
                            System.exit(1);
                        } catch(IOException ex) {
                            System.out.println("Could not create file.");
                            System.exit(1);
                        }

                        System.out.println("Created file \'" + fileNameString + "\'.");
                    }

                    System.out.println("Trying to return data packet.");
                    try {
                        serverSocket.send(returnPacket);
                    } catch(IOException ex) {
                        System.out.println("Could not send data.");
                        System.exit(1);
                    }
                    System.out.println("Returned data packet.");

                    break;
                }
            }
        }
    }
}
