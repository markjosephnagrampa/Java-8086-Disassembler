import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;

public class Disassembler {
	
	public static void main(String[] args){
		Disassembler d = new Disassembler();
		d.toAssembly();
	}

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
		String display =
				"\t display proc\n";                           
				display+= "\t\t mov bx, 10\n";                
				display+= "\t\t mov dx, 0000h \n";                
				display+= "\t\t mov cx, 0000h \n";                   

				display+= "\t\t Dloop1: \n";
				display+= "\t\t\t mov dx, 0000h \n";                   
				display+= "\t\t\t div bx \n";                          
				display+= "\t\t\t push dx \n";                         
				display+= "\t\t\t inc cx \n";                         
				display+= "\t\t\t cmp ax, 0 \n";                 
				display+= "\t\t\t jne Dloop1 \n";         

				display+= "\t\t Dloop2: \n";
				display+= "\t\t\t pop dx \n";                     
				display+= "\t\t\t add dx, 30h \n";            
				display+= "\t\t\t mov ah, 02h \n";                            
				display+= "\t\t\t int 21h \n";                    
				display+= "\t\t\t loop Dloop2 \n";             
				display+= "\t\t ret \n";     
				display+= "\t display endp \n";

		String  newLine = "\t\t mov dl, 0ah \n";
				newLine +="\t\t mov ah, 02h \n";
				newLine += "\t\t int 21h \n";
		String intString = "";
		String newFilename = "";
		boolean includeDisplay = false;
		String title = "title";
		String model = ".model small";
		String stack = ".stack 100h";
		Scanner input = new Scanner(new File(filename));
		BufferedReader output = new BufferedReader(new InputStreamReader(System.in));
		PrintWriter outputStream = null;
				
		newFilename = getNewFilename(filename);
		title = title + " " + newFilename;
		
		dataArray.add(".data");
		
		//add data variables here
		while (input.hasNextLine()) {
			currLine = input.nextLine();
			
			//Baklas Structure for if,else for,do,while,do while
			
			// 1. Print Message Strings
			// Cases Handled: System.out.println("Hello world!");
			if (currLine.contains("System.out")&&currLine.contains("\"")) {
				msgString = getMsgString (currLine);
				dataArray.add("\t msg" + counter + " db " +  '\''  + msgString + "\'," + "'$'");	
				
				codeArray.add("\t\t lea dx, " + "msg" + counter);
				codeArray.add("\t\t mov ah, 09h");
				codeArray.add("\t\t int 21h");
				codeArray.add(newLine);
				counter++;
			}
			// 1.2 Print Numbers
			// Cases Handled: int a = 255; System.out.println(a);
			else if (currLine.contains("System.out")&&!currLine.contains("\"")) {
				includeDisplay=true;
				String numString = getNumString(currLine);
				codeArray.add("\t\t mov ax, "+numString);
				codeArray.add("\t\t call display");
				codeArray.add(newLine);
			}
			// 2. Integer Variables (Declaration) int a=12; int b;
			else if (currLine.contains("int")) {
				intString = getIntString(currLine);
				System.out.println(intString);
				dataArray.add(intString);
			}
			// 3. Integer Variables (Assignment) 
			//Cases Handled: x = 3 + 2; x = 3 - 2; x++; x--; x = 0;
			else if (currLine.contains("=")||
				currLine.contains("++")||currLine.contains("--")) {
				
				intString = getAssignIntString(currLine);
				codeArray.add(intString);
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
			
			outputStream.println(".code");
			if(includeDisplay){outputStream.println(display);}
			outputStream.println("\t main proc");
			outputStream.println("\t mov ax, @data \n \t mov ds, ax");
			
			
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
	// 1. Function for Message Strings
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
	
	// 1.2 Function for Printing Variables
		public String getNumString(String currLine){
			String newLine="";
			currLine=currLine.trim();
			int a = currLine.indexOf("(");
			int b = currLine.indexOf(")");
			newLine = currLine.substring(a+1,b);
			return newLine;
		}
		
	// 2. Function for Int Variables
		public String getIntString(String currLine){
			String newLine="";
			currLine=currLine.trim();
			currLine=currLine.replaceFirst("int", "");
			currLine=currLine.replace(" ", "");
			// First Case: a=0; or a=255;
			if(currLine.contains("=")){
				int a = currLine.indexOf("=");
				int end = currLine.indexOf(";");
				int value = Integer.parseInt(currLine.substring(a+1,end));
				String varname = currLine.substring(0,a);
				newLine ="\t "+ varname + " dw "+ value;
				
			}
			// Second Case: a; longvarname;
			else{
				int end = currLine.indexOf(";");
				String varname = currLine.substring(0,end);
				newLine ="\t "+ varname + " dw ?";
			}
			return newLine;
		}// end function
	
	// 3. Function for Int Variables (Mid-code Assignment)
		public String getAssignIntString(String currLine){
			System.out.println(currLine);
			String newLine="";
			currLine=currLine.trim();
			currLine=currLine.replace(" ","");
			// x++;
			if(currLine.contains("++")){
				int a = currLine.indexOf("+");
				String varname = currLine.substring(0,a);
				newLine ="\t\t inc "+ varname;
			}
			// x--;
			else if(currLine.contains("--")){
				int a = currLine.indexOf("-");
				String varname = currLine.substring(0,a);
				newLine ="\t\t dec "+ varname;
			}
			else{
				String fOp="";
				String sOp="";
				int a = currLine.indexOf("=");
				String varname = currLine.substring(0,a);
				// x=3+2;
				if(currLine.contains("+")){
					int findex = currLine.indexOf("+");
					fOp = currLine.substring(a+1,findex);
					int end = currLine.indexOf(";");
					sOp = currLine.substring(findex+1,end);
					newLine = "\t\t mov "+varname+","+fOp+"\n";
					newLine += "\t\t add "+varname+","+sOp;
				}
				// x = 3 - 2;
				else if(currLine.contains("-")){
					int findex = currLine.indexOf("-");
					fOp = currLine.substring(a+1,findex);
					int end = currLine.indexOf(";");
					sOp = currLine.substring(findex+1,end);
					newLine = "\t\t mov "+varname+","+fOp+"\n";
					newLine += "\t\t sub "+varname+","+sOp;
				}
				// x = 0;
				else{
					int eq = currLine.indexOf("=");
					int end = currLine.indexOf(";");
					sOp = currLine.substring(eq+1,end);
					newLine = "\t\t mov "+varname+","+sOp;
				}
			}
			return newLine;
		}//end function
	
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
		return filename;
		
	} //end function
	
	
} //end class
