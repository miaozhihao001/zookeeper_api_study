package com.zhihao.miao.orgin.api;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 * Zookeeper Wathcher 
 * �������һ��Watcher�ࣨʵ����org.apache.zookeeper.Watcher�ࣩ
 * ZooKeeperWatcherʵ����Watcher����д��process����
 * ���γ���һ���������߳�һ�������̻߳���һ��process�������ڵ��̣߳����нڵ�ļ��
 * 
 * zookeeper��ؽڵ�Ҫ��Watcher��ؽڵ���¼����ͣ�����Ҫָ��booleanΪtrue��һ���ڷ����е����һ��������ָ��watcherΪtrue���֮��������������أ���Ҫ�ٴ��趨
 * watcherΪtrue������Ŀ����룬���demo�кܶ��װ�ķ��������������watcherΪtrue�ڷ�װ�������趨�ˣ���ʵ��һ����
 * 
 * zkWatch.getChildren(CHILDREN_PATH+"/c1", true);
 * zkWatch.createPath(CHILDREN_PATH+"/c1/c2", System.currentTimeMillis() + "", true);
 * 
 * Ҫ���c1�½ڵ��NodeChildrenChanged���ͱ���ָ���丸�ڵ㣬����watcherΪtrue����zkWatch.createPath
 * (CHILDREN_PATH+"/c1/c2", System.currentTimeMillis() + "", true);���trueֻ�Ǽ��NodeCreated�¼�����
 * 
 */
public class ZooKeeperWatcher implements Watcher {

	/** ����ԭ�ӱ��� */
	AtomicInteger seq = new AtomicInteger();
	/** ����sessionʧЧʱ�� */
	private static final int SESSION_TIMEOUT = 10000;
	/** zookeeper��������ַ */
	private static final String CONNECTION_ADDR = "192.168.5.124:2181,192.168.5.125:2181,192.168.5.126:2181";
	/** zk��·������ */
	private static final String PARENT_PATH = "/p";
	/** zk��·������ */
	private static final String CHILDREN_PATH = "/p/c";
	/** �����ʶ */
	private static final String LOG_PREFIX_OF_MAIN = "��Main��";
	/** zk���� */
	private ZooKeeper zk = null;
	/** �ź������ã����ڵȴ�zookeeper���ӽ���֮�� ֪ͨ���������������ִ�� */
	private CountDownLatch connectedSemaphore = new CountDownLatch(1);

	/**
	 * ����ZK����
	 * @param connectAddr ZK��������ַ�б�
	 * @param sessionTimeout Session��ʱʱ��
	 */
	public void createConnection(String connectAddr, int sessionTimeout) {
		//֮ǰ�������zk���ӵĻ�����close������ȥ��������
		this.releaseConnection();
		try {
			zk = new ZooKeeper(connectAddr, sessionTimeout, this);
			System.out.println(LOG_PREFIX_OF_MAIN + "��ʼ����ZK������");
			connectedSemaphore.await();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * �ر�ZK����
	 */
	public void releaseConnection() {
		if (this.zk != null) {
			try {
				this.zk.close();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * �����ڵ�
	 * @param path �ڵ�·��
	 * @param data ��������
	 * @return 
	 */
	public boolean createPath(String path, String data,boolean needWatch) {
		try {
			//���ü��(����zookeeper�ļ�ض���һ���Ե����� ÿ�α������ü��),���needWatch��true��ʾ����createPath�����������ģ���ʱ����Main�е�ZooKeeperWatcher������
			//��true�Ļ��ͱ�ʾZooKeeperWatcher������Main�еĲ�������false�����ڵڶ�����������new Watcher()��ʾ���������ĵ�Watcher���������Main�̵߳Ĳ�����Ҳ�Ͳ�����������
			//��ZooKeeperWatcher�е�process()����
			this.zk.exists(path, needWatch);
			System.out.println(LOG_PREFIX_OF_MAIN + "�ڵ㴴���ɹ�, Path: " + 
							   this.zk.create(	/**·��*/ 
									   			path, 
									   			/**����*/
									   			data.getBytes(), 
									   			/**���пɼ�*/
								   				Ids.OPEN_ACL_UNSAFE, 
								   				/**���ô洢*/
								   				CreateMode.PERSISTENT ) + 	
							   ", content: " + data);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * ��ȡָ���ڵ���������
	 * @param path �ڵ�·��
	 * @return
	 */
	public String readData(String path, boolean needWatch) {
		try {
			return new String(this.zk.getData(path, needWatch, null));
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * ����ָ���ڵ���������
	 * @param path �ڵ�·��
	 * @param data ��������
	 * @return
	 */
	public boolean writeData(String path, String data) {
		try {
			System.out.println(LOG_PREFIX_OF_MAIN + "�������ݳɹ���path��" + path + ", stat: " +
								this.zk.setData(path, data.getBytes(), -1));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * ɾ��ָ���ڵ�
	 * 
	 * @param path
	 *            �ڵ�path
	 */
	public void deleteNode(String path) {
		try {
			this.zk.delete(path, -1);
			System.out.println(LOG_PREFIX_OF_MAIN + "ɾ���ڵ�ɹ���path��" + path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * �ж�ָ���ڵ��Ƿ����
	 * @param path �ڵ�·��
	 */
	public Stat exists(String path, boolean needWatch) {
		try {
			return this.zk.exists(path, needWatch);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * ��ȡ�ӽڵ�
	 * @param path �ڵ�·��
	 */
	private List<String> getChildren(String path, boolean needWatch) {
		try {
			return this.zk.getChildren(path, needWatch);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * ɾ�����нڵ�
	 */
	public void deleteAllTestPath() {
		if(this.exists(CHILDREN_PATH, false) != null){
			this.deleteNode(CHILDREN_PATH);
		}
		if(this.exists(PARENT_PATH, false) != null){
			this.deleteNode(PARENT_PATH);
		}		
	}
	
	/**
	 * �յ�����Server��Watcher֪ͨ��Ĵ���
	 */
	@Override
	public void process(WatchedEvent event) {
		
		System.out.println("���� process ����������event = " + event);
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (event == null) {
			return;
		}
		
		// ����״̬
		KeeperState keeperState = event.getState();
		// �¼�����
		EventType eventType = event.getType();
		// ��Ӱ���path
		String path = event.getPath();
		//ԭ�Ӷ���seq��¼����process�Ĵ���
		String logPrefix = "��Watcher-" + this.seq.incrementAndGet() + "��";

		System.out.println(logPrefix + "�յ�Watcher֪ͨ");
		System.out.println(logPrefix + "����״̬:\t" + keeperState.toString());
		System.out.println(logPrefix + "�¼�����:\t" + eventType.toString());
       
		 //KeeperState.SyncConnected������zk��ʱ����ȥ���¼����ͽ��д���
		if (KeeperState.SyncConnected == keeperState) {   
			// �ɹ�������ZK����������һ������zk��������ʱ��ִ��countDown()����
			if (EventType.None == eventType) {
				System.out.println(logPrefix + "�ɹ�������ZK������");
				connectedSemaphore.countDown();
			} 
			//�����ڵ�
			else if (EventType.NodeCreated == eventType) {
				System.out.println(logPrefix + "�ڵ㴴��");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				this.exists(path, true);
			} 
			//���½ڵ�
			else if (EventType.NodeDataChanged == eventType) {
				System.out.println(logPrefix + "�ڵ����ݸ���");
				System.out.println("�ҿ����߲�������........");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println(logPrefix + "��������: " + this.readData(PARENT_PATH, true));
			} 
			//�����ӽڵ�
			else if (EventType.NodeChildrenChanged == eventType) {
				System.out.println(logPrefix + "�ӽڵ���");
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println(logPrefix + "�ӽڵ��б�" + this.getChildren(PARENT_PATH, true));
			} 
			//ɾ���ڵ�
			else if (EventType.NodeDeleted == eventType) {
				System.out.println(logPrefix + "�ڵ� " + path + " ��ɾ��");
			}
			else ;
		} 
		//����ʧ��
		else if (KeeperState.Disconnected == keeperState) {
			System.out.println(logPrefix + "��ZK�������Ͽ�����");
		} 
		//��֤ʧ��
		else if (KeeperState.AuthFailed == keeperState) {
			System.out.println(logPrefix + "Ȩ�޼��ʧ��");
		} 
		//�ỰʧЧ
		else if (KeeperState.Expired == keeperState) {
			System.out.println(logPrefix + "�ỰʧЧ");
		}
		else ;

		System.out.println("--------------------------------------------");

	}

	/**
	 * <B>�������ƣ�</B>����zookeeper���<BR>
	 * <B>��Ҫ˵����</B>��Ҫ����watch����<BR>
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		//����watcher
		ZooKeeperWatcher zkWatch = new ZooKeeperWatcher();
		//��������
		zkWatch.createConnection(CONNECTION_ADDR, SESSION_TIMEOUT);
		//System.out.println(zkWatch.zk.toString());
		
		Thread.sleep(1000);
		
		// ����ڵ�
		//zkWatch.deleteAllTestPath();
		
		//-------------------------��һ�����������ڵ� /p-------------------------
	   if (zkWatch.createPath(PARENT_PATH, System.currentTimeMillis() + "",true)) {
		//if (zkWatch.createPath(PARENT_PATH, System.currentTimeMillis() + "",false)) {
			
			Thread.sleep(1000);
			
			
			// ��ȡ����
			System.out.println("---------------------- read parent ----------------------------");
			//��������ڵ㣬����һ�ҪzkWatch����ҵ��¼����ͣ���ô����Ҫ�ڽڵ�仯����ǰ����������������е�һ������ȥ���ü�����ִ�������
			//zkWatch.writeData(PARENT_PATH, System.currentTimeMillis() + "");�����Ϳ��Լ�����
			zkWatch.readData(PARENT_PATH, true);
			//zkWatch.exists(PARENT_PATH, true);
			
			// ��ȡ�ӽڵ�
			//System.out.println("---------------------- read children path ----------------------------");
			//����Ǽ���ӽڵ��NodeChildrenChanged��
			zkWatch.getChildren(PARENT_PATH, true);

			// ��������
			zkWatch.writeData(PARENT_PATH, System.currentTimeMillis() + "");
			
			Thread.sleep(1000);
			
			// �����ӽڵ�
			zkWatch.createPath(CHILDREN_PATH, System.currentTimeMillis() + "",true);
			
			//--------------------�������������ӽڵ�Ĵ���
			//zkWatch.createPath(CHILDREN_PATH+"/c1", System.currentTimeMillis() + "", true);  
			//��ߵ�trueֻ�Ǽ��NodeCreated�¼��������Ҫ���NodeChildrenChanged�������������ǰ����zkWatch.getChildren(CHILDREN_PATH, true);
			//zkWatch.getChildren(CHILDREN_PATH, true);
			//zkWatch.createPath(CHILDREN_PATH+"/c1", System.currentTimeMillis() + "", true);   
			//zkWatch.createPath(CHILDREN_PATH+"/c1/c2", System.currentTimeMillis() + "", true);���trueֻ�Ǽ��/p/c/c1/c2����ڵ��NodeCreated�¼���
			//�����Ҫ���NodeChildrenChanged�������������ǰ����zkWatch.getChildren(CHILDREN_PATH, true);
			//zkWatch.getChildren(CHILDREN_PATH+"/c1", true);
			//zkWatch.createPath(CHILDREN_PATH+"/c1/c2", System.currentTimeMillis() + "", true);
			
			//--------------------���Ĳ�:�����ӽڵ����ݵĴ���--------------
			//�ڽ����޸�֮ǰ��������Ҫwatchһ������ڵ�;
			//Thread.sleep(1000);
			//zkWatch.readData(CHILDREN_PATH, true);
			//zkWatch.writeData(CHILDREN_PATH, System.currentTimeMillis() + "");
		}
		
		//Thread.sleep(50000);
		// ����ڵ�
		zkWatch.deleteAllTestPath();
		
		Thread.sleep(10000);
		zkWatch.releaseConnection();
	}
	}

