package unife.icedroid.utils;

import unife.icedroid.exceptions.TerminalCommandError;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Utils {
    private final static String TAG = "Utils";
    private final static boolean DEBUG = true;
    
    public static ArrayList<String> rootExec(String command) throws TerminalCommandError, IOException {
    	if (command.equals("")) {
    		throw new TerminalCommandError("Empty command string");
    	}

    	String[] commands = {command};
    	return rootExec(commands);
    }

    public static ArrayList<String> rootExec(String[] commands) throws TerminalCommandError, IOException {
    	boolean ok = false;
    	if (commands.length == 0) {
    		throw new TerminalCommandError("Empty command string");
    	}
    	for (String string : commands) {
			if (!string.equals("")) {
				ok = true;
				break;
			}
		}
    	if (!ok) {
    		throw new TerminalCommandError("Empty command string");
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
                throw new TerminalCommandError();
            }
            //Check to save some outputs
            while ((line = input.readLine()) != null) {
                results.add(line);
            	if (DEBUG) {
            		System.out.println(line);
            	}
            }
        } catch (TerminalCommandError tcerr) {
        	throw tcerr;
        } catch (IOException ioex) {
            String msg = ioex.getMessage();
            if (DEBUG) {
            	msg = TAG + ": " + ((msg != null) ? msg : 
            		"rootExec() - error reading results of the command: " + commandToRun);
            	System.err.println(msg);
            }
            throw ioex;
        }
        catch (InterruptedException iex) {
            String msg = iex.getMessage();
            if (DEBUG) {
            	msg = TAG + ": " + ((msg != null) ? msg :
            		"rootExec() - error reading results of the command: " + commandToRun);
            	System.err.println(msg);
            }
            throw new TerminalCommandError("Interrupted Exception raised while waiting for " + 
            		"the interactive shell to finish running jobs");
		}
        finally {
            //Close all streams
            input.close();
            output.close();
            error.close();
            interactiveShell.destroy();
        }

        return results;
    }

    public static ArrayList<String> exec(String commandToRun) throws TerminalCommandError, IOException {
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
                throw new TerminalCommandError();
            }

            //Check to save some outputs
            while ((line = input.readLine()) != null) {
                results.add(line);
            	if (DEBUG) {
            		System.out.println(line);
            	}
            }
        } catch (TerminalCommandError tcerr) {
        	throw tcerr;
        } catch (IOException ioex) {
            String msg = ioex.getMessage();
            if (DEBUG) {
            	msg = TAG + ": " + ((msg != null) ? msg : 
            		"exec() - error reading results of the command: " + commandToRun);
	            System.err.println(msg);
            }
            throw ioex;
        } finally {
        	input.close();
        	error.close();
        }

        return results;
    }

    public static boolean containsSubstring (ArrayList<String> results, String substring) {
        for (String line : results) {
        	if (DEBUG) {
        		System.out.println(line);
        	}
            if (line.contains(substring)) {
            	System.out.println("TRUE " + substring);
                return true;
            }
        }
        return false;
    }
}
