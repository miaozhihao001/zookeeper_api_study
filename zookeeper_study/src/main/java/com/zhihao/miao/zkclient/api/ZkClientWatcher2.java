package com.zhihao.miao.zkclient.api;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

public class ZkClientWatcher2 {

	/** zookeeper地址 */
	static final String CONNECT_ADDR = "192.168.5.124:2181,192.168.5.125:2181,192.168.5.126:2181";
	/** session超时时间 */
	static final int SESSION_OUTTIME = 5000;//ms 
	
	
	public static void main(String[] args) throws Exception {
		ZkClient zkc = new ZkClient(new ZkConnection(CONNECT_ADDR), 5000);
		
		zkc.createPersistent("/super", "1234");
		
		//对父节点添加监听子节点变化。subscribeDataChanges这个方法是对节点数据发生修改和删除时候进行监控
		zkc.subscribeDataChanges("/super", new IZkDataListener() {
			@Override
			public void handleDataDeleted(String path) throws Exception {
				System.out.println("删除的节点为:" + path);
			}
			
			@Override
			public void handleDataChange(String path, Object data) throws Exception {
				System.out.println("变更的节点为:" + path + ", 变更内容为:" + data);
			}
		});
		
		Thread.sleep(3000);
		zkc.writeData("/super", "456", -1);
		Thread.sleep(1000);

		zkc.delete("/super");
		Thread.sleep(Integer.MAX_VALUE);
		
		
	}
}
