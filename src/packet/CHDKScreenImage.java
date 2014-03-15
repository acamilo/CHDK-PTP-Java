package packet;

public class CHDKScreenImage extends Packet{
	public final static int Aspect_4_3 = 0;
	public final static int	Aspect_16_9 = 1;
	
	public CHDKScreenImage(byte[] packet) {
		super(packet);
		// TODO Auto-generated constructor stub
	}
	
	public int getMajorVersion(){ return 0; }
	public int getMinorVersion(){ return 0; }
	public int lcdAspectRatio(){ return 0;}
	public int palleteType(){ return 0; }
	public int paletteDataStart(){ return 0; }
	public int viewportDescriptorStart(){ return 0; }
	public int bitmapDescriptorStart(){ return 0; }
	
	// Viewport Metadata
	public int viewportFramebufferType(){ return 0; }
	public int viewportDataStart(){ return 0; }
	/*
    buffer width in pixels
    data size is always buffer_width*visible_height*(buffer bpp based on type)
    */
	public int viewportBufferWidth(){ return 0; }
    /*
    visible size in pixels
    describes data within the buffer which contains image data to be displayed
    any offsets within buffer data are added before sending, so the top left
    pixel is always the first first byte of data.
    width must always be <= buffer_width
    if buffer_width is > width, the additional data should be skipped
    visible_height also defines the number of data rows
    */
	public int viewportVisableHeight(){ return 0; }
	public int viewportVisableWidth(){ return 0; }
    /*
    margins
    pixels offsets needed to replicate display position on cameras screen
    not used for any buffer offsets
    */
	public int viewportMarginLeft(){ return 0; }
	public int viewportMarginTop(){ return 0; }
	public int viewportMarginRight(){ return 0; }
	public int viewportMarginBottom(){ return 0; }
	
	// Bitmap Metadata
	public int bitmapFramebufferType(){ return 0; }
	public int bitmapDataStart(){ return 0; }
	public int bitmapBufferWidth(){ return 0; }
	public int bitmapVisableHeight(){ return 0; }
	public int bitmapVisableWidth(){ return 0; }
	public int bitmapMarginLeft(){ return 0; }
	public int bitmapMarginTop(){ return 0; }
	public int bitmapMarginRight(){ return 0; }
	public int bitmapMarginBottom(){ return 0; }
	
	
	

}

