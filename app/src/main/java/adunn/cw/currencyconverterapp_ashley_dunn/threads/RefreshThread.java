package adunn.cw.currencyconverterapp_ashley_dunn.threads;

import android.os.Handler;

public class RefreshThread implements Runnable{
    public interface RefreshListener{
        void onRefreshMinute();
    }
    private Handler refreshHandler;
    private int minuteInterval = 60 * 1000;
    private RefreshListener listener;
    private boolean refreshing = false;

    public RefreshThread(Handler refreshHandler, RefreshListener listener){
        this.refreshHandler = refreshHandler;
        this.listener = listener;
    }

    public boolean isRefreshing(){
        return refreshing;
    }
    public void setRefreshing(boolean isRefreshing){
        this.refreshing = isRefreshing;
    }
    public void start(){
        if(refreshing){
            return;
        }
        refreshing = true;
        refreshHandler.postDelayed(this, minuteInterval);

    }
    public void stop(){
        refreshing = false;
        refreshHandler.removeCallbacks(this);
    }
    @Override
    public void run(){
        listener.onRefreshMinute();
        refreshHandler.postDelayed(this, minuteInterval);
    }
}
