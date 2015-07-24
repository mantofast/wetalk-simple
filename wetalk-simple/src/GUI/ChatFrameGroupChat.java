package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Queue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import message.GroupChatMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import usermanager.User;

public class ChatFrameGroupChat extends JFrame {
	protected static Logger log = 
			LoggerFactory.getLogger(ChatFrameGroupChat.class);

	private JPanel contentPane;
	private JTextField textField;
	private volatile JTextArea textArea;
	int GroupListenPort=5555;
	String groupName;
	InetAddress groupIp;
	User ME;
	static Queue<GroupChatMessage> chatcontent;

	/**
	 * Create the frame.
	 */
	public ChatFrameGroupChat(String groupName,InetAddress groupIp,User Me,Queue<GroupChatMessage> chatcontent) {
		
        this.groupIp=groupIp;
        this.groupName=groupName;
        this.ME=Me;
        this.chatcontent=chatcontent;
		setIconImage(Toolkit.getDefaultToolkit().getImage("./wetalk-single/icons/title.png"));	
        setTitle(this.groupName);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				chatExit(e);
			}
		});
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 509, 479);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JLayeredPane layeredPane = new JLayeredPane();
		layeredPane.setForeground(SystemColor.inactiveCaption);
		layeredPane.setBackground(SystemColor.inactiveCaption);
		contentPane.add(layeredPane, BorderLayout.CENTER);
		
		JLabel backgroundLabel = new JLabel("");
		backgroundLabel.setBackground(SystemColor.inactiveCaption);
		backgroundLabel.setFont(new Font("宋体", Font.PLAIN, 14));
		backgroundLabel.setForeground(SystemColor.inactiveCaption);
		backgroundLabel.setBounds(0, 0, 483, 431);
		layeredPane.add(backgroundLabel);
		
		JButton sendButton = new JButton("发送");
		sendButton.setForeground(new Color(0, 0, 128));
		sendButton.setBackground(SystemColor.inactiveCaption);
		sendButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				sendButtenEvent();
			}
		});
		sendButton.setFont(new Font("宋体", Font.BOLD, 16));
		sendButton.setBounds(390, 383, 83, 38);
		layeredPane.add(sendButton);
		
		textField = new JTextField();
		textField.setFont(new Font("宋体", Font.PLAIN, 18));
		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_ENTER){
					sendButtenEvent();
				} else if(e.getKeyCode()==KeyEvent.VK_ENTER){
					
				}
			}
		});
		textField.setForeground(new Color(153, 0, 0));
		textField.setBackground(SystemColor.inactiveCaption);
		textField.setBounds(0, 352, 380, 69);
		layeredPane.add(textField);
		textField.setColumns(10);
		textField.setHorizontalAlignment(JTextField.LEFT);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 0, 380, 342);
		layeredPane.add(scrollPane);
		
		textArea = new JTextArea();
		textArea.setBackground(SystemColor.inactiveCaption);
		textArea.setFont(new Font("宋体", Font.PLAIN, 20));
		textArea.setForeground(new Color(0, 0, 102));
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);
		
		
	}
	//显示收到的消息
	public class MessageReceive extends Thread{
	public void run(){
	while(true){
	while(!chatcontent.isEmpty()){
	GroupChatMessage recvmsg = chatcontent.poll();
	    if(recvmsg.getGroupIp().equals(groupIp)){
			 switch(recvmsg.getType()){
			 case GroupChatMessage.BUILD_GROUP_MESSAGE:		
				  break;
			 case GroupChatMessage.JION_GROUP_MESSAGE:
				 break;
			 case GroupChatMessage.LEAVE_GROUP_MESSAGE:
				 break;
				 //消息生产者
			 case GroupChatMessage.CHATING_GROUP_MESSAGE:
				 textArea.append(recvmsg.getContent()+ '\n');	//显示消息
				 break;
				 //前端界面线程调用队列中消息并显示
			 default:
				 break;
			 }
		
		}
	}
}
	}
}
	//发送消息（事件驱动）
	protected void sendButtenEvent(){
		String payload = textField.getText(); //获得用户名
		//判断发送消息是否为空
		if(payload.compareTo("")!=0 ) {
			
			textArea.append(this.ME.getName()+':');	//显示消息
		    textArea.append(payload + '\n');	//显示消息
			
			textField.setText(null);//发送框的
			//构造消息数据报
			GroupChatMessage content=new GroupChatMessage( GroupChatMessage.CHATING_GROUP_MESSAGE ,this.GroupListenPort ,this.groupIp, this.groupName,this.ME.getName()+':'+payload);
			try {
				MulticastSocket ms=new MulticastSocket();
			    byte send[]=content.srialize();
			    DatagramPacket pkt=new DatagramPacket(send,send.length,groupIp,GroupListenPort);
			    ms.send(pkt);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
		}
		
	}
	
	//关闭窗口退出操作
	private void chatExit(WindowEvent e){
		log.info("finish");
	}
		
	//显示会话窗口。静态方法
	public static void showFrameChat(String groupName,InetAddress groupIp,User Me,Queue<GroupChatMessage> chatcontent) {
		try {
			ChatFrameGroupChat framechat;
			framechat = new ChatFrameGroupChat(groupName,groupIp,Me,chatcontent);
			framechat.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			// 把窗口置于中心
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension frameSize = framechat.getSize();
			if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
			}
			if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
			}
			framechat.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height -
			frameSize.height) / 2);
			
			framechat.setVisible(true);
			ChatFrameGroupChat.MessageReceive rece=framechat.new MessageReceive();
		 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
