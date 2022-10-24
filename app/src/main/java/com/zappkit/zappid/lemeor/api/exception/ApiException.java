package com.zappkit.zappid.lemeor.api.exception;

public class ApiException extends HrException {
    // Error code
    private int mErrorCode;
    private String mErrorDesc;
    public static final int NETWORK_ERROR = -1;

    public ApiException(int errorCode, String errorDesc) {
        super(errorDesc);
        mErrorCode = errorCode;
        mErrorDesc = errorDesc;
    }

    public int getErrorCode() { return mErrorCode; }

    public String getErrorDesc() {
        return mErrorDesc;
    }

    @Override
    public String getMessage() {
        return getErrorDesc();
    }
}
