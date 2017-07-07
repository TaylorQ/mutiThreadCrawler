package multiThreadCrawler;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Channeltemplate {
	
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
	
	public Channeltemplate(){//构造函数
		
	}
	
	public String[] getdatafromDB() throws SQLException{ //从模板库获取模板
		DatabaseConnect database1 = new DatabaseConnect();
		database1.ConnectDb();
		String[] template = new String[17];
		for (int i = 0;i < 15;i++)
			template[i] = null;
		database1.rs = database1.stmt.executeQuery("select * from template where status = '正常'");
		ResultSet rs1 = database1.rs;
		rs1.first();
		//开始顺序获取模板
		do{
			template[0] = rs1.getString(1); //id
			template[1] = rs1.getString(2); //站点名
			template[2] = rs1.getString(3); //境内外
			template[3] = rs1.getString(4); //国家
			template[4] = rs1.getString(5); //语言
			template[5] = rs1.getString(6); //频道名
			template[6] = rs1.getString(7); //频道url
			template[7] = rs1.getString(8); //频道下一页url
			template[8] = rs1.getString(9); //url的xpath
			template[9] = rs1.getString(10); //开始时间
			template[10] = rs1.getString(11); //结束时间
			template[11] = rs1.getString(12); //状态
			template[12] = rs1.getString(13); //标题Xpath
			template[13] = rs1.getString(14); //作者path
			template[14] = rs1.getString(15); //发布时间Xpath
			template[15] = rs1.getString(16); //正文Xpath
			template[16] = rs1.getString(17); //来源Xpath
		}while((!urlTimeJudge(template[8], getCurrentTime()))&&rs1.next());		
		database1.close();
		return template;
	}
	
	public String getCurrentTime(){//获取当前时间
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(new Date());
	}
	
	public void setStartTimetoDB(String url) throws SQLException{//获取当前时间
		String time = new String(getCurrentTime());
		System.out.println(time);
		DatabaseConnect database2 = new DatabaseConnect();
		database2.ConnectDb();
		String sql = "update template set start_time = '"+time+"' where channel_url = '"+url+ "'";
		System.out.println(sql);
		database2.stmt.executeUpdate(sql);
		database2.close();
	}

	public void setEndTimetoDB(String url) throws SQLException{//设置开始时间
		String time = new String(getCurrentTime());
		DatabaseConnect database3 = new DatabaseConnect();
		database3.ConnectDb();
		database3.stmt.executeUpdate("update template set stop_time = '"+time+"' where channel_url = '"+url+"'");
		database3.close();
	}
	
	public String[] downloadURLlist(String url) throws IOException{ //下载URL列表
		Document doc = Jsoup.connect(url).get();
		Elements hrefs = doc.select("a[href]");
		System.out.println(hrefs.size());
		String[] urls = new String[hrefs.size()];
		System.out.println("开始下载url列表");
		for (int i = 0;i < hrefs.size();i++){
			System.out.println("获得的url");
			urls[i] = hrefs.get(i).attr("href");
			System.out.println(urls[i]);
		}
		return urls;
	}

	public boolean urlTimeJudge(String last_end_time, String current_time){//时间判定
		if (last_end_time == null)
			return true;
		String lastYtoH = last_end_time.substring(0, 14);
		System.out.println(lastYtoH);
		String currYtoH = current_time.substring(0, 14);
		System.out.println(currYtoH);
		if(lastYtoH.equals("")){
			return true;
		}
		if (lastYtoH.equals(currYtoH)){
			int lastMin = Integer.parseInt(last_end_time.substring(14, 16));
			int currMin = Integer.parseInt(current_time.substring(14, 16));
			if (currMin - lastMin > 1){
				return true;
			}else if (currMin - lastMin == 1){
				int lastSec = Integer.parseInt(last_end_time.substring(18));
				int currSec = Integer.parseInt(current_time.substring(18));
				return currSec >= lastSec;
			}else{
				return false;
			}
		}else{
			return true;
		}
	}
	
	public void execute() throws SQLException, IOException{
		String[] template;
		System.out.println("get template from DB.");
		template = getdatafromDB();
		if (template[int_channel_url] != null){
			this.setStartTimetoDB(template[int_channel_url]);
			System.out.println("download url list.");
			String[] URLlist = downloadURLlist(template[int_channel_url]);
			System.out.println("pagedownloader activate.");
			PageDownloader pd = new PageDownloader();
			pd.execute(URLlist, template);
			this.setEndTimetoDB(template[int_channel_url]);
		}
	}
}
