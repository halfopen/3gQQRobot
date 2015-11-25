package util;

public class NameValue {

	private StringBuilder sb = new StringBuilder();

	public void add(String name, String value) {
		sb.append("&");
		sb.append(name);
		sb.append("=");
		sb.append(value);
	}

	public String toString() {
		return sb.toString();
	}
}
