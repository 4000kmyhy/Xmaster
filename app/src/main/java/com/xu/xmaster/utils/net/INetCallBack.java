package com.xu.xmaster.utils.net;

public interface INetCallBack {
    void success(String response);

    void failed(Throwable throwable);
}
