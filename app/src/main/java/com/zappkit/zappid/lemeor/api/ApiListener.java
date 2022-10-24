package com.zappkit.zappid.lemeor.api;

import com.zappkit.zappid.lemeor.api.tasks.BaseTask;

public interface ApiListener<Output> {
    void onConnectionOpen(BaseTask task);
    void onConnectionSuccess(BaseTask task, Output data);
    void onConnectionError(BaseTask task, Exception exception);
}
