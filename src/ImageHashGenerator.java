import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ImageHashGenerator {

    private static final Logger logger = Logger.getLogger(ImageHashGenerator.class.getName());

    public static String generateImageHash(byte[] fileBytes) {
        try {
            // Create a MessageDigest instance for SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Update the digest with the byte array
            digest.update(fileBytes);

            // Convert the hash bytes to a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            byte[] hashBytes = digest.digest();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            String hash = hexString.toString(); // Return the generated hash
            logger.info("Generated hash: " + hash);
            return hash;
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.SEVERE, "Error generating hash: Algorithm not found", e);
            return null;
        }
    }
}
