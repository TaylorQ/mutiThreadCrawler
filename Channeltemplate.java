package multiThreadCrawler;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Channeltemplate extends Thread{
	
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
	
	private MainThread father;
	private int id;
	private static Logger infologger = Logger.getLogger("infoLogger");
	private static Logger errorlogger = Logger.getLogger("errorLogger");
	
	public Channeltemplate(MainThread father, int id){//构造函数
		this.father = father;
		this.id = id;
	}
	
	public String[] downloadURLlist(String url){ //下载URL列表
		Document doc = null;
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			e.printStackTrace();
			errorlogger.error("Exception occur when downloading:"+url+".");
			return null;
		}
		Elements hrefs = doc.select("a[href]");
		String[] urls = new String[hrefs.size()];
		System.out.println("[cp"+id+"]开始爬："+url);
		for (int i = 0;i < hrefs.size();i++){
			urls[i] = hrefs.get(i).attr("abs:href");
		}
		return urls;
	}

	public static boolean urlTimeJudge(String last_end_time, String current_time){//时间判定
		if (last_end_time == null)
			return true;
		if (last_end_time.length() < 14)
			return true;
		System.out.println(last_end_time.length()+"   "+current_time.length());
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
			System.out.println(lastMin+"   "+currMin);
			if (currMin - lastMin > 2){
				return true;
			}else if (currMin - lastMin == 2){
				int lastSec = Integer.parseInt(last_end_time.substring(17));
				int currSec = Integer.parseInt(current_time.substring(17));
				System.out.println(lastSec+"   "+currSec);
				return currSec >= lastSec;
			}else{
				return false;
			}
		}else{
			return true;
		}
	}
	
	public void run(){
		while(true){
			int craw_count = 0;
			String[] template = father.getdatafromDB();
			if (template != null){
				father.setStartTimetoDB(template[int_channel_url]);
				String[] URLlist = downloadURLlist(template[int_channel_url]);
				if (URLlist != null){
					PageDownloader pd = new PageDownloader(id);
					craw_count = pd.execute(URLlist, template);
				}
				father.setEndTimetoDB(template[int_channel_url]);
			}else{
				craw_count = -1;
			}
			try {
				this.sleep(craw_count == 0?5000:(craw_count == -1?120000:20000));
			} catch (InterruptedException e) {
				errorlogger.error("Exception occur when cp"+id+" sleeping.");
			}
		}
	}
}
