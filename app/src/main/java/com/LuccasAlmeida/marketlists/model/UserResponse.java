package com.LuccasAlmeida.marketlists.model;

import com.google.gson.annotations.SerializedName;

public class UserResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("email")
    private String email;

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}