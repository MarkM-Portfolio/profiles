/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.config.dm;

import java.util.Locale;

import com.ibm.lconn.profiles.config.BaseConfigObject;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;

public interface ConnectionTypeConfig extends BaseConfigObject
{
	public enum MessageAclEnum {
		PUBLIC("public"), PRIVATE("private"), SOURCE("source"), TARGET("target");
		private String name;

		private MessageAclEnum(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public static MessageAclEnum byName(String value) {
			for (MessageAclEnum v : MessageAclEnum.values()) {
				if (v.getName().equals(value)) {
					return v;
				}
			}
			return null;
		}

		public String toString() {
			return name;
		}
	}
	
  public enum GraphEnum {

    BIDIRECTIONAL("bidirectional"), DIRECTIONAL("directional");

    private String name;

    private GraphEnum(String name)
    {
      this.name = name;
    }

    public String getName()
    {
      return name;
    }

    public static GraphEnum byName(String value)
    {
      for (GraphEnum v : GraphEnum.values())
      {
        if (v.getName().equals(value))
        {
          return v;
        }
      }
      return null;
    }

    public String toString()
    {
      return name;
    }
  }
  
  public enum WorkflowEnum {

      CONFIRMED("confirmed"), NONE("none");

      private String name;
      
      private WorkflowEnum(String name) {
          this.name = name;
      }

      public String getName() {
          return name;
      }

      public static WorkflowEnum byName(String value) {
          for (WorkflowEnum v : WorkflowEnum.values()) {
              if (v.getName().equals(value)) {
                  return v;
              }
          }
          return null;
      }

      public String toString() {
          return name;
      }
  }

  public enum NodeOfCreator {
	SOURCE("source"), TARGET("target");
	
	private String name;
	
	private NodeOfCreator(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
    public static NodeOfCreator byName(String value) {
        for (NodeOfCreator v : NodeOfCreator.values()) {
            if (v.getName().equals(value)) {
                return v;
            }
        }
        return null;
    }

    public String toString() {
        return name;
    }	
  }
  
  
  public enum IndexAttributeForConnection {
	  TARGET_USER_DISPLAY_NAME,
	  TARGET_USER_UID;

	  public static final String getIndexFieldName(
				IndexAttributeForConnection i, String connectionType) {
			StringBuilder result = new StringBuilder("FIELD_CONNECTIONS_");
			result.append(connectionType.toUpperCase(Locale.ENGLISH));
			if (TARGET_USER_DISPLAY_NAME.equals(i)) {
				result.append("_FIELD");
			} else if (TARGET_USER_UID.equals(i)) {
				result.append("_UID_FIELD");
			} else {
				throw new ProfilesRuntimeException();
			}
			return result.toString();
		}
  }
  
  /**
   * The string used to identify the type of network in the DB.
   * @return
   */
  public String getType();
  
  /**
   * The type of workflow that governs the connection resource.
   * @return
   */
  public WorkflowEnum getWorkflow();
  
  /**
   * The type of graph that defines the network behavior.
   */
  public GraphEnum getGraph();
  
  /**
   * Controls how access to the message on the connection is enforced.
   * @return
   */
  public MessageAclEnum getMessageAcl();
  
  /**
   * The node in the edge that the creator is restricted to instantiating.
   * @return
   */
  public NodeOfCreator getNodeOfCreator();
  
  /**
   * If this connection results in a notification, this is the type of notification to send.
   * @return
   */
  public String getNotificationType();
  
  /**
   * Flag if this type of connection is added to the search index.
   * @return
   */
  public boolean isIndexed();
  
  /**
   * True if this is an extension connection object type
   * @return
   */
  public boolean isExtension();
  
  
}
