import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpSender {

    public static void main(String[] args) {
        String host = "127.0.0.1"; // Host to send the message to
        int port = 10110; // Port to send the message to
        //String message = "!AIVDM,1,1,,B,15MwkT1P37G?fl0EJbR0OwT0@MS,04E"; // Message to send
        String message = "$GPGGA,123519,47.6693,N,122.6893,W,2,06,1.0,100,M,00000,000067"; // Message to send
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(host);
            byte[] buffer = message.getBytes();

            for (int i = 0; i < 1000; i++) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
                socket.send(packet);
                System.out.println("Message sent: " + message);

                // Wait for 10 milliseconds to send 100 messages per second
                Thread.sleep(1000);
            }

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}