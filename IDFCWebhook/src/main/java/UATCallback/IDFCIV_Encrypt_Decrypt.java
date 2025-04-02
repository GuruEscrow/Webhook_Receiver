package UATCallback;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;

/*
* This Class demonstrates how the JAR encrypts and Decrypts using 
* AES key provided by user and Dynamic IV generated internally.
*  
*/

public class IDFCIV_Encrypt_Decrypt {
	private static SecureRandom random = new SecureRandom();
	private static final int lowAsciiLimit = 47; // letter '/'
	private static final int highAsciiLimit = 126; // letter 'z'

	private static String initVector;
	private static IvParameterSpec ivSpec;
	private static SecretKeySpec skeySpec;
	private static Cipher cipher;

	private static String finalEncryptedPayload = "";
	private static String decryptedText = "";

	/*
	 * This Method generates 16 byte IV with help of random 16 characters All these
	 * characters will be within the ASCII range 47-126
	 */
	public static String generateIv() {
		int ivLength = 16; // final length of IV String
		StringBuilder finalIvBuffer = new StringBuilder(ivLength);
		for (int i = 0; i < ivLength; i++) {
			int randomNumber = lowAsciiLimit + (int) (random.nextFloat() * (highAsciiLimit - lowAsciiLimit + 1));
			finalIvBuffer.append((char) randomNumber);
		}
		return finalIvBuffer.toString();
	}
	/*
	 * This Method Encrypts the String using the secret key provided by the user
	 * along with an internally generated IV generateIv() method.
	 * 
	 * The Secret Key must be in Hexadecimal format and 16/24/32 bytes in length after decoding of hex. 
	 * Encryption Cipher will be using AES/CBC/PKCS5PADDING
	 * 
	 * Returns a BASE64 Encoded String which is IV + Encrypted payload combination
	 */

	public static String encrypt(String dataToEncrypt, String secretHexKey)
			throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

		try {
			finalEncryptedPayload = "";
			//Decoding the Hexadecimal key to bytes
			byte[] secretKeyHexbytes = Hex.decodeHex(secretHexKey.toCharArray());

			//Generating the IV
			initVector = generateIv();
			//System.out.println("Dynamic IV: " + initVector);
			//Converting IVString to Spec
			ivSpec = new IvParameterSpec(initVector.getBytes("UTF-8"));

			//Creating a SecretKey Spec
			if (secretKeyHexbytes.length == 32 || secretKeyHexbytes.length == 24 || secretKeyHexbytes.length == 16) {
				skeySpec = new SecretKeySpec(secretKeyHexbytes, "AES");
			} else {
				throw new CustomException("Invalid Key Length, Must be 16/24/32 bytes");
			}
			
			cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			//Initialize the Cipher
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);

			//Encrypting the payload
			byte[] encryptedBytes = cipher.doFinal(dataToEncrypt.toString().getBytes());

			//Creating a final byte array, with iv length + length of encryptedBytes 
			byte[] finalarray = new byte[initVector.length() + encryptedBytes.length];

			//Copying IV bytes to final array.
			System.arraycopy(initVector.getBytes(), 0, finalarray, 0, initVector.getBytes().length);
			//Copying Encrypted Bytes to final array
			System.arraycopy(encryptedBytes, 0, finalarray, initVector.getBytes().length, encryptedBytes.length);

			//Encdoing the combined IV and encrypted payload in Base64
			finalEncryptedPayload = Base64.getEncoder().encodeToString(finalarray);

			return finalEncryptedPayload;

		} catch (UnsupportedEncodingException exc) {
			System.out.println(exc.getMessage());
			return finalEncryptedPayload;
		} catch (NoSuchAlgorithmException exc) {
			System.out.println(exc.getMessage());
			return finalEncryptedPayload;
		} catch (NoSuchPaddingException exc) {
			System.out.println(exc.getMessage());
			return finalEncryptedPayload;
		} catch (InvalidKeyException exc) {
			System.out.println(exc.getMessage());
			return finalEncryptedPayload;
		} catch (InvalidAlgorithmParameterException exc) {
			System.out.println(exc.getMessage());
			return finalEncryptedPayload;
		} catch (IllegalBlockSizeException exc) {
			System.out.println(exc.getMessage());
			return finalEncryptedPayload;
		} catch (BadPaddingException exc) {
			System.out.println(exc.getMessage());
			return finalEncryptedPayload;
		} catch (CustomException exc) {
			System.out.println(exc.getMessage());
			return finalEncryptedPayload;
		} catch (Exception exc) {
			System.out.println(exc.getMessage());
			return finalEncryptedPayload;
		}

	}

	/*
	 * This Method Decrypts the Encrypted String using the secret key provided by
	 * the user and the IV present in the first 16 bytes of payload.
	 * 
	 * The Secret Key must be in Hexadecimal format and 16/24/32 bytes in length
	 * after decoding of hex. Encryption Cipher will be AES/CBC/PKCS5PADDING
	 * 
	 * Returns Decrypted String
	 * 
	 */

	public static String decrypt(String encrypted, String secretKey)
			throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		try {
			decryptedText = "";
			//Decoding the Hexadecimal key to bytes
			byte[] secretKeyHexbytes = Hex.decodeHex(secretKey.toCharArray());
			//Creating the Secret key spec

			if (secretKeyHexbytes.length == 32 || secretKeyHexbytes.length == 24 || secretKeyHexbytes.length == 16) {
				skeySpec = new SecretKeySpec(secretKeyHexbytes, "AES");
			} else {
				throw new CustomException("Invalid Key Length, Must be 16/24/32 bytes");
			}

			//Decoding the Base64 string to combined byte array
			byte[] encryptedCombinedBytes = Base64.getDecoder().decode(encrypted);
			//Getting the IV from combined byte array
			byte[] iv = Arrays.copyOfRange(encryptedCombinedBytes, 0, 16); // IV String
			//Get the encrypted bytes from combined array for decryption
			byte[] encryptedPayload = Arrays.copyOfRange(encryptedCombinedBytes, iv.length,
					encryptedCombinedBytes.length); // encrypted text

			// creating the ivspec
			ivSpec = new IvParameterSpec(iv);
			
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			//Initialize the Cipher
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);

			//Decrypting the payload
			byte[] decryptedBytes = cipher.doFinal(encryptedPayload);

			//Creating a string from the decrypted bytes
			decryptedText = new String(decryptedBytes);

			return decryptedText;

		} catch (NoSuchAlgorithmException exc) {
			System.out.println(exc.getMessage());
			return decryptedText;
		} catch (NoSuchPaddingException exc) {
			System.out.println(exc.getMessage());
			return decryptedText;
		} catch (InvalidKeyException exc) {
			System.out.println(exc.getMessage());
			return decryptedText;
		} catch (InvalidAlgorithmParameterException exc) {
			System.out.println(exc.getMessage());
			return decryptedText;
		} catch (IllegalBlockSizeException exc) {
			System.out.println(exc.getMessage());
			return decryptedText;
		} catch (BadPaddingException exc) {
			System.out.println(exc.getMessage());
			return decryptedText;
		} catch (CustomException exc) {
			System.out.println(exc.getMessage());
			return finalEncryptedPayload;
		} catch (Exception exc) {
			System.out.println(exc.getMessage());
			return decryptedText;

		}
	}

	public static void main(String[] args) throws Exception {
		//Key and IV should be in Hexadecimal format only.
		String secretHexaKey = "12316d706c65445467654143536b959123616d706c65496488621144636b7562"; // 16 bit key in hexadecimal format
		String data = "{\"prefetchAccountReq\":{\"CBSTellerBranch\":\"\",\"CBSTellerID\":\"\",\"accountNumber\":\"21488530945\"}}";;

//		//Dynamic IV Encryption
		String encOutput = encrypt(data, secretHexaKey);
		System.out.println("Encryption: " + encOutput);

		String d = "S0F+XjBrWUlIMFFab2RmWvnPlONiCBfhwDtjFeZwSbphmUpd0rtXr5lsz1BnpCJrfB0Ui8pnWzkpqfgxute5muKg+tibhjbOeBGmKpsltbl0vuBu+syg4k/cqkLGskYutlYqGh3iqIVlSogZ8viqOR0XqomSdro7gkeQNgTM32TklZFS9CQqcFNtzXfr7df6/k3ZAI06nGebPOI8A6fVW66by3SpKgAJYe9KtcFsRzXIyUZs5o5rfs5wjQPGlAA8sF9dFPBDb/DY3NRIqWC1PZDcLvjYwS7ac6QO2M1huddDdX7TKNis3BAUwlAj2z9lGEfDojzVPyxS+0UEvtyB7L5PZCjwGs+zOs9DO8IQDQtpHjpGlARgqRpwwIgC4MQoP4zfaDWAFmXKqhE9iLjZRh8ToNWxgHUH5dJthHcq+cHMGV1iRE2wUtZXYPrVdS+7+A+p5Yteu6sCOVea6v1Jcw==";
		
		//Dynamic IV Decrytpion
		String decOutput = decrypt(encOutput, secretHexaKey);
		System.out.println("Decryption: " + decOutput);

	}
}
