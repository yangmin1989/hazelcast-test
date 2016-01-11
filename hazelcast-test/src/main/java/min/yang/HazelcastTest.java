package min.yang;

import java.util.Random;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.LoadBalancer;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.client.util.RandomLB;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
/**
 * reproduce the issue that hazelcast does not destroy some relative object 
 * such as proxy object or statistics object when the client call destroy() of ILock and IMap.
 * That may results in memory leak.
 * 
 * the steps to reproduce this issue:
 * 
 * 1. Before run the main() of this class,you must start a hazelcast server instance on localhost 
 * using the configuration hazelcast.xml in this project. Version of hazelcast must be 3.4.1
 * 
 * 2.Combile this class and run it.
 * 
 * 3.After step 2 is finished,dump the hazelcast server process using tool jmap.
 * 
 * Analysis the dumpfile with MAT or other tools. you will find three kinds of object that can not be destroyed:
 * 
 * 1.ConcurrentMap<ObjectNamespace, EntryTaskScheduler> evictionProcessors in LockServiceImpl
 * 2.ConcurrentMap<ObjectNamespace, LockStoreImpl> lockStores in LockStoreContainer
 * 3.ConcurrentMap<String, LocalMapStatsImpl> statsMap in LocalMapStatsProvider
 *   
 * @author yangmin
 *
 */
public class HazelcastTest
{
	public static void main( String[] args )
    {
    	HazelcastInstance hazelcastInstance = getClient();
    	//keeping create lock and map and destroy it
    	Random random=new Random();
    	int objectCount=2000000;
    	for(int i=0;i<objectCount;i++){
    		int subfix=random.nextInt(objectCount);
    		//test lock
    		ILock newLock=hazelcastInstance.getLock("Lock"+subfix);
    		newLock.lock();
    		newLock.unlock();
    		newLock.destroy();
    		
    		//test map
    		IMap<String,String> newMap=hazelcastInstance.getMap("Map"+subfix);
    		newMap.put("string", "string");
    		newMap.remove("string");
    		newMap.destroy(); 
    		System.out.println("index:"+i);
    	}
    }
    
    private static HazelcastInstance getClient(){
    	ClientConfig clientConfig = new ClientConfig();
		clientConfig.setGroupConfig(new GroupConfig("min.yang","min.yang"));
		ClientNetworkConfig networkConfig=new ClientNetworkConfig();
		networkConfig.addAddress("127.0.0.1");
		clientConfig.setNetworkConfig(networkConfig);
		LoadBalancer loadBalancer=new RandomLB();
		clientConfig.setLoadBalancer(loadBalancer);
        HazelcastInstance client = HazelcastClient.newHazelcastClient( clientConfig );
        return client;
    }
}
