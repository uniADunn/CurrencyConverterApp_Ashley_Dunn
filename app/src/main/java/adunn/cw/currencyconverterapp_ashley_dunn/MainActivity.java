package adunn.cw.currencyconverterapp_ashley_dunn;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

import adunn.cw.currencyconverterapp_ashley_dunn.fragments.ErrorFeed;
import adunn.cw.currencyconverterapp_ashley_dunn.fragments.RatesFragment;
import adunn.cw.currencyconverterapp_ashley_dunn.fragments.SearchFragment;
import adunn.cw.currencyconverterapp_ashley_dunn.rss_currency.CurrencyRate;
import adunn.cw.currencyconverterapp_ashley_dunn.rss_currency.RssFeedData;
import adunn.cw.currencyconverterapp_ashley_dunn.threads.RSSCurrency;
import adunn.cw.currencyconverterapp_ashley_dunn.view_models.CurrencyViewModel;

public class MainActivity extends AppCompatActivity implements SearchFragment.OnSearchListener{
    private static final int RSS_FEED_DATA_UPDATE = 1;//update message from thread rss feed data
    private static final int RSS_RATES_DATA_UPDATE = 2;//update message from thread rates data

    private SearchFragment searchFrag; //search fragment
    private RatesFragment ratesFrag; //rates fragment
    private ErrorFeed errorFeedFrag; //error fragment
    private boolean showSearch = false; //flag to show search fragment
    private CurrencyViewModel currencyVM; //currency view model
    private Handler updateUIHandler; //handler for updating UI


    // on create
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //create view model
        currencyVM = new ViewModelProvider(this).get(CurrencyViewModel.class);
        //set toolbar
        setToolbar();
        //create fragments
        createFragments();
        //create the ui update handler
        createUpdateUIHandler();
        //update rss data
        updateRssData();
        openFragment(ratesFrag);
    }
    private void setToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){

        if (item.getItemId() == R.id.action_search) {
            showSearch = !showSearch;
            if(showSearch){

                if(!currencyVM.isFiltered()) {
                    openFragment(searchFrag);
                }
                else{
                    closeFragment(searchFrag);
                }
            }
            else{
                closeFragment(searchFrag);
            }
        }
        else if(item.getItemId() == R.id.action_filterToggle){
            boolean isFiltered = currencyVM.isFiltered();
            isFiltered = !isFiltered;
            currencyVM.setFiltered(isFiltered);

            if (isFiltered) {
                item.setTitle("All Rates");
            } else {
                item.setTitle("Common Rates");
            }
            closeFragment(searchFrag);
            showSearch = false;
            ratesFrag.updateRecView();

        }
        return true;
    }
    private void createFragments(){
        searchFrag = new SearchFragment();
        ratesFrag = new RatesFragment();
        errorFeedFrag = new ErrorFeed();
    }
    private void openFragment(Fragment fragment){
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if(fragment instanceof SearchFragment){
            transaction.replace(R.id.searchFragment_container, searchFrag);
        }
        else if(fragment instanceof RatesFragment){
            transaction.replace(R.id.main_frame_layout, ratesFrag);
        }
        else if(fragment instanceof ErrorFeed){
            transaction.replace(R.id.main_frame_layout, errorFeedFrag);
        }
        transaction.commit();
    }
    private void closeFragment(Fragment fragment){
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if(fragment instanceof SearchFragment){
            currencyVM.setInputSearchLive("");
            ratesFrag.updateRecView();
            transaction.remove(searchFrag);
        }
        transaction.commit();
    }
    //CREATE HANDLER FOR UPDATING UI
    private void createUpdateUIHandler() {
        updateUIHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                //check the message type
                if(msg.what == RSS_FEED_DATA_UPDATE){
                    //set view model data
                    currencyVM.setRssFeedData((RssFeedData) msg.obj);
                    currencyVM.setLastPublished(currencyVM.getRssFeedData().getLastBuildDate());

                    // If error fragment was showing, replace it with rates fragment
//                    if (getSupportFragmentManager().findFragmentById(R.id.main_frame_layout) instanceof ErrorFeed) {
//                        openFragment(ratesFrag);
//                    }
                    //makes toast to show data was updated.
                    Toast.makeText(getApplicationContext(),
                                    "RSS Data Updated",
                                    Toast.LENGTH_SHORT)
                            .show();
                }
                //check the message type
                else if(msg.what == RSS_RATES_DATA_UPDATE){
                    if(msg.obj instanceof ArrayList){
                        currencyVM.setRates((ArrayList<CurrencyRate>)msg.obj);
                        //-----------could do this in the buildRatesList on the View model.--------------
                        for (CurrencyRate r : currencyVM.getRates()) {
                            r.extractTitle();
                            r.extractRate();
                            r.rateConvert();
                        }
                        //--------------------------------------------------------------------------------
                        //update the rates fragment
                        ratesFrag.updateRecView();

                        // If error fragment was showing, replace it with rates fragment
//                        if (getSupportFragmentManager().findFragmentById(R.id.main_frame_layout) instanceof ErrorFeed) {
//                            openFragment(ratesFrag);
//                        }
                        //makes toast to show rates data was updated
                        Toast.makeText(getApplicationContext(),
                                        "Rates Updated",
                                        Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            }
        };
    }
    //THREAD TO UPDATE RSS DATA
    public void updateRssData() {
        Thread t = new Thread(new RSSCurrency(updateUIHandler));
        t.start();
    }
    @Override
    public void onSearch(String query){
        currencyVM.setInputSearchLive(query);
        ratesFrag.updateRecView();
    }
}