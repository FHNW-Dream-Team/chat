package com.orbitrondev.Model;

public class MainModel {
    private ServerModel currentServer = null;
    private LoginModel currentLogin = null;

    public ServerModel getCurrentServer() {
        return currentServer;
    }

    public void setCurrentServer(ServerModel currentServer) {
        this.currentServer = currentServer;
    }

    public LoginModel getCurrentLogin() {
        return currentLogin;
    }

    public void setCurrentLogin(LoginModel currentLogin) {
        this.currentLogin = currentLogin;
    }
}
