import java.util.Scanner;

public class DisassemblerDriver {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Disassembler disassemble = new Disassembler();
		Scanner input = new Scanner(System.in);
		int choice = 0;
		
		do {
			System.out.println("MENU: \n 1. Assembly to Java \n 2. Java to Assembly \n 3. Quit");
			System.out.println("Enter your choice:");
			choice = input.nextInt();
			
			switch (choice) {
			case 1: disassemble.toJava(); break;
			case 2: disassemble.toAssembly(); break;
			case 3: break;
			
			}
			
		} while (choice != 3);
		
		System.out.println("Thank you for using the program. ouo");
		
	}

}
