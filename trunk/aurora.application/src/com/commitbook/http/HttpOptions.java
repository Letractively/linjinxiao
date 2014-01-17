package com.commitbook.http;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class HttpOptions {

	public static void main(String[] args) throws Exception {
		Socket socket = new Socket("127.0.0.1", 8080);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				socket.getOutputStream()));
		bw.write("OPTIONS /hec2dev/resource/aurora.ui.std/default/table/ HTTP/1.1\r\n");
		bw.write("Host: localhost:80\r\n");
		bw.write("UserAgent: IE9.0\r\n");
		bw.write("Connection: Keep-Alive\r\n");
		bw.write("\r\n");
		bw.flush();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		String line = null;
		while ((line = br.readLine()) != null) {
			System.out.println(line);
		}
		socket.close();
	}
}
