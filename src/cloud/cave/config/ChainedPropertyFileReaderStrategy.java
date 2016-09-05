package cloud.cave.config;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.apache.commons.io.FileUtils;

import cloud.cave.common.CaveConfigurationNotSetException;

/**
 * A configuration reader strategy based upon Chained Property Files (CPFs).
 * 
 * <p>
 * A CPF is a file, closely similar to Java property files, which allows
 * (key,value) pairs to be defined in files which are read at runtime using this
 * reader. A CPF, however, allows one file to chain (include) another so more complex
 * property sets can be defined in a modular way.
 * <p>
 * The format of any CPF is a UTF-8 encoded text files consisting of one of
 * three types of lines:
 * <ol>
 * <li>COMMENT: Any empty line or line starting with #
 * <li>PROPERTY: A line of format 'key = value'
 * <li>CHAIN: A line of format '&lt; filename' (See note on filenames below)
 * </ol>
 * <p>
 * When a CHAIN line is read in a CPF file, the named CPF file is read before
 * proceeding to the following lines. Any PROPERTY line assigns resulting
 * (key,value) pair in the order they are read, allowing overwriting
 * properties easily.
 * 
 * <p>
 * Example:
 * <p>
 * base.cpf contains
 * <pre>
 * prop1 = Mikkel
 * prop2 = Magnus
 * </pre>
 * while layer1.cpf contains
 * <pre>
 * &lt; base.cpf
 * prop1 = Mathilde
 * </pre>
 * Constructing a ChainedPropertyFileReaderStrategy, cpf, on 'layer1.cpf'
 * will result in two properties with the following values
 * 
 * <br>
 * cpf.getProp("prop1") == "Mathilde";
 * <br>
 * cpf.getProp("prop2") == "Magnus";
 * 
 * <p>
 * Thus CPFs are ideal for creating base configurations with default
 * properties defined, while small changes can easily be made just
 * by making a CPF that chains to the base configuration.
 * <p>
 * Note: Only filenames that strictly contain alphanumeric and dash
 * are accepted!
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 */

public class ChainedPropertyFileReaderStrategy implements PropertyReaderStrategy {

  /**
   * Read the CPF file (and any chained ones) and update the internal
   * properties.
   * 
   * @param cpfFile
   *          the name of the CPF file to read
   */
  public ChainedPropertyFileReaderStrategy(File cpfFile) {
    propertyMap = new HashMap<>();
    
    // We use a two parse - first we read the file
    // and all chained files into a list of raw
    // text lines
    List<String> contents = readCPFContentsAndRecurseChained(cpfFile);
    // Next we parse all lines and assign
    // properties / flag formatting errors
    parseContents(contents, cpfFile.getName());
  }

  /**
   * Read this file, find all CHAIN lines and recursively read them also.
   * 
   * @param cpfFile
   *          the CPF file
   * @return the full recursive contents of this file and all chained ones
   */
  private List<String> readCPFContentsAndRecurseChained(File cpfFile) {
    // Read this file into 'contents'
    List<String> contents;
    // Read in the file
    try {
      contents = FileUtils.readLines(cpfFile, "UTF-8");
    } catch (IOException e) {
      // Fail fast
      throw new CaveConfigurationNotSetException(
              "The CPF file '" + cpfFile.getName()
              + "' is missing.");
    }
    
    // Parse all lines in contents and if any "< base.cpf" is found
    // recursively construct a list of lines from that
    List<String> nextlayercontents = recurseOnAnyChainLines(contents);
    
    // Concat the contents such that the top layer (contents)
    // is after the lower layers (nextlayercontents) 
    nextlayercontents.addAll(contents);
    return nextlayercontents;
  }

  // Match the '< /opt/weather-service.cpf' cascading input line
  private static final String readLineRegexp = "^<\\s*([\\w-\\./]+)$"; 
  private static final Pattern readLinePattern = Pattern.compile(readLineRegexp);
  
  /**
   * Parse all lines in contents, and for each '< /opt/base.cpf' recurse on that
   * cascading file.
   * 
   * @param contents the lines to parse
   * @return the contents of any chained files recursively; note that
   * the input contents is NOT part of the returned contents
   */
  private List<String> recurseOnAnyChainLines(List<String> contents) {
    List<String> allContents = new ArrayList<String>();
    
    for(String line: contents) {
      Matcher m = readLinePattern.matcher(line);
      if (m.find()) {
        File nextLayerFile = new File(m.group(1));
        allContents.addAll(readCPFContentsAndRecurseChained(nextLayerFile));
      }
    }
    return allContents;
  }

  // Match 'key=value' with white space around the =
  private static final String propertyAssignmentRegexp = "^\\s*(\\w+)\\s*=\\s*(\\S+)\\s*$";
  private static final Pattern propertyPattern = Pattern.compile(propertyAssignmentRegexp);
  
  // Match either '# text' or empty line, nothing else
  private static final String commentLineRegexp = "^(\\s*|#.*)$"; 
  private static final Pattern commentPattern = Pattern.compile(commentLineRegexp);
  
  private Map<String,String> propertyMap;
  
  /** 
   * Parse a set of lines for any PROPERTY lines, and assign them
   * to the internal (key,value) store. Any other line types are
   * ignored; however ill formed lines throws a CaveConfigurationNotSetException
   * exception
   * @param contents the contents of a CPF file
   * @param fileName name of the root file, the top CPF file
   */
  private void parseContents(List<String> contents, String fileName) {
    int lineCount = 0;
    for (String line: contents) {
      lineCount++;
      Matcher m = propertyPattern.matcher(line);
      if (m.find()) {
        String key = m.group(1); String value = m.group(2);
        propertyMap.put(key, value);
      } else {
        m = commentPattern.matcher(line);
        if (m.find()) {
          // Ignore comments
        } else {
          m = readLinePattern.matcher(line);
          if (m.find()) {
            // Ignore chaining lines, they are processed in the
            // reading phase
          } else {
            throw new CaveConfigurationNotSetException("At line " + lineCount
                + " in file '" + fileName
                + "': The line does not match the CPF format.\n" + line);
          }
        }
      }
    }
  }

  @Override
  public String getValue(String key) {
    return propertyMap.get(key);
  }

}
