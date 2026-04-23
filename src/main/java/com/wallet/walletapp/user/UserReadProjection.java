package com.wallet.walletapp.user;

import java.util.UUID;

public interface UserReadProjection {

    UUID getId();

    String getUsername();

    Role getRole();

    String getTenantName();
}
