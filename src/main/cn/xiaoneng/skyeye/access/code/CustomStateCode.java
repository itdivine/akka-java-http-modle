package cn.xiaoneng.skyeye.access.code;

import akka.http.javadsl.model.StatusCode;
import akka.http.javadsl.model.StatusCodes;

/**
 * 全局返回码列表
 *
 * 必须在HttpCodeRegister中注册
 *
 * Created by XuYang on 2017/9/19.
 */
public class CustomStateCode {


    public final static StatusCode NOT_EXSIT = StatusCodes.custom(46001, "OK", "evs not exsit",true,true);
    public final static StatusCode TIMEOUT = StatusCodes.custom(42001, "OK", "request timeout",true,true);




}
