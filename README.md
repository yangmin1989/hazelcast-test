# hazelcast-test
* reproduce the issue that hazelcast does not destroy some relative object 
 * such as proxy object or statistics object when the client call destroy() of ILock and IMap.
 * That may results in memory leak.
 * 
 * the steps to reproduce this issue:
 * 
 * 1.start a hazelcast server instance on localhost using the configuration hazelcast.xml in this project. Version of hazelcast must be 3.4.1
 * 
 * 2.Combile HazelcastTest class in the project and run it.
 * 
 * 3.After step 2 is finished,dump the hazelcast server process using tool jmap.
 * 
 * Analysis the dumpfile with MAT or other tools. you will find three kinds of object that can not be destroyed:
 * 
 * 1.ConcurrentMap<ObjectNamespace, EntryTaskScheduler> evictionProcessors in LockServiceImpl
 * 2.ConcurrentMap<ObjectNamespace, LockStoreImpl> lockStores in LockStoreContainer
 * 3.ConcurrentMap<String, LocalMapStatsImpl> statsMap in LocalMapStatsProvider
 *
