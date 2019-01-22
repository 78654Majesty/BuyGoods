package com.snatch.goods.util;

/**
 * @author fanglingxiao
 * @date 2019/1/17
 */
public class ApiResult<T> {

    private int resCode;
    private String resMsg;
    private T data;

    private static final String SUCCESS="success";
    private static final String FAIL="fail";

    public int getResCode() {
        return resCode;
    }

    public void setResCode(int resCode) {
        this.resCode = resCode;
    }

    public String getResMsg() {
        return resMsg;
    }

    public void setResMsg(String resMsg) {
        this.resMsg = resMsg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static String getSUCCESS() {
        return SUCCESS;
    }

    public static String getFAIL() {
        return FAIL;
    }


}