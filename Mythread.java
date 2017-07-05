package multiThreadCrawler;

import java.io.IOException;
import java.sql.SQLException;

public class Mythread extends Thread {
	public void run(){
		Channeltemplate test = new Channeltemplate();
		try {
			test.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
