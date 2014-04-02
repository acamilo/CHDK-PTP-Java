package packet;

import java.util.Arrays;

public class PTPPacket extends Packet{
	public final static short PTP_USB_CONTAINER_COMMAND = 1;
	public final static short PTP_USB_CONTAINER_DATA = 2;
	public final static short PTP_USB_CONTAINER_RESPONSE = 3;
	public final static short PTP_USB_CONTAINER_EVENT = 4;
	
	public final static short PTP_OPPCODE_CHDK = (short) 0x9999;
	public final static short PTP_OPPCODE_OpenSession = (short) 0x1002;
	
	// Locations of the fields in the PTP Packet
	// packet length including header. It is 12 bytes + data.length
	private static final int iPTPLength = 0;
	// Payload type
	private static final int iPTPcontainerCommand = 4;
	// PTP Function. This executes a function on the camera.
	// Data packets have a payload in the payload section
	// command,return packets have 0 to 5 32 bit little endian arguments
	private static final int iPTPoppcode = 6;
	// PTP Transaction. This number is used for any Data and Status responses sent back by the camera.
	// the 160sx doesn't seem to care if it's incremented. The stacks i've seen do seem to care.
	private static final int iPTPtransaction = 8;
	// Payload. For command packets this is 0 to 5 32 bit LE integer arguments
	// for data packets this is just data.
	private static final int iPTPpayload = 12;
	
	// Header size is always 12 bytes
	private static final int PTPHeaderSize= 12;
	
	

	public PTPPacket(short containerCommand, short oppcode, int transaction, byte[] data){
		super(new byte[PTPHeaderSize+data.length]);
		
		this.encodeInt(iPTPLength, 12+data.length, ByteOrder.LittleEndian);
		this.encodeShort(iPTPcontainerCommand, containerCommand, ByteOrder.LittleEndian);
		this.encodeShort(iPTPoppcode, oppcode, ByteOrder.LittleEndian);
		this.encodeInt(iPTPtransaction, transaction, ByteOrder.LittleEndian);
		this.encodeByteArray(iPTPpayload,data);
	}
	public PTPPacket(byte[] packet) {
		// TODO Auto-generated constructor stub
		super(packet);
	}
	public int getLength(){
		return this.decodeInt(iPTPLength, ByteOrder.LittleEndian);
	}
	public void setLength(int length){
		this.encodeInt(iPTPLength, length, ByteOrder.LittleEndian);
	}
	public int getContainerCommand(){
		return this.decodeShort(iPTPcontainerCommand, ByteOrder.LittleEndian);
	}
	public void setContainerCommand(short command){
		this.encodeShort(iPTPcontainerCommand, command, ByteOrder.LittleEndian);
	}
	public int getOppcode(){
		return this.decodeShort(iPTPoppcode, ByteOrder.LittleEndian);
	}
	public void setOppcoded(short oppcode){
		this.encodeShort(iPTPoppcode, oppcode, ByteOrder.LittleEndian);
	}
	public int getTransaction(){
		return this.decodeInt(iPTPtransaction, ByteOrder.LittleEndian);
	}
	public void setTransaction(int transaction){
		this.encodeInt(iPTPtransaction, transaction, ByteOrder.LittleEndian);
	}
	public String toString(){
		String r = "PTP Packet-------------\n\tLength:\t\t"+this.getLength()+"\n\tType:\t\t";
		if (this.getContainerCommand()==this.PTP_USB_CONTAINER_COMMAND) r += "Command(1)\n";
		else if (this.getContainerCommand()==this.PTP_USB_CONTAINER_DATA) r += "Data(2)\n";
		else if (this.getContainerCommand()==this.PTP_USB_CONTAINER_RESPONSE) r += "Response(3)\n";
		else if (this.getContainerCommand()==this.PTP_USB_CONTAINER_EVENT) r += "Event(4)\n";
		else r += "Unknown("+this.getContainerCommand()+")\n";
		r+="\tOppcode:\t0x"+Integer.toHexString( (int)this.getOppcode() & 0xFFFF)+"\n";
		r+="\tTransaction:\t"+this.getTransaction()+"\n";
		r+="\tData:\t\t("+(this.getLength()-12)+")[";
		if (this.getLength()>12 & this.getLength()<200) for (int i=12; i<this.getLength(); i++) r+=" "+this.decodeByte(i);
		r += " ]\n";
		return r;
	}
	public byte[] getData() {
		// TODO Auto-generated method stub
		return this.decodeByteArray(iPTPpayload, this.getLength()-12);
	}

}
