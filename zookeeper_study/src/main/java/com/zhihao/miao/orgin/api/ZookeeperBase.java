package com.zhihao.miao.orgin.api;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;

public class ZookeeperBase {
	/** zookeeper地址 */
	static final String CONNECT_ADDR = "192.168.5.124:2181,192.168.5.125:2181,192.168.5.126:2181";
	/** session超时时间 */
	static final int SESSION_OUTTIME = 2000;//ms 
	/** 信号量，阻塞程序执行，用于等待zookeeper连接成功，发送成功信号 */
	static final CountDownLatch connectedSemaphore = new CountDownLatch(1);
	
	public static void main(String[] args) throws Exception{
		
		final ZooKeeper zk = new ZooKeeper(CONNECT_ADDR, SESSION_OUTTIME, new Watcher(){
			@Override
			public void process(WatchedEvent event) {
				//获取事件的状态
				KeeperState keeperState = event.getState();
				//获取事件的类型
				EventType eventType = event.getType();
				//如果是建立连接，SyncConnected是连接成功的状态
				if(KeeperState.SyncConnected == keeperState){
					if(EventType.None == eventType){
						//如果建立连接成功，则发送信号量，让后续阻塞程序向下执行
						connectedSemaphore.countDown();
						System.out.println("zk建立连接");
					}
				}
			}
		});

		//进行阻塞
		connectedSemaphore.await();
		
		System.out.println("执行啦。。。");
		//创建父节点，第一个参数是节点的路径，第二个参数是节点的value，第三个是节点的认证，一般选择OPEN_ACL_UNSAFE，第四个参数是创建节点的模式，临时节点还是永久节点
		/* 1.
		String ret = zk.create("/testRoot", "testRoot".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		System.out.println(ret);///testRoot
		*/
		
		
		//异步创建节点，当然zk的原生api不支持递归创建
		/*
	    zk.create("/testHello", "testHello".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT,new StringCallback() {
			@Override
			public void processResult(int rc, String path, Object ctx, String name) {
				System.out.println("rc:"+rc); //rc:0
				System.out.println("path:"+path); //path:/testHello
				System.out.println("ctx:"+ctx);  //ctx:aaa，回调参数
				System.out.println("name:"+name); //name:/testHello
				
			}
		},"aaa");
		
	    //不支持这一步，主线程挂掉那么异步的操作就不会执行
		Thread.sleep(10000);
		*/
		
		//异步删除,第一个参数是path，第二个参数-1是版本号（就是dataVersion = 0这个值，第一次值为0，，然后修改后这个值为1，依次递增），第二个参数取-1表示全部删除（跳过版本号）
		//第四个参数是参数，在第三个参数可以调用这个参数
		/** 2.
		zk.delete("/testRoot", -1, 
				new AsyncCallback.VoidCallback() {
					@Override
					public void processResult(int rc, String path, Object ctx) {
						System.out.println(rc);  //0，0表示调用成功
						System.out.println(path);  // /testRoot,删除的路径
						System.out.println(ctx);  //a，第四个参数传递进来的参数
					}
				}, "a");
		*/
		
		/* 3.
		//创建子节点，临时节点保证此次会话有效，zk.close();就删除了，可以实现分布式锁，但是用CreateMode.EPHEMERAL实现分布式锁有危险，一般使用EPHEMERAL_SEQUENTIAL它去做分布式锁
		//实现分布式锁的原理是，首先我get一个临时的节点，没有获取到则说明没有人创建该节点，然后我创建，下面执行我的业务代码，执行业务代码的时候我有锁，此时另外一个客户端先是get此节点有节点则等待另外一个客户端的业务代码逻辑执行完，session关闭
		//临时节点结束。此时这个节点可以进行操作。
		zk.create("/testRoot/children", "children data".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
		
		//模拟另外一个线程去获取，此时上面的代码刚创建好临时节点
		Thread ti = new Thread(new Runnable() {
			@Override
			public void run() {
				byte[] data = null;
				try {
					data = zk.getData("/testRoot/children", false, null);
					System.out.println(new String(data));
					if(data == null){
						//做自己的业务代码
					}
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
		});
		ti.start();
		Thread.sleep(10000);
		*/
		
		//获取节点洗信息
		/*4.
		//byte[] data = zk.getData("/testRoot", false, null);
		//System.out.println(new String(data));
		 */
		
		/*
		//获取一个路径下所有的节点
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
		//修改节点的值
		//zk.setData("/testRoot", "modify data root".getBytes(), -1);
		//byte[] data = zk.getData("/testRoot", false, null);
		//System.out.println(new String(data));	
		 */
		
		//判断节点是否存在,返回null就表示不存在
		System.out.println(zk.exists("/testRoot/a1", false)); //21474836539,21474836539,1482919867436,1482919867436,0,0,0,0,3,0,21474836539
		//删除节点，不支持递归删除
		zk.delete("/testRoot/a1", -1);
		System.out.println(zk.exists("/testRoot/a1", false));  //null
		
		//递归删除，发现报错，所以我们知道原生的api不支持递归的删除节点
		zk.delete("/testRoot", -1);
		
		zk.close();
		
		
		
	}
}
