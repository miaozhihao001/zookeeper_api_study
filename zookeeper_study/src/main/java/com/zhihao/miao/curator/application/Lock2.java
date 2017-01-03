package com.zhihao.miao.curator.application;

import java.util.concurrent.CountDownLatch;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * ��һ̨�������Ͽ���reentrantLock�����õģ���������������ڶ���ڵ�Ļ��Ͳ�����Ч����Ҳ���Ƿֲ�ʽ����Ӧ�ó�����
 * ʹ��InterProcessMutex�����зֲ�ʽ����������
 *
 */
public class Lock2 {

	/** zookeeper��ַ */
	static final String CONNECT_ADDR = "192.168.5.124:2181,192.168.5.125:2181,192.168.5.126:2181";
	/** session��ʱʱ�� */
	static final int SESSION_OUTTIME = 5000;// ms

	static int count = 10;

	public static void genarNo() {
		try {
			count--;
			System.out.println(count);
		} finally {

		}
	}

	public static void main(String[] args) throws Exception {
		final CountDownLatch countdown = new CountDownLatch(1);
		// ����������ʮ���߳�Ϊ��ģ��ʮ̨������������InterProcessMutex��ReentrantLock��߱����������߳��еľֲ�������������ʮ̨��������ÿ̨������������ݱ���
		// ʹ��ReentrantLock�ͻ����ʮ̨������������ִ��
		for (int i = 0; i < 10; i++) {
			new Thread(new Runnable() {
				public void run() {
					CuratorFramework cf = createCuratorFramework();
					cf.start();
					final InterProcessMutex lock = new InterProcessMutex(cf,
							"/super");
					// final ReentrantLock reentrantLock = new ReentrantLock();
					try {
						countdown.await();
						lock.acquire(); // ����һ�����
						// reentrantLock.lock();
						System.out.println(Thread.currentThread().getName()
								+ "ִ��ҵ���߼�..");
						Thread.sleep(1000);
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						try {
							// �ͷ�
							lock.release(); // �ͷ���
							// reentrantLock.unlock();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}, "t" + i).start();
		}
		Thread.sleep(100);
		countdown.countDown();
	}

	public static CuratorFramework createCuratorFramework() {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
		CuratorFramework cf = CuratorFrameworkFactory.builder()
				.connectString(CONNECT_ADDR).sessionTimeoutMs(SESSION_OUTTIME)
				.retryPolicy(retryPolicy).build();
		return cf;
	}
}
