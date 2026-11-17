package com.LuccasAlmeida.marketlists.model;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {

    @SerializedName("email")
    private String email;

    @SerializedName("senha")
    private String senha;

    public RegisterRequest(String email, String senha) {
        this.email = email;
        this.senha = senha;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }
}