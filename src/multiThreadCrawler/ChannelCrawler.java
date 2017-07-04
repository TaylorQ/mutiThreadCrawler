package multiThreadCrawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.client.HttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ChannelCrawler extends Thread {
	
	public ChannelCrawler(){
		
	}
	
	public String[] getURLfromDB(){
		String[] url_info = new String[4];
		//read url information from DB
		//0 վ����
		//1 ��ʼʱ��
		//2 ����ʱ��
		//3 url
		url_info[0] = "����";
		url_info[1] = "2017-07-03 14:09:28";
		url_info[2] = "2017-07-03 14:09:30";
		url_info[3] = "http://news.sina.com.cn/society/";
		
		return url_info;
	}
	
	public String[] getModulefromDB(String url){
		String[] module = {"title|text", "meta[property=\"article:author\"]|attrcontent",
				"meta[property=\"article:published_time\"]|attrcontent", "p|alltext"};
		return module;
	}
	
	public void setStartTimetoDB(String time, String url){
		
	}

	public void setEndTimetoDB(String time, String url){
		
	}
	
	public String[] downloadURLlist(String url){
		String html = getPageContent(url);
		Document doc = Jsoup.parse(html);
		Elements hrefs = doc.select("a[href]");
		String[] urls = new String[hrefs.size()];
		for (int i = 0;i < hrefs.size();i++){
			urls[i] = hrefs.get(i).attr("href");
			System.out.println(urls[i]);
		}
		return urls;
	}
	
	public String getPageContent(String strUrl) {  
        // ��ȡ�����ҳ  
        StringBuffer buffer = new StringBuffer();  
        System.setProperty("sun.net.client.defaultConnectTimeout", "5000");  
        System.setProperty("sun.net.client.defaultReadTimeout", "5000");  
        try {  
            URL newUrl = new URL(strUrl);  
            HttpURLConnection hConnect = (HttpURLConnection) newUrl  
                    .openConnection();  
            // ��ȡ����  
              
            BufferedReader rd = new BufferedReader(new InputStreamReader(  
                    hConnect.getInputStream()));  
            int ch;  
            for (int length = 0; (ch = rd.read()) > -1; length++)  
                buffer.append((char) ch);             
            rd.close();  
            hConnect.disconnect();  
            return buffer.toString().trim();  
        } catch (Exception e) {  
            // return "����:��ȡ��ҳʧ�ܣ�";  
            //  
            return null;
        }  
    }
	
	public String getCurrentTime(){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//�������ڸ�ʽ
		return df.format(new Date());// new Date()Ϊ��ȡ��ǰϵͳʱ��
	}
	
	public boolean urlTimeJudge(String last_end_time, String current_time){
		if (!last_end_time.equals("")){
			String lastYtoH = last_end_time.substring(0, 14);
			String currYtoH = current_time.substring(0, 14);
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
		}else{
			return true;
		}
	}
	
	public static void main(String args[]){
        ChannelCrawler cc = new ChannelCrawler();
        cc.start();
	}
	
	public void run(){
		while (true){
			System.out.println("get url from db.");
			String[] channel_info = getURLfromDB();
			System.out.println("url time judge.");
			System.out.println("url last end time:"+channel_info[2]);
			System.out.println("current time:"+getCurrentTime());
			if (this.urlTimeJudge(channel_info[2], getCurrentTime())){
				System.out.println("judge result true.");
				this.setStartTimetoDB(getCurrentTime(), channel_info[3]);
				System.out.println("download url list.");
				String[] URLlist = downloadURLlist(channel_info[3]);
				System.out.println("get module from DB.");
				String[] module = getModulefromDB(channel_info[3]);
				System.out.println("download pages.");
				PageDownloader pd = new PageDownloader(module, channel_info[3], URLlist);
				this.setEndTimetoDB(getCurrentTime(), channel_info[3]);
				
				try {
					this.sleep(60000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
