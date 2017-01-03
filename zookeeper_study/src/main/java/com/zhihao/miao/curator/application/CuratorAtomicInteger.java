package com.zhihao.miao.curator.application;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;

public class CuratorAtomicInteger {

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
		//cf.create().forPath("/super");
		//cf.delete().forPath("/super");
		

		//4 ʹ��DistributedAtomicInteger��RetryNTimes��������˼�����������Σ����ǰһ������û�гɹ��Ļ����Եļ��Ϊ1000ms��
		DistributedAtomicInteger atomicIntger = 
				new DistributedAtomicInteger(cf, "/super", new RetryNTimes(3, 1000));
		//atomicIntger.forceSet(0);
		AtomicValue<Integer> value = atomicIntger.add(1);
		System.out.println(value.succeeded());
		System.out.println(value.preValue());	//ԭʼֵ
		System.out.println(value.postValue());	//����ֵ
		
	}
}
