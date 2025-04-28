package UATCallback;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Servlet implementation class VANValidationCallBack
 */
@WebServlet("/vanvalidation/uat")
public class VANValidationCallBack extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public VANValidationCallBack() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}
	
	private static String getResponsePayload(String vanNum,String banRef,String status,String statusDesc,String errCode,String errDesc) {
		
		String jsonString = "{\n" +
			    "    \"vANum\": \""+vanNum+"\",\n" +
			    "    \"bankRef\": \""+banRef+"\",\n" +
			    "    \"status\": \""+status+"\",\n" +
			    "    \"statusDesc\": \""+statusDesc+"\",\n" +
			    "    \"errorCode\": \""+errCode+"\",\n" +
			    "    \"errorDesc\": \""+errDesc+"\"\n" +
			    "}";
		
		return jsonString;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//Key and IV should be in Hexadecimal format only.
		String secretHexaKey = "12316d706c65445467654143536b959123616d706c65496488621144636b7562";
		response.setContentType("application/octet-stream");
		
		//Creating object for encryption and decryption code
		IDFCIV_Encrypt_Decrypt idfcUtility = new IDFCIV_Encrypt_Decrypt();

		try {
			// Extracting the request body as json format body
			StringBuilder encryptedRequest = new StringBuilder();
			BufferedReader reader = request.getReader();
			String line;
			while ((line = reader.readLine()) != null) {
				encryptedRequest.append(line);
			}
			reader.close();
			
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String strJsonReq = idfcUtility.decrypt(encryptedRequest.toString(), secretHexaKey);
			
			JsonObject jsonReq = JsonParser.parseString(strJsonReq).getAsJsonObject();

			System.out.println("IDFC VAN Validation");
			System.out.println(gson.toJson(JsonParser.parseString(strJsonReq)));
			writeToFile(gson.toJson(JsonParser.parseString(strJsonReq.toString())));

			PrintWriter out = response.getWriter();
			// Validating all required fields are present
			if (!(jsonReq.has("VANum") && 
//			      jsonReq.has("remitterAc") && 
//			      jsonReq.has("remiterName") && 
//			      jsonReq.has("remitterAcType") && 
//			      jsonReq.has("remitterBankifsc") && 
			      jsonReq.has("txnAmt") && 
//			      jsonReq.has("remitterBranch") && 
//			      jsonReq.has("remitterBank")&&
			      jsonReq.has("bankRef"))) {
			    
			    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			    String res = "{\n" + 
			        " \"message\":\"VANum, txnAmt & bankRef are required fields\"\n" + 
			        "}";
			    String encryRes = idfcUtility.encrypt(res, secretHexaKey);
			    out.println(encryRes);
			    out.close();
			    System.out.println("=============================================================================");
			    return;
			}
			
			//Validating the paramenters value should be provided
			// Validate that none of the fields are null or empty
			String[] requiredFields = { "VANum","txnAmt","bankRef" };

			for (String field : requiredFields) {
				String value = jsonReq.get(field).getAsString();
			    if (value.trim().isEmpty()) {
			        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			        String res = "{\n" + 
			            " \"message\":\"" + field + " cannot be empty\"\n" + 
			            "}";
			        String encryRes = idfcUtility.encrypt(res, secretHexaKey);
			        out.println(res);
			        out.close();
			        System.out.println("=============================================================================");
			        return;
			    }
			}

			//Request parameters
			String vanNum = jsonReq.get("VANum").getAsString();
			String txnAmt = jsonReq.get("txnAmt").getAsString();
			String bankRef = jsonReq.get("bankRef").getAsString();
			
			//Existing Van
			ArrayList<String> vanNums = new ArrayList<String>();
			vanNums.add("WOWPEZVAN001");
			vanNums.add("WOWPEZVAN002"); 
			vanNums.add("WOWPEZVAN003");
			vanNums.add("WOWPEZVAN004");
			vanNums.add("WOWPEZVAN005");   //Expired
			vanNums.add("WOWPEZVAN006");   //Updated
			
			//Expired VAN
			ArrayList<String> expVanNums = new ArrayList<String>();
			expVanNums.add("ESCROZVAN005"); 
			
			//Expired VAN
			ArrayList<String> updVanNums = new ArrayList<String>();
			updVanNums.add("ESCROZVAN006"); 
			
			// Validating the Virtual Account and Amount
	         if(!(vanNums.contains(vanNum)&&txnAmt.matches(".*\\d*\\.\\d+.*"))) {
	        	 String res = getResponsePayload(vanNum, bankRef, "F", "Failed", "02", "VA_Number_does_not_exist_or_amount_invalid");
	        	 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	        	 out.println(idfcUtility.encrypt(res, secretHexaKey));
	        	 out.close();
	        	 System.out.println("=============================================================================");
				 return;
	         }
	         
	         //Validating for Expired Van
	         if(expVanNums.contains(vanNum)) {
	        	 String res = getResponsePayload(vanNum, bankRef, "F", "Failed", "05", "Expired");
	        	 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	        	 out.println(idfcUtility.encrypt(res, secretHexaKey));
	        	 out.close();
	        	 System.out.println("=============================================================================");
				 return;
	         }

	       //Validating for already updated VAN
	         if(updVanNums.contains(vanNum)) {
	        	 String res = getResponsePayload(vanNum, bankRef, "F", "Failed", "06", "VA_Number_Already_Updated");
	        	 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	        	 out.println(idfcUtility.encrypt(res, secretHexaKey));
	        	 out.close();
	        	 System.out.println("=============================================================================");
				 return;
	         }
	         
			// Validating the Success request
	         if(vanNum.equals(vanNums.get(0))) {
	        	 String res = getResponsePayload(vanNum, bankRef, "000", "Success", "000", "Success");
	        	 response.setStatus(HttpServletResponse.SC_OK);
	        	 out.println(idfcUtility.encrypt(res, secretHexaKey));
	        	 out.close();
	        	 System.out.println("=============================================================================");
				 return;
	         }
	         
		}catch (IllegalArgumentException decryptionExp) {
			// TODO: handle exception
			PrintWriter out = response.getWriter();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			String res = "{\n" + " \"message\":\"Not a proper BASE65 encrypted data\"\n" + "}";
			out.println(res);
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			PrintWriter out = response.getWriter();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			String res = "{\n" + " \"message\":\"Internal error\"\n" + "}";
			out.println(res);
			out.close();
			e.printStackTrace();
		}
	}
	
	public static void writeToFile(String dataToLog) {

		String filePath = "D:\\Phedora\\Banks\\IDFC\\IDFC_VanValidation_callback_log.txt";

		File file = new File(filePath);
		boolean fileExists = file.exists();

		try {
			if (!fileExists) {
				file.createNewFile();
			}

			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
				writer.newLine(); // Move to the next line
				writer.write(dataToLog); // Append new data
			}

		} catch (IOException e) {
			System.err.println("An error occurred while writing to the file.");
			e.printStackTrace();
		}

	}

}
