package com.zhihao.miao.zkclient.api;

import java.util.List;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

public class ZkClientBase {

	/** zookeeper��ַ */
	static final String CONNECT_ADDR = "192.168.5.124:2181,192.168.5.125:2181,192.168.5.126:2181";
	/** session��ʱʱ�� */
	static final int SESSION_OUTTIME = 5000;//ms 
	
	
	public static void main(String[] args) throws Exception {
		ZkClient zkc = new ZkClient(new ZkConnection(CONNECT_ADDR), 10000);
		
		
		//1. create and delete���� 
		zkc.createEphemeral("/temp");
		//�ݹ�Ĵ����ڵ㣬��ʱû��/super�ڵ㣬��Դ����API��ʵ�ֲ��˵ģ����ǵݹ�Ĵ�������ָ���ڵ��valueֵ
		//zkc.createPersistent("/super/c1", true);
		//zkc.writeData("/super/c1", "hello");
		//String value = zkc.readData("/super/c1");
		//System.out.println(value);
		Thread.sleep(10000);
		//�ݹ��ɾ��
		//zkc.deleteRecursive("/super");
		
		//2. ����path��data���Ҷ�ȡ�ӽڵ��ÿ���ڵ������
		zkc.createPersistent("/super", "1234");
		zkc.createPersistent("/super/c1", "c1����");
		zkc.createPersistent("/super/c2", "c2����");
		List<String> list = zkc.getChildren("/super");
		for(String p : list){
			System.out.println(p);
			String rp = "/super/" + p;
			String data = zkc.readData(rp);
			System.out.println("�ڵ�Ϊ��" + rp + "������Ϊ: " + data);
		}
		
		//3. ���º��жϽڵ��Ƿ����
		zkc.writeData("/super/c1", "������");
		System.out.println(zkc.readData("/super/c1"));
		System.out.println(zkc.exists("/super/c1"));
		
		//4.�ݹ�ɾ��/super����
		zkc.deleteRecursive("/super");		
	}
}
