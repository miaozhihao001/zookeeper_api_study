package com.zhihao.miao.curator.cluster;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * 1.就是一个客户端对/super节点进行操作，增，删，改那么其他监听这个节点的客户端也是知道的，也会对其进行相应的操作，这个demo就是test这个客户端对/super这个节点进行增，删，改，其他
 * 比如client1和client2也是监听到的，
 * 
 * 2.就是如果一个客户端对/super创建了二个节点，c1和c2，或者修改了value值，下次再去连接zookeeper的时候，还是会监听到这个变化，也就是重复注册的意思，这个可以在分布式系统中运用，就是一些配置参数
 * 修改或者新增，其他系统都是共用这份配置的话，就会一起重新注册使用这个配置的参数，也就是zookeeper的发布和订阅模式
 * 
 * 3.zookeeper除了可以实现DistributedDoubleBarrier和DistributedBarrier，还可以实现DistributedQueue，DistributedDelayQueue，
 * DistributedPriorityQueue
 *
 */
public class CuratorWatcher {

	/** 父节点path */
	static final String PARENT_PATH = "/super";
	
	/** zookeeper服务器地址 */
	public static final String CONNECT_ADDR = "192.168.5.124:2181,192.168.5.125:2181,192.168.5.126:2181";	/** 定义session失效时间 */
	
	public static final int SESSION_TIMEOUT = 30000;
	
	public CuratorWatcher() throws Exception{
		//1 重试策略：初试时间为1s 重试10次
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
		//2 通过工厂创建连接
		CuratorFramework cf = CuratorFrameworkFactory.builder()
					.connectString(CONNECT_ADDR)
					.sessionTimeoutMs(SESSION_TIMEOUT)
					.retryPolicy(retryPolicy)
					.build();
		//3 建立连接
		cf.start();
		
		//4 创建跟节点
		if(cf.checkExists().forPath(PARENT_PATH) == null){
			cf.create().withMode(CreateMode.PERSISTENT).forPath(PARENT_PATH,"super init".getBytes());
		}

		//4 建立一个PathChildrenCache缓存,第三个参数为是否接受节点数据内容 如果为false则不接受
		PathChildrenCache cache = new PathChildrenCache(cf, PARENT_PATH, true);
		//5 在初始化的时候就进行缓存监听
		cache.start(StartMode.POST_INITIALIZED_EVENT);
		cache.getListenable().addListener(new PathChildrenCacheListener() {
			/**
			 * <B>方法名称：</B>监听子节点变更<BR>
			 * <B>概要说明：</B>新建、修改、删除<BR>
			 * @see org.apache.curator.framework.recipes.cache.PathChildrenCacheListener#childEvent(org.apache.curator.framework.CuratorFramework, org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent)
			 */
			@Override
			public void childEvent(CuratorFramework cf, PathChildrenCacheEvent event) throws Exception {
				switch (event.getType()) {
				case CHILD_ADDED:
					System.out.println("CHILD_ADDED :" + event.getData().getPath());
					System.out.println("CHILD_ADDED :" + new String(event.getData().getData()));
					break;
				case CHILD_UPDATED:
					System.out.println("CHILD_UPDATED :" + event.getData().getPath());
					System.out.println("CHILD_UPDATED :" + new String(event.getData().getData()));
					break;
				case CHILD_REMOVED:
					System.out.println("CHILD_REMOVED :" + event.getData().getPath());
					System.out.println("CHILD_REMOVED :" + new String(event.getData().getData()));
					break;
				default:
					break;
				}
			}
		});
	}

}
