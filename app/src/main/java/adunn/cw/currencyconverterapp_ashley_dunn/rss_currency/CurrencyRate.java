package adunn.cw.currencyconverterapp_ashley_dunn.rss_currency;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class CurrencyRate implements Serializable {
    private static final long serialVersionUID = 1L;
    private String title;//title
    private String countryCode;//code
    private String flagUrlCode;//flag url code
    private String flagUrl;//flag url
    private String strRate;//str rate
    private double rate;//parsed rate
    private String link;//link
    private String guid;//link
    private String pubDate;//publish date
    private String description;//description of rate
    private String category;//category
    //constructor
    public CurrencyRate(){}
    //create flag url from country code
    public void createFlagUrlCode(){

        //special cases
        switch (countryCode){
            case "EUR":
                flagUrlCode = "eu";
                break;
            case "ANG":
                flagUrlCode = "CW";
                break;
            default:
                flagUrlCode = countryCode.substring(0,2).toUpperCase();
        }
        Log.d("flagUrlCode", flagUrlCode);
        if(flagUrlCode.equals("eu")){
            flagUrl = "https://flagcdn.com/w160/" + flagUrlCode + ".png";
        }
        else{
            flagUrl = "https://flagsapi.com/" + flagUrlCode + "/shiny/64.png";
        }
    }
    //getters and setters
    public String getFlagUrl(){
        return flagUrl;
    }
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

    //extract title and code
    public void extractTitle(){
        if (this.title == null) {
            this.title = "";
            this.countryCode = "";
            return;
        }
        String temp = this.title;
        int i = temp.indexOf("/");
        if (i != -1) {
            title = temp.substring(i + 1).trim();
        } else {
            title = temp.trim();
        }

        int openParenIndex = title.indexOf("(");
        if (openParenIndex != -1) {
            String code = title.substring(openParenIndex);
            extractCode(code);
            title = title.substring(0, openParenIndex);
        }
        //Log.d("Title extracted", title);
    }
    //extract country code
    public void extractCode(String code){
        //set country code
        countryCode = code.substring(code.lastIndexOf("(")+1, code.lastIndexOf(")"));
        //Log.d("code extracted", countryCode);
    }
    //extract rate
    public void extractRate(){
        if (this.description == null) {
            this.description = "";
            this.strRate = "0";
            this.rate = 0.0;
            return;
        }
        String temp = this.description;
        //Log.d("description", temp);
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
            this.strRate = "0";
        }
        //Log.d("rate", this.strRate);
    }
    //convert rate to double from string
    public void rateConvert(){
        try{
            this.rate = Double.parseDouble(this.strRate);
        }
        catch (NumberFormatException nfe){
            Log.d("NumberFormatException", nfe.getMessage() + " Error formatting number");
            this.rate = 0;
        }
    }
    @NonNull
    @Override
    public String toString(){
        return title +" "+  countryCode;
    }
}
