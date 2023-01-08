public class Server {
    public static void main(String args[]) {
        if(args.length != 1  && args.length != 3) {
            System.out.println("Expected parameters (in order): <port> [<loss rate> <delay>]");
            System.exit(1);
        }
    
        int port = 0, delay = 0;
        double lossrate = 0;
    
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
    }
}
