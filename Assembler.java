import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;

public class Assembler {
	
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
		
		//Arraylists that will contain the variable and corresponding data
		ArrayList<String> varnameArray = new ArrayList<String>();
		ArrayList<ArrayList<String>> vardataArray = new ArrayList<ArrayList<String>>();
		
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
		
		//The string to output
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
			
			ArrayList<String> vardataArr = fixData(vardata);
			
			varnameArray.add(varname.trim());
			vardataArray.add(vardataArr);
			ctr++;
		}
		
		//outputs variables to the file
		int ctrVar = 0;
		while(ctrVar < varnameArray.size()){
			int ctrVar2 = 0;
			String outputToFileTemp = "";
			while(ctrVar2 < vardataArray.get(ctrVar).size()){
				//checks if the data is integer or not.
				if(disass.isInteger(vardataArray.get(ctrVar).get(ctrVar2))){
					outputToFile += "\t\tchar "+varnameArray.get(ctrVar)+"_"+ctrVar2+" = "+vardataArray.get(ctrVar).get(ctrVar2).toString()+";\n";
				}else if(vardataArray.get(ctrVar).get(ctrVar2).compareTo("$")==0){
					ctrVar2++;
					continue;
				}else{
					if(vardataArray.get(ctrVar).get(ctrVar2).length()<=1){
						if(vardataArray.get(ctrVar).get(ctrVar2).length()==1){
							outputToFile += "\t\tchar "+varnameArray.get(ctrVar)+"_"+ctrVar2+" = \'"+vardataArray.get(ctrVar).get(ctrVar2).toString()+"\';\n";
						}else{
							outputToFile += "\t\tchar "+varnameArray.get(ctrVar)+"_"+ctrVar2+" = \'\\0\';\n";
						}	
					}else{
						outputToFile += "\t\tString "+varnameArray.get(ctrVar)+"_"+ctrVar2+" = \""+vardataArray.get(ctrVar).get(ctrVar2).toString()+"\";\n";
					}
				}
				ctrVar2++;
			}
			ctrVar++;
		}
		
		//initialize needed variables for checking
		boolean codeSeg = false;
		boolean ifElse = false;
		boolean elseTag = false;
		boolean possibleLoop = false;
		boolean whileLoop = false;
		boolean doSet = false;
		int elseCount = 0;
		String currLabel = "";
		String currLoopLabel = "";
		String conditionEnder = "";
		
		//gets the .code section
		while(input.hasNextLine()){
			String currInput = input.nextLine().trim();
			
			//current line is just the declaration of function or the start of the .code segment
			if((!currInput.contains(".code")&&!codeSeg)||currInput.contains("proc")||currInput.contains("end main")||currInput.contains("main endp")){
				continue;
			}else
			//current line is empty
			if(currInput.compareTo("")==0){
				continue;
			}else
			//current line is among the initializer with @data
			if(currInput.contains("@data")){
				continue;
			}else
			//current line is a comment
			if(currInput.trim().charAt(0)==';'){
				continue;
			}else
			//current line is a label
			if(currInput.contains(":")){
				String currInputOrig = currInput;
				currInput = currInput.replaceAll(":","");
				//current line is the else block
				if(currInput.compareTo(currLabel)==0){
					outputToFile+="\n\t}else{\n";
					elseTag = true;
					currLabel = "";
				}
				//current line is the end of if block
				else if(currInput.compareTo(conditionEnder)==0){
					int ctr3 = 0;
					while(ctr3 <= elseCount){
						outputToFile+="\n\t\t}";
						ctr3++;
					}
					elseCount = 0;
					elseTag = false;
					ifElse = false;
				}
				//current line is the start of a loop
				else if(currLoopLabel.compareTo("")==0){
					currLoopLabel = currInput;
					possibleLoop = true;
					Scanner newScan = new Scanner(new File(filename));
					String scan = newScan.nextLine().trim();
					while(scan.compareTo(currInputOrig)!=0){
						scan = newScan.nextLine().trim();
					}
					scan = newScan.nextLine().trim();
					while(scan.compareTo("")==0&&newScan.hasNextLine()){
						scan = newScan.nextLine().trim();
					}
					//after label, there is cmp, assuming that the loop is a while loop.
					if(scan.contains("cmp")){
						whileLoop = true;
					}
					//after label, code starts, assuming that the loop is a do while loop.
					else{
						outputToFile+="\n\tdo{\n";
						doSet = true;
					}
					newScan.close();
				}
			}
			codeSeg=true;
			
			//tokenize line by separating the three components. 
			//mov ax, 0
			//operation destination source
			ArrayList<String> currentLine = tokenizeLine(currInput);
			String operation = currentLine.get(0);
			String destination = currentLine.get(1);
			String source = currentLine.get(2);
			
				//sets the corresponding variables and registers with the values being moved
				if((operation.compareTo("lea")==0)||(operation.compareTo("mov")==0)){
					if(varnameArray.contains(source)){
						 source+="_0";
					 }
					switch(destination){
					case "ax": registersArrayDW[0] = source;
							   registersArrayDB[0] = "";
							   registersArrayDB[1] = "";
					   break;
					case "bx": registersArrayDW[1] = source;
							   registersArrayDB[2] = "";
							   registersArrayDB[3] = "";
					   break;
					case "cx": registersArrayDW[2] = source;
							   registersArrayDB[4] = "";
							   registersArrayDB[5] = "";
					   break;
					case "dx": registersArrayDW[3] = source;
							   registersArrayDB[6] = "";
							   registersArrayDB[7] = "";
					   break;
					case "ah": registersArrayDB[0] = source;
					   break;
					case "al": registersArrayDB[1] = source;
					   break;
					case "bh": registersArrayDB[2] = source;
					   break;
					case "bl": registersArrayDB[3] = source;
					   break;
					case "ch": registersArrayDB[4] = source;
					   break;
					case "cl": registersArrayDB[5] = source;
					   break;
					case "dh": registersArrayDB[6] = source;
					   break;
					case "dl": registersArrayDB[7] = source;
					   break;
					default: if(varnameArray.contains(destination)){
								ArrayList<String> sourceArr = new ArrayList<String>();
									switch(source){
									case "ax":  sourceArr.add(registersArrayDW[0]);
											   	source = registersArrayDW[0];
									   break;
									case "bx":  sourceArr.add(registersArrayDW[1]);
												source = registersArrayDW[1];
									   break;
									case "cx": sourceArr.add(registersArrayDW[2]);
												source = registersArrayDW[2];
									   break;
									case "dx": sourceArr.add(registersArrayDW[3]);
												source = registersArrayDW[3];
									   break;
									case "ah": sourceArr.add(registersArrayDB[0]);
												source = registersArrayDB[0];
									   break;
									case "al": sourceArr.add(registersArrayDB[1]);
												source = registersArrayDB[1];
									   break;
									case "bh": sourceArr.add(registersArrayDB[2]);
												source = registersArrayDB[2];
									   break;
									case "bl": sourceArr.add(registersArrayDB[3]);
												source = registersArrayDB[3];
									   break;
									case "ch": sourceArr.add(registersArrayDB[4]);
												source = registersArrayDB[4];
									   break;
									case "cl": sourceArr.add(registersArrayDB[5]);
												source = registersArrayDB[5];
									   break;
									case "dh": sourceArr.add(registersArrayDB[6]);
												source = registersArrayDB[6];
									   break;
									case "dl": sourceArr.add(registersArrayDB[7]);
												source = registersArrayDB[7];
									   break;
									default: sourceArr.add(source);
									   break;
								    }
									//data is mov to a variable in memory
									vardataArray.set(varnameArray.indexOf(destination),sourceArr);
									outputToFile+="\n\t\t"+varnameArray.get(varnameArray.indexOf(destination))+"_0 = "
									+sourceArr.get(0).toString()+" ;\n";
							 }
							 break;
				}
			}
			//an interrupt is performed
			else if(operation.compareTo("int")==0){
				outputToFile = doInterrupt(destination, registersArrayDB, registersArrayDW, outputToFile, varnameArray, vardataArray);
			}
			//the operation is either add or sub
			else if(operation.compareTo("add")==0||operation.compareTo("sub")==0){
				boolean isAdd = false;
				if(operation.compareTo("add")==0){
					isAdd = true;
				}
				int x = 0; int y = 0;
				y = checkVar(varnameArray, vardataArray, source);
				switch(destination){
				case "ax":  	x = checkVar(varnameArray, vardataArray, registersArrayDW[0]);
								if(isAdd){
									registersArrayDW[0] = Integer.toString(x + y);
								}else{
									registersArrayDW[0] = Integer.toString(x - y);
								}
				   break;
				case "bx": 		x = checkVar(varnameArray, vardataArray, registersArrayDW[1]);
								if(isAdd){
									registersArrayDW[1] = Integer.toString(x + y);
								}else{
									registersArrayDW[1] = Integer.toString(x - y);
								}
				   break;
				case "cx": 		x = checkVar(varnameArray, vardataArray, registersArrayDW[2]);
								if(isAdd){
									registersArrayDW[2] = Integer.toString(x + y);
								}else{
									registersArrayDW[2] = Integer.toString(x - y);
								}
				   break;
				case "dx": 		x = checkVar(varnameArray, vardataArray, registersArrayDW[3]);
								if(isAdd){
									registersArrayDW[3] = Integer.toString(x + y);
								}else{
									registersArrayDW[3] = Integer.toString(x - y);
								}
				   break;
				case "ah": 		x = checkVar(varnameArray, vardataArray, registersArrayDB[0]);
								if(isAdd){
									registersArrayDB[0] = Integer.toString(x + y);
								}else{
									registersArrayDB[0] = Integer.toString(x - y);
								}
				   break;
				case "al":  	x = checkVar(varnameArray, vardataArray, registersArrayDB[1]);
								if(isAdd){
									registersArrayDB[1] = Integer.toString(x + y);
								}else{
									registersArrayDB[1] = Integer.toString(x - y);
								}
				   break;
				case "bh":  	x = checkVar(varnameArray, vardataArray, registersArrayDB[2]);
								if(isAdd){
									registersArrayDB[2] = Integer.toString(x + y);
								}else{
									registersArrayDB[2] = Integer.toString(x - y);
								}
				   break;
				case "bl":  	x = checkVar(varnameArray, vardataArray, registersArrayDB[3]);
								if(isAdd){
									registersArrayDB[3] = Integer.toString(x + y);
								}else{
									registersArrayDB[3] = Integer.toString(x - y);
								}
				   break;
				case "ch": 		x = checkVar(varnameArray, vardataArray, registersArrayDB[4]);
								if(isAdd){
									registersArrayDB[4] = Integer.toString(x + y);
								}else{
									registersArrayDB[4] = Integer.toString(x - y);
								}
				   break;
				case "cl": 		x = checkVar(varnameArray, vardataArray, registersArrayDB[5]);
								if(isAdd){
									registersArrayDB[5] = Integer.toString(x + y);
								}else{
									registersArrayDB[5] = Integer.toString(x - y);
								}
				   break;
				case "dh": 		x = checkVar(varnameArray, vardataArray, registersArrayDB[6]);
								if(isAdd){
									registersArrayDB[6] = Integer.toString(x + y);
								}else{
									registersArrayDB[6] = Integer.toString(x - y);
								}
				   break;
				case "dl": 		x = checkVar(varnameArray, vardataArray, registersArrayDB[7]);
								if(isAdd){
									registersArrayDB[7] = Integer.toString(x + y);
								}else{
									registersArrayDB[7] = Integer.toString(x - y);
								}
				   break;
				default: if(varnameArray.contains(destination)){
								ArrayList<String> sourceArr = new ArrayList<String>();
								switch(source){
								case "ax": y = checkVar(varnameArray, vardataArray, registersArrayDW[0]);
									break;
								case "bx": y = checkVar(varnameArray, vardataArray, registersArrayDW[1]);
									break;
								case "cx": y = checkVar(varnameArray, vardataArray, registersArrayDW[2]);
									break;
								case "dx": y = checkVar(varnameArray, vardataArray, registersArrayDW[3]);
									break;
								case "ah": y = checkVar(varnameArray, vardataArray, registersArrayDB[0]);
									break;
								case "al": y = checkVar(varnameArray, vardataArray, registersArrayDB[1]);
									break;
								case "bh": y = checkVar(varnameArray, vardataArray, registersArrayDB[2]);
									break;
								case "bl": y = checkVar(varnameArray, vardataArray, registersArrayDB[3]);
									break;
								case "ch": y = checkVar(varnameArray, vardataArray, registersArrayDB[4]);
									break;
								case "cl": y = checkVar(varnameArray, vardataArray, registersArrayDB[5]);
									break;
								case "dh": y = checkVar(varnameArray, vardataArray, registersArrayDB[6]);
									break;
								case "dl": y = checkVar(varnameArray, vardataArray, registersArrayDB[7]);
									break;
									default: if(disass.isInteger(source)){
													y = Integer.parseInt(source);
												}else{
													source = source.replaceAll("\'", "");
													y = source.charAt(0);
												}
								   break;
							    }
								x = checkVar(varnameArray, vardataArray, destination);
								//variable add or subtract
								if(isAdd){
									sourceArr.add(Integer.toString(x + y));
									outputToFile+="\n\t\t"+varnameArray.get(varnameArray.indexOf(destination))+"_0 = "+x+" + "+y+ ";\n";
								}else{
									sourceArr.add(Integer.toString(x - y));
									outputToFile+="\n\t\t"+varnameArray.get(varnameArray.indexOf(destination))+"_0 = "+x+" - "+y+ ";\n";
								}
								vardataArray.set(varnameArray.indexOf(destination),sourceArr);
						 }
						 break;
				}
			}
			//operation increments or decrements
			else if(operation.compareTo("inc")==0||operation.compareTo("dec")==0){
				int x = 0;
				boolean isAdd = false;
				if(operation.compareTo("inc")==0){
					isAdd=true;
				}
				switch(destination){
				case "ax":  	x = checkVar(varnameArray, vardataArray, registersArrayDW[0]);
								if(isAdd){
									registersArrayDW[0] = Integer.toString(x + 1);
								}else{
									registersArrayDW[0] = Integer.toString(x - 1);
								}
				   break;
				case "bx": 		x = checkVar(varnameArray, vardataArray, registersArrayDW[1]);
								if(isAdd){
									registersArrayDW[1] = Integer.toString(x + 1);
								}else{
									registersArrayDW[1] = Integer.toString(x - 1);
								}
				   break;
				case "cx": 		x = checkVar(varnameArray, vardataArray, registersArrayDW[2]);
								if(isAdd){
									registersArrayDW[2] = Integer.toString(x + 1);
								}else{
									registersArrayDW[2] = Integer.toString(x - 1);
								}
				   break;
				case "dx": 		x = checkVar(varnameArray, vardataArray, registersArrayDW[3]);
								if(isAdd){
									registersArrayDW[3] = Integer.toString(x + 1);
								}else{
									registersArrayDW[3] = Integer.toString(x - 1);
								}
				   break;
				case "ah": 		x = checkVar(varnameArray, vardataArray, registersArrayDB[0]);
								if(isAdd){
									registersArrayDB[0] = Integer.toString(x + 1);
								}else{
									registersArrayDB[0] = Integer.toString(x - 1);
								}
				   break;
				case "al":  	x = checkVar(varnameArray, vardataArray, registersArrayDB[1]);
								if(isAdd){
									registersArrayDB[1] = Integer.toString(x + 1);
								}else{
									registersArrayDB[1] = Integer.toString(x - 1);
								}
				   break;
				case "bh":  	x = checkVar(varnameArray, vardataArray, registersArrayDB[2]);
								if(isAdd){
									registersArrayDB[2] = Integer.toString(x + 1);
								}else{
									registersArrayDB[2] = Integer.toString(x - 1);
								}
				   break;
				case "bl":  	x = checkVar(varnameArray, vardataArray, registersArrayDB[3]);
								if(isAdd){
									registersArrayDB[3] = Integer.toString(x + 1);
								}else{
									registersArrayDB[3] = Integer.toString(x - 1);
								}
				   break;
				case "ch": 		x = checkVar(varnameArray, vardataArray, registersArrayDB[4]);
								if(isAdd){
									registersArrayDB[4] = Integer.toString(x + 1);
								}else{
									registersArrayDB[4] = Integer.toString(x - 1);
								}
				   break;
				case "cl": 		x = checkVar(varnameArray, vardataArray, registersArrayDB[5]);
								if(isAdd){
									registersArrayDB[5] = Integer.toString(x + 1);
								}else{
									registersArrayDB[5] = Integer.toString(x - 1);
								}
				   break;
				case "dh": 		x = checkVar(varnameArray, vardataArray, registersArrayDB[6]);
								if(isAdd){
									registersArrayDB[6] = Integer.toString(x + 1);
								}else{
									registersArrayDB[6] = Integer.toString(x - 1);
								}
				   break;
				case "dl": 		x = checkVar(varnameArray, vardataArray, registersArrayDB[7]);
								if(isAdd){
									registersArrayDB[7] = Integer.toString(x + 1);
								}else{
									registersArrayDB[7] = Integer.toString(x - 1);
								}
				   break;
				default: if(varnameArray.contains(destination)){
								ArrayList<String> sourceArr = new ArrayList<String>();
								x = checkVar(varnameArray, vardataArray, destination);
								//variable is incremented or decremented
								if(isAdd){
									sourceArr.add(Integer.toString(x + 1));
									outputToFile+="\n\t\t"+varnameArray.get(varnameArray.indexOf(destination))+"_0++;\n";
								}else{
									sourceArr.add(Integer.toString(x - 1));
									outputToFile+="\n\t\t"+varnameArray.get(varnameArray.indexOf(destination))+"_0--;\n";
								}
								vardataArray.set(varnameArray.indexOf(destination),sourceArr);
						 }
						 break;
				}
			}
			//cmp operation is to be performed
			else if(operation.compareTo("cmp")==0){
				String nextLine = input.nextLine().trim();
				while(nextLine.compareTo("")==0&&input.hasNextLine()){
					nextLine = input.nextLine().trim();
				}
				ArrayList<String> currentNLine = tokenizeLine(nextLine);
				String operationCMP = currentNLine.get(0);
				String destinationCMP = currentNLine.get(1);
				//the block could be a if else statement
				if(!ifElse&&!possibleLoop){
					currLabel = destinationCMP;
					ifElse = true;
				}
				//the block is not a while loop
				else if(!whileLoop){
					currLabel = destinationCMP;
				}
				/* possibleLoop - if the block is a loop
				 * whileLoop - if block is a while loop
				 * doSet - if block is a do while loop
				 * switches between possible conditions
				 */
				switch(operationCMP){
				case "jl": if(possibleLoop){
								if(whileLoop){
									outputToFile+="\n\twhile("+destination+"_0 >= "+source+"){\n";
								}else if(doSet){
									outputToFile+="\n\t}while("+destination+"_0 < "+source+");\n";
									doSet=false;
									possibleLoop=false;
									currLoopLabel="";
								}
							}else{
								outputToFile+="\n\tif("+destination+"_0 >= "+source+"){\n";
							}
					break;
				case "jle": if(possibleLoop){
								if(whileLoop){
									outputToFile+="\n\twhile("+destination+"_0 > "+source+"){\n";
								}else if(doSet){
									outputToFile+="\n\t}while("+destination+"_0 <= "+source+");\n";
									doSet=false;
									possibleLoop=false;
									currLoopLabel="";
								}
							}else{
								outputToFile+="\n\tif("+destination+"_0 > "+source+"){\n";
							}
					break;
				case "jg": if(possibleLoop){
								if(whileLoop){
									outputToFile+="\n\twhile("+destination+"_0 <= "+source+"){\n";
								}else if(doSet){
									outputToFile+="\n\t}while("+destination+"_0 > "+source+");\n";
									doSet=false;
									possibleLoop=false;
									currLoopLabel="";
								}
							}else{
								outputToFile+="\n\tif("+destination+"_0 <= "+source+"){\n";
							}
					break;
				case "jge": if(possibleLoop){
								if(whileLoop){
									outputToFile+="\n\twhile("+destination+"_0 < "+source+"){\n";
								}else if(doSet){
									outputToFile+="\n\t}while("+destination+"_0 >= "+source+");\n";
									doSet=false;
									possibleLoop=false;
									currLoopLabel="";
								}
							}else{
								outputToFile+="\n\tif("+destination+"_0 < "+source+"){\n";
							}
					break;
				case "je": if(possibleLoop){
								if(whileLoop){
									outputToFile+="\n\twhile("+destination+"_0 != "+source+"){\n";
								}else if(doSet){
									outputToFile+="\n\t}while("+destination+"_0 == "+source+");\n";
									doSet=false;
									possibleLoop=false;
									currLoopLabel="";
								}
							}else{
								outputToFile+="\n\tif("+destination+"_0 != "+source+"){\n";
							}
					break;
				case "jne": if(possibleLoop){
								if(whileLoop){
									outputToFile+="\n\twhile("+destination+"_0 == "+source+"){\n";
								}else if(doSet){
									outputToFile+="\n\t}while("+destination+"_0 != "+source+");\n";
									doSet=false;
									possibleLoop=false;
									currLoopLabel="";
								}
							}else{
								outputToFile+="\n\tif("+destination+"_0 == "+source+"){\n";
							}
					break;
				}
			}
			//ends condition or loop
			else if(operation.compareTo("jmp")==0){
				conditionEnder = destination;
				if(ifElse){
					ifElse = false;
				}
				if(elseTag){
					elseCount++;
				}
				if(possibleLoop&&whileLoop){
					possibleLoop = false;
					whileLoop = false;
					outputToFile += "\n\t\t}\n";
				}
			}
		}
		input.close();
		outputToFile+="\n\n\t}\n}";
		//prints the generated code to an output file. 
		//format is the newFilename.java
		try {
			outputStream =  new PrintWriter(new FileOutputStream(newFilename + ".java"));
			outputStream.println(outputToFile);
			outputStream.close();
			System.out.println("------------------JAVA CODE ASSEMBLED!--------------------");
			System.out.println(outputToFile);
		}catch(Exception e){
			System.err.println("No file found.");
			System.exit(0);
		}
	}
	
	/* Helper functions
	 * 
	 */
	
	//fixes variable data from .data
	//removes single or double quotes and the $ terminator
	public ArrayList<String> fixData(String vardata){
		ArrayList<String> vardataOut = new ArrayList<String>();
		Disassembler disass = new Disassembler();
		while(true){
			String var = "";
			boolean pairSingle = false;
			boolean pairDouble = false;
			boolean inQuotes = false;
			
			if(vardata.charAt(0)=='\''){
				vardata=vardata.substring(1,vardata.length());
				pairSingle = true;
				inQuotes=true;
			}else if(vardata.charAt(0)=='"'){
				vardata=vardata.substring(1,vardata.length());
				pairDouble = true;
				inQuotes=true;
			}			
			int end = 0;
			// data is enclosed in '
			if(pairSingle&&inQuotes){
				for (int i = 0; i < vardata.length(); i++) {
					char currC = vardata.charAt(i);
					if (currC == '\''){
						end = i;
						break;
					}
				}
				var = vardata.substring(0, end);
			}
			else if(pairDouble&&inQuotes){
				for (int i = 0; i < vardata.length(); i++) {
					char currC = vardata.charAt(i);
					if (currC == '"'){
						end = i;
						break;
					}
				}
				var = vardata.substring(0, end);
			}else if(vardata.contains(",")){
				end = vardata.indexOf(",");
				var = vardata.substring(0, end).trim();
			}else{
				if(vardata.compareTo("?")==0){
					var = " ";
				}else{
					var = vardata.trim();
				}
			}
			if(end+2 < vardata.length()&&!disass.isInteger(vardata)){
				vardata = vardata.substring(end+1, vardata.length());
			}else{
				vardata = "";
			}
			if(var.compareTo("")!=0&&!var.contains("$")){
				if(var.compareTo(" ")==0){
					var = var.trim();
				}
				vardataOut.add(var);
			}
			if(var.contains("$")||vardata.compareTo("")==0){
				break;
			}
		}
		return vardataOut;
	}
	
	//tokenize the lines in the .code section
	//separates the three components: operation, destination, source
	public ArrayList<String> tokenizeLine(String currInput){
		ArrayList<String> lineReturned = new ArrayList<String>();
		int operationBreak = 0;
		int destinationBreak = 0;
		for (int i = 0; i < currInput.length(); i++) {
			char currC = currInput.charAt(i);
			if (currC == ' '){
				operationBreak = i;
				break;
			}
		}
		for (int j = operationBreak + 1; j < currInput.length(); j++) {
			char currC = currInput.charAt(j);
			if (currC == ','){
				destinationBreak = j;
				break;
			}
		}
		String operation = currInput.substring(0, operationBreak).trim();
		String destination = "";
		String source = "";
		if(destinationBreak == 0){
			destination = currInput.substring(operationBreak+1).trim();
		}else{
			destination = currInput.substring(operationBreak+1, destinationBreak).trim();
			source = currInput.substring(destinationBreak+1).trim();
			if(source.contains("offset")){
				source = source.replace("offset", "").trim();
			}
		}
		lineReturned.add(operation);
		lineReturned.add(destination);
		lineReturned.add(source);
		return lineReturned;
	}
	
	//do the interrupts that needs to be done
	//outputs corresponding results to file
	public String doInterrupt(String interrupt, String[] regArrayDB, String[] regArrayDW, String outputToFile, ArrayList<String> varnameArray, ArrayList<ArrayList<String>> vardataArray){
		switch(interrupt){
		case "21h": if(regArrayDB[0].compareTo("09h")==0){
						outputToFile+="\n\t\tSystem.out.print(";
						//checks if given source in DX is a variable or a string
						//System.out.println(regArrayDW[3]);
						if(varnameArray.contains(regArrayDW[3].replaceAll("_0", ""))){
							int ctr = 0;
							while(ctr < vardataArray.get(varnameArray.indexOf(regArrayDW[3].replaceAll("_0", ""))).size()){
								outputToFile+=varnameArray.get(varnameArray.indexOf(regArrayDW[3].replaceAll("_0", "")))+"_"+ctr;
								ctr++;
								if(ctr < vardataArray.get(varnameArray.indexOf(regArrayDW[3].replaceAll("_0", ""))).size()){
									outputToFile+="+";
								}
							}
						}else{
							//given source is a string, not a variable
							outputToFile+="\""+regArrayDW[3]+"\"";
						}
						outputToFile+=");\n";
					}else if(regArrayDB[0].compareTo("02h")==0){
						if(regArrayDB[7].toLowerCase().replaceAll("_0", "").compareTo("0ah")==0){
							outputToFile+="\n\t\tSystem.out.println(\"\");";
						}else{
							outputToFile+="\n\t\tSystem.out.print(";
							//checks if given source in DL is a variable or a character
							if(varnameArray.contains(regArrayDB[7].replaceAll("_0", ""))){
								int ctr = 0;
								while(ctr < vardataArray.get(varnameArray.indexOf(regArrayDB[7].replaceAll("_0", ""))).size()){
									outputToFile+=varnameArray.get(varnameArray.indexOf(regArrayDB[7].replaceAll("_0", "")))+"_"+ctr;
									ctr++;
									if(ctr < vardataArray.get(varnameArray.indexOf(regArrayDB[7].replaceAll("_0", ""))).size()){
										outputToFile+="+";
									}
								}
							}else{	
								//given source is a character, not a variable
								outputToFile+="'"+regArrayDB[3]+"'";
							}
							outputToFile+=");\n";
						}
					}
					break;
		}
		return outputToFile;
	}
	
	//returns the ascii/integer value
	//checks if the value being asked for is from a variable or not from a register
	public int checkVar(ArrayList<String> varnameArray, ArrayList<ArrayList<String>> vardataArray, String source){
		Disassembler disass = new Disassembler();
		int toReturn = 0;
		if(varnameArray.contains(source)){
			if(disass.isInteger(vardataArray.get(varnameArray.indexOf(source)).get(0))){
				toReturn = Integer.parseInt(vardataArray.get(varnameArray.indexOf(source)).get(0));
			}else{
				toReturn = vardataArray.get(varnameArray.indexOf(source)).get(0).charAt(0);
			}
		}else if(source.compareTo("ax")!=0 ||source.compareTo("bx")!=0 ||source.compareTo("cx")!=0 ||source.compareTo("dx")!=0 
				||source.compareTo("ah")!=0 ||source.compareTo("al")!=0 ||source.compareTo("bh")!=0 ||source.compareTo("bl")!=0 
				||source.compareTo("ch")!=0 ||source.compareTo("cl")!=0 ||source.compareTo("dh")!=0 ||source.compareTo("dl")!=0){
			if(disass.isInteger(source)){
				toReturn = Integer.parseInt(source);
			}else{
				toReturn = source.charAt(0);
			} 
		}
		return toReturn;
	}

}
