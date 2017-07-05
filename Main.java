package multiThreadCrawler;

import java.io.IOException;
import java.sql.SQLException;

public class Main {

	public static void main(String[] args) throws SQLException, IOException {
		// TODO Auto-generated method stub
		Mythread thread1 = new Mythread();
		thread1.start();
		Mythread thread2 = new Mythread();
		thread2.start();
	}
}
