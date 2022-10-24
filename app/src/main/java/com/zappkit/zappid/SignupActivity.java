package com.zappkit.zappid;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.zappkit.zappid.lemeor.MainMenuActivity;
import com.zappkit.zappid.lemeor.RifeApp;
import com.zappkit.zappid.lemeor.tools.SharedPreferenceHelper;
import com.zappkit.zappid.lemeor.tools.Utilities;
import com.zappkit.zappid.views.CustomFontEditText;
import com.zappkit.zappid.views.CustomFontTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    CustomFontEditText mEdNameRegister,mEdEmailRegister,mEdPasswordRegister,mEdConfirmPasswordRegister;
    CustomFontTextView mBtnGetStartedRegister;
    LinearLayout ll_login;
    private ProgressDialog progressDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        progressDialog = new ProgressDialog(SignupActivity.this);
        progressDialog.setMessage("Please wait.....");
        progressDialog.setCancelable(false);
        
        mEdNameRegister = findViewById(R.id.mEdNameRegister);
        mEdEmailRegister = findViewById(R.id.mEdEmailRegister);
        mEdPasswordRegister = findViewById(R.id.mEdPasswordRegister);
        mEdConfirmPasswordRegister = findViewById(R.id.mEdConfirmPasswordRegister);

        mBtnGetStartedRegister = findViewById(R.id.mBtnGetStartedRegister);
        ll_login = findViewById(R.id.ll_login);
        
        ll_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mBtnGetStartedRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String pass=mEdPasswordRegister.getText().toString();
                String cpass=mEdConfirmPasswordRegister.getText().toString();

                if(mEdNameRegister.getText().toString().trim().isEmpty())
                {
                    mEdNameRegister.setError("Please enter your name");
                }
                else if(mEdEmailRegister.getText().toString().trim().isEmpty())
                {
                    mEdEmailRegister.setError("Please enter your email");
                }
                else if(mEdPasswordRegister.getText().toString().trim().isEmpty())
                {
                    mEdPasswordRegister.setError("Please enter your password");
                }
                else if(mEdConfirmPasswordRegister.getText().toString().trim().isEmpty())
                {
                    mEdConfirmPasswordRegister.setError("Please enter your password");
                }
                else if(!mEdPasswordRegister.getText().toString().trim().equals(mEdConfirmPasswordRegister.getText().toString().trim()))
                {
                    mEdConfirmPasswordRegister.setError("Please enter your password and confirm password same");
                }
                else {
                    if(Utilities.isNetworkAvailable(SignupActivity.this))
                    {
                        callSignupAPI();
                    }
                    else
                    {
                        Toast.makeText(SignupActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                    }
                }
                
            }
        });
    }

    private void callSignupAPI() {
        String url = "https://apiadmin.qienergy.ai/api/register";

        Log.i("response","res->"+url);

        progressDialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.i("response","res->"+response.toString());
                            JSONObject jsonObject = new JSONObject(response);
                            if(jsonObject!=null) {
                                JSONArray jsonArray = jsonObject.getJSONArray("user");
                                JSONObject jsonObject1 = jsonArray.getJSONObject(0);
                                if (jsonObject1.getInt("fetch_flag")==1) {
                                    progressDialog.dismiss();
                                    Toast.makeText(SignupActivity.this, "Your account register successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                   progressDialog.dismiss();
                                   String rsp_msg = jsonObject1.getString("rsp_msg");
                                   Toast.makeText(SignupActivity.this, rsp_msg, Toast.LENGTH_SHORT).show();
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
                        try {
                            String responseBody = new String(error.networkResponse.data, "utf-8");
                            JSONObject data = new JSONObject(responseBody);
                            JSONArray errors = data.getJSONArray("errors");
                            JSONObject jsonMessage = errors.getJSONObject(0);
                            String message = jsonMessage.getString("message");
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                        } catch (UnsupportedEncodingException errorr) {
                        }
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", mEdEmailRegister.getText().toString().trim());
                params.put("name", mEdNameRegister.getText().toString().trim());
                params.put("password", mEdPasswordRegister.getText().toString().trim());
                params.put("confirm_password", mEdConfirmPasswordRegister.getText().toString().trim());
                params.put("dateofbirth","1985-05-20");
                params.put("gender", "1");
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(0, 1, 1.0f));
        RifeApp.getInstance().addToRequestQueue(request);
        request.setTag(VolleyLog.TAG);
    }
}