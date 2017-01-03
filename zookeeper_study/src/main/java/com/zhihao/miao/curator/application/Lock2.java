package com.zhihao.miao.curator.application;

import java.util.concurrent.CountDownLatch;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * 在一台服务器上可以reentrantLock是有用的，但是如果服务部署在多个节点的话就不会有效果，也就是分布式锁的应用场景，
 * 使用InterProcessMutex来进行分布式锁的问题解决
 *
 */
public class Lock2 {

	/** zookeeper地址 */
	static final String CONNECT_ADDR = "192.168.5.124:2181,192.168.5.125:2181,192.168.5.126:2181";
	/** session超时时间 */
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
		// 里面启动了十个线程为了模拟十台服务器，所以InterProcessMutex和ReentrantLock这边变量都是在线程中的局部变量，部署在十台服务器上每台服务器都有这份变量
		// 使用ReentrantLock就会造成十台服务器并发的执行
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
						lock.acquire(); // 给了一个许可
						// reentrantLock.lock();
						System.out.println(Thread.currentThread().getName()
								+ "执行业务逻辑..");
						Thread.sleep(1000);
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						try {
							// 释放
							lock.release(); // 释放锁
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
