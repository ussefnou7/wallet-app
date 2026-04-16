package com.wallet.walletapp.user;

public interface UserReadProjection {

    String getUsername();

    Role getRole();

    String getTenantName();
}
