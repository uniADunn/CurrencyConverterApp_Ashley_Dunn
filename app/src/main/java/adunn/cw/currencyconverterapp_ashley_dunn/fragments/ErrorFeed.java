package adunn.cw.currencyconverterapp_ashley_dunn.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import adunn.cw.currencyconverterapp_ashley_dunn.MainActivity;
import adunn.cw.currencyconverterapp_ashley_dunn.R;
import adunn.cw.currencyconverterapp_ashley_dunn.threads.RSSCurrency;

public class ErrorFeed extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.error_feed_layout, container, false);
        //currency view model
        setWidgets(v);
        return v;
    }
    //set widgets and update button on click listener
    private void setWidgets(View v) {
        //update data button
        //button to call update for rates data
        Button updateBtn = v.findViewById(R.id.update_button);
        updateBtn.setOnClickListener(v1 -> {
            RSSCurrency.setUrlTest("https://www.fx-exchange.com/gbp/rss.xml");// used for testing no rates available
            ((MainActivity) requireActivity()).updateRssData(false);
            ((MainActivity) requireActivity()).openFragment(new LoadingFrag());
        });
    }
}
