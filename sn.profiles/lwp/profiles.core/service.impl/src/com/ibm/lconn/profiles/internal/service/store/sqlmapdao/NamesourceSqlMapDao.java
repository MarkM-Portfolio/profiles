/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service.store.sqlmapdao;

import java.sql.SQLException;

import org.apache.commons.lang.math.NumberUtils;

import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;
import com.ibm.lconn.profiles.data.AbstractName.NameSource;

/**
 * Special conversion class to handle converting of integer to enum namesource value
 * 
 * @author annette_s_riffe@us.ibm.com
 *
 */
public class NamesourceSqlMapDao implements TypeHandlerCallback 
{
	public Object getResult(ResultGetter getter)
		throws SQLException 
	{
        return NameSource.getNameSourceByCode(getter.getInt());
	}

	public void setParameter(ParameterSetter setter, Object parameter)
		throws SQLException 
	{
		if(parameter != null) {
            NameSource n = (NameSource)parameter;
            setter.setInt(n.getCode());
		}
	}
	
	public Object valueOf(String value) 
	{
		int code = NumberUtils.toInt(value,Integer.MIN_VALUE);
		
		if (code == Integer.MIN_VALUE)
			throw new IllegalArgumentException("Illegal name source code value: " + value);
		
		return NameSource.getNameSourceByCode(code);
	}
}
