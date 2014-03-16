package camera;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

import packet.CHDKScreenImage;

public class displayImage extends JPanel{
	CHDKScreenImage image;
	public displayImage(CHDKScreenImage i){
		image=i;
		JFrame frame = new JFrame("Image Display");
		frame.add(this);
		frame.setSize(i.viewportVisableWidth(), i.viewportVisableHeight());
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public byte ClipYUV(int v){
		 if (v<0) return 0;
		    if (v>255) return (byte)255;
		    return (byte)v;
	}
	public byte YUV_to_R(byte y, byte v){
		return ClipYUV(((y<<12) +          v*5743 + 2048)>>12);
	}
	public byte YUV_to_G(byte y, byte u, byte v){
		return ClipYUV(((y<<12) - u*1411 - v*2925 + 2048)>>12);
	}
	public byte YUV_to_B(byte y, byte u){
		return ClipYUV(((y<<12) + u*7258          + 2048)>>12);
	}
	
	
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		BufferedImage b = new BufferedImage(image.viewportVisableWidth(),image.viewportVisableHeight(),BufferedImage.TYPE_3BYTE_BGR);
		b.setRGB(719,239, 0x00ff0000);
		byte[] buf = image.decodeByteArray(image.viewportDataStart(), image.getPacket().length);
		int index=0;
		for (int y=0; y<image.viewportVisableHeight()-1; y++){
			for (int x=0; x<image.viewportVisableWidth()-1; x+=4){
				b.setRGB(x,y, buf[index+1]);
				b.setRGB(x+1,y, buf[index+3]);
				b.setRGB(x+2,y, buf[index+4]);
				b.setRGB(x+3,y, buf[index+5]);
				index+=6;
				//byte red=0,green=0,blue=0,yy=0,u=0,v=0;
				
				//yy = buf(nd+1)
				//u = 
				//v=
			
				//red = YUV_to_R(yy,v);
				//green = YUV_to_G(yy,u,v);
				//blue = YUV_to_B(yy,u);
			}
		}
		
		
		
		g2d.drawImage(b, 0, 0, b.getWidth(),b.getHeight() , null);
	}
}
