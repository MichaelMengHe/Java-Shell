package commands;

import data.FileSystem;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;
import java.util.ArrayList;

public class Grep implements Command {
  

  @Override
  public String execute(FileSystem fs, String params) {
    String result = "";
    Pattern pattern;
    // Get the first token from the parameters. This will either be the
    // -R flag or the regex.
    params = params.trim();
    int currentSearchIndex = params.indexOf(' ');
    String firstToken = params.substring(0, currentSearchIndex);
    // Remove the first token from the parameters, and trim them
    params = params.substring(currentSearchIndex+1).trim();
    // If the first token is the -R flag, the second token should be the regex
    // Remove it from the parameters as well
    try{
      if (firstToken.equalsIgnoreCase("-r")){
        currentSearchIndex = params.indexOf(' ');
        String regex = params.substring(0, currentSearchIndex);
        params = params.substring(currentSearchIndex+1).trim();
        pattern = Pattern.compile(regex);
      } else{
        // If not, the first token is the regex
        pattern = Pattern.compile(firstToken);
      }
    } catch (PatternSyntaxException e){
      // If a valid regex is not formed, return an error message
      return "Error - Invalid regex";
    }
    // Get the list of paths from the parameters
    ArrayList<String> paths = getPaths(params);
    // If the -R flag is given, process the paths with grepDirectories
    if (firstToken.equalsIgnoreCase("-r")){
      result = grepDirectories(fs, paths, pattern);
    } else{
    // Otherwise, process them with grepFiles
      result = grepFiles(fs, paths, pattern);
    }
    // Return the result of whichever is run
    return result;
  }
  
  // Handles grepping a list of files
  private String grepFiles(FileSystem fs, ArrayList<String> paths,
      Pattern pattern){
    String result = "";
    // For each path,
    for (String path : paths){
      // Try to get the contents of that path
      try{
        String contents = fs.getFileContents(path);
        // Split this by newlines
        String[] lines = contents.split("\n");
        // For each line,
        for (String line : lines){
          // See if the line matches the regex. If so,
          // add it to the result string + a newline
          Matcher matcher = pattern.matcher(line);
          if (matcher.matches()){
            result += line + "\n";
          }
        }
      } catch (data.InvalidPathException e){        
      // If this fails, return an error message
        return "Error - Invalid path\n";
      }
    }
    return result;

  }
  
  private String grepDirectories(FileSystem fs, ArrayList<String> paths,
      Pattern pattern){
    String result = "";
    // For each path,
    for (String path : paths){
      // Try to get the list of files in the directory
      try{
        // Loop through the list of files
        data.Directory currDir = fs.getDirectory(path);
        ArrayList<String> fileNames = currDir.getFiles();
        for (String file : fileNames){
          // If the directory name doesn't end with a slash, add one. Add the
          // filename to get its full path.
          if (!path.endsWith("/")){
            path += "/";
          }
          String fullPath = path + file;
          // Get the contents of the file
          String fileContents = fs.getFileContents(fullPath);
          // Split it by newlines
          String[] lines = fileContents.split("\n");
          // For each line, if it matches the regex,
          // Add the file path, the line, and a newline of the result string
          for (String line : lines){
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()){
              result += fullPath + ": " + line + "\n";
            }
          }
          // Run this method on the list of subdirectories and append the
          // output to the result string
          result += grepDirectories(fs, currDir.getSubdirNames(), pattern);
        }
      
      } catch(data.InvalidPathException e){
      // If this fails, return an error message.
        return "Error - Invalid path\n";
      }
    }
    return result;
  }
  
  // Getting paths from the parameters
  private ArrayList<String> getPaths(String params) {
    // Create the result list
    ArrayList<String> result = new java.util.ArrayList<String>();
    // Loop until the parameters are empty
    while (!params.isEmpty()) {
      // If the first character is a quote, look for the next quote.
      if (params.startsWith("\"")) {
        int nextQuote = params.indexOf('"', 1);
        // If there is one, add the substring between them to the list
        if (nextQuote != -1) {
          result.add(params.substring(1, nextQuote));
          // Remove this substing + the quotes and trim the string
          params = params.substring(nextQuote + 1).trim();
        }
      }
      // Look for the next space
      int nextSpace = params.indexOf(' ');
      // If there is one, add everything up to it to the list
      if (nextSpace != -1) {
        result.add(params.substring(0, nextSpace));
        // Remove what was just added from the params and trim them
        params = params.substring(nextSpace + 1).trim();
      } else {
        // If not, add the remainder of params to the list and clear the params
        result.add(params);
        params = "";
      }
    }
    return result;
  }
}
