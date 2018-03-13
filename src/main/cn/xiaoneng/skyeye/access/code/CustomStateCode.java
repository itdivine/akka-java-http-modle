package cn.xiaoneng.skyeye.access.code;

import akka.http.scaladsl.model.StatusCode;
import akka.http.scaladsl.model.StatusCodes;

/**
 * 全局返回码列表
 *
 * Created by XuYang on 2017/9/19.
 */
public class CustomStateCode {


    public final static StatusCode EVS_NOT_EXSIT = StatusCodes.custom(46001, "OK", "evs is not exsit",true,true);
    public final static StatusCode TIMEOUT = StatusCodes.custom(42001, "OK", "request timeout",true,true);




}
