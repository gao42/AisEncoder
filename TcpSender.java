import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class TcpSender {

    public static void main(String[] args) {
        String serverAddress = "localhost"; // Server address
        int port = 10110; // Server port
        //String message = "$GPRMC,135743,A,2352.8438,N,07615.0533,W,000.5,022.6,270322,,,A*61"; // Message to send
        //String message = "$GPGGA,161229.487,3723.2475,N,12158.3416,W,1,07,1.0,9.0,M,,M,,0000*18";
        //String message = "!AIVDM,1,1,,B,15N:Pj0P00PD;A6q=jqHrwbN0@E6,0*55";
        String message = "!AIVDM,1,1,,B,15N:Pj0P00PD;A6q=jqHrwbN0@E6,0*";
        //String message = "$GPRMC,225446,A,4916.45,N,12311.12,W,000.5,054.7,191194,020.3,E*68";
        //String message = "$GPRMC,225446,A,4741.15,N,12235.85,W,000.5,054.7,191194,003.1,W*79";
        //String message = "$RATLL,,3647.422,N,01432.592,E,,,,*58";
        //String message = "$RATLL,,3647.422,N,01432.592,E,,,,*";
        //String message = "$RATLL,91,4741.000,N,12235.6466,W,Tester,,,*";

        String checksum = calculateChecksum(message);
        message += checksum;

        try (Socket socket = new Socket(serverAddress, port);
             OutputStream outputStream = socket.getOutputStream();
             PrintWriter writer = new PrintWriter(outputStream, true)) {

            while (true) {
                writer.println(message);
                System.out.println("Message sent: " + message);
                Thread.sleep(1000); // Wait for 1 second
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String calculateChecksum(String nmeaSentence) {
        int checksum = 0;
        // Start after the '$' and stop before the '*'
        for (int i = 1; i < nmeaSentence.indexOf('*'); i++) {
            checksum ^= nmeaSentence.charAt(i);
        }
        // Return the checksum as a two-digit hexadecimal value
        return String.format("%02X", checksum);
    }

}