import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.*;
import java.util.Stack;

//Had a bunch of issues with class and buildpaths so i decided to bring all necessary methods
//into this one java file


public class ToyLangInterpreter {
	/**
	 * 
	 * @param args functioon call line
	 * @throws FileNotFoundException  if filename does not match input
	 * @throws IOException in case issues getting file
	 */
	
	public static Stack<String> DebugStack = new Stack<String>();
	
	//starting with main method first cause of pseudocode
	public static void main(String[] args) throws FileNotFoundException, IOException {
		int PrintDebug = 0;
		if(args.length > 1) {
			PrintDebug = 1;
		}
		
		
		DebugStack.push("[DEBUG]: START");
    	StringBuilder inputCode = new StringBuilder();
    	String inputFile1 = null;
    	String inputLine;
    	
    	
    		//first arguement is our file
            System.out.println(inputFile1);
    		inputFile1 = args[0];
    		
    		
    	
    	
    		/**
    		 * we create a buffered reader to read from our input file
    		 * each line is appended to our string builder so we can avoid creating multiple strig objects
    		 */
            try (BufferedReader reader = new BufferedReader(new FileReader(inputFile1))) {
            	System.out.println("Inputs from file:");
            	while ((inputLine = reader.readLine()) != null) {
                	System.out.println(inputLine);
                    inputCode.append(inputLine).append("\n");
                }
            }
    	
    	
        //our inputs string from our input file
            String code = inputCode.toString();
            
        //code for test purposes
        // String code = "x = 001;\n x_2 = 0; \n x = 1;\n y = 2;\n z = ---(x+y)*(x+-y);";
        
        /**
         * we create an interpreter and initialize it with our code
         * create a map of our symbols which we get from our interpreter
         * for every entry in our symbolmap we will print the output
         * we print the key first and then the value
         */
            
        ToyLangInterpreter interpreter = new ToyLangInterpreter(code);
        Map<String, Integer> symbols = interpreter.getSymbols();
        System.out.println("Output:");
        for (Map.Entry<String, Integer> entry : symbols.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
        
        
    if(PrintDebug != 0) {
    	for(int i = 0; i < DebugStack.size(); i++) {
    	    System.out.println(DebugStack.get(i));}
    	}
	}
	
	
	
	
	
    private ToyLangTokenizer tokenizer;
    private Map<String, String> curToken;
    public Map<String, Integer> symbols;

    public ToyLangInterpreter(String inCode) {
        this.tokenizer = new ToyLangTokenizer(inCode);
        this.curToken = new HashMap<>();
        this.symbols = new HashMap<>();
        this.useToken(); 
        this.parseCode();
        DebugStack.push("[DEBUG] code parsed ");
    }
    
    
    //accessor method to retrieve our symbol map
    public Map<String, Integer> getSymbols() {
        return symbols;
    }

    private void parseCode() {
        while (!this.curToken.get("type").equals("end")) {
        	DebugStack.push("[DEBUG] parseCode: " + curToken.get("type") +  " " + curToken.get("token"));
            this.parseAssign();
        }
    }

    private void useToken() {
        this.curToken = this.tokenizer.nextToken();
        DebugStack.push("[DEBUG] retrieve: " + this.curToken.get("type") + " = " + this.curToken.get("token"));
    }

    private void match(String expectedToken) throws Exception {
        if (!this.curToken.get("token").equals(expectedToken)) {
            throw new Exception("error wrong token: " + this.curToken.get("token"));
        }
        this.useToken();
    }

    
    private void parseAssign() {
        if (this.curToken.get("type").equals("Identifier")) {
            String varName = this.curToken.get("token");
            this.useToken();
            
            //matches assignment
            try {
                this.match("=");

                int expr = this.expr();

                this.match(";");

                this.symbols.put(varName, expr);

            } catch (Exception e) {
                e.printStackTrace();
            }
            
            //throws error assignment will only have '=' or ';'
        } else {
            throw new RuntimeException("error wrong token: " + this.curToken.get("token"));
        }
    }

    //expression will have term and a prime expression
    private int expr() {
        int i = this.term();
        return i + this.expPrim();
    }

    /**
     * 
     * @return
     */
    private int expPrim() {
        if (this.curToken != null && (this.curToken.get("token").equals("+") || this.curToken.get("token").equals("-"))) {
            String operator = this.curToken.get("token");
            this.useToken();
            int i = this.term();
            if (operator.equals("+")) {
                return i + this.expPrim();
            } else {
                return -i + this.expPrim();
            }
        } else {
            return 0;
        }
    }

    private int term() {
        int n = this.parseFactor();
        return n * this.termPrime();
    }

    private int termPrime() {
        if (this.curToken != null && this.curToken.get("token").equals("*")) {
            this.useToken();
            int n = this.parseFactor();
            return n * this.termPrime();
        } else {
            return 1;
        }
    }

    private int parseFactor() {
        if (this.curToken != null && this.curToken.get("type").equals("Literal")) {
            String token = this.curToken.get("token");
            int literal = Integer.parseInt(token);
            this.useToken();
            return literal;
        } else if (this.curToken != null && this.curToken.get("type").equals("Identifier")) {
            String varName = this.curToken.get("token");
            this.useToken();
            if (this.symbols.containsKey(varName)) {
                return this.symbols.get(varName);
            } else {
                throw new RuntimeException("error uninitialize " + varName);
            }
        } else if (this.curToken != null && this.curToken.get("token").equals("(")) {
            this.useToken();
            int expr = this.expr();
            try {
                this.match(")");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return expr;
        } else if (this.curToken != null && this.curToken.get("token").equals("+")) {
            this.useToken();
            return this.parseFactor();
        } else if (this.curToken != null && this.curToken.get("token").equals("-")) {
            this.useToken();
            return -this.parseFactor();
        } else {
            throw new RuntimeException("error wrong token: " + this.curToken.get("token"));
        }
    }
    
}
