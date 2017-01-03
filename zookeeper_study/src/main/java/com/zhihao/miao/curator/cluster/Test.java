package com.zhihao.miao.curator.cluster;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

public class Test {

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

		
		//Thread.sleep(3000);
		//System.out.println(cf.getChildren().forPath("/super").get(0));
		
		//4 �����ڵ�
//		Thread.sleep(1000);
		cf.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/super/c1","c1����".getBytes());
		Thread.sleep(1000);
		cf.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/super/c2","c2����".getBytes());
		Thread.sleep(1000);
//		
//		
//		
//		//5 ��ȡ�ڵ�
//		Thread.sleep(1000);
//		String ret1 = new String(cf.getData().forPath("/super/c1"));
//		System.out.println(ret1);
//
//		
//		//6 �޸Ľڵ�
		Thread.sleep(1000);
		cf.setData().forPath("/super/c2", "�޸ĵ���c2����".getBytes());
		String ret2 = new String(cf.getData().forPath("/super/c2"));
		

		
		//7 ɾ���ڵ�
		Thread.sleep(1000);
		cf.delete().forPath("/super/c1");
		
		Thread.sleep(1000);
		cf.close();
		
		
		
		
	}
}
