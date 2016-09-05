package cloud.cave.config;

import org.junit.Test;

import cloud.cave.common.CaveConfigurationNotSetException;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.*;

/**
 * TDD of the Chained Property File (CPF) reading system utilized in SkyCave to
 * read in properties that direct delegate construction.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public class TestChainedPropertyFiles {

  @Test
  public void shouldReadCorrectlyFormattedCPFFile() throws IOException {
    File cpfFile = new File("test/testcases/base.cpf");
    
    ChainedPropertyFileReaderStrategy cpfReader = new
        ChainedPropertyFileReaderStrategy(cpfFile);
    
    assertThat(cpfFile, is(notNullValue()));
    
    assertThat(cpfReader.getValue(Config.SKYCAVE_APPSERVER), is("localhost:37123"));
    assertThat(cpfReader.getValue(Config.SKYCAVE_CLIENTREQUESTHANDLER_IMPLEMENTATION), 
        is("cloud.cave.doubles.AllTestDoubleClientRequestHandler"));
    
    assertThat(cpfReader.getValue(Config.SKYCAVE_SUBSCRIPTION_IMPLEMENTATION), 
        is("cloud.cave.doubles.TestStubSubscriptionService"));
    assertThat(cpfReader.getValue(Config.SKYCAVE_SUBSCRIPTIONSERVER), 
        is("localhost:42042"));
  }

  @Test
  public void shouldRejectNonCommentLine() throws IOException {
    File cpfFile = new File("test/testcases/wrong1.cpf");

    try {
      new ChainedPropertyFileReaderStrategy(cpfFile);
      fail("No exception was thrown!");
    } catch (CaveConfigurationNotSetException e) {
      assertThat(e.getMessage(), containsString("At line 3 in file 'wrong1.cpf'"));
      assertThat(e.getMessage(), containsString("=== Configure for"));
    }
  }
  
  @Test
  public void shouldRejectLineWithSet() throws IOException {
    File cpfFile = new File("test/testcases/wrong2.cpf");

    try {
      new ChainedPropertyFileReaderStrategy(cpfFile);
      fail("No exception was thrown!");
    } catch (CaveConfigurationNotSetException e) {
      assertThat(e.getMessage(), containsString("At line 4 in file 'wrong2.cpf'"));
      assertThat(e.getMessage(), containsString("set SKYCAVE"));
    }
  }

  @Test
  public void shouldRejectLineWithExport() throws IOException {
    File cpfFile = new File("test/testcases/wrong3.cpf");

    try {
      new ChainedPropertyFileReaderStrategy(cpfFile);
      fail("No exception was thrown!");
    } catch (CaveConfigurationNotSetException e) {
      assertThat(e.getMessage(), containsString("At line 4 in file 'wrong3.cpf'"));
      assertThat(e.getMessage(), containsString("export SKYCAVE"));
    }
  }
  
  @Test
  public void shouldErrOnNonExistingFile() throws IOException {
    File cpfFile = new File("test/testcases/nonexisting.cpf");

    try {
      new ChainedPropertyFileReaderStrategy(cpfFile);
      fail("No exception was thrown!");
    } catch (CaveConfigurationNotSetException e) {
      assertThat(e.getMessage(), containsString("The CPF file 'nonexisting.cpf' is missing"));
    }
  }

  @Test
  public void shouldReadCorrectlyChainedProperties() throws IOException {
    File cpfFile = new File("test/testcases/layer1.cpf");
    
    ChainedPropertyFileReaderStrategy cpfReader = new
        ChainedPropertyFileReaderStrategy(cpfFile);
    
    // Verify that the contents of layer1 has been read
    assertThat(cpfReader.getValue("SKYCAVE_PROPERTY1"), is("wingdings"));
    // Verify that layer1 properties take precedence over chained layers
    assertThat(cpfReader.getValue(Config.SKYCAVE_APPSERVER), is("10.11.85.66:37123"));
    
    // Verify that chained layer properties are defined
    assertThat(cpfReader.getValue(Config.SKYCAVE_SUBSCRIPTION_IMPLEMENTATION), 
        is("cloud.cave.doubles.TestStubSubscriptionService"));
    assertThat(cpfReader.getValue(Config.SKYCAVE_WEATHERSERVER), 
        is("localhost:8281"));
  }
  
  @Test
  public void shouldFailOnBrokenChains() throws IOException {
    File cpfFile = new File("test/testcases/layerbroken.cpf");
    
    try {
      new ChainedPropertyFileReaderStrategy(cpfFile);
      fail("No exception was thrown!");
    } catch (CaveConfigurationNotSetException e) {
      assertThat(e.getMessage(), containsString("The CPF file 'nonexist.cpf' is missing"));
      
    }
  }
  
  @Test
  public void shouldHandleCPFsWithFilenameWithDashInThem() throws IOException {
    // The latest reads TWO cpfs, 
    // < test/testcases/root-file.cpf
    // < test/testcases/base.cpf
    // in root-file, the port is 37145 while in base it is 37123
    // Thus 37123 must take precedens as it is read last
    File cpfFile = new File("test/testcases/latest.cpf");

    ChainedPropertyFileReaderStrategy cpfReader = new
        ChainedPropertyFileReaderStrategy(cpfFile);

    assertThat(cpfReader.getValue(Config.SKYCAVE_APPSERVER), is("localhost:37123"));

    // Verify that the contents of layer1 has been read
    assertThat(cpfReader.getValue("SKYCAVE_PROPERTY1"), is("wingdings"));

  }

}
