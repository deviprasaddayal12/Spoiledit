package com.spoiledit.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.spoiledit.R;
import com.spoiledit.constants.Constants;
import com.spoiledit.constants.Status;
import com.spoiledit.utils.PreferenceUtils;
import com.spoiledit.viewmodels.SplashViewModel;

public class SplashActivity extends RootActivity {
    public static final String TAG = SplashActivity.class.getCanonicalName();

    private SplashViewModel splashViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        splashViewModel = ViewModelProviders.of(this).get(SplashViewModel.class);
        setContentView(R.layout.activity_splash);
    }

    @Override
    public void setUpToolBar() {

    }

    @Override
    public void initUi() {

    }

    @Override
    public void initialiseListener() {

    }

    @Override
    public void setData() {

    }

    @Override
    public void addObservers() {
        getNetworkMutable().observe(this, available -> {
            if (available)
                splashViewModel.requestToken();
            else
                showFailure("Seems you don't have internet connectivity! Please ensure 'Internet's On'.");
        });

        splashViewModel.getApiStatusModelMutable().observe(this, apiStatusModel -> {
            if (apiStatusModel.getApi() == Constants.Api.TOKEN) {
                if (apiStatusModel.getStatus() == Status.Request.API_SUCCESS) {
                    PreferenceUtils.saveString(this, PreferenceUtils.KEY_TOKEN, apiStatusModel.getMessage());

                    switch (PreferenceUtils.loginStatus(this)) {
                        case Status.Login.REQUIRE_NOTHING:
                            startActivity(new Intent(this, DashboardActivity.class));
                            finish();
                            break;
                        case Status.Login.REQUIRE_SIGN_IN_NOT_CREDS:
                        case Status.Login.REQUIRE_SIGN_IN_AND_CREDS:
                        case Status.Login.REQUIRE_SIGN_UP:
                            startActivity(new Intent(this, SignInActivity.class));
                            finish();
                            break;
                    }
                } else
                    showFailure(apiStatusModel.getMessage());
            }
        });
    }
}
