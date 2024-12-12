package eazypayouts.webhooks;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import DataBase.EazypayoutLiveWebhookInsertToDB;

/**
 * Servlet implementation class LiveWebhook
 */
@WebServlet("/eazypayoutslive/webhook")
public class LiveWebhook extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public LiveWebhook() {
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
		//
		response.setContentType("application/json");
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

			Object jsonObject = objectMapper.readValue(requestBody.toString(), Object.class);
			String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
			JsonNode jsonNode = objectMapper.readTree(prettyJson);

			if (jsonNode.has("payout_ref")) {
				String payout_ref = jsonNode.path("payout_ref").asText();
				String bankref = jsonNode.path("bankref").asText();

				// Seprating the timestmap from payout ref for the payout timestamp
				String payoutTimeStamp = jsonNode.path("payout_ref").asText().substring(0, 14);
				DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
				DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				LocalDateTime payoutTime = LocalDateTime.parse(payoutTimeStamp, inputFormatter);
				String formattedPayoutTimeStamp = payoutTime.format(outputFormatter);

				// Fetching the webhook recived timestamp
				LocalDateTime webhookTimes = LocalDateTime.now();
				String webhookTimeStamp = webhookTimes.format(outputFormatter);

				String status = jsonNode.path("status").asText();

				String cr_timestamp = null;
				if (status.equals("processed")) {
					String bpmsg = jsonNode.path("additional_info").path("bp_msg").asText();
					cr_timestamp = bpmsg.split("on")[1].trim();
				} else {
					String bpmsg = jsonNode.path("additional_info").path("message").asText();
					cr_timestamp = bpmsg;
				}

				// To find the delay
				long delay = ChronoUnit.SECONDS.between(LocalDateTime.parse(formattedPayoutTimeStamp, outputFormatter),
						LocalDateTime.parse(webhookTimeStamp, outputFormatter));
				if (delay >= 3600) {
					EazypayoutLiveWebhookInsertToDB.insertPayoutRefToDelayTable(payout_ref,
							payoutTime.format(outputFormatter), webhookTimes.format(outputFormatter), delay);
				}

				// System.out.println(bpmsg);
				// System.out.println(currentTimestamp.format(outputFormatter));
				EazypayoutLiveWebhookInsertToDB.insertDataToDB(payout_ref, payoutTime.format(outputFormatter),
						webhookTimes.format(outputFormatter), bankref, delay, status, requestBody.toString(),
						cr_timestamp);
			}
			
			System.out.println(prettyJson);
			System.out.println("===============================================================");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
