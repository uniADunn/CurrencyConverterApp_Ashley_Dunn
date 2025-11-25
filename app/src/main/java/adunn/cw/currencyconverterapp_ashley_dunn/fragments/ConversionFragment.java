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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //view model
        CurrencyViewModel currencyVM = new ViewModelProvider(requireActivity()).get(CurrencyViewModel.class);
        View v = inflater.inflate(R.layout.conversion_layout, container, false);//set parent fragment view
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        //set child fragments into parent fragment
        transaction.replace(R.id.input_control_container, new InputControlFragment());
        transaction.replace(R.id.rate_details_container, new RateDetailsFragment());
        transaction.commit();
        return v;
    }





}
