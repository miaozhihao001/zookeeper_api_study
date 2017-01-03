package com.zhihao.miao.curator.application;

import java.util.Random;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.barriers.DistributedDoubleBarrier;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class CuratorBarrier1 {

	/** zookeeper��ַ */
	static final String CONNECT_ADDR = "192.168.5.124:2181,192.168.5.125:2181,192.168.5.126:2181";
	/** session��ʱʱ�� */
	static final int SESSION_OUTTIME = 5000;//ms 
	
	public static void main(String[] args) throws Exception {
		
		
		
		for(int i = 0; i < 5; i++){
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						//��ЩRetryPolicy��CuratorFramework�������߳���ȥ����������Ϊ��ģ����jvm����
						RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
						CuratorFramework cf = CuratorFrameworkFactory.builder()
									.connectString(CONNECT_ADDR)
									.retryPolicy(retryPolicy)
									.build();
						cf.start();
						
						
						//����ͻ���ͬʱ׼�����У�ͬʱ���н�������������������5���ͻ��˳���,��ʵ����5���ͻ���ͬʱ��enter��ʼ���У���׼���õ�ҲҪ����׼���õģ�����ͻ���ͬʱ���н����������н�����ҲҪ����leaveǰ�ȴ������ĳ���һ�����һ�����
						DistributedDoubleBarrier barrier = new DistributedDoubleBarrier(cf, "/super", 5);
						Thread.sleep(1000 * (new Random()).nextInt(8)); 
						System.out.println(Thread.currentThread().getName() + "�Ѿ�׼��");
						barrier.enter();
						System.out.println("ͬʱ��ʼ����...");
						Thread.sleep(1000 * (new Random()).nextInt(8));
						System.out.println(Thread.currentThread().getName() + "�������");
						barrier.leave();
						System.out.println("ͬʱ�˳�����...");
						

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			},"t" + i).start();
		}

		
		
	}
}

