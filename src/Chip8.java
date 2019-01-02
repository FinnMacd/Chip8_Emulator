import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class Chip8 implements Runnable{
	
	private byte[] RAM = new byte[4096];
	private byte[] registers = new byte[16];
	private short[] stack = new short[16];
	private long[] display = new long[32];
	
	private short PC, I, KEYBOARD;
	
	private byte DT, sound, SP;
	
	private Thread chipThread;
	
	public Chip8() {
		
		initSprites();
		
		chipThread = new Thread(this);
		
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
		
		
	}
	
	
}
