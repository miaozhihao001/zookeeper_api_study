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
	
	/** zookeeper地址 */
	static final String CONNECT_ADDR = "192.168.5.124:2181,192.168.5.125:2181,192.168.5.126:2181";
	/** session超时时间 */
	static final int SESSION_OUTTIME = 5000;//ms 
	
	public static void main(String[] args) throws Exception {
		
		//1 重试策略：初试时间为1s 重试10次
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
		//2 通过工厂创建连接
		CuratorFramework cf = CuratorFrameworkFactory.builder()
					.connectString(CONNECT_ADDR)
					.sessionTimeoutMs(SESSION_OUTTIME)    
					.retryPolicy(retryPolicy)
					.build();
		//3 开启连接
		cf.start();
		
		// 新加、删除
		//4creatingParentsIfNeeded是否需要创建父节点，也就是递归创建
		//建立节点 指定节点类型（不加withMode默认为持久类型节点）、路径、数据内容
		//cf.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/super/c1","c1内容".getBytes());
		//5 删除节点，deletingChildrenIfNeeded删除子节点
		//cf.delete().guaranteed().deletingChildrenIfNeeded().forPath("/super");
		
		// 读取、修改
		//创建节点
		cf.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/super/c1","c1内容".getBytes());
		cf.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/super/c2","c2内容".getBytes());
		//读取节点
		String ret1 = new String(cf.getData().forPath("/super/c2"));
		System.out.println(ret1);
		//修改节点
		cf.setData().forPath("/super/c2", "修改c2内容".getBytes());
		String ret2 = new String(cf.getData().forPath("/super/c2"));
		System.out.println(ret2);	
		
		// 绑定回调函数
		ExecutorService pool = Executors.newCachedThreadPool();
		cf.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
		.inBackground(new BackgroundCallback() {
			@Override
			public void processResult(CuratorFramework cf, CuratorEvent event) throws Exception {
				System.out.println("code:" + event.getResultCode());  //服务器的状态码，0
				System.out.println("type:" + event.getType());  //服务器事件类型，create
				System.out.println("线程为:" + Thread.currentThread().getName());
			}
		}, pool)   //线程池的意义是在批量处理节点的时候可以使效率更高一点
		.forPath("/super/c3","c3内容".getBytes());
		Thread.sleep(Integer.MAX_VALUE);
		
		// 读取子节点getChildren方法 和 判断节点是否存在checkExists方法
		List<String> list = cf.getChildren().forPath("/super");
		for(String p : list){
			System.out.println(p);
		}
		
		//stat不为null表示存在，为null表示为存在
		Stat stat = cf.checkExists().forPath("/super/c3");
		System.out.println(stat);
		
		//删除（递归删除）
		Thread.sleep(2000);
		cf.delete().guaranteed().deletingChildrenIfNeeded().forPath("/super");
		
		cf.close();
		
	}
}
