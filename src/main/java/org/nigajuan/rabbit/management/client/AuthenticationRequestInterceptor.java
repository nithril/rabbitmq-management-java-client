package org.nigajuan.rabbit.management.client;

import retrofit.RequestInterceptor;

import java.util.Base64;

/**
 * Created by nlabrot on 21/11/14.
 */
public class AuthenticationRequestInterceptor implements RequestInterceptor {

    private String authorization;

    public AuthenticationRequestInterceptor(String username, String password) {
        setCredentials(username, password);
    }

    @Override
    public void intercept(RequestInterceptor.RequestFacade requestFacade) {
        requestFacade.addHeader("Authorization", authorization);
    }

    public void setCredentials(String username, String password){
        this.authorization = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }
}
