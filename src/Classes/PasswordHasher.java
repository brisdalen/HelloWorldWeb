package Classes;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

public class PasswordHasher {

    public PasswordHasher() { }

    public String stringToSaltedHash(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        return stringToSaltedHash(password, salt);
    }

    public String stringToSaltedHash(String password, byte[] saltBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        int iterations = 65536;
        char[] passChar = password.toCharArray();
        byte[] salt = saltBytes;

        PBEKeySpec spec = new PBEKeySpec(passChar, salt, iterations, 512);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = factory.generateSecret(spec).getEncoded();

        return toHex(salt) + ":" + toHex(hash);
    }

    public boolean matchPasswords(String password1, String password2) throws InvalidKeySpecException, NoSuchAlgorithmException {

        System.out.println("Password 1: " + password1);

        String[] parts1 = password1.split(":");
        for(String s : parts1) {
            System.out.println(s);
        }

        String hash2 = stringToSaltedHash(password2, fromHex(parts1[1]));

        System.out.println("Password 2: " + hash2);

        return password1.equals(hash2);
    }

    private static String toHex(byte[] array) throws NoSuchAlgorithmException {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0)
        {
            return String.format("%0"  +paddingLength + "d", 0) + hex;
        }else{
            return hex;
        }
    }

    private static byte[] fromHex(String hex) throws NoSuchAlgorithmException {
        byte[] bytes = new byte[hex.length() / 2];
        for(int i = 0; i<bytes.length ;i++)
        {
            bytes[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    /*
    public static void main(String[] args) {
        try {
            PasswordHasher pt = new PasswordHasher();
            String originalPass = pt.stringToSaltedHash("1234");
            System.out.println(pt.testPasswords(originalPass, "1234"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }
    */
}
