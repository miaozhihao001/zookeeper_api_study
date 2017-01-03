package com.zhihao.miao.curator.application;

import java.util.Random;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.barriers.DistributedDoubleBarrier;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class CuratorBarrier1 {

	/** zookeeper地址 */
	static final String CONNECT_ADDR = "192.168.5.124:2181,192.168.5.125:2181,192.168.5.126:2181";
	/** session超时时间 */
	static final int SESSION_OUTTIME = 5000;//ms 
	
	public static void main(String[] args) throws Exception {
		
		
		
		for(int i = 0; i < 5; i++){
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						//这些RetryPolicy和CuratorFramework对象在线程中去创建，就是为了模拟多个jvm环境
						RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
						CuratorFramework cf = CuratorFrameworkFactory.builder()
									.connectString(CONNECT_ADDR)
									.retryPolicy(retryPolicy)
									.build();
						cf.start();
						
						
						//五个客户端同时准备运行，同时运行结束，第三个参数就是5个客户端程序,其实就是5个客户端同时从enter开始运行，早准备好的也要等晚准备好的，五个客户端同时运行结束，早运行结束的也要等在leave前等待其他的程序一起到这边一起结束
						DistributedDoubleBarrier barrier = new DistributedDoubleBarrier(cf, "/super", 5);
						Thread.sleep(1000 * (new Random()).nextInt(8)); 
						System.out.println(Thread.currentThread().getName() + "已经准备");
						barrier.enter();
						System.out.println("同时开始运行...");
						Thread.sleep(1000 * (new Random()).nextInt(8));
						System.out.println(Thread.currentThread().getName() + "运行完毕");
						barrier.leave();
						System.out.println("同时退出运行...");
						

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			},"t" + i).start();
		}

		
		
	}
}

