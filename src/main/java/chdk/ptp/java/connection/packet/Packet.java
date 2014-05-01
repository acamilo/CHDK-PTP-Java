package chdk.ptp.java.connection.packet;

import java.util.Arrays;

public class Packet {

    private byte[] packet;

    public Packet(byte[] packet) {
	this.packet = packet;
    }

    public byte[] getPacket() {
	return packet;
    }

    public void setPacket(byte[] packet) {
	this.packet = packet;
    }

    public byte decodeByte(int index) {
	return packet[index];
    }

    public short decodeShort(int index, ByteOrder order) {
	switch (order) {
	case BigEndian:
	    return (short) ((0x000000ff & packet[index + 1]) | (0x000000ff & packet[index]) << 8);
	case LittleEndian:
	    return (short) ((0x000000ff & packet[index]) | (0x000000ff & packet[index + 1]) << 8);
	}
	return 0;
    }

    public int decodeInt(int index, ByteOrder order) {
	switch (order) {
	case LittleEndian:
	    return (0x000000ff & packet[index])
		    | (0x000000ff & packet[index + 1]) << 8
		    | (0x000000ff & packet[index + 2]) << 16
		    | (0x000000ff & packet[index + 3]) << 24;
	case BigEndian:
	    return (0x000000ff & packet[index + 3])
		    | (0x000000ff & packet[index + 2]) << 8
		    | (0x000000ff & packet[index + 1]) << 16
		    | (0x000000ff & packet[index]) << 24;
	}
	return 0;
    }

    public void encodeByte(int index, byte b) {
	packet[index] = b;
    }

    public void encodeShort(int index, short s, ByteOrder order) {
	// Add container command
	switch (order) {
	case LittleEndian:
	    packet[index] = (byte) (s);
	    packet[index + 1] = (byte) (s >> 8);
	    break;
	case BigEndian:
	    packet[index + 1] = (byte) (s);
	    packet[index] = (byte) (s >> 8);
	}
    }

    public void encodeInt(int index, int s, ByteOrder order) {
	// Add container command
	switch (order) {
	case LittleEndian:
	    packet[index] = (byte) (s);
	    packet[index + 1] = (byte) (s >> 8);
	    packet[index + 2] = (byte) (s >> 16);
	    packet[index + 3] = (byte) (s >> 24);
	    break;
	case BigEndian:
	    packet[index + 3] = (byte) (s);
	    packet[index + 2] = (byte) (s >> 8);
	    packet[index + 1] = (byte) (s >> 16);
	    packet[index] = (byte) (s >> 24);
	}
    }

    public void encodeByteArray(int index, byte[] arr) {
	for (int j = 0; j < arr.length; j++) {
	    packet[j + index] = arr[j];
	}
    }

    public byte[] decodeByteArray(int index, int len) {
	return Arrays.copyOfRange(packet, index, index + len);

    }

}
