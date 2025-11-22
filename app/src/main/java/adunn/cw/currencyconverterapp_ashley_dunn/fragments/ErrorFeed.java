package adunn.cw.currencyconverterapp_ashley_dunn.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import adunn.cw.currencyconverterapp_ashley_dunn.MainActivity;
import adunn.cw.currencyconverterapp_ashley_dunn.R;
import adunn.cw.currencyconverterapp_ashley_dunn.threads.RSSCurrency;
import adunn.cw.currencyconverterapp_ashley_dunn.view_models.CurrencyViewModel;

public class ErrorFeed extends Fragment {
    private TextView errorTxt;
    private Button updateBtn;
    private CurrencyViewModel currencyVM;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.error_feed_layout, container, false);
        currencyVM = new ViewModelProvider(requireActivity()).get(CurrencyViewModel.class);
        setWidgets(v);
        return v;
    }
    private void setWidgets(View v) {
        errorTxt = v.findViewById(R.id.error_message);
        updateBtn = v.findViewById(R.id.update_button);
        updateBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                RSSCurrency.setURLTEST("https://www.fx-exchange.com/gbp/rss.xml");// used for testing no rates available
                ((MainActivity) requireActivity()).updateRssData();
                //((MainActivity) requireActivity()).openFragment();

            }
        });
    }
}
