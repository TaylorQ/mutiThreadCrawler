package multiThreadCrawler;

import java.sql.*;
public class DatabaseConnect {
	String url = "jdbc:mysql://localhost:3306/pachong_model?useUnicode=true&characterEncoding=UTF-8";
	String user = "root";
	String password = "XHF20209031";
	Connection conn = null;
	Statement stmt = null;
	ResultSet rs = null;
	public void ConnectDb(){
		try
		{
		        Class.forName("com.mysql.jdbc.Driver"); 
		        conn = DriverManager.getConnection(url,user,password);
		        stmt = conn.createStatement();
		}
		catch (ClassNotFoundException e)
		{
		        System.out.println("FAIL");
		}
		catch (SQLException e)
		{
				System.out.println("FAIL");
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
