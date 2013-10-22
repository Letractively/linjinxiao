package com.commitbook;


import java.io.UnsupportedEncodingException;

public class Chinese {

	public static String GB2312 = "GB2312";
	public static String ISO8859_1 = "ISO8859-1";
	public static String GBK = "GBK";
	public static String UTF8 = "UTF-8";
	private static Chinese tool;

	public Chinese() {
	}

	public String chinese(String origninalString) {
		String objectString = null;
		try {
			objectString = new String(origninalString.getBytes("GB2312"), "GBK");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return objectString;
	}

	public String convertFormat(String originalString, String orginalFormat,
			String objectFormat) {
//		System.out.println("originalString:" + originalString);
		String objectString = null;
		try {
			if (objectFormat != null) {
				objectString = new String(originalString
						.getBytes(orginalFormat), objectFormat);
			} else
				objectString = new String(originalString
						.getBytes(orginalFormat));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
//		System.out.println("objectString:" + objectString);
		return objectString;
	}
    public static void justTest(String originalString){
		String[] orif ={Chinese.GB2312,Chinese.GBK,Chinese.ISO8859_1,Chinese.UTF8};
		String[] objf ={Chinese.GB2312,Chinese.GBK,Chinese.ISO8859_1,Chinese.UTF8};
		for(int i=0;i<orif.length;i++){
			for(int j=0;j<objf.length;j++){
				String abc = Chinese.getInstance().convertFormat(originalString,orif[i],objf[j]);
				System.out.println(orif[i]+":"+objf[j]+":"+abc);
			}

		}
		String abc= Chinese.UTF82Chinese("UTF82Chinese:"+originalString);
		System.out.println(abc);
    }

	public static String UTF82Chinese(String source) {
		if (null == source || "".equals(source)) {
			return source;
		}
		StringBuffer sb = new StringBuffer();
		int i = 0;
		while (i < source.length()) {
			if (source.charAt(i) == '\\') {
				int j = Integer.parseInt(source.substring(i + 2, i + 6), 16);
				sb.append((char) j);
				i += 6;
			} else {
				sb.append(source.charAt(i));
				i++;
			}
		}
		return sb.toString();
	}

	public static Chinese getInstance() {
		if (tool == null)
			tool = new Chinese();
		return tool;
	}

	public static void main(String[] args) {
		String string = "\\u62A5\u8868\u6A21\u677F";//
		System.out.println(Chinese.UTF82Chinese(string));
		String originalString= "/QgnOcW4h5KZ7HLQ+O78rBemdt4cFKJpRY2pytZ79b1sG15jQEiBUdyp0O7Jmn34GhrH361vEigV +tM7M/FXeYso9HHb/LGjiU8DGwbYEQ8rIaTjXBhuHcMyCPxzVtboDxphZ7ddg4Fukic1FbbwIzGU rZ7JQnJ2us01Ujr+VqcthkZGWoBWhTeNvZu0WQXdOsyANaTMqQu5TSaRwitqKfUu+Vhg6QhxZ6uF mbEr40sPyLIBZ0UW5KYyYg9+NVh1";
		Chinese.justTest(originalString);

	}
}
