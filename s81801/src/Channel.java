import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Random;

/** @author Jörg Vogt */

public class Channel {
  private static double lossRate = 0.0;
  private static int averageDelay = 0; // milliseconds
  private Random random;
  private static final int MTU_max = 65536; // for receiver

  public Channel() {
    // Create random number generator for use in simulating
    // packet loss and network delay.
    random = new Random();
    long seed = 1; // TODO remove if not debug
    random.setSeed(seed);
  }

  public Channel(double loss, int delay) {
    lossRate = loss;
    averageDelay = delay;
    random = new Random();
  }

  public static void setChannelSimulator(double loss, int delay) {
    lossRate = loss;
    averageDelay = delay;
  }

  public void sendPacket(DatagramSocket socket, DatagramPacket packet) throws Exception {
    if (simulateWANisOK()) {
      socket.send(packet);
      // System.out.println("Send packet...");
    } else System.err.println("Send packet lost...");
  }

  public DatagramPacket receivePacket(DatagramSocket socket)
      throws IOException, InterruptedException {
    DatagramPacket dataPacket = new DatagramPacket(new byte[MTU_max], MTU_max);
    do {
      socket.receive(dataPacket);
    } while (!simulateWANisOK());
    return dataPacket;
  }

  private boolean simulateWANisOK() throws InterruptedException {
    // Simulate network delay.
    Thread.sleep((int) (random.nextDouble() * 2 * averageDelay));
    // Decide whether to reply, or simulate packet loss
    return !(random.nextDouble() < lossRate);
  }
}
