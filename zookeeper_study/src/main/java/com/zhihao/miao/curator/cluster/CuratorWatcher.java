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
 * 1.����һ���ͻ��˶�/super�ڵ���в���������ɾ������ô������������ڵ�Ŀͻ���Ҳ��֪���ģ�Ҳ����������Ӧ�Ĳ��������demo����test����ͻ��˶�/super����ڵ��������ɾ���ģ�����
 * ����client1��client2Ҳ�Ǽ������ģ�
 * 
 * 2.�������һ���ͻ��˶�/super�����˶����ڵ㣬c1��c2�������޸���valueֵ���´���ȥ����zookeeper��ʱ�򣬻��ǻ����������仯��Ҳ�����ظ�ע�����˼����������ڷֲ�ʽϵͳ�����ã�����һЩ���ò���
 * �޸Ļ�������������ϵͳ���ǹ���������õĻ����ͻ�һ������ע��ʹ��������õĲ�����Ҳ����zookeeper�ķ����Ͷ���ģʽ
 * 
 * 3.zookeeper���˿���ʵ��DistributedDoubleBarrier��DistributedBarrier��������ʵ��DistributedQueue��DistributedDelayQueue��
 * DistributedPriorityQueue
 *
 */
public class CuratorWatcher {

	/** ���ڵ�path */
	static final String PARENT_PATH = "/super";
	
	/** zookeeper��������ַ */
	public static final String CONNECT_ADDR = "192.168.5.124:2181,192.168.5.125:2181,192.168.5.126:2181";	/** ����sessionʧЧʱ�� */
	
	public static final int SESSION_TIMEOUT = 30000;
	
	public CuratorWatcher() throws Exception{
		//1 ���Բ��ԣ�����ʱ��Ϊ1s ����10��
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
		//2 ͨ��������������
		CuratorFramework cf = CuratorFrameworkFactory.builder()
					.connectString(CONNECT_ADDR)
					.sessionTimeoutMs(SESSION_TIMEOUT)
					.retryPolicy(retryPolicy)
					.build();
		//3 ��������
		cf.start();
		
		//4 �������ڵ�
		if(cf.checkExists().forPath(PARENT_PATH) == null){
			cf.create().withMode(CreateMode.PERSISTENT).forPath(PARENT_PATH,"super init".getBytes());
		}

		//4 ����һ��PathChildrenCache����,����������Ϊ�Ƿ���ܽڵ��������� ���Ϊfalse�򲻽���
		PathChildrenCache cache = new PathChildrenCache(cf, PARENT_PATH, true);
		//5 �ڳ�ʼ����ʱ��ͽ��л������
		cache.start(StartMode.POST_INITIALIZED_EVENT);
		cache.getListenable().addListener(new PathChildrenCacheListener() {
			/**
			 * <B>�������ƣ�</B>�����ӽڵ���<BR>
			 * <B>��Ҫ˵����</B>�½����޸ġ�ɾ��<BR>
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
