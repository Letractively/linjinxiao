package com.commitbook;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Authenticator;

import uncertain.composite.CompositeMap;

import com.sun.xml.internal.messaging.saaj.util.Base64;

public class CommonTest {

	double random;

	private ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();
	private Lock writeLock = rwlock.writeLock();
	private Lock readLock = rwlock.readLock();
	float load_factor = 0.75f;

	HashMap<String, String> cache = new HashMap<String, String>(16, load_factor);

	AtomicBoolean can_not_read = new AtomicBoolean(false);

	Object lock = new Object();

	int readThreadCount = 5000;
	int writeThreadCount = 1;
	int sumThreadCount = readThreadCount + writeThreadCount;

	int readTime = 50000;
	int writeTime = 100;

	int key_value_count = 1000000;

	boolean lockable = true;

	public CommonTest() {
		random = Math.random();
	}

	public static void main(String[] args) throws Exception {
		CommonTest test = new CommonTest();
		test.print();
		// test.isSocketUsed(20001);
//		test.decode("Basic YWRtaW5pc3RyYXRvcjpIQG5kMTIzLg==");
//		test.testUrl();
		// test.close();
		// test.url("http://127.0.0.1:8080/hec/modules/sys/WS/sample/svc/sys_provide_webservice_one_record_sample.svc");
		// test.removeCDATA("<test><![CDATA[ok]]></test>");
		// double a=10080692.2;
		// System.out.println(a);
		// BigDecimal b= new BigDecimal(a);
		// System.out.println(b);

		// double a=10080692.02;
		// System.out.println(a);
		// String b= BigDecimal.valueOf(a).toString();
		// System.out.println(b);

	}
	private void print(){
		CompositeMap field_node = new CompositeMap("Field");
		field_node.put("Name", "ID");
		field_node.setText("New");
		
		System.out.println(field_node.toXML());
	}
	public void decode(String base64){
		if (base64 != null) {
			String encodeAuth = base64.substring("Basic ".length());
			String decode = Base64.base64Decode(encodeAuth);
			String[] strs = decode.split(":");
			System.out.println("user:"+strs[0]);
			System.out.println("password#"+strs[1]);
		}
	}

	public static void testUrl() throws Exception {
		// String appLocation = "http://doctest";
		String appLocation = "http://moss15/sites/Doc";
		String appSite;
		URI app = new URI(appLocation);
		String appPath = app.getPath();
		if ("".equals(appPath)) {
			appSite = appLocation;
			if (!appSite.endsWith("/")) {
				appSite = appSite + "/";
			}
		} else {
			int pathIndex = appLocation.indexOf(appPath) + 1;
			appSite = appLocation.substring(0, pathIndex);
		}
		System.out.println(appSite);

	}

	private static boolean isSocketUsed(int port) {
		Socket clientSocket = null;
		try {
			log("$$$$$$ 一");
			InetAddress addr = InetAddress.getByName("127.0.0.1");
			log("$$$$$$ 二");
			clientSocket = new Socket();
			InetSocketAddress socketAddress = new InetSocketAddress(addr, port);
			log("$$$$$$ 三");
			clientSocket.connect(socketAddress, 5);
			log("$$$$$$ 四");
			return true;
		} catch (Exception e) {
			log("$$$$$$ 五");
			return false;
		} finally {
			closeSocket(clientSocket);
			log("$$$$$$ 六");
		}
	}

	private static void closeSocket(Socket socket) {
		if (socket == null)
			return;
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void log(String message) {
		String fullMessage = getPrefix() + message;
		System.out.println(fullMessage);
	}

	private static String getPrefix() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("************* yyyy-MM-dd HH:mm:ss E ");
		String sysdate = dateFormat.format(new Date());
		return sysdate;
	}

	private void close() {
		Runnable shutdownHook = new Runnable() {
			public void run() {
				System.out.println("Stopping server...");
			}
		};

		// add shutdown hook
		Runtime runtime = Runtime.getRuntime();
		runtime.addShutdownHook(new Thread(shutdownHook));
		System.exit(0);
	}

	private void url(String targetUrl) throws Exception {
		URI uri = new URI(targetUrl);
		URL url = uri.toURL();
		System.out.println(url);
	}

	private void regex() {
		Pattern pattern = Pattern.compile("([\\w]*)(.*)");

		Matcher matcher = pattern.matcher("bgt_budget_reserves（不超预算）.sql");

		// String str = "abc<";
		//
		// pattern = Pattern.compile("([//w]+)<");
		//
		// matcher = pattern.matcher(str);

		while (matcher.find()) {

			System.out.println(matcher.group(1));
			System.out.println(matcher.group(2));

		}
		// String str = "ceponline@yahoo.com.cn";
		// Pattern pattern =
		// Pattern.compile("[//w//.//-]+@([//w//-]+//.)+[//w//-]+",
		// Pattern.CASE_INSENSITIVE);
		// Matcher matcher = pattern.matcher(str);
		// System.out.println(matcher.matches());
	}

	class SmtpAuth extends Authenticator {
		private String username, password;

		public SmtpAuth(String username, String password) {
			this.username = username;
			this.password = password;
		}

		protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
			return new javax.mail.PasswordAuthentication(username, password);
		}
	}

	private String removeCDATA(String source) {
		source = source.replaceAll("<!\\[CDATA\\[", "");
		source = source.replaceAll("]]>", "");
		System.out.println(source);
		return source;

	}

	public void excepToStr() {
		try {
			throw new IllegalArgumentException("test");
		} catch (Exception e) {
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
		for (int i = 0; i < readThreadCount; i++) {
			new TestReadThread(doneSignal).start();
		}
		new TestWriteThread(doneSignal).start();
		try {
			doneSignal.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		System.out.println("load_factor:" + load_factor + " lockable:" + lockable + " total:" + (end - begin) / 1000 + "s");
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
				if (lockable)
					getValue(String.valueOf(num));
				else {
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
				if (lockable)
					writeValue();
				else {
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
