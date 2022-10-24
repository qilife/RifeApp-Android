package com.zappkit.zappid.lemeor.api.tasks;

import android.content.Context;

import com.zappkit.zappid.lemeor.api.ApiListener;
import com.zappkit.zappid.lemeor.api.TaskApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GetFlashSaleTask extends BaseTask<String> {

    public GetFlashSaleTask(Context context, ApiListener<String> listener) {
        super(context, listener);
    }

    @Override
    protected String callApiMethod(){
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String responseString = "";
        try {
            URL url = new URL(TaskApi.TASK_WS_PLASH_SALE);
            connection = (HttpURLConnection)url.openConnection();
            connection.connect();
            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder buffer = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                buffer.append(line).append("\n");
                line = reader.readLine();
            }
            responseString = buffer.toString();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ea) {
                ea.printStackTrace();
            }
        }
        return responseString;
    }
}
