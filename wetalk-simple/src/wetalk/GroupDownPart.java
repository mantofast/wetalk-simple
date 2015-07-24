package wetalk;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import usermanager.User;
import message.BaseMessage;
import message.GroupChatMessage;
import GUI.*;
public class GroupDownPart extends Thread{
	protected static Logger log;
	static int GroupListenPort = 5678;
	static boolean isRunning = true;	//线程运行标志
	 Queue<GroupChatMessage> chatcontent=null;
	//侦听群聊端口消息，后台一直要在运行的
	public void run(){
		 GroupListen();
	}
	public GroupDownPart(  Queue<GroupChatMessage>  chatcontent  ){
		this.chatcontent=chatcontent;
	}
	public void GroupListen(){
		log.info("Group service start.");
		MulticastSocket ms;
		byte[] buffer = new byte[1024];
		DatagramPacket pkt = null;
	    pkt = new DatagramPacket(buffer,buffer.length);
	    //群消息记录队列
	   
	    try {
			 ms=new MulticastSocket(GroupListenPort);
			 while(isRunning){
				 ms.receive(pkt);
				 byte[] pktdata = pkt.getData();
				 GroupChatMessage recvmsg = GroupChatMessage.deserialize(pktdata);
				//用一个消息并发队列进行存储
				 chatcontent.add(recvmsg);
				 //群聊界面的工作需要分消息类型进行处理
				 switch(recvmsg.getType()){
				 case GroupChatMessage.BUILD_GROUP_MESSAGE:
					 //加入群组
					 ms.joinGroup(recvmsg.getGroupIp());					 
					 break;
				 case GroupChatMessage.JION_GROUP_MESSAGE:
					 break;
				 case GroupChatMessage.LEAVE_GROUP_MESSAGE:
					 break;
					 //消息生产者
				 case GroupChatMessage.CHATING_GROUP_MESSAGE:
					 break;
					 //前端界面线程调用队列中消息并显示
				 default:
					 break;
				 }
			 }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	//前台触发，点击了确定建群按钮，即选定了人员后,输入群组名称
	public void BulidGroup(List<User> groupUser,MulticastSocket ms,String groupName){
		try {
		//建组，选择一个组播地址
			 InetAddress groupip = InetAddress.getByName("224.2.2.2"); 
			 //由用户自己选择
			 
			 ms.joinGroup(groupip);
		//构造邀请广播消息
			 GroupChatMessage invitation=new GroupChatMessage( GroupChatMessage.BUILD_GROUP_MESSAGE,GroupListenPort ,groupip, groupName,null);
			 invitation.setUser(groupUser);
			 byte inv[]=invitation.srialize();
			 
	   //遍历成员列表，发送广播
		     ListIterator<User> iter=groupUser.listIterator();
		        //遍历名单，发送邀请
		        while(iter.hasNext()){
		        	DatagramPacket pkt=  new DatagramPacket(inv,inv.length,InetAddress.getByName(iter.next().getIPAddress()),GroupListenPort);
		        	ms.send(pkt);
		        	}	
		        
		
		
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	
}
	
public void sendGroupMessage(MulticastSocket ms,String groupName,InetAddress groupIp){
	//构造群聊消息
	String msg=null;//从窗口读入即将发送的消息
	GroupChatMessage content=new GroupChatMessage( GroupChatMessage.CHATING_GROUP_MESSAGE ,GroupListenPort ,groupIp, groupName,msg);
	
	 byte send[]=content.srialize();
	
}
		
	
}


