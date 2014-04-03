package testing;

import static org.junit.Assert.*;

import org.junit.Test;

import packet.ByteOrder;
import packet.Packet;

public class testPacket {

	@Test
	public void test() throws Exception {
		byte[] buff = new byte[100];
		Packet p = new Packet(buff);
		
		// reading out the packet
		assertArrayEquals(buff,p.getPacket());
		
		// encode byte test
		p.encodeByte(0, (byte) 0x53);
		assertEquals(buff[0],0x53);
		
		// decode byte test
		buff[0]=(byte)0xf5;
		assertEquals(p.decodeByte(0),(byte)0xf5);
		
		// encodeShort test
		p.encodeShort(0, (short)8546, ByteOrder.LittleEndian);
		assertEquals(buff[0],(byte)0x62);
		assertEquals(buff[1],(byte)0x21);
		assertEquals((short)8546,p.decodeShort(0,  ByteOrder.LittleEndian));
		
		// encodeShort test
		p.encodeInt(0, (int)8544326, ByteOrder.LittleEndian);
		assertEquals(buff[0],(byte)0x46);
		assertEquals(buff[1],(byte)0x60);
		assertEquals(buff[2],(byte)0x82);
		assertEquals(buff[3],(byte)0x00);
		assertEquals(8544326,(int)p.decodeInt(0,  ByteOrder.LittleEndian));
		
		// encodeShort test
		p.encodeShort(0, (short)8546, ByteOrder.BigEndian);
		assertEquals(buff[1],(byte)0x62);
		assertEquals(buff[0],(byte)0x21);
		assertEquals((short)8546,p.decodeShort(0,  ByteOrder.BigEndian));
		
		// encodeShort test
		p.encodeInt(0, (int)8544326, ByteOrder.BigEndian);
		assertEquals(buff[3],(byte)0x46);
		assertEquals(buff[2],(byte)0x60);
		assertEquals(buff[1],(byte)0x82);
		assertEquals(buff[0],(byte)0x00);
		assertEquals(8544326,(int)p.decodeInt(0,  ByteOrder.BigEndian));
		
		// array tesy
		byte[] sample = new byte[]{1,2,3,4,5,6,7,8,9};
		p.encodeByteArray(5, sample);
		assertArrayEquals(p.decodeByteArray(5, sample.length),sample);
		
		
	}

}
