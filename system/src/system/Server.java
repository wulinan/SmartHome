package system;
import java.net.*;
import java.util.*;
import java.io.*;
import system.DeviceType;
import system.Message;


public class Server extends ServerSocket{
	private static final int SERVER_PORT = 2016;//服务器端口
	private static Object lock = new Object();//同步锁
	private static boolean hasMessage = false;//是否输出消息标志
	private static List<DeviceType> device_list = new ArrayList<DeviceType>();//记录连接设备
	private static List<ServerThread> thread_list = new ArrayList<ServerThread>();//记录设备对应的服务器线程
	private static Map<DeviceType,Message> message_list = new HashMap<DeviceType,Message>();//全局的信息存储
	private ServerMsgListener mServerMsgListener;

	public Server() throws IOException{
		super(SERVER_PORT);
		try{
			while(true){
			Socket socket = accept();
			new ServerThread(socket);
		    }
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			close();
		}
	}

	public void sendMsgToAllClients(String msg){
		new Thread(new Runnable(){
			@Override
			public void run(){
				for(ServerThread thread:thread_list){
					thread.sendMessage(msg);
				}
			}

		}).start();
	}

    //向特定设备发送消息
	public void send(DeviceType type,Message msg){
		System.out.printf("发送消息:%s",type.getName());
		if(thread_list.size()==0) return;
		for(ServerThread thread:thread_list){
			if(thread.type.getName().equals(type.getName())){
				thread.sendMessage(msg.getContext());
				System.out.println("发送成功");
				return;
			}
		}
		System.out.println("没有找到相应设备");
	}

    //监听是否收到相应设备的消息
	public void receive(DeviceType type){
		new Thread(new Runnable(){
			@Override
			public void run(){
				while(true){
					synchronized(lock){
						if(hasMessage){
							//如果没有收到相应设备的消息则继续监听，如果收到了则返回消息。
							if(!message_list.containsKey(type)){
								System.out.println("没有这个设备的消息");
								continue;
							}else{
								System.out.printf("接收消息:%s",type.getName());
							    Message msg= message_list.get(type);
								mServerMsgListener.handleMsg(msg);//定义相应接口，由应用程序编程实现如何处理获得的消息
							}
						}
					}
				}
			}
		}).start();
	}

	public List<DeviceType> listOnlineDevice(){
		String s = "----在线设备列表----\n";
		for(DeviceType device : device_list){
			s+=device+"\n";
		}
		s+="----------------------";
		System.out.println(s);
     	return device_list;
	}
    
    //每个客户端对应的服务器线程
	class ServerThread extends Thread{
		private Socket client;
		private PrintWriter out;
		private BufferedReader in;
		private DeviceType type;

		public ServerThread(Socket s) throws IOException{
			client = s;
			out = new PrintWriter(client.getOutputStream(),true);
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			out.println("成功连接上服务器，请告诉我你的类型");
			start();
		}

		public void run(){
			try{
				//规定客户端连接上服务器之后首先需要发送自己的类型。
				String line = in.readLine();
				type = strToDT(line);
				if(type==null) {
					client.close();
					in.close();
					out.close();
				}
				device_list.add(type);
				thread_list.add(this);
				out.println("可以进行数据交流了");
                receiveMessage();
				}catch(IOException e){
					e.printStackTrace();
				}
		}

		//服务器向客户端发送消息
        void sendMessage(String msg){
        	out.println(msg);
        } 

        //服务器接收客户端消息
        void receiveMessage(){
            new Thread(new Runnable(){
            	@Override
            	public void run(){
            		while(!client.isClosed()){
            			try{
            				String str = in.readLine();
            				if(str==null || str.equals("")) continue;
            				Message msg = new Message(type,client,str,1,Calendar.getInstance().getTime());
            				System.out.printf("receive message from client:%s",str);
            				pushMessage(msg);
            			}catch(IOException e){
            				e.printStackTrace();
            			}
            		}
            	}

            }).start();
        }
        //加入消息队列
        void pushMessage(Message msg){
        	//如果其他线程有对这两个变量的改动，则一定要加锁
        	synchronized(lock){
        		message_list.put(type,msg);
        		hasMessage = true;
        	}
        }
	}
	
//	public void beginListenClient(DeviceType type){
//		new Thread(new Runnable(){
//
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				if(device_list.contains(type))
//					mServerMsgListener.handleMsg(msg);
//			}
//			
//		}).start();
//	}


	DeviceType strToDT(String s){
		switch(s){
			case "LIGHT":
				return DeviceType.LIGHT;
			case "TV":
				return DeviceType.TV;
			case "AIR_CONTAINER":
				return DeviceType.AIR_CONTAINER;
			case "THERMOMETER":
				return DeviceType.THERMOMETER;
			default:
				System.out.println("未识别的设备类型");
				return null;
		}
	}
   //定义接口，引用程序可以继承该接口定义自定义如何处理获得的消息
	public static interface ServerMsgListener{
		public void handleMsg(Message msg);
	}
	
	public void setServerMsgListener(ServerMsgListener mServerMsgListener){
		this.mServerMsgListener = mServerMsgListener;
	}
	
	@SuppressWarnings("resource")
	public static void main(String[] args){
		try {
			Server server = new Server();
			List<DeviceType> onlineDevice = server.listOnlineDevice();
			new Thread(new Runnable(){
				@Override
				public void run() {
					System.out.println("进入线程");
					// TODO Auto-generated method stub
					while(true){
						if(onlineDevice.contains(DeviceType.LIGHT) && onlineDevice.contains(DeviceType.TV)){
							server.send(DeviceType.LIGHT, new Message(null,null,"state",1,null));
							server.receive(DeviceType.LIGHT);
							server.send(DeviceType.TV, message_list.get(DeviceType.LIGHT));
						}
					}
				}
				
			}).start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
