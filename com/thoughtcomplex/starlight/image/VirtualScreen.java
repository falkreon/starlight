package com.thoughtcomplex.starlight.image;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;



public class VirtualScreen {
	//static boolean keepRunning = true;
	//static VirtualScreen instance = null;
	private int handle = -1;
	private int fboHandle = -1;
	private int stencilHandle = -1;
	private int width = 200;
	private int height = 100;
	private boolean active = false;
	
	
	public VirtualScreen(int width, int height) {
		this.width = width;
		this.height = height;
		bindFrameBuffer();
	}
	
	
	
	public void clear() {
		if (!active) {
			activateSurface();
			GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			deactivateSurface();
		} else {
			GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		}
	}
	
	
	public void activateSurface() {
		active = true;
		if (fboHandle==-1) bindFrameBuffer();
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboHandle);
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glPushAttrib(GL11.GL_VIEWPORT_BIT);
		
		GL11.glMatrixMode(GL11.GL_PROJECTION);
	    GL11.glPushMatrix();
	    GL11.glLoadIdentity();
	    GL11.glOrtho(0, width, height, 0, 0, 1);
	    //GL11.glTranslatef(-1.0f, -1.0f, 0);
	    GL11.glViewport(0, 0, width, height);
	}
	
	public void deactivateSurface() {
		active=false;
		GL11.glPopMatrix();
		GL11.glPopAttrib();
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}
	
	public void paint(int x, int y, int w, int h) {
		if (active) deactivateSurface(); //We cannot be both a source and a target at the same time.
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		//GL11.glDisable(GL11.GL_COLOR);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, handle);
		GL11.glColor3d(1, 1, 1);
		//Draw a textured Quad with appropriate coords
		GL11.glBegin(GL11.GL_QUADS);
			GL11.glTexCoord2d(0.0, 1.0);
			GL11.glVertex2i(x, y);
			GL11.glTexCoord2d(1.0, 1.0);
			GL11.glVertex2i(x+w, y);
			GL11.glTexCoord2d(1.0, 0.0);
			GL11.glVertex2i(x+w, y+h);
			GL11.glTexCoord2d(0.0, 0.0);
			GL11.glVertex2i(x, y+h);
		GL11.glEnd();
	}
	
	public void bindFrameBuffer() {
		if (handle!=-1 & fboHandle!=-1) return; //Both handles are initialized. We're clearly already intialized.
		
		//One or both handle is invalid; regenerate the FBO.
		
		//Setup and bind the texture
		handle = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, handle);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8 , width, height, // need screen dimension
		    0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer)null);
		//UNBIND the texture so we can attach a FBO to it
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		//Create the Frame Buffer Object
		fboHandle = GL30.glGenFramebuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboHandle);
		//Attach the texture to the object
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, handle, 0);
		 
		//Create the RenderBuffer
		stencilHandle = GL30.glGenRenderbuffers();
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, stencilHandle);
		GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH24_STENCIL8, width, height);
		GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, stencilHandle);
		GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, stencilHandle);
		
		//Check status to make sure everything turned out okay.
		if ( !this.isReady() ) System.out.println("FATAL: Frame buffer could not be created!");

		//UNBIND the frame buffer so we can use *either* the texture *or* the FBO, whichever we need next.
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,0);
	}
	
	public boolean isReady() {
		int status2 = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
		return status2==GL30.GL_FRAMEBUFFER_COMPLETE;
	}
}
