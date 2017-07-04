package multiThreadCrawler;

public class Controller {
	
	static int crawler_num = 2;

	public static void main(String[] args) {
		ChannelCrawler[] crawlers = new ChannelCrawler[crawler_num];
		for(int i = 0;i < crawler_num;i++){
			crawlers[i] = new ChannelCrawler();
			crawlers[i].start();
		}
	}

}
