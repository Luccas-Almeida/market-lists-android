package com.LuccasAlmeida.marketlists.model;

import com.google.gson.annotations.SerializedName;

public class HealthResponse {

    @SerializedName("status")
    private String status;

    public String getStatus() {
        return status;
    }
}