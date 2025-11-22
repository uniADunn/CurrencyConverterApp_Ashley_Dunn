package adunn.cw.currencyconverterapp_ashley_dunn.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import adunn.cw.currencyconverterapp_ashley_dunn.R;

public class AcknowledgementFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.acknowledgements_layout, container, false);
    }

}
