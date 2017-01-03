package com.zhihao.miao.curator.application;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * ���demo�������DistributedDoubleBarrier���demo��һ��֮������һ��ʼ���У�����Ҫ��һ�����н�����
 * �������DistributedDoubleBarrier��demoҪһ��ʼ���У�һ���������
 *
 */
public class CuratorBarrier2 {

	/** zookeeper��ַ */
	static final String CONNECT_ADDR = "192.168.5.124:2181,192.168.5.125:2181,192.168.5.126:2181";
	/** session��ʱʱ�� */
	static final int SESSION_OUTTIME = 5000;//ms 
	
	static DistributedBarrier barrier = null;
	
	public static void main(String[] args) throws Exception {
		
		
		
		for(int i = 0; i < 5; i++){
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
						CuratorFramework cf = CuratorFrameworkFactory.builder()
									.connectString(CONNECT_ADDR)
									.sessionTimeoutMs(SESSION_OUTTIME)
									.retryPolicy(retryPolicy)
									.build();
						cf.start();
						barrier = new DistributedBarrier(cf, "/super");
						System.out.println(Thread.currentThread().getName() + "����barrier!");
						barrier.setBarrier();	//����
						barrier.waitOnBarrier();	//�ȴ�
						System.out.println("---------��ʼִ�г���----------");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			},"t" + i).start();
		}

		Thread.sleep(5000);
		//��������������ķֲ�ʽϵͳ�л����һ��������Ŀ�У�������jar��������5������Ŀ��������������ʵ�ֳ���դ����Ȼ������ֲ�ʽϵͳһ������
		barrier.removeBarrier();	//�ͷ�
		
		
	}
}
