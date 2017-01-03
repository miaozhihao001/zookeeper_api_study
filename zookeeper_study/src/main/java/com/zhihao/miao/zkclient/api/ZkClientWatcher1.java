package com.zhihao.miao.zkclient.api;

import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

public class ZkClientWatcher1 {

	/** zookeeper��ַ */
	static final String CONNECT_ADDR = "192.168.5.124:2181,192.168.5.125:2181,192.168.5.126:2181";
	/** session��ʱʱ�� */
	static final int SESSION_OUTTIME = 5000;//ms 
	
	
	public static void main(String[] args) throws Exception {
		ZkClient zkc = new ZkClient(new ZkConnection(CONNECT_ADDR), 5000);
		
		//�Ը��ڵ���Ӽ����ӽڵ�仯��subscribeChildChangesֻ�����ӽڵ��������ɾ�����߱���ڵ��������ɾ�����������ڵ����ݵ��޸Ĳ���
		zkc.subscribeChildChanges("/super", new IZkChildListener() {
			@Override
			//����parentPathΪ�������ڵ��ȫ·����currentChildsΪ���µ��ӽڵ��б����·������
			public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
				System.out.println("parentPath: " + parentPath);
				System.out.println("currentChilds: " + currentChilds);
			}
		});
		
		Thread.sleep(3000);
		
		zkc.createPersistent("/super");
		Thread.sleep(1000);
		
		//Ҳû�м���/super�ڵ��update����
		zkc.writeData("/super", "aaa");
		Thread.sleep(1000);
		
		zkc.createPersistent("/super" + "/" + "c1", "c1����");
		Thread.sleep(1000);
		
		zkc.createPersistent("/super" + "/" + "c2", "c2����");
		Thread.sleep(1000);		
		
	    //����������Խڵ��update����
		zkc.writeData("/super"+"/"+"c1", "������");
		Thread.sleep(1000);
		
		zkc.delete("/super/c2");
		Thread.sleep(1000);	
		
		zkc.deleteRecursive("/super");
		Thread.sleep(Integer.MAX_VALUE);
		
		
	}
}
