package indianbank.webhooks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Servlet implementation class LiveWebhook
 */
@WebServlet("/indianuat/validation")
public class IndianBankUatValidationWebhook extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public IndianBankUatValidationWebhook() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("application/json");
		response.setStatus(200);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		final String key = "INDESCR0w@24ioE0";
		response.setContentType("application/json");

		// Extracting the request body as json format body
		StringBuilder requestBody = new StringBuilder();
		BufferedReader reader = request.getReader();
		String line;
		while ((line = reader.readLine()) != null) {
			requestBody.append(line);
		}
		reader.close();
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

			//Decrypting the request body
			Object requestJson = objectMapper.readValue(requestBody.toString(), Object.class);
			String requestString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestJson);
			JsonNode requestJsonNode = objectMapper.readTree(requestString);
			String decrypted_requestbody = decrypt(requestJsonNode.get("data").asText(), key);
			
            //Converting the decrypted body to json format body
			Object jsonObject = objectMapper.readValue(decrypted_requestbody, Object.class);
			String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
			JsonNode jsonNode = objectMapper.readTree(prettyJson);

			System.out.println("Confirmation Requested body:");
			System.out.println(prettyJson);

			// Validating the required fields are there or not
			PrintWriter out = response.getWriter();
			if (!(jsonNode.has("VirtualAccountNo") && jsonNode.has("Amount") && jsonNode.has("RemiiterAccountNo") && jsonNode.has("RemitterIFSCode"))) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				String res = "{" + " \"message\":\"VirturalAccoutnNo, Amount, RemitterAccountNo & RemitterIFSCode are required field\"" + "}";
				String encryRes = "{" + " \"data\":\""+encrypt(res, key)+"\"" + "}";
				out.println(encryRes);
				out.close();
				System.out.println("=============================================================================");
				return;
			}

			// Validating the Virtual Account formating
			if (!(jsonNode.get("VirtualAccountNo").asText().length() >= 8)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				String res = "{" + " \"message\":\"VirturalAccoutnNo must be greater than 7 characters\"" + "}";
				String encryRes = "{" + " \"data\":\""+encrypt(res, key)+"\"" + "}";
				out.println(encryRes);
				out.close();
				System.out.println("=============================================================================");
				return;
			} else {
				if (!(jsonNode.get("VirtualAccountNo").asText().substring(0, 6).equals("ESCROW"))) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					String res = "{" + " \"message\":\"Invalid corparte prefix in Virtual Account No\"" + "}";
					String encryRes = "{" + " \"data\":\""+encrypt(res, key)+"\"" + "}";
					out.println(encryRes);
					out.close();
					System.out.println("=============================================================================");
					return;
				}
			}

			// Validating the amount
			if (!(jsonNode.get("Amount").asDouble() >= 1)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				String res = "{" + " \"message\":\"Amount should be greater than 0\"" + "}";
				String encryRes = "{" + " \"data\":\""+encrypt(res, key)+"\"" + "}";
				out.println(encryRes);
				out.close();
				System.out.println("=============================================================================");
				return;
			}

			// Validating that VAN is present in the data base
			ArrayList<String> vanNum = new ArrayList<String>();
			vanNum.add("ESCROW001");
			vanNum.add("ESCROW002");

			Map<String, Map<String, String>> whitelistedAcc = new LinkedHashMap<String, Map<String, String>>();
			whitelistedAcc.put("ESCROW001", new LinkedHashMap<String, String>() {
				{
					put("123456789", "ABCD0000001");
					put("234567891", "ABCD0000002");
				}
			});
			whitelistedAcc.put("ESCROW002", new LinkedHashMap<String, String>() {
				{
					put("345678912", "ABCD0000001");
					put("456789123", "ABCD0000002");
				}
			});

			String van = jsonNode.get("VirtualAccountNo").asText();
			String remiterAccNo = jsonNode.get("RemiiterAccountNo").asText();
			String remiterIfsc = jsonNode.get("RemitterIFSCode").asText();
			if (vanNum.contains(van)) {
				Map<String,String> whitelistedAccDetls = whitelistedAcc.get(van);
				if(whitelistedAccDetls.containsKey(remiterAccNo)) {
					if(whitelistedAccDetls.get(remiterAccNo).equals(remiterIfsc)) {
						response.setStatus(HttpServletResponse.SC_OK);
						String res = "{\n" + " \"ResponseCode\":\"0\",\n" + "    \"ResponseResult\":\"Success\"" + "\n}";
						String encryRes = "{" + " \"data\":\""+encrypt(res, key)+"\"" + "}";
						out.println(encryRes);
						out.close();
					}else {
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						String res = "{" + " \"message\":\"IFSC is not matching\"" + "}";
						String encryRes = "{" + " \"data\":\""+encrypt(res, key)+"\"" + "}";
						out.println(encryRes);
						out.close();
						System.out.println("=============================================================================");
						return;
					}
				}else {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					String res = "{" + " \"message\":\"Remitting account is not whitelisted\"" + "}";
					String encryRes = "{" + " \"data\":\""+encrypt(res, key)+"\"" + "}";
					out.println(encryRes);
					out.close();
					System.out.println("=============================================================================");
					return;
				}
				
			} else {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				String res = "{\n" + " \"ResponseCode\":\"1\",\n" + "    \"ResponseResult\":\"Demant mismatch\"" + "\n}";
				String encryRes = "{" + " \"data\":\""+encrypt(res, key)+"\"" + "}";
				out.println(encryRes);
				out.close();
			}

			System.out.println("=============================================================================");
		}catch (IllegalArgumentException decryptionExp) {
			// TODO: handle exception
			PrintWriter out = response.getWriter();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			String res = "{" + " \"message\":\"Not a proper BASE65 encrypted data\"" + "}";
			out.println(res);
			out.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	// Encryption code
	public static String encrypt(String vMessage, String vKey) {

		String myKey = vKey;// "IND1Pay37@#78Zj1";

		String encrypted = null;
		try {
			SecretKey key = new SecretKeySpec(myKey.getBytes(), "AES");
			Cipher ecipher = Cipher.getInstance("AES");
			ecipher.init(Cipher.ENCRYPT_MODE, key);

			byte[] utf8 = vMessage.getBytes("UTF-8");
			byte[] enc = ecipher.doFinal(utf8);

			encrypted = Base64.getEncoder().encodeToString(enc);
		} catch (Exception e) {
			e.printStackTrace();
			// Handle the exception according to your needs
		}
		return encrypted;
	}

	// Decryption code
	public static String decrypt(String encryptedMessage, String secretKey) {
		
		try {
			SecretKey key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8),"AES");
			
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			
			byte[] encryptedBytes = Base64.getDecoder().decode(encryptedMessage);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            
            return new String(decryptedBytes, StandardCharsets.UTF_8);
		}catch (Exception e) {
			// TODO: handle exception
			//e.printStackTrace();
			return null;
		}
	}

}
