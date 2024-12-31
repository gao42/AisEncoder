import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class GpmrcGenerator {

    public static void main(String[] args) {
        String serverAddress = "localhost"; // Server address
        int port = 10110; // Server port

        double latitude = 47.6896; // Initial latitude
        double longitude = -122.6060; // Initial longitude
        double speed = 7.0; // Speed in knots
        double course = 180.0; // Course in degrees

        try (Socket socket = new Socket(serverAddress, port);
             OutputStream outputStream = socket.getOutputStream();
             PrintWriter writer = new PrintWriter(outputStream, true)) {

            while (true) {
                String message = generateGPRMCSentence(latitude, longitude, speed, course);
                writer.println(message);
                System.out.println("Message sent: " + message);

                // Update latitude and longitude based on speed and course
                double distance = speed * 1.852 / 3600.0; // Distance in kilometers per second
                double earthRadiusKm = 6371.0; // Earth's radius in kilometers

                double deltaLat = distance * Math.cos(Math.toRadians(course)) / earthRadiusKm;
                double deltaLon = distance * Math.sin(Math.toRadians(course)) / (earthRadiusKm * Math.cos(Math.toRadians(latitude)));

                latitude += Math.toDegrees(deltaLat);
                longitude += Math.toDegrees(deltaLon);

                Thread.sleep(1000); // Wait for 1 second
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String generateGPRMCSentence(double latitude, double longitude, double speed, double course) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HHmmss.SSS");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String time = dateFormat.format(new Date());

        String latHemisphere = latitude >= 0 ? "N" : "S";
        String lonHemisphere = longitude >= 0 ? "E" : "W";

        latitude = Math.abs(latitude);
        longitude = Math.abs(longitude);

        String latString = String.format("%02d%07.4f", (int) latitude, (latitude - (int) latitude) * 60);
        String lonString = String.format("%03d%07.4f", (int) longitude, (longitude - (int) longitude) * 60);

        String sentence = String.format("$GPRMC,%s,A,%s,%s,%s,%s,%.1f,%.1f,%s,,",
                time, latString, latHemisphere, lonString, lonHemisphere, speed, course, new SimpleDateFormat("ddMMyy").format(new Date()));

        return sentence + "*" + calculateChecksum(sentence);
    }

    private static String calculateChecksum(String sentence) {
        int checksum = 0;
        for (int i = 1; i < sentence.length(); i++) {
            checksum ^= sentence.charAt(i);
        }
        return String.format("%02X", checksum);
    }
}