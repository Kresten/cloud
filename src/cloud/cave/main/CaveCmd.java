package cloud.cave.main;

import java.io.*;

import cloud.cave.broker.*;
import cloud.cave.client.*;
import cloud.cave.config.*;
import cloud.cave.domain.*;

/**
 * Main method for a command line client. It is configured through the given CPF
 * file.
 * 
 * @see Config
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class CaveCmd {
  public static void main(String[] args) throws IOException {
    CaveClientFactory factory;
    PropertyReaderStrategy propertyReader;
    
    String cpfFilename = args[0];
    String loginName = args[1];
    String pwd = args[2];

    propertyReader = new ChainedPropertyFileReaderStrategy(new File(cpfFilename));
    factory = new StandardClientFactory(propertyReader);
    
    ClientRequestHandler requestHandler = factory.createClientRequestHandler();
    Cave cave = new CaveProxy(requestHandler);
    
    System.out.println("Starting cmd with Cpf File = " + cpfFilename);
    
    new CmdInterpreter(cave, loginName, pwd, 
        System.out, System.in).readEvalLoop();
  }
}

