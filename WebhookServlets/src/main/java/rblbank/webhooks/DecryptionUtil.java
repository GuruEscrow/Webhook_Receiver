package rblbank.webhooks;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class DecryptionUtil {

	Cipher _cx;

	byte[] _key, _iv;

	public DecryptionUtil() throws NoSuchAlgorithmException, NoSuchPaddingException {

		_cx = Cipher.getInstance("AES/CBC/PKCS5Padding");
		_key = new byte[32];
		_iv = new byte[16];
	}

	public String decrypt(String _encryptedText, String _encryptionKey)
			throws InvalidKeyException, UnsupportedEncodingException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException {
		String _out = "";
		int len = _encryptionKey.getBytes("UTF-8").length;
		if (_encryptionKey.getBytes("UTF-8").length > _key.length)
			len = _key.length;
		System.arraycopy(_encryptionKey.getBytes("UTF-8"), 0, _key, 0, len);
		SecretKeySpec keySpec = new SecretKeySpec(_key, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(_iv);
		_cx.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
		byte[] decodedValue = Base64.decodeBase64(_encryptedText.getBytes());
		byte[] decryptedVal = _cx.doFinal(decodedValue);
		_out = new String(decryptedVal);
		return _out;
	}

}
