package cn.xiaoneng.skyeye.bodyspace.model;

/**
 * Created by liangyongheng on 2016/10/24 10:17.
 *
 * 主体空间mdoel
 */
public class BodySpaceModel {

    private String id;

    private String name;

    private int status;

    public BodySpaceModel() {


    }

    public BodySpaceModel(String id, String name, int status) {

        this.id = id;

        this.name = name;

        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
