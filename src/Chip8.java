import java.awt.Dimension;
import java.awt.Graphics;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Random;

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
				(byte) 0xF0, (byte) 0x10, (byte) 0xF0, (byte) 0x10, (byte) 0xF0,
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
		
		double timers = 1000000000.0/60.0, clock = 1000000000.0/1000.0;
		double t0 = 0, t1 = 0, lastTime = System.nanoTime();
		
		while(true) {
			
			double time = System.nanoTime();
			
			t0 += time-lastTime;
			t1 += time-lastTime;
			
			while(t0>timers) {
				update();
				t0-=timers;
			}
			
			while(t1>clock) {
				emulateCycle();
				t1-=timers;
			}
			
			lastTime = time;
			
		}
		
	}
	
	int graphicsOffset = 0;
	
	private void update() {
		
		if(graphicsChanged && ++graphicsOffset==2) {
			graphicsOffset = 0;
			repaint();
		}
		
		if(ST != 0)ST--;
		
		if(DT != 0)DT--;
		
		
	}
	
	private void emulateCycle() {
		
		
		short opcode = (short)((unsigned(RAM[PC]) << 8) | unsigned(RAM[PC+1]));
		PC+=2;
		//System.out.println(unsigned(PC));
		int nnn = opcode&0xFFF;
		int n = opcode&0xF;
		int x = (opcode&0xF00)>>8;
		int y = (opcode&0xF0)>>4;
		int kk = opcode&0xFF;
		
		switch((opcode&0xF000)>>12) {
		case 0x0:
			if(nnn == 0xE0) {
				//clear display
				System.out.println("clear");
				for(long l:display)l = 0;
			}
			else if(nnn == 0xEE) {
				//return from subroutine
				PC = stack[unsigned(--SP)];
			}
			else PC = (short)nnn;//jump to addr - not used
			break;
		case 0x1:
			//jump to nnn
			PC = (short)nnn;
			break;
		case 0x2:
			//call subroutine nnn
			stack[unsigned(SP++)] = PC;
			PC = (short) nnn;
			break;
		case 0x3:
			//skip instruction if Vx == kk
			if(unsigned(registers[x]) == kk)PC+=2;
			break;
		case 0x4:
			//skip if Vx != kk
			if(unsigned(registers[x]) != kk)PC+=2;
			break;
		case 0x5:
			//skip if Vx == Vy
			if(registers[x] == registers[y])PC+=2;
			break;
		case 0x6:
			//set Vx = kk
			registers[x] = (byte)kk;
			break;
		case 0x7:
			//set Vx = Vx + kk
			registers[x] += kk;
			break;
		case 0x8:
			switch(n) {
			case 0x0:
				//set Vx = Vy
				registers[x] = registers[y];
				break;
			case 0x1:
				//set Vx = Vx or Vy
				registers[x] |= registers[y];
				break;
			case 0x2:
				//use and
				registers[x] &= registers[y];
				break;
			case 0x3:
				//use xor
				registers[x] ^= registers[y];
				break;
			case 0x4:
				//vx = vx+vy, vf = carry
				registers[x] = (byte)(unsigned(registers[x]) + unsigned(registers[y]));
				registers[0xF] = (byte)((unsigned(registers[x]) + unsigned(registers[y]) >= 1<<7)?1:0);
				break;
			case 0x5:
				//vx = vx - vy, vf = not borrow
				registers[0xF] = (byte)(unsigned(registers[x]) > unsigned(registers[y])?1:0);
				registers[x] = (byte)(unsigned(registers[x]) - unsigned(registers[y]));
				break;
			case 0x6:
				//vx = vx>>1, vf = underflow
				registers[0xF] = (byte)((unsigned(registers[x]) % 2 == 1)?1:0);
				registers[x] = (byte)(unsigned(registers[x]) >> 1);
				break;
			case 0x7:
				//vx = vy - vx, vf = not borrow
				registers[0xF] = (byte)(unsigned(registers[x]) < unsigned(registers[y])?1:0);
				registers[x] = (byte)(unsigned(registers[y]) - unsigned(registers[x]));
				break;
			case 0xE:
				//vx = vx<<1, vf = overflow
				registers[0xF] = (byte)((unsigned(registers[x]) / 128 == 1)?1:0);
				registers[x] = (byte)(unsigned(registers[x]) << 1);
				break;
			default:
				System.err.println("Invalid Opcode: " + opcode);
				System.exit(1);
			}
			break;
		case 0x9:
			//skip if vx != vy
			if(registers[x] != registers[y])PC+=2;
			break;
		case 0xA:
			//I = nnn
			I = (short)nnn;
			break;
		case 0xB:
			//PC = nnn + V0
			PC = (short)(nnn + unsigned(registers[0]));
			break;
		case 0xC:
			// vx = random byte & kk;
			Random r = new Random();
			registers[x] = (byte)(r.nextInt()&kk);
			break;
		case 0xD:
			//draw stuff;
			registers[0xF] = 0;
			for(int i = 0; i < n; i++) {
				int cy = i + unsigned(registers[y]);
				if(cy > 31)cy-=32;
				int cx = 64-(unsigned(registers[x])+8);
				
				BigInteger start = BigInteger.valueOf(display[cy]);
				BigInteger mod = BigInteger.valueOf(unsigned(RAM[I+i]));
				
				mod = mod.shiftLeft(cx);
				
				if(start.compareTo(BigInteger.valueOf(0)) < 0)start = start.add(new BigInteger("18446744073709551616"));
				
				if((start.and(mod)).compareTo(BigInteger.valueOf(0)) != 0)registers[0xF] = 1;
				display[cy] = start.xor(mod).longValue();
			}
			graphicsChanged = true;
			break;
		case 0xE:
			if(kk == 0x9E) {
				//skip if key Vx is pressed
				if((KEYBOARD & (1<<x)) == 1)PC+=2;
			}else if(kk == 0xA1) {
				//skip if key Vx is not pressed
				if((KEYBOARD & (1<<x)) == 0)PC+=2;
			}else {
				System.err.println("Invalid Opcode: " + opcode);
				System.exit(1);
			}
			break;
		case 0xF:
			switch (kk) {
			case 0x07:
				//Vx = delay timer
				registers[x] = DT;
				break;
			case 0x0A:
				// wait for key, put in vx
				short before = KEYBOARD;
				while(!keyboardChanged) {}
				registers[x] = (byte)(Math.log((before & (before ^ KEYBOARD)))/Math.log(2));
				break;
			case 0x15:
				// set delay timer = vx
				DT = registers[x];
				break;
			case 0x18:
				// sound timer = vx
				ST = registers[x];
				break;
			case 0x1E:
				// i += Vx
				I += registers[x];
				break;
			case 0x29:
				// i = location for digit vx
				I = (short)(unsigned(registers[x])*5);
				break;
			case 0x33:
				// store unsigned decimal representation of vx in i, i+1, i+2
				RAM[I] = (byte)(unsigned(registers[x]) / 100);
				RAM[I+1] = (byte)((unsigned(registers[x]) / 10)%10);
				RAM[I+2] = (byte)(unsigned(registers[x])%10);
				break;
			case 0x55:
				//store v0 through vx in memory starting at i
				for(int i = 0; i <= x; i++)RAM[I+i] = registers[i];
				break;
			case 0x65:
				//read from i into v0 through vx
				for(int i = 0; i <= x; i++)registers[i] = RAM[I+i];
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

		for (int j = 0; j < 32; j++) {
			BigInteger t = BigInteger.valueOf(display[j]);
			for (int i = 63; i >= 0; i--) {
				
				if (!t.testBit(63-i)) {
					g.fillRect(i * (screenWidth / 64), j * (screenHeight / 32), screenWidth / 64, screenHeight / 32);
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
