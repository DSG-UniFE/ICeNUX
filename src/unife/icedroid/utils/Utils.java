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

            if (DEBUG) {
        		System.out.println("Executed command: " + commandToRun);
        	}

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
            	if (DEBUG) {
            		System.out.println(line);
            	}
            }

        } catch (CommandImpossibleToRun citr) {
            String msg = citr.getMessage();
            if (DEBUG) {
            	msg = TAG + ": " + ((msg != null) ? msg : 
            		"rootExec() - impossible to run the command: " + commandToRun);
            	System.err.println(msg);
            }
    		System.err.println("Errors detected after running command " + commandToRun);
        	throw citr;
        } catch (Exception ex) {
            String msg = ex.getMessage();
            if (DEBUG) {
            	msg = TAG + ": " + ((msg != null) ? msg : 
            		"rootExec() - impossible to run the command: " + commandToRun);
            	System.err.println(msg);
            }
    		System.err.println("General error detected after running command " + commandToRun);
            throw ex;
        }
        finally {
            //Close all
            input.close();
            output.close();
            error.close();
            interactiveShell.destroy();
        }

        return results;
    }

    public static ArrayList<String> exec(String commandToRun) throws CommandImpossibleToRun {
        Process process = null;
        BufferedReader input = null;
        BufferedReader error = null;
        ArrayList<String> results = new ArrayList<>();
        String line = null;

        try {
            //Run the command
            process = Runtime.getRuntime().exec(commandToRun);
            input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            error = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            if (DEBUG) {
        		System.out.println("Executed command: " + commandToRun);
        	}

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
            	if (DEBUG) {
            		System.out.println(line);
            	}
            }

        } catch (Exception ex) {
            String msg = ex.getMessage();
            if (DEBUG) {
            	msg = TAG + ": " + ((msg != null) ? msg : 
            		"exec() - impossible to run the command: " + commandToRun);
	            System.err.println(msg);
            }
            
            throw new CommandImpossibleToRun("Impossible to run the command " + commandToRun);
        }

        return results;
    }
}
