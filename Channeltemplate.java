package multiThreadCrawler;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Channeltemplate {
	
	public Channeltemplate(){//构造函数
		
	}
	
	public String[] getdatafromDB() throws SQLException{ //杩炴帴鏁版嵁搴擄紝鑾峰緱鏁版嵁
		DatabaseConnect database1 = new DatabaseConnect();
		database1.ConnectDb();
		String[] url_info = new String[15];
		database1.rs = database1.stmt.executeQuery("select * from model where status = '正常'");
		ResultSet rs1 = database1.rs;
		rs1.next();
		//从模板库获取模板
		do{
					url_info[0] = rs1.getString(1); //网址
					System.out.println(url_info[0]);
					url_info[1] = rs1.getString(2); //境内外
					System.out.println(url_info[1]);
					url_info[2] = rs1.getString(3); //国家
					System.out.println(url_info[2]);
					url_info[3] = rs1.getString(4); //语言
					System.out.println(url_info[3]);
					url_info[4] = rs1.getString(5); //频道名
					System.out.println(url_info[4]);
					url_info[5] = rs1.getString(6); //频道url
					System.out.println(url_info[5]);
					url_info[6] = rs1.getString(7); //频道下一页url
					System.out.println(url_info[6]);
					url_info[7] = rs1.getString(8); //开始时间
					System.out.println(url_info[7]);
					url_info[8] = rs1.getString(9); //结束时间
					System.out.println(url_info[8]);
					url_info[9] = rs1.getString(10); //状态
					System.out.println(url_info[9]);
					url_info[10] = rs1.getString(11); //标题Xpath
					System.out.println(url_info[10]);
					url_info[11] = rs1.getString(12); //作者path
					System.out.println(url_info[11]);
					url_info[12] = rs1.getString(13); //发布时间Xpath
					System.out.println(url_info[12]);
					url_info[13] = rs1.getString(14); //内容Xpath
					System.out.println(url_info[13]);
					url_info[14] = rs1.getString(15); //来源Xpath
					System.out.println(url_info[14]);
		}while((!urlTimeJudge(url_info[8], getCurrentTime()))&&rs1.next());		
		database1.close();
		return url_info;
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
		String sql = "update model set start_time = '"+time+"' where channel_url = '"+url+ "'";
		System.out.println(sql);
		database2.stmt.executeUpdate(sql);
		database2.close();
	}

	public void setEndTimetoDB(String url) throws SQLException{//设置开始时间
		String time = new String(getCurrentTime());
		DatabaseConnect database3 = new DatabaseConnect();
		database3.ConnectDb();
		database3.stmt.executeUpdate("update model set stop_time = '"+time+"' where channel_url = '"+url+"'");
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
		String[] channel_info;
		channel_info = getdatafromDB();
		System.out.println("url time judge.");
		System.out.println("url last end time:"+channel_info[8]);
		System.out.println("current time:"+getCurrentTime());
		if (this.urlTimeJudge(channel_info[8], getCurrentTime())&& channel_info[9].equals("正常")){
			System.out.println("judge result true");
			this.setStartTimetoDB(channel_info[5]);
			System.out.println("download url list.");
			String[] URLlist = downloadURLlist(channel_info[5]);
			System.out.println("get module from DB.");
			PageDownloader pd = new PageDownloader();
			pd.execute(URLlist,channel_info);
			this.setEndTimetoDB(channel_info[5]);
		}
	}
}
