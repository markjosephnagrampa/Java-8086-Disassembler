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
		ArrayList<String> toConvertData = new ArrayList<String>();
		try {
			Scanner input = new Scanner (System.in);
			System.out.println("Enter .asm filename: ");
			filename = input.nextLine();
			BufferedReader inputStream = new BufferedReader(new FileReader(filename));
			String line = "";
			while ((line = inputStream.readLine()) != null) {
				String tolowerLine = line.toLowerCase();
				if(tolowerLine.contains(" db ")||tolowerLine.contains(" dw ")||tolowerLine.contains(" dd ")){
					toConvertData.add(line);
				}
			}
			inputStream.close();
			convertToJava(toConvertData, filename);
		}
		
		catch (FileNotFoundException e) {
			System.err.println("File not found.");
		}
		catch (IOException e) {
			System.err.println("Error in filestream.");
		}
	} //end function
	
	public void convertToJava(ArrayList<String> toConvert, String filename) throws IOException {
		Disassembler disass = new Disassembler();
		ArrayList<String> varnameArray = new ArrayList<String>();
		ArrayList<String> vardataArray = new ArrayList<String>();
		
		// ah 0 al 1, bh 2 bl 3, ch 4 cl 5, dh 6 dl 7
		// registers and corresponding indexes
		String[] registersArrayDB = new String[8];
		// ax 0, bx 1, cx 2, dx 3
		// registers and corresponding indexes
		String[] registersArrayDW = new String[4];
		
		FileInputStream inputStream = new FileInputStream (filename);
		//gets the new filename for class
		String newFilename = "";	   
		newFilename = disass.getNewFilename(filename);
		
		String outputToFile = "import java.io.*; \n\npublic class " + newFilename + "{ \n";
			   outputToFile+= "\tpublic static void main(String[]args){\n";
		
		Scanner input = new Scanner(new File(filename));
		PrintWriter outputStream = null;  
		
		int ctr = 0;
		//Converts .data variables to variables in Java. 
		//int and String. No arrays supported.
		while(ctr < toConvert.size()){
			int g = 0;
			int l = 0;
			String currLine = toConvert.get(ctr);
			System.out.println(currLine);
			String currLineL = currLine.toLowerCase();
			// gets the variable name and the variable data
			// loop breaks the line into varname and vardata
			for (int i = 0; i < currLineL.length(); i++) {
				char curr = currLineL.charAt(i);
				if(curr == ' '){
					curr = currLineL.charAt(i+1);
					if (curr == 'd') {
						char curr2 = currLineL.charAt(i+2);
						if (curr2 == 'b' || curr2 == 'w' || curr2 == 'd'){
							curr2 = currLineL.charAt(i+3);
							if(curr2 == ' '){
								g = i;
								l = i+3;
								break;
							}
						}
					}
				}				
			}
			String varname = currLine.substring(0, g).trim();
			String vardata = currLine.substring(l, currLine.length()).trim();
			
			vardata = fixData(vardata);
			
			varnameArray.add(varname.trim());
			vardataArray.add(vardata.trim());
			ctr++;
		}
		
		//outputs variables to the file
		int ctrVar = 0;
		while(ctrVar < varnameArray.size()&& ctrVar < vardataArray.size()){
			//checks if the data is integer or not.
			if(disass.isInteger(vardataArray.get(ctrVar))){
				outputToFile += "\t\tint "+varnameArray.get(ctrVar)+" = "+vardataArray.get(ctrVar)+";\n";
			}else{
				outputToFile += "\t\tString "+varnameArray.get(ctrVar)+" = \""+vardataArray.get(ctrVar)+"\";\n";
			}
			ctrVar++;
		}
	}
	
	//fixes variable data from .data
	//removes single or double quotes and the $ terminator
	public String fixData(String vardata){
		//booleans check if data enclosed in single or double quotes, or no quotes at all (ex. int)
		boolean pairSingle = false;
		boolean pairDouble = false;
		boolean inQuotes = false;
		// checks if the data is enclosed in ' or "
		// this only supports variables, not arrays like dup or with multiple commas
		if(vardata.charAt(0)=='\''){
			pairSingle = true;
			inQuotes=true;
		}else if(vardata.charAt(0)=='"'){
			pairDouble = true;
			inQuotes=true;
		}
		int end = 0;
		vardata = vardata.substring(1, vardata.length());
		// data is enclosed in '
		if(pairSingle&&inQuotes){
			for (int i = 0; i < vardata.length(); i++) {
				char currC = vardata.charAt(i);
				if (currC == '\''){
					end = i;
					break;
				}
		}
		vardata = vardata.substring(0, end);
		
		}
		// data is enclosed in "
		else if(pairDouble&&inQuotes){
			for (int i = 0; i < vardata.length(); i++) {
				char currC = vardata.charAt(i);
				if (currC == '"'){
					end = i;
					break;
				}
			}
			vardata = vardata.substring(0, end);
		}
					
		//removes the $ terminator if still not removed (ex. "String $" or 'String $')
		if(vardata.contains("$")){
			vardata.replace('$', ' ');
		}
		return vardata;
	}
}
