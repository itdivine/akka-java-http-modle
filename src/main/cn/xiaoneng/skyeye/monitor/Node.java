package cn.xiaoneng.skyeye.monitor;


public enum Node {

	Collector, CollectorHandler, CollectorController,CollectorCopy,

	BodySpaceManager, BodySpace, BodyNode, NTBodyNode, CookieBodyNode, NTMessageRouter, PVMsgExcutor,

	RealTimeAnalyzeService,

	NavigationSpaceManager, NavigationPVRouter, NavigationSpace, NavigationNode,

	TrackerManager, TrackReportPVProcessor, Tracker, NTTracker, Record, GetTrackRouter,

	Neo4jDataAccess
	
}
