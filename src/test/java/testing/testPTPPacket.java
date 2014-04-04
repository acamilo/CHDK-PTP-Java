package testing;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.acamilo.chdk.ptp.java.connection.packet.PTPPacket;
import org.junit.Test;

public class testPTPPacket {

	@Test
	public void test() {
		
		byte[] testPacket = new byte[]{ 20, 0, 0, 0, 1, 0, (byte)-103, (byte)-103, 1, 0, 0, 0, 12, 0, 0, 0, 1, 0, 0, 0 };
		PTPPacket p = new PTPPacket(testPacket);
		assertEquals(20,p.getLength());
		assertEquals(PTPPacket.PTP_USB_CONTAINER_COMMAND,p.getContainerCommand());
		assertEquals((short)0x9999,p.getOppcode());
		assertEquals(1,p.getTransaction());
		
		System.out.println(p);
		for (byte c : p.getData()) 		System.out.print(" "+c);
		System.out.println();
		for (byte c : Arrays.copyOfRange(testPacket,12,testPacket.length)) 		System.out.print(" "+c);

		assertArrayEquals(Arrays.copyOfRange(testPacket,12,testPacket.length),p.getData());
		
		
		PTPPacket g = new PTPPacket(PTPPacket.PTP_USB_CONTAINER_COMMAND,(short)0x9999,1,Arrays.copyOfRange(testPacket,12,testPacket.length));
		
		assertArrayEquals(g.getPacket(),testPacket);
	}

}
