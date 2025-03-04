import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.BitSet;

public class AisEncoder {

    public static void main(String[] args) throws InterruptedException {
        // Example ship data
        int mmsi = 610000950; // Maritime Mobile Service Identity
        int navigationalStatus = 0; // Under way using engine
        int rateOfTurn = 0; // Not turning
        int speedOverGround = 100; // Speed over ground in knots * 10
        int positionAccuracy = 1; // High accuracy
        double longitude = -122.3394; // Longitude in degrees
        double latitude = 47.6035; // Latitude in degrees
        int courseOverGround = 2700; // Course over ground in degrees * 10
        int trueHeading = 270; // True heading in degrees
        int timestamp = 80; // UTC second when the report was generated

        // UDP socket setup
        String udpHost = "localhost";
        int udpPort = 10110;
        DatagramSocket socket = null;

        try {
            socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(udpHost);

            for (int i = 0; i < 100; i++) {
                // Update latitude and longitude based on speed over ground and true heading
                double distance = (speedOverGround / 10.0) / 3600.0; // distance in nautical miles
                double angle = Math.toRadians(trueHeading );
                latitude += distance * Math.cos(angle) / 60.0; // 1 nautical mile = 1 minute of latitude
                longitude += distance * Math.sin(angle) / (60.0 * Math.cos(Math.toRadians(latitude))); // Adjust for longitude

                String aisSentence = encodeAisSentence(mmsi, navigationalStatus, rateOfTurn, speedOverGround, positionAccuracy, longitude, latitude, courseOverGround, trueHeading, timestamp);
                String aivdmSentence = createAivdmSentence(aisSentence);

                String checksum = calculateChecksum(aivdmSentence);
                aivdmSentence += checksum;

                System.out.println("AIVDM Sentence: " + aivdmSentence);

                // Send the AIS sentence over UDP
                byte[] buffer = aivdmSentence.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, udpPort);
                socket.send(packet);

                // Increment timestamp
                timestamp = (timestamp + 1) % 60;

                // Wait for 1 second
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    public static String encodeAisSentence(int mmsi, int navigationalStatus, int rateOfTurn, int speedOverGround, int positionAccuracy, double longitude, double latitude, int courseOverGround, int trueHeading, int timestamp) {
        BitSet bitSet = new BitSet(168);

        // Encode Message Type (6 bits) - Type 1 for Position Report Class A
        encodeInteger(bitSet, 1, 0, 6);

        // Encode MMSI (30 bits)
        encodeInteger(bitSet, mmsi, 8, 30);

        // Encode Navigational Status (4 bits)
        encodeInteger(bitSet, navigationalStatus, 38, 4);

        // Encode Rate of Turn (8 bits)
        encodeInteger(bitSet, rateOfTurn, 42, 8);

        // Encode Speed Over Ground (10 bits)
        encodeInteger(bitSet, speedOverGround, 50, 10);

        // Encode Position Accuracy (1 bit)
        encodeInteger(bitSet, positionAccuracy, 60, 1);

        // Encode Longitude (28 bits)
        encodeInteger(bitSet, (int) (longitude * 600000), 61, 28);

        // Encode Latitude (27 bits)
        encodeInteger(bitSet, (int) (latitude * 600000), 89, 27);

        // Encode Course Over Ground (12 bits)
        encodeInteger(bitSet, courseOverGround, 116, 12);

        // Encode True Heading (9 bits)
        encodeInteger(bitSet, trueHeading, 128, 9);

        // Encode Timestamp (6 bits)
        encodeInteger(bitSet, timestamp, 137, 6);

        // Convert BitSet to binary string
        StringBuilder binaryString = new StringBuilder();
        for (int i = 0; i < 168; i++) {
            binaryString.append(bitSet.get(i) ? '1' : '0');
        }

        // Convert binary string to AIS 6-bit encoded string
        return binaryToAis6Bit(binaryString.toString());
    }

    public static void encodeInteger(BitSet bitSet, int value, int startIndex, int length) {
        for (int i = 0; i < length; i++) {
            bitSet.set(startIndex + i, (value & (1 << (length - i - 1))) != 0);
        }
    }

    public static String createAivdmSentence(String aisSentence) {
        // Placeholder for creating AIVDM sentence from AIS sentence
        return "!AIVDM,1,1,,A," + aisSentence + ",*";
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

    public static String binaryToAis6Bit(String binaryString) {
        StringBuilder ais6BitString = new StringBuilder();
        for (int i = 0; i < binaryString.length(); i += 6) {
            int value = Integer.parseInt(binaryString.substring(i, Math.min(i + 6, binaryString.length())), 2);
            if (value < 40) {
                value += 48;
            } else {
                value += 56;
            }
            ais6BitString.append((char) value);
        }
        return ais6BitString.toString();
    }
}