package com.zhihao.miao.curator.api;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

public class CuratorBase {
	
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
		
		// �¼ӡ�ɾ��
		//4creatingParentsIfNeeded�Ƿ���Ҫ�������ڵ㣬Ҳ���ǵݹ鴴��
		//�����ڵ� ָ���ڵ����ͣ�����withModeĬ��Ϊ�־����ͽڵ㣩��·������������
		//cf.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/super/c1","c1����".getBytes());
		//5 ɾ���ڵ㣬deletingChildrenIfNeededɾ���ӽڵ�
		//cf.delete().guaranteed().deletingChildrenIfNeeded().forPath("/super");
		
		// ��ȡ���޸�
		//�����ڵ�
		cf.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/super/c1","c1����".getBytes());
		cf.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/super/c2","c2����".getBytes());
		//��ȡ�ڵ�
		String ret1 = new String(cf.getData().forPath("/super/c2"));
		System.out.println(ret1);
		//�޸Ľڵ�
		cf.setData().forPath("/super/c2", "�޸�c2����".getBytes());
		String ret2 = new String(cf.getData().forPath("/super/c2"));
		System.out.println(ret2);	
		
		// �󶨻ص�����
		ExecutorService pool = Executors.newCachedThreadPool();
		cf.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
		.inBackground(new BackgroundCallback() {
			@Override
			public void processResult(CuratorFramework cf, CuratorEvent event) throws Exception {
				System.out.println("code:" + event.getResultCode());  //��������״̬�룬0
				System.out.println("type:" + event.getType());  //�������¼����ͣ�create
				System.out.println("�߳�Ϊ:" + Thread.currentThread().getName());
			}
		}, pool)   //�̳߳ص�����������������ڵ��ʱ�����ʹЧ�ʸ���һ��
		.forPath("/super/c3","c3����".getBytes());
		Thread.sleep(Integer.MAX_VALUE);
		
		// ��ȡ�ӽڵ�getChildren���� �� �жϽڵ��Ƿ����checkExists����
		List<String> list = cf.getChildren().forPath("/super");
		for(String p : list){
			System.out.println(p);
		}
		
		//stat��Ϊnull��ʾ���ڣ�Ϊnull��ʾΪ����
		Stat stat = cf.checkExists().forPath("/super/c3");
		System.out.println(stat);
		
		//ɾ�����ݹ�ɾ����
		Thread.sleep(2000);
		cf.delete().guaranteed().deletingChildrenIfNeeded().forPath("/super");
		
		cf.close();
		
	}
}
