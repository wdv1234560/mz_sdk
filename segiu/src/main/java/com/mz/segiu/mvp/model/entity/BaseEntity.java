package com.mz.segiu.mvp.model.entity;

import java.io.Serializable;

public class BaseEntity<T> implements Serializable {
    public int status;
    public int code;
    public T result;
    public String msg;
    public String message;
    public String requestCode;
    public int page;
    public int pages;
    public int limit;
    public int count;

    public boolean isSuccess(){
        return code==200;
    }
}
