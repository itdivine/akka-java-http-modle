package cn.xiaoneng.skyeye.util;


import java.lang.reflect.Field;

/**
 * Created by liangyongheng on 2016/12/1 20:24.
 */
public class BeanUtil {

    public static String getTableName(String bean) {

        String tableName;
        try {
            Class clz = Class.forName(bean);
            //得到类名
            Field f = clz.getField("tableName");

            tableName = (String) f.get(clz);

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return tableName;
    }

    public static String getBeanFilesList(String bean) {
        try {
            Class clz = Class.forName(bean);
            Field[] strs = clz.getDeclaredFields();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < strs.length; i++) {
                if (!strs[i].getName().equals("tableName") && !strs[i].getType().equals("List") && !strs[i].getName().equals("id") && !strs[i].getName().contains("pk")) {
                    sb.append(strs[i].getName() + ",");
                }
            }
            sb.deleteCharAt(sb.toString().lastIndexOf(","));
            return sb.toString();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getInsertSql(String bean) {
        String filesList = getBeanFilesList(bean);
        int fl = filesList.split(",").length;
        String wenhao = "";
        for (int i = 0; i < fl; i++) {
            if (i == fl - 1) {
                wenhao = wenhao + "?";
            } else {
                wenhao = wenhao + "?,";
            }
        }
        return "insert into " + getTableName(bean) + "(" + filesList + ") values(" + wenhao + ")";
    }

    public static String getUpdateSql(String bean, String wherePart) {
        String filesList = getBeanFilesList(bean);
        String[] f1 = filesList.split(",");
        String wenhao = "";
        for (int i = 0; i < f1.length; i++) {
            if (i == f1.length - 1) {
                wenhao = wenhao + f1[i] + " = ? ";
            } else {
                wenhao = wenhao + f1[i] + " = ?,";
            }
        }
        return "update " + getTableName(bean) + " set " + wenhao + wherePart;
    }

    public static String getQuerySql(String bean) {

        String sql = "select * from " + getTableName(bean) + " where isdel = ?";

        return sql;
    }

//    public static void main(String[] args) {
////        System.out.println(BeanUtil.getUpdateSql(AuthModel.class.getName(),"where isdel = 0"));
//        System.out.println(BeanUtil.getInsertSql(SourceTypeModel.class.getName()));
//    }
}
