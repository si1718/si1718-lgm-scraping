package web.scraping.jsoup;

//import java.net.URL;
import java.util.ArrayList;
//import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
//import java.util.Map;
import java.util.Random;
import java.io.IOException;
import java.net.MalformedURLException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class JSoupBook implements Runnable{
	private static final String URL_BASE = "https://investigacion.us.es/sisius";
	//private static final String SEARCH = "%%%";
	private static String[] keywords = {"business","science","nova"};
	private static Integer maxIndex=0;
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
			try {
				scraping();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}
			
	//public static void main(String[] args) throws MalformedURLException, IOException{
	public static void scraping() throws MalformedURLException, IOException{
		MongoClientURI connectionString = new MongoClientURI("mongodb://djluis:djluis@ds149855.mlab.com:49855/si1718-lgm-books");
		MongoClient mongoClient = new MongoClient(connectionString);
		MongoDatabase database = mongoClient.getDatabase("si1718-lgm-books");
		
		//IMPORTANTE: Añadida nueva BBDD para pruebas del scraping
		MongoCollection<org.bson.Document> collection = database.getCollection("booksScraping");
		List<String> researchersLink = new ArrayList<>();

		Document researchers = Jsoup
                .connect(URL_BASE)
                .data("text2search", "%%%")
				.data("en", "1")
                .data("inside", "1")
                .maxBodySize(10 * 1024 * 1024)
				.post();

		Elements elements = researchers.select("td.data a");
        int i = 0;

        for(Iterator<Element> iterator = elements.iterator(); iterator.hasNext(); ) {
            Element researcher = iterator.next();
            if(i % 2 != 1) {
                String link = researcher.attr("href");
                if(link.contains("sis_showpub.php")) {
                    researchersLink.add(link);
                }
            }
            i++;
        }
        
        for(String researcherBookLink : researchersLink) {
        	Document books = Jsoup
            		.connect("https://investigacion.us.es"+researcherBookLink)
            		.get();
        	
        	Element bookTag = books.select("h5").first();
        	if(bookTag != null) {
        		if(bookTag.toString().equals("<h5>Libros</h5>")) {
        			String page = books.body().html();
        			String[] split = page.split("<h5>Libros</h5>");
        			String book = split[1].split("<h5>")[0];
        			split = book.split("</div>");
        			book = split[0];
        			
        			String[] booksDemo = book.replace("\n", "").split("        <br>       <br>        ");
        			List<org.bson.Document> bookList = new ArrayList<org.bson.Document>();
        			for(String booksD : booksDemo) {
        				ArrayList<String> authorsFinal = new ArrayList<String>();
        				String[] booksD1 = booksD.replace("<u>", "").split(":</u>       <br>");
        				String[] authors = booksD1[0].replace("        ", "").split(", ");
        				String[] aux;
        				String keywordsBook = "";
        				new Random().ints(1, 0, 3).forEach(x -> maxIndex=x);
        				System.out.println(maxIndex);
        				Integer isbnInt =((int) Math.round(Math.random()*10000000))+1;
        				String isbn = isbnInt.toString()+"rand";
        				String title = "Default title";
        				Integer year = 2017;
        				String publisher = "Default publisher";
        				for(i=0;i<(authors.length/2);i=i+2) {
        					String name = authors[i+1]+" "+authors[i];
        					authorsFinal.add(name);
        				}
        				System.out.println(authorsFinal.toString());
        				if(booksD1.length>1) {
        				if(booksD1[1].contains("ISBN")){
        					isbn = booksD1[1].split("ISBN")[1];
        				}
        				
        				if(booksD1[1].contains(".")) {
        					aux = (booksD1[1]).split("\\.");
        					title = aux[0];
        					if(aux.length>=4) {
        						publisher = aux[aux.length-3];
        						try {
            						year = Integer.valueOf((aux[aux.length-2]).trim());
            					}catch (NumberFormatException e){
            						System.out.println("Number error: " + aux[aux.length-2]);
            					}
        					}else if(aux.length==3) {
        						if(aux[2].length()==4) {
        							try {
                						year = Integer.valueOf((aux[2]).trim());
                					}catch (NumberFormatException e){
                						System.out.println("Number error: " + aux[2]);
                					}
        						}
        						publisher = aux[1];
        					}else if(aux.length==2) {
        						if(aux[1].length()==4) {
        							try {
                						year = Integer.valueOf((aux[1]).trim());
                					}catch (NumberFormatException e){
                						System.out.println("Number error: " + aux[1]);
                					}
        						}
        					}
        				}else {
        					title = booksD1[1];
        				}
        				}
        				for(int e=0;e<maxIndex;e++) {
        					if(keywordsBook.equals("")) {
        						keywordsBook = keywords[e];
        					}else {
        						keywordsBook = keywordsBook +", "+ keywords[e];
        					}
        				}
        				org.bson.Document libro = new org.bson.Document("idBooks", isbn.trim())
        						.append("year", year)
        						.append("title", title)
        						.append("publisher", publisher)
        						.append("author", authorsFinal)
        						.append("keywords",keywordsBook);
        				bookList.add(libro);
        				if(bookList.size()>50) {
        					collection.insertMany(bookList);
        					bookList.clear();
        				}
        				
        				
        			}
        			if(bookList.size()!=0) {
        				collection.insertMany(bookList);
        			}
        		}
        	} 
        }

        mongoClient.close();
	}
}
