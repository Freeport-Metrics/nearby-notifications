package com.freeportmetrics.nearby.beaconsimulator;

import android.support.annotation.NonNull;

/**
 * Created by skamycki on 02/10/2017.
 */

public class NearbyViewModel {

    private String url;

    private boolean advertising;

    public NearbyViewModel(@NonNull String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isAdvertising() {
        return advertising;
    }

    public void setAdvertising(boolean advertising) {
        this.advertising = advertising;
    }
}
