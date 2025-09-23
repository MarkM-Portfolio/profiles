/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.population.tool;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.ws.sdo.mediator.jdbc.queryengine.Argument;

/**
 * A tool to generate a multi-tenant directory
 */
public class DirectoryGenerator
{

  class InputData
  {
    Map<String, String> properties;

    PersonAccount sysAdmin;

    List<PersonAccount> personAccounts;
  }

  private static Map<String, String> parseArguments(String[] arguments)
  {
    Map<String, String> argumentMap = new HashMap<String, String>(2);
    for (String argument : arguments)
    {
      if (argument.startsWith(Arguments.INPUT))
      {
        String value = argument.substring(Arguments.INPUT.length());
        argumentMap.put(Arguments.INPUT, value);
      }
      else if (argument.startsWith(Arguments.OUTPUT))
      {
        String value = argument.substring(Arguments.OUTPUT.length());
        argumentMap.put(Arguments.OUTPUT, value);
      }
      else if (argument.startsWith(Arguments.MODE))
      {
        String value = argument.substring(Arguments.MODE.length());
        argumentMap.put(Arguments.MODE, value);
      }
    }
    return argumentMap;
  }

  private static DirectoryGeneratorInputData parseInputFile(File inputFile) throws Exception
  {
    DirectoryGeneratorInputData result = new DirectoryGeneratorInputData();

    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setNamespaceAware(false);

    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    Document document = documentBuilder.parse(inputFile);

    XPathFactory xpathFactory = XPathFactory.newInstance();
    XPath xpath = xpathFactory.newXPath();

    // parse input property elements into memory
    NodeList propertyNodes = (NodeList) xpath.evaluate("//property", document, XPathConstants.NODESET);
    for (int i = 0; i < propertyNodes.getLength(); i++)
    {
      Node propertyNode = propertyNodes.item(i);
      String name = (String) xpath.evaluate("@key", propertyNode, XPathConstants.STRING);
      String value = (String) xpath.evaluate("@value", propertyNode, XPathConstants.STRING);
      result.getProperties().put(name, value);
    }

    // parse sys account
    Node sysAdminNode = (Node) xpath.evaluate("//sysAdmin", document, XPathConstants.NODE);
    if (sysAdminNode != null)
    {
      PersonAccount sysAdmin = new PersonAccount();
      sysAdmin = new PersonAccount();
      sysAdmin.setUid((String) xpath.evaluate("@uid", sysAdminNode, XPathConstants.STRING));
      sysAdmin.setCn((String) xpath.evaluate("@cn", sysAdminNode, XPathConstants.STRING));
      sysAdmin.setSn((String) xpath.evaluate("@sn", sysAdminNode, XPathConstants.STRING));
      result.setSysAdmin(sysAdmin);
    }

    // parse person account pool
    NodeList personAccountNodes = (NodeList) xpath.evaluate("//personAccount", document, XPathConstants.NODESET);
    for (int i = 0; i < personAccountNodes.getLength(); i++)
    {
      Node personAccountNode = personAccountNodes.item(i);
      PersonAccount personAccount = new PersonAccount();
      personAccount.setUid((String) xpath.evaluate("@uid", personAccountNode, XPathConstants.STRING));
      personAccount.setCn((String) xpath.evaluate("@cn", personAccountNode, XPathConstants.STRING));
      personAccount.setSn((String) xpath.evaluate("@sn", personAccountNode, XPathConstants.STRING));
      result.getPersonAccounts().add(personAccount);
    }

    return result;
  }

  public static void main(String[] args) throws Exception
  {
    // parse arguments -input=<inputFile> -output=<outputPath>
    Map<String, String> argumentMap = parseArguments(args);

    // validate we have valid arguments
    File inputFile = null;
    File outputFolder = null;
    String mode = argumentMap.get(Arguments.MODE);
    if (mode == null || mode.length() == 0)
    {
      mode = Arguments.MODE_FILE_REGISTRY;
    }

    if (argumentMap.containsKey(Arguments.INPUT))
    {
      String inputFilePath = argumentMap.get(Arguments.INPUT);
      inputFile = new File(inputFilePath);
    }
    if (argumentMap.containsKey(Arguments.OUTPUT))
    {
      String outputPath = argumentMap.get(Arguments.OUTPUT);
      outputFolder = new File(outputPath);
    }
    if (inputFile == null)
    {
      System.out.println("Missing argument: -input=<inputFile.xml>");
      System.exit(0);
    }
    if (!inputFile.exists())
    {
      System.out.println("Input file does not exist:" + inputFile.getAbsolutePath());
      System.exit(0);
    }
    if (outputFolder == null)
    {
      System.out.println("Missing argument: -output=<pathToOutputFolder>");
      System.exit(0);
    }
    if (!outputFolder.isDirectory())
    {
      System.out.println("Output path is not a folder:" + outputFolder.getAbsolutePath());
      System.exit(0);
    }

    // parse the input file data
    DirectoryGeneratorInputData inputData = parseInputFile(inputFile);

    // get property values
    int numTenants = Integer.parseInt((String) inputData.getProperties().get(DirectoryGeneratorInputData.PROPERTY_NUM_TENANTS));
    int maxPerson = Integer.parseInt((String) inputData.getProperties().get(
        DirectoryGeneratorInputData.PROPERTY_MAX_PERSON_ACCOUNT_PER_TENANT));
    int minPerson = Integer.parseInt((String) inputData.getProperties().get(
        DirectoryGeneratorInputData.PROPERTY_MIN_PERSON_ACCOUNT_PER_TENANT));
    String orgCnPrefix = (String) inputData.getProperties().get(DirectoryGeneratorInputData.PROPERTY_ORG_CN_PREFIX);
    String orgOPrefix = (String) inputData.getProperties().get(DirectoryGeneratorInputData.PROPERTY_ORG_O_PREFIX);
    String password = (String) inputData.getProperties().get(DirectoryGeneratorInputData.PROPERTY_PASSWORD);
    String orgUrl = (String) inputData.getProperties().get(DirectoryGeneratorInputData.PROPERTY_ORG_URL);

    if (maxPerson <= minPerson)
    {
      System.out.println("Max person must be greater than minPerson");
    }

    // random number generator
    Random random = new Random();
    int range = maxPerson - minPerson;

    // how many ids do we generate names from?
    int userPoolSize = inputData.getPersonAccounts().size();

    // build the writer
    DirectoryGeneratorWriter writer = new FileRegistryGeneratorWriter();
    writer.init(outputFolder, password);
    writer.start();

    // write the sysadmin at top for ease
    writer.writePersonAccount(inputData.getSysAdmin());

    System.out.println("NUMBER OF TENANTS TO GENERATE:" + numTenants);
    System.out.println("RANGE OF PEOPLE PER TENANT: " + minPerson + "-" + maxPerson);

    int totalPersonAccounts = 0;

    final String tenantVar = "{tenant}";
    
    // run algorithm to generate
    for (int i = 0; i < numTenants; i++)
    {
      // write the tenant information
      OrganizationAccount tenant = new OrganizationAccount();
      tenant.setCn(orgCnPrefix + "." + i);
      tenant.setO(orgOPrefix + "." + i);                                    
      tenant.setUrl(orgUrl.replace("{tenant}", tenant.getO()));
      tenant.setSaasOrganizationId(UUID.randomUUID().toString());
      writer.writeOrganizationAccount(tenant);

      // write the person accounts for tenant
      int numPersonAccounts = random.nextInt(range) + minPerson;
      // index into user pool
      int userPoolIndex = 0;
      // number of times we have gone through the pool
      int userPoolCounter = 1;

      System.out.println("NUMBER OF PEOPLE IN TENANT[" + i + "]:" + numPersonAccounts);

      for (int j = 0; j < numPersonAccounts; j++)
      {
        PersonAccount personAccount = inputData.getPersonAccounts().get(userPoolIndex);
        PersonAccount newPerson = new PersonAccount();
        newPerson.setCn(personAccount.getCn() + "." + tenant.getCn() + "." + userPoolCounter);
        newPerson.setSn(personAccount.getSn() + "." + tenant.getCn() + "." + userPoolCounter);
        newPerson.setUid(personAccount.getUid() + "." + i + "." + userPoolCounter);
        newPerson.setSaasOrgId(tenant.getSaasOrganizationId());
        newPerson.setMail(newPerson.getUid() + "@" + tenant.getUrl());
        newPerson.setSaasUserAccountId(UUID.randomUUID().toString());
        
        writer.writePersonAccount(newPerson);

        // do not exceed the size of the input data
        userPoolIndex++;
        if (userPoolIndex >= userPoolSize)
        {
          userPoolIndex = 0;
          userPoolCounter++;
        }
        totalPersonAccounts++;
      }
    }

    writer.end();

    System.out.println("TOTAL PERSON ACCOUNTS:" + totalPersonAccounts);
  }
}
