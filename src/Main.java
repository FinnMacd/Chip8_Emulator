
public class Main {

	public static void main(String[] args) {
		
		Chip8 chip = new Chip8(640*2, 320*2);
		
		chip.loadROM("./Pong (alt).ch8");
		
		chip.start();
		
	}

}
