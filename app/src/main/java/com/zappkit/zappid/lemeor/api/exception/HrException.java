package com.zappkit.zappid.lemeor.api.exception;

public class HrException extends Exception {
    private static final long serialVersionUID = 1L;
    
    private String mExtra;

    public HrException(String message) {
        super(message);
    }

    public HrException(String message, String extra) {
        super(message);
        mExtra = extra;
    }
    
    public String getExtra() {
        return mExtra;
    }
}
