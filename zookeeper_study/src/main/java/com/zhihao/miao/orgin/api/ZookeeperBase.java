package com.zhihao.miao.orgin.api;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;

public class ZookeeperBase {
	/** zookeeper��ַ */
	static final String CONNECT_ADDR = "192.168.5.124:2181,192.168.5.125:2181,192.168.5.126:2181";
	/** session��ʱʱ�� */
	static final int SESSION_OUTTIME = 2000;//ms 
	/** �ź�������������ִ�У����ڵȴ�zookeeper���ӳɹ������ͳɹ��ź� */
	static final CountDownLatch connectedSemaphore = new CountDownLatch(1);
	
	public static void main(String[] args) throws Exception{
		
		final ZooKeeper zk = new ZooKeeper(CONNECT_ADDR, SESSION_OUTTIME, new Watcher(){
			@Override
			public void process(WatchedEvent event) {
				//��ȡ�¼���״̬
				KeeperState keeperState = event.getState();
				//��ȡ�¼�������
				EventType eventType = event.getType();
				//����ǽ������ӣ�SyncConnected�����ӳɹ���״̬
				if(KeeperState.SyncConnected == keeperState){
					if(EventType.None == eventType){
						//����������ӳɹ��������ź������ú���������������ִ��
						connectedSemaphore.countDown();
						System.out.println("zk��������");
					}
				}
			}
		});

		//��������
		connectedSemaphore.await();
		
		System.out.println("ִ����������");
		//�������ڵ㣬��һ�������ǽڵ��·�����ڶ��������ǽڵ��value���������ǽڵ����֤��һ��ѡ��OPEN_ACL_UNSAFE�����ĸ������Ǵ����ڵ��ģʽ����ʱ�ڵ㻹�����ýڵ�
		/* 1.
		String ret = zk.create("/testRoot", "testRoot".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		System.out.println(ret);///testRoot
		*/
		
		
		//�첽�����ڵ㣬��Ȼzk��ԭ��api��֧�ֵݹ鴴��
		/*
	    zk.create("/testHello", "testHello".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT,new StringCallback() {
			@Override
			public void processResult(int rc, String path, Object ctx, String name) {
				System.out.println("rc:"+rc); //rc:0
				System.out.println("path:"+path); //path:/testHello
				System.out.println("ctx:"+ctx);  //ctx:aaa���ص�����
				System.out.println("name:"+name); //name:/testHello
				
			}
		},"aaa");
		
	    //��֧����һ�������̹߳ҵ���ô�첽�Ĳ����Ͳ���ִ��
		Thread.sleep(10000);
		*/
		
		//�첽ɾ��,��һ��������path���ڶ�������-1�ǰ汾�ţ�����dataVersion = 0���ֵ����һ��ֵΪ0����Ȼ���޸ĺ����ֵΪ1�����ε��������ڶ�������ȡ-1��ʾȫ��ɾ���������汾�ţ�
		//���ĸ������ǲ������ڵ������������Ե����������
		/** 2.
		zk.delete("/testRoot", -1, 
				new AsyncCallback.VoidCallback() {
					@Override
					public void processResult(int rc, String path, Object ctx) {
						System.out.println(rc);  //0��0��ʾ���óɹ�
						System.out.println(path);  // /testRoot,ɾ����·��
						System.out.println(ctx);  //a�����ĸ��������ݽ����Ĳ���
					}
				}, "a");
		*/
		
		/* 3.
		//�����ӽڵ㣬��ʱ�ڵ㱣֤�˴λỰ��Ч��zk.close();��ɾ���ˣ�����ʵ�ֲַ�ʽ����������CreateMode.EPHEMERALʵ�ֲַ�ʽ����Σ�գ�һ��ʹ��EPHEMERAL_SEQUENTIAL��ȥ���ֲ�ʽ��
		//ʵ�ֲַ�ʽ����ԭ���ǣ�������getһ����ʱ�Ľڵ㣬û�л�ȡ����˵��û���˴����ýڵ㣬Ȼ���Ҵ���������ִ���ҵ�ҵ����룬ִ��ҵ������ʱ������������ʱ����һ���ͻ�������get�˽ڵ��нڵ���ȴ�����һ���ͻ��˵�ҵ������߼�ִ���꣬session�ر�
		//��ʱ�ڵ��������ʱ����ڵ���Խ��в�����
		zk.create("/testRoot/children", "children data".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
		
		//ģ������һ���߳�ȥ��ȡ����ʱ����Ĵ���մ�������ʱ�ڵ�
		Thread ti = new Thread(new Runnable() {
			@Override
			public void run() {
				byte[] data = null;
				try {
					data = zk.getData("/testRoot/children", false, null);
					System.out.println(new String(data));
					if(data == null){
						//���Լ���ҵ�����
					}
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
		});
		ti.start();
		Thread.sleep(10000);
		*/
		
		//��ȡ�ڵ�ϴ��Ϣ
		/*4.
		//byte[] data = zk.getData("/testRoot", false, null);
		//System.out.println(new String(data));
		 */
		
		/*
		//��ȡһ��·�������еĽڵ�
		//[zk: localhost:2181(CONNECTED) 9] create /testRoot/a1 1111
		//Created /testRoot/a1
		//[zk: localhost:2181(CONNECTED) 10] create /testRoot/a2 2222
		//Created /testRoot/a2
		//[zk: localhost:2181(CONNECTED) 11] create /testRoot/a3 3333
		//Created /testRoot/a3
		List<String> list = zk.getChildren("/testRoot", false);
		for(String path:list){
			System.out.println(path);
			String relpath="/testRoot/"+path;
			System.out.println(new String(zk.getData(relpath, false, null)));
		}
		*/
		
		/* 5.
		//�޸Ľڵ��ֵ
		//zk.setData("/testRoot", "modify data root".getBytes(), -1);
		//byte[] data = zk.getData("/testRoot", false, null);
		//System.out.println(new String(data));	
		 */
		
		//�жϽڵ��Ƿ����,����null�ͱ�ʾ������
		System.out.println(zk.exists("/testRoot/a1", false)); //21474836539,21474836539,1482919867436,1482919867436,0,0,0,0,3,0,21474836539
		//ɾ���ڵ㣬��֧�ֵݹ�ɾ��
		zk.delete("/testRoot/a1", -1);
		System.out.println(zk.exists("/testRoot/a1", false));  //null
		
		//�ݹ�ɾ�������ֱ�����������֪��ԭ����api��֧�ֵݹ��ɾ���ڵ�
		zk.delete("/testRoot", -1);
		
		zk.close();
		
		
		
	}
}
