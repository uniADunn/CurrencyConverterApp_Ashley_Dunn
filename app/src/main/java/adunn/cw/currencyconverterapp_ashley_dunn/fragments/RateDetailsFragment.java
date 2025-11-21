package adunn.cw.currencyconverterapp_ashley_dunn.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import java.math.BigDecimal;
import java.math.RoundingMode;

import adunn.cw.currencyconverterapp_ashley_dunn.R;
import adunn.cw.currencyconverterapp_ashley_dunn.rss_currency.CurrencyRate;
import adunn.cw.currencyconverterapp_ashley_dunn.view_models.CurrencyViewModel;

public class RateDetailsFragment extends Fragment {
    private CurrencyViewModel currencyVM;
    private TextView codeResult;
    private TextView titleResult;
    private TextView rateResult;
    private TextView exchangeRate;
    private ImageView flagImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.rate_details_layout, container, false);
        currencyVM = new ViewModelProvider(requireActivity()).get(CurrencyViewModel.class);
        setWidgets(v);
        observeVM();
        addFlagImage();
        return v;
    }
    private void addFlagImage(){
        CurrencyRate rate = currencyVM.getRateSelected().getValue();
        if(rate.getFlagUrl() != null){
            Glide.with(this.flagImage)
                    .load(rate.getFlagUrl())
                    .into(flagImage);
        }
    }
    private void observeVM(){
        currencyVM.getRateSelected().observe(getViewLifecycleOwner(), rateSelected -> {

            if (rateSelected != null) {
                codeResult.setText(rateSelected.getCountryCode());
                titleResult.setText(rateSelected.getTitle());
                rateResult.setText(String.valueOf(rateSelected.getRate()));
                updateExchangeRate(rateSelected, currencyVM.getInputAmountLive().getValue());
            } else {
                codeResult.setText("");
                titleResult.setText("");
                rateResult.setText("");
                exchangeRate.setText("--");
            }
        });

        currencyVM.getInputAmountLive().observe(getViewLifecycleOwner(), inputAmount -> {

            CurrencyRate rateSelected = currencyVM.getRateSelected().getValue();
            if (rateSelected != null) {
                updateExchangeRate(rateSelected, inputAmount);
            }
        });

        currencyVM.getGbpToXLive().observe(getViewLifecycleOwner(), isGbpToX -> {

            CurrencyRate rateSelected = currencyVM.getRateSelected().getValue();
            if (rateSelected != null) {
                updateExchangeRate(rateSelected, currencyVM.getInputAmountLive().getValue());
            }
        });
    }

    private void updateExchangeRate(CurrencyRate rateSelected, String inputAmount) {

        if (inputAmount == null || inputAmount.isEmpty()) {
            exchangeRate.setText("--");
            return;
        }

        BigDecimal bdRate = calculateRate(inputAmount, String.valueOf(rateSelected.getRate()));
        if (bdRate.compareTo(BigDecimal.ZERO) <= 0) {
            exchangeRate.setText("--");

        } else {
            if(currencyVM.isGbpToX()){
                exchangeRate.setText(rateSelected.getCountryCode() + " " + bdRate);
            }
            else {
                exchangeRate.setText(  getString(R.string.gbp) +" " + bdRate);
            }

        }
    }

    private BigDecimal calculateRate(String inputAmount, String strRate){
        try {
            BigDecimal input = new BigDecimal(inputAmount);
            BigDecimal rate = new BigDecimal(strRate);

            if (input.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(BigDecimal.ZERO) <= 0) {
                return BigDecimal.ZERO;
            }

            if (currencyVM.isGbpToX()) {
                return input.multiply(rate).setScale(2, RoundingMode.HALF_UP);
            } else {
                return input.divide(rate, 2, RoundingMode.HALF_UP);
            }
        } catch (Exception e) {

            return BigDecimal.ZERO;
        }
    }
    private void setWidgets(View v){
        codeResult = v.findViewById(R.id.rcCode);
        titleResult = v.findViewById(R.id.rcTitle);
        rateResult = v.findViewById(R.id.rcRate);
        exchangeRate = v.findViewById(R.id.rcExchangeRate);
        flagImage = v.findViewById(R.id.imageFlag);
    }

}
