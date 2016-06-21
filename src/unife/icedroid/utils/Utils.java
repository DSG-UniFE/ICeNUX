package unife.icedroid.utils;

import unife.icedroid.exceptions.CommandImpossibleToRun;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Utils {
    private final static String TAG = "Utils";
    private final static boolean DEBUG = true;
    
    public static ArrayList<String> rootExec(String command) throws CommandImpossibleToRun, Exception {
    	if (command.equals("")) {
    		throw new CommandImpossibleToRun("Empty command string");
    	}

    	String[] commands = {command};
    	return rootExec(commands);
    }

    public static ArrayList<String> rootExec(String[] commands) throws CommandImpossibleToRun, Exception {
    	boolean ok = false;
    	if (commands.length == 0) {
    		throw new CommandImpossibleToRun("Empty command string");
    	}
    	for (String string : commands) {
			if (!string.equals("")) {
				ok = true;
				break;
			}
		}
    	if (!ok) {
    		throw new CommandImpossibleToRun("Empty command string");
    	}

    	
        Process interactiveShell = null;
        BufferedReader input = null;
        PrintWriter output = null;
        BufferedReader error = null;
        String su = "su";
        ArrayList<String> results = new ArrayList<>();
        String line = null;
        String commandToRun = "";

        try {
            //Invoke the interactive shell
            interactiveShell = Runtime.getRuntime().exec(su);
            input = new BufferedReader(new InputStreamReader(interactiveShell.getInputStream()));
            output = new PrintWriter(interactiveShell.getOutputStream());
            error = new BufferedReader(new InputStreamReader(interactiveShell.getErrorStream()));

            //Run the command
            for (String command : commands) {
            	commandToRun += command + " ";
			};
			output.println(commandToRun);
            output.println("exit");
            output.flush();
            interactiveShell.waitFor();

            //Check for errors
            if ((line = error.readLine()) != null) {
            	if (DEBUG) {
	            	do {
	            		System.err.println(line);
	            	} while ((line = error.readLine()) != null);
            	}
                throw new CommandImpossibleToRun();
            }

            //Check to save some outputs
            while ((line = input.readLine()) != null) {
                results.add(line);
            }

        } catch (CommandImpossibleToRun citr) {
        	throw citr;
        } catch (Exception ex) {
            String msg = ex.getMessage();
            if (DEBUG) {
            	msg = (msg != null) ? TAG + ": " + msg : TAG + ": " + 
            		"rootExec() - impossible to run the command: " + commandToRun;
            	System.out.println(msg);
            }
            
            throw ex;
        }
        finally {
            //Close all
            input.close();
            output.close();
            interactiveShell.destroy();
        }

        return results;
    }

    public static ArrayList<String> exec(String command) throws CommandImpossibleToRun {
        Process process = null;
        BufferedReader input = null;
        BufferedReader error = null;
        ArrayList<String> results = new ArrayList<>();

        try {
            //Run the command
            process = Runtime.getRuntime().exec(command);
            input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            error = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            //Check for errors
            if (error.readLine() != null) {
                throw new CommandImpossibleToRun();
            }

            //Check to save some outputs
            String line = null;
            while ((line = input.readLine()) != null) {
                results.add(line);
            }

        } catch (Exception ex) {
            String msg = ex.getMessage();
            if (DEBUG) {
	            if (msg != null) {
	            	msg = TAG + ": " + msg;
	            } else {
	            	msg = TAG + ": " + "exec() - Impossible to run the command: " + command;
	            }
	            System.out.println(msg);
            }
            
            throw new CommandImpossibleToRun("Impossible to run the command");
        }

        return results;
    }
}
