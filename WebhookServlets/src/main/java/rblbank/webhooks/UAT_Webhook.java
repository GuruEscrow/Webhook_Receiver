package rblbank.webhooks;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Servlet implementation class UAT_Webhook
 */
@WebServlet("/upi_intent/uat")
public class UAT_Webhook extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private String decryptionKey = "pa8kwajfiv0zsxymoyving88rms9n8o0pj2dt6r829qdhgv679qnh0jyr2mo4fl1";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UAT_Webhook() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		 // Read the XML content from the request input stream
        InputStream inputStream = request.getInputStream();
        
//      Create a new document builder to conver the XML string to XML format
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		Document doc = null;
        // Parse the incoming XML request to a Document object
        try {
		doc	 = builder.parse(inputStream);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//      Creating the instance for the DecryptionUtil to get the decrypt method acces
        System.out.println("Encrypted call back: "+doc.getElementsByTagName("data").item(0).getTextContent());
        DecryptionUtil decrypt = null;
		try {
			decrypt = new DecryptionUtil();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Calling the decrypt the encrypted data of RBL call back
        try {
			String decryptedData = decrypt.decrypt(doc.getElementsByTagName("data").item(0).getTextContent(), decryptionKey);
			System.out.println("Decrypted Data: "+decryptedData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		String responseBody = "<UPI_PUSH_Response xmlns=\"http://rssoftware.com/callbackadapter/domain/\">"
				+             "<statuscode>0<statuscode/>"
				+             "<description>ACK Success<description/>"
				+             "<UPI_PUSH_Response/> ";
		
		/*
		 * Returning the response to API in xml content type
		 * First setting up the content type
		 * and Setting up character Encoding that enscure that text encoded
		 * Creating the Print writer variable to write the response and return the response by using flush
		 */
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/xml");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		out.write(responseBody);
		out.flush();
		
		
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

}
