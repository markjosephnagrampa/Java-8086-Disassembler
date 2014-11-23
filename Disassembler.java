import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;

public class Disassembler {

	public void toJava() {
		String filename = "";

		try {
			
			Scanner input = new Scanner (System.in);
			System.out.println("Enter .asm filename: ");
			filename = input.nextLine();
			BufferedReader inputStream = new BufferedReader(new FileReader(filename));
			//add stuff to do here
			String line = "";
			
			while ((line = inputStream.readLine()) != null) {
				System.out.println(line);
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

	public void toAssembly() {
		
		String filename = "";

		try {
			
			Scanner input = new Scanner (System.in);
			System.out.println("Enter .java filename: ");
			filename = input.nextLine();
			BufferedReader inputStream = new BufferedReader(new FileReader(filename));
			//add stuff to do here
			String line = "";
			
			while ((line = inputStream.readLine()) != null) {
				System.out.println(line);
			}
			
			inputStream.close();
			convertToAssembly(filename);
			
		}
		
		catch (FileNotFoundException e) {
			System.err.println("File not found.");
		} 
		catch (IOException e) {
			System.err.println("Error in filestream.");
		}
		
	} //end function
	
	public void convertToAssembly (String filename) throws IOException {
		
		int d = 0;
		ArrayList<String> dataArray = new ArrayList<String>();
		ArrayList<String> codeArray = new ArrayList<String>();
		
		
		String newFilename = "";
		String title = "title";
		String model = ".model small";
		String stack = ".stack 100h";
		BufferedReader output = new BufferedReader(new InputStreamReader(System.in));
		PrintWriter outputStream = null;
		
		for (int c = 0; c < filename.length(); c++) {
			char curr = filename.charAt(c);
			if (curr == '.') {
				d = c;
				break;
			}
		}
		
		newFilename = filename.substring(0, d);
		title = title + " " + newFilename;
		
		dataArray.add(".data");
		codeArray.add(".code");
		codeArray.add("\t main proc");
		codeArray.add("\t mov ax, @data \n \t mov ds, ax");
		codeArray.add("\t mov ax, 4c00h \n \t int 21h");
		codeArray.add("\t main endp \n \t end main");
		
		
		try {
			outputStream =  new PrintWriter(new FileOutputStream(newFilename + ".asm"));
			outputStream.println(title);
			outputStream.println(model);
			outputStream.println(stack);
			for (int e = 0; e < dataArray.size(); e++) {
				outputStream.println(dataArray.get(0));
			}
			
			for (int f = 0; f < codeArray.size(); f++) {
				outputStream.println(codeArray.get(f));
			}
			outputStream.close();
			System.out.println("successful");
		}
		
		catch (FileNotFoundException e) {
			System.err.println("No file found.");
			System.exit(0);
		}
	}
	
} //end class
