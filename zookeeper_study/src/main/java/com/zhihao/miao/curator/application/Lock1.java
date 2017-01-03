package com.zhihao.miao.curator.application;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;


//此列的弊端在于如果在同一个jvm下，那么没有问题，如果在多个jvm环境下，那么这个重入锁也就失去了作用
public class Lock1 {

	static ReentrantLock reentrantLock = new ReentrantLock();
	static int count = 10;
	public static void genarNo(){
		try {
			reentrantLock.lock();
			count--;
			System.out.println(count);
		} finally {
			reentrantLock.unlock();
		}
	}
	
	public static void main(String[] args) throws Exception{
		
		final CountDownLatch countdown = new CountDownLatch(1);
		for(int i = 0; i < 10; i++){
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						countdown.await();
						genarNo();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
					}
				}
			},"t" + i).start();
		}
		Thread.sleep(50);
		countdown.countDown();

		
	}
}
