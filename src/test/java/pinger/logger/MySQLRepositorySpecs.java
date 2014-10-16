package pinger.logger;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import pinger.servlet.Ping;

public class MySQLRepositorySpecs {

	static final String dbDriverName = "com.mysql.jdbc.Driver";
	static String urlToDatabase;
	static DatabaseDetails databaseDetails;
	static Connection connection;
	
	@BeforeClass
	public static void setupDatabase() throws ClassNotFoundException, SQLException {
		Connection dbSetupConnection;
		String tableName = "testTable";
		String databaseName = "testdb";
		
		Properties prop = new Properties();
        try {
			prop.load(new FileInputStream("webContent/WEB-INF/databaseConfig.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}

        String ipToDatabase  = prop.getProperty("db_ip");
        String dbUsername = prop.getProperty("db_username");
        String dbPassword = prop.getProperty("db_password");
        String url =  "jdbc:mysql://" + ipToDatabase;

		
		
		Class.forName(dbDriverName);
		dbSetupConnection = DriverManager.getConnection(url, dbUsername, dbPassword);
		Statement statement = dbSetupConnection.createStatement();
		
		statement.executeUpdate("drop database if exists " + databaseName + ";");		
		statement.executeUpdate("create database " + databaseName + ";");		
		statement.execute("use " + databaseName +";");
		statement.execute("drop table if exists " + tableName + ";");
		statement.executeUpdate("create table " + tableName + "(dummyString varchar(20), dummyValue bigint);");		
		dbSetupConnection.close();

		
		urlToDatabase = "jdbc:mysql://" + ipToDatabase+"/"+databaseName;
		databaseDetails = new DatabaseDetails(urlToDatabase, dbUsername, dbPassword, tableName );
		connection = DriverManager.getConnection(urlToDatabase, dbUsername, dbPassword);
	}
	
	@AfterClass
	public static void tearDownDatabase() throws SQLException{
	}
	
	@Test
	public void itShouldWriteToMySQLDatabase() throws ClassNotFoundException, SQLException, IOException {
		// Given
		PingRepository mySQLRepositoryWriter = new MySQLRepository(databaseDetails);
		
		// When
		mySQLRepositoryWriter.write(new Ping("Fri Oct 12 11 00 00", 12345));

		// Then
		String expected = getExpected();

		Assert.assertEquals(expected, "Fri Oct 12 11 00 00:12345");

	}
	
	
	private String getExpected() {
		ResultSet resultSet;
		String returnString = "";
		try {
			Class.forName(dbDriverName);
			Statement statement = connection.createStatement();
			resultSet = statement.executeQuery("select * from " +databaseDetails.getTableName());

			resultSet.last();

			returnString = (resultSet.getString(1) + ":" + resultSet.getString(2));

		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return returnString;
	}
	
	
	@Test
	public void itShouldReadFromMySQLDatabase() throws ClassNotFoundException, SQLException {
		//given
		PingRepository mySQLRepositoryReader = new MySQLRepository(databaseDetails);

		writeDummyValuesToTestDb();
		
		//when
		List<Ping> pings = mySQLRepositoryReader.readAll();
		
		//then
		assertThat(pings, hasItems(new Ping("dummyTimeOne",1),new Ping("dummyTimeTwo",2)));	
	}
	
	
	private void writeDummyValuesToTestDb() {
		try {
			Class.forName(dbDriverName);
			
			PreparedStatement preparedStatementOne = connection
					.prepareStatement("insert into testTable values (?, ?)");
			preparedStatementOne.setString(1, "dummyTimeOne");
			preparedStatementOne.setString(2,"1");
			preparedStatementOne.executeUpdate();

			PreparedStatement preparedStatementTwo = connection
					.prepareStatement("insert into testTable values (?, ?)");
			preparedStatementTwo.setString(1, "dummyTimeTwo");
			preparedStatementTwo.setString(2,"2");
			preparedStatementTwo.executeUpdate();

		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
