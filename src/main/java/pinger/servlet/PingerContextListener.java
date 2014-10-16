package pinger.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import pinger.logger.DatabaseDetails;
import pinger.logger.MySQLRepository;

public class PingerContextListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {

		MySQLRepository repositoryWriter;

		ServletContext servletContext = servletContextEvent.getServletContext();

		try {
			DatabaseDetails mySQLDetails = readPropertiesFile(servletContext);
			repositoryWriter = new MySQLRepository(mySQLDetails);
			servletContext.setAttribute("repositoryWriter", repositoryWriter);

		} catch (IOException | ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	private DatabaseDetails readPropertiesFile(ServletContext servletContext) throws IOException,
			FileNotFoundException {
		String databaseName = "pingerpb";
		String tableName = "Ping_logs";

		String urlToDatabase;
		String dbUsername;
		String dbPassword;
		String ipToDatabase;

		Properties properties = new Properties();
		properties.load(servletContext.getResourceAsStream("/WEB-INF/databaseConfig.properties"));	
		
		ipToDatabase = properties.getProperty("db_ip");
		dbUsername = properties.getProperty("db_username");
		dbPassword = properties.getProperty("db_password");
		System.out.println("PingerContextListener.readPropertiesFile() : " +  properties);

		urlToDatabase = "jdbc:mysql://" + ipToDatabase + "/" + databaseName;

		DatabaseDetails mySQLDetails = new DatabaseDetails(urlToDatabase,
				dbUsername, dbPassword, tableName);
		return mySQLDetails;
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		System.out.println("Context Destroyed");
	}
}
