package system;

import java.net.Socket;
import java.util.Date;

public class Message {
	private DeviceType type;
	private Socket socket;
    private String context;
    private int device_id;
    private Date date;

    public Message(DeviceType type,Socket socket,String context,int device_id,Date date){
    	this.type = type;
    	this.socket = socket;
    	this.context = context;
    	this.device_id = device_id;
    	this.date = date;
    }
    
    public DeviceType getType(){
    	return type;
    }

    public void setType(DeviceType type){
    	this.type = type;
    }

    public Socket getSocket(){
    	return socket;
    }

    public void setSocket(Socket socket){
    	this.socket = socket;
    }

    public String getContext(){
    	return context;
    }

    public void setContext(String context){
    	this.context = context;
    }

    public int getDevice_id(){
    	return device_id;
    }

    public void setDevice_id(int device_id){
    	this.device_id = device_id;
    }

    public Date getDate(){
    	return date;
    }

    public void setDate(Date date){
    	this.date = date;
    }

    public String toString(){
    	return "Message from [DeviceType:"+type+" socket:"+socket+" device_id:"+device_id
    		+" date:"+date+" context:"+context+"]";
    }

}
