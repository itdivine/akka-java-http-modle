//package cn.xiaoneng.skyeye.db;
//
//import cn.xiaoneng.skyeye.bodyspace.model.BodyNodeModel;
//import cn.xiaoneng.skyeye.bodyspace.model.NTBodyNodeModel;
//import cn.xiaoneng.skyeye.monitor.Monitor;
//import cn.xiaoneng.skyeye.monitor.MonitorCenter;
//import cn.xiaoneng.skyeye.navigation.bean.NavNodeInfo;
//import cn.xiaoneng.skyeye.track.bean.RecordInfoFull;
//import cn.xiaoneng.skyeye.util.ActorNames;
//import cn.xiaoneng.storage.consumer.service.dao.skyeye.Neo4jInterface;
//import org.apache.commons.collections.map.HashedMap;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
////import cn.xiaoneng.storage.consumer.service.dao.skyeye.Neo4jInterface;
//
///**
// * 第一版账号关联
// * Created by xuyang on 2016/9/5.
// */
//public class Neo4jDataAccess {
//
//    //存储服务SDK
//    private static Neo4jInterface neo4jInterface = new Neo4jInterface();
//
//    //@test：本地测试类
////    private static Neo4jDataAccess_source neo4jInterface = new Neo4jDataAccess_source();
//
//    protected final static Logger log = LoggerFactory.getLogger(Neo4jDataAccess.class);
//
//    private static Monitor monitor = MonitorCenter.getMonitor(cn.xiaoneng.skyeye.monitor.Node.Neo4jDataAccess);
//
//
//    /**
//     * 查询完整主体节点
//     * @param labs : 起始主体节点标签  例: 主体空间[:Body:QQ]
//     * @param map  Node's properties, like key,value
//     */
//    public static BodyNodeModel getBodyNodeModel(String labs, Map map) {
//
//        long start = System.currentTimeMillis();
//
//        BodyNodeModel model =  neo4jInterface.getBodyNodeModel(labs, map);
//        log.info("getBodyNodeModel: " +  labs + " " + map);
//        monitor.newReadTime("getBodyNodeModel", System.currentTimeMillis() - start, true);
//        return model;
//    }
//
//    /**
//     * 查询完整主体节点
//     * @param labs : 起始主体节点标签  例: 主体空间[:Body:QQ]
//     * @param map  Node's index properties, Can't contain
//     */
//    public static NTBodyNodeModel getNTBodyNodeModel(String labs, Map map) {
//
//        long start = System.currentTimeMillis();
//
//        NTBodyNodeModel model = neo4jInterface.getNTBodyNodeModel(labs, map);
//        log.info("getNTBodyNodeModel: " +  labs + " " + map);
//        monitor.newReadTime("getNTBodyNodeModel", System.currentTimeMillis() - start, true);
//        return model;
//    }
//
//    /**
//     * 查询导航节点
//     * @param labs : 节点标签  例: [:Navigation:WEB]
//     * @param map  Node's properties, like key,value
//     */
//    public static NavNodeInfo getNavigationNode(String labs, Map map) {
//
//        long start = System.currentTimeMillis();
//
//        NavNodeInfo navNodeInfo = neo4jInterface.getNavigationNode(labs, map);
//        log.info("getNavigationNode: " +  labs + " " + map);
//        monitor.newReadTime("getNavigationNode", System.currentTimeMillis() - start, true);
//
//        return navNodeInfo;
//    }
//
//
//    /**
//     * 写入一个主体节点
//     *
//     * 当siteId、nt_id、id值相等，但是spaceName不等时，会生成两个节点
//     *
//     * MERGE : 表示有则不写，无则写入
//     * CREATE：表示一定写入
//     *
//     * @spaceName 主体空间名字，在Neo4j中为标签，如：QQ
//     */
//    public static void setBodyNode(String spaceName, Map map) {
//
//        long start = System.currentTimeMillis();
//
//        neo4jInterface.setBodyNode(spaceName, map);
//        log.info("setBodyNode: " +  spaceName + " " + map);
//        monitor.newWriteTime("setBodyNode", System.currentTimeMillis() - start, true);
//    }
//
//    /**
//     * 写入绑定关系【主体节点之间】
//     * @param fromSpaceName 主体空间名
//     * @param map  key: id nt_id
//     */
//    public static void setBodyBondRelation(String fromSpaceName, Map map) {
//
//        long start = System.currentTimeMillis();
//
//        neo4jInterface.setBodyBondRelation(fromSpaceName, map);
//        log.info("setBodyBondRelation: " +  fromSpaceName + " " + map);
//        monitor.newWriteTime("setBodyBondRelation", System.currentTimeMillis() - start, true);
//    }
//
//    /**
//     * 删除绑定关系【主体节点之间】
//     * @param fromSpaceName 主体空间名
//     * @param map  key: id nt_id
//     */
//    public static void deleteBodyBondRelation(String fromSpaceName, Map map) {
//
//        long start = System.currentTimeMillis();
//
//        neo4jInterface.deleteBodyBondRelation(fromSpaceName, map);
//        log.info("deleteBodyBondRelation: " +  fromSpaceName + " " + map);
//        monitor.newWriteTime("deleteBodyBondRelation", System.currentTimeMillis() - start, true);
//    }
//
//    /**
//     * 写入一个等价关系【主体节点之间】
//     * @param map keys: nt_id1,nt_id2
//     */
//    public static void setBodyRelation(Map map) {
//
//        long start = System.currentTimeMillis();
//
//        neo4jInterface.setBodyRelation(map);
//        log.info("setBodyRelation: " + map);
//        monitor.newWriteTime("setBodyRelation", System.currentTimeMillis() - start, true);
//    }
//
//
//    /**
//     * 写入一个导航节点
//     *
//     * 当siteId、nt_id、id值相等，但是spaceName不等时，会生成两个节点
//     *
//     * MERGE : 表示有则不写，无则写入
//     * CREATE：表示一定写入
//     *
//     * @spaceName 导航空间名字，在Neo4j中为标签，如：LBS
//     */
//    public static void setNavigationNode(String spaceName, Map map) {
//
//        long start = System.currentTimeMillis();
//
//        neo4jInterface.setNavigationNode(spaceName, map);
//        log.info("setNavigationNode: " +  spaceName + " " + map);
//        monitor.newWriteTime("setNavigationNode", System.currentTimeMillis() - start, true);
//    }
//
//
//    /**
//     * 写入1个主体导航关系
//     * @param bodyLab
//     * @param navLab
//     * @param ntId
//     * @param navId
//     * @param map VISIT关系上的所有属性集合
//     */
//    public static void setVisitRelation(String bodyLab, String navLab, String ntId, String navId, Map map) {
//
//        long start = System.currentTimeMillis();
//
//        neo4jInterface.setVisitRelation(bodyLab, navLab, ntId, navId, map);
//        log.info("setVisitRelation: " + bodyLab + " " + navLab + " " + ntId + " " + navId + " " + map);
//        monitor.newWriteTime("setVisitRelation", System.currentTimeMillis() - start, true);
//    }
//
//
//    /**
//     * 分页倒序查找用户来访ID集合
//     * @param map
//     * @param skip
//     * @param limit
//     */
//    public static List<String> getSidList(Map map, int skip, int limit) {
//
//        long start = System.currentTimeMillis();
//
//        List<String> sidList = neo4jInterface.getSidList(map, skip, limit);
//        log.info("getSidList: " + skip + " " + limit + " " + map);
//        monitor.newReadTime("getSidList", System.currentTimeMillis() - start, true);
//
//        return sidList;
//
//    }
//
//    /**
//     * 查询某次来访的轨迹
//     * @param map
//     * @return
//     */
//    public static List<RecordInfoFull> getSidTrack(Map map) {
//
//        long start = System.currentTimeMillis();
//
//        List<RecordInfoFull> recordInfoFullList = neo4jInterface.getSidTrack(map);
//        log.info("getSidTrack: " + map);
//        monitor.newReadTime("getSidTrack", System.currentTimeMillis() - start, true);
//
//        return recordInfoFullList;
//
////            // cypher 语句
////            String cypher = "MATCH (n:Body:nt{siteId:{siteId},id:{id}})-[r:VISIT{sid:{sid}}]-(m:Navigation) RETURN r,m order by r.time asc";
////
////            // cypher log
////            log.debug("MATCH (n:Body:nt { siteId: '"+map.get("siteId")+"',id:'"+map.get("id")+"' })" +
////                    "-[r:VISIT{sid:'"+map.get("sid")+"'}]-(m:Navigation)  RETURN r,m order by r.time asc");
////
//
//    }
//
//    /**
//     * 分页查找用户轨迹
//     * @param map
//     */
//    public static Set<Integer> getLevels(Map map) {
//
//        if (map == null || map.size() == 0) {
//            log.warn("labs and map are empty. ");
//        }
//
//        long start = System.currentTimeMillis();
//
//        Set<Integer> keylevelList = neo4jInterface.getLevels(map);
//        log.info("getLevels: " + map);
//        long end = System.currentTimeMillis();
//        long span = end - start;
//        monitor.newReadTime("getRelation", span, true);
//        if (span > 1000)
//            log.debug("getRelation spans " + span);
//
//        return keylevelList;
//    }
//
//    /**
//     *  创建节点之间的关系
//     *  例如：
//     *      1. 创建咨询节点和订单节点的关系
//     *
//     * @param fromLabs : 节点标签  例: [:Navigation:Order]
//     * @param fromLabs : 节点标签  例: [:Navigation:Chat]
//     * @param fromMap  Node's properties
//     * @param toMap   Node's properties
//     * @param relationLabs 节点之间的关系 例: CP  VISIT
//     */
//    public static boolean setRelation(String fromLabs, String toLabs, Map fromMap, Map toMap, String relationLabs, Map relationMap) {
//
//        if (fromLabs==null || fromLabs.isEmpty()) {
//            log.warn("fromLabs is empty.");
//            return false;
//        }
//
//        if (toLabs==null || toLabs.isEmpty()) {
//            log.warn("toLabs is empty.");
//            return false;
//        }
//
//        try {
//            // cypher 语句
//            String cypher = "MATCH (n " + fromLabs;
//            if(fromMap!=null && fromMap.size()>0) {
//                cypher = cypher + " {" + getProperties(fromMap) + "}";
//            }
//            cypher += "),(m" + toLabs;
//            if(toMap!=null && toMap.size()>0) {
//                cypher = cypher + " {" + getProperties(toMap) + "}";
//            }
//            cypher += ") CREATE (m)-[:" + relationLabs;
//            if(relationMap!=null && relationMap.size()>0) {
//                cypher = cypher + " {" + getProperties(relationMap) + "}";
//            }
//            cypher += "]->(n)";
//
//            Map map = new HashedMap();
//            if(fromMap!=null)
//                map.putAll(fromMap);
//
//            if(toMap!=null)
//                map.putAll(toMap);
//
//            if(relationMap!=null)
//                map.putAll(relationMap);
//
//            neo4jInterface.setRelation(cypher, map);
//            log.info("setRelation: " +  cypher + " " + map);
//
//        } catch (Exception e) {
//            log.error("Exception " + e.getMessage());
//            StackTraceElement[] er = e.getStackTrace();
//            for (int i = 0; i < er.length; i++) {
//                log.info(er[i].toString());
//            }
//        }
//
//        return true;
//    }
//
//    /**
//     *  获取节点之间的关系
//     *
//     * @param fromLabs : 节点标签  例: [:Body:nt]
//     * @param fromLabs : 节点标签  例: [:Navigation:Order]
//     * @param fromMap  Node's properties
//     * @param toMap   Node's properties
//     * @param relationLabs 节点之间的关系 例: CP  VISIT
//     */
//    public static Map getRelation(String fromLabs, String toLabs, Map fromMap, Map toMap, String relationLabs, Map relationMap) {
//        if (fromLabs==null || fromLabs.isEmpty()) {
//            log.warn("fromLabs is empty.");
//            return null;
//        }
//
//        if (toLabs==null || toLabs.isEmpty()) {
//            log.warn("toLabs is empty.");
//            return null;
//        }
//
//        try {
//            // cypher 语句
//            String cypher = "MATCH (n " + fromLabs;
//            if(fromMap!=null && fromMap.size()>0) {
//                cypher = cypher + " {" + getProperties(fromMap) + "}";
//            }
//            cypher += ")-[r:" + relationLabs;
//            if(relationMap!=null && relationMap.size()>0) {
//                cypher = cypher + " {" + getProperties(relationMap) + "}";
//            }
//            cypher += "]->(m" + toLabs;
//            if(toMap!=null && toMap.size()>0) {
//                cypher = cypher + " {" + getProperties(toMap) + "}";
//            }
//            cypher += ") RETURN r order by r.time desc limit 1";
//
//            Map map = new HashedMap();
//            if(fromMap!=null)
//                map.putAll(fromMap);
//
//            if(toMap!=null)
//                map.putAll(toMap);
//
//            if(relationMap!=null)
//                map.putAll(relationMap);
//
//            log.info("getRelation: " +  cypher + " " + map);
//            return neo4jInterface.getRelation(cypher, map);
//
//        } catch (Exception e) {
//            log.error("Exception " + e.getMessage());
//            StackTraceElement[] er = e.getStackTrace();
//            for (int i = 0; i < er.length; i++) {
//                log.info(er[i].toString());
//            }
//        }
//
//        return null;
//    }
//
//
//        /**
//         *  查找节点关联的节点
//         *  例如：
//         *      1. 查找订单节点关联的咨询节点
//         *      2. 查找订单节点关联的web节点
//         *
//         * @param fromLabs : 节点标签  例: [:Navigation:Order]
//         * @param fromLabs : 节点标签  例: [:Navigation:Chat]
//         * @param fromMap  Node's properties
//         * @param toMap   Node's properties
//         * @param relation 节点之间的关系 例: CP  VISIT
//         */
//    public static List<NavNodeInfo> getNavNode(String fromLabs, String toLabs, Map fromMap, Map toMap, String relation) {
//
//        if (fromLabs==null || fromLabs.isEmpty()) {
//            log.warn("fromLabs is empty.");
//            return null;
//        }
//
//        if (toLabs==null || toLabs.isEmpty()) {
//            log.warn("toLabs is empty.");
//            return null;
//        }
//
//        String cypher;
//        List<NavNodeInfo> navNodeInfoList = null;
//
//        try {
//            // cypher 语句
//            cypher = "MATCH (n " + fromLabs;
//            if(fromMap!=null && fromMap.size()>0) {
//                cypher = cypher + " {" + getProperties(fromMap) + "}";
//            }
//            cypher += ")-[r:" + relation + "]-(m" + toLabs;
//            if(toMap!=null && toMap.size()>0) {
//                cypher = cypher + " {" + getProperties(toMap) + "}";
//            }
//            cypher += ") RETURN m";
//
//            Map map = new HashedMap();
//            if(fromMap!=null)
//                map.putAll(fromMap);
//
//            if(toMap!=null)
//                map.putAll(toMap);
//
//            navNodeInfoList = neo4jInterface.getNavNode(cypher, map);
//            log.info("getNavNode: " +  cypher + " " + map);
//
//        } catch (Exception e) {
//            log.error("Exception " + e.getMessage());
//            StackTraceElement[] er = e.getStackTrace();
//            for (int i = 0; i < er.length; i++) {
//                log.info(er[i].toString());
//            }
//        }
//
//        return navNodeInfoList;
//    }
//
//    /**
//     * 查询访客在一定时间范围内,最近一次访问导航空间的某个节点
//     *  1. 查询访客3天内最近一次下单
//     *  2. 查询访客3天内最近一次咨询
//     *
//     * @param fromLabs : 节点标签  例: [:Navigation:WEB]
//     * @param fromLabs : 节点标签  例: [:Navigation:WEB]
//     * @param map  Node's properties, like key,value
//     * @param time 过去某一时间(时间戳)
//     */
//    public static NavNodeInfo getRecentlyAccessedNavNode(String fromLabs, String toLabs, Map map, long time) {
//
//        long start = System.currentTimeMillis();
//
//        NavNodeInfo model = neo4jInterface.getRecentlyAccessedNavNode(fromLabs, toLabs, map, time);
//        log.info("getRecentlyAccessedNavNode: " +  fromLabs + " " + map + " " + toLabs);
//        monitor.newReadTime("getSidList", System.currentTimeMillis() - start, true);
//
//        return model;
//    }
//
//    private static String getProperties(Map map) {
//
//        String attrs = "";
//        Set keys = map.keySet();
//        for(Object key:keys) {
//            attrs = attrs + key + ":{" + key + "},";
//        }
//        attrs = attrs.substring(0,attrs.length()-1);
//        return attrs;
//    }
//
//    public static void main(String[] args){
//
//        HashMap<String,Object> map = new HashMap();
//
////        map.put("siteId", "kf_0001");
////        map.put("id", "c_fengyu");
////        NTBodyNodeModel ntModel = Neo4jDataAccess.getNTBodyNodeModel("nt", map);
////        System.out.print(ntModel.toString());
//
//
//        if(true)
//            return;
//
//        //Neo4j
////	public static int neo4j_maxSession = 200;
////	public static String neo4j_userName = "neo4j";
////	public static String neo4j_password = "xuyang";
////	public static String neo4j_url = "bolt://192.168.30.230";
//
//        //test
//        String url = "bolt://dev_in.ntalker.com:7687";
//        String userName = "neo4j";
//        String password = "xuyang";
//
//        //rd
////        String url = "bolt://192.168.30.230";
////        String userName = "neo4j";
////        String password = "1234";
//
//
//        int maxSession = 200;
//
////        Neo4jUtil.getInstance().init(url, maxSession, userName, password);
//
//
//
//        String labs = "";
//
//        //0.查询一个访客的轨迹
//        /*int skip = 0;
//        int limit = 5;
//        map.put("siteId","kf_1000");
//        map.put("id","kf_1000_ISME9754_guestTEMPBD42-DDB1-BD");
//        List<String> sidList = Neo4jDataAccess.getSidList(map, skip, limit);
//        if(sidList != null && sidList.size()>0) {
//            for(String sid:sidList) {
//                map.put("sid", sid);
//                getSidTrack(map);
//            }
//        }*/
//
//
//
//
//        // 账号中心专用
//        //1.任意账号查询nt
//        // MATCH (m :Body:cookie {siteId:'kf_1000',id:'guestF46A9B91-6208-9C46-997B-71880CCC6A53'})-[:EQUAL]->(n:Body) RETURN n
////        String cookie = "guestF46A9B91-6208-9C46-997B-71880CCC6A53";
////        labs = ":Body:" + ActorNames.COOKIE_BODYSPACE;
////        map.put("siteId", "kf_1000");
////        map.put("id", cookie);
////        String nt = Neo4jDataAccess.getRelationNTID(labs, map);
////        log.debug("账号中心专用1  " + nt);
//
////
////        //2.ntId查询关联的cookie账号
////        nt = "N1";
////        String fromLabs = ActorNames.NT_BODYSPACE;
////        String toLabs = ActorNames.COOKIE_BODYSPACE;
////        map.put("id", nt);
////        Set<String> set = Neo4jDataAccess.getIDs(fromLabs, toLabs, map);
////        log.debug("账号中心专用2  " + set.toString());
//
//
//
//        // 1.查询主体节点(8个纬度)
////        labs = ":Body:" + ActorNames.COOKIE_BODYSPACE;
////        map.clear();
////        map.put("siteId", "kf_3004");
////        map.put("id", "9014D5-8EEE9DDC-29A5-B085-EECD-C519EC85D294");
////
////        BodyNodeModel model = Neo4jDataAccess_source.getBodyNodeModel(labs, map);
////        if(model!=null)
////            System.out.println(model.toString());
//
//        // 查询NT节点
////        labs = ":Body:" + ActorNames.NT_BODYSPACE;
////        map.clear();
////        map.put("siteId", "kf_0001");
////        map.put("id", "N1");
////
////        NTBodyNodeModel model = Neo4jDataAccess.getNTBodyNodeModel(labs, map);
////        if(model!=null)
////            log.debug(model.toString());
//
////
////
////        // 2.查询导航节点
////        labs = ":Navigation:WEB";
////        map.clear();
////        map.put("siteId", "kf_55");
////        map.put("id", "105");
////        NavNodeInfo info = Neo4jDataAccess.getNavigationNode(labs, map);
////        if(info!=null)
////        {
////            long count = Neo4jDataAccess.getNavNodeVisitedCount(labs, map);
////            info.setVisitedCount(count);
////            log.debug(info.toString());
////        }
////
////        // 3.创建主体节点和关系
//        //3-1.游客来访  C1-N1
//        /*map.clear();
//        map.put("siteId", "kf_0001");
//        map.put("id", "C1");
//        map.put("createTime", System.currentTimeMillis());
//        Neo4jDataAccess.setBodyNode(ActorNames.COOKIE_BODYSPACE, map);
//
//        map.clear();
//        map.put("siteId", "kf_0001");
//        map.put("id", "N1");
//        map.put("createTime", System.currentTimeMillis());
//        Neo4jDataAccess.setBodyNode(ActorNames.NT_BODYSPACE, map);
//
//        map.clear();
//        map.put("siteId", "kf_0001");
//        map.put("id1", "C1");
//        map.put("id2", "N1");
//        Neo4jDataAccess.setBodyBondRelation(ActorNames.COOKIE_BODYSPACE, map);
//
//        //3-2.C1登录L1  L1-N1
//        map.clear();
//        map.put("siteId", "kf_0001");
//        map.put("id", "L1");
//        map.put("createTime", System.currentTimeMillis());
//        Neo4jDataAccess.setBodyNode(ActorNames.LOGIN_BODYSPACE, map);
//
//        map.clear();
//        map.put("siteId", "kf_0001");
//        map.put("id1", "L1");
//        map.put("id2", "N1");
//        Neo4jDataAccess.setBodyBondRelation(ActorNames.LOGIN_BODYSPACE, map);
//
//        //3-2.C1登录L2  L2-N2 C1-N2 解除绑定C1-N1
//        map.clear();
//        map.put("siteId", "kf_0001");
//        map.put("id", "L2");
//        map.put("createTime", System.currentTimeMillis());
//        Neo4jDataAccess.setBodyNode(ActorNames.LOGIN_BODYSPACE, map);
//
//        map.clear();
//        map.put("siteId", "kf_0001");
//        map.put("id", "N2");
//        map.put("createTime", System.currentTimeMillis());
//        Neo4jDataAccess.setBodyNode(ActorNames.NT_BODYSPACE, map);
//
//        map.clear();
//        map.put("siteId", "kf_0001");
//        map.put("id1", "L2");
//        map.put("id2", "N2");
//        Neo4jDataAccess.setBodyBondRelation(ActorNames.LOGIN_BODYSPACE, map);
//
//        map.clear();
//        map.put("siteId", "kf_0001");
//        map.put("id1", "C1");
//        map.put("id2", "N2");
//        Neo4jDataAccess.setBodyBondRelation(ActorNames.COOKIE_BODYSPACE, map);
//*/
//        map.clear();
//        map.put("siteId", "kf_3004");
//        map.put("id1", "cookie_111");
//        map.put("id2", "NT_222");
//        Neo4jDataAccess.deleteBodyBondRelation(ActorNames.COOKIE_BODYSPACE, map);
///*
//        //3-3.C2游客  C2-N3
//        map.clear();
//        map.put("siteId", "kf_0001");
//        map.put("id", "C2");
//        map.put("createTime", System.currentTimeMillis());
//        Neo4jDataAccess.setBodyNode(ActorNames.COOKIE_BODYSPACE, map);
//
//        map.clear();
//        map.put("siteId", "kf_0001");
//        map.put("id", "N3");
//        map.put("createTime", System.currentTimeMillis());
//        Neo4jDataAccess.setBodyNode(ActorNames.NT_BODYSPACE, map);
//
//        map.clear();
//        map.put("siteId", "kf_0001");
//        map.put("id1", "C2");
//        map.put("id2", "N3");
//        Neo4jDataAccess.setBodyBondRelation(ActorNames.COOKIE_BODYSPACE, map);
//
//        //3-4.C2登录L1  C2-N1,N1-N2 解绑C2-N3
//        map.clear();
//        map.put("siteId", "kf_0001");
//        map.put("id1", "C2");
//        map.put("id2", "N1");
//        Neo4jDataAccess.setBodyBondRelation(ActorNames.COOKIE_BODYSPACE, map);
//
//        map.clear();
//        map.put("siteId", "kf_0001");
//        map.put("id1", "C2");
//        map.put("id2", "N3");
//        Neo4jDataAccess.deleteBodyBondRelation(ActorNames.COOKIE_BODYSPACE, map);
//
//        map.clear();
//        map.put("siteId", "kf_0001");
//        map.put("id1", "N1");
//        map.put("id2", "N3");
//        Neo4jDataAccess.setBodyRelation(map);
//
//
//
//        // 4.创建导航节点
//        map.clear();
//        map.put("siteId", "kf_0001");
//        map.put("id", "中国河北唐山".hashCode() + "");
//        map.put("country", "中国");
//        map.put("province", "河北");
//        map.put("city", "唐山");
//        Neo4jDataAccess.setNavigationNode("LBS", map);
//
//        // 5.创建主体导航关系
//        map.clear();
//        map.put("siteId", "kf_0001");
//        map.put("name", "divine");
//        map.put("sid", "1472709659677611");
//        map.put("createtime", 1472702590686L);
//        Neo4jDataAccess.setVisitRelation("nt", "LBS", "N1", "1665735573", map);
//*/
//        // 6.一步创建 3~5 中所有节点和关系
//
//
//
//
//
//    }
//
//}
//
///*
//public static void setWebNode(Map map) {
//
//        Session session = null;
//        long start = System.currentTimeMillis();
//
//        try {
//            session = Neo4jUtil.getInstance().getSession();
//
//            session.run("MERGE (web:Navigation:WEB { siteId:{siteId},url_id:{url_id}, title:{title}, url:{url}, level:{level} })", Values.value(map));
//
//        } catch (Exception e) {
//            log.error("Exception " + e.getMessage());
//        } finally {
//            if(session != null)
//                session.close();
//
//            long end = System.currentTimeMillis();
//            long span = end - start;
//            if(span > 1000)
//                log.debug("setWebNode spans " + span);
//        }
//    }
//
//
//    public static void setLBSNode(Map map) {
//
//        Session session = null;
//        long start = System.currentTimeMillis();
//
//        try {
//            session = Neo4jUtil.getInstance().getSession();
//
//            session.run("MERGE (lbs:Navigation:LBS {siteId:{siteId}, country:{country}, city:{city}, province:{province}, lng:{lng}, lat:{lat}})", Values.value(map));
//
//        } catch (Exception e) {
//            log.error("Exception " + e.getMessage());
//        } finally {
//            if(session != null)
//                session.close();
//
//            long end = System.currentTimeMillis();
//            long span = end - start;
//            if(span > 1000)
//                log.debug("setLBSNode spans " + span);
//        }
//    }
//
//    public static void setTerminalNode(Map map) {
//
//        Session session = null;
//        long start = System.currentTimeMillis();
//
//        try {
//            session = Neo4jUtil.getInstance().getSession();
//
//            session.run("MERGE (terminal:Navigation:Terminal {siteId:{siteId}, system:{system}, device:{device}, flash:{flash}," +
//                    " language:{language}, screensize:{screensize}, browser:{browser}})", Values.value(map));
//
//        } catch (Exception e) {
//            log.error("Exception " + e.getMessage());
//        } finally {
//            if(session != null)
//                session.close();
//
//            long end = System.currentTimeMillis();
//            long span = end - start;
//            if(span > 1000)
//                log.debug("setTerminalNode spans " + span);
//        }
//    }
//
//    public static void setSourceNode(Map map) {
//
//        Session session = null;
//        long start = System.currentTimeMillis();
//
//        try {
//            session = Neo4jUtil.getInstance().getSession();
//
//            session.run("MERGE (terminal:Navigation:Source {siteId:{siteId}, source:{source}, keyword:{keyword}})", Values.value(map));
//
//        } catch (Exception e) {
//            log.error("Exception " + e.getMessage());
//        } finally {
//            if(session != null)
//                session.close();
//
//            long end = System.currentTimeMillis();
//            long span = end - start;
//            if(span > 1000)
//                log.debug("setTerminalNode spans " + span);
//        }
//    }
//
//    public static void setEventNode(Map map) {
//
//        Session session = null;
//        long start = System.currentTimeMillis();
//
//        try {
//            session = Neo4jUtil.getInstance().getSession();
//
//            session.run("MERGE (event:Navigation:Event {siteId:{siteId}, node_id:{node_id}, node_name:{node_name}, " +
//                    "event_source:{event_source}, event_lab:{event_lab}})", Values.value(map));
//
//        } catch (Exception e) {
//            log.error("Exception " + e.getMessage());
//        } finally {
//            if(session != null)
//                session.close();
//
//            long end = System.currentTimeMillis();
//            long span = end - start;
//            if(span > 1000)
//                log.debug("setTerminalNode spans " + span);
//        }
//    }
//
//        /**
//     * 写入1个主体节点，1个导航节点，1个关系
//     */
//    /*public static void setRelation(String siteId, String nt_id1, String nt_id2,String qq, String loginId, long sid) {
//
//        Session session = null;
//        long start = System.currentTimeMillis();
//
//        try {
//            session = Neo4jUtil.getInstance().getSession();
//
//            Map qqMap = getMap(siteId, nt_id1, nt_id2, qq, loginId, sid);
//            session.run(
//                    "MATCH (qq:Body:QQ {siteId:{siteId}, nt_id:{nt_id1}})," +
//                            "(web:Navigation:WEB { siteId:{siteId}, url_id:{url_id} })"+
//                            "CREATE (qq)-[:VISIT{ sid: {sid}, createtime:{createtime}}]->(web)\n"
//                    , Values.value(qqMap));
//
//
//        } catch (Exception e) {
//            log.error("Exception " + e.getMessage());
//        } finally {
//            if(session != null)
//                session.close();
//
//            //pvCount.getAndIncrement();
//            long end = System.currentTimeMillis();
//            long span = end - start;
//            if(span > 1000)
//                log.debug("writeOnce spans " + span);
//            //else
//            //log.debug("writeOnePV pvCount = " + pvCount);
//        }
//    }*/
//
//    /*public static void setRecord(Set<Neo4jNode> bodySet, Set<Neo4jNode> sessionSet, Set<Neo4jNode> navSet,) {
//
//        Session session = null;
//        long start = System.currentTimeMillis();
//
//        try {
//            session = Neo4jUtil.getInstance().getSession();
//
//            session.run(
//                    "MERGE (qq:Body:QQ {siteId:{siteId}, nt_id:{nt_id1}, qq:{qq}})\n" +
//                            "MERGE (login:Body:Login { siteId:{siteId}, nt_id:{nt_id2}, qq:{loginId}})\n" +
//                            "MERGE (web:Navigation:WEB { siteId:{siteId},url_id:{url_id}, title:{title}, url:{url} })\t\n"+
//                            "MERGE (lbs:Navigation:LBS { siteId:{siteId}, country:{country}, city:{city}, province:{province}}) \n"+
//                            "MERGE (qq)-[:EQUAL]->(login)\n" +
//                            "MERGE (qq)<-[:EQUAL]-(login)\n" +
//                            "MERGE (qq)-[:VISIT{ sid: {sid}, createtime:{createtime}}]->(web)\n" +
//                            "MERGE (login)-[:VISIT{ sid: {sid}, createtime:{createtime}}]->(web)\n" +
//                            "MERGE (qq)-[:VISIT{ sid: {sid}, createtime:{createtime}}]->(lbs)\n"+
//                            "MERGE (login)-[:VISIT{ sid: {sid}, createtime:{createtime}}]->(lbs)"
//                    , Values.value(qqMap));
//
//
//        } catch (Exception e) {
//            log.error("Exception " + e.getMessage());
//        } finally {
//            if(session != null)
//                session.close();
//
//            long end = System.currentTimeMillis();
//            long span = end - start;
//            if(span > 1000)
//                log.debug("writeOnePV spans " + span);
//        }
//    }*/
//
//    /*public static void setNode(String labs, Map map) {
//
//        Session session = null;
//        long start = System.currentTimeMillis();
//
//        try {
//            // TODO 这里有问题，如果下次多一个属性，会创建一个新节点，修改如下
////            MERGE (n:Body:QQ { siteId: 'kf_0001', nt_id: '111' })
////            ON CREATE SET
////            n.id= '123530055',
////                    n.createTime= 12353005599
//            String cypher = "MERGE (node " + labs + " {" + getProperties(map) + "})";
//
//            log.debug(cypher);
//
//            session = Neo4jUtil.getInstance().getSession();
//            session.run(cypher, Values.value(map));
//
//
//        } catch (Exception e) {
//            log.error("Exception " + e.getMessage());
//        } finally {
//            if(session != null)
//                session.close();
//
//            long end = System.currentTimeMillis();
//            long span = end - start;
//            if(span > 1000)
//                log.debug("setTerminalNode spans " + span);
//        }
//    }*/
//
//
//
//
///*
//    public static void getOnceUser(String siteId, String nt_id1, String nt_id2,String qq, String loginId, long sid) {
//
//        Session session = null;
//        long start = System.currentTimeMillis();
//
//        try {
//            session = Neo4jUtil.getInstance().getSession();
//
//            Map qqMap = getMap(siteId, nt_id1, nt_id2, qq, loginId, sid);
//            StatementResult result = session.run(
//                    "MATCH (n:Body { siteId: {siteId},nt_id:{nt_id1} })-[r:VISIT]-(m:Navigation) RETURN n,r,m order by r.createtime asc"
//                    , Values.value(qqMap));
//
//            while (result.hasNext())
//                System.out.println(result.next().toString());
//
//        } catch (Exception e) {
//            log.error("Exception " + e.getMessage());
//        } finally {
//            if(session != null)
//                session.close();
//
//            long end = System.currentTimeMillis();
//            long span = end - start;
////            if(span > 1000)
//            log.debug("getOnceUser spans " + span);
//            //else
//            //log.debug("writeOnePV pvCount = " + pvCount);
//        }
//    }
//
//    public static void writeOnce(String siteId, String nt_id1, String nt_id2,String qq, String loginId, long sid) {
//
//        Session session = null;
//        long start = System.currentTimeMillis();
//
//        try {
//            session = Neo4jUtil.getInstance().getSession();
//
//            Map qqMap = getMap(siteId, nt_id1, nt_id2, qq, loginId, sid);
//            session.run(
//                    "MERGE (qq:Body:QQ {siteId:{siteId}, nt_id:{nt_id1}, qq:{qq}})\n" +
//                            "MERGE (web:Navigation:WEB { siteId:{siteId}, title:{title}, url:{url} })\t\n"+
//                            "CREATE (qq)-[:VISIT{ sid: {sid}, createtime:{createtime}}]->(web)\n"
//                    , Values.value(qqMap));
//
//
//        } catch (Exception e) {
//            log.error("Exception " + e.getMessage());
//        } finally {
//            if(session != null)
//                session.close();
//
//            //pvCount.getAndIncrement();
//            long end = System.currentTimeMillis();
//            long span = end - start;
//            if(span > 1000)
//                log.debug("writeOnce spans " + span);
//            //else
//            //log.debug("writeOnePV pvCount = " + pvCount);
//        }
//    }
//
//    public static Map getMap(String siteId, String nt_id1, String nt_id2, String qq, String loginId, long sid) {
//
//        long time = System.currentTimeMillis();
//        String url = "https://host/product" + (int)(Math.random()*10000000);
//        int url_id = url.hashCode();
//
//        Map map = new HashMap();
//        map.put("siteId", siteId);
//        map.put("nt_id1", nt_id1);
//        map.put("nt_id2", nt_id2);
//        map.put("qq", qq);
//        map.put("loginId", loginId);
//        map.put("url", url);
//        map.put("url_id", url_id);
//        map.put("title", "商品页");
//        map.put("country", "中国");
//        map.put("province", "河北" + (int)(Math.random()*10));
//        map.put("city", "石家庄" + (int)(Math.random()*100));
//        map.put("createtime", time);
//        map.put("sid", sid);
//
//        return map;
//    }
//
//    public static void writeOneWebNavigation(String siteId, String nt_id1, String nt_id2,String qq, String loginId, long sid) {
//
//        Session session = null;
//        long start = System.currentTimeMillis();
//
//        try {
//            session = Neo4jUtil.getInstance().getSession();
//
//            Map qqMap = getMap(siteId, nt_id1, nt_id2, qq, loginId, sid);
//            session.run(
//                    "MERGE (web:Navigation:WEB { siteId:{siteId},url_id:{url_id}, title:{title}, url:{url}})", Values.value(qqMap));
//
//
//        } catch (Exception e) {
//            log.error("Exception " + e.getMessage());
//        } finally {
//            if(session != null)
//                session.close();
//
//            //pvCount.getAndIncrement();
//            long end = System.currentTimeMillis();
//            long span = end - start;
////            if(span > 1000)
//            log.debug("writeOneNavigation spans " + span);
//            //else
//            //log.debug("writeOneNavigation pvCount = " + pvCount);
//        }
//    }
//
//    public static void writeOneLBSNavigation(String siteId, String nt_id1, String nt_id2,String qq, String loginId, long sid) {
//
//        Session session = null;
//        long start = System.currentTimeMillis();
//
//        try {
//            session = Neo4jUtil.getInstance().getSession();
//
//            Map qqMap = getMap(siteId, nt_id1, nt_id2, qq, loginId, sid);
//            session.run(
//                    "MERGE (lbs:Navigation:LBS { siteId:{siteId}, country:{country}, city:{city}, province:{province}})", Values.value(qqMap));
//
//
//        } catch (Exception e) {
//            log.error("Exception " + e.getMessage());
//        } finally {
//            if(session != null)
//                session.close();
//
//            //pvCount.getAndIncrement();
//            long end = System.currentTimeMillis();
//            long span = end - start;
////            if(span > 1000)
//            log.debug("writeOneNavigation spans " + span);
//            //else
//            //log.debug("writeOneNavigation pvCount = " + pvCount);
//        }
//    }
//
//*/
