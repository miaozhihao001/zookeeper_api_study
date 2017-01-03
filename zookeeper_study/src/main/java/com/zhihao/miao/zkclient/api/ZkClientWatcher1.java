package com.zhihao.miao.zkclient.api;

import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

public class ZkClientWatcher1 {

	/** zookeeper地址 */
	static final String CONNECT_ADDR = "192.168.5.124:2181,192.168.5.125:2181,192.168.5.126:2181";
	/** session超时时间 */
	static final int SESSION_OUTTIME = 5000;//ms 
	
	
	public static void main(String[] args) throws Exception {
		ZkClient zkc = new ZkClient(new ZkConnection(CONNECT_ADDR), 5000);
		
		//对父节点添加监听子节点变化。subscribeChildChanges只监听子节点的新增和删除或者本身节点的新增和删除，不监听节点数据的修改操作
		zkc.subscribeChildChanges("/super", new IZkChildListener() {
			@Override
			//参数parentPath为所监听节点的全路径，currentChilds为最新的子节点列表（相对路径）。
			public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
				System.out.println("parentPath: " + parentPath);
				System.out.println("currentChilds: " + currentChilds);
			}
		});
		
		Thread.sleep(3000);
		
		zkc.createPersistent("/super");
		Thread.sleep(1000);
		
		//也没有监听/super节点的update操作
		zkc.writeData("/super", "aaa");
		Thread.sleep(1000);
		
		zkc.createPersistent("/super" + "/" + "c1", "c1内容");
		Thread.sleep(1000);
		
		zkc.createPersistent("/super" + "/" + "c2", "c2内容");
		Thread.sleep(1000);		
		
	    //并不会监听自节点的update操作
		zkc.writeData("/super"+"/"+"c1", "新内容");
		Thread.sleep(1000);
		
		zkc.delete("/super/c2");
		Thread.sleep(1000);	
		
		zkc.deleteRecursive("/super");
		Thread.sleep(Integer.MAX_VALUE);
		
		
	}
}
