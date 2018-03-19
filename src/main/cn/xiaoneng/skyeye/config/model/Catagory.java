package cn.xiaoneng.skyeye.config.model;

import cn.xiaoneng.skyeye.config.container.CatagoryContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;


/**
 * @author xy
 * @version 创建时间：2014-4-4 上午10:53:10
 */
public class Catagory {

    private static Logger log = Logger.getLogger(Catagory.class.getName());

    private String id;
    private String name;
    private String pnodeid;

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
    public void setPnodeid(String pnodeid) {
        this.pnodeid = pnodeid;
    }
    public String getPnodeid() {
        return pnodeid;
    }

    /**
     * 从pageid1;pageid2解析到id1名字;id2名字
     * web端格式：首页:全部分类:女装;半身裙;商品详情
     * @param str
     * @return
     */
    public static String paraseCatagory(String siteid,String str,CatagoryContainer container)
    {

        List<String> itemlist = new ArrayList<String>();
        try
        {
            if(str == null || str.length()<=0)
            {
                return str;
            }

            Map<String,Catagory> items = container.getCatagorys(siteid);
            if(items == null)
            {
                return str;
            }

            String[] itemarray = str.split(";");
            if(itemarray == null || itemarray.length<=0)
            {
                return str;
            }

            for (String item : itemarray)
            {
                //短路径 补全 为长路径
                if(!item.contains(":"))
                {
                    String fullCatagory = getFullCatagory(items, item);
                    if(fullCatagory != null && !fullCatagory.isEmpty())
                        item = fullCatagory;
                }

                StringBuffer itemsbf = new StringBuffer();
                String[] strarray = item.split(":");
                if(strarray == null || strarray.length<=0)
                {
                    itemsbf.append(item);
                }
                else
                {
                    int loop = 0;
                    int  count = strarray.length;

                    for (String id : strarray)
                    {
                        if(id == null || id.length()<=0)
                        {
                            continue;
                        }
                        loop ++;

                        Catagory cat = items.get(id);
                        if(cat == null)
                        {
                            if(loop>=count)
                            {
                                itemsbf.append(id);
                            }
                            else
                            {
                                itemsbf.append(id).append(":");
                            }
                            continue;
                        }
                        if(loop>=count)
                        {
                            itemsbf.append(cat.getName());
                        }
                        else
                        {
                            itemsbf.append(cat.getName()).append(":");
                        }


                    }
                }
                String tranfercontent = itemsbf.toString();
                if(tranfercontent == null || tranfercontent.length()<=0)
                {
                    itemsbf.append(item);
                }

                itemlist.add(itemsbf.toString());

            }


        } catch (Exception e) {
            log.warning("Exception "+e.toString() +siteid+" /"+str);
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }
        StringBuffer sbf = new StringBuffer();
        try
        {
            if(itemlist == null || itemlist.size()<=0)
            {
                return str;
            }
            for (String itemstr : itemlist)
            {
                if(itemstr == null || itemstr.length()<=0)
                {
                    continue;
                }
                sbf.append(itemstr).append(";");
            }


        } catch (Exception e) {
            log.warning("Exception "+e.toString() +siteid+" /"+str);
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }
        return sbf.toString().trim();
    }

    public static String getFullCatagory(Map<String,Catagory> items, String nodeId) {

        if(items == null || items.size() <=0)
        {
            return null;
        }

        if(nodeId == null || nodeId.isEmpty())
        {
            return null;
        }

        //String catagoryName = null;
        String nodeids = nodeId;

        try {

            Catagory catagory = items.get(nodeId);
            if(catagory != null)
            {
//				String name = catagory.getName();
//				if(name != null && !name.isEmpty())
//					catagoryName = name;

                String pnodeid = catagory.getPnodeid();
                if(pnodeid == null)
                    return nodeids;

                if(pnodeid.equals("0"))
                {
                    return nodeids;
                }
                else
                {
                    //catagoryName = getFullCatagory(items, pnodeid) + ":" + catagoryName;
                    nodeids = getFullCatagory(items, pnodeid) + ":" + nodeids;
                }
            }


        } catch (Exception e) {
            log.warning("Exception " + e.toString());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }

        return nodeids;
    }

    /**
     * 短路径：C;D 转换为
     * 长路径：A:B:C;A:B:D
     *
     * @param siteid
     * @param catetory  短路径：C;D
     * @return  长路径：A:B:C;A:B:D
     */
    public static String paraseFullCatagory(String siteid, String str,CatagoryContainer container) {

        List<String> itemlist = new ArrayList<String>();
        try
        {
            if(str == null || str.length()<=0)
            {
                return str;
            }

            Map<String,Catagory> items = container.getCatagorys(siteid);
            if(items == null)
            {
                return str;
            }

            String[] itemarray = str.split(";");
            if(itemarray == null || itemarray.length<=0)
            {
                return str;
            }

            for (String item : itemarray)
            {
                if(item.contains(":"))
                    continue;

                String nodeId = Catagory.getNodeIdByName(items, item);
                if(nodeId == null)
                {
                    itemlist.add(nodeId);
                    continue;
                }

                String fullCatagory = Catagory.getFullCatagory(items, nodeId);
                if(fullCatagory == null)
                    itemlist.add(nodeId);
                else
                    itemlist.add(fullCatagory);
            }

        } catch (Exception e) {
            log.warning("Exception "+e.toString() +siteid+" /"+str);
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }
        StringBuffer sbf = new StringBuffer();
        try
        {
            if(itemlist == null || itemlist.size()<=0)
            {
                return str;
            }
            for (String itemstr : itemlist)
            {
                if(itemstr == null || itemstr.length()<=0)
                {
                    continue;
                }
                sbf.append(itemstr).append(";");
            }


        } catch (Exception e) {
            log.warning("Exception "+e.toString() +siteid+" /"+str);
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }
        return sbf.toString().trim();
    }

    private static String getNodeIdByName(Map<String, Catagory> items, String name) {

        if(items == null)
            return null;

        if(name == null)
            return null;

        Catagory catagory = null;

        for(Entry<String, Catagory> entry:items.entrySet())
        {
            catagory = entry.getValue();

            if(catagory == null)
                continue;

            if(catagory.getName().equals(name))
                return catagory.getId();
        }

        return null;
    }
}
