package multiThreadCrawler;

import java.io.IOException;
import java.sql.SQLException;

public class Mythread {
	public void run() throws SQLException, IOException{
		Channeltemplate test = new Channeltemplate();
		test.execute();
	}
}
