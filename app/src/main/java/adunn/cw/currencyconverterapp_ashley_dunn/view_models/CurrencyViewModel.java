package adunn.cw.currencyconverterapp_ashley_dunn.view_models;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.Locale;

import adunn.cw.currencyconverterapp_ashley_dunn.rss_currency.*;
import adunn.cw.currencyconverterapp_ashley_dunn.threads.RSSCurrency;

public class CurrencyViewModel extends ViewModel {
    private RssFeedData rssData;
    private ArrayList<CurrencyRate> rates;//hold rates
    private ArrayList<CurrencyRate> filteredRates; // holds filtered rates
    private String lastPublished;//last published date
    //private String inputSearch; // user query input
    private boolean isFiltered = false; //true if filtered
    private final MutableLiveData<String> inputSearchLive = new MutableLiveData<>();//live user input data: query
    private final MutableLiveData<String> inputAmountLive = new MutableLiveData<>("");//live user input data: amount
    private final MutableLiveData<Boolean> gbpToXLive = new MutableLiveData<>(true);//true if GBP to X (false if X to GBP)

    private final MutableLiveData<CurrencyRate> rateSelectedLive = new MutableLiveData<>();

    public ArrayList<CurrencyRate> buildRateLists(){
        Log.d("currency view model", "buildRateLists: building Rates...");

        ArrayList<CurrencyRate> allRates = (rates != null) ? rates : new ArrayList<>();
        // Start with a list of all rates, or just the common rates if the filter is on
        ArrayList<CurrencyRate> intermediateList = new ArrayList<>();
        if (isFiltered) {
            for (CurrencyRate r : allRates) {
                String code = r.getCountryCode().toUpperCase();
                if ("USD".equals(code) || "EUR".equals(code) || "JPY".equals(code)) {
                    intermediateList.add(r);
                }
            }
        } else {
            intermediateList.addAll(allRates);
        }

        if (inputSearchLive.getValue() != null && !inputSearchLive.getValue().isEmpty()) {
            ArrayList<CurrencyRate> outRates = new ArrayList<>();
            String lowerCaseQuery = inputSearchLive.getValue().toLowerCase();
            for (CurrencyRate r : intermediateList) {
                String title = r.getTitle().toLowerCase();
                String code = r.getCountryCode().toLowerCase();
                if (title.contains(lowerCaseQuery) || code.contains(lowerCaseQuery)) {
                    outRates.add(r);
                }
            }
            sortRatesByCountryCode(outRates);
            return outRates;
        }


        sortRatesByCountryCode(intermediateList);
        return intermediateList;
    }
    private ArrayList<CurrencyRate> sortRatesByCountryCode(ArrayList<CurrencyRate> rates){
        rates.sort((r1,r2)->{
            String c1 = r1.getCountryCode() == null ? "" : r1.getCountryCode();
            String c2 = r1.getCountryCode() == null ? "" : r2.getCountryCode();
            return c1.compareTo(c2);
        });
        return rates;
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
//    public void setFilteredRates(ArrayList<CurrencyRate> filteredRates){
//        this.filteredRates = filteredRates;
//    }
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
