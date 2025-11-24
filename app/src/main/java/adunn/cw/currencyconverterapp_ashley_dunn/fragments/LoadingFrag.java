package adunn.cw.currencyconverterapp_ashley_dunn.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import adunn.cw.currencyconverterapp_ashley_dunn.R;
import adunn.cw.currencyconverterapp_ashley_dunn.view_models.CurrencyViewModel;

public class LoadingFrag extends Fragment {
    public ProgressBar bar1;//progress bar
    private TextView txtTitleBar;//progress bar title


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.loading_bar_layout, container, false);
        bar1 = v.findViewById(R.id.bar1);
        txtTitleBar = v.findViewById(R.id.txtTitleBar);
        txtTitleBar.setText("Loading Data...");
        bar1.setMin(0);
        return v;
    }
    //set progress bar max (total amount of items in currency feed
    public void setProgressBarMax(int max) {
        if (bar1 != null) {
            bar1.setMax(max);
        }
    }
    //set progress bar
    public void setProgress(int progress) {
        if (bar1 != null) {
            bar1.setProgress(progress);
        }
    }
}
