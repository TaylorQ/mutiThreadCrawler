package multiThreadCrawler;

import java.awt.List;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import redis.clients.jedis.Jedis;

public class PageDownloader{
	
	String[] url_list;//所有URL
	String[] data;//模版的所有数据
	
	public void execute(String[] url_list,String[] data) throws SQLException{
		this.url_list = url_list;
		this.data = data;
		int Failnum = 0;
		for (String url : url_list){
			if (url.contains(".")&&url.startsWith("http")){
				System.out.println("pull out info from page:"+url);
				String[] content = getPageContent(url);
				if (!(content[0].equals("")&&content[1].equals("")&&content[2].equals("")&&content[3].equals("")&&content[4].equals("")))
				{
					if(content[0].equals("")||content[1].equals("")||content[2].equals("")||content[3].equals("")||content[4].equals(""))
					{
						Failnum++;
					}
					if(Failnum == 10){
						UpdateStateFailtoDB(content);
					}
					storeToDB(content);
				}
				else{
					updateModuleFailtoDB();
				}
			}
		}
	}
	
	public String[] getPageContent(String url){
		String[] content = new String[5];
		Document doc = null;
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			for(int i = 0;i<5;i++){
				content[i] = "";
			}
			return content;
		}
		
		String[] title_data = data[10].split("|");
		Elements content_title = doc.select(title_data[0]);//title
		String content_title_text = "";
		if (title_data[1].startsWith("attr")){
			content_title_text = content_title.attr(title_data[1].substring(4));
		}else{
			content_title_text = content_title.text();
		}
		content[0] = content_title_text;
		System.out.println(content_title_text);
		
		String[] author_data = data[10].split("|");
		Elements content_author = doc.select(author_data[0]);//author
		String content_author_text = "";
		if (author_data[1].startsWith("attr")){
			content_author_text = content_author.attr(author_data[1].substring(4));
		}else{
			content_author_text = content_author.text();
		}
		content[0] = content_author_text;
		System.out.println(content_author_text);
		
		String[] pubtime_data = data[10].split("|");
		Elements content_pubtime = doc.select(pubtime_data[0]);//pubtime
		String content_pubtime_text = "";
		if (pubtime_data[1].startsWith("attr")){
			content_pubtime_text = content_pubtime.attr(pubtime_data[1].substring(4));
		}else{
			content_pubtime_text = content_pubtime.text();
		}
		content[0] = content_pubtime_text;
		System.out.println(content_pubtime_text);
		
		String[] content_data = data[10].split("|");
		Elements content_content = doc.select(content_data[0]);//content
		String content_content_text = "";
		if (content_data[1].startsWith("attr")){
			content_content_text = content_content.attr(content_data[1].substring(4));
		}else{
			content_content_text = content_content.text();
		}
		content[0] = content_content_text;
		System.out.println(content_content_text);
		
		content[4] = url;
		return content;
	}
	
	public boolean storeToDB(String[] content) {
		/*DatabaseConnect database4 = new DatabaseConnect();
		database4.ConnectDb();
		String sql = "insert into content (title , author , pubtime , content , source)values('"+content[0]+"' ,'"+content[1]+"' , '"+content[2]+"' ,'"+content[3]+"' ,'"+content[4]+"')";
		System.out.println(sql);
		database4.stmt.execute(sql);
		database4.close();
		*/
		
		Jedis jedis;
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
		return true;
		
		
		
	}
	
	public void updateModuleFailtoDB(){
		System.out.println("未能爬取到数据");
	}

	public void UpdateStateFailtoDB(String[] content) throws SQLException{
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
		String sql = "update model set status = '"+insertstring+"'";
		database5.stmt.execute(sql);
		database5.close();
	}
}
