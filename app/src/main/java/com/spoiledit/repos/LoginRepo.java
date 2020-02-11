package com.spoiledit.repos;

import android.content.Context;

import com.android.volley.VolleyError;
import com.spoiledit.constants.Constants;
import com.spoiledit.constants.Urls;
import com.spoiledit.networks.VolleyProvider;
import com.spoiledit.utils.NetworkUtils;
import com.spoiledit.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class LoginRepo extends RootRepo {
    public static final String TAG = LoginRepo.class.getCanonicalName();

    private Context context;

    public LoginRepo(Context context) {
        this.context = context;

        init(context);
    }

    public void requestLogin(String[] credentials) {
        int api = Constants.Api.USER_SIGN_IN;
        try {
            apiRequestHit(api, "Requesting login...");
            getVolleyProvider().executeUrlEncodedRequest(
                    Urls.USER_SIGN_IN.getUrl(),
                    getParamsMap(api, credentials),
                    new VolleyProvider.OnResponseListener<String>() {
                        @Override
                        public void onSuccess(String response) {

                        }

                        @Override
                        public void onFailure(VolleyError volleyError) {
                            apiRequestFailure(api, NetworkUtils.getDisplayError(volleyError));
                        }
                    }, false, true);

        } catch (Exception e) {
            e.printStackTrace();
            apiRequestFailure(api, StringUtils.getErrorString(e));
        }
    }

    public void requestPassword(String[] values) {
        int api = Constants.Api.FORGOT_PASSWORD;
        try {
            apiRequestHit(api, "Requesting password change...");
            getVolleyProvider().executeUrlEncodedRequest(
                    Urls.FORGOT_PASSWORD.getUrl(),
                    getParamsMap(api, values),
                    new VolleyProvider.OnResponseListener<String>() {
                        @Override
                        public void onSuccess(String response) {

                        }

                        @Override
                        public void onFailure(VolleyError volleyError) {
                            apiRequestFailure(api, NetworkUtils.getDisplayError(volleyError));
                        }
                    }, false, true);

        } catch (Exception e) {
            e.printStackTrace();
            apiRequestFailure(api, StringUtils.getErrorString(e));
        }
    }

    private Map<String, String> getParamsMap(int api, String[] values) {
        Map<String, String> hashMap = new HashMap<>();
        try {
            if (api == Constants.Api.USER_SIGN_IN) {
                hashMap.put("username", values[0]);
                hashMap.put("password", values[1]);

            } else if (api == Constants.Api.FORGOT_PASSWORD) {
                hashMap.put("email", values[0]);
                hashMap.put("password", values[1]);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return hashMap;
    }
}