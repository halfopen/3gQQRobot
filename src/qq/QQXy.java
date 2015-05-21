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
	 * sid就是手机腾讯网身份标识.登录成功后,可以获得24位sid。 也可以保存好sid,在一定时间内sid都有效(这就是书签的原理)
	 * 以后无论做什么操作.都带sid提交.
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
	 * 登录
	 * 
	 * @param qq
	 *            号码
	 * @param pwd
	 *            密码
	 * @param loginType
	 *            状态(1:正常登录,2:隐身登录)
	 * @return 常量 LOGIN_
	 */
	public int login(String qq, String pwd, int loginType) {
		// 初始化页面,用以获取提交所必须的参数
		String html = WebRequest.get("http://pt.3g.qq.com/s?aid=nLogin3gqq",
				"UTF-8");
		if (html == null) {
			return LOGIN_ERROR;
		}
		Document doc = Jsoup.parse(html);
		String url = doc.getElementsByTag("go").get(1).attr("href");

		// 提交登录请求
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

		// 判断登录结果
		// System.out.println(html);
		if (html == null) {
			return LOGIN_ERROR;
		}
		if (html.contains("登录3GQQ成功")) {
			sid = WebRequest.mid(html, "sid=", "&");
			System.out.println("sid=" + sid);
			return LOGIN_SUCCESS;
		}
		if (html.contains("您填写的帐号或密码不正确")) {
			return LOGIN_FAIL;
		}
		if (html.contains("请输入验证码")) {
			doc_verify = Jsoup.parse(html);
			return LOGIN_VERIFY;
		}
		return LOGIN_FAIL;
	}

	/**
	 * 在login方法返回LOGIN_VERIFY的情况下,方可调用，取验证码图片(GIF格式)
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
	 * 在login方法返回LOGIN_VERIFY的情况下,方可调用，带验证码提交登录请求
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

		// 判断登录结果
		// System.out.println(html);
		if (html == null) {
			return LOGIN_ERROR;
		}
		if (html.contains("登录3GQQ成功")) {
			sid = WebRequest.sub(html, "sid=", 24);
			System.out.println("sid=" + sid);
			return LOGIN_SUCCESS;
		}
		if (html.contains("您填写的帐号或密码不正确")) {
			return LOGIN_FAIL;
		}
		if (html.contains("请输入验证码")) {
			doc_verify = Jsoup.parse(html);
			return LOGIN_VERIFY;
		}
		return LOGIN_FAIL;
	}

	/**
	 * 通过sid登录QQ.
	 * 
	 * @param sid
	 */
	public boolean loginBySid(String sid) {
		String html = WebRequest.get(
				"http://pt.3g.qq.com/s?aid=nLogin3gqqbysid&3gqqsid=" + sid,
				"UTF-8");
		if (html.contains("3GQQ聊天-手机腾讯网")) {
			this.sid = sid;
			return true;
		}
		return false;
	}

	/**
	 * 取好友分组
	 * 
	 * @return
	 */
	public List<Group> getGroups() {
		List<Group> list = new ArrayList<Group>();
		String html = WebRequest.get("http://q16.3g.qq.com/g/s?sid=" + sid
				+ "&aid=nqqGroup", "UTF-8");
		Document doc = Jsoup.parse(html);
		Elements gos = doc.getElementsByTag("go");
		for (int i = 0; i < gos.size() - 1; i++) { // 这里 -1 是因为最后一个不是我们要的
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
	 * 取某一分组下的好友列表
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
		while (true) { // 循环读多页
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
			if (temp.size() == 0) { // 沒读到,就跳出循环
				break;
			}
			list.addAll(temp);// 否则添加到列表
			if (len == 0) { // 如果读完所有人数,也跳出循环
				break;
			}

		}
		return list;
	}

	/**
	 * 发送QQ消息
	 * 
	 * @param qq
	 *            对方号码
	 * @param content
	 *            内容
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
		return html.contains("消息发送成功");
	}

	/**
	 * 取最近消息记录(解析太麻烦,懒得搞了,这里直接返回网页文本)
	 * 
	 * @param qq
	 *            对方号码
	 * @return
	 */
	public String getMessageRecord(String qq) {
		String html = WebRequest.get("http://q16.3g.qq.com/g/s?sid=" + sid
				+ "&aid=nqqChat&u=" + qq + "&on=1", "UTF-8");
		// System.out.println(html);
		try {
			Document doc = Jsoup.parse(html);
			html = doc.text().replace(" ", "\n");
			html = WebRequest.mid(html,"发送短信给", "【QQ功能】");
			if(html.startsWith("他") || html.startsWith("她")){
				html = html.substring(1);
			}
			else if(html.startsWith("他/她")){
				html = html.substring(3);
			}
		} catch (Exception e) {
			return "";
		}
		return html;
	}// 她/他

	/**
	 * 取好友资料(解析太麻烦,懒得搞了,这里直接返回网页文本)
	 * 
	 * @param qq
	 *            对方号码
	 * @return
	 */
	public String getUserInfo(String qq) {
		String html = WebRequest.get("http://q32.3g.qq.com/g/s?sid=" + sid
				+ "&aid=nqqUserInfo&u=" + qq + "&on=1", "UTF-8");
		Document doc = Jsoup.parse(html);
		return doc.text().replace(" ", "\n");
	}

	/*
	 * 获取最近消息的好友的QQ号
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
	 * 获取最近消息的好友消息
	 * */
	
	
	/**
	 * 退出登录
	 */
	public void logout() {
		WebRequest.get("http://pt.3g.qq.com/s?sid=" + sid + "&aid=nLogout",
				"UTF-8");
	}

}
