package multiThreadCrawler;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainThread {
	
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

	public static void main(String[] args) throws SQLException, IOException {
		MainThread main = new MainThread();
		Channeltemplate cp1 = new Channeltemplate(main, 1);
		Channeltemplate cp2 = new Channeltemplate(main, 2);
		cp1.start();
		cp2.start();
	}
	
	public synchronized String[] getdatafromDB(){ //从模板库获取模板
		String[] template = new String[18];
		for (int i = 0;i < 15;i++)
			template[i] = null;
		DatabaseConnect database1 = new DatabaseConnect();
		database1.ConnectDb();
		try{
			ResultSet rs1 = database1.stmt.executeQuery("select * from template where status = '正常'");
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
				template[9] = rs1.getString(10).substring(0, 19); //开始时间
				template[10] = rs1.getString(11).substring(0, 19); //结束时间
				template[11] = rs1.getString(12); //状态
				template[12] = rs1.getString(13); //标题Xpath
				template[13] = rs1.getString(14); //作者path
				template[14] = rs1.getString(15); //发布时间Xpath
				template[15] = rs1.getString(16); //正文Xpath
				template[16] = rs1.getString(17); //来源Xpath
				template[17] = rs1.getString(19); //失效次数
			}while((!urlTimeJudge(template[int_start_time], getCurrentTime()))&&rs1.next());
		}catch(SQLException e){
			System.out.println("Exception occur when getting template from DB.");
		}
		database1.close();
		if (urlTimeJudge(template[int_start_time], getCurrentTime())){
			setStartTimetoDB(template[int_channel_url]);
			return template;
		}else{
			return null;
		}
		
	}
	
	public String getCurrentTime(){//获取当前时间
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(new Date());
	}
	
	public void setStartTimetoDB(String url){//获取当前时间
		String time = new String(getCurrentTime());
		String sql = "update template set start_time = '"+time+"' where channel_url = '"+url+ "'";
		DatabaseConnect database2 = new DatabaseConnect();
		database2.ConnectDb();
		try {
			database2.stmt.executeUpdate(sql);
		} catch (SQLException e) {
			System.out.println("Exception occur when setting start_time to DB.");
		}
		database2.close();
	}

	public void setEndTimetoDB(String url){//设置开始时间
		String time = new String(getCurrentTime());
		String sql = "update template set stop_time = '"+time+"' where channel_url = '"+url+"'";
		DatabaseConnect database3 = new DatabaseConnect();
		database3.ConnectDb();
		try {
			database3.stmt.executeUpdate(sql);
		} catch (SQLException e) {
			System.out.println("Exception occur when setting stop_time to DB.");
		}
		database3.close();
	}
	
	private boolean urlTimeJudge(String last_end_time, String current_time){//时间判定
		if (last_end_time == null)
			return true;
		if (last_end_time.length() < 14)
			return true;
		String lastYtoH = last_end_time.substring(0, 14);
		String currYtoH = current_time.substring(0, 14);
		if (lastYtoH.equals(currYtoH)){
			int lastMin = Integer.parseInt(last_end_time.substring(14, 16));
			int currMin = Integer.parseInt(current_time.substring(14, 16));
			if (currMin - lastMin > 20){
				return true;
			}else if (currMin - lastMin == 20){
				int lastSec = Integer.parseInt(last_end_time.substring(17));
				int currSec = Integer.parseInt(current_time.substring(17));
				return currSec >= lastSec;
			}else{
				return false;
			}
		}else{
			return true;
		}
	}
}
