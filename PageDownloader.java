package multiThreadCrawler;

import java.awt.List;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class PageDownloader{
	
	String[] url_list;//所有URL
	String[] template;//模版的所有数据
	

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
	
	private int id;
	private static Logger infologger = Logger.getLogger("infoLogger");
	private static Logger errorlogger = Logger.getLogger("errorLogger");
	
	public PageDownloader(int id){
		this.id = id;
	}
	
	public int execute(String[] url_list, String[] template){
		this.url_list = url_list;
		this.template = template;
		int craw_count = 0;
		int Failnum = Integer.parseInt(template[int_fail_count]);
		DatabaseConnect database = new DatabaseConnect();
		database.ConnectDb();
		try{
			for (String url : url_list){
				if (url.startsWith("http")&&(url.endsWith("html")||url.endsWith("htm")||(url.charAt(url.length()-1)<='9'&&url.charAt(url.length()-1)>='0')))
						if(!database.stmt.executeQuery("select * from url where url = '"+url+"'").first()){
							infologger.info("[cp"+id+"]pull out info from page:"+url);
							String[] content = getPageContent(url);
							if (!(content[1].equals("")&&content[2].equals("")&&content[3].equals("")&&content[4].equals("")))
							{
								if(content[0].equals("")||content[1].equals("")||content[2].equals("")||content[3].equals("")||content[4].equals(""))
								{
									Failnum++;
									if(Failnum >= 10){
										break;
									}
								}
								storeToDB(content);
								craw_count++;
							}else{
								infologger.info("[cp"+id+"]Fail to craw:"+url);
							}
							String sql = "insert into url (tid,url) values ('"+template[int_id]+"','"+url+"')";
							database.stmt.executeUpdate(sql);
						}			
			}
		}catch(SQLException e){
			errorlogger.error("Exception occur when executing pagedownloader.");
		}
		if (Failnum < 10){
			UpdateFailureCount(template[int_channel_url],Failnum);
		}else{
			UpdateFailureCount(template[int_channel_url],0);
			UpdateStateFailtoDB(template[int_channel_url]);
		}
		database.close();
		return craw_count;
	}
	
	private void UpdateStateFailtoDB(String url) {
		DatabaseConnect database = new DatabaseConnect();
		database.ConnectDb();
		String sql = "update template set status = '异常' where channel_url = '"+url+"'";
		try {
			database.stmt.executeUpdate(sql);
		} catch (SQLException e) {
			errorlogger.error("Exception occur when setting status to DB.");
		}
		database.close();
	}

	private void UpdateFailureCount(String url, int failnum) {
		DatabaseConnect database = new DatabaseConnect();
		database.ConnectDb();
		String sql = "update template set failure_count = '"+failnum+"' where channel_url = '"+url+"'";
		try {
			database.stmt.executeUpdate(sql);
		} catch (SQLException e) {
			errorlogger.error("Exception occur when setting failnum to DB.");
		}
		database.close();		
	}

	private String[] getPageContent(String url){//获取页面内容
		String[] content = new String[7];//用于存储内容的数组
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
			content[0] = content_title_text;
			System.out.println("[cp"+id+"]"+content_title_text);
		}else{
			content[0] = "";
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
			content[3] = content_author_text;
			System.out.println("[cp"+id+"]"+content_author_text);
		}else{
			content[3] = "";
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
			content[2] = content_pubtime_text
					.replaceAll("年", "-")
					.replaceAll("月", "-")
					.replaceAll("日", "");
			System.out.println("[cp"+id+"]"+content_pubtime_text);
		}else{
			content[2] = "";
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
			content[1] = content_content_text;
			System.out.println("[cp"+id+"]"+content_content_text);
		}else{
			content[1] = "";
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
			content[4] = content_source_text;
			System.out.println("[cp"+id+"]"+content_source_text);
		}else{
			content[4] = "";
		}
		
		content[5] = getCurrentTime();
		
		content[6] = url;
		
		return content;
	}
	
	public boolean storeToDB(String[] content){
		DatabaseConnect database = new DatabaseConnect();
		database.ConnectDb();
		for (int i = 0;i < 5;i++){
			content[i].replaceAll("\\(", "");
			content[i].replaceAll("\\)", "");
		}
		String sql = "insert into website(tid,website_name,region,country,language,channel_name,status,"
				+ "title,content,pubtime,author,source,crawler_time,url,update_time)"
				+ " values ('"+template[int_id]+"','"+template[int_website_name]+"','"+template[int_region]
				+ "','"+template[int_country]+"','"+template[int_language]+"','"+template[int_channel_name]
				+ "','"+template[int_status]
				+ "','"+content[0]+"','"+content[1]+"','"+content[2]+"','"+content[3]+"','"+content[4]
				+ "','"+content[5]+"','"+content[6]+"','"+getCurrentTime()+"')";
		try {
			database.stmt.execute(sql);
		} catch (SQLException e) {
			errorlogger.error("Exception occur when inserting data to DB.");
		}
		database.close();
		return true;
	}
	
	public String getCurrentTime(){//获取当前时间
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(new Date());
	}
}
