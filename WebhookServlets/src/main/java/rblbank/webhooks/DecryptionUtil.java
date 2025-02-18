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

	private static String decryptionKey = "pa8kwajfiv0zsxymoyving88rms9n8o0pj2dt6r829qdhgv679qnh0jyr2mo4fl1";
	
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
	
	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		
		DecryptionUtil util = new DecryptionUtil();
		
		String encrypt = "hiaJETeRhlNgA6Eo9SF+HGn649IK2CzQ76/zjRJCgyXdh7lr5tkuGt/nQKhoLJOMIxXR0IWPv3F t+gobasj3hNqowREER6/z+dn/Bd7SzILyefazP2E1XidDlpmFUPo/FWKd6hmln9vM5NRj9GvTx8c/j W5++69tpfMF/ACXcgsYQ+IlDEqfk/qWoSzqx+lTszRJt1C0531D9qW0RSCKd/lfckqNApd7U9fqLVuT Et7rrzOZl9opbQcmrxAns0O3oLYpRntgdFyK+X0fIk9JxTEIFO5ShI0ajBr15NlQMhl87hrYAejus+n7pD LAT90xJRrjHC90pAwOYikSyBocy7MfPs0ba8+jFfvBUO8YplAHMYEEw3J5Qx/lJ9FAe1XauPS4mC0wZSHHLlBsBEzNMb1BXIg+fdyUf0QENkIxwMqBJD1u4pzT7H4ODqUzQDNcZJ9sWnbmWbxgnccbDY uL9RheJOJNYW97cgp2G6t8rgMm4uwIJzB2pX3MTuzuUZIwjhCK2q+PoiEKlB//ES+xg9NXJnPigZws vrUd4OPrXwQ/+D+6mDEyMtEg5ljZ+oVdanXnLYIbjfMCjdSp3Q9Mmm1b+aQLs3rAIf3F2c5vsMGv usrobacdWmc4bm66KUxxqZE8s+qq3UkCFC1yhBC+geBQ7sYei/r/G2WADI9wIX8Sq86le0KHBzF9 CDfAK17p5+/5rKMpk91u9MSvFYvWFd9/gNZ+q9BQD60Uy8W/l6qWoXUhdYoyPYpz8Zam5BmO Xu9WlAqEAit/dsMs4Pbh5XfwKvs9SKtKSzU77DOnlmpGU7/ptEw8+td21gQSaoy6PP8Nz5cwsZmJ DK1JMLYMEv4CjpQSwmQj2Y2W1enKfVzD96eBvCH/Gsw4FcM2O/gtOOoZmRZMRJr18+XsrXSZZ0 mnixkB77HjqhmdAsHV9eEDUuMKvpv2luZReMvwmHLutPBDXfik5SMsZxOmkUoygNJFYIRA/IXa+ eApLl5Zyz+Yod2Pqkzm3aOX5rtaqk7yV5uwt16usCXk9YEMijxbC+iObtBILkUf/Vh9JsfYcQFTra2otF q66kp6NeW2D33110eTSvocbWBhkLHx4YS8sJ0BOvB3r9R0Qiy8KFstIVbnVJeH+4Gf8tePMLV2RzE 2WyIFAF8qH3C1AkKjzgyO6ThdKSfmeetT7NjrRsW9Jc49GtioPJAafQMINJdqaJnerK9UtSFLde2LGst gSJrMj0oTluvARlO32MgZoUJEiKW2zOjW9Rdl/9jkICRFXxvzD0FNk3aocSAsfPfDUHFUFCZUriOlN6b Yo3yNvMEeAQw=";
		
		
			try {
				String decryptString = util.decrypt(encrypt, decryptionKey);
				System.out.println(decryptString);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}

}
