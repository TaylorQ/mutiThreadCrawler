package multiThreadCrawler;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Test {

	public static void main(String[] args) {
		String url = "http://city.sina.com.cn/focus/t/2017-07-05/144460577.html";
		Document doc = null;
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.print(doc.select("meta[name]"));
		//System.out.print(doc);
	}

}
