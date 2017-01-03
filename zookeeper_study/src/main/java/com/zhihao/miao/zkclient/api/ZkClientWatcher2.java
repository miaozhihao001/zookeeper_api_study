package com.zhihao.miao.zkclient.api;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

public class ZkClientWatcher2 {

	/** zookeeper��ַ */
	static final String CONNECT_ADDR = "192.168.5.124:2181,192.168.5.125:2181,192.168.5.126:2181";
	/** session��ʱʱ�� */
	static final int SESSION_OUTTIME = 5000;//ms 
	
	
	public static void main(String[] args) throws Exception {
		ZkClient zkc = new ZkClient(new ZkConnection(CONNECT_ADDR), 5000);
		
		zkc.createPersistent("/super", "1234");
		
		//�Ը��ڵ���Ӽ����ӽڵ�仯��subscribeDataChanges��������ǶԽڵ����ݷ����޸ĺ�ɾ��ʱ����м��
		zkc.subscribeDataChanges("/super", new IZkDataListener() {
			@Override
			public void handleDataDeleted(String path) throws Exception {
				System.out.println("ɾ���Ľڵ�Ϊ:" + path);
			}
			
			@Override
			public void handleDataChange(String path, Object data) throws Exception {
				System.out.println("����Ľڵ�Ϊ:" + path + ", �������Ϊ:" + data);
			}
		});
		
		Thread.sleep(3000);
		zkc.writeData("/super", "456", -1);
		Thread.sleep(1000);

		zkc.delete("/super");
		Thread.sleep(Integer.MAX_VALUE);
		
		
	}
}
