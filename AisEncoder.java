import java.util.BitSet;

public class AisEncoder {

    public static void main(String[] args) {
        // Example ship data
        int mmsi = 610000950; // Maritime Mobile Service Identity
        int navigationalStatus = 0; // Under way using engine
        int rateOfTurn = 0; // Not turning
        int speedOverGround = 150; // Speed over ground in knots * 10
        int positionAccuracy = 1; // High accuracy
        double longitude = 123.456; // Longitude in degrees
        double latitude = 45.678; // Latitude in degrees
        int courseOverGround = 2710; // Course over ground in degrees * 10
        int trueHeading = 270; // True heading in degrees
        int timestamp = 60; // UTC second when the report was generated

        String aisSentence = encodeAisSentence(mmsi, navigationalStatus, rateOfTurn, speedOverGround, positionAccuracy, longitude, latitude, courseOverGround, trueHeading, timestamp);
        String aivdmSentence = createAivdmSentence(aisSentence);
        System.out.println("AIVDM Sentence: " + aivdmSentence);
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

    private static void encodeInteger(BitSet bitSet, int value, int startIndex, int length) {
        for (int i = 0; i < length; i++) {
            bitSet.set(startIndex + i, (value & (1 << (length - i - 1))) != 0);
        }
    }

    private static String binaryToAis6Bit(String binaryString) {
        StringBuilder ais6BitString = new StringBuilder();
        for (int i = 0; i < binaryString.length(); i += 6) {
            int value = Integer.parseInt(binaryString.substring(i, i + 6), 2);
            if (value < 40) {
                ais6BitString.append((char) (value + 48));
            } else {
                ais6BitString.append((char) (value + 56));
            }
        }
        return ais6BitString.toString();
    }

    private static String createAivdmSentence(String aisSentence) {
        String payload = aisSentence;
        int fillBits = (6 - (payload.length() * 6) % 8) % 6;
        String aivdm = "!AIVDM,1,1,,A," + payload + "," + fillBits;
        return aivdm;
    }
}