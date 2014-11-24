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
		ArrayList<String> toConvert = new ArrayList<String>();

		try {
			
			Scanner input = new Scanner (System.in);
			System.out.println("Enter .java filename: ");
			filename = input.nextLine();
			BufferedReader inputStream = new BufferedReader(new FileReader(filename));
			//add stuff to do here
			String line = "";
			
			while ((line = inputStream.readLine()) != null) {
				System.out.println(line);
				if (line.contains(".nextLine()")); {
					toConvert.add(line);
				}
				
			}
			
			inputStream.close();
			convertToAssembly(toConvert, filename);
			
		}
		
		catch (FileNotFoundException e) {
			System.err.println("File not found.");
		} 
		catch (IOException e) {
			System.err.println("Error in filestream.");
		}
		
	} //end function
	
	public void convertToAssembly (ArrayList<String> toConvert, String filename) throws IOException {
		
		ArrayList<String> dataArray = new ArrayList<String>();
		ArrayList<String> codeArray = new ArrayList<String>();
		FileInputStream inputStream = new FileInputStream (filename);
		
		int counter = 1;
		String currLine = "";
		String msgString = "";
		String newFilename = "";
		String title = "title";
		String model = ".model small";
		String stack = ".stack 100h";
		Scanner input = new Scanner(new File(filename));
		BufferedReader output = new BufferedReader(new InputStreamReader(System.in));
		PrintWriter outputStream = null;
				
		newFilename = getNewFilename(filename);
		title = title + " " + newFilename;
		
		dataArray.add(".data");
		codeArray.add(".code");
		codeArray.add("\t main proc");
		codeArray.add("\t mov ax, @data \n \t mov ds, ax");
		
		//add data variables here
		while (input.hasNextLine()) {
			currLine = input.nextLine();
			
			if (currLine.contains("System.out")) {
				msgString = getMsgString (currLine);
				dataArray.add("\t msg" + counter + " db " +  '\''  + msgString + "\'," + "'$'");	
				
				codeArray.add("\t lea dx, " + "msg" + counter);
				codeArray.add("\t mov ah, 09h");
				codeArray.add("\t int 21h");
				counter++;
			}
			
		}
		
		
		codeArray.add("\t mov ax, 4c00h \n \t int 21h");
		codeArray.add("\t main endp \n \t end main");
		
		
		try {
			outputStream =  new PrintWriter(new FileOutputStream(newFilename + ".asm"));
			outputStream.println(title);
			outputStream.println(model);
			outputStream.println(stack);
			for (int e = 0; e < dataArray.size(); e++) {
				outputStream.println(dataArray.get(e));
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
	
	public String getMsgString (String currLine) {
		
		int g = 0;
		int h = 0;
		String newLine = "";
		
		for (int i = 0; i < currLine.length(); i++) {
			char curr = currLine.charAt(i);
			if (curr == '"') {
				g = i;
				break;
			}
			
		}
		
		for (int j = 0; j < currLine.length(); j++) {
			char curr = currLine.charAt(j);
			if (curr == '"' && j != g) {
				h = j;
				break;
			}
		}
		
		g++;
		newLine = currLine.substring(g, h);
		return newLine;
		
	} //end function
	
	public String getNewFilename(String filename) {
		
		int d = 0;
		
		for (int c = 0; c < filename.length(); c++) {
			char curr = filename.charAt(c);
			if (curr == '.') {
				d = c;
				break;
			}
		}
		
		System.out.println(d);
		filename = filename.substring(0, d);
		System.out.println(filename);
		return filename;
		
	} //end function
	
	
} //end class
