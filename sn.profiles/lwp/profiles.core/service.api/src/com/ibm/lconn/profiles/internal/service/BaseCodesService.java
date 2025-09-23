/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2009, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ibm.lconn.profiles.data.codes.AbstractCode;

/**
 *
 */
@Service
public interface BaseCodesService<CT extends AbstractCode> {

	/**
	 * Internal class to cache codes
	 * 
	 */
	public interface CodeCache<CT> {
		public CT get(String codeId);
		public CT get(String codeId, String tenantKey);
		public void reload();
		public void disable();
		public void enable();
		public boolean isEnabled();
		public boolean isInit();
	}
	
	public CT getById(String codeId);

	public CT getById(String codeId, String tenantKey);
	
	public List<CT> getAll();
	
	public void create(CT code);
	
	public void update(CT code);
	
	public void delete(String codeId);
	
	public Class<CT> codeType();
	
	public CodeCache<CT> codeCache();
	
}
