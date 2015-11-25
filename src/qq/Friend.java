package qq;

public class Friend {
	
	private String qq;
	private String name;
	private boolean online;
	
	
	public boolean isOnline() {
		return online;
	}
	public void setOnline(boolean online) {
		this.online = online;
	}
	public String getQq() {
		return qq;
	}
	public void setQq(String qq) {
		this.qq = qq;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Friend(String qq, String name) {
		this.qq = qq;
		this.name = name;
	}
	public Friend(){
		
	}
	
}
