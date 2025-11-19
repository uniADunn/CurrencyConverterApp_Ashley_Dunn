package adunn.cw.currencyconverterapp_ashley_dunn.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import adunn.cw.currencyconverterapp_ashley_dunn.R;
import adunn.cw.currencyconverterapp_ashley_dunn.adapters.RecViewAdapter;
import adunn.cw.currencyconverterapp_ashley_dunn.rss_currency.CurrencyRate;
import adunn.cw.currencyconverterapp_ashley_dunn.view_models.CurrencyViewModel;

public class RatesFragment extends Fragment implements RecViewAdapter.OnRateClickListener{
    private RecyclerView rcRates;
    private RecViewAdapter rcAdapter;
    private CurrencyViewModel currencyVM;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        currencyVM = new ViewModelProvider(requireActivity()).get(CurrencyViewModel.class);
        View v = inflater.inflate(R.layout.rates_recycler_layout, container, false);
        setWidgets(v);
        createRecViewAdapter();
        setLayoutManager();
        updateRecView();
        return v;

    }
    @Override
    public void onRateClick(int position){
        CurrencyRate rate = rcAdapter.getItem(position);
        if (rate != null) {
            currencyVM.setRateSelected(rate);
            currencyVM.setFiltered(true);
        }

        FragmentManager manager = requireActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.main_frame_layout, new ConversionFragment());
        transaction.addToBackStack(null);
        transaction.commit();
        rcRates.setAdapter(rcAdapter);
    }
    private void createRecViewAdapter(){
        rcAdapter = new RecViewAdapter(currencyVM);
        rcAdapter.setRateClickListener(this);
        rcRates.setAdapter(rcAdapter);
    }
    private void setWidgets(View v){
        rcRates = v.findViewById(R.id.rcRates);
    }
    private void setLayoutManager(){
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rcRates.setLayoutManager(llm);
    }
    public void updateRecView(){
        if(rcAdapter != null && currencyVM != null){
            if(currencyVM.getRssFeedData() != null && currencyVM.getRates() != null){
                ArrayList<CurrencyRate> rates = currencyVM.buildRateLists();
                rcAdapter.updateData(rates);
            }


        }
    }
}