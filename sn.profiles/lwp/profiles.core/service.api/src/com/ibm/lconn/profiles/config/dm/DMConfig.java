/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.config.dm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.ibm.lconn.profiles.config.AbstractConfigObject;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.dm.ConnectionTypeConfig.GraphEnum;
import com.ibm.lconn.profiles.config.dm.ConnectionTypeConfig.MessageAclEnum;
import com.ibm.lconn.profiles.config.dm.ConnectionTypeConfig.NodeOfCreator;
import com.ibm.lconn.profiles.config.dm.ConnectionTypeConfig.WorkflowEnum;
import com.ibm.lconn.profiles.config.types.ExtensionType;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.lconn.profiles.resources.SvcApiRes;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

/**
 * This object is responsible for holding configuration data for two areas.
 * 
 * It handles the declaration of global extension attributes. Extension attributes may get aggregrated into a concrete
 * <code>ProfileType</code> definition and surfaced in the application.
 * 
 * It handles the declaration of global draftable attributes. A draftable attribute is any attribute whose change may get synched back to
 * the corporate directory via Profiles TDI.
 * 
 */
public final class DMConfig extends AbstractConfigObject
{

  private static final long serialVersionUID = 1L;

  private static final Logger logger = Logger.getLogger(DMConfig.class.getName(), SvcApiRes.BUNDLE);

  private final HierarchicalConfiguration dmConfig;

  private final Map<String, ExtensionType> extensionPropertiesToType;
  
  private final Map<String, ExtensionAttributeConfig> extensionAttributeConfig;

  private final Map<ExtensionType, Set<String>> extensionIdsByType;

  private final Set<String> draftableAttributes;

  private final boolean forTdi;

  private Map<String, ConnectionTypeConfig> connectionTypeConfigs;
  
  private Map<String, SearchAttributeConfig> searchAttributeConfigs;
  
  private Map<String, TagConfig> tagConfigs;
  
  /**
   * Performs initial initialization of data-model objects based on config file. Defers completion until API and UI models are initialized.
   * 
   * @param profilesConfig
   */
  public DMConfig(HierarchicalConfiguration profilesConfig, boolean forTdi)
  {
    if (logger.isLoggable(Level.FINER))
    {
      logger.entering(getClass().getName(), "DMConfig(profilesConfig, forTdi)", new Object[] { profilesConfig, forTdi });
    }

    try
    {
      this.forTdi = forTdi;
      this.dmConfig = this.forTdi ? profilesConfig : (HierarchicalConfiguration) profilesConfig.subset("profileDataModels");

      // build the tag types      
      tagConfigs = new HashMap<String, TagConfig>();
      
      // default out of the box tag support defined here
      TagConfigImpl tagConfigImpl = new TagConfigImpl();
      tagConfigImpl.setPhraseSupported(false);
      tagConfigImpl.setType(TagConfig.DEFAULT_TYPE);
      tagConfigs.put(tagConfigImpl.getType(), tagConfigImpl);
      
      // now add any tag types from configuration
      HierarchicalConfiguration tagsConfig = (HierarchicalConfiguration) dmConfig.subset("tagsConfig");
      int maxTagConfigs = tagsConfig.getMaxIndex("tagConfig");
      for (int i=0; i <= maxTagConfigs; i++) {
    	  HierarchicalConfiguration tagConfig = (HierarchicalConfiguration) tagsConfig.subset("tagConfig(" + i + ")");    	  
    	  TagConfigImpl extension = new TagConfigImpl();
    	  String type = tagConfig.getString("[@type]");
    	  boolean phraseSupported = tagConfig.getBoolean("[@phraseSupported]", false);    	  
    	  extension.setType(type);
    	  extension.setPhraseSupported(phraseSupported);
    	            
          // validation that we can support this permutation
          if (tagConfigs.get(extension.getType()) != null) {
          	logger.log(Level.SEVERE, "ERROR - duplicate tag configurations where type=" + extension.getType());
          	throw new Exception();
          }
          
          if (extension.getType() == null || extension.getType().length() == 0) {
        	logger.log(Level.SEVERE, "ERROR - extension tag configuration with missing type label found in configuration.");
        	throw new Exception();
          }
          
          tagConfigs.put(extension.getType(), extension);
      }
      
      // log tag information
      if (logger.isLoggable(Level.INFO))
      {
        logger.log(Level.INFO, "Profile Tag Types");
        for (TagConfig tagConfig : tagConfigs.values())
        {
          logger.log(Level.INFO, "- tag " + tagConfig);
        }
      }      
      
      // build the default connection types
      connectionTypeConfigs = new HashMap<String, ConnectionTypeConfig>();
      ConnectionTypeConfigImpl colleagueConnection = new ConnectionTypeConfigImpl();
      colleagueConnection.setGraph(GraphEnum.BIDIRECTIONAL);
      colleagueConnection.setIndexed(true);
      colleagueConnection.setNotificationType("notify");
      colleagueConnection.setType(PeoplePagesServiceConstants.COLLEAGUE);
      colleagueConnection.setWorkflow(WorkflowEnum.CONFIRMED);
      colleagueConnection.setExtension(false);
      colleagueConnection.setNodeOfCreator(NodeOfCreator.TARGET);
      colleagueConnection.setMessageAcl(MessageAclEnum.SOURCE);
      connectionTypeConfigs.put(colleagueConnection.getType(), colleagueConnection);      
      // now add any connections from configuration
      HierarchicalConfiguration connectionTypes = (HierarchicalConfiguration) dmConfig.subset("connectionTypeConfig");      
      int maxConnections = connectionTypes.getMaxIndex("connection");
      for (int i=0; i <= maxConnections; i++) {
        HierarchicalConfiguration connectionConfig = (HierarchicalConfiguration) connectionTypes.subset("connection(" + i + ")");
        
        ConnectionTypeConfigImpl extensionConnection = new ConnectionTypeConfigImpl();
        String graph = connectionConfig.getString("[@graph]", GraphEnum.BIDIRECTIONAL.getName());
        String nodeOfCreator = connectionConfig.getString("[@nodeOfCreator]", NodeOfCreator.SOURCE.getName());
        boolean indexed = connectionConfig.getBoolean("[@indexed]", true);
        String notificationType = connectionConfig.getString("[@notificationType]");
        String type = connectionConfig.getString("[@type]").trim();
        String workflow = connectionConfig.getString("[@workflow]", WorkflowEnum.NONE.getName());
        String messageAcl = connectionConfig.getString("[@messageAcl]", MessageAclEnum.SOURCE.getName());
        
        extensionConnection.setGraph(GraphEnum.byName(graph));
        extensionConnection.setIndexed(indexed);
        extensionConnection.setNodeOfCreator(NodeOfCreator.byName(nodeOfCreator));
        extensionConnection.setNotificationType(notificationType);
        extensionConnection.setType(type);
        extensionConnection.setWorkflow(WorkflowEnum.byName(workflow));
        extensionConnection.setExtension(true);
        extensionConnection.setMessageAcl(MessageAclEnum.byName(messageAcl));
        
        // validation that we can support this permutation
        if (connectionTypeConfigs.get(extensionConnection.getType()) != null) {
        	logger.log(Level.SEVERE, "ERROR - duplicate connection type configurations where type=" + extensionConnection.getType());
        	throw new Exception();
        }
        
        if (extensionConnection.getType() == null || extensionConnection.getType().length() == 0) {
        	logger.log(Level.SEVERE, "ERROR - extension connection with missing type label found in configuration.");
        	throw new Exception();
        }
                
        connectionTypeConfigs.put(extensionConnection.getType(), extensionConnection);        
      }
      
      // log connection information
      if (logger.isLoggable(Level.INFO))
      {
        logger.log(Level.INFO, "Profile Connection Types");
        for (ConnectionTypeConfig connectionType : connectionTypeConfigs.values())
        {
          logger.log(Level.INFO, "- connection " + connectionType);
        }
      }      
      
      // load the extension attribute configuration
      HierarchicalConfiguration extensionAttributes = (HierarchicalConfiguration) dmConfig.subset("profileExtensionAttributes");
      HierarchicalConfiguration profileDataModel = (HierarchicalConfiguration) dmConfig.subset("profileDataModel");

      Map<String, ExtensionAttributeConfig> configMap = new HashMap<String, ExtensionAttributeConfig>();
      Map<String, ExtensionType> extensionToType = new HashMap<String, ExtensionType>();      
      Map<ExtensionType, Set<String>> typeIdMap = new HashMap<ExtensionType, Set<String>>();
      // first handle simple attributes
      Set<String> simpleAttributes = new HashSet<String>();
      typeIdMap.put(ExtensionType.SIMPLE, simpleAttributes);
      int maxIndex = extensionAttributes.getMaxIndex("simpleAttribute");
      for (int i = 0; i <= maxIndex; i++)
      {
        HierarchicalConfiguration extAttrConfig = (HierarchicalConfiguration) extensionAttributes.subset("simpleAttribute(" + i + ")");
        SimpleExtensionAttributeConfigImpl extAttrConfigObj = new SimpleExtensionAttributeConfigImpl(extAttrConfig);
        configMap.put(extAttrConfigObj.getExtensionId(), extAttrConfigObj);
        simpleAttributes.add(extAttrConfigObj.getExtensionId());
        extensionToType.put(extAttrConfigObj.getExtensionId(), ExtensionType.SIMPLE);
      }

      Set<String> xmlAttributes = new HashSet<String>();
      typeIdMap.put(ExtensionType.XMLFILE, xmlAttributes);
      maxIndex = extensionAttributes.getMaxIndex("xmlFileAttribute");
      for (int i = 0; i <= maxIndex; i++)
      {
        HierarchicalConfiguration extAttrConfig = (HierarchicalConfiguration) extensionAttributes.subset("xmlFileAttribute(" + i + ")");
        XmlFileExtensionAttributeConfigImpl extAttrConfigObj = new XmlFileExtensionAttributeConfigImpl(extAttrConfig);
        configMap.put(extAttrConfigObj.getExtensionId(), extAttrConfigObj);
        xmlAttributes.add(extAttrConfigObj.getExtensionId());
        extensionToType.put(extAttrConfigObj.getExtensionId(), ExtensionType.XMLFILE);
      }

      Set<String> richTextAttributes = new HashSet<String>();
      typeIdMap.put(ExtensionType.RICHTEXT, richTextAttributes);
      maxIndex = extensionAttributes.getMaxIndex("richtextAttribute");
      for (int i = 0; i <= maxIndex; i++)
      {
        HierarchicalConfiguration extAttrConfig = (HierarchicalConfiguration) extensionAttributes.subset("richtextAttribute(" + i + ")");
        RichtextExtensionAttributeConfigImpl extAttrConfigObj = new RichtextExtensionAttributeConfigImpl(extAttrConfig);
        configMap.put(extAttrConfigObj.getExtensionId(), extAttrConfigObj);
        richTextAttributes.add(extAttrConfigObj.getExtensionId());
        extensionToType.put(extAttrConfigObj.getExtensionId(), ExtensionType.RICHTEXT);
      }
      this.extensionAttributeConfig = Collections.unmodifiableMap(configMap);
      this.extensionIdsByType = Collections.unmodifiableMap(typeIdMap);
      this.extensionPropertiesToType = Collections.unmodifiableMap(extensionToType);

      // load the draftable attribute configuration
      Set<String> draftAttributes = new HashSet<String>(50);
      maxIndex = profileDataModel.getMaxIndex("draftableAttribute");
      for (int i = 0; i <= maxIndex; i++)
      {
        String attrId = profileDataModel.getString("draftableAttribute(" + i + ")");
        draftAttributes.add(attrId);
      }

      maxIndex = profileDataModel.getMaxIndex("draftableExtensionAttribute");
      for (int i = 0; i <= maxIndex; i++)
      {
        String attrId = Employee.getAttributeIdForExtensionId(profileDataModel
            .getString("draftableExtensionAttribute(" + i + ").[@extensionIdRef]"));
        draftAttributes.add(attrId);
      }
      draftableAttributes = Collections.unmodifiableSet(draftAttributes);

      // build the search attribute config
      searchAttributeConfigs = new HashMap<String, SearchAttributeConfig>();
      HierarchicalConfiguration searchAttributes = (HierarchicalConfiguration) dmConfig.subset("searchAttributeConfig");
      int maxSearchAttributes = searchAttributes.getMaxIndex("searchAttribute");
      for (int i=0; i <= maxSearchAttributes; i++)
      {
    	  HierarchicalConfiguration searchAttributeConfigElement = (HierarchicalConfiguration) searchAttributes.subset("searchAttribute(" + i + ")");
    	  SearchAttributeConfigImpl searchAttributeConfig = new SearchAttributeConfigImpl();
    	  String fieldId = searchAttributeConfigElement.getString("[@fieldId]");
    	  boolean fieldSearchable = searchAttributeConfigElement.getBoolean("[@fieldSearchable]", true);
    	  boolean contentSearchable = searchAttributeConfigElement.getBoolean("[@contentSearchable]", true);
    	  boolean returnable = searchAttributeConfigElement.getBoolean("[@returnable]", true);
    	  boolean exactMatchSupported = searchAttributeConfigElement.getBoolean("[@exactMatchSupported]", false);
    	  boolean sortable = searchAttributeConfigElement.getBoolean("[@sortable]", false);
    	  boolean parametric = searchAttributeConfigElement.getBoolean("[@parametric]", false);
    	  
    	  searchAttributeConfig.setContentSearchable(contentSearchable);
    	  searchAttributeConfig.setExactMatchSupported(exactMatchSupported);
    	  searchAttributeConfig.setFieldId(fieldId);
    	  searchAttributeConfig.setFieldSearchable(fieldSearchable);
    	  searchAttributeConfig.setReturnable(returnable);
    	  searchAttributeConfig.setFieldId(fieldId);
    	  searchAttributeConfig.setFieldSearchable(fieldSearchable);
    	  searchAttributeConfig.setParametric(parametric);
    	  searchAttributeConfig.setSortable(sortable);
    	  
    	  // read facet info (if there)
    	  int maxSearchFacets = searchAttributeConfigElement.getMaxIndex("searchFacet");
    	  for (int j=0; j <= maxSearchFacets; j++) {
    		  HierarchicalConfiguration searchFacetElement = (HierarchicalConfiguration) searchAttributeConfigElement.subset("searchFacet(" + j + ")");
    		  String taxonomy = searchFacetElement.getString("[@taxonomy]", "");
    		  String association = searchFacetElement.getString("[@association]", "");
    		  String description = searchFacetElement.getString("[@description]", "");
    		  
    		  SearchFacetConfigImpl searchFacetConfig = new SearchFacetConfigImpl();
    		  searchFacetConfig.setAssociation(association);
    		  searchFacetConfig.setDescription(description);
    		  searchFacetConfig.setTaxonomy(taxonomy);
    		  
    		  searchAttributeConfig.getSearchFacetConfigs().add(searchFacetConfig);
    	  }
    	      	  
          // validation that we can support this permutation
          if (searchAttributeConfigs.get(searchAttributeConfig.getFieldId()) != null) {
          	logger.log(Level.SEVERE, "ERROR - duplicate searchAttribute in searchAttributeConfig where fieldId=" + fieldId);
          	throw new Exception();
          }    	  
    	  
    	  // map by field id
    	  searchAttributeConfigs.put(searchAttributeConfig.getFieldId(), searchAttributeConfig);
    	  
      }   
      
      // log the configuration
      if (logger.isLoggable(Level.INFO))
      {
        logger.log(Level.INFO, "Profile search attribute configuration");
        for (SearchAttributeConfig o : searchAttributeConfigs.values())
        {
          logger.log(Level.INFO, "- searchAttribute " + o);
        }
      }    
      
      
    }
    catch (Exception e)
    {
      if (logger.isLoggable(Level.SEVERE))
      {
        logger.log(Level.SEVERE, "error.initializing.extension.config", e);
      }
      throw new ProfilesRuntimeException(e);
    }
    finally
    {
      if (logger.isLoggable(Level.FINER))
      {
        logger.exiting(getClass().getName(), "DMConfig");
      }
    }
  }

  /**
   * Map of <ExtensionId,ConfigObject> values.
   * 
   * @return
   */
  public final Map<String, ? extends ExtensionAttributeConfig> getExtensionAttributeConfig()
  {
    return extensionAttributeConfig;
  }

  public final List<String> getExtensionIds(ExtensionType... extensionTypes)
  {
    List<String> resultSet = new ArrayList<String>();
    for (ExtensionType extensionType : extensionTypes)
    {
      resultSet.addAll(extensionIdsByType.get(extensionType));
    }
    return resultSet;
  }

  /**
   * Retrieve the set of attribute identifiers that are 'draftable'
   * 
   * @return
   */
  public final Set<String> getDraftableAttributes()
  {
    return draftableAttributes;
  }

  /**
   * Map of available Connection type configurations.
   * 
   * @return
   */
  public Map<String, ? extends ConnectionTypeConfig> getConnectionTypeConfigs()
  {
    return connectionTypeConfigs;
  }

  public Map<String, ? extends TagConfig> getTagConfigs() {
	 return tagConfigs;
  }
  
  public Map<String, ? extends SearchAttributeConfig> getSearchAttributeConfigs()
  {
	  return searchAttributeConfigs;
  }
  
  public Map<String, ExtensionType> getExtensionPropertiesToType()
  {
	  return extensionPropertiesToType;
  }
  
  /**
   * Syntax sugar to get copy of current DMConfig
   * 
   * @return
   */
  public final static DMConfig instance()
  {
    return ProfilesConfig.instance().getDMConfig();
  }

}
