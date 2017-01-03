package com.zhihao.miao.zkclient.api;

import java.util.List;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

public class ZkClientBase {

	/** zookeeper地址 */
	static final String CONNECT_ADDR = "192.168.5.124:2181,192.168.5.125:2181,192.168.5.126:2181";
	/** session超时时间 */
	static final int SESSION_OUTTIME = 5000;//ms 
	
	
	public static void main(String[] args) throws Exception {
		ZkClient zkc = new ZkClient(new ZkConnection(CONNECT_ADDR), 10000);
		
		
		//1. create and delete方法 
		zkc.createEphemeral("/temp");
		//递归的创建节点，此时没有/super节点，在源生的API是实现不了的，但是递归的创建不能指定节点的value值
		//zkc.createPersistent("/super/c1", true);
		//zkc.writeData("/super/c1", "hello");
		//String value = zkc.readData("/super/c1");
		//System.out.println(value);
		Thread.sleep(10000);
		//递归的删除
		//zkc.deleteRecursive("/super");
		
		//2. 设置path和data并且读取子节点和每个节点的内容
		zkc.createPersistent("/super", "1234");
		zkc.createPersistent("/super/c1", "c1内容");
		zkc.createPersistent("/super/c2", "c2内容");
		List<String> list = zkc.getChildren("/super");
		for(String p : list){
			System.out.println(p);
			String rp = "/super/" + p;
			String data = zkc.readData(rp);
			System.out.println("节点为：" + rp + "，内容为: " + data);
		}
		
		//3. 更新和判断节点是否存在
		zkc.writeData("/super/c1", "新内容");
		System.out.println(zkc.readData("/super/c1"));
		System.out.println(zkc.exists("/super/c1"));
		
		//4.递归删除/super内容
		zkc.deleteRecursive("/super");		
	}
}
