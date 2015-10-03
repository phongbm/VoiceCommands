package com.android.internal.telephony;

public interface ITelephony {
    public abstract boolean endCall();

    public abstract void answerRingingCall();

    public abstract void silenceRinger();

}