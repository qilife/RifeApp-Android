package com.zappkit.zappid;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.zappkit.zappid.lemeor.MainMenuActivity;
import com.zappkit.zappid.lemeor.RifeApp;
import com.zappkit.zappid.lemeor.base.BaseActivity;
import com.zappkit.zappid.lemeor.main_menu.MainActivity;
import com.zappkit.zappid.lemeor.tools.Constants;
import com.zappkit.zappid.lemeor.tools.SharedPreferenceHelper;
import com.zappkit.zappid.lemeor.tools.Utilities;
import com.zappkit.zappid.views.CustomFontButton;
import com.zappkit.zappid.views.CustomFontEditText;
import com.zappkit.zappid.views.CustomFontTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    CustomFontEditText mEdEmailSignIn,mEdPasswordSignIn;
    CustomFontTextView mBtnSignIn;
    LinearLayout ll_signup;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Please wait.....");
        progressDialog.setCancelable(false);

        mEdEmailSignIn = findViewById(R.id.mEdEmailSignIn);
        mEdPasswordSignIn = findViewById(R.id.mEdPasswordSignIn);

        mBtnSignIn = findViewById(R.id.mBtnSignIn);

        ll_signup = findViewById(R.id.ll_signup);

        ll_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signupIntent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(signupIntent);
            }
        });

        mBtnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEdEmailSignIn.getText().toString().trim().isEmpty())
                {
                    mEdEmailSignIn.setError("This field is required!");
                    mEdEmailSignIn.requestFocus();
                }
                else if(mEdPasswordSignIn.getText().toString().trim().isEmpty())
                {
                    mEdPasswordSignIn.setError("This field is required!");
                    mEdPasswordSignIn.requestFocus();
                }
                else
                {
                    if(Utilities.isNetworkAvailable(LoginActivity.this))
                    {
                        callLoginAPI();
                    }
                    else
                    {
                        Toast.makeText(LoginActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void callLoginAPI() {
        String url = "https://apiadmin.qienergy.ai/api/login";

        progressDialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if(jsonObject!=null) {
                                JSONArray jsonArray = jsonObject.getJSONArray("user");
                                JSONObject jsonObject1 = jsonArray.getJSONObject(0);
                                if (jsonObject1.getInt("fetch_flag")==1) {
                                    progressDialog.dismiss();
                                    SharedPreferenceHelper.getInstance(LoginActivity.this).setBool("islogin", true);
                                    SharedPreferenceHelper.getInstance(LoginActivity.this).set("user_name", mEdEmailSignIn.getText().toString());
                                    if (jsonObject1.getString("is_subscribe").equals("1")) {
                                        SharedPreferenceHelper.getInstance(LoginActivity.this).setBool(Constants.KEY_PURCHASED, true);
                                    }
                                    Intent mainIntent = new Intent(LoginActivity.this, MainMenuActivity.class);
                                    startActivity(mainIntent);
                                    finish();
                                } else {
                                    progressDialog.dismiss();
                                    String rsp_msg = jsonObject1.getString("rsp_msg");
                                    Toast.makeText(LoginActivity.this, rsp_msg, Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this,error.toString(),Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", mEdEmailSignIn.getText().toString().trim());
                params.put("password", mEdPasswordSignIn.getText().toString().trim());
                return params;
            }

        };
        request.setRetryPolicy(new DefaultRetryPolicy(0, 1, 1.0f));
        RifeApp.getInstance().addToRequestQueue(request);
        request.setTag(VolleyLog.TAG);
    }
}