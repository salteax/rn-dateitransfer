/** @author Jörg Vogt */

/*
 * Remarks: UDP-checksum calculation, UDP-Lite RFC 3828
 * UDP checksum is calculated over IP-Pseudo-Header, UDP-Header, UDP-Data
 * No option to disable checksum in JAVA for UDP
 * UDP-Lite is part of Linux-kernel since 2.6.20
 * UDP-Lite support in java not clear
 */

public class FileCopy {

  public static void main(String[] args) throws Exception {
    int port;

    if (args.length != 2 && args.length !=4) {
      System.out.println("Usage: FileCopy server port [loss] [delay]");
      System.out.println("Usage: FileCopy client host port file");
      System.exit(1);
    }

    switch (args[0]) {
      case "client":
        String host = args[1];
        port = Integer.parseInt(args[2]);
        String fileName = args[3];
        System.out.println("Client started for connection to: " + host + " at port " + port);
        sendFile(host, port, fileName);
        break;

      case "server":
        port = Integer.parseInt(args[1]);
        if (args.length == 4) {
          double loss = Double.parseDouble(args[2]);
          int delay = Integer.parseInt(args[3]);
          Channel.setChannelSimulator(loss, delay);
        }
        System.out.println("Server started at port: " + port);
        handleConnection(port);
        break;
      default:
    }
  }

  private static void sendFile(String host, int port, String fileName) throws Exception {
    FileTransfer myFT = new FileTransfer(host, port, fileName);
    boolean c = myFT.file_req();
    if (c) System.out.println("Client: Ready");
    else {
      System.out.println("Client: Abort because of maximum retransmission");
      System.exit(1);
    }
  }

  private static void handleConnection(int port) throws Exception {
    FileTransfer myFT = new FileTransfer();
    myFT.file_init(port);
  }
}
