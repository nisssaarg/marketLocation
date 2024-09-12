import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ImageHashGenerator {

    private static final String ERROR_GENERATING_HASH_ALGORITHM_NOT_FOUND = "Error generating hash: Algorithm not found";
    private static final String GENERATED_HASH = "Generated hash: ";
    private static final String SHA_256 = "SHA-256";
    private static final Logger logger = Logger.getLogger(ImageHashGenerator.class.getName());

    public static String generateImageHash(byte[] fileBytes) {
        try {
            // Create a MessageDigest instance for SHA-256
            MessageDigest digest = MessageDigest.getInstance(SHA_256);

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
            logger.info(GENERATED_HASH + hash);
            return hash;
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.SEVERE, ERROR_GENERATING_HASH_ALGORITHM_NOT_FOUND, e);
            return null;
        }
    }
}
