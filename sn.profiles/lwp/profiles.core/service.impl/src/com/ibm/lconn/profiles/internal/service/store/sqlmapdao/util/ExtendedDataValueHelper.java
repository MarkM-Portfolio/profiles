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
package com.ibm.lconn.profiles.internal.service.store.sqlmapdao.util;

import java.sql.SQLException;

import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;

/**
 * Special conversion class to handle converting of empty string to byte array[].
 * 
 * @author ahernm@us.ibm.com
 *
 */
public class ExtendedDataValueHelper implements TypeHandlerCallback 
{
	public Object getResult(ResultGetter getter)
		throws SQLException 
	{
		return getter.getBytes();
	}

	public void setParameter(ParameterSetter setter, Object parameter)
		throws SQLException 
	{
		if(parameter != null) {
			byte[] bytes = (byte[]) parameter;
			setter.setBytes(bytes);
		}
	}
	
	public Object valueOf(String value) 
	{
		if (value == null)
			return null;
		
		return value.getBytes();
	}

}
