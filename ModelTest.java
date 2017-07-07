package multiThreadCrawler;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ModelTest {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String url = "http://www.toutiao.com/a6438280895622955265/";
		Document doc = Jsoup.connect(url).get();
		String m1 = "title|text";
		String m2 = "div[class=\"box01\"] div[class=\"f1\"]|text";
		String m3 = "div[class=\"box01\"] div[class=\"f1\"] a|text";
		String m4 = "div[id=\"rwb_zw\"] p|alltext";
		System.out.print(doc.select("a#comment_area").attr("href"));
	}
}
