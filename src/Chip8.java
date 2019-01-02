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
		
		
		short opcode = (short)((unsigned(RAM[PC]) << 8) | unsigned(RAM[PC+1]));
		PC+=2;
		
		int nnn = opcode&0xFFF;
		int n = opcode&0xF;
		int x = (opcode&0xF00)>>8;
		int y = (opcode&0xF0)>>4;
		int kk = opcode&0xFF;
		
		switch((opcode&0xF000)>>12) {
		case 0x0:
			if(kk == 0xE0);//clear display
			else if(kk == 0xEE);//return from subroutine
			else ;//jump to addr - not used
			break;
		case 0x1:
			//jump to addr
			break;
		case 0x2:
			//call subroutine addr
			break;
		case 0x3:
			//skip instruction if Vx == kk
			break;
		case 0x4:
			//skip if Vxx != kk
			break;
		case 0x5:
			//skip if Vx == Vy
			break;
		case 0x6:
			//set Vx = kk
			break;
		case 0x7:
			//set Vx = Vx + kk
		case 0x8:
			switch(n) {
			case 0x0:
				//set Vx = Vy
				break;
			case 0x1:
				//set Vx = Vx or Vy
				break;
			case 0x2:
				//use and
				break;
			case 0x3:
				//use xor
				break;
			case 0x4:
				//vx = vx+vy, vf = carry
				break;
			case 0x5:
				//vx = vx - vy, vf = not borrow
				break;
			case 0x6:
				//vx = vx>>1, vf = underflow
				break;
			case 0x7:
				//vx = vy - vx, vf = not borrow
				break;
			case 0xE:
				//vx = vx<<1, vf = overflow
				break;
			default:
				System.err.println("Invalid Opcode: " + opcode);
				System.exit(1);
			}
			break;
		case 0x9:
			//skip if vx != vy
			break;
		case 0xA:
			//I = nnn
			break;
		case 0xB:
			//PC = nnn + V0
			break;
		case 0xC:
			// vx = random byte & kk;
			break;
		case 0xD:
			//draw stuff;
			break;
		case 0xE:
			if(kk == 0x9E) {
				//skip if key Vx is pressed
			}else if(kk == 0xA1) {
				//skip if key Vx is not pressed
			}else {
				System.err.println("Invalid Opcode: " + opcode);
				System.exit(1);
			}
			break;
		case 0xF:
			switch (kk) {
			case 0x07:
				//Vx = delay timer
				break;
			case 0x0A:
				// wait for key, put in vx
				break;
			case 0x15:
				// set delay timer = vx
				break;
			case 0x18:
				/// sound timer = vx
				break;
			case 0x1E:
				// i += Vx
				break;
			case 0x29:
				// i = location for digit vx
				break;
			case 0x33:
				// store unsigned decimal representation of vx in i, i+1, i+2
				break;
			case 0x55:
				//store v0 through vx in memory starting at i
				break;
			case 0x65:
				//read from i into v0 through vx
				break;
			default:
				System.err.println("Invalid Opcode: " + opcode);
				System.exit(1);
			}
			break;
		default:
			System.err.println("Invalid Opcode: " + opcode);
			System.exit(1);
		}
		
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
	
	private int unsigned(short a) {
		if(a<0)return ((int)1<<16) + a;
		return a;
	}
	
	private int unsigned(byte a) {
		if(a<0)return ((int)1<<8) + a;
		return a;
	}
	
}
