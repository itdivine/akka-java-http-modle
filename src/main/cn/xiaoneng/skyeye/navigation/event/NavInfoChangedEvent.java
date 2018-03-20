package cn.xiaoneng.skyeye.navigation.event;

import cn.xiaoneng.skyeye.navigation.bean.NavigationSpaceInfo;

import java.io.Serializable;

/**
 * 导航空间信息改变事件
 *
 * 发布时机： 1、导航空间创建时  2、导航空间信息改变时
 *
 * Created by xuyang on 2016/8/16.
 */
public class NavInfoChangedEvent implements Serializable {

    private NavigationSpaceInfo navInfo;

    public NavInfoChangedEvent(NavigationSpaceInfo navInfo) {
        this.navInfo = navInfo;
    }

    public NavigationSpaceInfo getNavInfo() {
        return navInfo;
    }

    public void setNavInfo(NavigationSpaceInfo navInfo) {
        this.navInfo = navInfo;
    }
}
