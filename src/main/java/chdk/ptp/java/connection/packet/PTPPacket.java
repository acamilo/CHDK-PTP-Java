package chdk.ptp.java.connection.packet;

import chdk.ptp.java.exception.InvalidPacketException;

public class PTPPacket extends Packet {
	public final static short PTP_USB_CONTAINER_COMMAND = 1;
	public final static short PTP_USB_CONTAINER_DATA = 2;
	public final static short PTP_USB_CONTAINER_RESPONSE = 3;
	public final static short PTP_USB_CONTAINER_EVENT = 4;

	// CHDK Oppcodes
	public final static int CHDK_Version = 0;
	public final static int CHDK_GetMemory = 1;
	public final static int CHDK_SetMemory = 2;
	public final static int CHDK_CallFunction = 3;
	public final static int CHDK_TempData = 4;
	public final static int CHDK_UploadFile = 5;
	public final static int CHDK_DownloadFile = 6;
	public final static int CHDK_ExecuteScript = 7;
	public final static int CHDK_ScriptStatus = 8;
	public final static int CHDK_ScriptSupport = 9;
	public final static int CHDK_ReadScriptMsg = 10;
	public final static int CHDK_WriteScriptData = 11;
	public final static int CHDK_GetDisplayData = 12;
	public final static int CHDK_RemoteCaptureIsReady = 13;
	public final static int CHDK_RemoteCaptureGetData = 14;

	public final static short PTP_OPPCODE_CHDK = (short) 0x9999;
	public final static short PTP_OPPCODE_OpenSession = (short) 0x1002;
	public final static short PTP_OPPCODE_Response_OK = (short) 0x2001;

	// Locations of the fields in the PTP Packet
	// chdk.ptp.java.connection.packet length including header. It is 12 bytes +
	// data.length
	public static final int iPTPLength = 0;
	// Payload type
	public static final int iPTPcontainerCommand = 4;
	// PTP Function. This executes a function on the chdk.ptp.java.connection.
	// Data packets have a payload in the payload section
	// command,return packets have 0 to 5 32 bit little endian arguments
	public static final int iPTPoppcode = 6;
	// PTP Transaction. This number is used for any Data and Status responses
	// sent back by the chdk.ptp.java.connection.
	// the 160sx doesn't seem to care if it's incremented. The stacks i've seen
	// do seem to care.
	public static final int iPTPtransaction = 8;
	// Payload. For command packets this is 0 to 5 32 bit LE integer arguments
	// for data packets this is just data.
	public static final int iPTPpayload = 12;

	// Header size is always 12 bytes
	public static final int PTPHeaderSize = 12;

	// PTP command chdk.ptp.java.connection.packet has 0-5 args. max 5.
	public static final int iPTPCommandARG0 = iPTPpayload + 0;
	public static final int iPTPCommandARG1 = iPTPpayload + 4;
	public static final int iPTPCommandARG2 = iPTPpayload + 8;
	public static final int iPTPCommandARG3 = iPTPpayload + 12;
	public static final int iPTPCommandARG4 = iPTPpayload + 16;

	public PTPPacket(short containerCommand, short oppcode, int transaction,
			byte[] data) {
		super(new byte[PTPHeaderSize + data.length]);

		this.encodeInt(iPTPLength, 12 + data.length, ByteOrder.LittleEndian);
		this.encodeShort(iPTPcontainerCommand, containerCommand,
				ByteOrder.LittleEndian);
		this.encodeShort(iPTPoppcode, oppcode, ByteOrder.LittleEndian);
		this.encodeInt(iPTPtransaction, transaction, ByteOrder.LittleEndian);
		this.encodeByteArray(iPTPpayload, data);
	}

	public PTPPacket(byte[] packet) throws InvalidPacketException {
		super(packet);
		// TODO Auto-generated constructor stub
		if (packet.length < 12) {
			for (int i = 0; i < packet.length; i++)
				System.out.print(packet[i] + ",");
			System.out.println();
			throw new InvalidPacketException("Packet of len" + packet.length
					+ " is too short!");
		}
	}

	public int getLength() {
		return this.decodeInt(iPTPLength, ByteOrder.LittleEndian);
	}

	public void setLength(int length) {
		this.encodeInt(iPTPLength, length, ByteOrder.LittleEndian);
	}

	public int getContainerCommand() {
		return this.decodeShort(iPTPcontainerCommand, ByteOrder.LittleEndian);
	}

	public void setContainerCommand(short command) {
		this.encodeShort(iPTPcontainerCommand, command, ByteOrder.LittleEndian);
	}

	public int getOppcode() {
		return this.decodeShort(iPTPoppcode, ByteOrder.LittleEndian);
	}

	public void setOppcoded(short oppcode) {
		this.encodeShort(iPTPoppcode, oppcode, ByteOrder.LittleEndian);
	}

	public int getTransaction() {
		return this.decodeInt(iPTPtransaction, ByteOrder.LittleEndian);
	}

	public void setTransaction(int transaction) {
		this.encodeInt(iPTPtransaction, transaction, ByteOrder.LittleEndian);
	}

	public int getDataLength() {
		return this.getLength() - 12;
	}

	@Override
	public String toString() {
		String DIR = "";
		if (this.getContainerCommand() == this.PTP_USB_CONTAINER_COMMAND)
			DIR = " >>>";
		if (this.getContainerCommand() == this.PTP_USB_CONTAINER_DATA)
			DIR = " ***";
		if (this.getContainerCommand() == this.PTP_USB_CONTAINER_RESPONSE)
			DIR = " <<<";
		if (this.getContainerCommand() == this.PTP_USB_CONTAINER_EVENT)
			DIR = " <<<!";

		String r = "PTP Packet" + DIR + "\n\tLength:\t\t" + this.getLength()
				+ "\n\tType:\t\t";
		if (this.getContainerCommand() == this.PTP_USB_CONTAINER_COMMAND)
			r += "Command(1)\n";
		else if (this.getContainerCommand() == this.PTP_USB_CONTAINER_DATA)
			r += "Data(2)\n";
		else if (this.getContainerCommand() == this.PTP_USB_CONTAINER_RESPONSE)
			r += "Response(3)\n";
		else if (this.getContainerCommand() == this.PTP_USB_CONTAINER_EVENT)
			r += "Event(4)\n";
		else
			r += "Unknown(" + this.getContainerCommand() + ")\n";
		r += "\tOppcode:\t0x" + Integer.toHexString(this.getOppcode() & 0xFFFF)
				+ "\n";
		r += "\tTransaction:\t" + this.getTransaction() + "\n";
		r += "\tData:\t\t(" + (this.getLength() - 12) + ")[";

		if (this.getLength() > 12)// & this.getLength() < 200)
			for (int i = 12; i < ((this.getLength() < 200) ? this.getLength()
					: 200); i++)
				r += " " + String.format("%02X", this.decodeByte(i));
		r += " ]\n";
		return r;
	}

	public byte[] getData() {
		// TODO Auto-generated method stub
		return this.decodeByteArray(iPTPpayload, this.getLength() - 12);
	}

}
