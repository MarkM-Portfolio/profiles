/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014                                          */
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
import com.ibm.lconn.profiles.internal.data.profile.UserMode;

public class UserModeHandler implements TypeHandlerCallback {

	public Object getResult(ResultGetter getter) throws SQLException {
		return UserMode.fromCode(getter.getInt());
	}

	public void setParameter(ParameterSetter setter, Object parameter) throws SQLException {
		if(parameter != null) {
			UserMode m = (UserMode)parameter;
            setter.setInt(m.getCode());
		}
	}

	public Object valueOf(String value) {
		int code = NumberUtils.toInt(value,Integer.MIN_VALUE);
		if (code == Integer.MIN_VALUE){
			throw new IllegalArgumentException("Illegal user state code value: " + value);
		}
		return UserMode.fromCode(code);
	}
}
