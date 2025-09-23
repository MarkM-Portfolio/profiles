/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package org.apache.shindig.vulcanext.person.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.shindig.protocol.model.FilterOperation;
import org.apache.shindig.social.core.model.AddressImpl;
import org.apache.shindig.social.core.model.ListFieldImpl;
import org.apache.shindig.social.core.model.NameImpl;
import org.apache.shindig.social.core.model.UrlImpl;
import org.apache.shindig.social.opensocial.model.Address;
import org.apache.shindig.social.opensocial.model.ListField;
import org.apache.shindig.social.opensocial.model.Name;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.model.Url;

import com.ibm.lconn.core.appext.msgvector.data.EntryMessage;
import com.ibm.lconn.profiles.data.codes.WorkLocation;
import com.ibm.peoplepages.data.Employee;

public class VulcanPersonServiceMapper {

	static Map<Person.Field, Converter> map = new HashMap<Person.Field, Converter>(Person.Field.values().length*2);

	public static interface Converter {
		/**
		 * Sets a Person field from a Employee field
		 * 
		 * @param person Opens Social Person	
		 * @param emp Employee
		 */
		void setField(Person person, Employee emp);

		/**
		 * Returns a sort key field name
		 * @return
		 */
		String getSortKey();

		/**
		 * Returns if field supports sorting.
		 * 
		 * @return
		 */
		boolean isSortable();

		/**
		 * Returns if the field supports filtering and the particular filter operation.
		 * Not all operations may make sense for a field - like contains op for a numeric field.
		 * 
		 * @param fop
		 * @return
		 */
		boolean isFilterable(FilterOperation fop);

		/**
		 * returns Employee value for this filter field name
		 * @param employee
		 * @return
		 */
		String getFilterValue(Employee employee);
	}

	/*
	 * Initialize each mappable Person.Field enum object to to a converter object that 
	 * is specifically tailored to that field.
	 */
	static {
		map.put(Person.Field.ABOUT_ME, new Converter() {
			public void setField(Person person, Employee emp) {
				person.setAboutMe(emp.getDescription());
			}

			public boolean isSortable() {
				return true;
			}

			public boolean isFilterable(FilterOperation fop) {
				return true;
			}

			public String getSortKey() {
				return "description";
			}

			public String getFilterValue(Employee emp) {
				return emp.getDescription();
			}
		});

		map.put(Person.Field.ADDRESSES, new Converter() {
			public void setField(Person person, Employee emp) {
				WorkLocation workLoc = emp.getWorkLocation();
				if (workLoc != null) {
					Address address = new AddressImpl();
					address.setType("work"); // TODO: assume work location or leave it blank
					address.setPrimary(true);
					address.setCountry(emp.getCountryDisplayValue()); // TODO: countryCode perhaps
					// address.setLatitude(0F);
					address.setLocality(workLoc.getCity());
					// address.setLongitude(0F);
					address.setRegion(workLoc.getState());
					address.setStreetAddress(workLoc.getAddress1());
					// TODO: what about emp address2
					address.setPostalCode(workLoc.getPostalCode());
					ArrayList<Address> addresses = new ArrayList<Address>(1);
					addresses.add(address);
					person.setAddresses(addresses);
				}
			}

			public boolean isSortable() {
				return false;
			}

			public boolean isFilterable(FilterOperation fop) {
				return false;
			}

			public String getSortKey() {
				return "";
			}

			public String getFilterValue(Employee emp) {
				return null;
			}
		});

		map.put(Person.Field.DISPLAY_NAME, new Converter() {
			public void setField(Person person, Employee emp) {
				person.setDisplayName(emp.getDisplayName());
			}

			public boolean isSortable() {
				return true;
			}

			public boolean isFilterable(FilterOperation fop) {
				switch (fop) {
				case contains:
					return true;
				case equals:
					return true;
				case present:
					return true;
				case startsWith:
					return true;
				default:
					return false;
				}
			}

			public String getSortKey() {
				return "displayName";
			}

			public String getFilterValue(Employee emp) {
				return emp.getDisplayName();
			}
		});

		map.put(Person.Field.EMAILS, new Converter() {
			public void setField(Person person, Employee emp) {
				ArrayList<ListField> emails = new ArrayList<ListField>(3);
				String anEmail = emp.getEmail();
				if (anEmail != null && anEmail.length() > 0) {
					ListField email = new ListFieldImpl();
					email.setPrimary(true);
					email.setType("work"); // TODO: leave it blank ??
					email.setValue(anEmail);
					emails.add(email);
				}
				anEmail = emp.getGroupwareEmail();
				if (anEmail != null && anEmail.length() > 0) {
					ListField email = new ListFieldImpl();
					email.setPrimary(false);
					email.setType("alternate"); // TODO: leave it blank ??
					email.setValue(anEmail);
					emails.add(email);
				}
				if (emails.size() > 0) {
					person.setEmails(emails);
				}
			}

			public boolean isSortable() {
				return true;
			}

			public boolean isFilterable(FilterOperation fop) {
				switch (fop) {
				case contains:
					return true;
				case equals:
					return true;
				case present:
					return true;
				case startsWith:
					return true;
				default:
					return false;
				}
			}

			public String getSortKey() {
				return "email";
			}

			public String getFilterValue(Employee emp) {
				return emp.getEmail();
			}
		});

		map.put(Person.Field.ID, new Converter() {
			public void setField(Person person, Employee emp) {
				person.setId(emp.getUserid());
			}

			public boolean isSortable() {
				return true;
			}

			public boolean isFilterable(FilterOperation fop) {
				switch (fop) {
				case contains:
					return true;
				case equals:
					return true;
				case present:
					return true;
				case startsWith:
					return true;
				default:
					return false;
				}
			}

			public String getSortKey() {
				return "id";
			}

			public String getFilterValue(Employee emp) {
				return emp.getUserid();
			}
		});

		map.put(Person.Field.LANGUAGES_SPOKEN, new Converter() {
			public void setField(Person person, Employee emp) {
				ArrayList<String> langs = new ArrayList<String>(1);
				langs.add(emp.getPreferredLanguage());
				person.setLanguagesSpoken(langs);
			}

			public boolean isSortable() {
				return true;
			}

			public boolean isFilterable(FilterOperation fop) {
				switch (fop) {
				case contains:
					return true;
				case equals:
					return true;
				case present:
					return true;
				case startsWith:
					return true;
				default:
					return false;
				}
			}

			public String getSortKey() {
				return "preferredLanguage";
			}

			public String getFilterValue(Employee emp) {
				return emp.getPreferredLanguage();
			}
		});

		map.put(Person.Field.LAST_UPDATED, new Converter() {
			public void setField(Person person, Employee emp) {
				person.setUpdated(emp.getLastUpdate());
			}

			public boolean isSortable() {
				return true;
			}

			public boolean isFilterable(FilterOperation fop) {
				switch (fop) {
				case contains:
					return true;
				case equals:
					return true;
				case present:
					return true;
				case startsWith:
					return true;
				default:
					return false;
				}
			}

			public String getSortKey() {
				return "lastUpdate";
			}

			public String getFilterValue(Employee emp) {
				return emp.getLastUpdate().toString();
			}
		});

		map.put(Person.Field.NAME, new Converter() {
			public void setField(Person person, Employee emp) {
				Name name = new NameImpl();
				name.setAdditionalName(emp.getAlternateLastname()); // TODO: is this OK? maybe use native names
				name.setFamilyName(emp.getSurname());
				name.setGivenName(emp.getGivenName());
				name.setFormatted(emp.getDisplayName()); // TODO: is display name ok here?
				name.setHonorificPrefix(emp.getCourtesyTitle());
				// name.setHonorificSuffix("");
				person.setName(name);
			}

			public boolean isSortable() {
				return true;
			}

			public boolean isFilterable(FilterOperation fop) {
				switch (fop) {
				case contains:
					return true;
				case equals:
					return true;
				case present:
					return true;
				case startsWith:
					return true;
				default:
					return false;
				}
			}

			public String getSortKey() {
				return "displayName";
			} 

			public String getFilterValue(Employee emp) {
				//TODO: is displayname ok for name filter - would be redundant with Person.Field.DISPLAY_NAME
				return emp.getDisplayName(); 
			}
		});

		map.put(Person.Field.PHONE_NUMBERS, new Converter() {
			public void setField(Person person, Employee emp) {
				ArrayList<ListField> phones = new ArrayList<ListField>(5);
				String num = emp.getTelephoneNumber();
				if (num != null && num.length() > 0) {
					ListField phone = new ListFieldImpl();
					phone.setType("work"); // TODO: leave it blank ??
					phone.setPrimary(true); // TODO:
					phone.setValue(num);
					phones.add(phone);
				}
				num = emp.getMobileNumber();
				if (num != null && num.length() > 0) {
					ListField phone = new ListFieldImpl();
					phone.setType("mobile"); // TODO: leave it blank ??
					// phone.setPrimary(true);
					phone.setValue(num);
					phones.add(phone);
				}
				num = emp.getFaxNumber();
				if (num != null && num.length() > 0) {
					ListField phone = new ListFieldImpl();
					phone.setType("fax"); // TODO: leave it blank ??
					phone.setPrimary(false);
					phone.setValue(num);
					phones.add(phone);
				}
				num = emp.getIpTelephoneNumber();
				if (num != null && num.length() > 0) {
					ListField phone = new ListFieldImpl();
					phone.setType("ip"); // TODO: leave it blank ??
					// phone.setPrimary(true);
					phone.setValue(num);
					phones.add(phone);
				}
				num = emp.getPagerNumber(); // TODO: pager type and provider no being mapped here
				if (num != null && num.length() > 0) {
					ListField phone = new ListFieldImpl();
					phone.setType("pager"); // TODO: leave it blank ??
					// phone.setPrimary(true);
					phone.setValue(num);
					phones.add(phone);
				}
				if (phones.size() > 0) {
					person.setPhoneNumbers(phones);
				}

			}

			public boolean isSortable() {
				return true;
			}

			public boolean isFilterable(FilterOperation fop) {
				switch (fop) {
				case contains:
					return true;
				case equals:
					return true;
				case present:
					return true;
				case startsWith:
					return true;
				default:
					return false;
				}
			}

			public String getSortKey() {
				return "telephoneNumber";
			}

			public String getFilterValue(Employee emp) {
				return emp.getTelephoneNumber();
			}
		});
		
		map.put(Person.Field.STATUS, new Converter() {
			public void setField(Person person, Employee emp) {
				EntryMessage msg = emp.getStatus();
				if (msg != null) {
					person.setStatus(msg.getSummary()); // TODO: status the same type here
				}
			}

			public boolean isSortable() {
				//TODO: to make this sortable - need to make the sort object 
				// handle EntryMessage stuff
				return false;
			}

			public boolean isFilterable(FilterOperation fop) {
				return true;
			}

			public String getSortKey() {
				return "status";
			}

			public String getFilterValue(Employee emp) {
				EntryMessage msg = emp.getStatus();
				if (msg != null) {
					return msg.getSummary(); // TODO: status the same type here
				}
				return "";
			}
		});

		map.put(Person.Field.THUMBNAIL_URL, new Converter() {
			public void setField(Person person, Employee emp) {
				person.setThumbnailUrl(emp.getImageUrl()); // TODO: is image a thumbnail?, i see thumbnail in photo table
			}

			public boolean isSortable() {
				return true;
			}

			public boolean isFilterable(FilterOperation fop) {
				switch (fop) {
				case contains:
					return true;
				case equals:
					return true;
				case present:
					return true;
				case startsWith:
					return true;
				default:
					return false;
				}
			}

			public String getSortKey() {
				return "photoUrl";
			}

			public String getFilterValue(Employee emp) {
				return emp.getImageUrl();
			}
		});

		map.put(Person.Field.URLS, new Converter() {
			public void setField(Person person, Employee emp) {
				ArrayList<Url> urls = new ArrayList<Url>(3);
				String aUrl = emp.getBlogUrl();
				if (aUrl != null && aUrl.length() > 0) {
					Url url = new UrlImpl();
					url.setLinkText("My Blog"); // TODO: hard coded
					url.setType("blog");
					url.setValue(aUrl);
					urls.add(url);
				}
				aUrl = emp.getCalendarUrl();
				if (aUrl != null && aUrl.length() > 0) {
					Url url = new UrlImpl();
					url.setLinkText("Calender"); // TODO: hard coded
					url.setType("calendar");
					url.setValue(aUrl);
					urls.add(url);
				}
				aUrl = emp.getFreeBusyUrl();
				if (aUrl != null && aUrl.length() > 0) {
					Url url = new UrlImpl();
					url.setLinkText("Free Busy"); // TODO: hard coded
					url.setType("freeBusy");
					url.setValue(aUrl);
					urls.add(url);
				}
				if (urls.size() > 0) {
					person.setUrls(urls);
				}
			}

			public boolean isSortable() {
				return false;
			}

			public boolean isFilterable(FilterOperation fop) {
				return false;
			}

			public String getSortKey() {
				return "";
			}

			public String getFilterValue(Employee emp) {
				return null;
			}
		});

		map.put(Person.Field.UTC_OFFSET, new Converter() {
			public void setField(Person person, Employee emp) {
				String timezone = emp.getTimezone();
				if (timezone != null && timezone.length() > 0) {
					TimeZone profileTz = TimeZone.getTimeZone(timezone);
					double offset = profileTz.getRawOffset();
					// shindig wants offset in minutes
					long minutes = (long) (offset / 60000); // TODO: minutes OK
					person.setUtcOffset(minutes);
				}
			}

			public boolean isSortable() {
				return true;
			}

			public boolean isFilterable(FilterOperation fop) {
				switch (fop) {
				case contains:
					return true;
				case equals:
					return true;
				case present:
					return true;
				case startsWith:
					return true;
				default:
					return false;
				}
			}

			public String getSortKey() {
				return "timezone";
			}

			public String getFilterValue(Employee emp) {
				String timezone = emp.getTimezone();
				long minutes = 0;
				if (timezone != null && timezone.length() > 0) {
					TimeZone profileTz = TimeZone.getTimeZone(timezone);
					double offset = profileTz.getRawOffset();
					// shindig wants offset in minutes
					minutes = (long) (offset / 60000); // TODO: minutes OK
				}
				return String.valueOf(minutes);
			}
		});
		
    /*
     * below are list of Person fields not mapped
     */
	//case ACCOUNTS :
	//case ACTIVITIES :
	//case AGE :
	//case APP_DATA :
	//case BIRTHDAY :
	//case BODY_TYPE :
	//case BOOKS :
	//case CARS :
	//case CHILDREN :
	//case CURRENT_LOCATION :
	//case DRINKER :
	//case ETHNICITY :
	//case FASHION :
	//case FOOD :
	//case GENDER :
	//case HAPPIEST_WHEN :
	//case HAS_APP :
	//case HEROES :
	//case HUMOR :
	//case IMS :
	//case INTERESTS :
	//case JOB_INTERESTS :
	//case LIVING_ARRANGEMENT :
	//case LOOKING_FOR :
	//case MOVIES :
	//case MUSIC :
	//case NETWORKPRESENCE :
	//case NICKNAME :
	//case ORGANIZATIONS :
	//case PETS :
	//case PHOTOS :
	//case POLITICAL_VIEWS :
	//case PREFERRED_USERNAME : 
	//case PROFILE_SONG :
	//case PROFILE_URL :
	//case PROFILE_VIDEO :
	//case QUOTES :
	//case RELATIONSHIP_STATUS :
	//case RELIGION :
	//case ROMANCE :
	//case SCARED_OF :
	//case SEXUAL_ORIENTATION :
	//case SMOKER :
	//case SPORTS :
	//case TURN_OFFS :
	//case TURN_ONS :
	//case TV_SHOWS :
	}

	private VulcanPersonServiceMapper() {
	}

    public static Map<Person.Field, Converter> getMap() {
    	return map;
    }
}
