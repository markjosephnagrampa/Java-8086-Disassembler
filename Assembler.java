import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;

public class Assembler {
	public static void main(String[] args){
		Assembler ass = new Assembler();
		ass.toJava();
	}
	
	public void toJava() {
		String filename = "";
		ArrayList<String> toConvert = new ArrayList<String>();
		try {
			Scanner input = new Scanner (System.in);
			System.out.println("Enter .asm filename: ");
			filename = input.nextLine();
			BufferedReader inputStream = new BufferedReader(new FileReader(filename));
			String line = "";
			while ((line = inputStream.readLine()) != null) {
				System.out.println(line);
				toConvert.add(line);
			}
			inputStream.close();
		}
		
		catch (FileNotFoundException e) {
			System.err.println("File not found.");
		}
		catch (IOException e) {
			System.err.println("Error in filestream.");
		}
	} //end function
	
	
}
