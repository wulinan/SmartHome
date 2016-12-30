package system;

public enum DeviceType {
	LIGHT("LIGHT",1),TV("TV",2),AIR_CONTAINER("AIR_CONTAINER",3),THERMOMETER("THERMOMETER",4);

	private String name;
	private int index;

	private DeviceType(String name,int index){
		this.name = name;
		this.index = index;
	}

	public static String getName(int index){
		for(DeviceType type:DeviceType.values()){
			if(type.getIndex()==index)
				return type.name;
		}
		return null;
	}

	public int getIndex(){
		return index;
	}

	public void setIndex(int index){
		this.index = index;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public String toString(){
		return this.index+"_"+this.name;
	}

}
