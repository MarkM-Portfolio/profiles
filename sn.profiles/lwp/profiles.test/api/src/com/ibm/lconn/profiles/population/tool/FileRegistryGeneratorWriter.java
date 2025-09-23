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
import java.io.FileOutputStream;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.apache.abdera.parser.stax.StaxStreamWriter;
import org.apache.abdera.writer.StreamWriter;

public class FileRegistryGeneratorWriter extends DirectoryGeneratorWriter implements Constants
{

  private File fileRegistry;

  private StreamWriter writer;

  private String password;

  @Override
  public void init(File output, String password) throws Exception
  {
    super.init(output, password);
    // the common password
    this.password = password;
    // create the fileRegisty.xml file on disk
    fileRegistry = new File(output, "fileRegistry.xml");
    if (fileRegistry.exists())
    {
      fileRegistry.delete();
    }
  }

  @Override
  public void writeOrganizationAccount(OrganizationAccount organizationAccount)
  {
    super.writeOrganizationAccount(organizationAccount);
    
    String parent = "o=defaultWIMFileBasedRealm";
    String externalId = organizationAccount.getSaasOrganizationId();
    String externalName = "o=" + organizationAccount.getO() + "," + parent;
    String uniqueId = externalId;
    String uniqueName = externalName;

    String policyId = organizationAccount.getO() + "-" + "policy";

    // organization
    writer.startElement(ENTITIES);
    writer.writeAttribute(TYPE, "wim:OrgContainer");
    writer.startElement(IDENTIFIER);
    writer.writeAttribute(EXTERNAL_ID, externalId);
    writer.writeAttribute(EXTERNAL_NAME, externalName);
    writer.writeAttribute(UNIQUE_ID, uniqueId);
    writer.writeAttribute(UNIQUE_NAME, uniqueName);
    writer.endElement();
    writer.startElement(PARENT);
    writer.startElement(IDENTIFIER);
    writer.writeAttribute(UNIQUE_NAME, parent);
    writer.endElement();
    writer.endElement();
    writer.startElement(O).writeElementText(organizationAccount.getO()).endElement();
    writer.startElement(CN).writeElementText(organizationAccount.getCn()).endElement();
    writer.startElement(IBM_VMM_ORG_POLICY_ID).writeElementText(policyId).endElement();
    writer.startElement(IBM_SAAS_ORGANIZATION_ID).writeElementText(uniqueId).endElement();
    writer.startElement(IBM_SAAS_ORGANIZATION_URL).writeElementText(organizationAccount.getUrl()).endElement();
    writer.endElement();

    externalId = UUID.randomUUID().toString();
    externalName = "uid=" + policyId + "," + parent;
    uniqueId = externalId;
    uniqueName = externalName;
    // policy
    writer.startElement(ENTITIES);
    writer.writeAttribute(TYPE, "ic:OrgPolicy");
    writer.startElement(IDENTIFIER);
    writer.writeAttribute(EXTERNAL_ID, externalId);
    writer.writeAttribute(EXTERNAL_NAME, externalName);
    writer.writeAttribute(UNIQUE_ID, uniqueId);
    writer.writeAttribute(UNIQUE_NAME, uniqueName);
    writer.endElement();
    writer.startElement(PARENT);
    writer.startElement(IDENTIFIER);
    writer.writeAttribute(UNIQUE_NAME, parent);
    writer.endElement();
    writer.endElement();
    writer.startElement(UID).writeElementText(policyId).endElement();
    writer.startElement(IBM_SAAS_SHARING_INTENT).writeElementText("1").endElement();
    writer.startElement(IBM_SAAS_HAS_GROUPS).writeElementText("false").endElement();
    writer.endElement();

  }

  @Override
  public void writePersonAccount(PersonAccount personAccount)
  {
    String parent = "o=defaultWIMFileBasedRealm";
    String externalId = UUID.randomUUID().toString();
    String externalName = "uid=" + personAccount.getUid() + "," + parent;
    String uniqueId = externalId;
    String uniqueName = externalName;

    writer.startElement(ENTITIES);
    writer.writeAttribute(TYPE, "wim:PersonAccount");
    writer.startElement(IDENTIFIER);
    writer.writeAttribute(EXTERNAL_ID, externalId);
    writer.writeAttribute(EXTERNAL_NAME, externalName);
    writer.writeAttribute(UNIQUE_ID, uniqueId);
    writer.writeAttribute(UNIQUE_NAME, uniqueName);
    writer.endElement();
    writer.startElement(PARENT);
    writer.startElement(IDENTIFIER);
    writer.writeAttribute(UNIQUE_NAME, parent);
    writer.endElement();
    writer.endElement();
    writer.startElement(PASSWORD).writeElementText(password).endElement();
    writer.startElement(UID).writeElementText(personAccount.getUid()).endElement();
    writer.startElement(CN).writeElementText(personAccount.getCn()).endElement();
    writer.startElement(SN).writeElementText(personAccount.getSn()).endElement();
    writer.startElement(MAIL).writeElementText(personAccount.getMail()).endElement();
    writer.startElement(IBM_SAAS_USER_ACCOUNT_ID).writeElementText(personAccount.getSaasUserAccountId()).endElement();
    writer.startElement(IBM_SAAS_ORGANIZATION_ID).writeElementText(personAccount.getSaasOrgId()).endElement();
    writer.startElement(IBM_SAAS_MULTI_TENANCY_ID).writeElementText(personAccount.getSaasOrgId()).endElement();
    writer.startElement(IBM_SAAS_PRIMARY_ORGANIZATION_ID).writeElementText(personAccount.getSaasOrgId()).endElement();
    writer.endElement();
  }

  @Override
  public void start() throws Exception
  {
    super.start();
    
    // initialize a stream-writer to the file
    FileOutputStream fileOutputStream = new FileOutputStream(fileRegistry);
    writer = new StaxStreamWriter(fileOutputStream);
    writer.setAutoIndent(true);

    writer.startDocument();
    writer.startElement(DATAGRAPH);
    writer.writeNamespace(SDO_PREFIX, SDO_NSURI);
    writer.writeNamespace(XSI_PREFIX, XSI_NSURI);
    writer.writeNamespace(WIM_PREFIX, WIM_NSURI);
    writer.writeNamespace(IC_PREFIX, IC_NSURI);
    writer.startElement(ROOT);

  }

  @Override
  public void end() throws Exception
  {
    super.end();
    
    // root
    writer.endElement();
    // datagraph
    writer.endElement();

    writer.endDocument();
    writer.close();
  }

  public static void main(String[] args) throws Exception
  {
    PersonAccount person = new PersonAccount();
    person.setCn("Derek Carr");
    person.setSn("Carr");
    person.setUid("dwcarr");
    person.setSaasOrgId("myOrgId");
    FileRegistryGeneratorWriter writer = new FileRegistryGeneratorWriter();
    writer.init(new File("c:\\dev\\temp"), "U0hBLTE6am8wY3l5Y2ozbXJ5OldDczBJamF2dThxZkdRNy81b3RVdmRvSHlDND0NCg==");
    writer.start();
    writer.writePersonAccount(person);
    writer.end();
  }
}
