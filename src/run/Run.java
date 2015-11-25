package run;
import java.util.Scanner;

import qq.QQXy;

public class Run {
	public static void main(String args[]) throws InterruptedException{
		boolean f = true;
		Scanner scn = new Scanner(System.in);
		QQXy qq = new QQXy();
		while(f){
		System.out.println("开始");
		
		int i = qq.login("528397553", "dandan520", 0);
		if(i == 3){
			qq.loginBySid(scn.next());
		}
		System.out.println(i);
		boolean same;
		String mO = "";
		String qqNum="528397553",qqMsg= "1",ans="1";
		while(f){
			qqNum = qq.getLatestFriend();
			if(qqNum.isEmpty() || qqMsg.isEmpty())break;
			System.out.println("最近联系人:"+qqNum);
			mO = qqMsg;
			qqMsg = qq.getLatestMessage(qqNum);
			System.out.println("last message:"+qqMsg);
			if(qqNum.isEmpty() || qqMsg.isEmpty())break;
			if( ans.equals(qqMsg) || mO.equals(qqMsg) ){
				System.out.println("等新消息");
			}else{
				System.out.println("有新的消息");
				System.out.println(mO+" "+qqMsg);
				ans = Robot.Robot.Answer(qqMsg );
				if(qq.sendMessage(qqNum,ans))System.out.println("发送成功");
				else System.out.println("发送失败");
				System.out.println("\tta说\t:"+qqMsg);	
				System.out.println("\t我回答\t:"+ans);
			}						
			System.out.println("sleep------------------------\n");
			Thread.sleep(5000);
		}
		System.out.println("logout");
		qq.logout();
		}
	}
}
