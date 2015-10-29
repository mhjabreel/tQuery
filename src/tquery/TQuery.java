/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tquery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import tquery.utils.TweetsWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import tquery.utils.CSVTweetsWriter;

/**
 *
 * @author MHJ
 */
public class TQuery {



    public static void crawlTweets(String query, TweetsWriter writer) {
        
        
        String startURL;
        try {
            startURL = "https://twitter.com/search?f=tweets&vertical=default&q=" + URLEncoder.encode(query, "UTF-8") + "&src=typd";
            Document doc = Jsoup.connect(startURL).get();
            Elements timeline = doc.select("#timeline");
            
            if (timeline != null && !timeline.isEmpty()) {
                
                Element streamContainer = timeline.first().select("div.stream-container").first();
                
                String maxPosition = streamContainer.attr("data-max-position");
                
                Elements tweets = streamContainer.select("li.stream-item");
                
                if(tweets == null || tweets.isEmpty()) {
                    return;
                }
                
                System.out.println(String.format("%s - Found %d tweet(s) ..", getCurrentTime(), tweets.size()));
                
                int writed = processTweets(tweets);
                
                System.out.println(String.format("%s - Writed %d tweet(s) ..", getCurrentTime(), writed));
                
                while(!maxPosition.isEmpty()) {
                    String nextPage = getNextPage(query, maxPosition);

                    JSONObject jsonObj = new JSONObject(nextPage);

                    maxPosition = jsonObj.getString("min_position"); 
                    
                    doc = Jsoup.parse(jsonObj.getString("items_html"));
                    
                    tweets = doc.select("li.stream-item");
                    
                    if(tweets.isEmpty()) {
                        return;
                    }
                    
                    System.out.println(String.format("%s - Found %d tweet(s) ..", getCurrentTime(), tweets.size()));

                    writed = processTweets(tweets);

                    System.out.println(String.format("%s - Writed %d tweet(s) ..", getCurrentTime(), writed));                    
                    
                }

            }
            
            System.out.println();            
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(TQuery.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TQuery.class.getName()).log(Level.SEVERE, null, ex);
        }

        
    }
    
    
    private static String getNextPage(String query, String maxPosition) {
        
        String autoLoadURL;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            autoLoadURL = "https://twitter.com/i/search/timeline?vertical=default&q=" +
                    URLEncoder.encode(query, "UTF-8") + "&src=typd&include_available_features=1&include_entities=1&max_position=" +
                    maxPosition + "&reset_error_state=false";
            URL url = new URL(autoLoadURL);
            
            //System.out.println(url.toString());
            
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            
            String strTemp = "";
            
            while (null != (strTemp = br.readLine())) {
                stringBuilder.append(strTemp);
                
            }            
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(TQuery.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(TQuery.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TQuery.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return stringBuilder.toString();
        
        
    }
    
    private static int processTweets(Elements tweets) {
        for (Element tweet: tweets) {
            long tweetID = Long.parseLong(tweet.attr("data-item-id"));
            
            Elements tweetTime = tweet.select("a.tweet-timestamp");
            String createdAt = "";
            if(!tweetTime.isEmpty()) {    
                createdAt = tweetTime.first().attr("title");
            }
            
            String tweetText = tweet.select("p.tweet-text").first().text();
            
            Elements user = tweet.select("span.username");
            
            String userName = "";
            
            if(!user.isEmpty()) {
                userName = user.first().text();
            }
            
            System.out.println(tweetID + ", " + tweetText);
            
        }
        return 0;
    }


    
    private static String getCurrentTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();    
        return dateFormat.format(date);
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        //crawlTweets("from:mhjabreel since:2014-03-02", null);
        
        TweetsSpider spider = new TweetsSpider(new CSVTweetsWriter("E:\\tst.csv"));
        spider.crawlTweets("from:mhjabreel since:2014-03-02");

        //q=from:mhjabreel since:2014-03-02
    }

}
