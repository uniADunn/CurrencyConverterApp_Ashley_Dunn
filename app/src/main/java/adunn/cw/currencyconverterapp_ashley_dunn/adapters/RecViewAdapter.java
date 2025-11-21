package adunn.cw.currencyconverterapp_ashley_dunn.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import adunn.cw.currencyconverterapp_ashley_dunn.R;
import adunn.cw.currencyconverterapp_ashley_dunn.rss_currency.CurrencyRate;
import adunn.cw.currencyconverterapp_ashley_dunn.view_models.CurrencyViewModel;

public class RecViewAdapter extends RecyclerView.Adapter<RecViewAdapter.ViewHolder> {


    public interface OnRateClickListener{
        void onRateClick(int position);
    }
    private OnRateClickListener rateClickListener;
    private static final String TAG = "RecViewAdapter";
    private ArrayList<CurrencyRate> dataSet; //container for data in the recycler view
    private CurrencyViewModel currencyVM; //access to the view model

    public RecViewAdapter(CurrencyViewModel vm){
        currencyVM = vm;
        dataSet = new ArrayList<>();
        setHasStableIds(true);
    }
    @Override
    public long getItemId(int position){
        String code = dataSet.get(position).getCountryCode();
        return code == null ? position : code.hashCode();
    }
    public void setRateClickListener(OnRateClickListener listener){
        rateClickListener = listener;
    }
    public void updateData(ArrayList<CurrencyRate> rates) {
        dataSet = rates != null ? rates : new ArrayList<>();
        notifyDataSetChanged();
    }
    // New method to get a CurrencyRate at a specific position
    public CurrencyRate getItem(int position) {
        if (position >= 0 && position < dataSet.size()) {
            return dataSet.get(position);
        }
        return null;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView rcTitle;
        private final TextView rcRate;
        private final TextView rcCode;
        private final ImageView imageFlag;
        private ConstraintLayout recViewLayout;
        private RecViewAdapter adapter; // Reference to the adapter

        public ViewHolder(View v, OnRateClickListener listener, RecViewAdapter adapter){
            super(v);
            this.adapter = adapter; // Initialize the adapter reference

            rcTitle = v.findViewById(R.id.rcTitle);
            rcRate = v.findViewById(R.id.rcRate);
            rcCode = v.findViewById(R.id.rcCode);
            imageFlag = v.findViewById(R.id.imageFlag);
            recViewLayout = v.findViewById(R.id.recViewLayout);

            //define click listener for the viewholders view
            v.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    int position = getAbsoluteAdapterPosition();
                    if(listener != null && position != RecyclerView.NO_POSITION && position < adapter.getItemCount()){
                        listener.onRateClick(position);
                    }
                }
            });
        }
        public TextView getRcTitle(){
            return rcTitle;
        }
        public TextView getRcRate(){
            return rcRate;
        }
        public TextView getRcCode(){
            return rcCode;
        }
        public ImageView getImageFlag(){
            return imageFlag;
        }
        public ConstraintLayout getRecViewLayout(){
            return recViewLayout;
        }

    }
    //create each new item views (this is invoked by the layout manager
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        //create a new view inflating our custom item layout
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.rates_layout, viewGroup, false);
        // Pass the listener AND the adapter instance
        return new ViewHolder(v, rateClickListener, this);
    }
    //replace the contents of a view for one of the rates in the dataset
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position){
        CurrencyRate rate = dataSet.get(position);

        viewHolder.getRcTitle().setText(rate.getTitle());
        viewHolder.getRcRate().setText(rate.getStrRate());
        viewHolder.getRcCode().setText(rate.getCountryCode());
        String flagUrl = rate.getFlagUrl();
        if(flagUrl != null){
            Glide.with(viewHolder.itemView.getContext())
                    .load(flagUrl)
                    .into(viewHolder.getImageFlag());
        }
        else{
            viewHolder.getImageFlag().setImageDrawable(null);
        }


//---------Change colour depending on rate value----------------------------------------------------
        try {

            double rateValue = rate.getRate();
            double lowThresh = currencyVM.getLowThreshold();
            double highThresh = currencyVM.getHighThreshold();

            if(rateValue <= lowThresh){
                viewHolder.getRecViewLayout().setBackgroundColor(viewHolder.itemView.getResources().getColor(R.color.pastel_red));
            }
            else if(rateValue <= highThresh){
                viewHolder.getRecViewLayout().setBackgroundColor(viewHolder.itemView.getResources().getColor(R.color.amber));
            }
            else{
                viewHolder.getRecViewLayout().setBackgroundColor(viewHolder.itemView.getResources().getColor(R.color.pastel_green));
            }

        } catch (Exception e) {
            Log.d("Exception", e.getMessage());
        }
//---------------------------------------------------------------------------------------------------
    }
    //return the size of your dataset
    @Override
    public int getItemCount(){
        return dataSet.size();
    }

}