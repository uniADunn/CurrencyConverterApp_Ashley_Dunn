package adunn.cw.currencyconverterapp_ashley_dunn.rss_currency;

import android.util.Log;

import androidx.annotation.NonNull;

public class CurrencyRate {
    private String title;
    private String countryCode;
    private String strRate;
    private double rate;
    private String link;
    private String guid;
    private String pubDate;
    private String description;
    private String category;

    public CurrencyRate(){}
    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle(){
        return title;
    }
    public String getCountryCode(){
        return countryCode;
    }
    public String getStrRate(){
        return strRate;
    }
    public double getRate(){
        return rate;
    }
    public void setRate(double rate){
        this.rate = rate;
    }
    public void setLink(String link) {
        this.link = link;
    }
    public void setGuid(String guid) {
        this.guid = guid;
    }
    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public void extractTitle(){
        if (this.title == null) {
            Log.w("CurrencyRate", "Title is null when calling extractTitle(). Initializing to empty string.");
            this.title = "";
            this.countryCode = "";
            return;
        }
        String temp = this.title;
        int i = temp.indexOf("/");
        if (i != -1) {
            title = temp.substring(i + 1).trim();
        } else {
            Log.w("CurrencyRate", "Title '" + temp + "' does not contain '/', cannot extract. Using original title.");
            title = temp.trim();
        }

        int openParenIndex = title.indexOf("(");
        if (openParenIndex != -1) {
            String code = title.substring(openParenIndex);
            extractCode(code);
            title = title.substring(0, openParenIndex);
        } else {
            Log.w("CurrencyRate", "Title '" + title + "' does not contain '(', cannot extract country code.");
        }
        Log.d("Title extracted", title);
    }
    public void extractCode(String code){
        //set country code
        countryCode = code.substring(code.lastIndexOf("(")+1, code.lastIndexOf(")"));
        Log.d("code extracted", countryCode);
    }
    public void extractRate(){
        if (this.description == null) {
            Log.w("CurrencyRate", "Description is null when calling extractRate(). Initializing to empty string.");
            this.description = "";
            this.strRate = "0";
            this.rate = 0.0;
            return;
        }
        String temp = this.description;
        Log.d("description", temp);
        int i = temp.indexOf("=");
        if (i != -1) {
            temp = temp.substring(i + 1).trim();
            char[] chars = temp.toCharArray();
            StringBuilder rateBuilder = new StringBuilder();
            for(char c : chars){
                if(Character.isDigit(c) || c == '.'){
                    rateBuilder.append(c);
                }
            }
            this.strRate = rateBuilder.toString();
        } else {
            Log.w("CurrencyRate", "Description '" + temp + "' does not contain '=', cannot extract rate. Setting strRate to '0'.");
            this.strRate = "0";
        }
        Log.d("rate", this.strRate);
    }
    public void rateConvert(){
        try{
            this.rate = Double.parseDouble(this.strRate);
        }
        catch (NumberFormatException nfe){
            Log.d("NumberFormatException", nfe.getMessage());
            this.rate = 0;
        }
    }

    @NonNull
    @Override
    public String toString(){
        return title +" "+  countryCode;
    }
}
