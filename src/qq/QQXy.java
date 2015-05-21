package qq;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import util.NameValue;
import util.WebRequest;
import util.HttpRequest;

public class QQXy {

	/**
	 * sid�����ֻ���Ѷ����ݱ�ʶ.��¼�ɹ���,���Ի��24λsid�� Ҳ���Ա����sid,��һ��ʱ����sid����Ч(�������ǩ��ԭ��)
	 * �Ժ�������ʲô����.����sid�ύ.
	 */
	private String sid;
	private Document doc_verify;

	public static final int LOGIN_ERROR = 0;
	public static final int LOGIN_SUCCESS = 1;
	public static final int LOGIN_FAIL = 2;
	public static final int LOGIN_VERIFY = 3;

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	/**
	 * ��¼
	 * 
	 * @param qq
	 *            ����
	 * @param pwd
	 *            ����
	 * @param loginType
	 *            ״̬(1:������¼,2:�����¼)
	 * @return ���� LOGIN_
	 */
	public int login(String qq, String pwd, int loginType) {
		// ��ʼ��ҳ��,���Ի�ȡ�ύ������Ĳ���
		String html = WebRequest.get("http://pt.3g.qq.com/s?aid=nLogin3gqq",
				"UTF-8");
		if (html == null) {
			return LOGIN_ERROR;
		}
		Document doc = Jsoup.parse(html);
		String url = doc.getElementsByTag("go").get(1).attr("href");

		// �ύ��¼����
		NameValue nv = new NameValue();
		nv.add("qq", qq);
		nv.add("pwd", pwd);
		nv.add("bid_code", "3GQQ");
		nv.add("toQQchat", "true");
		nv.add("login_url", "http://pt.3g.qq.com/s?aid=nLoginnew&q_from=3GQQ");
		nv.add("q_from", "");
		nv.add("modifySKey", "0");
		nv.add("loginType", loginType + "");
		nv.add("aid", "nLoginHandle");
		nv.add("i_p_w", "qq|pwd|");
		html = WebRequest.post(url, nv.toString(), "UTF-8");

		// �жϵ�¼���
		// System.out.println(html);
		if (html == null) {
			return LOGIN_ERROR;
		}
		if (html.contains("��¼3GQQ�ɹ�")) {
			sid = WebRequest.mid(html, "sid=", "&");
			System.out.println("sid=" + sid);
			return LOGIN_SUCCESS;
		}
		if (html.contains("����д���ʺŻ����벻��ȷ")) {
			return LOGIN_FAIL;
		}
		if (html.contains("��������֤��")) {
			doc_verify = Jsoup.parse(html);
			return LOGIN_VERIFY;
		}
		return LOGIN_FAIL;
	}

	/**
	 * ��login��������LOGIN_VERIFY�������,���ɵ��ã�ȡ��֤��ͼƬ(GIF��ʽ)
	 * 
	 * @return
	 */
	public byte[] getVerifyCode() {
		if (doc_verify == null) {
			return null;
		}
		String src = doc_verify.getElementsByTag("img").get(0).attr("src");
		return WebRequest.file(src);
	}

	/**
	 * ��login��������LOGIN_VERIFY�������,���ɵ��ã�����֤���ύ��¼����
	 * 
	 * @param verifyCode
	 * @return
	 */
	public int login(String verifyCode) {
		if (doc_verify == null) {
			return LOGIN_ERROR;
		}
		Element loginTag = doc_verify.getElementsByTag("go").get(1);
		String url = loginTag.attr("href");
		Elements postfields = loginTag.getElementsByTag("postfield");
		NameValue nv = new NameValue();
		for (Element e : postfields) {
			nv.add(e.attr("name"), e.attr("value"));
		}
		String data = nv.toString();
		data = data.replace("imgType=$imgType", "imgType=gif");
		data = data.replace("verify=$verify", "verify=" + verifyCode);

		String html = WebRequest.post(url, data, "UTF-8");

		// �жϵ�¼���
		// System.out.println(html);
		if (html == null) {
			return LOGIN_ERROR;
		}
		if (html.contains("��¼3GQQ�ɹ�")) {
			sid = WebRequest.sub(html, "sid=", 24);
			System.out.println("sid=" + sid);
			return LOGIN_SUCCESS;
		}
		if (html.contains("����д���ʺŻ����벻��ȷ")) {
			return LOGIN_FAIL;
		}
		if (html.contains("��������֤��")) {
			doc_verify = Jsoup.parse(html);
			return LOGIN_VERIFY;
		}
		return LOGIN_FAIL;
	}

	/**
	 * ͨ��sid��¼QQ.
	 * 
	 * @param sid
	 */
	public boolean loginBySid(String sid) {
		String html = WebRequest.get(
				"http://pt.3g.qq.com/s?aid=nLogin3gqqbysid&3gqqsid=" + sid,
				"UTF-8");
		if (html.contains("3GQQ����-�ֻ���Ѷ��")) {
			this.sid = sid;
			return true;
		}
		return false;
	}

	/**
	 * ȡ���ѷ���
	 * 
	 * @return
	 */
	public List<Group> getGroups() {
		List<Group> list = new ArrayList<Group>();
		String html = WebRequest.get("http://q16.3g.qq.com/g/s?sid=" + sid
				+ "&aid=nqqGroup", "UTF-8");
		Document doc = Jsoup.parse(html);
		Elements gos = doc.getElementsByTag("go");
		for (int i = 0; i < gos.size() - 1; i++) { // ���� -1 ����Ϊ���һ����������Ҫ��
			Elements postfields = gos.get(i).getElementsByTag("postfield");
			Group g = new Group();
			g.setName(postfields.get(1).attr("value"));
			g.setId(Integer.parseInt(postfields.get(2).attr("value")));
			list.add(g);
		}
		Matcher mat = Pattern.compile("\\(\\d*?/\\d*?\\)").matcher(html);
		for (int i = 0; mat.find() && i < list.size(); i++) {
			list.get(i).setNum(mat.group());
		}
		return list;
	}

	/**
	 * ȡĳһ�����µĺ����б�
	 * 
	 * @param g
	 * @return
	 */
	public List<Friend> getFriends(Group g) {
		List<Friend> list = new ArrayList<Friend>();
		NameValue nv = new NameValue();
		nv.add("aid", "nqqGrpF");
		nv.add("name", WebRequest.encode(g.getName(), "UTF-8"));
		nv.add("id", g.getId() + "");
		nv.add("gindex", g.getId() + "");

		int pid = 1;
		int len = Integer.parseInt(WebRequest.mid(g.getNum(), "/", ")"));
		List<Friend> temp = null;
		while (true) { // ѭ������ҳ
			temp = new ArrayList<Friend>();
			String html = WebRequest.post(
					"http://q32.3g.qq.com/g/s?sid=" + sid, nv.toString()
							+ "&pid=" + pid++, "UTF-8");
			// System.out.println(html);
			Matcher mat = Pattern.compile("u=(\\d*?)\"><img.*?/>(.*?)</a>")
					.matcher(html);
			while (mat.find()) {
				temp.add(new Friend(mat.group(1), mat.group(2)));
				len--;
			}
			if (temp.size() == 0) { // �]����,������ѭ��
				break;
			}
			list.addAll(temp);// ������ӵ��б�
			if (len == 0) { // ���������������,Ҳ����ѭ��
				break;
			}

		}
		return list;
	}

	/**
	 * ����QQ��Ϣ
	 * 
	 * @param qq
	 *            �Է�����
	 * @param content
	 *            ����
	 * @return
	 */
	public boolean sendMessage(String qq, String content) {
		NameValue nv = new NameValue();
		nv.add("msg", WebRequest.encode(content, "UTF-8"));
		nv.add("u", qq);
		nv.add("saveURL", "0");
		nv.add("do", "send");
		nv.add("on", "1");
		nv.add("num", qq);
		nv.add("aid", "%E5%8F%91%E9%80%81");
		nv.add("do", "sendsms");
		String html = WebRequest.post("http://q32.3g.qq.com/g/s?sid=" + sid,
				nv.toString(), "UTF-8");
		// System.out.println(html);
		return html.contains("��Ϣ���ͳɹ�");
	}

	/**
	 * ȡ�����Ϣ��¼(����̫�鷳,���ø���,����ֱ�ӷ�����ҳ�ı�)
	 * 
	 * @param qq
	 *            �Է�����
	 * @return
	 */
	public String getMessageRecord(String qq) {
		String html = WebRequest.get("http://q16.3g.qq.com/g/s?sid=" + sid
				+ "&aid=nqqChat&u=" + qq + "&on=1", "UTF-8");
		// System.out.println(html);
		try {
			Document doc = Jsoup.parse(html);
			html = doc.text().replace(" ", "\n");
			html = WebRequest.mid(html,"���Ͷ��Ÿ�", "��QQ���ܡ�");
			if(html.startsWith("��") || html.startsWith("��")){
				html = html.substring(1);
			}
			else if(html.startsWith("��/��")){
				html = html.substring(3);
			}
		} catch (Exception e) {
			return "";
		}
		return html;
	}// ��/��

	/**
	 * ȡ��������(����̫�鷳,���ø���,����ֱ�ӷ�����ҳ�ı�)
	 * 
	 * @param qq
	 *            �Է�����
	 * @return
	 */
	public String getUserInfo(String qq) {
		String html = WebRequest.get("http://q32.3g.qq.com/g/s?sid=" + sid
				+ "&aid=nqqUserInfo&u=" + qq + "&on=1", "UTF-8");
		Document doc = Jsoup.parse(html);
		return doc.text().replace(" ", "\n");
	}

	/*
	 * ��ȡ�����Ϣ�ĺ��ѵ�QQ��
	 * */
	public String getLatestFriend(){
		String result = "";
		String html = WebRequest.get("http://q32.3g.qq.com/g/s?sid="+sid+
				"&aid=nqqRecent","UTF-8");

		Document doc = Jsoup.parse(html);

		Matcher mat = Pattern.compile("u=(\\d*?)\"").matcher(html);
		if(mat.find())
			result = mat.group();
		mat = Pattern.compile("[0-9]{5,11}").matcher(result);
		if(mat.find())
			result = mat.group();
		return result;	
	}
	public String getLatestMessage(String q){
		String m="";
		String html = WebRequest.get("http://sqq2.3g.qq.com/roam/?sid="+sid+
				"&q="+q,"UTF-8");

		try{
			if(html.isEmpty())return "";
		}catch(NullPointerException e){
			e.printStackTrace();
			return "";
		}
		Document doc = Jsoup.parse(html);
		//System.out.println(html);
		Matcher mat = Pattern.compile("[0-9]{2}:[0-9]{2}:[0-9]{2}.*br")
				.matcher(html);
		if(mat.find()){
			m = mat.group();
			//System.out.println("m\n"+m+"\nyes\n");
			mat = Pattern.compile("[0-9]{2}:[0-9]{2}:[0-9]{2}.*br").matcher(m);
			if(mat.find()){
				//System.out.println("$");
				m = mat.group();
				m = m.replace(" ", "");
				doc = Jsoup.parse(m);
				m = doc.text();
				String[] sourceArray = m.split(" ");
				m = sourceArray[sourceArray.length-1];				
			}
		}
		
		return m;
	}
	/*
	 * ��ȡ�����Ϣ�ĺ�����Ϣ
	 * */
	
	
	/**
	 * �˳���¼
	 */
	public void logout() {
		WebRequest.get("http://pt.3g.qq.com/s?sid=" + sid + "&aid=nLogout",
				"UTF-8");
	}

}
