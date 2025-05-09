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
 * Servlet implementation class VanCollect
 */
@WebServlet("/collect/uat")
public class VanCollect extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public VanCollect() {
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

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		StringBuilder callBack = new StringBuilder();
		BufferedReader reader = request.getReader();
		String line;
		while ((line = reader.readLine()) != null) {
			callBack.append(line);
		}
		reader.close();
		
		System.out.println("plain Callback --> "+callBack);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		System.out.println("\nNSDL collect callBack --> "+gson.toJson(JsonParser.parseString(callBack.toString())));
		
		writeToFile("\nNSDL collect callBack --> "+gson.toJson(JsonParser.parseString(callBack.toString())));
		
		JsonObject jsonReq = JsonParser.parseString(callBack.toString()).getAsJsonObject();
		
		PrintWriter out = response.getWriter();
		// Validating all required fields are present
		String[] requiredFields = { 
			    "channelid", 
			    "appid", 
			    "partnerid", 
			    "token", 
			    "signcs", 
			    "virtualcorpid", 
			    "trantype", 
			    "reqbytype", 
			    "txnRefNo", 
			    "txnAmount", 
			    "debitoraccountno", 
//			    "debitoraccountname", 
//			    "debitorbankname", 
//			    "debitorifsc", 
			    "virtualaccountno", 
			    "txnmode" 
			};

			for (String field : requiredFields) {
			    if (!jsonReq.has(field) || jsonReq.get(field).isJsonNull()) {
			        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			        String res = "{\n" + 
			        	"  \"respmessage\":\"FAILURE\",\n" +
		                "  \"respcode\":\"99\"\n" +
			            "  \"message\": \"" + field + " is required\"\n" + 
			        		
			            "}";
			        out.println(res);
			        out.close();
			        System.out.println("=============================================================================");
			        return;
			    }

			    //Validating t
			    String value = jsonReq.get(field).getAsString();
			    if (value.trim().isEmpty()) {
			        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			        String res = "{\n" + 
			            "  \"respmessage\":\"FAILURE\",\n" +
			            "  \"respcode\":\"99\"\n" +
			            "  \"message\": \"" + field + " cannot be empty\"\n" + 
			            "}";
			        out.println(res);
			        out.close();
			        System.out.println("=============================================================================");
			        return;
			    }
			}
			
			
			
			//Request parameters
			String vanNum = jsonReq.get("virtualaccountno").getAsString();
//			String txnAmt = jsonReq.get("utrNo").getAsString();
			String txnRefNo = jsonReq.get("txnRefNo").getAsString();
			String virtualCorpid = jsonReq.get("virtualcorpid").getAsString();
			
			//Existing Van
			ArrayList<String> vanNums = new ArrayList<String>();
			vanNums.add("ESTA0001");
			vanNums.add("ESTA0002");
			vanNums.add("ESTA0003");
			
			//Validating for Expired Van
	         if(vanNums.contains(vanNum)) {
	        	 String res = "{\n" +
	                     "\"virtualcorpid\":\""+virtualCorpid+"\",\n" +
	                     "\"respmessage\":\"SUCCESSFUL\",\n" +
	                     "\"respcode\":\"00\"\n" +
	                     "}";
	        	 response.setStatus(HttpServletResponse.SC_OK);
	        	 out.println(res);
	        	 out.close();
	        	 System.out.println("=============================================================================");
				 return;
	         }else {
	        	 String res = "{\n" +
	                     "\"virtualcorpid\":\""+virtualCorpid+"\",\n" +
	                     "\"respmessage\":\"FAILURE\",\n" +
	                     "\"respcode\":\"99\"\n" +
	                     "}";
	        	 response.setStatus(HttpServletResponse.SC_OK);
	        	 out.println(res);
	        	 out.close();
	        	 System.out.println("=============================================================================");
				 return;
	         }
	}
	
	public static void writeToFile(String dataToLog) {

		String filePath = "D:\\Phedora\\Banks\\NSDL\\nsdl_VanCollectLog.txt";

		File file = new File(filePath);
		boolean fileExists = file.exists();

		try {
			if (!fileExists) {
				file.createNewFile();
				System.out.println("File created: " + filePath);
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
