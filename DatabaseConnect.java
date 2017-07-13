package multiThreadCrawler;

import java.sql.*;
public class DatabaseConnect {
	String url = "jdbc:mysql://localhost:3306/crawler";
	String user = "taylor";
	String password = "19951202";
	Connection conn = null;
	Statement stmt = null;
	public void ConnectDb(){
		try
		{
		        Class.forName("com.mysql.jdbc.Driver"); 
		        conn = DriverManager.getConnection(url,user,password);
		        stmt = conn.createStatement();
		}
		catch (ClassNotFoundException e)
		{
		        System.out.println("FAIL..");
		}
		catch (SQLException e)
		{
				System.out.println("FAIL");
				e.printStackTrace();
		}	
	}
	
	public void close(){
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
