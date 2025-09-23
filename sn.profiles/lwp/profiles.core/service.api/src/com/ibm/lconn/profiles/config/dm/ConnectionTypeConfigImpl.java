/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.config.dm;

import com.ibm.lconn.profiles.config.AbstractConfigObject;

public class ConnectionTypeConfigImpl extends AbstractConfigObject implements ConnectionTypeConfig
{

  private static final long serialVersionUID = 1L;

  private String type;
  private WorkflowEnum workflow = WorkflowEnum.NONE;
  private GraphEnum graph = GraphEnum.DIRECTIONAL;
  private NodeOfCreator nodeOfCreator = NodeOfCreator.SOURCE;
  private MessageAclEnum messageAcl = MessageAclEnum.SOURCE;
  private String notificationType;
  private boolean indexed = true;
  private boolean extension = false;
  
  public String getType()
  {
    return type;
  }
  public void setType(String type)
  {
    this.type = type;
  }
  public WorkflowEnum getWorkflow()
  {
    return workflow;
  }
  public void setWorkflow(WorkflowEnum workflow)
  {
    this.workflow = workflow;
  }
  public GraphEnum getGraph()
  {
    return graph;
  }
  public void setGraph(GraphEnum graph)
  {
    this.graph = graph;
  }
  public NodeOfCreator getNodeOfCreator()
  {
    return nodeOfCreator;
  }
  public void setNodeOfCreator(NodeOfCreator n)
  {
    this.nodeOfCreator = n;
  }
  public String getNotificationType()
  {
    return notificationType;
  }
  public void setNotificationType(String notificationType)
  {
    this.notificationType = notificationType;
  }
  public boolean isIndexed()
  {
    return indexed;
  }
  public void setIndexed(boolean indexed)
  {
    this.indexed = indexed;
  } 
  public boolean isExtension()
  {
	  return extension;
  }
  public void setExtension(boolean extension)
  {
	  this.extension = extension;
  }
  public MessageAclEnum getMessageAcl() {
	  return messageAcl;
  }
  public void setMessageAcl(MessageAclEnum messageAcl) {
	  this.messageAcl = messageAcl;	  
  }
  public String toString() {
    StringBuilder result = new StringBuilder().append("[");
    result.append(" type=").append(type);
    result.append(" extension=").append(extension);
    result.append(" indexed=").append(indexed);
    result.append(" graph=").append(graph);
    result.append(" nodeOfCreator=").append(nodeOfCreator);
    result.append(" notificationType=").append(notificationType);
    result.append(" messageAcl=").append(messageAcl);
    result.append(" workflow=").append(workflow).append("]");
    return result.toString();    
  }
}