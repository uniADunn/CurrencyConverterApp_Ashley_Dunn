package adunn.cw.currencyconverterapp_ashley_dunn.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import adunn.cw.currencyconverterapp_ashley_dunn.R;
import adunn.cw.currencyconverterapp_ashley_dunn.view_models.CurrencyViewModel;

public class ConversionFragment extends Fragment {
    private static final String TAG = "ConversionFragment";
    private CurrencyViewModel currencyVM;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        currencyVM = new ViewModelProvider(requireActivity()).get(CurrencyViewModel.class);
        View v = inflater.inflate(R.layout.conversion_layout, container, false);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.input_control_container, new InputControlFragment());
        transaction.replace(R.id.rate_details_container, new RateDetailsFragment());
        transaction.commit();
        return v;
    }





}
