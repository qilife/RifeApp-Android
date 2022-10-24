package com.zappkit.zappid.lemeor.api.tasks;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;

import com.zappkit.zappid.lemeor.api.ApiListener;
import com.zappkit.zappid.lemeor.api.TaskApi;

public abstract class BaseTask<Output> extends AsyncTask<Void,Exception, Output> {
    protected TaskApi mApi;
    private ApiListener<Output> mListener;
    private Exception mException = null;

    public BaseTask(Context context, @Nullable ApiListener<Output> listener) {
        mListener = listener;
        mApi = new TaskApi(context);
    }

    @Override
    @MainThread
    final protected void onPreExecute() {
        if (mListener != null) mListener.onConnectionOpen(this);
    }

    @Override
    final protected Output doInBackground(Void... params) {
        try {
            return callApiMethod();
        } catch (Exception e) {
            mException = e;
            return null;
        }
    }

    @Override
    @MainThread
    final protected void onPostExecute(Output output) {
        if (mListener != null && mException != null)
            mListener.onConnectionError(this, mException);
        else if (mListener != null)
            mListener.onConnectionSuccess(this, output);
    }

    protected abstract Output callApiMethod() throws Exception;
}
