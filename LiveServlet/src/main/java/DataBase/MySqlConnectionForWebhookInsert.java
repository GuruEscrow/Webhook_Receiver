package DataBase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySqlConnectionForWebhookInsert {

	public static void insertDataToDB(String payout_ref, String payout_timestamp, String webhook_timestamp,
			String bank_ref, long delay, String status, String webhook,String cr_timestamp) throws SQLException, ClassNotFoundException {
		// JDBC URL, username, and password of the MySQL database
		String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/payouts?useSSL=false";
		String username = "Guruprasad";
		String password = "MySql@#123";
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		// Establishing the connection
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			connection = DriverManager.getConnection(jdbcUrl, username, password);
			// System.out.println("Connected to the database!");
			String query = "INSERT INTO payouts.payout_webhooks (`payout_ref`, `payout_timestamp`, `webhook_timestamp`, `bankref`, `webhook_delay_in_seconds`, `status`, `webhook`,`cr_ben_timestamp`)"
					+ "VALUES ('" + payout_ref + "', '" + payout_timestamp + "', '" + webhook_timestamp + "', '"
					+ bank_ref + "', '" + delay + "', '" + status + "', '" + webhook + "', '" + cr_timestamp + "')";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.executeUpdate();

		} catch (SQLException e) {
			System.err.println("Connection failed. Error: " + e.getMessage());
		} finally {
			if (resultSet != null)
				resultSet.close();
			if (resultSet != null)
				preparedStatement.close();
			if (connection != null)
				connection.close();
		}
	}

	// This method to insert the payout_ref those txn received teh webhooks after 1
	// hr
	public static void insertPayoutRefToDelayTable(String payout_ref, String payout_timestamp, String webhook_timestamp,
			long delay) throws ClassNotFoundException, SQLException {
		// JDBC URL, username, and password of the MySQL database
		String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/payouts?useSSL=false";
		String username = "Guruprasad";
		String password = "MySql@#123";
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		// Establishing the connection
		// Establishing the connection
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			connection = DriverManager.getConnection(jdbcUrl, username, password);
			// System.out.println("Connected to the database!");
			String query = "INSERT INTO payouts.webhooks_delayed_1hr (`payout_ref`,`payout_timestamp`,`webhook_timestamp`,`delay`)"
					+ "VALUES ('" + payout_ref + "','" + payout_timestamp + "','" + webhook_timestamp + "','" + delay
					+ "')";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.executeUpdate();

		} catch (SQLException e) {
			System.err.println("Connection failed. Error: " + e.getMessage());
		} finally {
			if (resultSet != null)
				resultSet.close();
			if (resultSet != null)
				preparedStatement.close();
			if (connection != null)
				connection.close();
		}
	}

}
