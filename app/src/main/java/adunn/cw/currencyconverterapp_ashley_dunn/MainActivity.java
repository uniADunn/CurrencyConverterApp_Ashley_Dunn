package adunn.cw.currencyconverterapp_ashley_dunn;

import android.app.AlertDialog;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;

import adunn.cw.currencyconverterapp_ashley_dunn.fragments.AcknowledgementFragment;
import adunn.cw.currencyconverterapp_ashley_dunn.fragments.ConversionFragment;
import adunn.cw.currencyconverterapp_ashley_dunn.fragments.ErrorFeed;
import adunn.cw.currencyconverterapp_ashley_dunn.fragments.LoadingFrag;
import adunn.cw.currencyconverterapp_ashley_dunn.fragments.RateDetailsFragment;
import adunn.cw.currencyconverterapp_ashley_dunn.fragments.RatesFragment;
import adunn.cw.currencyconverterapp_ashley_dunn.fragments.SearchFragment;
import adunn.cw.currencyconverterapp_ashley_dunn.rss_data.CurrencyRate;
import adunn.cw.currencyconverterapp_ashley_dunn.rss_data.RssFeedData;
import adunn.cw.currencyconverterapp_ashley_dunn.threads.RSSCurrency;
import adunn.cw.currencyconverterapp_ashley_dunn.threads.RefreshThread;
import adunn.cw.currencyconverterapp_ashley_dunn.view_models.CurrencyViewModel;

public class MainActivity extends AppCompatActivity implements SearchFragment.OnSearchListener
{
    private static final int RSS_FEED_DATA_UPDATE = 1;//update message for thread rss feed data
    private static final int RSS_RATES_DATA_UPDATE = 2;//update message for thread rates data
    private static final int RSS_RATE_PROGRESS_UPDATE = 3;//update message for progress updates
    private static final int ERROR_FEED_DATA = 4; //update message for error feed data
    private static final String FILE_NAME = "currency_data.txt"; //file name
    private SearchFragment searchFrag; //search fragment
    private RatesFragment ratesFrag; //rates fragment
    private ErrorFeed errorFeedFrag; //error fragment
    private LoadingFrag loadingFrag; //loading fragment
    private boolean showSearch = false; //flag to show search fragment
    private CurrencyViewModel currencyVM; //currency view model
    private Handler updateUIHandler; //handler for updating UI
    private Toolbar toolbar; //toolbar menu
    private boolean isHorizontal; //flag if in landscape mode
    private RefreshThread refreshThread; //refresh thread
    private boolean rssLoading = false;//flag for rss loading
    private Thread rssThread = null;//rss currency thread



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
        //create the ui update handler
        createUpdateUIHandler();

        Handler refreshHandler = new Handler(Looper.getMainLooper());
        refreshThread = new RefreshThread(refreshHandler, new RefreshThread.RefreshListener(){
            @Override
            public void onRefreshMinute() {
                updateRssData(true);
            }
        });

        isHorizontal = findViewById(R.id.main_frame2_layout) != null;
        currencyVM.setHorizontal(findViewById(R.id.main_frame2_layout)!= null);
        //set toolbar
        setToolbar();
        //create fragments
        createFragments();

    }
    //file operations
    private void fileSave() {
        if (currencyVM.getRates() == null || currencyVM.getRates().isEmpty()) {
            return; //no rates to save
        }
        File dir = getApplication().getFilesDir();
        String path = dir.getAbsolutePath();
        File file = new File(path, FILE_NAME);
        ObjectOutputStream objectOutStream = null;
        FileOutputStream fileOutStream = null;
        try {
            fileOutStream = new FileOutputStream(file);
            objectOutStream = new ObjectOutputStream(fileOutStream);
            //objects to be saved
            objectOutStream.writeObject(currencyVM.getRates());//rates
            objectOutStream.writeObject(currencyVM.getLastPublished());//last published
            objectOutStream.writeObject(currencyVM.getLowThreshold());//low threshold
            objectOutStream.writeObject(currencyVM.getHighThreshold());//high threshold
            //flush)
            objectOutStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (objectOutStream != null) {
                    objectOutStream.close();
                }
                if (fileOutStream != null) {
                    fileOutStream.close();
                }
            } catch (IOException ignored) {

            }
        }
    }
    private void fileLoad() throws FileNotFoundException {
        File dir = getApplication().getFilesDir();
        String path = dir.getAbsolutePath();
        File file = new File(path, FILE_NAME);
        if(!file.exists()){
            return;//file does not exist
        }
        FileInputStream fileInStream;
        ObjectInputStream objectInStream;
        try{
            fileInStream = new FileInputStream(file);
            objectInStream = new ObjectInputStream(fileInStream);

            ArrayList<CurrencyRate> rates = (ArrayList<CurrencyRate>) objectInStream.readObject();
            String lastPublished = (String) objectInStream.readObject();
            double lowThresh = (double) objectInStream.readObject();
            double highThresh = (double) objectInStream.readObject();

            currencyVM.setRates(rates);
            currencyVM.setLastPublished(lastPublished);
            currencyVM.setLowThreshold(lowThresh);
            currencyVM.setHighThreshold(highThresh);
            currencyVM.buildRateLists();
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    //life cycle operations
    @Override
    public void onStart(){
        super.onStart();
        try {
            fileLoad();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        if(currencyVM.getRates() != null && !currencyVM.getRates().isEmpty()){
            openFragment(ratesFrag);
            refreshThread.start();
        }
        else{
            updateRssData(false);
            refreshThread.start();
            welcomeDialogCustom();
        }
    }
    @Override
    public void onPause(){
        super.onPause();
        fileSave();
        refreshThread.stop();
    }
    @Override
    public void onStop(){
        super.onStop();
        fileSave();
        refreshThread.stop();
    }
    @Override
    public void onResume(){
        super.onResume();
        if(currencyVM.getRates() != null && !currencyVM.getRates().isEmpty()){
            openFragment(ratesFrag);
            refreshThread.start();
        }
        else{
            updateRssData(false);
            refreshThread.start();
        }
    }


    //welcome dialog
    private void welcomeDialogCustom(){
        //create dialog builder and inflate custom welcome dialog layout into view
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View WelcomeDialogView = getLayoutInflater().inflate(R.layout.custom_welcome_dialog, null);
        builder.setView(WelcomeDialogView);
        //set title
        builder.setTitle(R.string.string_welcome_dialog_title);
        //create the dialog and set onShow listener
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            //get the dialog object
            AlertDialog dialog1 = (AlertDialog) d;
            //get the text view and set the text
            TextView customDialogText = dialog1.findViewById(R.id.customDialogText);
            customDialogText.setText(R.string.string_welcome_dialog_text);
            //get the button and set the text and onClick listener
            Button OKBtn = dialog1.findViewById(R.id.customDialogButton);
            OKBtn.setText(R.string.string_string_custom_dialog_button_text);
            OKBtn.setOnClickListener(v -> {
                //ok button dismisses dialog
                dialog1.dismiss();
            });
        });
        //show the dialog
        dialog.show();
    }
    //exit dialog
    private void confirmExitDialog(){
        //create dialog builder and  inflate custom exit dialog layout into view
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View exitDialogView = getLayoutInflater().inflate(R.layout.exit_dialog_layout, null);
        builder.setView(exitDialogView);
        //set title
        builder.setTitle("Exit Application");
        //create the dialog and set onShow listener
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            //get the dialog object
            AlertDialog dialog1 = (AlertDialog) d;
            //get yes button and set onClickListener
            Button yesBtn = dialog1.findViewById(R.id.yes_btn);
            yesBtn.setOnClickListener(v -> {
                //yes button: shows toast and closes application
                Toast.makeText(getApplicationContext(),
                        "Exiting Application",
                        Toast.LENGTH_SHORT).show();
                finish();
            });
            //get no button and set onClick listener
            Button noBtn = dialog1.findViewById(R.id.no_btn);
            noBtn.setOnClickListener(v -> {
                //no button: shows toast and dismisses dialog
                Toast.makeText(getApplicationContext(),
                        "Exit Cancelled",
                        Toast.LENGTH_SHORT).show();
                dialog1.dismiss();
            });
        });
        //show dialog
        dialog.show();
    }
    //set toolbar
    private void setToolbar(){
        //set toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
    private void updateToolbar() {
        //get the current fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_frame_layout);
        //check toolbar is not null
        if (toolbar != null) {
            //get menu items search and filter
            MenuItem search = toolbar.getMenu().findItem(R.id.action_search);
            MenuItem filter = toolbar.getMenu().findItem(R.id.action_filterToggle);

            //check the current instance of the fragments
            if (currentFragment instanceof AcknowledgementFragment) {
                //fragment is an acknowledgement fragment
                toolbar.setTitle("Acknowledgements");//set title
                search.setVisible(false);//hide search
                filter.setVisible(false);//hide filter
            } else if (currentFragment instanceof ConversionFragment) {
                //fragment is conversion fragment
                toolbar.setTitle("Conversion");//set title
                search.setVisible(false);//hide search
                filter.setVisible(false);//hide filter
            } else {
                //fragment is not an acknowledgement or conversion fragment
                toolbar.setTitle(R.string.app_name); //set default app name
            }
        }
        //triggers the toolbar to update
        invalidateOptionsMenu();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        //inflate menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        //get the menu items
        MenuItem searchIcon = menu.findItem(R.id.action_search);
        MenuItem filterToggle = menu.findItem(R.id.action_filterToggle);
        //get the current fragment in main frame
        Fragment currentFrag = getSupportFragmentManager().findFragmentById(R.id.main_frame_layout);
        //check current fragment instance
        if(currentFrag instanceof RatesFragment){
            //current fragment is RatesFragment
            //check if filter is on or off
            if(currencyVM.isFiltered()){
                //filter is on
                filterToggle.setTitle("All Rates");//set filter toggle title
                searchIcon.setVisible(false).setEnabled(false);//hide search
            }
            else{
                //filter is off
                filterToggle.setTitle("Common Rates");//set filter toggle title
                searchIcon.setVisible(true).setEnabled(true);//show search
            }
            //update toolbar toggle filter title
            if(toolbar != null) {
                if(currencyVM.isFiltered()){
                    toolbar.setTitle("Common Rates");
                } else {
                    toolbar.setTitle("All Rates");
                }
            }
        } else if(currentFrag instanceof AcknowledgementFragment || currentFrag instanceof ConversionFragment){
            //fragment is either acknowledgement or conversion fragment, both should hide search and filter
            searchIcon.setVisible(false).setEnabled(false);
            filterToggle.setVisible(false).setEnabled(false);
        }
        //update toolbar
        updateToolbar();
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get the menu items
        MenuItem search = toolbar.getMenu().findItem(R.id.action_search);
        MenuItem filter = toolbar.getMenu().findItem(R.id.action_filterToggle);
        MenuItem reload = toolbar.getMenu().findItem(R.id.action_reload);
        MenuItem acknowledgements = toolbar.getMenu().findItem(R.id.action_acknowledgements);
        MenuItem exit = toolbar.getMenu().findItem(R.id.action_exit);
        //check which item was selected
        if (item.getItemId() == search.getItemId()) {//search item
            showSearch = !showSearch; //flip flag
            if (showSearch){
                //if flag is true check if filter is on or off
                if (!currencyVM.isFiltered()) {
                    //flag is false, not showing filtered list, open search fragment
                    openSearchFragment();
                } else {
                    //list is filtered close the search fragment
                    closeFragment(searchFrag);
                }
            }
            else {
                //show search is flipped to false, close the search fragment
                closeFragment(searchFrag);
            }
        }
        else if(item.getItemId() == filter.getItemId()){//filter rates toggle
            boolean isFiltered = currencyVM.isFiltered();//get current filter state
            isFiltered = !isFiltered;//flip state
            currencyVM.setFiltered(isFiltered);//set the state in the viewmodel
            //check the state of filter
            if (isFiltered) {
                //filter is on
                item.setTitle("All Rates");//set title
                toolbar.setTitle("Common Rates");//set toolbar title
                //hide search icon
                search.setVisible(false).setEnabled(false);
                closeFragment(searchFrag);//closes search fragment
                showSearch = false;//sets state
            }
            else {//filter is off
                item.setTitle("Common Rates");//set title
                toolbar.setTitle("All Rates");//set toolbar title
                //show search icon
                search.setVisible(true).setEnabled(true);
            }
            //update the rates fragment
            ratesFrag.updateRecView();
            //update toolbar
            invalidateOptionsMenu();
        }
        else if(item.getItemId() == reload.getItemId()) {
            updateRssData(false);
        }
        else if(item.getItemId() == acknowledgements.getItemId()){
            showSearch = false;//set flag state
            closeFragment(searchFrag);//close search fragment
            //open acknowledgement fragment
            openFragment(new AcknowledgementFragment());
        }
        else if(item.getItemId() == exit.getItemId()){
            //get exit dialog
            confirmExitDialog();
        }
        //update toolbar
        updateToolbar();
        return true;
    }
    //fragments create, open, close
    private void createFragments(){
        searchFrag = new SearchFragment();
        ratesFrag = new RatesFragment();
        errorFeedFrag = new ErrorFeed();
        loadingFrag = new LoadingFrag();
    }
    public void openFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        if (isHorizontal) {
            //landscape mode
            Log.d("Phone Landscape Mode", "In landscape mode");
            // In landscape mode
            if (fragment instanceof ConversionFragment) {
                //right side
                Log.d("conversion fragment", "conversion fragment into main frame 2 layout");
                transaction.replace(R.id.main_frame2_layout, fragment);
            } else if (fragment instanceof RatesFragment) {
                //left side
                Log.d("rates fragment", "rates fragment into main frame layout");
                transaction.replace(R.id.main_frame_layout, ratesFrag);
            } else if (fragment instanceof AcknowledgementFragment || fragment instanceof ErrorFeed || fragment instanceof LoadingFrag) {
                Fragment currentRightFragment = manager.findFragmentById(R.id.main_frame2_layout);
                if (currentRightFragment != null) {
                    transaction.remove(currentRightFragment);
                }
                transaction.replace(R.id.main_frame_layout, fragment);
                transaction.addToBackStack(fragment.getClass().getSimpleName());
            }
        } else {
            //portrait mode
            if(fragment instanceof ErrorFeed){
                transaction.replace(R.id.main_frame_layout, errorFeedFrag);
            }
            else if(fragment instanceof RatesFragment){
                transaction.remove(loadingFrag);
                transaction.replace(R.id.main_frame_layout, ratesFrag);
            }
            else if(fragment instanceof RateDetailsFragment){
                transaction.replace(R.id.main_frame_layout, fragment);
                transaction.addToBackStack("Rate Details Fragment");
            }
            else if(fragment instanceof ConversionFragment){
                transaction.replace(R.id.main_frame_layout, fragment);
                transaction.addToBackStack("Conversion Fragment");
            }
            else if(fragment instanceof AcknowledgementFragment){
                transaction.replace(R.id.main_frame_layout, fragment);
                transaction.addToBackStack("Acknowledgement Fragment");
            }
            else if(fragment instanceof LoadingFrag){
                transaction.replace(R.id.main_frame_layout, loadingFrag, "LoadingFrag");
            }
        }

        //commit the transaction
        transaction.commit();
        //update the toolbar
        invalidateOptionsMenu();
    }
    private void openSearchFragment(){
        //get the fragment manager and transaction
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        //replace the search fragment
        transaction.replace(R.id.searchFragment_container, searchFrag);
        //commit the transaction
        transaction.commit();
    }
    private void closeFragment(Fragment fragment){
        //get the fragment manager and transaction
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        //check which fragment to close
        if(fragment instanceof SearchFragment){
            //fragment is search fragment
            currencyVM.setInputSearchLive("");//set input to empty
            ratesFrag.updateRecView();//update the rates fragment
            transaction.remove(searchFrag);//remove the search fragment
        }
        else if(fragment instanceof LoadingFrag){
            //fragment is loading fragment
            Fragment existingLoadingFrag = manager.findFragmentByTag("LoadingFrag");
            if(existingLoadingFrag != null){
                transaction.remove(existingLoadingFrag);
            }
        }
        //commit transaction
        transaction.commit();
        //trigger toolbar update
        invalidateOptionsMenu();
    }
    //CREATE HANDLER FOR UPDATING UI
    private void createUpdateUIHandler() {
        //create handler
        updateUIHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                //check the message type
                if(msg.what == RSS_FEED_DATA_UPDATE){
                        //set view model data
                        currencyVM.setRssFeedData((RssFeedData) msg.obj);
                        currencyVM.setLastPublished(currencyVM.getRssFeedData().getLastBuildDate());
                }
                //check the message type
                else if(msg.what == RSS_RATE_PROGRESS_UPDATE){
                    //updates the progress bar
                    int progress = msg.arg1;
                    int max = msg.arg2;
                    if (loadingFrag != null) {
                        loadingFrag.setProgressBarMax(max);
                        loadingFrag.setProgress(progress);
                        if(progress == max){
                            //makes toast to show rates data was updated

                            Toast.makeText(getApplicationContext(),
                                            "Rates Updated",
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                }
                else if(msg.what == RSS_RATES_DATA_UPDATE){
                    rssLoading = false;
                    if(msg.obj instanceof ArrayList){
                        ArrayList<CurrencyRate> ratesFromRss = (ArrayList<CurrencyRate>) msg.obj;
                        currencyVM.setRates(ratesFromRss);
                        //get the rates from each Currency rate
                        ArrayList<Double> values = new ArrayList<>();
                        for (CurrencyRate r : ratesFromRss) {
                            values.add(r.getRate());
                        }
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
                        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_frame_layout);
                        if(currentFragment instanceof LoadingFrag){
                            openFragment(ratesFrag);
                        }
                    }
                }
                else if(msg.what == ERROR_FEED_DATA){
                    openFragment(errorFeedFrag);
                }
            }
        };
    }
    //THREAD TO UPDATE RSS DATA
    public synchronized void updateRssData(boolean autoUpdate) {
        if(rssLoading){
            //refresh triggered when rss loading
            if(autoUpdate){
                return;//skip update refresh
            }
            if(rssThread != null && rssThread.isAlive()){
                rssThread.interrupt();
            }
            updateUIHandler.removeMessages(RSS_FEED_DATA_UPDATE);
            updateUIHandler.removeMessages(RSS_RATES_DATA_UPDATE);
            updateUIHandler.removeMessages(RSS_RATE_PROGRESS_UPDATE);
            updateUIHandler.removeMessages(ERROR_FEED_DATA);
        }
        rssLoading = true;
        if(!autoUpdate){
            openFragment(loadingFrag);

        }
        rssThread = new Thread(new RSSCurrency(updateUIHandler));
        rssThread.start();
    }
    //listener for on search
    @Override
    public void onSearch(String query){
        currencyVM.setInputSearchLive(query);
        ratesFrag.updateRecView();
    }
}