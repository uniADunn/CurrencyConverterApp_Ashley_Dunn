package adunn.cw.currencyconverterapp_ashley_dunn.threads;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import adunn.cw.currencyconverterapp_ashley_dunn.rss_currency.*;

public class RSSCurrency implements Runnable{

    private final static int RSS_FEED_DATA_UPDATE = 1; //update feed data message
    private final static int RSS_RATES_DATA_UPDATE = 2; //update rates feed data message
    private final static int RSS_RATE_PROGRESS_UPDATE = 3; //update for progress updates
    private static final int ERROR_FEED_DATA = 4;//update ui if errors

    private static String urlSource = ""; //url source: https://www.fx-exchange.com/gbp/rss.xml
    private String result= "";//result of parsing
    private final Handler rssDataHandler; //handler for updating UI
    private ArrayList<CurrencyRate> rates; //list of currency rates
    //CONSTRUCTOR
    public static void setURLTEST(String url){
        urlSource = url;
    }
    public RSSCurrency(Handler uiHandler){
        rates = new ArrayList<>();
        rssDataHandler = uiHandler;
    }
    @Override
    public void run(){
        URL aurl;
        URLConnection yc;
        BufferedReader in;
        String inputline;
        //GET CURRENCY DATA FROM FX EXCHANGE
        try{
            Log.d("RSSCurrency", "RSS DATA UPDATE STARTING...");
            aurl = new URL(urlSource);
            yc = aurl.openConnection();
            in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            while ((inputline = in.readLine()) != null) {
                result = result + inputline;
            }
            in.close();
        } catch (MalformedURLException e) {
            Log.e("RSSCurrency", "Malformed URL Exception: " + e.getMessage());
            updateUI(ERROR_FEED_DATA, null);
            return;
        }
        catch(IOException e){
            Log.e("RSSCurrency", "IO Exception: " + e.getMessage());
            return;
        }

        //clean up any leading garbage characters
        int i = result.indexOf("<?");//initial tag
        result = result.substring(i);

        //clean up any trailing garbage at the end of the file
        i = result.indexOf("</rss>");//final tag
        result = result.substring(0, i + 6);

        Log.d("RSSCurrency", "RSS DATA PARSING...");
        //got the data start parsing
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(result));

            //parse here
            int eventType = xpp.getEventType();
            String text = "";
            boolean isItem = false;
            RssFeedData rssFeedData = new RssFeedData();
            CurrencyRate currencyRate = null;

            ArrayList<CurrencyRate> tempRates = new ArrayList<>(); // list to hold rates

            //parse to get total number of items, by counting the amount of start tags that start with item
            XmlPullParser xppForCount = factory.newPullParser();
            xppForCount.setInput(new StringReader(result));
            int count = 0;
            int eventTypeCount = xppForCount.getEventType();
            while (eventTypeCount != XmlPullParser.END_DOCUMENT) {
                if (eventTypeCount == XmlPullParser.START_TAG && xppForCount.getName().equalsIgnoreCase("item")) {
                    count++;
                }
                eventTypeCount = xppForCount.next();
            }
            final int totalItems = count;//total amount of items, for setting progress bar max value

            //parse to get rates and rss feed data
            xpp.setInput(new StringReader(result));
            eventType = xpp.getEventType();

            while(eventType != XmlPullParser.END_DOCUMENT){

                if(eventType == XmlPullParser.START_TAG){
                    if(xpp.getName().equalsIgnoreCase("item")){
                        isItem = true;
                        currencyRate = new CurrencyRate();
                    }
                    else if (xpp.getName().equalsIgnoreCase("rss")) {
                        rssFeedData.setStrVersionNumber(xpp.getAttributeValue(0));
                    }
                    else if (xpp.getName().equalsIgnoreCase("title")) {
                        text = xpp.nextText();
                        if (!isItem) {
                            rssFeedData.setTitle(text);
                        }
                        else {
                            currencyRate.setTitle(text);
                        }
                    }
                    else if (xpp.getName().equalsIgnoreCase("link")) {
                        text = xpp.nextText();
                        if (!isItem) {
                            rssFeedData.setLink(text);
                        }
                        else {
                            currencyRate.setLink(text);
                        }
                    }
                    else if (xpp.getName().equalsIgnoreCase("description")) {
                        text = xpp.nextText();
                        if (!isItem) {
                            rssFeedData.setDescription(text);
                        }
                        else {
                            currencyRate.setDescription(text);
                        }
                    }
                    else if (xpp.getName().equalsIgnoreCase("lastbuilddate")) {
                        text = xpp.nextText();
                        rssFeedData.setLastBuildDate(text);
                    } else if (xpp.getName().equalsIgnoreCase("language")) {
                        text = xpp.nextText();
                        rssFeedData.setLanguage(text);
                    } else if (xpp.getName().equalsIgnoreCase("copyright")) {
                        text = xpp.nextText();
                        rssFeedData.setCopyright(text);
                    } else if (xpp.getName().equalsIgnoreCase("docs")) {
                        text = xpp.nextText();
                        rssFeedData.setDoc(text);
                    } else if (xpp.getName().equalsIgnoreCase("ttl")) {
                        text = xpp.nextText();
                        rssFeedData.setTtl(text);
                    }
                    else if (xpp.getName().equalsIgnoreCase("guid")) {
                        text = xpp.nextText();
                        if (currencyRate != null) {
                            currencyRate.setGuid(text);
                        }
                    } else if (xpp.getName().equalsIgnoreCase("pubdate")) {
                        text = xpp.nextText();
                        if (currencyRate != null) {
                            currencyRate.setPubDate(text);
                        }
                    } else if (xpp.getName().equalsIgnoreCase("category")) {
                        text = xpp.nextText();
                        if (currencyRate != null) {
                            currencyRate.setCategory(text);
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (xpp.getName().equalsIgnoreCase("item")) {
                        isItem = false;
                        if (currencyRate != null) {
                            currencyRate.extractTitle();
                            currencyRate.extractRate();
                            currencyRate.rateConvert();
                            currencyRate.createFlagUrlCode();
                            tempRates.add(currencyRate);
                            // Send progress update
                            updateUIProgress(RSS_RATE_PROGRESS_UPDATE, tempRates.size(), totalItems);
                            try {
                                Thread.sleep(15);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                }
                eventType = xpp.next();
            }//end of while
            //add rates to rss feed data and rates in view model
            rates.addAll(tempRates);
            rssFeedData.setItems(rates);
            //update main thread
            updateUI(RSS_FEED_DATA_UPDATE, rssFeedData);
            updateUI(RSS_RATES_DATA_UPDATE, rates);
        }
        catch(XmlPullParserException | IOException e){
            Log.e("Parsing", "Exception: " + e.getMessage());
        }
        Log.d("RSSCurrency", "RSS DATA UPDATE COMPLETE!!!");
    }
    //update ui with rss data
    private void updateUI(int update, Object updateData){
        Message msg = new Message();
        msg.what = update;
        msg.obj = updateData;
        rssDataHandler.sendMessage(msg);
    }

    //update ui with progress
    private void updateUIProgress(int what, int progress, int max) {
        Message msg = new Message();
        msg.what = what;
        msg.arg1 = progress; // current progress
        msg.arg2 = max;      // max value for the progress bar
        rssDataHandler.sendMessage(msg);
    }
}
