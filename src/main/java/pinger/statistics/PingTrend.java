package pinger.statistics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import pinger.logger.DatabaseDetails;
import pinger.logger.FileRepository;
import pinger.logger.MySQLRepository;
import pinger.logger.PingRepository;
import pinger.servlet.Ping;

public class PingTrend {

	private final PingRepository pingRepository;
	private final FileRepository pingFileRepository;

	public PingTrend(PingRepository pingRepository,
			FileRepository fileRepository) {
		this.pingRepository = pingRepository;
		this.pingFileRepository = fileRepository;
	}

	public void retrieve() {
		List<Ping> pings = pingRepository.readAll();

		for (Ping ping : pings) {
			pingFileRepository.write(ping);
		}
	}

	public static void main(String[] args) throws IOException,
			ClassNotFoundException, SQLException {
		if (args[0].equals("-f")) {
			FileWriter fileWriter = new FileWriter(new File(args[1]));

			String urlToDatabase;
			String dbUsername;
			String dbPassword;
			String ipToDatabase;
			String databaseName = "pingerpb";
			String tableName = "Ping_logs"; 
			
			Properties properties = new Properties();
			InputStream configFileAsStream = PingTrend.class.getClassLoader().getResourceAsStream("databaseConfig.properties"); 
			
			properties.load(configFileAsStream);
	 
			ipToDatabase  = properties.getProperty("db_ip");
	 
			dbUsername = properties.getProperty("db_username");
			dbPassword = properties.getProperty("db_password");
	 
			urlToDatabase = "jdbc:mysql://" + ipToDatabase + "/"+ databaseName;
	 
			DatabaseDetails databaseDetails = new DatabaseDetails(urlToDatabase, dbUsername, dbPassword, tableName);

			PingTrend pingTrend = new PingTrend(new MySQLRepository(databaseDetails), 
					new FileRepository(new BufferedWriter(fileWriter)));
			pingTrend.retrieve();
		}
	}
}