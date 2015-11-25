package util;

import java.net.*;
import java.io.*;


public class WebRequest {

	public static String post(String url, String data, String charset) {
		try {
			URL __url = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) __url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			OutputStreamWriter bos = new OutputStreamWriter(
					conn.getOutputStream(), charset);
			bos.write(data);
			bos.flush();
			BufferedReader bis = new BufferedReader(new InputStreamReader(
					conn.getInputStream(), charset));
			String line = null;
			StringBuilder sb = new StringBuilder();
			while ((line = bis.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			bis.close();
			return sb.toString();
		} catch (Exception e) {
			return null;
		}
	}

	public static String get(String url, String charset) {
		try {
			URL __url = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) __url.openConnection();
			BufferedReader bis = new BufferedReader(new InputStreamReader(
					conn.getInputStream(), charset));
			String line = null;
			StringBuilder sb = new StringBuilder();
			while ((line = bis.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			bis.close();
			return sb.toString();
		} catch (Exception e) {
			return null;
		}
	}

	public static byte[] file(String url) {
		try {
			URL __url = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) __url.openConnection();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			BufferedInputStream bis = new BufferedInputStream(
					conn.getInputStream());
			byte[] b = new byte[1024];
			int length = -1;
			while ((length = bis.read(b)) != -1) {
				bos.write(b, 0, length);
				bos.flush();
			}
			bis.close();
			bos.close();
			return bos.toByteArray();
		} catch (Exception e) {
			return null;
		}
	}

	public static String mid(String value, String left, String right) {
		try {
			int i = value.indexOf(left) + left.length();
			return value.substring(i, value.indexOf(right, i));
		} catch (Exception e) {
			return null;
		}
	}

	public static String sub(String value, String mark, int len) {
		try {
			int i = value.indexOf(mark) + mark.length();
			return value.substring(i, i + len);
		} catch (Exception e) {
			return null;
		}
	}

	public static String decode(String value, String charset) {
		try {
			return URLDecoder.decode(value, charset);
		} catch (Exception e) {
			return null;
		}
	}

	public static String encode(String value, String charset) {
		try {
			return URLEncoder.encode(value, charset);
		} catch (Exception e) {
			return null;
		}
	}

}
