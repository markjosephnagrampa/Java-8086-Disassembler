import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Disassembler {
	
	//Disassembler Variables
	static ArrayList<String> toConvert = new ArrayList<String>();
	static ArrayList<String> dataArray = new ArrayList<String>();
	static ArrayList<String> codeArray = new ArrayList<String>();
	static ArrayList<String> userStr = new ArrayList<String>();
	static ArrayList<String> charStr = new ArrayList<String>();
	static int msgcounter=1;
	static int ifcount=0;
	static int ifelsecount=0;
	static int forcount=0;
	static int docount=0;
	static int whilecount=0;
	static boolean includeDisplay=false;
	
	public static void main(String[] args){
		Disassembler d = new Disassembler();
		d.toAssembly();
	}

	public void toAssembly() {
		
		String filename = "";

		try {
			
			Scanner input = new Scanner (System.in);
			System.out.println("Enter .java filename: ");
			filename = input.nextLine();
			BufferedReader inputStream = new BufferedReader(new FileReader(filename));
			
			// 1. Place Source Code in a Single String Variable
			
			String line = "";
			String sourceCode="";
			while ((line = inputStream.readLine()) != null) {
				line=line.trim();
				sourceCode+=line;
			}
			
			// 2. Clean Source Code String
				sourceCode.trim();
				sourceCode=sourceCode.replace(sourceCode.substring(0,1+sourceCode.indexOf("{")),"");
				sourceCode=sourceCode.substring(0,sourceCode.length()-2);
				sourceCode=sourceCode.replace("{","{;");
				sourceCode=sourceCode.replace("}",";};");
			// 3. Tokenize sourceCode by Blocks {,} or by ;
				
				StringTokenizer st = new StringTokenizer(sourceCode, ";");
				while (st.hasMoreTokens()) {
					String token = st.nextToken();
					
					//Special Tokenization for for line for(int i=0;i<10;i++){
					if(token.contains("for")){token+="{";}
					if((token.contains("++")||token.contains("--"))&&(token.contains("{")&&
						token.contains(")"))){
						token=token.replace("{", "");
					}
					System.out.println(token);
					toConvert.add(token);
			     }
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
		
		String display =
				"\tdisplay proc\n";                           
				display+= "\t\tmov bx, 10\n";                
				display+= "\t\tmov dx, 0000h \n";                
				display+= "\t\tmov cx, 0000h \n";                   

				display+= "\t\tDloop1: \n";
				display+= "\t\t\tmov dx, 0000h \n";                   
				display+= "\t\t\tdiv bx \n";                          
				display+= "\t\t\tpush dx \n";                         
				display+= "\t\t\tinc cx \n";                         
				display+= "\t\t\tcmp ax, 0 \n";                 
				display+= "\t\t\tjne Dloop1 \n";         

				display+= "\t\tDloop2: \n";
				display+= "\t\t\tpop dx \n";                     
				display+= "\t\t\tadd dx, 30h \n";            
				display+= "\t\t\tmov ah, 02h \n";                            
				display+= "\t\t\tint 21h \n";                    
				display+= "\t\t\tloop Dloop2 \n";             
				display+= "\t\tret \n";     
				display+= "\tdisplay endp \n";
		String newFilename = "";
		String title = "title";
		String model = ".model small";
		String stack = ".stack 100h";
		BufferedReader output = new BufferedReader(new InputStreamReader(System.in));
		PrintWriter outputStream = null;
		dataArray.add(".data");
		
		//add data variables here
		for(int i=0; i<toConvert.size();i++) {
			String currLine = toConvert.get(i);
			
			//Block Statements Handler (if,if-else,...)
			if(currLine.contains("{")){
				String bstate = currLine.substring(0,currLine.indexOf("{"));
				i=convertBToAssembly(i,bstate,1);
			}
			else{
				convertNBToAssembly(i,i+1,1);
			}
		}
		
		
		codeArray.add("\n\tmov ax, 4c00h \n \tint 21h");
		codeArray.add("\tmain endp \n \tend main");
		
		newFilename = getNewFilename(filename);
		title = title + " " + newFilename;
		
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
			outputStream.println("\tmain proc");
			outputStream.println("\tmov ax, @data \n \tmov ds, ax\n");
			
			
			for (int f = 0; f < codeArray.size(); f++) {
				outputStream.println(codeArray.get(f));
			}
			outputStream.close();
			
			//Print Contents of .asm file
			for(String item: dataArray){
				System.out.println(item);
			}
			
			for(String item: codeArray){
				System.out.println(item);
			}
			
			System.out.println("successful");
		}
		
		catch (FileNotFoundException e) {
			System.err.println("No file found.");
			System.exit(0);
		}
	}
	
	// Block Functions	
	
		// 1. Function for Converting Non-Block Statements
		public void convertNBToAssembly(int start, int end, int tabcount){
			
			//Place Tabs for .asm Code Readability
			String tab="";
			for(int j=0;j<tabcount;j++){tab+="\t";}
			String  newLine = tab+"mov dl, 0ah \n";
			newLine +=tab+"mov ah, 02h \n";
			newLine += tab+"int 21h";
			for(int i=start;i<end;i++){
				String currLine = toConvert.get(i);
				
				// A. Multi-Level Block Handling
				if(currLine.contains("{")){
					String bstate = currLine.substring(0,currLine.indexOf("{"));
					i=convertBToAssembly(i,bstate,tabcount);
				}
				
				// B. Line for Line Converter
				else{
					// 1. Print Message Strings
					// Cases Handled: System.out.println("Hello world!");
					if (currLine.contains("System.out")&&currLine.contains("\"")) {
						String msgString = getMsgString (currLine);
						dataArray.add("\tmsg" + msgcounter + " db " +  '\''  + msgString + "\'," + "'$'");	
									
						codeArray.add(tab+"lea dx, " + "msg" + msgcounter);
						codeArray.add(tab+"mov ah, 09h");
						codeArray.add(tab+"int 21h");
						if(currLine.contains("println")){
							codeArray.add(newLine);
						}
						msgcounter++;
					}
					// 1.2 Print Variables
					// Cases Handled: int a = 255; System.out.println(a);
					// String hello ="hello"; System.out.println (hello);
					// char c ='c'; System.out.println (c);
					else if (currLine.contains("System.out")&&!currLine.contains("\"")) {
						String varString = getVarString(currLine);
						if(checkStrVar(varString)){
							codeArray.add(tab+"lea dx, " + varString);
							codeArray.add(tab+"mov ah, 09h");
							codeArray.add(tab+"int 21h");
							if(currLine.contains("println")){codeArray.add(newLine);}
						}
						else if(checkCharVar(varString)){
							codeArray.add(tab+"mov dl, " + varString);
							codeArray.add(tab+"mov ah, 02h");
							codeArray.add(tab+"int 21h");
							if(currLine.contains("println")){codeArray.add(newLine);}
						}
						else{
							includeDisplay=true;
							codeArray.add(tab+"mov ax, "+varString);
							codeArray.add(tab+"call display");
							if(currLine.contains("println")){codeArray.add(newLine);}
						}
					}
					// 2. Integer Variables (Declaration) int a=12; int b;
					else if (currLine.contains("int")) {
						String intString = getICString(currLine,"int");
									
						dataArray.add(intString);
					}
					
					// 3. Store Message Strings
					// Cases Handled: String str="hello world";
					else if(currLine.contains("String")){
						String msgString = getMsgString (currLine);
						currLine = currLine.replace("String","");
						currLine = currLine.replace("static","");
						String varName = currLine.substring(0,currLine.indexOf("="));
						varName=varName.trim();
						dataArray.add("\t" + varName + " db " +  '\''  + msgString + "\'," + "'$'");
						
						userStr.add(varName);
					}
					
					// 4. Store Character Variables
					// Cases Handled: char c='c'; char c; char c,d,e;
					else if(currLine.contains("char")){
						
						String charString = getICString(currLine,"char");
						dataArray.add(charString);
					}
					
					// 5. Int Variables (Assignment) 
					//Cases Handled: x = 3 + 2; x = 3 - 2; x = 0;
					// c='c';
					else if (currLine.contains("=")
						||currLine.contains("++")||currLine.contains("--")) {
						getAssignICString(currLine,tabcount);
					}
					
				}
			}
			
		}
		
		// 2. Function for Converting Block Statements (If,If-Else,For,Do-While,While)
		public int convertBToAssembly(int start,String bstate, int tabcount){
			int i=start;
			if(bstate.contains("if")){
				// Apply Structure for If Code Only
				if(checkElse(i+1)==-1){
					int rend = findEnd(i+1);
					ifcount++;
					convertIf(i,rend,tabcount);
					i=rend;
				}
				// Apply Structure for If-Else Code
				else{
					int rend = findEnd(i+1);
					int elseStart = rend+1;
					int elseEnd = findEnd(elseStart+1);
					ifelsecount++;
					convertIfElse(i,rend,elseStart,elseEnd,tabcount);
					i=elseEnd;
					
				}
			}
			else if(bstate.contains("while")){
				int rend = findEnd(i+1);
				whilecount++;
				convertWhile(i,rend,tabcount);
				i=rend;
			}
			else if(bstate.contains("do")){
				int rend = findEnd(i+1);
				docount++;
				convertDoWhile(i,rend,tabcount);
				i=rend;
			}
			else if(bstate.contains("for")){
				int rend = findEnd(i+3);
				forcount++;
				convertFor(i,rend,tabcount);
				i=rend;
			}
			
			return i;
		}
		
	//Block Statement Parser Functions
		
	// 1. If Block Converter
	public void convertIf(int start, int end, int tabcount){
		String bstate=toConvert.get(start);
		String left="";
		String right="";
		String cond="";
		String jmp="";
		String cmp="";
		String label="";
		String tab="";
		// A. Add tabs for .asm readability
		for(int i=0;i<tabcount;i++){tab+="\t";}
		
		// B. Set jxx label, Tokenize Left/Right Expressions
		cond = setCond(bstate);
		left = setLeftExpr(bstate);
		right = setRightExpr(bstate);
		if(checkCharVar(right)){
			cmp= tab+"mov cl,"+right+"\n";
			cmp+= tab+"cmp "+left+",cl";
		}
		else if(!isInteger(right)&&!right.contains("'")){
			cmp= tab+"mov cx,"+right+"\n";
			cmp+= tab+"cmp "+left+",cx";
		}
		else{cmp = tab+"cmp "+left+","+right;}
		
		// C. Convert Block to asm Code
			// a. Set Label Names
			label =tab+ "endif"+ifcount+":";
			jmp =tab+ cond+" endif"+ifcount;
			
			
			// b. Store asm Code to ArrayList handler
			codeArray.add(cmp);
			codeArray.add(jmp);
			convertNBToAssembly(start+1,end,tabcount+1);
			codeArray.add(label);
	}
	
	// 2. If-Else Block Converter
	public void convertIfElse(int ifstart, int ifend, int elsestart, int elseend, int tabcount){
		String bstate=toConvert.get(ifstart);
		String left="";
		String right="";
		String cond="";
		String jmpElse="";
		String jmpIf="";
		String cmp="";
		String iflabel="";
		String elselabel="";
		String tab="";
		
		// A. Add tabs for .asm readability
		for(int i=0;i<tabcount;i++){tab+="\t";}
		
		// B. Set jxx label, Tokenize Left/Right Expressions
		cond = setCond(bstate);
		left = setLeftExpr(bstate);
		right = setRightExpr(bstate);
		
		if(checkCharVar(right)){
			cmp= tab+"mov cl,"+right+"\n";
			cmp+= tab+"cmp "+left+",cl";
		}
		else if(!isInteger(right)&&!right.contains("'")){
			cmp= tab+"mov cx,"+right+"\n";
			cmp+= tab+"cmp "+left+",cx";
		}
		else{cmp = tab+"cmp "+left+","+right;}
			
		// C. Convert block to asm code
			// a. Set Label Names
			iflabel=tab+"endifelse"+ifelsecount+":";
			elselabel=tab+"else_block"+ifelsecount+":";
			jmpIf=tab+"jmp endifelse"+ifelsecount;
			jmpElse =tab+ cond+" else_block"+ifelsecount;
			
			// b. Store asm Code to ArrayList handler
			codeArray.add(cmp);
			codeArray.add(jmpElse);
			convertNBToAssembly(ifstart+1,ifend,tabcount+1);
			codeArray.add(jmpIf);
			codeArray.add(elselabel);
			convertNBToAssembly(elsestart+1,elseend,tabcount+1);
			codeArray.add(iflabel);
		
		
	}
	
	// 3. While Block Converter
	public void convertWhile(int start, int end, int tabcount){
		String bstate=toConvert.get(start);
		String left="";
		String right="";
		String cond="";
		String whilelabel="";
		String endwhile="";
		String cmp="";
		String jxx="";
		String jmp="";
		String tab="";
		
		// A. Add tabs for .asm readability
		for(int j=0;j<tabcount;j++){tab+="\t";}
		
		// B. Set jxx label, Tokenize Left/Right Expressions
		cond = setCond(bstate);
		left = setLeftExpr(bstate);
		right = setRightExpr(bstate);
		
		if(checkCharVar(right)){
			cmp= tab+"mov cl,"+right+"\n";
			cmp+= tab+"cmp "+left+",cl";
		}
		else if(!isInteger(right)&&!right.contains("'")){
			cmp= tab+"mov cx,"+right+"\n";
			cmp+= tab+"cmp "+left+",cx";
		}
		else{cmp = tab+"cmp "+left+","+right;}
		
		// C. Convert Block to asm Code
			// a. Set Label Names
			whilelabel = tab+"whilelabel"+whilecount+":";
			jxx = tab+cond+" endwhile"+whilecount;
			jmp = tab+"jmp whilelabel"+whilecount;
			endwhile = tab+"endwhile"+whilecount+":";
			
			// b. Store asm Code to ArrayList handler
			codeArray.add(whilelabel);
			codeArray.add(cmp);
			codeArray.add(jxx);
			convertNBToAssembly(start+1,end,tabcount+1);
			codeArray.add(jmp);
			codeArray.add(endwhile);			
		
	}
	
	// 4. Do-While Block Converter
	public void convertDoWhile(int start, int end, int tabcount){
		String bstate=getWhile(start+1);
		String left="";
		String right="";
		String cond="";
		String label="";
		String jxx="";
		String tab="";
		String cmp="";
		
		// A. Add tabs for .asm readability
		for(int j=0;j<tabcount;j++){tab+="\t";}
		
		// B. Set jxx label, Tokenize Left/Right Expressions
			cond = setDoCond(bstate);
			left = setLeftExpr(bstate);
			right = setRightExpr(bstate);
			
			if(checkCharVar(right)){
				cmp= tab+"mov cl,"+right+"\n";
				cmp+= tab+"cmp "+left+",cl";
			}
			else if(!isInteger(right)&&!right.contains("'")){
				cmp= tab+"mov cx,"+right+"\n";
				cmp+= tab+"cmp "+left+",cx";
			}
			else{cmp = tab+"cmp "+left+","+right;}
			
		// C. Convert Block to asm Code
			
			// a. Set Label Names
			label=tab+"do"+docount+":";
			jxx=tab+cond+" do"+docount;
			
			// b. Store asm Code to ArrayList handler
			codeArray.add(label);
			convertNBToAssembly(start+1,end,tabcount+1);
			codeArray.add(cmp);
			codeArray.add(jxx);
	}
	
	// 5. For Block Converter
	public void convertFor(int start, int end, int tabcount){
		
		// I. For Line Tokenization for(int i=0;i<10;i++)
			// A. Declaration Initialization for(int i=0) part
		
			String declare = toConvert.get(start);
			declare=declare.replace("for", "");
			declare=declare.replace("(", "");
			declare=declare.replace("{", "");
			toConvert.set(start, declare);
			
			convertNBToAssembly(start,start+1,tabcount);
			
			// B. Condition Fetch for(..;i<10) part
			
			String bstate = toConvert.get(start+1);
			bstate = "("+bstate;
			bstate = bstate+")";
		
		// II. Asm Convertion
			
			String left="";
			String right="";
			String cond="";
			String forlabel="";
			String endfor="";
			String cmp="";
			String jxx="";
			String jmp="";
			String tab="";
			
			// A. Add tabs for .asm readability
			for(int j=0;j<tabcount;j++){tab+="\t";}
			
			// B. Set jxx label, Tokenize Left/Right Expressions
			cond = setCond(bstate);
			left = setLeftExpr(bstate);
			right = setRightExpr(bstate);
			
			if(checkCharVar(right)){
				cmp= tab+"mov cl,"+right+"\n";
				cmp+= tab+"cmp "+left+",cl";
			}
			else if(!isInteger(right)&&!right.contains("'")){
				cmp= tab+"mov cx,"+right+"\n";
				cmp+= tab+"cmp "+left+",cx";
			}
			else{cmp = tab+"cmp "+left+","+right;}
			
			// C. Convert Block to asm Code
				// a. Set Label Names
				forlabel = tab+"forlabel"+forcount+":";
				jxx = tab+cond+" endfor"+forcount;
				jmp = tab+"jmp forlabel"+forcount;
				endfor = tab+"endfor"+forcount+":";
				
				// b. Store asm Code to ArrayList handler
				codeArray.add(forlabel);
				codeArray.add(cmp);
				codeArray.add(jxx);
				convertNBToAssembly(start+3,end,tabcount+1); // for loop body
				convertNBToAssembly(start+2,start+3,tabcount+1); //inc statement
				codeArray.add(jmp);
				codeArray.add(endfor);
	}
	
	//Helper Functions for Block Statements
	
	// 1. Function to find the corresponding '}' of a block statement
		public int findEnd(int start){
			int opencount=0;
			int closecount=-1;
			for(int j=start;j<toConvert.size();j++){
				String line = toConvert.get(j);
				if(line.contains("{"))opencount++;
				else if(line.contains("}")){
					closecount++;
					if(opencount==closecount){
						return j;
					}
				}
			}
			return -1;
		}
	// 2. If-Else vs If Block Identifier
		public int checkElse(int start){
			int opencount=0;
			int closecount=-1;
			for(int j=start;j<toConvert.size();j++){
				String line = toConvert.get(j);
				if(opencount==closecount&&!line.contains("else")){break;}
				if(opencount==closecount&&line.contains("else")){
					return j;
				}
				if(line.contains("{"))opencount++;
				else if(line.contains("}")){
					closecount++;
					
				}
					
			}
			return -1;
		}
	// 3. Fetch while condition of dowhile statement
		public String getWhile(int start){
			int opencount=0;
			int closecount=-1;
			for(int j=start;j<toConvert.size();j++){
				String line = toConvert.get(j);
				if(opencount==closecount&&!line.contains("while")){break;}
				if(opencount==closecount&&line.contains("while")){
					return line;
				}
				if(line.contains("{"))opencount++;
				else if(line.contains("}")){
					closecount++;	
				}
			}
			return null;
		}
		
	//Miscellaneous Helper Functions
	
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
			if(newLine.length()==0){newLine=" ";} // Special Case for blank strings
			return newLine;
			
		} //end function
	
	// 1.2 Function for Printing Numbers
		public String getVarString(String currLine){
			String newLine="";
			currLine=currLine.trim();
			int a = currLine.indexOf("(");
			int b = currLine.indexOf(")");
			newLine = currLine.substring(a+1,b);
			return newLine;
		}
		
	// 2. Function for Int & Char Variables
		public String getICString(String currLine, String type){
			String dtype="";
			String dsize="";
			if(type.compareTo("int")==0){
				dtype="int";
				dsize=" dw ";
			}
			else if(type.compareTo("char")==0){
				dtype="char";
				dsize=" db ";
			}
			String newLine="";
			currLine=currLine.trim();
			currLine=currLine.replaceFirst(dtype, "");
			currLine=currLine.replaceFirst("static", "");
			currLine=currLine.replace(" ", "");
			// First Case: a=0; or a=255;
			if(currLine.contains("=")){
				int a = currLine.indexOf("=");
				int end = currLine.length();
				int value = Integer.parseInt(currLine.substring(a+1,end));
				String varname = currLine.substring(0,a);
				newLine ="\t"+ varname + dsize+ value;
				
				//Store Character Variable Name if Input was a Character
				if(type.compareTo("char")==0){charStr.add(varname);}
			}
			// Second Case: a; longvarname;
			else{
				// Multiple Declaration int x,y,z;
				if(currLine.contains(",")){
					StringTokenizer st = new StringTokenizer(currLine, ",");
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						newLine+="\t" + token + dsize+ "?\n";
						//Store Character Variable Name if Input was a Character
						if(type.compareTo("char")==0){charStr.add(token);}
					}
					newLine=newLine.substring(0,newLine.length()-1); // remove last \n
				}
				// Single Declaration int x;
				else{
					int end = currLine.length();
					String varname = currLine.substring(0,end);
					newLine ="\t"+ varname +dsize+ "?";
					//Store Character Variable Name if Input was a Character
					if(type.compareTo("char")==0){charStr.add(varname);}
				}
			}
			
			return newLine;
		}// end function
	
	// 3. Function for Int and Char Variables (Mid-code Assignment)
		public void getAssignICString(String currLine, int tabcount){
			String tab="";
			for(int j=0;j<tabcount;j++){tab+="\t";}
			currLine=currLine.trim();
			currLine=currLine.replace(" ","");
			
			// x++;
			if(currLine.contains("++")){
				int a = currLine.indexOf("+");
				String varname = currLine.substring(0,a);
				codeArray.add(tab+"inc "+ varname);
			}
			// x--;
			else if(currLine.contains("--")){
				int a = currLine.indexOf("-");
				String varname = currLine.substring(0,a);
				codeArray.add(tab+"dec "+ varname);
			}
			else{
				String fOp="";
				String sOp="";
				int a = currLine.indexOf("=");
				String varname = currLine.substring(0,a);
				// x=3+2;
				if(currLine.contains("+")||currLine.contains("-")){
					String sign = "";
					String op = "";
					if(currLine.contains("+")){sign="+";op="add ";}
					if(currLine.contains("-")){sign="-";op="sub ";}
					
					int findex = currLine.indexOf(sign);
					int end = currLine.length();
					fOp = currLine.substring(a+1,findex);
					sOp = currLine.substring(findex+1,end);
					// x = x + y; x = x - y;
					if(!isInteger(fOp)&&!checkCharVar(varname)){
						codeArray.add(tab+"mov cx,"+fOp);
						codeArray.add(tab+"mov "+varname+",cx");
					}
					else{
						codeArray.add(tab+"mov "+varname+","+fOp);
					}
					if(!isInteger(sOp)&&!checkCharVar(varname)){
						codeArray.add(tab+"mov cx,"+sOp);
						codeArray.add(tab+op+varname+",cx");
					}
					else{
						codeArray.add(tab+op+varname+","+sOp);
					}
				}
				// x = 0;
				else{
					int eq = currLine.indexOf("=");
					int end = currLine.length();
					sOp = currLine.substring(eq+1,end);
					if(!isInteger(sOp)&&!checkCharVar(varname)){
						codeArray.add(tab+"mov cx,"+sOp);
						codeArray.add(tab+"mov "+varname+",cx");
					}
					else{
						//Cases Handled: c='c' and c=d; 
						if(checkCharVar(sOp)){
							codeArray.add(tab+"mov cl,"+sOp);
							codeArray.add(tab+"mov "+varname+",cl");
						}
						else{
							codeArray.add(tab+"mov "+varname+","+sOp);
						}
					}
					
				}
			}
		}//end function
		
	// 4. Function for Equality Comparators (jxx setter) (branches false)
	public String setCond(String bstate){
		String cond="";
		if(bstate.contains("==")){cond = "jne";}
		else if(bstate.contains("!=")){cond = "je";}
		else if(bstate.contains(">=")){cond = "jl";}
		else if(bstate.contains("<=")){cond = "jg";}
		else if(bstate.contains(">")){cond = "jle";}
		else if(bstate.contains("<")){cond = "jge";}
		return cond;
	}
	
	// 4.1 Function for Equality Comparators (jxx setter) (branches true)
		public String setDoCond(String bstate){
			String cond="";
			if(bstate.contains("==")){cond = "je";}
			else if(bstate.contains("!=")){cond = "jne";}
			else if(bstate.contains(">=")){cond = "jge";}
			else if(bstate.contains("<=")){cond = "jle";}
			else if(bstate.contains(">")){cond = "jg";}
			else if(bstate.contains("<")){cond = "jl";}
			return cond;
		}
	
	// 5. Tokenize Left Side of Equality Comparator
	public String setLeftExpr(String bstate){
		String left="";
		if(bstate.contains("==")){left = bstate.substring(bstate.indexOf("(")+1,bstate.indexOf("="));}
		else if(bstate.contains("!=")){left = bstate.substring(bstate.indexOf("(")+1,bstate.indexOf("!"));}
		else if(bstate.contains(">=")){left = bstate.substring(bstate.indexOf("(")+1,bstate.indexOf(">"));}
		else if(bstate.contains("<=")){left = bstate.substring(bstate.indexOf("(")+1,bstate.indexOf("<"));}
		else if(bstate.contains(">")){left = bstate.substring(bstate.indexOf("(")+1,bstate.indexOf(">"));}
		else if(bstate.contains("<")){left = bstate.substring(bstate.indexOf("(")+1,bstate.indexOf("<"));}
		return left;
	}
	// 6. Tokenize Right Side of Equality Comparator
	public String setRightExpr(String bstate){
		String right="";
		if(bstate.contains("==")){right = bstate.substring(bstate.indexOf("=",bstate.indexOf("=")+1)+1,bstate.indexOf(")"));}
		if(bstate.contains("!=")){right = bstate.substring(bstate.indexOf("=")+1,bstate.indexOf(")"));}
		else if(bstate.contains(">=")){right = bstate.substring(bstate.indexOf("=")+1,bstate.indexOf(")"));}
		else if(bstate.contains("<=")){right = bstate.substring(bstate.indexOf("=")+1,bstate.indexOf(")"));}
		else if(bstate.contains(">")){right = bstate.substring(bstate.indexOf(">")+1,bstate.indexOf(")"));}
		else if(bstate.contains("<")){right = bstate.substring(bstate.indexOf("<")+1,bstate.indexOf(")"));}
		return right;
	}
	// 7. Check if Variable is a user defined string
	public boolean checkStrVar(String currLine){
		for(String item: userStr){
			item=item.trim();
			if(currLine.compareTo(item)==0){
				return true;
			}
		}
		return false;
	}
	// 8. Check if Variable is a user defined character
		public boolean checkCharVar(String currLine){
			
			for(String item: charStr){
				item=item.trim();
				if(currLine.compareTo(item)==0){
					return true;
				}
			}
			return false;
		}
	
	public String getNewFilename(String filename) {
		
		int d = 0;
		
		for (int c = 0; c < filename.length(); c++) {
			char curr = filename.charAt(c);
			if (curr == '.') {
				d = c;
				break;
			}
		}
		filename = filename.substring(0, d);
		return filename;
		
	} //end function
	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    // only got here if we didn't return false
	    return true;
	}
	
} //end class
