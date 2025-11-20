package adunn.cw.currencyconverterapp_ashley_dunn;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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

public class MainActivity extends AppCompatActivity implements SearchFragment.OnSearchListener
{
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
        //openFragment(ratesFrag);
        welcomeDialogCustom();
        openFragment();
        //refresh data
    }
    private void welcomeDialogCustom(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View WelcomeDialogView = getLayoutInflater().inflate(R.layout.custom_welcome_dialog, null);
        builder.setView(WelcomeDialogView);
        builder.setTitle(R.string.string_welcome_dialog_title);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener(){
            @Override
            public void onShow(DialogInterface d){
                AlertDialog dialog = (AlertDialog) d;

                TextView customDialogText = dialog.findViewById(R.id.customDialogText);
                customDialogText.setText(R.string.string_welcome_dialog_text);

                Button dialogBtn = dialog.findViewById(R.id.customDialogButton);
                dialogBtn.setText(R.string.string_string_custom_dialog_button_text);
                dialogBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });
        dialog.show();
    }
    private void setToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);

        if(currencyVM.isFiltered()){
            menu.findItem(R.id.action_filterToggle).setTitle("All Rates");
            menu.findItem(R.id.action_search).setVisible(false).setEnabled(false);
        }
        else{
            menu.findItem(R.id.action_filterToggle).setTitle("Common Rates");
            menu.findItem(R.id.action_search).setVisible(true).setEnabled(true);
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Currency Rates");
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        //check which item was selected
        if (item.getItemId() == R.id.action_search) {//search item
            showSearch = !showSearch; //flip flag
            if(showSearch){//if flag is true
                if(!currencyVM.isFiltered()) {//check if filter toggle not filtering for common
                    openSearchFragment();
                }
                else{
                    //list is filtered close the search fragment
                    closeFragment(searchFrag);
                }
            }
            else{
                //show search is flipped to false, close the search fragment
                closeFragment(searchFrag);
            }
        }
        if(item.getItemId() == R.id.action_filterToggle){//filter rates toggle
            Toolbar toolbar = findViewById(R.id.toolbar);
            MenuItem searchItem = toolbar.getMenu().findItem(R.id.action_search);

            boolean isFiltered = currencyVM.isFiltered();//get current filter state
            isFiltered = !isFiltered;//flip state
            currencyVM.setFiltered(isFiltered);//set the state in the viewmodel

            if (isFiltered) {//filter is on
                item.setTitle("All Rates");
                searchItem.setVisible(false).setEnabled(false);
            } else {//filter is off
                item.setTitle("Common Rates");
                searchItem.setVisible(true).setEnabled(true);
            }

            closeFragment(searchFrag);//closes search fragment
            showSearch = false;//sets state
            ratesFrag.updateRecView();//updates the view
        }
        ratesFrag.updateRecView();
        return true;
    }
    private void createFragments(){
        searchFrag = new SearchFragment();
        ratesFrag = new RatesFragment();
        errorFeedFrag = new ErrorFeed();
    }
    public void openFragment() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if (currencyVM.getRates() == null || currencyVM.getRates().isEmpty()) {
            Log.d("MainActivity", "Displaying error feed fragment.");
            transaction.replace(R.id.main_frame_layout, errorFeedFrag);
        }
        else {
            Log.d("MainActivity", "Displaying rates fragment.");
            ratesFrag.updateRecView();
            transaction.replace(R.id.main_frame_layout, ratesFrag);
        }
        transaction.commit();
    }
    private void openSearchFragment(){
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.searchFragment_container, searchFrag);
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

                    //makes toast to show data was updated.
                    Toast.makeText(getApplicationContext(),
                                    "RSS Data Updated",
                                    Toast.LENGTH_SHORT)
                            .show();
                }
                //check the message type
                else if(msg.what == RSS_RATES_DATA_UPDATE){
                    if(msg.obj instanceof ArrayList){
                        currencyVM.setRates((ArrayList<CurrencyRate>) msg.obj);
                        //-----------could do this in the buildRatesList on the View model.--------------
                        for (CurrencyRate r : currencyVM.getRates()) {
                            r.extractTitle();
                            r.extractRate();
                            r.rateConvert();
                        }
                        //--------------------------------------------------------------------------------
                        //update the rates fragment
                        ratesFrag.updateRecView();
                        openFragment();
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