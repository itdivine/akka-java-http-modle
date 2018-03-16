package cn.xiaoneng.skyeye.collector.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liangyongheng on 2016/8/5 20:45.
 */
public class FieldMessage implements Serializable {

    private List<String> fieldNames = new ArrayList<>();

    private int fieldOperation;

    public FieldMessage(List<String> fieldNames, int fieldOperation) {

        this.fieldNames = fieldNames;

        this.fieldOperation = fieldOperation;
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }

    public void setFieldNames(List<String> fieldNames) {
        this.fieldNames = fieldNames;
    }

    public int getFieldOperation() {
        return fieldOperation;
    }

    public void setFieldOperation(int fieldOperation) {
        this.fieldOperation = fieldOperation;
    }
}
