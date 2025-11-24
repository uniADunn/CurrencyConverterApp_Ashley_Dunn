package adunn.cw.currencyconverterapp_ashley_dunn.rss_currency;

import java.util.List;

public class RssFeedData {
    private String strVersionNumber;//version number
    private String title;//title
    private String link;// fx exchange link
    private String description;//description
    private String lastBuildDate;//last build date
    private String language;//language
    private String copyright; //copyright
    private String doc;//doc
    private String ttl;//ttl
    private List<CurrencyRate> items;//list of currency rates
    //constructor
    public RssFeedData(){}
    //getters and setters
    public void addItems(CurrencyRate item){
        items.add(item);
    }
    public String getStrVersionNumber() {
        return strVersionNumber;
    }
    public void setStrVersionNumber(String strVersionNumber) {
        this.strVersionNumber = strVersionNumber;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLastBuildDate() {
        return lastBuildDate;
    }

    public void setLastBuildDate(String lastBuildDate) {
        this.lastBuildDate = lastBuildDate;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getDoc() {
        return doc;
    }

    public void setDoc(String doc) {
        this.doc = doc;
    }

    public List<CurrencyRate> getItems() {
        return items;
    }

    public void setItems(List<CurrencyRate> items) {
        this.items = items;
    }

    public String getTtl() {
        return ttl;
    }

    public void setTtl(String ttl) {
        this.ttl = ttl;
    }
}
