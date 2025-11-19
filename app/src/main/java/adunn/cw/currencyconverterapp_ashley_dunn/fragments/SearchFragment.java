package adunn.cw.currencyconverterapp_ashley_dunn.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import adunn.cw.currencyconverterapp_ashley_dunn.R;
import adunn.cw.currencyconverterapp_ashley_dunn.view_models.CurrencyViewModel;

public class SearchFragment extends Fragment {
    public interface OnSearchListener{
        void onSearch(String query);
    }
    private OnSearchListener searchListener;
    private CurrencyViewModel currencyVM;
    private EditText searchInput;
    private boolean updatingFromVM = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.search_layout, container, false);
        currencyVM = new ViewModelProvider(requireActivity()).get(CurrencyViewModel.class);
        setWidgets(v);
        observeVM();
        watchSearchInput();
        return v;
    }
    private void setWidgets(View v){
        searchInput = v.findViewById(R.id.inputSearch);
        if(currencyVM.getInputSearchLive().getValue() != null){
            searchInput.setText(currencyVM.getInputSearchLive().getValue());
        }else{
            searchInput.setHint("Search Currency...");
        }
    }
    private void observeVM(){
        currencyVM.getInputSearchLive().observe(getViewLifecycleOwner(), query ->{
            if(query == null || query.isEmpty()){
                searchInput.setHint("Search Currency");
            }
            String currentStr = searchInput.getText().toString();
            if(!currentStr.equals(query)){
                updatingFromVM = true;
                searchInput.setText(query);
                searchInput.setSelection(query.length());
                updatingFromVM = false;
            }
        });
    }
    private void watchSearchInput(){
        searchInput.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after){
                if(!updatingFromVM){
                    currencyVM.setInputSearchLive(s.toString());
                    searchListener.onSearch(s.toString());
                }
            }
            @Override
            public void afterTextChanged(android.text.Editable s){}
        });
    }
    public void onAttach(@NonNull Context context){
        super.onAttach(context);
        if(context instanceof OnSearchListener){
            searchListener = (OnSearchListener) context;
        }
        else{
            throw new RuntimeException(context + " Must implement OnSearchListener");
        }
    }
    @Override
    public void onDetach(){
        super.onDetach();
        searchListener = null;
    }
}