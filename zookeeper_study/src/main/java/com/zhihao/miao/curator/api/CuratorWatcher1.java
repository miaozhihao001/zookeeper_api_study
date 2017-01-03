package com.zhihao.miao.curator.api;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class CuratorWatcher1 {
	
	/** zookeeper��ַ */
	static final String CONNECT_ADDR = "192.168.5.124:2181,192.168.5.125:2181,192.168.5.126:2181";
	/** session��ʱʱ�� */
	static final int SESSION_OUTTIME = 5000;//ms 
	
	public static void main(String[] args) throws Exception {
		
		//1 ���Բ��ԣ�����ʱ��Ϊ1s ����10��
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
		//2 ͨ��������������
		CuratorFramework cf = CuratorFrameworkFactory.builder()
					.connectString(CONNECT_ADDR)
					.sessionTimeoutMs(SESSION_OUTTIME)
					.retryPolicy(retryPolicy)
					.build();
		
		//3 ��������
		cf.start();
		
		//4 ����һ��cache���棬��������������˼���Ƿ�֧��ѹ��
		final NodeCache cache = new NodeCache(cf, "/super", false);
		cache.start(true);
		
		//�ڻ���ڵ��ϼ�һ��Listener
		cache.getListenable().addListener(new NodeCacheListener() {
			/**
			 * <B>�������ƣ�</B>nodeChanged<BR>
			 * <B>��Ҫ˵����</B>�����¼�Ϊ�����ڵ�͸��½ڵ㣬��ɾ���ڵ��ʱ�򲢲������˲�����<BR>
			 * @see org.apache.curator.framework.recipes.cache.NodeCacheListener#nodeChanged()
			 */
			@Override
			public void nodeChanged() throws Exception {
				System.out.println("·��Ϊ��" + cache.getCurrentData().getPath());
				System.out.println("����Ϊ��" + new String(cache.getCurrentData().getData()));
				System.out.println("״̬Ϊ��" + cache.getCurrentData().getStat());
				System.out.println("---------------------------------------");
			}
		});
		
		Thread.sleep(1000);
		cf.create().forPath("/super", "123".getBytes());
		
		Thread.sleep(1000);
		cf.setData().forPath("/super", "456".getBytes());
		
		Thread.sleep(1000);
		cf.delete().forPath("/super");
		
		Thread.sleep(Integer.MAX_VALUE);
		
		

	}
}
