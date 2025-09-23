/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2004, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.util;

import com.ibm.lconn.core.util.ResourceBundleHelper;
import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.peoplepages.data.Employee;

// jtw: Note during 4.5: static analysis tools complain about this class, in particular the equals() method.
// It looks like virtually none of this class is used except for formatMessageForUser, which does not
// even use the internal MessageFormat variable (_mf). My guess is this class fell into disuse, but
// the single used method was put here just to find a home. Commenting out the majority of this class
// in preparation for being deleted. Can the one method that is used be moved to another util class?
//
// this class does not derive from MessageFormat
///**
// * An LWP class derived from Java SDK MessageFormat class.  This class handles escaping of single quotes at run-time.
// */
//
//public class MessageFormatter implements Cloneable, Serializable {
public class MessageFormatter {

//    private static final long serialVersionUID = 1L;
//
//    private MessageFormat _mf = null;
//
//	private static final char SINGLE_QUOTE = '\'';
//	private static final char CURLY_BRACE_LEFT = '{';
//	private static final char CURLY_BRACE_RIGHT = '}';
//
//	private static final int STATE_INITIAL = 0;
//	private static final int STATE_SINGLE_QUOTE = 1;
//	private static final int STATE_MSG_ELEMENT = 2;
//
//	/**
//	 * default constructor should never be called
//	 */
//	private MessageFormatter() {
//	}
//
//	/**
//	 *  non-default constructor
//	 */
//	public MessageFormatter(String pattern) {
//
//		_mf = new MessageFormat(fixPattern(pattern));
//	}
//
//	private MessageFormatter(MessageFormat mf) {
//		_mf = mf;
//	}
//
//	public Object clone() {
//		return (new MessageFormatter((MessageFormat) _mf.clone()));
//	}
//
//	void applyPattern(String newPattern) {
//		_mf.applyPattern(fixPattern(newPattern));
//	}
//
//	public boolean equals(Object obj) {
//		if ( (obj instanceof MessageFormatter) == false ){
//			return false;
//		}
//		return (_mf.equals(obj));
//	}
//
//	public StringBuffer format(
//		Object[] source,
//		StringBuffer result,
//		FieldPosition ignore) {
//		return (_mf.format(source, result, ignore));
//	}
//
//	public StringBuffer format(
//		Object source,
//		StringBuffer result,
//		FieldPosition ignore) {
//		return (_mf.format(source, result, ignore));
//	}
//
//	static String format(String pattern, Object[] arguments) {
//		return (MessageFormat.format(fixPattern(pattern), arguments));
//	}
	
	/*
	 * Utility to log user related messages
	 */
	public static final String formatMessageForUser(ProfileDescriptor desc, 
			ResourceBundleHelper rbh, String msgKey) {
		Employee emp = desc.getProfile();
		String msg = rbh.getString(
				msgKey,
				new Object[]{emp.getDisplayName(), 
							 emp.getKey(), emp.getUid(), emp.getDistinguishedName()});
		return msg;
	}

//	public Format[] getFormats() {
//		return (_mf.getFormats());
//	}
//
//	public Locale getLocale() {
//		return (_mf.getLocale());
//	}
//
//	public int hashCode() {
//		return (_mf.hashCode());
//	}
//
//	public Object[] parse(String source) throws ParseException {
//		return (_mf.parse(source));
//	}
//
//	public Object[] parse(String source, ParsePosition status) {
//		return (_mf.parse(source, status));
//	}
//
//	public Object parseObject(String text, ParsePosition status) {
//		return (_mf.parseObject(text, status));
//	}
//
//	public void setFormat(int variable, Format newFormat) {
//		_mf.setFormat(variable, newFormat);
//	}
//
//	public void setFormats(Format[] newFormats) {
//		_mf.setFormats(newFormats);
//	}
//
//	public void setLocale(Locale theLocale) {
//		_mf.setLocale(theLocale);
//	}
//
//	public String toPattern() {
//		return (_mf.toPattern());
//	}
//
//	public static String fixPattern(String pattern) {
//		StringBuffer buf = new StringBuffer(pattern.length() * 2);
//		int state = STATE_INITIAL;
//		int i = 0;
//		int j = pattern.length();
//		while (i < j) {
//			char c = pattern.charAt(i);
//			switch (state) {
//
//				case STATE_INITIAL :
//					switch (c) {
//						case SINGLE_QUOTE :
//							state = STATE_SINGLE_QUOTE;
//							// If '}' or '{', don't replace with double quotes
//							if (i + 2 < j) {
//								char cnext = pattern.charAt(i + 1);
//								if (cnext == CURLY_BRACE_LEFT
//									|| cnext == CURLY_BRACE_RIGHT) {
//									char cnextnext = pattern.charAt(i + 2);
//									if (cnextnext == SINGLE_QUOTE) {
//										buf.append(SINGLE_QUOTE);
//										buf.append(cnext);
//										c = SINGLE_QUOTE;
//										// Will get added at end of this switch block.
//										i += 2;
//										state = STATE_INITIAL;
//									}
//								}
//							}
//
//							break;
//						case CURLY_BRACE_LEFT :
//							state = STATE_MSG_ELEMENT;
//							break;
//					}
//					break;
//
//				case STATE_SINGLE_QUOTE :
//					switch (c) {
//						case SINGLE_QUOTE :
//							state = STATE_INITIAL;
//							break;
//						default :
//							buf.append(SINGLE_QUOTE);
//							state = STATE_INITIAL;
//					}
//					break;
//
//				case STATE_MSG_ELEMENT :
//					switch (c) {
//						case CURLY_BRACE_RIGHT :
//							state = STATE_INITIAL;
//							break;
//					}
//					break;
//
//				default : // This should not happen
//			}
//
//			buf.append(c);
//			i++;
//		}
//		// End of scan
//		if (state == STATE_SINGLE_QUOTE) {
//			buf.append(SINGLE_QUOTE);
//		}
//		return new String(buf);
//	}

}
