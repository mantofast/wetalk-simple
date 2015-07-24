package GroupTalk;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Queue;

import usermanager.User;
import GUI.ChatFrameGroupChat;
import message.GroupChatMessage;

public class groupMessageReceive {
	MulticastSocket ms;
    int GroupListenPort=5555;
    boolean isRunning=true;
    static Queue<GroupChatMessage> chatcontent;
    User Me;
    public groupMessageReceive(Queue<GroupChatMessage> chatcontent,User Me){
    	this.chatcontent=chatcontent;
    	this.Me=Me;
    }
    
    //群消息记录队列
   public  void MessageReceive(){
	while(true){
    try {
		 ms=new MulticastSocket(GroupListenPort);
		 byte[] buffer = new byte[1024];
	     DatagramPacket pkt;
	     pkt = new DatagramPacket(buffer,buffer.length);
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
				 //新建群聊界面，打开群聊界面：ChatFrameGroup,开启群聊窗口
				  ChatFrameGroupChat.showFrameChat(recvmsg.getGroupName(),recvmsg.getGroupIp(),this.Me,chatcontent);
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
   }
}
