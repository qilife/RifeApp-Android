package com.zappkit.zappid.lemeor.api.tasks;

import android.content.Context;

import com.zappkit.zappid.lemeor.api.ApiListener;
import com.zappkit.zappid.lemeor.api.models.GetAPKsNewVersionOutput;
import com.zappkit.zappid.lemeor.api.models.GetTokenOutput;

public class GetAPKNewVersionTask extends BaseTask<GetAPKsNewVersionOutput> {

    public GetAPKNewVersionTask(Context context, ApiListener<GetAPKsNewVersionOutput> listener) {
        super(context, listener);
    }

    @Override
    protected GetAPKsNewVersionOutput callApiMethod() throws Exception {
        GetTokenOutput tokenOutput = mApi.getToken();
        mApi.setCredentials("Bearer " + tokenOutput.token);
        return mApi.getAPKsNewVersion();
    }
}
