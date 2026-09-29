package com.tactive.hooks;

import com.tactive.utils.AuthStateManager;
import io.cucumber.java.BeforeAll;

public class AuthHooks {

    @BeforeAll
    public static void ensureAuthenticated() {
        if (!AuthStateManager.storageStateExists()) {
            AuthStateManager.generateStorageState();
        }
    }
}