package org.linjinxiao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class VOADownloadAll {

	public static final String VOA_Standard_English = "VOA_Standard_English";
	public static final String VOA_Special_English = "VOA_Special_English";

	public static File base_dir = new File("E:/workspace/current", "voa");

	public static void down_mp3(String mp3_url, File filename) {
		try {
			OutputStream os = new FileOutputStream(filename);
			Connection conn = Jsoup.connect(mp3_url);
			os.write(conn.execute().bodyAsBytes());
			os.close();
			System.out.println("Downloaded");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(mp3_url + " not downloaded");
		}
	}

	public static void parse_page(final String url, final String voaType) throws IOException {
		(new Thread() {
			public void run() {
				try {
					Document content = Jsoup.connect(url).get();
					String title = content.title().split("-")[1].trim().replace(" ", "_");
					// String temp_date =
					// content.select("span.datetime").first().text();
					// String pub_date = temp_date.replace(",","").replace(" ",
					// "_");
					Elements urls = content.select("a[href]");
					String mp3_url = "";
					String lrc_url = "";
					String href = null;
					for (Element i : urls) {
						href = i.attr("href");
						if (!"".equals(mp3_url)) {
							if (href.startsWith("/lrc/") && href.endsWith(".lrc")) {
								lrc_url = "http://www.51voa.com" + href;
							}
							break;
						}
						if (href.startsWith("http://down.51voa.com/") && href.endsWith(".mp3")) {
							mp3_url = href;
						}

					}
					String text = content.select("div#content").html();
					text = text.substring(text.lastIndexOf("</span>") + "</span>".length());

					File voaType_dir = new File(base_dir, voaType);
					if (!voaType_dir.exists()) {
						voaType_dir.mkdirs();
					}
					File mp3_dir = new File(voaType_dir, title);
					if (!mp3_dir.exists()) {
						mp3_dir.mkdirs();
					}
					File filename = new File(mp3_dir, mp3_url.substring(mp3_url.lastIndexOf("/"), mp3_url.length() - 4));
					File txtFile = new File(filename + ".txt");
					if (!txtFile.exists()) {
						txtFile.createNewFile();
						PrintWriter pw = new PrintWriter(txtFile);
						pw.append(text);
						pw.close();
					}
					System.out.println(mp3_url + " Text saved");
					File mp3_file = new File(filename + ".mp3");
					if (!mp3_file.exists()) {
						mp3_file.createNewFile();
						down_mp3(mp3_url, mp3_file);
					}

					if (!"".equals(lrc_url)) {
						File lrc_file = new File(filename + ".lrc");
						if (!lrc_file.exists()) {
							lrc_file.createNewFile();
							down_mp3(lrc_url, lrc_file);
						}
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}).start();

	}

	public static List<String> get_url_list(String url, String voaTypeUrl) throws IOException {
		Document content = Jsoup.connect(url).get();
		Elements urls = content.select("a[href]");
		List<String> url_list = new LinkedList<String>();
		String href = null;
		for (Element i : urls) {
			href = i.attr("href");
			if (href.startsWith(voaTypeUrl) && href.endsWith("html")) {
				url_list.add("http://www.51voa.com" + href);
			}
		}
		return url_list;
	}

	public static void main(String[] args) throws Exception {
		if (!base_dir.exists())
			base_dir.mkdirs();
		String[] voaTypes = new String[] { VOA_Standard_English };
		for (int i = 0; i < voaTypes.length; i++) {
			String voaType = voaTypes[i];
			String voaTypeUrl = getVoaTypeUrlPath(voaType);
			List<String> topicList = getTopicList(voaTypeUrl);
			for (String topic : topicList) {
				downLoadTopic(topic, voaType);
			}
		}
	}

	private static void downLoadTopic(final String topic, final String voaType) throws IOException {
		(new Thread() {
			public void run() {
				try {
					Document document = Jsoup.connect(topic).get();
					String thisPageUrl = topic.substring(topic.lastIndexOf("/") + 1);
					String urlPre = topic.substring(0, topic.length() - thisPageUrl.length());
					Elements pageBeginElement = document.select("[href=" + thisPageUrl + "]");
					if (pageBeginElement == null || pageBeginElement.size() != 1)
						throw new RuntimeException("Has more than 1 result, please check the selector.");
					List<String> pages = new LinkedList<String>();
					Element pageEle = pageBeginElement.first();
					while (pageEle != null) {
						pages.add(urlPre + pageEle.attr("href"));
						pageEle = pageEle.nextElementSibling();
					}
					for (String pageUrl : pages) {
						downLoadPage(pageUrl, voaType);
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}).start();

	}

	private static void downLoadPage(final String topicUrl, final String voaType) throws IOException {
		(new Thread() {
			public void run() {
				try {
					String voaTypeUrl = getVoaTypeUrlPath(voaType);
					List<String> url_list = get_url_list(topicUrl, voaTypeUrl);
					for (String url : url_list) {
						parse_page(url, voaType);
						// break;
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}).start();
	}

	private static List<String> getTopicList(String voaTypeUrl) throws IOException {
		String url = "http://www.51voa.com/";
		Document document = Jsoup.connect(url).get();
		Elements topicTopElement = document.select("[href=" + voaTypeUrl + "]");
		if (topicTopElement == null || topicTopElement.size() != 1)
			throw new RuntimeException("Has more than 1 result, please check the selector.");
		Element ulEle = topicTopElement.first().parent().nextElementSibling();
		if (ulEle == null || !"ul".equals(ulEle.nodeName()))
			throw new RuntimeException("The element is not ul!");
		Elements aElements = ulEle.select("a[href]");
		if (aElements == null)
			throw new RuntimeException("No Child be found !");
		List<String> topicList = new LinkedList<String>();
		for (Element a : aElements) {
			topicList.add("http://www.51voa.com" + a.attr("href"));
		}
		return topicList;
	}

	private static String getVoaTypeUrlPath(String voaType) {
		return "/" + voaType + "/";
	}

}
