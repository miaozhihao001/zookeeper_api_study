package com.zhihao.miao.orgin.api.cluster;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

public class ZKWatcher implements Watcher {

	/** zk���� */
	private ZooKeeper zk = null;
	
	/** ���ڵ�path */
	static final String PARENT_PATH = "/super";
	
	/** �ź������ã����ڵȴ�zookeeper���ӽ���֮�� ֪ͨ���������������ִ�� */
	private CountDownLatch connectedSemaphore = new CountDownLatch(1);
	
	private List<String> cowaList = new CopyOnWriteArrayList<String>();
	
	
	/** zookeeper��������ַ */
	public static final String CONNECTION_ADDR = "192.168.5.124:2181,192.168.5.125:2181,192.168.5.126:2181";
	/** ����sessionʧЧʱ�� */
	public static final int SESSION_TIMEOUT = 30000;
	
	public ZKWatcher() throws Exception{
		zk = new ZooKeeper(CONNECTION_ADDR, SESSION_TIMEOUT, this);
		System.out.println("��ʼ����ZK������");
		connectedSemaphore.await();
	}


	@Override
	public void process(WatchedEvent event) {
		// ����״̬
		KeeperState keeperState = event.getState();
		// �¼�����
		EventType eventType = event.getType();
		// ��Ӱ���path
		String path = event.getPath();
		System.out.println("��Ӱ���path : " + path);
		
		
		if (KeeperState.SyncConnected == keeperState) {
			// �ɹ�������ZK������
			if (EventType.None == eventType) {
				System.out.println("�ɹ�������ZK������");
				connectedSemaphore.countDown();
				try {
					if(this.zk.exists(PARENT_PATH, false) == null){
						this.zk.create(PARENT_PATH, "root".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);		 
					}
					List<String> paths = this.zk.getChildren(PARENT_PATH, true);
					for (String p : paths) {
						System.out.println(p);
						this.zk.exists(PARENT_PATH + "/" + p, true);
					}
				} catch (KeeperException | InterruptedException e) {
					e.printStackTrace();
				}		
			} 
			//�����ڵ�
			else if (EventType.NodeCreated == eventType) {
				System.out.println("�ڵ㴴��");
				try {
					this.zk.exists(path, true);
				} catch (KeeperException | InterruptedException e) {
					e.printStackTrace();
				}
			} 
			//���½ڵ�
			else if (EventType.NodeDataChanged == eventType) {
				System.out.println("�ڵ����ݸ���");
				try {
					//update nodes  call function
					this.zk.exists(path, true);
				} catch (KeeperException | InterruptedException e) {
					e.printStackTrace();
				}
			} 
			//�����ӽڵ�
			else if (EventType.NodeChildrenChanged == eventType) {
				System.out.println("�ӽڵ� ... ���");
				try {
					List<String> paths = this.zk.getChildren(path, true);
					if(paths.size() >= cowaList.size()){
						paths.removeAll(cowaList);
						for(String p : paths){
							this.zk.exists(path + "/" + p, true);
							//this.zk.getChildren(path + "/" + p, true);
							System.out.println("������������ӽڵ� : " + path + "/" + p);
							//add new nodes  call function
						}
						cowaList.addAll(paths);
					} else {
						cowaList = paths;
					}
					System.out.println("cowaList: " + cowaList.toString());
					System.out.println("paths: " + paths.toString());
					
				} catch (KeeperException | InterruptedException e) {
					e.printStackTrace();
				}
			} 
			//ɾ���ڵ�
			else if (EventType.NodeDeleted == eventType) {
				System.out.println("�ڵ� " + path + " ��ɾ��");
				try {
					//delete nodes  call function
					this.zk.exists(path, true);
				} catch (KeeperException | InterruptedException e) {
					e.printStackTrace();
				}
			}
			else ;
		} 
		else if (KeeperState.Disconnected == keeperState) {
			System.out.println("��ZK�������Ͽ�����");
		} 
		else if (KeeperState.AuthFailed == keeperState) {
			System.out.println("Ȩ�޼��ʧ��");
		} 
		else if (KeeperState.Expired == keeperState) {
			System.out.println("�ỰʧЧ");
		}
		else ;

		System.out.println("--------------------------------------------");
	}
	
	

}
