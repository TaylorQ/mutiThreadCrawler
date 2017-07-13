package multiThreadCrawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class ModelTest {
	
	final static int int_id               = 0;
	final static int int_website_name     = 1;
	final static int int_region           = 2;
	final static int int_country          = 3;
	final static int int_language         = 4;
	final static int int_channel_name     = 5;
	final static int int_channel_url      = 6;
	final static int int_channel_next_url = 7;
	final static int int_url_xpath        = 8;
	final static int int_start_time       = 9;
	final static int int_stop_time        = 10;
	final static int int_status           = 11;
	final static int int_title_xpath      = 12;
	final static int int_author_xpath     = 13;
	final static int int_pubtime_xpath    = 14;
	final static int int_content_xpath    = 15;
	final static int int_source_xpath     = 16;
	final static int int_fail_count       = 17;
	
	final int id = 0;
	
	public static void main(String[] args) throws IOException {
		ModelTest mt = new ModelTest();
		String[][] templates = mt.getdatafromDB();
		for (int i = 0;i < templates.length;i++){
			mt.getPageContent(templates[i][int_channel_next_url], templates[i]);
		}
	}
	
	public String[][] getdatafromDB(){ //从模板库获取模板
		String[][] templates = null;
		DatabaseConnect database = new DatabaseConnect();
		database.ConnectDb();
		try{
			ResultSet rs1 = database.stmt.executeQuery("select * from template where status = '正常'");
			rs1.last();
			templates = new String[rs1.getRow()][18];
			rs1.beforeFirst();
			//开始顺序获取模板
			int i = 0;
			while(rs1.next()){
				String[] template = new String[18];
				template[0] = rs1.getString(1); //id
				template[1] = rs1.getString(2); //站点名
				template[2] = rs1.getString(3); //境内外
				template[3] = rs1.getString(4); //国家
				template[4] = rs1.getString(5); //语言
				template[5] = rs1.getString(6); //频道名
				template[6] = rs1.getString(7); //频道url
				template[7] = rs1.getString(8); //频道下一页url
				template[8] = rs1.getString(9); //url的xpath
				template[9] = rs1.getString(10).substring(0, 19); //开始时间
				template[10] = rs1.getString(11).substring(0, 19); //结束时间
				template[11] = rs1.getString(12); //状态
				template[12] = rs1.getString(13); //标题Xpath
				template[13] = rs1.getString(14); //作者path
				template[14] = rs1.getString(15); //发布时间Xpath
				template[15] = rs1.getString(16); //正文Xpath
				template[16] = rs1.getString(17); //来源Xpath
				template[17] = rs1.getString(19); //失效次数
				templates[i] = template;
				i++;
			}
		}catch(SQLException e){
			System.out.println("Exception occur when getting template from DB.");
		}
		database.close();
		return templates;
	}
	
	public String[] getPageContent(String url, String[] template){//获取页面内容
		String print = template[0];
		boolean fail = false;
		String[] content = new String[5];//用于存储内容的数组
		Document doc = null;
		try {
			doc = Jsoup.connect(url).get();//jsoup连接
		} catch (IOException e) {
			for(int i = 0;i<5;i++){//连接失败，返回空数组
				content[i] = "";
			}
			return content;
		}
		
		//基于模板的数据抽取，依次为标题、作者、发布时间、正文、转发来源
	    String title_select = template[int_title_xpath].substring(0, template[int_title_xpath].indexOf("|"));
	    String title_get = template[int_title_xpath].substring(template[int_title_xpath].indexOf("|")+1);
		Element content_title = doc.select(title_select).first();//title
		if (content_title != null){
			String content_title_text = "";
			if (title_get.startsWith("attr")){
				content_title_text = content_title.attr(title_get.substring(4));
			}else{
				content_title_text = content_title.text();
			}
			if (content_title_text.equals("")){
				fail = true;
				print+="title";
			}
		}else{
			fail = true;
			print+="title";
		}
		
		String author_select = template[int_author_xpath].substring(0, template[int_author_xpath].indexOf("|"));
		String author_get = template[int_author_xpath].substring(template[int_author_xpath].indexOf("|")+1);
		Element content_author = doc.select(author_select).first();//author
		if (content_author != null){
			String content_author_text = "";
			if (author_get.startsWith("attr")){
				content_author_text = content_author.attr(author_get.substring(4));
			}else{
				content_author_text = content_author.text();
			}
			if (content_author_text.equals("")){
				fail = true;
				print+="author";
			}
		}else{
			fail = true;
			print+="author";
		}
		
		String pubtime_select = template[int_pubtime_xpath].substring(0, template[int_pubtime_xpath].indexOf("|"));
		String pubtime_get = template[int_pubtime_xpath].substring(template[int_pubtime_xpath].indexOf("|")+1);
		Elements content_pubtime = doc.select(pubtime_select);//pubtime
		if (content_pubtime != null){
			String content_pubtime_text = "";
			if (pubtime_get.startsWith("attr")){
				content_pubtime_text = content_pubtime.attr(pubtime_get.substring(4));
			}else{
				content_pubtime_text = content_pubtime.text();
			}
			if (content_pubtime_text.equals("")){
				fail = true;
				print+="pubtime";
			}
		}else{
			fail = true;
			print+="pubtime";
		}
		
		String content_select = template[int_content_xpath].substring(0, template[int_content_xpath].indexOf("|"));
		String content_get = template[int_content_xpath].substring(template[int_content_xpath].indexOf("|")+1);
		Elements content_content = doc.select(content_select);//content
		if (content_content != null){
			String content_content_text = "";
			if (content_get.startsWith("attr")){
				content_content_text = content_content.attr(content_get.substring(4));
			}else{
				content_content_text = content_content.text();
			}
			if (content_content_text.equals("")){
				fail = true;
				print+="content";
			}
		}else{
			fail = true;
			print+="content";
		}

		String source_select = template[int_source_xpath].substring(0, template[int_source_xpath].indexOf("|"));
		String source_get = template[int_source_xpath].substring(template[int_source_xpath].indexOf("|")+1);
		Element content_source = doc.select(source_select).first();//source
		if (content_source != null){
			String content_source_text = "";
			if (source_get.startsWith("attr")){
				content_source_text = content_source.attr(source_get.substring(4));
			}else{
				content_source_text = content_source.text();
			}
			if (content_source_text.equals("")){
				fail = true;
				print+="source";
			}
		}else{
			fail = true;
			print+="source";
		}
		
		if (fail){
			System.out.println(print);
		}
		
		return content;
	}
}
