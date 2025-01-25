import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpSender {

    public static void main(String[] args) {
        String host = "127.0.0.1"; // Host to send the message to
        int port = 7811; //10110; // Port to send the message to
        String messageTemplate = "$D@DBSI#,093430.5,6,100,50,200,191,28,250125"; // Message to send

        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(host);

            System.out.println("Sending messages on UDP port " + port + " to host " + host);

            for (int i = 0; i < 1000; i++) {
                String message = incrementValues(messageTemplate, i);
                //String checksum = calculateChecksum(message);
                //message += checksum;

                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
                socket.send(packet);
                System.out.println("Message sent: " + message);

                // Wait for 10 milliseconds to send 100 messages per second
                Thread.sleep(2000);
            }

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static String incrementValues(String message, int increment) {
        String[] parts = message.split(",");
        float timeValue = Float.parseFloat(parts[1]);
        int xValue = Integer.parseInt(parts[3]);
        timeValue += increment;
        xValue += increment;
        parts[1] = String.valueOf(timeValue);
        parts[3] = String.valueOf(xValue);
        return String.join(",", parts);
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