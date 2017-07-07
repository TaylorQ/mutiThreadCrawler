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
	

	final int int_id               = 0;
	final int int_website_name     = 1;
	final int int_region           = 2;
	final int int_country          = 3;
	final int int_language         = 4;
	final int int_channel_name     = 5;
	final int int_channel_url      = 6;
	final int int_channel_next_url = 7;
	final int int_url_xpath        = 8;
	final int int_start_time       = 9;
	final int int_stop_time        = 10;
	final int int_status           = 11;
	final int int_title_xpath      = 12;
	final int int_author_xpath     = 13;
	final int int_pubtime_xpath    = 14;
	final int int_content_xpath    = 15;
	final int int_source_xpath      = 16;
	
	public void execute(String[] url_list, String[] template) throws SQLException{
		this.url_list = url_list;
		this.template = template;
		int Failnum = getFailureCount(template[int_channel_url]);
		DatabaseConnect database9 = new DatabaseConnect();
		database9.ConnectDb();
		for (String url : url_list){
			if (url.startsWith(template[int_channel_url])&&url.endsWith("html")
					&&!database9.stmt.executeQuery("select * from url where url = '"+url+"'").first()){
				System.out.println("pull out info from page:"+url);
				String[] content = getPageContent(url);
				if (!(content[0].equals("")&&content[1].equals("")&&content[2].equals("")&&content[3].equals("")&&content[4].equals("")))
				{
					if(content[0].equals("")||content[1].equals("")||content[2].equals("")||content[3].equals("")||content[4].equals(""))
					{
						Failnum++;
						if(Failnum >= 10){
							break;
						}
					}
					storeToDB(content);
				}else{
					System.out.println("Fail to craw this page.");
				}
				String sql = "insert into url (tid,url) values ('"+template[int_id]+"','"+url+"')";
				database9.stmt.executeUpdate(sql);
			}			
		}
		database9.close();
		if (Failnum < 10){
			UpdateFailureCount(template[int_channel_url],Failnum);
		}else{
			UpdateFailureCount(template[int_channel_url],0);
			UpdateStateFailtoDB(template[int_channel_url]);
		}
	}
	
	public String[] getPageContent(String url){//获取页面内容
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
			System.out.println(content_title_text);
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
			System.out.println(content_author_text);
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
			content[2] = content_pubtime_text;
			System.out.println(content_pubtime_text);
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
			System.out.println(content_content_text);
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
			System.out.println(content_source_text);
		}else{
			content[4] = "";
		}
		
		content[5] = getCurrentTime();
		
		content[6] = url;
		
		return content;
	}
	
	public boolean storeToDB(String[] content) throws SQLException {
		DatabaseConnect database8 = new DatabaseConnect();
		database8.ConnectDb();
		String sql = "insert into website(tid,website_name,region,country,language,channel_name,status,"
				+ "title,content,pubtime,author,source,crawler_time,url,update_time)"
				+ " values ('"+template[int_id]+"','"+template[int_website_name]+"','"+template[int_region]
				+ "','"+template[int_country]+"','"+template[int_language]+"','"+template[int_channel_name]
				+ "','"+template[int_status]
				+ "','"+content[0]+"','"+content[1]+"','"+content[2]+"','"+content[3]+"','"+content[4]
				+ "','"+content[5]+"','"+content[6]+"','"+getCurrentTime()+"')";
		System.out.println(sql);
		database8.stmt.executeUpdate(sql);
		database8.close();
		
		return true;
	}
	
	public void UpdateFailureCount(String url, int failnum) throws SQLException{//更新当前模板失效次数
		DatabaseConnect database6 = new DatabaseConnect();
		database6.ConnectDb();
		String sql = "update template set failure_count = "+failnum+" where channel_url = '"+url+"'";
		System.out.println(sql);
		database6.stmt.execute(sql);
		database6.close();
	}
	
	public int getFailureCount(String url) throws SQLException{//获取当前模板的失效次数
		DatabaseConnect database7 = new DatabaseConnect();
		database7.ConnectDb();
		String sql = "select * from template where channel_url = '"+url+"'";
		System.out.println(sql);
		database7.rs = database7.stmt.executeQuery(sql);
		ResultSet rs1 =database7.rs; 
		rs1.next();
		int failurecount = rs1.getInt(19);
		database7.close();
		return failurecount;
	}

	public void UpdateStateFailtoDB(String url) throws SQLException{//更新模板的异常状态
		DatabaseConnect database5 = new DatabaseConnect();
		database5.ConnectDb();
		String sql = "update template set status = '异常' where channel_url = '"+url+"'";
		database5.stmt.execute(sql);
		database5.close();
	}
	
	public String getCurrentTime(){//获取当前时间
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(new Date());
	}
}
