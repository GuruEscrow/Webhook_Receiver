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
 * Servlet implementation class CollectCallback
 */
@WebServlet("/collect/uat")
public class CollectCallback extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CollectCallback() {
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

	private static String getResponsePayload(String vanNum, String banRef, String status, String statusDesc,
			String errCode, String errDesc) {

		String jsonString = "{\n" +
			    "   \"corRefNo\":\""+vanNum+"\",\n" +
			    "   \"ReqrefNo\":\""+banRef+"\",\n" +
			    "   \"status\":\""+status+"\",\n" +
			    "   \"statusDesc\":\""+statusDesc+"\",\n" +
			    "   \"errorCode\":\""+errCode+"\",\n" +
			    "   \"errorDesc\":\""+errDesc+"\"\n" +
			    "}";

		return jsonString;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
			
//			String strJsonReq = idfcUtility.decrypt(encryptedRequest.toString(), secretHexaKey);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			
		    JsonObject jsonReq = JsonParser.parseString(encryptedRequest.toString()).getAsJsonObject();

			System.out.println("IDFC Collect Insta Alert");
			System.out.println(gson.toJson(JsonParser.parseString(encryptedRequest.toString())));
			writeToFile(gson.toJson(JsonParser.parseString(encryptedRequest.toString())));

			PrintWriter out = response.getWriter();
			// Validating all required fields are present
			if (!(jsonReq.has("customerCode") && 
			      jsonReq.has("customerName") && 
			      jsonReq.has("productCode") && 
//			      jsonReq.has("productDescription") && 
			      jsonReq.has("poolingAccountNumber") && 
//			      jsonReq.has("transactionType") && 
//			      jsonReq.has("dataKey") && 
			      jsonReq.has("batchAmt") && 
//			      jsonReq.has("batchCrAmtCcd") && 
//			      jsonReq.has("creditDate") && 
			      jsonReq.has("vaNumber") && 
			      jsonReq.has("utrNo") && 
			      jsonReq.has("creditGenerationTime")
//			      jsonReq.has("remitterName") && 
//			      jsonReq.has("remitterAccountNumber") && 
//			      jsonReq.has("remittingBankName") && 
//			      jsonReq.has("ifscCode")
			      )) {
			    
			    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			    String res = "{\n" + 
			        " \"message\":\"customerCode, customerName, productCode, poolingAccountNumber, batchAmt, vaNumber, utrNo & creditGenerationTime are required fields\"\n" + 
			        "}";
//			    String encryRes = idfcUtility.encrypt(res, secretHexaKey);
			    out.println(res);
			    out.close();
			    System.out.println("=============================================================================");
			    return;
			}
			
			//Validating the paramenters value should be provided
			// Validate that none of the fields are null or empty
			String[] requiredFields = { "customerCode", "customerName", "productCode", "poolingAccountNumber",
					"batchAmt", "vaNumber", "utrNo", "creditGenerationTime" };

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
			String vanNum = jsonReq.get("vaNumber").getAsString();
//			String txnAmt = jsonReq.get("utrNo").getAsString();
			String bankRef = jsonReq.get("utrNo").getAsString();
			
			//Existing Van
			ArrayList<String> vanNums = new ArrayList<String>();
			vanNums.add("WOWPEZVAN001");
			vanNums.add("WOWPEZVAN002"); 
			vanNums.add("WOWPEZVAN003");
			vanNums.add("WOWPEZVAN004");
	         
	         //Validating for Expired Van
	         if(vanNums.contains(vanNum)) {
	        	 String res = getResponsePayload(vanNum, bankRef, "000", "Success", "000", "Success");
	        	 response.setStatus(HttpServletResponse.SC_OK);
	        	 out.println(res);
	        	 out.close();
	        	 System.out.println("=============================================================================");
				 return;
	         }else {
	        	 String res = getResponsePayload(vanNum, bankRef, "F", "Failure", "1001", "VirtualAccountNumber provided does not exist");
	        	 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	        	 out.println(res);
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

		String filePath = "D:\\Phedora\\Banks\\IDFC\\IDFC_Collect_callback_log.txt";

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
