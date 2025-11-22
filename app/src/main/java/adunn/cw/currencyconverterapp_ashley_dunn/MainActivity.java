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
import java.util.Collections;
import adunn.cw.currencyconverterapp_ashley_dunn.fragments.AcknowledgementFragment;
import adunn.cw.currencyconverterapp_ashley_dunn.fragments.ConversionFragment;
import adunn.cw.currencyconverterapp_ashley_dunn.fragments.ErrorFeed;
import adunn.cw.currencyconverterapp_ashley_dunn.fragments.LoadingFrag;
import adunn.cw.currencyconverterapp_ashley_dunn.fragments.RateDetailsFragment;
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
    private static final int RSS_RATE_PROGRESS_UPDATE = 3; // New message type for progress updates
    private SearchFragment searchFrag; //search fragment
    private RatesFragment ratesFrag; //rates fragment
    private ErrorFeed errorFeedFrag; //error fragment
    private LoadingFrag loadingFrag;
    private boolean showSearch = false; //flag to show search fragment
    private CurrencyViewModel currencyVM; //currency view model
    private Handler updateUIHandler; //handler for updating UI
    private Toolbar toolbar;

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
        if(currencyVM.getRates() != null && !currencyVM.getRates().isEmpty()){
            openFragment(ratesFrag);
        }
        else{

            updateRssData();
        }
        welcomeDialogCustom();

    }

    private void updateToolbar() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_frame_layout);
        if (toolbar != null) {
            MenuItem search = toolbar.getMenu().findItem(R.id.action_search);
            MenuItem filter = toolbar.getMenu().findItem(R.id.action_filterToggle);
            if (currentFragment instanceof AcknowledgementFragment) {
                toolbar.setTitle("Acknowledgements");
                search.setVisible(false);
                filter.setVisible(false);
            } else if (currentFragment instanceof ConversionFragment) {
                toolbar.setTitle("Conversion");
                search.setVisible(false);
                filter.setVisible(false);
            } else { // RatesFragment, ErrorFeed, or other default fragments
                toolbar.setTitle(R.string.app_name); // Set to your default app name
            }
        }
        invalidateOptionsMenu();
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
    private void confirmExitDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View exitDialogView = getLayoutInflater().inflate(R.layout.exit_dialog_layout, null);
        builder.setView(exitDialogView);
        builder.setTitle("Exit Application");

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener(){
            @Override
            public void onShow(DialogInterface d) {
                AlertDialog dialog = (AlertDialog) d;
                Button yesBtn = dialog.findViewById(R.id.yes_btn);
                yesBtn.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        Toast.makeText(getApplicationContext(),
                                "Exiting Application",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                Button noBtn = dialog.findViewById(R.id.no_btn);
                noBtn.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        Toast.makeText(getApplicationContext(),
                                "Exit Cancelled",
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
            }
        });
        dialog.show();
    }
    //set toolbar
    private void setToolbar(){
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);

        MenuItem searchIcon = menu.findItem(R.id.action_search);
        MenuItem filterToggle = menu.findItem(R.id.action_filterToggle);
        //get the current fragment in main frame
        Fragment currentFrag = getSupportFragmentManager().findFragmentById(R.id.main_frame_layout);

        if(currentFrag instanceof RatesFragment){
            //check if filter is on or off
            if(currencyVM.isFiltered()){
                //filter is on
                filterToggle.setTitle("All Rates");
                searchIcon.setVisible(false).setEnabled(false);
            }
            else{
                //filter is off
                filterToggle.setTitle("Common Rates");
                searchIcon.setVisible(true).setEnabled(true);
            }
            if(toolbar != null) {
                if(currencyVM.isFiltered()){
                    toolbar.setTitle("Common Rates");
                } else {
                    toolbar.setTitle("All Rates");
                }
            }
        } else if(currentFrag instanceof AcknowledgementFragment || currentFrag instanceof ConversionFragment){
            //hide toolbar search and filter toggle
            searchIcon.setVisible(false).setEnabled(false);
            filterToggle.setVisible(false).setEnabled(false);
        }
        else{

            filterToggle.setTitle("Common Rates"); // Default to common rates if no specific fragment logic
            searchIcon.setVisible(true).setEnabled(true);
            if(toolbar != null) {
                toolbar.setTitle(R.string.app_name); // Default title
            }
        }
        updateToolbar();
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        MenuItem search = toolbar.getMenu().findItem(R.id.action_search);
        MenuItem filter = toolbar.getMenu().findItem(R.id.action_filterToggle);
        //check which item was selected
        if (item.getItemId() == search.getItemId()) {//search item
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
        else if(item.getItemId() == filter.getItemId()){//filter rates toggle
            boolean isFiltered = currencyVM.isFiltered();//get current filter state
            isFiltered = !isFiltered;//flip state
            currencyVM.setFiltered(isFiltered);//set the state in the viewmodel
            //check the state of filter
            if (isFiltered) {//filter is on
                item.setTitle("All Rates");
                toolbar.setTitle("Common Rates");
                //hide search icon
                search.setVisible(false).setEnabled(false);
                closeFragment(searchFrag);//closes search fragment
                showSearch = false;//sets state
            } else {//filter is off
                item.setTitle("Common Rates");
                toolbar.setTitle("All Rates");
                //show search icon
                search.setVisible(true).setEnabled(true);
            }
            ratesFrag.updateRecView();//updates the view
            invalidateOptionsMenu(); // Invalidate to ensure correct state after filter change
        }
        else if(item.getItemId() == R.id.action_reload) {
            updateRssData();
        }
        else if(item.getItemId() == R.id.action_Aknowledgements){
            showSearch = false;
            closeFragment(searchFrag);
            openFragment(new AcknowledgementFragment());
            // Title and menu visibility handled by onBackStackChanged and onCreateOptionsMenu
        }
        else if(item.getItemId() == R.id.action_exit){
            confirmExitDialog();
        }
        updateToolbar();
        return true;
    }
    //create fragments, open fragments, open search fragment, close fragments
    private void createFragments(){
        searchFrag = new SearchFragment();
        ratesFrag = new RatesFragment();
        errorFeedFrag = new ErrorFeed();
        loadingFrag = new LoadingFrag();
    }

    public void openFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if(fragment instanceof ErrorFeed){
            transaction.replace(R.id.main_frame_layout, errorFeedFrag);
        }
        else if(fragment instanceof RatesFragment){
            transaction.remove(loadingFrag);
            transaction.replace(R.id.main_frame_layout, ratesFrag);
        }
        else if(fragment instanceof RateDetailsFragment){
            transaction.replace(R.id.main_frame_layout, new RateDetailsFragment());
            transaction.addToBackStack("Rate Details Fragment");
        }
        else if(fragment instanceof AcknowledgementFragment){
            transaction.replace(R.id.main_frame_layout, fragment);
            transaction.addToBackStack("Acknowledgement Fragment");
        }
        else if(fragment instanceof LoadingFrag){
            transaction.replace(R.id.main_frame_layout, loadingFrag, "LoadingFrag");

        }
        transaction.commit();
        invalidateOptionsMenu();
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
        else if(fragment instanceof LoadingFrag){
            Fragment existingLoadingFrag = manager.findFragmentByTag("LoadingFrag");
            if(existingLoadingFrag != null){
                transaction.remove(existingLoadingFrag);
            }
        }
        transaction.commit();
        invalidateOptionsMenu();
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
                else if(msg.what == RSS_RATE_PROGRESS_UPDATE){
                    int progress = msg.arg1;
                    int max = msg.arg2;
                    if (loadingFrag != null) {
                        loadingFrag.setProgressBarMax(max);
                        loadingFrag.setProgress(progress);
                    }
                }
                else if(msg.what == RSS_RATES_DATA_UPDATE){
                    if(msg.obj instanceof ArrayList){
                        ArrayList<CurrencyRate> ratesFromRss = (ArrayList<CurrencyRate>) msg.obj;
                        currencyVM.setRates(ratesFromRss);

                        ArrayList<Double> values = new ArrayList<>();
                        for (CurrencyRate r : ratesFromRss) { // Iterate through the rates from RSS
                            values.add(r.getRate());
                        }
                        //--------------------------------------------------------------------------------
                        //colour thresholds
                        double lowThresh;
                        double highThresh;
                        if(!values.isEmpty()){
                            Collections.sort(values);
                        }
                        int n = values.size();
                        int lowIndex = n/3;
                        int highIndex = (2* n) / 3;

                        lowThresh = values.get(lowIndex);
                        highThresh = values.get(highIndex);

                        currencyVM.setLowThreshold(lowThresh);
                        currencyVM.setHighThreshold(highThresh);
                        //update the rates fragment
                        ratesFrag.updateRecView();
                        // Replace loading fragment with rates fragment after loading is complete
                        openFragment(ratesFrag);
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
        openFragment(loadingFrag);
        Thread t = new Thread(new RSSCurrency(updateUIHandler));
        t.start();
    }
    //listener for on search
    @Override
    public void onSearch(String query){
        currencyVM.setInputSearchLive(query);
        ratesFrag.updateRecView();
    }
}