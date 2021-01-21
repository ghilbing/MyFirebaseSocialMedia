package com.hilbing.myfirebasesocialmedia.notifications;

public class Token {
    //An FCM Token or registration token
    String token;

    public Token(String token) {
        this.token = token;
    }

    public Token() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
