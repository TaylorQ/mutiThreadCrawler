package multiThreadCrawler;

import java.awt.List;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import redis.clients.jedis.Jedis;

public class PageDownloader{
	
	String[] url_list;//所有URL
	String[] data;//模版的所有数据
	
	public void execute(String channel_url, String[] url_list, String[] data) throws SQLException{
		this.url_list = url_list;
		this.data = data;
		int Failnum = 0;
		int id = 0;
		int tid = 0;
		String temp = null;
		for (String url : url_list){
			if (url.startsWith(channel_url)){
				System.out.println("pull out info from page:"+url);
				String[] content = getPageContent(url);
				if (!(content[0].equals("")&&content[1].equals("")&&content[2].equals("")&&content[3].equals("")&&content[4].equals("")))
				{
					if(content[0].equals("")||content[1].equals("")||content[2].equals("")||content[3].equals("")||content[4].equals(""))
					{
						 UpdateFailureCount(channel_url);
						 if(getFailureCount(channel_url) == 10)
						 {
							 UpdateStateFailtoDB(content,channel_url);
							 continue;
						 }
					}
					for(int i=  0;i<5;i++){
						System.out.println(content[i]);
					}
					storeToDB(content);
				}
				else{
					updateModuleFailtoDB();
				}
			}
		}
	}
	
	public String[] getPageContent(String url){//获取页面内容
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
		
		//基于模板的数据抽取，依次为标题、作者、发布时间、正文
	    String title_select = data[10].substring(0, data[10].indexOf("|"));
	    String title_get = data[10].substring(data[10].indexOf("|")+1);
		Elements content_title = doc.select(title_select);//title
		String content_title_text = "";
		if (title_get.startsWith("attr")){
			content_title_text = content_title.attr(title_get.substring(4));
		}else{
			content_title_text = content_title.text();
		}
		content[0] = content_title_text;
		System.out.println(content_title_text);
		
		String author_select = data[11].substring(0, data[11].indexOf("|"));
		String author_get = data[11].substring(data[11].indexOf("|")+1);
		Elements content_author = doc.select(author_select);//author
		System.out.println(content_author.size());
		System.out.println(author_select);
		String content_author_text = "";
		if (author_get.startsWith("attr")){
			content_author_text = content_author.attr(author_get.substring(4));
		}else{
			content_author_text = content_author.text();
		}
		content[1] = content_author_text;
		System.out.println(content_author_text);
		
		String pubtime_select = data[12].substring(0, data[12].indexOf("|"));
		String pubtime_get = data[12].substring(0, data[12].indexOf("|"));
		Elements content_pubtime = doc.select(pubtime_select);//pubtime
		String content_pubtime_text = "";
		if (pubtime_get.startsWith("attr")){
			content_pubtime_text = content_pubtime.attr(pubtime_get.substring(4));
		}else{
			content_pubtime_text = content_pubtime.text();
		}
		content[2] = content_pubtime_text;
		System.out.println(content_pubtime_text);
		
		String content_select = data[13].substring(0, data[13].indexOf("|"));
		String content_get = data[13].substring(0, data[13].indexOf("|"));
		Elements content_content = doc.select(content_select);//content
		String content_content_text = "";
		if (content_get.startsWith("attr")){
			content_content_text = content_content.attr(content_get.substring(4));
		}else{
			content_content_text = content_content.text();
		}
		content[3] = content_content_text;
		System.out.println(content_content_text);
		
		content[4] = url;
		
		return content;
	}
	
	public boolean storeToDB(String[] content) {//将抽取到的数据存入数据库
		/*DatabaseConnect database4 = new DatabaseConnect();
		database4.ConnectDb();
		String sql = "insert into content (title , author , pubtime , content , source)values('"+content[0]+"' ,'"+content[1]+"' , '"+content[2]+"' ,'"+content[3]+"' ,'"+content[4]+"')";
		System.out.println(sql);
		database4.stmt.execute(sql);
		database4.close();
		*/
		
		/*Jedis jedis;
		jedis = new Jedis("211.87.229.80",6379);
		System.out.println(jedis.ping());
		
		Map<String,String> map = new HashMap<String,String>();
		map.put("title", content[0]);
		map.put("author", content[1]);
		map.put("pubtime", content[2]);
		map.put("content", content[3]);
		//map.put("website", value);
		jedis.hmset(content[5], map);
		
		List rsmap = (List) jedis.hmget(content[5], "title", "author", "pubtime","content");
        System.out.println(rsmap);  
		System.out.println("save sucess");
		return true;*/
		
		Connection con;
		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc : mysql : //localhost:3306/crawler";
		String user = "ryan";
		String password = "1234";
		try{
			Class.forName(driver);
			con = DriverManager.getConnection(url,user,password);
			if(!con.isClosed()){
				System.out.println("Succeeded connecting to the Database");
			}
			Statement statement = con.createStatement();
			String sql  = "insert into website(id,tid,website_name,region,country,language,channel_name,status,title,content,pubtime,author,source,crawler_time,url,update_time)"
					+ " values (id,tid,a[0],a[1],a[2],a[3],a[4],a[9],a[10],a[13],a[12],a[11],a[14],a[7],a[5],a[8]) ";
			ResultSet rs = statement.executeQuery(sql);
			
			
		
		} 
		catch(ClassNotFoundException e) {   
		     System.out.println("Sorry,can`t find the Driver!");   
	         e.printStackTrace();   
		}
		catch(SQLException e) {
	         e.printStackTrace();  
		}
		catch (Exception e) {
		     e.printStackTrace();
		}
		
		return true;
	}
	
	public void updateModuleFailtoDB(){
		System.out.println("未能爬取到数据");
	}
	
	public int getFailureCount(String url) throws SQLException{//获取当前模板的失效次数
		DatabaseConnect database7 = new DatabaseConnect();
		database7.ConnectDb();
		String sql = "select * from model where channel_url = '"+url+"'";
		System.out.println(sql);
		database7.rs = database7.stmt.executeQuery(sql);
		ResultSet rs1 =database7.rs; 
		rs1.next();
		int failurecount = rs1.getInt(18);
		database7.close();
		return failurecount;
	}
	
	public void UpdateFailureCount(String url) throws SQLException{//更新当前模板失效次数
		DatabaseConnect database6 = new DatabaseConnect();
		database6.ConnectDb();
		String sql = "update model set failure_count = failure_count+1 where channel_url = '"+url+"'";
		System.out.println(sql);
		database6.stmt.execute(sql);
		database6.close();
	}

	public void UpdateStateFailtoDB(String[] content , String url) throws SQLException{//更新模板的异常状态
		String insertstring = "异常";
		if(content[0].equals("")){
			insertstring = insertstring+",title异常" ;
		}
		if(content[1].equals("")){
			insertstring = insertstring+",author异常" ;
		}
		if(content[2].equals("")){
			insertstring = insertstring+",pubtime异常" ;
		}
		if(content[3].equals("")){
			insertstring = insertstring+",content异常" ;
		}
		if(content[4].equals("")){
			insertstring = insertstring+",source异常" ;
		}
		DatabaseConnect database5 = new DatabaseConnect();
		database5.ConnectDb();
		String sql = "update model set status = '"+insertstring+"' where channel_url = '"+url+"'";
		database5.stmt.execute(sql);
		database5.close();
	}
}
