package web.scraping.jsoup;

import java.net.URL;
import java.util.Iterator;
import java.io.IOException;
import java.net.MalformedURLException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TestJsoup {
	private static final String URL_BASE = "https://investigacion.us.es/sisius/sisius.php?struct=1&en=1&text2search=&ct=&cs=&inside=1";
	
			
	public static void main(String[] args) throws MalformedURLException, IOException{
		Document doc = Jsoup.parse(new URL(URL_BASE), 10000);
		Elements elementos = doc.getElementsByAttribute("valign");
		Element top = elementos.get(0);
		System.out.println(top);
		int i = 0;
		
		for(Iterator<Element> it = top.getElementsByTag("a").iterator(); it.hasNext();) {
			Element e = it.next();
			if(i % 2 != 0) {
				System.out.println(e.attr("href"));
				System.out.println(e.text());
			}
			i++;
		}
		
		//DAO sobre Mongo y persisto.
	}
}
