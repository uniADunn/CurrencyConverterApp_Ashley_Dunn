package adunn.cw.currencyconverterapp_ashley_dunn.view_models;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import adunn.cw.currencyconverterapp_ashley_dunn.rss_currency.CurrencyRate;
import adunn.cw.currencyconverterapp_ashley_dunn.rss_currency.RssFeedData;

public class CurrencyViewModel extends ViewModel {
    private boolean isHorizontal; //flag if in landscape mode
    private double lowThreshold; //low threshold for colour
    private double highThreshold;//high threshold for colour
    private RssFeedData rssData;//rss data details
    private ArrayList<CurrencyRate> rates;//hold rates
    private ArrayList<CurrencyRate> filteredRates; // holds filtered rates
    private String lastPublished;//last published date
    //private String inputSearch; // user query input
    private boolean isFiltered = false; //true if filtered
    private final MutableLiveData<String> inputSearchLive = new MutableLiveData<>();//live user input data: query
    private final MutableLiveData<String> inputAmountLive = new MutableLiveData<>("");//live user input data: amount
    private final MutableLiveData<Boolean> gbpToXLive = new MutableLiveData<>(true);//true if GBP to X (false if X to GBP)
    private final MutableLiveData<CurrencyRate> rateSelectedLive = new MutableLiveData<>();//selected rate
    //build lists for recycler view based on vm fields
    public ArrayList<CurrencyRate> buildRateLists() {
        Log.d("currency view model", "buildRateLists: building Rates...");
        ArrayList<CurrencyRate> allRates = (rates != null) ? rates : new ArrayList<>();
        ArrayList<CurrencyRate> outRates = new ArrayList<>();

        //check for query
        String query = inputSearchLive.getValue();
        if (query != null && !query.isEmpty()) {

            String lowerCaseQuery = query.toLowerCase();
            for (CurrencyRate r : allRates) { // Search the entire list of rates
                String title = r.getTitle().toLowerCase();
                String code = r.getCountryCode().toLowerCase();
                if (title.contains(lowerCaseQuery) || code.contains(lowerCaseQuery)) {
                    outRates.add(r);
                }
            }
            return sortRatesByCountryCode(outRates); // Return only the search results
        }
        if (isFiltered) {
            //filter toggle is set to true
            for (CurrencyRate r : allRates) {
                String code = r.getCountryCode().toUpperCase();
                if ("USD".equals(code) || "EUR".equals(code) || "JPY".equals(code)) {
                    outRates.add(r);
                }
            }
        } else {
            //filtered rates is false show all rates
            outRates.addAll(allRates);
        }
        return sortRatesByCountryCode(outRates);
    }
    //sort rates by country code
    private ArrayList<CurrencyRate> sortRatesByCountryCode(ArrayList<CurrencyRate> rates){
        rates.sort((r1,r2)->{
            String c1 = r1.getCountryCode() == null ? "" : r1.getCountryCode();
            String c2 = r1.getCountryCode() == null ? "" : r2.getCountryCode();
            return c1.compareTo(c2);
        });
        return rates;
    }
    //getters and setters
    public void setHorizontal(boolean isHorizontal){
        this.isHorizontal = isHorizontal;
    }
    public boolean isHorizontal(){
        return isHorizontal;
    }
    public void setLowThreshold(double low){
        this.lowThreshold = low;
    }
    public void setHighThreshold(double high){
        this.highThreshold = high;
    }
    public double getLowThreshold(){
        return lowThreshold;
    }
    public double getHighThreshold(){
        return highThreshold;
    }
    public void setRateSelected(CurrencyRate rate){
        rateSelectedLive.setValue(rate);
    }
    public MutableLiveData<CurrencyRate> getRateSelected(){
        return rateSelectedLive;
    }
    public void setRssFeedData(RssFeedData rssData) {
        this.rssData = rssData;
    }
    public RssFeedData getRssFeedData(){
        return rssData;
    }
    public void setFiltered(boolean isFiltered){
        this.isFiltered = isFiltered;
    }
    public boolean isFiltered(){
        return isFiltered;
    }
    public void setGbpToX(boolean gbpTo){
        gbpToXLive.setValue(gbpTo);
    }
    public boolean isGbpToX(){
        Boolean value = gbpToXLive.getValue();
        return value != null ? value : true;
    }
    public MutableLiveData<Boolean> getGbpToXLive() {
        return gbpToXLive;
    }
    public void setRates(ArrayList<CurrencyRate> rates){
        this.rates = rates;
    }
    public void setLastPublished(String lastPublished){
        this.lastPublished = lastPublished;
    }
    public void setInputSearchLive(String inputSearch){
        this.inputSearchLive.setValue(inputSearch);
    }
    public ArrayList<CurrencyRate> getRates(){
        return rates;
    }
    public String getLastPublished(){
        return lastPublished;
    }
    public MutableLiveData<String> getInputSearchLive(){
        return inputSearchLive;
    }
    public MutableLiveData<String> getInputAmountLive(){
        return inputAmountLive;
    }
}
