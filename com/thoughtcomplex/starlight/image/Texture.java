package com.thoughtcomplex.starlight.image;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

import com.thoughtcomplex.starlight.util.ResourceLoader;

public class Texture {
	String resourceLocation = "";
	int width = 8;
	int height = 8;
	int internalWidth = 8;
	int internalHeight = 8;
	ByteBuffer rawdata = null;
	int handle = -1;
	private float left = 0;
	private float top = 0;
	private float bottom = 1.0f;
	private float right = 1.0f;
	//the fractions that represent the distance between the center of one pixel and the center of the next in each direction
	private float xPixelIncrement = right / ((float)width);
	private float yPixelIncrement = bottom / ((float)width);
	
	public Texture(String resource) {
		resourceLocation = resource;
		BufferedImage temp = ResourceLoader.getImage(resource);
		width = temp.getWidth();
		height = temp.getHeight();
		rawdata = ResourceLoader.convertImageData(temp);
		init();
	}
	
	public Texture(int width, int height, ByteBuffer data) {
		this.width = width;
		this.height = height;
		this.rawdata = data;
		if (rawdata!=null) init();
	}
	
	private void init() {
		if (handle!=-1) return; //prevent from double-initializing
		//generate a handle
		
		handle = GL11.glGenTextures();
		//System.out.println(resourceLocation+" bound to texture "+handle);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, handle);
		//Setup wrap mode
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		
        //Turn off linear interpolation
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, rawdata);
        internalWidth = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
        internalHeight = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
        
        if (internalWidth!=width) {
        	right = ((float)width) / ((float)internalWidth);
        }
        
        if (internalHeight!=height) {
        	bottom = ((float)height) / ((float)internalHeight);
        }
        
        xPixelIncrement = right / ((float)width);
    	yPixelIncrement = bottom / ((float)height);
	}
	
	public void bind() {
		if (handle!=-1) GL11.glBindTexture(GL11.GL_TEXTURE_2D, handle);
	}
	
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	
	public void paint(int x, int y) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, handle);
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(1, 1, 1);
		GL11.glBegin(GL11.GL_QUADS);
		{
			GL11.glTexCoord2f(left,top);
			GL11.glVertex2i(x, y);
			
			GL11.glTexCoord2f(right,top);
			GL11.glVertex2i(x+width,y);
			
			GL11.glTexCoord2f(right,bottom);
			GL11.glVertex2i(x+width, y+height);
			
			GL11.glTexCoord2f(left,bottom);
			GL11.glVertex2i(x, y+height);
		}
		GL11.glEnd();
	}
	
	public void paint(int destx, int desty, int destwidth, int destheight) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, handle);
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(1, 1, 1);
		GL11.glBegin(GL11.GL_QUADS);
		{
			GL11.glTexCoord2f(left,top);
			GL11.glVertex2i(destx, desty);
			
			GL11.glTexCoord2f(right,top);
			GL11.glVertex2i(destx+destwidth,desty);
			
			GL11.glTexCoord2f(right,bottom);
			GL11.glVertex2i(destx+destwidth, desty+destheight);
			
			GL11.glTexCoord2f(left,bottom);
			GL11.glVertex2i(destx, desty+destheight);
		}
		GL11.glEnd();
	}
	
	public void paint(int destx, int desty, int destwidth, int destheight, int srcx, int srcy, int srcwidth, int srcheight) {
		paint(destx,desty,destwidth,destheight,srcx,srcy,srcwidth,srcheight,GLColor.WHITE);
	}
	
	public void paint(int destx, int desty, int destwidth, int destheight, int srcx, int srcy, int srcwidth, int srcheight, GLColor c) {
		//System.out.println("Making texture "+resourceLocation+" available by binding #"+handle);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, handle);
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		c.bind();
		GL11.glBegin(GL11.GL_QUADS);
		{
			//GL11.glColor3b(c.getRedByte(), c.getGreenByte(), c.getBlueByte());
			GL11.glTexCoord2f(srcx*xPixelIncrement, srcy*yPixelIncrement);
			GL11.glVertex2i(destx, desty);
			
			GL11.glTexCoord2f((srcx+srcwidth)*xPixelIncrement, srcy*yPixelIncrement);
			GL11.glVertex2i(destx+destwidth,desty);
			
			GL11.glTexCoord2f((srcx+srcwidth)*xPixelIncrement,(srcy+srcheight)*yPixelIncrement);
			GL11.glVertex2i(destx+destwidth, desty+destheight);
			
			GL11.glTexCoord2f(srcx*xPixelIncrement,(srcy+srcheight)*yPixelIncrement);
			GL11.glVertex2i(destx, desty+destheight);
		}
		GL11.glEnd();
	}

}
