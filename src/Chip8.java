import java.awt.Dimension;
import java.awt.Graphics;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Chip8 extends JPanel implements Runnable{
	
	//emulator
	
	private byte[] RAM = new byte[4096];
	private byte[] registers = new byte[16];
	private short[] stack = new short[16];
	private long[] display = new long[32];
	
	private short PC, I, KEYBOARD;
	
	private byte DT, ST, SP;
	
	//mechanics
	
	private Thread chipThread;
	
	boolean graphicsChanged, keyboardChanged;
	
	private JFrame frame;
	private int screenWidth, screenHeight;
	
	public Chip8(int width, int height) {
		
		setPreferredSize(new Dimension(width, height));
		requestFocus();
		
		screenWidth = width;
		screenHeight = height;
		
		initFrame();
		initSprites();
		
		chipThread = new Thread(this);
		
	}
	
	public void start() {
		PC = 512;
		I = 0;
		SP = 0;
		DT = 0;
		ST = 0;
		KEYBOARD = 0;
		
		display[1] = 1;
		
		chipThread.start();
	}
	
	public void loadROM(String path) {
		
		int i = 512;
		
		try {
			File file = new File("./Pong (alt).ch8");
			InputStream in = new BufferedInputStream(new FileInputStream(file));
			
			byte[] program = new byte[(int)file.length()];
			
			int totalBytesRead = 0;
			
			while(totalBytesRead < program.length) {
				int bytesRead = in.read(program, totalBytesRead, program.length - totalBytesRead);
				
				if(bytesRead > 0)totalBytesRead += bytesRead;
				
			}
			
			for(;i<512+file.length();i++) {
				RAM[i] = program[i-512];
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void initSprites() {
		
		byte[] sprites = new byte[] {
				(byte) 0xF0, (byte) 0x90, (byte) 0x90, (byte) 0x90, (byte) 0xF0,
				(byte) 0x20, (byte) 0x60, (byte) 0x20, (byte) 0x20, (byte) 0x70,
				(byte) 0xF0, (byte) 0x10, (byte) 0xF0, (byte) 0x80, (byte) 0xF0,
				(byte) 0x90, (byte) 0x10, (byte) 0xF0, (byte) 0x10, (byte) 0xF0,
				(byte) 0xF0, (byte) 0x90, (byte) 0xF0, (byte) 0x10, (byte) 0x10,
				(byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x10, (byte) 0xF0,
				(byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x90, (byte) 0xF0,
				(byte) 0xF0, (byte) 0x10, (byte) 0x20, (byte) 0x40, (byte) 0x40,
				(byte) 0xF0, (byte) 0x90, (byte) 0xF0, (byte) 0x90, (byte) 0xF0,
				(byte) 0xF0, (byte) 0x90, (byte) 0xF0, (byte) 0x10, (byte) 0xF0,
				(byte) 0xE0, (byte) 0x90, (byte) 0xE0, (byte) 0x90, (byte) 0xE0,
				(byte) 0xF0, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0xF0,
				(byte) 0xE0, (byte) 0x90, (byte) 0x90, (byte) 0x90, (byte) 0xE0,
				(byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x80, (byte) 0xF0,
				(byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x80, (byte) 0x80,
		};
		
		for(int i = 0; i < sprites.length; i++)RAM[i] = sprites[i];
		
	}
	
	private void initFrame() {
		
		frame = new JFrame("Chip8 Emulator");
		
		frame.setResizable(false);
		frame.add(this);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		
		frame.setVisible(true);
		
	}
	
	public void run() {
		
		double NS = 1000.0/60.0;
		double t = 0, lastTime = System.currentTimeMillis();
		
		while(true) {
			
			double time = System.currentTimeMillis();
			
			t += time-lastTime;
			
			while(t>NS) {
				update();
				t-=NS;
			}
			
			lastTime = time;
			
			
			
		}
		
		
	}
	
	private void update() {
		
		emulateCycle();
		
		if(graphicsChanged)repaint();
		
		if(ST != 0)ST--;
		
		if(DT != 0)DT--;
		
		
	}
	
	private void emulateCycle() {
		
		display[0]++;
		display[1]*=2;
		graphicsChanged = true;
		
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		
		for(int i = 0; i < 64; i++) {
			for(int j = 0; j < 32; j++) {
				if((display[j] & (long)((long)1<<(63-i))) != 0) {
					g.fillRect(i*(screenWidth/64), j*(screenHeight/32), screenWidth/64, screenHeight/32);
				}
			}
		}
		
		graphicsChanged = false;
		
	}
	
	
}
