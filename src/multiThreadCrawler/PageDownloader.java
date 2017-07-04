package multiThreadCrawler;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PageDownloader{
	
	String[] module;
	String[] url_list;
	String channel_url;
	
	public PageDownloader(String[] module, String channel_url, String[] url_list){
		this.module = module;
		this.url_list = url_list;
		this.channel_url = channel_url;
		for (String url : url_list){
			if (url.contains(".")){
				System.out.println("pull out info from page:"+url);
				String[] content = getPageContent(url);
				if (content != null){
					storeToDB(content);
				
					//for test
					System.out.println("title:"+content[0]);
					System.out.println("author:"+content[1]);
					System.out.println("pubtime:"+content[2]);
					System.out.println("content:"+content[3]);
				}
			}
		}
	}
	
	public String[] getPageContent(String url){
		Document doc = null;
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		String[] content = new String[4];
		for (int i = 0;i < 4;i++){
			String[] tag = module[i].split("|");
			String target = tag[tag.length-1];
			content[i] = "";
			Elements temp = doc.getAllElements();
			for (int ii = 0;ii < tag.length-3;ii++){
				temp = temp.select(tag[ii]);
			}
			if (target.startsWith("all")){
				Elements elems = temp.select(tag[tag.length-2]);
				target = target.substring(3);
				if (target.startsWith("attr")){
					for (int ii = 0;ii < elems.size();ii++){
						content[i] += elems.get(ii).attr(target).trim()+"\n";
					}
				}else if(target.equals("text")){
					for (int ii = 0;ii < elems.size();ii++){
						content[i] += elems.get(ii).text().trim()+"\n";
					}
				}
			}else{
				Element elem = temp.select(tag[tag.length-2]).get(0);
				if (target.startsWith("attr")){
					target = target.substring(4);
					content[i] += elem.attr(target).trim();
				}else if(target.equals("text")){
					content[i] += elem.text().trim();
				}
			}
		}
		return content;
	}
	
	public boolean storeToDB(String[] content){
		return true;
	}
	
	public void updateModuleFailtoDB(String url, String part){
		
	}
	
	public static void main(String args[]){
		String[] url_list = {"http://news.sina.com.cn/s/wh/2017-07-02/doc-ifyhrxtp6424620.shtml"};
		String[] module = {"title|text", "meta[property=\"article:author\"]|attrcontent",
				"meta[property=\"article:published_time\"]|attrcontent", "div[id=\"artibody\"]|p|alltext"};
		PageDownloader pd = new PageDownloader(module, "", url_list);
	}
	
}
