/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2006, 2020                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.webui.actions;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.core.image.ImageResizer;
import com.ibm.lconn.core.io.LConnIOUtils;
import com.ibm.lconn.profiles.internal.service.PhotoService;
import com.ibm.lconn.profiles.web.actions.BaseAction;
import com.ibm.peoplepages.webui.actions.TempPhotoHelper.TempFile;

//import javax.activation.MimetypesFileTypeMap;

public class TempPhotoViewAction extends BaseAction 
{
	
	protected ActionForward doExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
	throws Exception 
	{
		TempFile tempFile = TempPhotoHelper.getTempFile(request);

		// allows users to only access their own files
		if (tempFile != null)
		{
			response.reset();
			response.setContentType("image/jpeg");

			response.setHeader("Expires", "Thurs, 1 Jan 1970 00:00:00 GMT");
			response.setHeader("Cache-Control", "no-cache, must-revalidate");
			response.setHeader("Pragma", "no-cache");
			//response.setHeader("Content-disposition", "attachment; filename=" + fileName);
			response.setDateHeader("Last-Modified", tempFile.getLastMod());
			
			ServletOutputStream sos = response.getOutputStream();
		
			if (request.getParameter("resize") != null && 
					request.getParameter("resize").equals("true")) {
				int xSize = PhotoService.SMALL_PHOTO_SIZE;
				int ySize = PhotoService.SMALL_PHOTO_SIZE;
				try {		
				  xSize = Math.min(PhotoService.LARGE_PHOTO_SIZE, Integer.parseInt(request.getParameter("xsize")));
				  ySize = Math.min(PhotoService.LARGE_PHOTO_SIZE, Integer.parseInt(request.getParameter("ysize")));

                  								
				  ByteArrayOutputStream os = new ByteArrayOutputStream();
				  ImageResizer.resizeImage(tempFile.getFileHandle(), os, ySize, xSize, PhotoService.SMALL_PHOTO_SIZE, PhotoService.SMALL_PHOTO_SIZE, 
						ImageResizer.DEFAULT_COLOR, BufferedImage.TYPE_INT_RGB);
				
				  byte[] bytes = os.toByteArray();
				  response.setIntHeader("Content-Length", bytes.length);
				  sos.write(bytes);
				}
                catch (NumberFormatException e) {
					// Do nothing: this try/catch is meant to catch hacked xSize/ySize request parms and 
                    // avoid rendering WAS error stack to the user
				}	
			}
			else {
				response.setIntHeader("Content-Length", (int) tempFile.getFileHandle().getFileSize());
				LConnIOUtils.copyFlushClose(sos, tempFile.getFileHandle().open());
			}
		
			sos.close();
		}
		return null;
	}

	protected long getLastModified(HttpServletRequest request) throws Exception 
	{
		TempFile tempFile = TempPhotoHelper.getTempFile(request);
		
		if (tempFile != null) return tempFile.getLastMod();
		else return UNDEF_LASTMOD;
	}
}
