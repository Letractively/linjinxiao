package com.commitbook;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import uncertain.composite.CompositeLoader;
import uncertain.composite.CompositeMap;
import uncertain.composite.XMLOutputter;
import uncertain.datatype.ConvertionException;
import uncertain.datatype.DataType;



public class CommonTest {

	double random;

	private ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();
	private Lock writeLock = rwlock.writeLock();
	private Lock readLock = rwlock.readLock();
	float load_factor = 0.75f;
	
	HashMap<String, String> cache = new HashMap<String, String>(16,load_factor);
	
	AtomicBoolean can_not_read = new AtomicBoolean(false);

	Object lock = new Object();
	
	int readThreadCount = 5000;
	int writeThreadCount = 1;
	int sumThreadCount = readThreadCount+writeThreadCount;
	
	int readTime = 50000;
	int writeTime = 100;
	
	int key_value_count = 1000000;
	
	boolean lockable = true;

	
	public CommonTest() {
		random = Math.random();
	}

	public static void main(String[] args) throws Exception {
		
		String a = "12260::46010000:2013:2013-04:46010000:DE02:AC01102001E::0:0::PR03:PRO02:CH02:::::::::";
		 
		a = a.replaceAll(":", ": ");
		String[] b = a.split(":");
		System.out.println(b.length);
		for(int i=0;i<b.length;i++){
			System.out.println(b[i].trim());
		}
		
//		System.out.println(Class.forName("java.util.Date").getName());
		
//		String longMessage = "09939测试abc?1111111111111111111111111111111111111111111111111111111111111111111111";
//		String shortMessage = "您在test申请了兑换积分券。将获得1张test电子券。请回复您的姓名以确认此号码有效。本短信免费,回复短信不收另外费1111";
//		System.out.println(longMessage.length()+",shortMessage:"+shortMessage.length());
//		CommonTest cast = new CommonTest();
//		cast.run();

		//		cast.excepToStr();
	}
//	public Object convert( Object value, Class prefered_class) throws ConvertionException{
//		DataType dt = getDataType(prefered_class);
//		if( dt == null) return null;
//		else return dt.convert(value);
//	}
	public void excepToStr(){
		try{
			throw new IllegalArgumentException("test");
		}catch(Exception e){
			System.out.println(e.toString());
		}
		
	}

	public void run() {
		for (int i = 0; i < key_value_count; i++) {
			String numStr = String.valueOf(i);
			cache.put(numStr, numStr);
		}
		long begin = System.currentTimeMillis();
		CountDownLatch doneSignal = new CountDownLatch(sumThreadCount);   
		for(int i=0;i<readThreadCount;i++){
			new TestReadThread(doneSignal).start();
		}
		new TestWriteThread(doneSignal).start();
		try {
			doneSignal.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		System.out.println("load_factor:"+load_factor+" lockable:"+lockable+" total:"+(end-begin)/1000+"s");
	}

	public Object getValue(Object key) {
		readLock.lock();
		try {
			return cache.get(key);
		} finally {
			readLock.unlock();
		}
	}

	public void writeValue() {
		writeLock.lock();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			writeLock.unlock();
		}
	}

	public Object getValue1(Object key) {
		while (can_not_read.get()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return cache.get(key);
	}

	public void writeValue1() {
		can_not_read.set(true);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			can_not_read.set(false);
		}
	}

	class TestReadThread extends Thread {
		CountDownLatch doneSignal;
		public TestReadThread(CountDownLatch doneSignal) {
			this.doneSignal = doneSignal;
		}

		public void run() {
			for (int i = 0; i < readTime; i++) {
				long num = Math.round((Math.random() * key_value_count));
				if(lockable)
					getValue(String.valueOf(num));
				else{
					getValue1(String.valueOf(num));
				}
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			doneSignal.countDown();
		}
	}

	class TestWriteThread extends Thread {
		CountDownLatch doneSignal;
		public TestWriteThread(CountDownLatch doneSignal) {
			this.doneSignal = doneSignal;
		}

		public void run() {
			for (int i = 0; i < writeTime; i++) {
				if(lockable)
					writeValue();
				else{
					writeValue1();
				}
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			doneSignal.countDown();
		}
	}
}
