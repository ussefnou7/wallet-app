package com.wallet.walletapp.branch;

import java.util.UUID;

public interface BranchReadProjection {

    UUID getBranchId();

    String getName();

    boolean getActive();

    String getTenantName();
}
