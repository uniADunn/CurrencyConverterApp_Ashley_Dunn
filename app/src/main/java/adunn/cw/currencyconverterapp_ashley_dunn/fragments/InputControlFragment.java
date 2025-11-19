package adunn.cw.currencyconverterapp_ashley_dunn.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import adunn.cw.currencyconverterapp_ashley_dunn.R;
import adunn.cw.currencyconverterapp_ashley_dunn.rss_currency.CurrencyRate;
import adunn.cw.currencyconverterapp_ashley_dunn.view_models.CurrencyViewModel;

public class InputControlFragment extends Fragment {
    private static final String TAG = "InputControlFragment";
    private EditText amountInput;
    private ToggleButton convertToggle;
    private CurrencyViewModel currencyVM;
    private boolean updatingFromVM = false;

    public InputControlFragment(){}//empty constructor required for fragment

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View v = inflater.inflate(R.layout.input_control_layout, container, false);
        currencyVM = new ViewModelProvider(requireActivity()).get(CurrencyViewModel.class);
        setWidgets(v);
        setListeners();
        observeVM();
        return v;
    }

    private void setListeners(){
        amountInput.addTextChangedListener(new TextWatcher(){
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after){
                if(!updatingFromVM){
                    currencyVM.getInputAmountLive().setValue(s.toString());
                }
            }
            @Override public void afterTextChanged(Editable s){}
        });
        convertToggle.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                currencyVM.setGbpToX(isChecked);
                updateToggleText(currencyVM.getRateSelected().getValue(), isChecked);
            }
        });
    }
    private void observeVM(){
        currencyVM.getInputAmountLive().observe(getViewLifecycleOwner(), amount -> {
            if (amount != null && !amount.equals(amountInput.getText().toString())) {
                updatingFromVM = true;
                amountInput.setText(amount);
                amountInput.setSelection(amount.length());
                updatingFromVM = false;
            }
        });

        currencyVM.getRateSelected().observe(getViewLifecycleOwner(), rate -> {
            updateToggleText(rate, currencyVM.isGbpToX());
        });
    }

    private void setWidgets(View v){
        convertToggle = v.findViewById(R.id.toggleConversion);
        amountInput = v.findViewById(R.id.txtAmount);

        // Set initial state from ViewModel
        String initialAmount = currencyVM.getInputAmountLive().getValue();
        if (initialAmount != null && !initialAmount.isEmpty()) {
            amountInput.setText(initialAmount);
            amountInput.setSelection(initialAmount.length());
            Log.d(TAG, "setWidgets: Initial amount set from VM: " + initialAmount);
        }

        // Set initial toggle state
        convertToggle.setChecked(currencyVM.isGbpToX());
        updateToggleText(currencyVM.getRateSelected().getValue(), currencyVM.isGbpToX());
    }

    private void updateToggleText(CurrencyRate rate, boolean isGbpToX) {
        if (rate != null) {
            String countryCode = rate.getCountryCode();
            if (isGbpToX) {
                convertToggle.setTextOn("GBP --> " + countryCode);
                convertToggle.setTextOff("GBP --> " + countryCode);
            } else {
                convertToggle.setTextOn(countryCode + " --> GBP");
                convertToggle.setTextOff(countryCode + " --> GBP");
            }
            convertToggle.setText(convertToggle.isChecked() ? convertToggle.getTextOn() : convertToggle.getTextOff());
        } else {
            convertToggle.setTextOn("GBP --> X");
            convertToggle.setTextOff("X --> GBP");
            convertToggle.setText(convertToggle.isChecked() ? "GBP --> X" : "X --> GBP");
        }
    }
}
