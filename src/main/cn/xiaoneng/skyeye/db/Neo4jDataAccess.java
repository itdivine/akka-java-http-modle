package cn.xiaoneng.skyeye.db;

import cn.xiaoneng.skyeye.bodyspace.model.BodyNodeModel;
import cn.xiaoneng.skyeye.bodyspace.model.NTBodyNodeModel;
import cn.xiaoneng.skyeye.monitor.Monitor;
import cn.xiaoneng.skyeye.monitor.MonitorCenter;
import cn.xiaoneng.skyeye.navigation.bean.NavNodeInfo;
import cn.xiaoneng.skyeye.track.bean.RecordInfoFull;
import cn.xiaoneng.skyeye.util.ActorNames;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 第一版账号关联
 * Created by xuyang on 2016/9/5.
 */
public class Neo4jDataAccess {

    protected final static Logger log = LoggerFactory.getLogger(Neo4jDataAccess.class);

    private static Monitor monitor = MonitorCenter.getMonitor(cn.xiaoneng.skyeye.monitor.Node.Neo4jDataAccess);

    /**
     * 查询完整主体节点
     * @param labs : 起始主体节点标签  例: 主体空间[:Body:QQ]
     * @param map  Node's properties, like key,value
     */
    public static BodyNodeModel getBodyNodeModel(String labs, Map map) {

        BodyNodeModel model = getBodyNode(labs, map);
        if(model == null) {
            return null;
        }

        model.setNt_id(getRelationNTID(labs, map));
        return model;
    }

    /**
     * 查询完整主体节点
     * @param labs : 起始主体节点标签  例: 主体空间[:Body:QQ]
     * @param map  Node's index properties, Can't contain
     */
    public static NTBodyNodeModel getNTBodyNodeModel(String labs, Map map) {

        NTBodyNodeModel model = (NTBodyNodeModel)getBodyNode(labs, map);
        if(model == null) {
            return null;
        }

        getRelationIDs(model, labs, map);
        return model;
    }

    /**
     * 查询主体节点【不包括关系】
     * @param labs : 节点标签  例: 主体空间[:Body:QQ]
     * @param map  Node's properties, like key,value
     */
    private static BodyNodeModel getBodyNode(String labs, Map map) {

        if ((labs==null || labs.isEmpty()) && (map == null || map.size() == 0)) {
            log.warn("labs and map are empty. ");
            return null;
        }

        Session session = null;
        NTBodyNodeModel model = null;

        long start = System.currentTimeMillis();

        try {
            // cypher 语句
            String cypher = "MATCH (m ";
            if (labs!=null && !labs.isEmpty()) {
                cypher = cypher + labs;
            }

            if(map!=null && map.size()>0) {
                cypher = cypher + " {" + getProperties(map) + "}";
            }
            cypher = cypher + ") RETURN m";

            // cypher log
            log.debug("getBodyNode sql: " + getNodeCypherLog(labs, map));

            // run
            session = Neo4jUtil.getInstance().getSession();
            StatementResult result = session.run(cypher, Values.value(map));

            // db result
            if (result.hasNext()) {
                Record record = result.next();

                model = new NTBodyNodeModel();

                List<Value> list = record.values();

                for (Value value : list) {
                    model.setId((value.get("id").asString()));
                    model.setSiteId(value.get("siteId").asString());
                    model.setCreateTime(value.get("createTime").asLong());
                }

                //log.debug(model.toString());
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        } finally {
            if (session != null)
                session.close();

            long end = System.currentTimeMillis();
            long span = end - start;
            monitor.newReadTime("getBodyNode", span, true);
            if (span > 1000)
                log.debug("setTerminalNode spans " + span);
        }

        return model;
    }

    /**
     * 查询主体关系集合【等价的主体节点nt_id集合】【账号中心】
     * @param labs : 起始主体节点标签  例: 主体空间[:Body:QQ]
     * @param map  Node's properties, like key,value
     */
    private static String getRelationNTID(String labs, Map map) {

        if (labs==null || labs.isEmpty() && (map == null || map.size() == 0)) {
            log.warn("labs and map are empty. ");
            return null;
        }

        Session session = null;
        long start = System.currentTimeMillis();

        try {
            // cypher语句
            String cypher = "MATCH ( m";
            if (labs!=null && !labs.isEmpty()) {
                cypher = cypher + labs;
            }
            if(map!=null && map.size()>0) {
                cypher = cypher + " {" + getProperties(map) + "}";
            }
            cypher = cypher + ")-[:EQUAL]->(n:Body) RETURN n.id";

            // cypher log
            log.debug(getNodeRelationCypherLog(labs, map));

            // run
            session = Neo4jUtil.getInstance().getSession();
            StatementResult result = session.run(cypher, Values.value(map));

            // db result
            if (result.hasNext()) {
                Record record = result.next();
                return record.get("n.id").asString();
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        } finally {
            if (session != null)
                session.close();

            long end = System.currentTimeMillis();
            long span = end - start;
            monitor.newReadTime("getRelationNTID", span, true);
            if (span > 1000)
                log.debug("getRelationNTID spans " + span);
        }

        return null;
    }

    /**
     * 查询主体关系集合【等价的主体节点nt_id集合】
     * @param labs : 起始主体节点标签  例: 主体空间[:Body:QQ]
     * @param map  Node's properties, like key,value
     */
    private static Set<String> getRelationIDs(NTBodyNodeModel model, String labs, Map map) {

        if (labs==null || labs.isEmpty() && (map == null || map.size() == 0)) {
            log.warn("labs and map are empty. ");
            return null;
        }

        Session session = null;
        Set<String> ntSet = new HashSet<String>();
        Set<String> cookieSet = new HashSet<>();
        Map<String,String> accountNumMap = new HashMap<String,String>();

        long start = System.currentTimeMillis();

        try {
            // cypher语句
            String cypher = "MATCH ( m";
            if (labs!=null && !labs.isEmpty()) {
                cypher = cypher + labs;
            }
            if(map!=null && map.size()>0) {
                cypher = cypher + " {" + getProperties(map) + "}";
            }
            cypher = cypher + ")-[:EQUAL]->(n:Body) RETURN n";

            // cypher log
            log.debug(getNodeRelationCypherLog(labs, map));

            // run
            session = Neo4jUtil.getInstance().getSession();
            StatementResult result = session.run(cypher, Values.value(map));

            // Record: 节点
            while (result.hasNext()) {
                Record record = result.next();
                List<Value> list = record.values();
                for(Value value:list) {
                    Node entity = (Node)value.asEntity();
                    Iterable<String> it = entity.labels();
                    Iterator<String> ite = it.iterator();
                    while (ite.hasNext()) {
                        String lab = ite.next();
                        if(lab.equals(ActorNames.BODY)) {
                            continue;
                        }
                        if(lab.equals(ActorNames.NT_BODYSPACE)) {
                            ntSet.add(value.get("id").asString());
                        }
                        else if(lab.equals(ActorNames.COOKIE_BODYSPACE)) {
                            cookieSet.add(value.get("id").asString());
                        }
                        else if(lab.equals(ActorNames.LOGIN_BODYSPACE)) {
                            accountNumMap.put(ActorNames.LOGIN_BODYSPACE, value.get("id").asString());
                        }
                        else if(lab.equals(ActorNames.QQ_BODYSPACE)) {
                            accountNumMap.put(ActorNames.QQ_BODYSPACE, value.get("id").asString());
                        }
                        else if(lab.equals(ActorNames.EMAIL_BODYSPACE)) {
                            accountNumMap.put(ActorNames.EMAIL_BODYSPACE, value.get("id").asString());
                        }
                        else if(lab.equals(ActorNames.PHONE_BODYSPACE)) {
                            accountNumMap.put(ActorNames.PHONE_BODYSPACE, value.get("id").asString());
                        }
                        else if(lab.equals(ActorNames.WX_BODYSPACE)) {
                            accountNumMap.put(ActorNames.WX_BODYSPACE, value.get("id").asString());
                        }
                    }
                }

            }

            model.setRelateNtSet(ntSet);
            model.setAccountNumMap(accountNumMap);
            model.setCookieSet(cookieSet);

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        } finally {
            if (session != null)
                session.close();

            long end = System.currentTimeMillis();
            long span = end - start;
            monitor.newReadTime("getRelationIDs", span, true);
            if (span > 1000)
                log.debug("getRelationNTID spans " + span);
        }

        return ntSet;
    }

    /**
     * 查询主体关联的指定纬度的账号集合
     * @param fromLab : 起始主体节点标签  例: 主体空间 nt
     * @param toLab :   终止主体节点标签  例: 主体空间 qq
     * @param map  Node's properties, like key,value
     */
    private static Set<String> getIDs(String fromLab, String toLab, Map map) {

        if (fromLab==null || fromLab.isEmpty()) {
            log.warn("fromLabs is empty. ");
            return null;
        }

        if (toLab==null || toLab.isEmpty()) {
            log.warn("toLabs is empty. ");
            return null;
        }

        if (map == null || map.size() == 0){
            log.warn("map is empty. ");
            return null;
        }

        Session session = null;
        Set<String> set = new HashSet<String>();

        long start = System.currentTimeMillis();

        try {
            // cypher语句
            String cypher = "MATCH ( m:" + ActorNames.BODY + ":" + fromLab;

            if(map!=null && map.size()>0) {
                cypher = cypher + " {" + getProperties(map) + "}";
            }

            cypher = cypher + ")-[:EQUAL]->(n:" + ActorNames.BODY + ":"  + toLab + ") RETURN n";

            // cypher log
            log.debug(cypher);

            // run
            session = Neo4jUtil.getInstance().getSession();
            StatementResult result = session.run(cypher, Values.value(map));

            // db result
            while (result.hasNext()) {
                Record record = result.next();
                List<Value> list = record.values();
                for(Value value:list) {
                    Node entity = (Node)value.asEntity();
                    Iterable<String> it = entity.labels();
                    Iterator<String> ite = it.iterator();
                    while (ite.hasNext()) {
                        String lab = ite.next();
                        if(lab.equals(ActorNames.BODY)) {
                            continue;
                        }
                        if(lab.equals(toLab)) {
                            set.add(value.get("id").asString());
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        } finally {
            if (session != null)
                session.close();

            long end = System.currentTimeMillis();
            long span = end - start;
            monitor.newReadTime("getIDs", span, true);
            if (span > 1000)
                log.debug("getRelationNTID spans " + span);
        }

        return set;
    }

    /**
     * 查询导航节点
     * @param labs : 节点标签  例: [:Navigation:WEB]
     * @param map  Node's properties, like key,value
     */
    public static NavNodeInfo getNavigationNode(String labs, Map map) {

        if ((labs==null || labs.isEmpty()) && (map == null || map.size() == 0)) {
            log.warn("labs and map are empty. ");
            return null;
        }

        Session session = null;
        NavNodeInfo model = null;

        long start = System.currentTimeMillis();

        try {
            // cypher 语句
            String cypher = "MATCH (m ";
            if (labs!=null && !labs.isEmpty()) {
                cypher = cypher + labs;
            }

            if(map!=null && map.size()>0) {
                cypher = cypher + " {" + getProperties(map) + "}";
            }
            cypher = cypher + ") RETURN m";

            // cypher log
            log.debug(getNodeCypherLog(labs, map));

            // run
            session = Neo4jUtil.getInstance().getSession();
            StatementResult result = session.run(cypher, Values.value(map));

            // db result
            if (result.hasNext()) {
                Record record = result.next();

                model = new NavNodeInfo();

                List<Value> list = record.values();

                for (Value value : list) {
                    model.setSiteId(value.get("siteId").asString());
                    model.setId(value.get("id").asString());
                    model.setCreateTime(value.get("createTime").asLong());
                    Map values = value.asMap();
                    model.setParams(values);
                }

                //log.debug(model.toString());
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        } finally {
            if (session != null)
                session.close();

            long end = System.currentTimeMillis();
            long span = end - start;
            monitor.newReadTime("getNavigationNode", span, true);
            if (span > 1000)
                log.debug("getNavigationNode spans " + span);
        }

        return model;
    }

    /**
     * 查询导航节点被访问次数
     * @param labs : 起始主体节点标签  例: 导航空间[:Navigation:WEB]
     * @param map  Node's properties, like key,value
     */
    private static long getNavNodeVisitedCount(String labs,  Map map) {

        long count = 0;

        if ((labs==null || labs.isEmpty()) && (map == null || map.size() == 0)) {
            log.warn("labs and map are empty. ");
            return count;
        }

        Session session = null;
        Set<String> set = new HashSet<String>();

        long start = System.currentTimeMillis();

        try {
            // cypher语句
            String cypher = "MATCH ( m:Body)-[r:VISIT]->(n";
            if (labs!=null && !labs.isEmpty()) {
                cypher = cypher + labs;
            }
            if(map!=null && map.size()>0) {
                cypher = cypher + " {" + getProperties(map) + "}";
            }
            cypher = cypher + ") RETURN count(r) as count";

            // cypher log
            log.debug(getNodeRelationCypherLog(labs, map));

            // run
            session = Neo4jUtil.getInstance().getSession();
            StatementResult result = session.run(cypher, Values.value(map));

            // db result
            if (result.hasNext()) {
                Record record = result.next();
                count = record.get("count").asLong();
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        } finally {
            if (session != null)
                session.close();

            long end = System.currentTimeMillis();
            long span = end - start;
            monitor.newReadTime("getNavNodeVisitedCount", span, true);
            if (span > 1000)
                log.debug("setTerminalNode spans " + span);
        }

        return count;
    }

    private static String getProperties(Map map) {

        String attrs = "";
        Set keys = map.keySet();
        for(Object key:keys) {
            attrs = attrs + key + ":{" + key + "},";
        }
        attrs = attrs.substring(0,attrs.length()-1);
        return attrs;
    }

    /**
     * n.createTime={createTime}
     * @param map
     * @return
     */
    private static String getSetProperties(String name, Map map) {

        String attrs = "";
        Set keys = map.keySet();
        for(Object key:keys) {
            attrs = attrs + name + "." + key + "={" + key + "},";
        }
        attrs = attrs.substring(0,attrs.length()-1);
        return attrs;
    }

    private static String getNodeCypherLog(String labs, Map map) {

        String cypher = "MATCH (m ";
        if (labs!=null && !labs.isEmpty()) {
            cypher = cypher + labs;
        }

        if(map!=null && map.size()>0) {
            cypher = cypher + " {" + getPropertiesLog(map) + "}";
        }

        cypher = cypher + ") RETURN m";
        return cypher;
    }

    private static String getNodeRelationCypherLog(String labs, Map map) {

        String cypher = "MATCH (m ";
        if (labs!=null && !labs.isEmpty()) {
            cypher = cypher + labs;
        }

        if(map!=null && map.size()>0) {
            cypher = cypher + " {" + getPropertiesLog(map) + "}";
        }

        cypher = cypher + ")-[:EQUAL]->(n:Body) RETURN n";
        return cypher;
    }

    private static String getPropertiesLog(Map map) {

        String attrs = "";
        Set keys = map.keySet();
        for(Object key:keys) {
            attrs = attrs + key + ":'" + map.get(key) + "',";
        }
        attrs = attrs.substring(0,attrs.length()-1);
        return attrs;
    }


    /**
     * 写入一个主体节点
     *
     * 当siteId、nt_id、id值相等，但是spaceName不等时，会生成两个节点
     *
     * MERGE : 表示有则不写，无则写入
     * CREATE：表示一定写入
     *
     * @spaceName 主体空间名字，在Neo4j中为标签，如：QQ
     */
    public static void setBodyNode(String spaceName, Map map) {

        Session session = null;
        long start = System.currentTimeMillis();

        try {
            log.debug("setBodyNode sql: MERGE (n:Body:" + spaceName + " {siteId:'"+map.get("siteId")+"', id:'"+map.get("id")+"'}) " +
                    "ON CREATE SET n.createTime="+map.get("createTime"));

            session = Neo4jUtil.getInstance().getSession();
            session.run("MERGE (n:Body:" + spaceName + " {siteId:{siteId}, id:{id}}) ON CREATE SET n.createTime={createTime}", Values.value(map));

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        } finally {
            if(session != null)
                session.close();

            long end = System.currentTimeMillis();
            long span = end - start;
            monitor.newWriteTime("setBodyNode", span, true);
            if(span > 1000)
            log.debug("setBodyNode spans " + span);
        }
    }

    /**
     * 写入绑定关系【主体节点之间】
     * @param fromSpaceName 主体空间名
     * @param map  key: id nt_id
     */
    public static void setBodyBondRelation(String fromSpaceName, Map map) {

        Session session = null;
        long start = System.currentTimeMillis();

        try {
            log.debug("setBodyBondRelation sql: MATCH (a:Body:" + fromSpaceName + " {siteId:'"+map.get("siteId")+"',id:'"+map.get("id1")+"'}),(b:Body:"
                    + ActorNames.NT_BODYSPACE + " {siteId:'"+map.get("siteId")+"',id:'"+map.get("id2")+"'}) " +
                    "MERGE (a)-[:EQUAL]->(b)" +
                    "MERGE (a)<-[:EQUAL]-(b) ");

            session = Neo4jUtil.getInstance().getSession();
            session.run("MATCH (a:Body:" + fromSpaceName + " {siteId:{siteId},id:{id1}}),(b:Body:" + ActorNames.NT_BODYSPACE + " {siteId:{siteId},id:{id2}}) " +
                    "MERGE (a)-[:EQUAL]->(b)" +
                    "MERGE (a)<-[:EQUAL]-(b)", Values.value(map));

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        } finally {
            if(session != null)
                session.close();

            long end = System.currentTimeMillis();
            long span = end - start;
            monitor.newWriteTime("setBodyBondRelation", span, true);
            if(span > 1000)
                log.debug("setBodyBondRelation spans " + span);
        }
    }

    /**
     * 删除绑定关系【主体节点之间】
     * @param fromSpaceName 主体空间名
     * @param map  key: id nt_id
     */
    public static void deleteBodyBondRelation(String fromSpaceName, Map map) {

        Session session = null;
        long start = System.currentTimeMillis();

        try {
            session = Neo4jUtil.getInstance().getSession();
            session.run("MATCH (a:Body:" + fromSpaceName + " {siteId:{siteId},id:{id1}}),(b:Body:" + ActorNames.NT_BODYSPACE + " {siteId:{siteId},id:{id2}}) " +
                    "MATCH (a)-[r1:EQUAL]->(b)" +
                    "MATCH (a)<-[r2:EQUAL]-(b)" +
                    "delete r1", Values.value(map));

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        } finally {
            if(session != null)
                session.close();

            long end = System.currentTimeMillis();
            long span = end - start;
            monitor.newWriteTime("deleteBodyBondRelation", span, true);
            if(span > 1000)
                log.debug("setBodyBondRelation spans " + span);
        }
    }

    /**
     * 写入一个等价关系【主体节点之间】
     * @param map keys: nt_id1,nt_id2
     */
    public static void setBodyRelation(Map map) {

        Session session = null;
        long start = System.currentTimeMillis();

        try {
            session = Neo4jUtil.getInstance().getSession();
            session.run("MATCH (a:Body:" + ActorNames.NT_BODYSPACE + " {siteId:{siteId},id:{id1}}),(b:Body:" + ActorNames.NT_BODYSPACE + " {siteId:{siteId},id:{id2}}) " +
                    "MERGE (a)-[:EQUAL]->(b)" +
                    "MERGE (a)<-[:EQUAL]-(b)", Values.value(map));

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        } finally {
            if(session != null)
                session.close();

            long end = System.currentTimeMillis();
            long span = end - start;
            monitor.newWriteTime("setBodyRelation", span, true);
            if(span > 1000)
                log.debug("setBodyRelation spans " + span);
        }
    }


    /**
     * 写入一个导航节点
     *
     * 当siteId、nt_id、id值相等，但是spaceName不等时，会生成两个节点
     *
     * MERGE : 表示有则不写，无则写入
     * CREATE：表示一定写入
     *
     * @spaceName 导航空间名字，在Neo4j中为标签，如：LBS
     */
    public static void setNavigationNode(String spaceName, Map map) {

        Session session = null;
        long start = System.currentTimeMillis();

        try {
            // cypher 语句
            String cypher = "MERGE (n:Navigation:" + spaceName + " {siteId:{siteId}, id:{id}}) ON CREATE SET ";

            if(map!=null && map.size()>0) {
                cypher = cypher + " " + getSetProperties("n", map);
            }

            log.debug(cypher);

            session = Neo4jUtil.getInstance().getSession();
            session.run(cypher, Values.value(map));
            //session.run("MERGE (n:Navigation:" + spaceName + " {siteId:{siteId}, id:{id}}) ON CREATE SET n.createTime={createTime}", Values.value(map));

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        } finally {
            if(session != null)
                session.close();

            long end = System.currentTimeMillis();
            long span = end - start;
            monitor.newWriteTime("setNavigationNode", span, true);
            if(span > 1000)
                log.debug("setNavigationNode spans " + span);
        }
    }


    /**
     * 写入1个主体导航关系
     * @param bodyLab
     * @param navLab
     * @param ntId
     * @param navId
     * @param map VISIT关系上的所有属性集合
     */
    public static void setVisitRelation(String bodyLab, String navLab, String ntId, String navId, Map map) {

        Session session = null;
        long start = System.currentTimeMillis();

        try {

            String cypher = "Match (n:Body:"+bodyLab+"{siteId:{siteId},id:{bodyId}}),(m:Navigation:"+navLab+" {siteId:{siteId},id:{navId}})" +
                    "MERGE (n)-[:VISIT{ ";

            if(map!=null && map.size()>0) {
                cypher = cypher + " " + getProperties(map);
            }

            cypher = cypher + " }]->(m)";

            map.put("bodyId", ntId);
            map.put("navId", navId);

            log.debug(cypher + " " + map);

            session = Neo4jUtil.getInstance().getSession();
            session.run(cypher, Values.value(map));


        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        } finally {
            if(session != null)
                session.close();

            //pvCount.getAndIncrement();
            long end = System.currentTimeMillis();
            long span = end - start;
            monitor.newWriteTime("setVisitRelation", span, true);
            if(span > 1000)
                log.debug("setVisitRelation spans " + span);
        }
    }


    /**
     * 分页查找用户轨迹
     * @param map
     * @param skip
     * @param limit
     */
    public static List<String> getSidList(Map map, int skip, int limit) {

        if (map == null || map.size() == 0) {
            log.warn("labs and map are empty. ");
        }

        Session session = null;
        List<String> sidList = new ArrayList<String>();

        long start = System.currentTimeMillis();

        try {
            // cypher 语句
            String cypher = "MATCH (n:Body:nt{siteId:{siteId},id:{id}})-[r:VISIT]-(m:Navigation) " +
                    "RETURN distinct r.sid order by r.sid desc skip " + skip + " limit " + limit;

            // cypher log
            System.out.print("MATCH (n:Body:nt{siteId:'"+map.get("siteId")+"',id:'"+map.get("id")+"'})-[r:VISIT]-(m:Navigation) " +
                    "RETURN distinct r.sid order by r.sid desc skip " + skip + " limit " + limit);

            // run
            session = Neo4jUtil.getInstance().getSession();
            StatementResult result = session.run(cypher, Values.value(map));

            // db result
            while (result.hasNext()) {
                Record record = result.next();
                List<Value> list = record.values();
                for (Value value : list) {
                    String sid = value.asString();
                    sidList.add(sid);
                }
            }

            //result
            log.debug(sidList.toString());


        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        } finally {
            if (session != null)
                session.close();

            long end = System.currentTimeMillis();
            long span = end - start;
            monitor.newReadTime("getSidList", span, true);
            if (span > 1000)
                log.debug("getSidList spans " + span);
        }

        return sidList;

    }

    /**
     * 查询某次来访的轨迹
     * @param map
     * @return
     */
    public static List<RecordInfoFull> getSidTrack(Map map) {

        if (map == null || map.size() == 0) {
            log.warn("labs and map are empty. ");
        }

        Session session = null;
        RecordInfoFull recordInfoFull = null;
        List<RecordInfoFull> recordInfoFullList = new ArrayList<RecordInfoFull>();

        long start = System.currentTimeMillis();

        try {
            // cypher 语句
            String cypher = "MATCH (n:Body:nt{siteId:{siteId},id:{id}})-[r:VISIT{sid:{sid}}]-(m:Navigation) RETURN r,m order by r.time asc";
//            String cypher = "MATCH (n:Body:nt{siteId:{siteId},id:{id}})-[r:VISIT{sid:{sid}}]-(m:Navigation)-[e:EXIST*0..1]-(o:Navigation) RETURN r,m,o order by r.time asc";

            // cypher log
            log.info("MATCH (n:Body:nt { siteId: '"+map.get("siteId")+"',id:'"+map.get("id")+"' })" +
                    "-[r:VISIT{sid:'"+map.get("sid")+"'}]-(m:Navigation) RETURN r,m order by r.time asc");

            // run
            session = Neo4jUtil.getInstance().getSession();
            StatementResult result = session.run(cypher, Values.value(map));

            // db result
            Record record = null;
            while (result.hasNext()) {
                record = result.next();

                Relationship rs = record.get("r").asRelationship();
                Map<String, Object> visitProperties = rs.asMap();
                String siteId = (String)map.get("siteId");

                NavNodeInfo mNodeInfo = getNavNodeInfo(siteId, record, "m");
                recordInfoFull = new RecordInfoFull((String)map.get("id"), mNodeInfo, visitProperties, (Long)visitProperties.get("time"));
                recordInfoFullList.add(recordInfoFull);

                /*NavNodeInfo oNodeInfo = getNavNodeInfo(siteId, record, "o");
                if(mNodeInfo.getId().equals(oNodeInfo.getId())) {
                    recordInfoFull = new RecordInfoFull((String)map.get("id"), mNodeInfo, visitProperties, (Long)visitProperties.get("time"));
                    recordInfoFullList.add(recordInfoFull);
                } else {
                    recordInfoFull = new RecordInfoFull((String)map.get("id"), oNodeInfo, visitProperties, (Long)visitProperties.get("time"));
                    recordInfoFullList.add(recordInfoFull);
                }*/
            }

            log.debug("getSidTrack result = " + recordInfoFullList.size());

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        } finally {
            if (session != null)
                session.close();

            long end = System.currentTimeMillis();
            long span = end - start;
            monitor.newReadTime("getSidTrack", span, true);
            if (span > 1000)
                log.debug("getSidList spans " + span);
        }
        return recordInfoFullList;
    }

    private static NavNodeInfo getNavNodeInfo(String siteId, Record record, String nodeName) {

        if(!record.containsKey(nodeName))
            return null;
        NavNodeInfo navNodeInfo = new NavNodeInfo();
        Node node = record.get(nodeName).asNode();
        Iterator<String> ite = node.labels().iterator();
        while (ite.hasNext()) {
            String lab = ite.next();
            if(!lab.equals("Navigation"))
                navNodeInfo.setSpaceName(lab);
        }
        // node.asMap()有一个bug，会把value的类型从int转换成long
        Map<String, Object> nodeProperties = node.asMap();
        navNodeInfo.setParams(nodeProperties);
        navNodeInfo.setSiteId(siteId);
        navNodeInfo.setId((String)nodeProperties.get("id"));
        navNodeInfo.setCreateTime((Long)nodeProperties.get("createTime"));
        return navNodeInfo;
    }


    /**
     * 查询导航节点被谁访问过
     */
//    public static List<RecordInfo> getRecordByNav(Map map) {
//
//        if (map == null || map.size() == 0) {
//            log.warn("map is empty. ");
//        }
//
//        RecordInfo recordInfo = null;
//        Session session = null;
//        RecordInfoFull recordInfoFull = null;
//        List<RecordInfoFull> recordInfoFullList = new ArrayList<RecordInfoFull>();
//
//        long start = System.currentTimeMillis();
//
//        try {
//            String navSpaceName = (String)map.get("name");
//
//            // cypher 语句
//            String cypher = "MATCH (n:Body:nt})-[r:VISIT]-(m:Navigation:" + navSpaceName + "{siteId:{siteId},id:{id}) RETURN m,n,r order by r.time asc";
//
//            // cypher log
//            log.info("MATCH (n:Body:nt )-[r:VISIT]-" +
//                    "(m:Navigation:" + navSpaceName + "{siteId:'"+map.get("siteId")+"',id:'"+map.get("id")+"'})  RETURN m,n,r order by r.time asc");
//
//            // run
//            session = Neo4jUtil.getInstance().getSession();
//            StatementResult result = session.run(cypher, Values.value(map));
//
//            // db result
//            Record record = null;
//            while (result.hasNext()) {
//                recordInfo = new RecordInfo();
//                record = result.next();
//
//                Relationship rs = record.get("r").asRelationship();
//                Map<String, Object> visitProperties = rs.asMap();
//
//                Node node = record.get("m").asNode();
//                Iterator<String> ite = node.labels().iterator();
//                while (ite.hasNext()) {
//                    String lab = ite.next();
//                    if(!lab.equals("Navigation"))
//                        navNodeInfo.setSpaceName(lab);
//                }
//
//
//                // node.asMap()有一个bug，会把value的类型从int转换成long
//                Map<String, Object> nodeProperties = node.asMap();
//                navNodeInfo.setParams(nodeProperties);
//                navNodeInfo.setSiteId((String)map.get("siteId"));
//                navNodeInfo.setId((String)nodeProperties.get("id"));
//                navNodeInfo.setCreateTime((Long)nodeProperties.get("createTime"));
//
//                recordInfoFull = new RecordInfoFull((String)map.get("id"), navNodeInfo, visitProperties, (Long)visitProperties.get("time"));
//                recordInfoFullList.add(recordInfoFull);
//
//            }
//
//            //result
//            log.debug("getSidTrack result = " + recordInfoFullList.size());
//
//
//        } catch (Exception e) {
//            log.error("Exception " + e.getMessage());
//            StackTraceElement[] er = e.getStackTrace();
//            for (int i = 0; i < er.length; i++) {
//                log.info(er[i].toString());
//            }
//        } finally {
//            if (session != null)
//                session.close();
//
//            long end = System.currentTimeMillis();
//            long span = end - start;
//            monitor.newReadTime("getSidTrack", span, true);
//            if (span > 1000)
//                log.debug("getSidList spans " + span);
//        }
//
//        return recordInfoFullList;
//
//    }

    /**
     * 查找咨询节点和订单节点关系的level属性值
     * @param map
     */
    public static Set<Integer> getLevels(Map map) {

        if (map == null || map.size() == 0) {
            log.warn("labs and map are empty. ");
        }

        Session session = null;
        Set<Integer> keylevelList = new HashSet<Integer>();

        try {
            // cypher 语句
            String cypher = "MATCH (n:Navigation:Order{siteId:{siteId},oi:{oi}})-[r:VISIT]-(m:Navigation:Chat{siteId:{siteId},converid:{converid}}) " +
                    " return distinct r.keylevel" ;

            session = Neo4jUtil.getInstance().getSession();
            StatementResult result = session.run(cypher, Values.value(map));

            // db result
            while (result.hasNext()) {
                Record record = result.next();
                List<Value> list = record.values();
                for (Value value : list) {
                    keylevelList.add(value.asInt());
                }
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        } finally {
            if (session != null)
                session.close();
        }
        return keylevelList;
    }

    /**
     *  创建节点之间的关系
     *  例如：
     *      1. 创建咨询节点和订单节点的关系
     *      2. 创建web节点和订单节点的关系 EXIST
     */
    public static boolean setRelation(String cypher, Map map) {

        if (cypher==null || cypher.isEmpty()) {
            log.warn("cypher is empty.");
            return false;
        }

        if (map==null || map.isEmpty()) {
            log.warn("map is empty.");
            return false;
        }

        Session session = null;

        try {
            session = Neo4jUtil.getInstance().getSession();
            session.run(cypher, Values.value(map));

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.warn(er[i].toString());
            }
        } finally {
            if (session != null)
                session.close();
        }

        return true;
    }

    /**
     *  获取节点之间的关系
     */
    public static Map getRelation(String cypher, Map map) {

        Session session = null;

        try {
            session = Neo4jUtil.getInstance().getSession();
            StatementResult result = session.run(cypher, Values.value(map));
            while (result.hasNext()) {
                Record record = result.next();
                List<Value> list = record.values();
                for (Value value : list) {
                    return value.asMap();
                }
            }
        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.warn(er[i].toString());
            }
        } finally {
            if (session != null)
                session.close();
        }
        return null;
    }

    public static List<NavNodeInfo> getNavNode(String fromLabs, String toLabs, Map fromMap, Map toMap, String relation) {
        return null;
    }

    /**
     *  查找节点关联的节点
     *  例如：
     *      1. 查找订单节点关联的咨询节点
     *      2. 查找订单节点关联的web节点
     *
     */
    public static List<NavNodeInfo> getNavNode(String cypher, Map map) {

        Session session = null;
        NavNodeInfo navNodeInfo = null;
        List<NavNodeInfo> navNodeInfos = null;

        try {
            session = Neo4jUtil.getInstance().getSession();
            StatementResult result = session.run(cypher, Values.value(map));

            while (result.hasNext()) {
                Record record = result.next();
                navNodeInfo = new NavNodeInfo();
                List<Value> list = record.values();
                for (Value value : list) {
                    navNodeInfo.setSiteId(value.get("siteId").asString());
                    navNodeInfo.setId(value.get("id").asString());
                    navNodeInfo.setCreateTime(value.get("createTime").asLong());
                    Map values = value.asMap();
                    navNodeInfo.setParams(values);
                    if(navNodeInfos == null)
                        navNodeInfos = new ArrayList<>();
                    navNodeInfos.add(navNodeInfo);
                }
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        } finally {
            if (session != null)
                session.close();
        }

        return navNodeInfos;
    }

    /**
     * 查询访客在一定时间范围内,最近一次访问导航空间的某个节点
     *  1. 查询访客3天内最近一次下单
     *  2. 查询访客3天内最近一次咨询
     *
     * @param fromLabs : 节点标签  例: [:Navigation:WEB]
     * @param fromLabs : 节点标签  例: [:Navigation:WEB]
     * @param map  Node's properties, like key,value
     * @param time 过去某一时间(时间戳)
     */
    public static NavNodeInfo getRecentlyAccessedNavNode(String fromLabs, String toLabs, Map map, long time) {

        if (fromLabs==null || fromLabs.isEmpty()) {
            log.warn("fromLabs is empty.");
            return null;
        }

        if (toLabs==null || toLabs.isEmpty()) {
            log.warn("toLabs is empty.");
            return null;
        }

        if (map == null || map.size() == 0) {
            log.warn("map is empty.");
            return null;
        }

        String cypher;
        Session session = null;
        NavNodeInfo model = null;

        try {
            // cypher 语句
            cypher = "MATCH (n " + fromLabs + " {" + getProperties(map) + "}"
                    + ")-[r:VISIT]-(m" + toLabs + ") WHERE m.createTime> "
                    + time + " RETURN m order by m.createTime desc limit 1";

            // run
            session = Neo4jUtil.getInstance().getSession();
            StatementResult result = session.run(cypher, Values.value(map));

            // db result
            if (result.hasNext()) {
                Record record = result.next();
                model = new NavNodeInfo();
                List<Value> list = record.values();
                for (Value value : list) {
                    model.setSiteId(value.get("siteId").asString());
                    model.setId(value.get("id").asString());
                    model.setCreateTime(value.get("createTime").asLong());
                    Map values = value.asMap();
                    model.setParams(values);
                }
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        } finally {
            if (session != null)
                session.close();
        }
        return model;
    }


    /*public static void testChatOrder() {

        String siteId = "kf_1000";
        String ntId = "f9d8f591-4149-4cbd-aa3d-aa247edb2ed7";
        String ntLabs = ":Body:nt";
        String orderLabs = ":Navigation:Order";
        String chatLabs = ":Navigation:Chat";
        long time = System.currentTimeMillis() - 3*24*60*60*1000;
        time = 1508573997840L; //demo时间 @test

        //1.查询访客3天内最近一次下单
//        HashMap<String,Object> ntMap = new HashMap();ntMap.put("siteId",siteId);ntMap.put("id",ntId);
//        NavNodeInfo orderInfo = Neo4jDataAccess_source.getRecentlyAccessedNavNode(ntLabs, orderLabs, ntMap, time);
//        System.out.println(orderInfo.toString());

        //2.查找订单节点关联的咨询节点
        HashMap<String,Object> orderMap = new HashMap();orderMap.put("siteId",siteId);orderMap.put("oi","XFSH15138445419331");
//        NavNodeInfo chatInfo = Neo4jDataAccess_source.getNavNode(orderLabs, chatLabs, orderMap, null, "CP");
//        System.out.println(chatInfo.toString());

        //3.创建咨询节点和订单节点的关系
        HashMap<String,Object> chatMap = new HashMap(); chatMap.put("siteId",siteId);chatMap.put("converid","converid123456");
        HashMap<String,Object> relationMap = new HashMap(); relationMap.put("keylevel",3);relationMap.put("time",System.currentTimeMillis());
//        Neo4jDataAccess.setRelation(orderLabs, chatLabs, orderMap, chatMap, "CP", relationMap);
//        System.out.println(chatInfo.toString());

        //4.查找订单节点和nt节点的关系
        Map list = Neo4jDataAccess.getRelation(ntLabs, orderLabs, null,orderMap, "VISIT", null );
        System.out.println(list);
    }*/

    /**
     * 初始化本地Neo4j
     */
    public static void init() {
        //rd
        String url = "bolt://192.168.30.230:7687";
        String userName = "neo4j";
        String password = "xuyang";
        int maxSession = 200;
        Neo4jUtil.getInstance().init(url, maxSession, userName, password);
    }

    public static void testGetSidTrack() {

        HashMap<String,Object> map = new HashMap();
        map.put("siteId","kf_1000");
        map.put("id","71a46473-d85c-40cc-94ff-3762d56571646");
        map.put("sid","1513844537417672");
        List<RecordInfoFull> recordInfoFullList = getSidTrack(map);
        for(RecordInfoFull recordInfoFull:recordInfoFullList) {
            System.out.println(recordInfoFull.toString());
        }
    }

    public static void main(String[] args){

        //test:域名会报错，ip正常
//        String url = "bolt://192.168.30.215:10015";
//        String userName = "neo4j";
//        String password = "111111";

        init();

        //1.测试咨询订单
//        testChatOrder();

        //2.查询访客某次来访轨迹
        //testGetSidTrack();


//        HashMap<String,Object> map = new HashMap();
//        String labs = "";

        //0.查询一个访客的轨迹
//        int skip = 0;
//        int limit = 5;
//        map.put("siteId","kf_1000");
//        map.put("id","kf_1000_ISME9754_guestTEMPBD42-DDB1-BD");
//        List<String> sidList = Neo4jDataAccess_source.getSidList(map, skip, limit);
//        if(sidList != null && sidList.size()>0) {
//            for(String sid:sidList) {
//                map.put("sid", sid);
//                getSidTrack(map);
//            }
//        }




        // 账号中心专用
        //1.任意账号查询nt
//         MATCH (m :Body:cookie {siteId:'kf_1000',id:'guestF46A9B91-6208-9C46-997B-71880CCC6A53'})-[:EQUAL]->(n:Body) RETURN n
//        String cookie = "111AEE-FEC2E569-E20D-2732-102D-ECCC7520B955";
//        labs = ":Body:" + ActorNames.COOKIE_BODYSPACE;
//        map.put("siteId", "kf_3004");
//        map.put("id", cookie);
//        String nt = Neo4jDataAccess_source.getRelationNTID(labs, map);
//        System.out.print("账号中心专用1  " + nt);

//
//        //2.ntId查询关联的cookie账号
//        nt = "N1";
//        String fromLabs = ActorNames.NT_BODYSPACE;
//        String toLabs = ActorNames.COOKIE_BODYSPACE;
//        map.put("id", nt);
//        Set<String> set = Neo4jDataAccess_source.getIDs(fromLabs, toLabs, map);
//        log.debug("账号中心专用2  " + set.toString());



        // 1.查询主体节点(8个纬度)
//        labs = ":Body:" + ActorNames.COOKIE_BODYSPACE;
//        map.clear();
//        map.put("siteId", "kf_0001");
//        map.put("id", "C1");
//
//        BodyNodeModel model = Neo4jDataAccess_source.getBodyNodeModel(labs, map);
//        if(model!=null)
//            log.debug(model.toString());

        // 查询NT节点
//        labs = ":Body:" + ActorNames.NT_BODYSPACE;
//        map.clear();
//        map.put("siteId", "kf_0001");
//        map.put("id", "N1");
//
//        NTBodyNodeModel model = Neo4jDataAccess_source.getNTBodyNodeModel(labs, map);
//        if(model!=null)
//            log.debug(model.toString());

//
//
//        // 2.查询导航节点
//        labs = ":Navigation:WEB";
//        map.clear();
//        map.put("siteId", "kf_55");
//        map.put("id", "105");
//        NavNodeInfo info = Neo4jDataAccess_source.getNavigationNode(labs, map);
//        if(info!=null)
//        {
//            long count = Neo4jDataAccess_source.getNavNodeVisitedCount(labs, map);
//            info.setVisitedCount(count);
//            log.debug(info.toString());
//        }
//
//        // 3.创建主体节点和关系
        //3-1.游客来访  C1-N1
        /*map.clear();
        map.put("siteId", "kf_0001");
        map.put("id", "C1");
        map.put("createTime", System.currentTimeMillis());
        Neo4jDataAccess_source.setBodyNode(ActorNames.COOKIE_BODYSPACE, map);

        map.clear();
        map.put("siteId", "kf_0001");
        map.put("id", "N1");
        map.put("createTime", System.currentTimeMillis());
        Neo4jDataAccess_source.setBodyNode(ActorNames.NT_BODYSPACE, map);

        map.clear();
        map.put("siteId", "kf_0001");
        map.put("id1", "C1");
        map.put("id2", "N1");
        Neo4jDataAccess_source.setBodyBondRelation(ActorNames.COOKIE_BODYSPACE, map);

        //3-2.C1登录L1  L1-N1
        map.clear();
        map.put("siteId", "kf_0001");
        map.put("id", "L1");
        map.put("createTime", System.currentTimeMillis());
        Neo4jDataAccess_source.setBodyNode(ActorNames.LOGIN_BODYSPACE, map);

        map.clear();
        map.put("siteId", "kf_0001");
        map.put("id1", "L1");
        map.put("id2", "N1");
        Neo4jDataAccess_source.setBodyBondRelation(ActorNames.LOGIN_BODYSPACE, map);

        //3-2.C1登录L2  L2-N2 C1-N2 解除绑定C1-N1
        map.clear();
        map.put("siteId", "kf_0001");
        map.put("id", "L2");
        map.put("createTime", System.currentTimeMillis());
        Neo4jDataAccess_source.setBodyNode(ActorNames.LOGIN_BODYSPACE, map);

        map.clear();
        map.put("siteId", "kf_0001");
        map.put("id", "N2");
        map.put("createTime", System.currentTimeMillis());
        Neo4jDataAccess_source.setBodyNode(ActorNames.NT_BODYSPACE, map);

        map.clear();
        map.put("siteId", "kf_0001");
        map.put("id1", "L2");
        map.put("id2", "N2");
        Neo4jDataAccess_source.setBodyBondRelation(ActorNames.LOGIN_BODYSPACE, map);

        map.clear();
        map.put("siteId", "kf_0001");
        map.put("id1", "C1");
        map.put("id2", "N2");
        Neo4jDataAccess_source.setBodyBondRelation(ActorNames.COOKIE_BODYSPACE, map);
*/
//        map.clear();
//        map.put("siteId", "kf_0001");
//        map.put("id1", "C1");
//        map.put("id2", "N1");
//        Neo4jDataAccess_source.deleteBodyBondRelation(ActorNames.COOKIE_BODYSPACE, map);

//        map.clear();
//        map.put("siteId", "kf_3004");
//        map.put("id1", "cookie_111");
//        map.put("id2", "NT_222");
//        Neo4jDataAccess_source.deleteBodyBondRelation(ActorNames.COOKIE_BODYSPACE, map);
/*
        //3-3.C2游客  C2-N3
        map.clear();
        map.put("siteId", "kf_0001");
        map.put("id", "C2");
        map.put("createTime", System.currentTimeMillis());
        Neo4jDataAccess_source.setBodyNode(ActorNames.COOKIE_BODYSPACE, map);

        map.clear();
        map.put("siteId", "kf_0001");
        map.put("id", "N3");
        map.put("createTime", System.currentTimeMillis());
        Neo4jDataAccess_source.setBodyNode(ActorNames.NT_BODYSPACE, map);

        map.clear();
        map.put("siteId", "kf_0001");
        map.put("id1", "C2");
        map.put("id2", "N3");
        Neo4jDataAccess_source.setBodyBondRelation(ActorNames.COOKIE_BODYSPACE, map);

        //3-4.C2登录L1  C2-N1,N1-N2 解绑C2-N3
        map.clear();
        map.put("siteId", "kf_0001");
        map.put("id1", "C2");
        map.put("id2", "N1");
        Neo4jDataAccess_source.setBodyBondRelation(ActorNames.COOKIE_BODYSPACE, map);

        map.clear();
        map.put("siteId", "kf_0001");
        map.put("id1", "C2");
        map.put("id2", "N3");
        Neo4jDataAccess_source.deleteBodyBondRelation(ActorNames.COOKIE_BODYSPACE, map);

        map.clear();
        map.put("siteId", "kf_0001");
        map.put("id1", "N1");
        map.put("id2", "N3");
        Neo4jDataAccess_source.setBodyRelation(map);
*/


        // 4.创建导航节点
//        map.clear();
//        map.put("siteId", "kf_0001");
//        map.put("id", "中国河北唐山".hashCode() + "");
//        map.put("country", "中国");
//        map.put("province", "河北");
//        map.put("city", "唐山");
//        Neo4jDataAccess_source.setNavigationNode("LBS", map);

        // 5.创建主体导航关系
//        map.clear();
//        map.put("siteId", "kf_0001");
//        map.put("name", "divine");
//        map.put("sid", "1472709659677611");
//        map.put("createtime", 1472702590686L);
//        Neo4jDataAccess_source.setVisitRelation("QQ", "LBS", "123530015", "1665735573", map);

        // 6.一步创建 3~5 中所有节点和关系





    }

    public static void setRelation(String orderLabs, String s, HashMap<String, Object> orderMap, HashMap<String, Object> chatMap, String cp, HashMap<String, Object> relationMap) {
    }

    public static Map getRelation(String ntLabs, String orderLabs, Object o, HashMap<String, Object> orderMap, String visit, Object o1) {
        return null;
    }
}

/*
public static void setWebNode(Map map) {

        Session session = null;
        long start = System.currentTimeMillis();

        try {
            session = Neo4jUtil.getInstance().getSession();

            session.run("MERGE (web:Navigation:WEB { siteId:{siteId},url_id:{url_id}, title:{title}, url:{url}, level:{level} })", Values.value(map));

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        } finally {
            if(session != null)
                session.close();

            long end = System.currentTimeMillis();
            long span = end - start;
            if(span > 1000)
                log.debug("setWebNode spans " + span);
        }
    }


    public static void setLBSNode(Map map) {

        Session session = null;
        long start = System.currentTimeMillis();

        try {
            session = Neo4jUtil.getInstance().getSession();

            session.run("MERGE (lbs:Navigation:LBS {siteId:{siteId}, country:{country}, city:{city}, province:{province}, lng:{lng}, lat:{lat}})", Values.value(map));

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        } finally {
            if(session != null)
                session.close();

            long end = System.currentTimeMillis();
            long span = end - start;
            if(span > 1000)
                log.debug("setLBSNode spans " + span);
        }
    }

    public static void setTerminalNode(Map map) {

        Session session = null;
        long start = System.currentTimeMillis();

        try {
            session = Neo4jUtil.getInstance().getSession();

            session.run("MERGE (terminal:Navigation:Terminal {siteId:{siteId}, system:{system}, device:{device}, flash:{flash}," +
                    " language:{language}, screensize:{screensize}, browser:{browser}})", Values.value(map));

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        } finally {
            if(session != null)
                session.close();

            long end = System.currentTimeMillis();
            long span = end - start;
            if(span > 1000)
                log.debug("setTerminalNode spans " + span);
        }
    }

    public static void setSourceNode(Map map) {

        Session session = null;
        long start = System.currentTimeMillis();

        try {
            session = Neo4jUtil.getInstance().getSession();

            session.run("MERGE (terminal:Navigation:Source {siteId:{siteId}, source:{source}, keyword:{keyword}})", Values.value(map));

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        } finally {
            if(session != null)
                session.close();

            long end = System.currentTimeMillis();
            long span = end - start;
            if(span > 1000)
                log.debug("setTerminalNode spans " + span);
        }
    }

    public static void setEventNode(Map map) {

        Session session = null;
        long start = System.currentTimeMillis();

        try {
            session = Neo4jUtil.getInstance().getSession();

            session.run("MERGE (event:Navigation:Event {siteId:{siteId}, node_id:{node_id}, node_name:{node_name}, " +
                    "event_source:{event_source}, event_lab:{event_lab}})", Values.value(map));

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        } finally {
            if(session != null)
                session.close();

            long end = System.currentTimeMillis();
            long span = end - start;
            if(span > 1000)
                log.debug("setTerminalNode spans " + span);
        }
    }

        /**
     * 写入1个主体节点，1个导航节点，1个关系
     */
    /*public static void setRelation(String siteId, String nt_id1, String nt_id2,String qq, String loginId, long sid) {

        Session session = null;
        long start = System.currentTimeMillis();

        try {
            session = Neo4jUtil.getInstance().getSession();

            Map qqMap = getMap(siteId, nt_id1, nt_id2, qq, loginId, sid);
            session.run(
                    "MATCH (qq:Body:QQ {siteId:{siteId}, nt_id:{nt_id1}})," +
                            "(web:Navigation:WEB { siteId:{siteId}, url_id:{url_id} })"+
                            "CREATE (qq)-[:VISIT{ sid: {sid}, createtime:{createtime}}]->(web)\n"
                    , Values.value(qqMap));


        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        } finally {
            if(session != null)
                session.close();

            //pvCount.getAndIncrement();
            long end = System.currentTimeMillis();
            long span = end - start;
            if(span > 1000)
                log.debug("writeOnce spans " + span);
            //else
            //log.debug("writeOnePV pvCount = " + pvCount);
        }
    }*/

    /*public static void setRecord(Set<Neo4jNode> bodySet, Set<Neo4jNode> sessionSet, Set<Neo4jNode> navSet,) {

        Session session = null;
        long start = System.currentTimeMillis();

        try {
            session = Neo4jUtil.getInstance().getSession();

            session.run(
                    "MERGE (qq:Body:QQ {siteId:{siteId}, nt_id:{nt_id1}, qq:{qq}})\n" +
                            "MERGE (login:Body:Login { siteId:{siteId}, nt_id:{nt_id2}, qq:{loginId}})\n" +
                            "MERGE (web:Navigation:WEB { siteId:{siteId},url_id:{url_id}, title:{title}, url:{url} })\t\n"+
                            "MERGE (lbs:Navigation:LBS { siteId:{siteId}, country:{country}, city:{city}, province:{province}}) \n"+
                            "MERGE (qq)-[:EQUAL]->(login)\n" +
                            "MERGE (qq)<-[:EQUAL]-(login)\n" +
                            "MERGE (qq)-[:VISIT{ sid: {sid}, createtime:{createtime}}]->(web)\n" +
                            "MERGE (login)-[:VISIT{ sid: {sid}, createtime:{createtime}}]->(web)\n" +
                            "MERGE (qq)-[:VISIT{ sid: {sid}, createtime:{createtime}}]->(lbs)\n"+
                            "MERGE (login)-[:VISIT{ sid: {sid}, createtime:{createtime}}]->(lbs)"
                    , Values.value(qqMap));


        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        } finally {
            if(session != null)
                session.close();

            long end = System.currentTimeMillis();
            long span = end - start;
            if(span > 1000)
                log.debug("writeOnePV spans " + span);
        }
    }*/

    /*public static void setNode(String labs, Map map) {

        Session session = null;
        long start = System.currentTimeMillis();

        try {
            // TODO 这里有问题，如果下次多一个属性，会创建一个新节点，修改如下
//            MERGE (n:Body:QQ { siteId: 'kf_0001', nt_id: '111' })
//            ON CREATE SET
//            n.id= '123530055',
//                    n.createTime= 12353005599
            String cypher = "MERGE (node " + labs + " {" + getProperties(map) + "})";

            log.debug(cypher);

            session = Neo4jUtil.getInstance().getSession();
            session.run(cypher, Values.value(map));


        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        } finally {
            if(session != null)
                session.close();

            long end = System.currentTimeMillis();
            long span = end - start;
            if(span > 1000)
                log.debug("setTerminalNode spans " + span);
        }
    }*/




/*
    public static void getOnceUser(String siteId, String nt_id1, String nt_id2,String qq, String loginId, long sid) {

        Session session = null;
        long start = System.currentTimeMillis();

        try {
            session = Neo4jUtil.getInstance().getSession();

            Map qqMap = getMap(siteId, nt_id1, nt_id2, qq, loginId, sid);
            StatementResult result = session.run(
                    "MATCH (n:Body { siteId: {siteId},nt_id:{nt_id1} })-[r:VISIT]-(m:Navigation) RETURN n,r,m order by r.createtime asc"
                    , Values.value(qqMap));

            while (result.hasNext())
                System.out.println(result.next().toString());

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        } finally {
            if(session != null)
                session.close();

            long end = System.currentTimeMillis();
            long span = end - start;
//            if(span > 1000)
            log.debug("getOnceUser spans " + span);
            //else
            //log.debug("writeOnePV pvCount = " + pvCount);
        }
    }

    public static void writeOnce(String siteId, String nt_id1, String nt_id2,String qq, String loginId, long sid) {

        Session session = null;
        long start = System.currentTimeMillis();

        try {
            session = Neo4jUtil.getInstance().getSession();

            Map qqMap = getMap(siteId, nt_id1, nt_id2, qq, loginId, sid);
            session.run(
                    "MERGE (qq:Body:QQ {siteId:{siteId}, nt_id:{nt_id1}, qq:{qq}})\n" +
                            "MERGE (web:Navigation:WEB { siteId:{siteId}, title:{title}, url:{url} })\t\n"+
                            "CREATE (qq)-[:VISIT{ sid: {sid}, createtime:{createtime}}]->(web)\n"
                    , Values.value(qqMap));


        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        } finally {
            if(session != null)
                session.close();

            //pvCount.getAndIncrement();
            long end = System.currentTimeMillis();
            long span = end - start;
            if(span > 1000)
                log.debug("writeOnce spans " + span);
            //else
            //log.debug("writeOnePV pvCount = " + pvCount);
        }
    }

    public static Map getMap(String siteId, String nt_id1, String nt_id2, String qq, String loginId, long sid) {

        long time = System.currentTimeMillis();
        String url = "https://host/product" + (int)(Math.random()*10000000);
        int url_id = url.hashCode();

        Map map = new HashMap();
        map.put("siteId", siteId);
        map.put("nt_id1", nt_id1);
        map.put("nt_id2", nt_id2);
        map.put("qq", qq);
        map.put("loginId", loginId);
        map.put("url", url);
        map.put("url_id", url_id);
        map.put("title", "商品页");
        map.put("country", "中国");
        map.put("province", "河北" + (int)(Math.random()*10));
        map.put("city", "石家庄" + (int)(Math.random()*100));
        map.put("createtime", time);
        map.put("sid", sid);

        return map;
    }

    public static void writeOneWebNavigation(String siteId, String nt_id1, String nt_id2,String qq, String loginId, long sid) {

        Session session = null;
        long start = System.currentTimeMillis();

        try {
            session = Neo4jUtil.getInstance().getSession();

            Map qqMap = getMap(siteId, nt_id1, nt_id2, qq, loginId, sid);
            session.run(
                    "MERGE (web:Navigation:WEB { siteId:{siteId},url_id:{url_id}, title:{title}, url:{url}})", Values.value(qqMap));


        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        } finally {
            if(session != null)
                session.close();

            //pvCount.getAndIncrement();
            long end = System.currentTimeMillis();
            long span = end - start;
//            if(span > 1000)
            log.debug("writeOneNavigation spans " + span);
            //else
            //log.debug("writeOneNavigation pvCount = " + pvCount);
        }
    }

    public static void writeOneLBSNavigation(String siteId, String nt_id1, String nt_id2,String qq, String loginId, long sid) {

        Session session = null;
        long start = System.currentTimeMillis();

        try {
            session = Neo4jUtil.getInstance().getSession();

            Map qqMap = getMap(siteId, nt_id1, nt_id2, qq, loginId, sid);
            session.run(
                    "MERGE (lbs:Navigation:LBS { siteId:{siteId}, country:{country}, city:{city}, province:{province}})", Values.value(qqMap));


        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        } finally {
            if(session != null)
                session.close();

            //pvCount.getAndIncrement();
            long end = System.currentTimeMillis();
            long span = end - start;
//            if(span > 1000)
            log.debug("writeOneNavigation spans " + span);
            //else
            //log.debug("writeOneNavigation pvCount = " + pvCount);
        }
    }

*/
