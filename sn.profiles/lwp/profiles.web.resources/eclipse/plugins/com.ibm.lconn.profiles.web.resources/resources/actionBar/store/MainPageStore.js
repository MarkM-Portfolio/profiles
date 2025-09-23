/* *************************************************************** */
/*                                                                 */
/* HCL Confidential                                                */
/*                                                                 */
/* OCO Source Materials                                            */
/*                                                                 */
/* Copyright HCL Technologies Limited 2013, 2022                   */
/*                                                                 */
/* The source code for this program is not published or otherwise  */
/* divested of its trade secrets, irrespective of what has been    */
/* deposited with the U.S. Copyright Office.                       */
/*                                                                 */
/* *************************************************************** */

dojo.provide("lconn.profiles.actionBar.store.MainPageStore");


dojo.require("lconn.profiles.actionBar.store.BaseStore");

dojo.require("lconn.profiles.invite.Invite");
dojo.require("lconn.profiles.Following");
dojo.require("lconn.profiles.ProfilesCore");

dojo.require("lconn.core.filepicker");
dojo.require("com.ibm.oneui.util.proxy");
dojo.require("lconn.core.util.html");

dojo.require("lconn.core.config.properties");
dojo.requireLocalization("lconn.profiles", "ui");

(function() {

	dojo.declare(
		"lconn.profiles.actionBar.store.MainPageStore",
		[lconn.profiles.actionBar.store.BaseStore],
		{
			shareFileXmlTemplate: '<?xml version="1.0" encoding="UTF-8"?>\n\
				<cmis:acl xmlns:cmis="http://docs.oasis-open.org/ns/cmis/core/200908/">\n\
					<cmis:permission>\n\
						<cmis:principal>\n\
							<cmis:principalId>{userid}</cmis:principalId>\n\
						</cmis:principal>\n\
						<cmis:permission>cmis:read</cmis:permission>\n\
						<cmis:direct>true</cmis:direct>\n\
					</cmis:permission>\n\
				</cmis:acl>\n\
			',

			loadData: function loadData_$0(callback) {
				var pEmailIcon = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' class='MuiSvgIcon-root' focusable='false' viewBox='0 0 32 32' aria-hidden='true' role='presentation' data-mui-test='emailIcon'%3E%3Cpath fill='%233D5466' d='M28 6H4a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h24a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2zm-2.2 2L16 14.78 6.2 8zM4 24V8.91l11.43 7.91a1 1 0 0 0 1.14 0L28 8.91V24z'%3E%3C/path%3E%3C/svg%3E";
				var pChatIcon = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' class='MuiSvgIcon-root' focusable='false' viewBox='0 0 32 32' aria-hidden='true' role='presentation' data-mui-test='chatIcon'%3E%3Cpath fill='%233D5466' d='M17.74 30L16 29l4-7h6a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h9v2H6a4 4 0 0 1-4-4V8a4 4 0 0 1 4-4h20a4 4 0 0 1 4 4v12a4 4 0 0 1-4 4h-4.84z'%3E%3C/path%3E%3Cpath fill='%233D5466' d='M8 10h16v2H8zm0 6h10v2H8z'%3E%3C/path%3E%3C/svg%3E";
				var pWebmeetingIcon = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' class='MuiSvgIcon-root' focusable='false' viewBox='0 0 32 32' aria-hidden='true' role='presentation' data-mui-test='videoIcon'%3E%3Cpath fill='%233D5466' d='M21 26H4a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h17a2 2 0 0 1 2 2v4.06l5.42-3.87A1 1 0 0 1 30 9v14a1 1 0 0 1-1.58.81L23 19.94V24a2 2 0 0 1-2 2zM4 8v16h17v-6a1 1 0 0 1 1.58-.81L28 21.06V10.94l-5.42 3.87A1 1 0 0 1 21 14V8z'%3E%3C/path%3E%3C/svg%3E";
				var shareIcon = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' class='MuiSvgIcon-root' focusable='false' viewBox='0 0 32 32' aria-hidden='true' role='presentation' data-mui-test='shareIcon'%3E%3Cpath fill='%233D5466' d='M23 20a5 5 0 0 0-3.89 1.89l-7.31-4.57a4.46 4.46 0 0 0 0-2.64l7.31-4.57A5 5 0 1 0 18 7a4.79 4.79 0 0 0 .2 1.32l-7.31 4.57a5 5 0 1 0 0 6.22l7.31 4.57A4.79 4.79 0 0 0 18 25a5 5 0 1 0 5-5zm0-16a3 3 0 1 1-3 3 3 3 0 0 1 3-3zM7 19a3 3 0 1 1 3-3 3 3 0 0 1-3 3zm16 9a3 3 0 1 1 3-3 3 3 0 0 1-3 3z'%3E%3C/path%3E%3C/svg%3E";
				var pCanInviteIcon = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' class='MuiSvgIcon-root' focusable='false' viewBox='0 0 32 32' aria-hidden='true' role='presentation' data-mui-test='user--followIcon'%3E%3Cpath fill='%233D5466' d='M32 14h-4v-4h-2v4h-4v2h4v4h2v-4h4v-2zM12 4a5 5 0 1 1-5 5 5 5 0 0 1 5-5m0-2a7 7 0 1 0 7 7 7 7 0 0 0-7-7zm10 28h-2v-5a5 5 0 0 0-5-5H9a5 5 0 0 0-5 5v5H2v-5a7 7 0 0 1 7-7h6a7 7 0 0 1 7 7z'%3E%3C/path%3E%3C/svg%3E";
				var pCanUnFriendIcon = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 23 22'%3E%3Cg id='Page-1' stroke='none' stroke-width='1' fill='none' fill-rule='evenodd'%3E%3Cg id='Remove-From-Network' transform='translate(0.500000, 0.500000)' fill='%233D5466' fill-rule='nonzero'%3E%3Cpath d='M16,8 L22,8 L22,10 L16,10 L16,8 Z M7.5,1.5 C9.5710678,1.5 11.25,3.1789322 11.25,5.25 C11.25,7.3210678 9.5710678,9 7.5,9 C5.4289322,9 3.75,7.3210678 3.75,5.25 C3.7523559,3.1799088 5.4299088,1.5023559 7.5,1.5 L7.5,1.5 Z M7.5,0 C4.6005051,0 2.25,2.3505051 2.25,5.25 C2.25,8.1494949 4.6005051,10.5 7.5,10.5 C10.3994949,10.5 12.75,8.1494949 12.75,5.25 C12.75,2.3505051 10.3994949,0 7.5,0 Z M15,21 L13.5,21 L13.5,17.25 C13.4975616,15.179943 11.820057,13.5024384 9.75,13.5 L5.25,13.5 C3.179943,13.5024384 1.5024384,15.179943 1.5,17.25 L1.5,21 L0,21 L0,17.25 C0.003389,14.35191 2.35191,12.003389 5.25,12 L9.75,12 C12.64809,12.003389 14.996611,14.35191 15,17.25 L15,21 Z' id='path-1'%3E%3C/path%3E%3C/g%3E%3C/g%3E%3C/svg%3E";
				var pInvitedIcon = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' class='MuiSvgIcon-root' focusable='false' viewBox='0 0 32 32' aria-hidden='true' role='presentation' data-mui-test='user--adminIcon' style='font-size: 3em;'%3E%3Cpath d='M12 4a5 5 0 1 1-5 5 5 5 0 0 1 5-5m0-2a7 7 0 1 0 7 7 7 7 0 0 0-7-7zm10 28h-2v-5a5 5 0 0 0-5-5H9a5 5 0 0 0-5 5v5H2v-5a7 7 0 0 1 7-7h6a7 7 0 0 1 7 7zm3-13.82l-2.59-2.59L21 15l4 4 7-7-1.41-1.41L25 16.18z'%3E%3C/path%3E%3C/svg%3E";
				var pFollowIcon = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' class='MuiSvgIcon-root' focusable='false' viewBox='0 0 32 32' aria-hidden='true' role='presentation' data-mui-test='user--roleIcon' style='font-size: 3em;'%3E%3Cpath d='M28.07 21L22 15l6.07-6 1.43 1.41L24.86 15l4.64 4.59L28.07 21zM22 30h-2v-5a5 5 0 0 0-5-5H9a5 5 0 0 0-5 5v5H2v-5a7 7 0 0 1 7-7h6a7 7 0 0 1 7 7zM12 4a5 5 0 1 1-5 5 5 5 0 0 1 5-5m0-2a7 7 0 1 0 7 7 7 7 0 0 0-7-7z'%3E%3C/path%3E%3C/svg%3E";
				var pUnFollowcon = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink' viewBox='0 0 23 22' version='1.1'%3E%3Cpath d='M46.4319805,31.3713203 L44.3106602,29.25 L46.4319805,27.1286797 L45.3713203,26.0680195 L43.25,28.1893398 L41.1286797,26.0680195 L40.0680195,27.1286797 L42.1893398,29.25 L40.0680195,31.3713203 L41.1286797,32.4319805 L43.25,30.3106602 L45.3713203,32.4319805 L46.4319805,31.3713203 L46.4319805,31.3713203 Z M32,21 C34.0710678,21 35.75,22.6789322 35.75,24.75 C35.75,26.8210678 34.0710678,28.5 32,28.5 C29.9289322,28.5 28.25,26.8210678 28.25,24.75 C28.2523559,22.6799088 29.9299088,21.0023559 32,21 L32,21 Z M32,19.5 C29.1005051,19.5 26.75,21.8505051 26.75,24.75 C26.75,27.6494949 29.1005051,30 32,30 C34.8994949,30 37.25,27.6494949 37.25,24.75 C37.25,21.8505051 34.8994949,19.5 32,19.5 Z M39.5,40.5 L38,40.5 L38,36.75 C37.9975616,34.679943 36.320057,33.0024384 34.25,33 L29.75,33 C27.679943,33.0024384 26.0024384,34.679943 26,36.75 L26,40.5 L24.5,40.5 L24.5,36.75 C24.503389,33.85191 26.85191,31.503389 29.75,31.5 L34.25,31.5 C37.14809,31.503389 39.496611,33.85191 39.5,36.75 L39.5,40.5 Z' id='path-1'/%3E%3Cg id='Artboard' transform='translate(-24.000000, -19.000000)'%3E%3Cuse id='icon-color' fill='%233D5466' xlink:href='%23path-1'/%3E%3C/g%3E%3C/svg%3E";
				var vCardIcon = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' class='MuiSvgIcon-root' focusable='false' viewBox='0 0 32 32' aria-hidden='true' role='presentation' data-mui-test='closed-caption--altIcon'%3E%3Cpath fill='%233D5466' d='M28 6H4a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h24a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2zm0 2v3H4V8zM4 24V13h24v11z'/%3E%3Cpath fill='%233D5466' d='M6 20h10v2H6z'%3E%3C/path%3E%3C/svg%3E";

				//we only want these actions if the displayed user is not inactive
				if (this.profile.isActive) {

					//send email action
					if (this.profile.showEmail && this.profile.email && this.profile.email !== "") {
						this.addDataItem({
							id: this.ACTION_ID_PREFIX + "personCardSendEMail",
							menu_text: this.strings["personCardSendEMail"],
							onClick: dojo.hitch(this, this.sendEmail),
                            icon: pEmailIcon,
						});
					}

					//sametime integration
					// First lets see if sametime, sametime proxy , or the cloud's implementation of stproxy are enabled.
					// If one of them is enabled but not yet available, we will put a disabled Chat button on the actionbar.
					// Once the service is available, the actionbar will reload and an enabled Chat button will appear.

					// check to see if either service is configured and enabled
					// only show is the user is logged in, looking at a different profile, and one of the sametime services is enabled.
					var pc = lconn.profiles.ProfilesCore;
					var isAppRegEnabled = lconn.core.config.services;
					
					console.debug('Is App Registry Service Enabled: ', isAppRegEnabled.extensionRegistry);

					if (isAppRegEnabled.extensionRegistry && (isAppRegEnabled.extensionRegistry["secureUrl"] !== "" || null)) { // check if App Registry Service is enabled in environment otherwise don't check for extensions

						if (this.isLoggedIn && !this.isMyProfile && isAppRegEnabled ? pc.isAnyChatServiceEnabled() : pc.isAnySametimeServiceAvailable()) {
							var extChatLabel = pc.isExtensionChatAvailable() ? pc.getExtensionChatLabel() : '';
	  
							  //disable the button if it is not yet available
							  var itemArgs = {
								  id: this.ACTION_ID_PREFIX + "personCardChat",
								  menu_text: extChatLabel || this.strings["personCardChat"],
								  onClick: dojo.hitch(this, this.openChat),
								  disabled: !(pc.isAnyChatServiceAvailable()),
                                  icon: pChatIcon,
							  };
	  
							  // if the button is disabled, then the service is still loading.  We need to setup
							  // an interval to monitor it's loading and reload the actionbar when it becomes
							  // available.
							  if (itemArgs.disabled) {
								this._sametimeCheckHandle = setInterval(dojo.hitch(this, function() {
								  if (pc.isAnyChatServiceAvailable()) {
									this.reloadActionBar({});
									clearInterval(this._sametimeCheckHandle);
								  }
								}), 500);
							  }
	  
							  this.addDataItem(itemArgs);
						  }
						  
							if (
                this.isLoggedIn &&
                !this.isMyProfile &&
                isAppRegEnabled &&
                pc.isAnyWebmeetingServiceEnabled()
              ) {
                var extWebmeetingLabel = pc.isExtensionWebmeetingAvailable()
                  ? pc.getExtensionWebmeetingLabel()
                  : "";

                //disable the button if it is not yet available
                var itemArgs = {
                  id: this.ACTION_ID_PREFIX + "webmeeting",
                  menu_text: extWebmeetingLabel,
                  onClick: dojo.hitch(this, this.openWebmeeting),
                  disabled: false,
                  icon: pWebmeetingIcon,
                };

                // if the button is disabled, then the service is still loading.  We need to setup
                // an interval to monitor it's loading and reload the actionbar when it becomes
                // available.
                if (itemArgs.disabled) {
                  this._sametimeCheckHandle = setInterval(
                    dojo.hitch(this, function () {
                      if (pc.isAnyWebmeetingServiceEnabled()) {
                        this.reloadActionBar({});
                        clearInterval(this._sametimeCheckHandle);
                      }
                    }),
                    500
                  );
                }

                this.addDataItem(itemArgs);
              }
						  
						  if (this.isLoggedIn && !this.isMyProfile && pc.isExtensionVideoCallAvailable()) {
	  
							var extVideoCallLabel = pc.isExtensionVideoCallAvailable() ? pc.getExtensionVideoCallLabel() : '';
	  
							//hide the button if it is not yet ensure video call is supported
							var videoCallItemArgs = {
								id: this.ACTION_ID_PREFIX + "personCardVideoCall",
								menu_text: extVideoCallLabel,
								onClick: dojo.hitch(this, this.openVideoCall),
								hidden: !(pc.isExtensionVideoCallAvailable()),
                                icon: pWebmeetingIcon,
							};
							
							// if the button is hidden, then app reg may be still loading.  
							// Defer until app reg ready then reload the actionbar when it becomes
							// available.
							if (videoCallItemArgs.hidden) {
							  pc.waitForAppRegReady(dojo.hitch(this, function() {
								  if (pc.isExtensionVideoCallAvailable()) {
									this.reloadActionBar({});
								  }
							  }));
							}
							
							this.addDataItem(videoCallItemArgs);
						  }
					} else {
						console.debug('To enabled Profiles extensions in this environment, enabled the "extensionRegistry" service in the LotusConnections-config.xml');
					}

					if (this.isLoggedIn) {
						if (this.checkAcl("profile.colleague$profile.colleague.connect")) {
							//add as network contact
							if (this.connection.canFriend) {
								this.addDataItem({
									id: this.ACTION_ID_PREFIX + "personCardAddAsMyColleagues",
									menu_text: this.strings["personCardAddAsMyColleagues"],
									onClick: dojo.hitch(this, this.addColleague),
									icon: pCanInviteIcon,
								});

								//if the &invite=true parameter is in the location, automatically launch the invite dialog
								try {
									if (location.search && location.search.indexOf("?") == 0 && dojo.queryToObject(location.search.substring(1))["invite"] == "true") {
										this.addColleague();
									}
								} catch (e) {}
							} else if(window.ui && typeof window.ui._check_ui_enabled === 'function' && window.ui._check_ui_enabled() && !this.connection.canFriend && this.connection.status !== this.connection.TYPES.INVITED && !this.isMyProfile) {
								this.addDataItem({
									id: this.ACTION_ID_PREFIX + "personCardInNetwork",
									menu_text: this.strings["personCardInNetwork"],
									onClick: dojo.hitch(this, this.addColleague),
									icon: pInvitedIcon,
									disabled: true
								});
							}
						}

						//remove from network contact
						if (this.connection.canUnfriend) {
							this.addDataItem({
								id: this.ACTION_ID_PREFIX + "personCardRemoveFromNetwork",
								menu_text: this.strings["personCardRemoveFromNetwork"],
								onClick: dojo.hitch(this, this.removeColleague),
								icon: pCanUnFriendIcon,
							});
						}

						//accept invitation to be contact
						if (this.connection.status === this.connection.TYPES.INVITED) {
							this.addDataItem({
								id: this.ACTION_ID_PREFIX + "personCardAcceptInv",
								menu_text: this.strings["personCardAcceptInv"],
								onClick: dojo.hitch(this, this.acceptColleague),
								icon: pCanInviteIcon,
							});
						}

						//follow
						if (this.checkAcl("profile.following$profile.following.view")) {
							if (this.connection.canFollow) {
								this.addDataItem({
									id: this.ACTION_ID_PREFIX + "label_following_follow",
									menu_text: this.strings["label_following_follow"],
									onClick: dojo.hitch(this, this.follow),
									icon: pFollowIcon,
								});
							} else if (window.ui && typeof window.ui._check_ui_enabled === 'function' && window.ui._check_ui_enabled() && !this.connection.canFollow && !this.isMyProfile) {
								this.addDataItem({
									id: this.ACTION_ID_PREFIX + "label.following.following",
									menu_text: this.strings["label.following.following"],
									icon: pFollowIcon,
									disabled: true
								});
							}
						}
					}
				}

				//unfollow - We can unfollow inactive users.
				if (this.connection.canUnfollow && this.isLoggedIn && this.checkAcl("profile.following$profile.following.view")) {
					this.addDataItem({
						id: this.ACTION_ID_PREFIX + "label_following_unfollow",
						menu_text: this.strings["label_following_unfollow"],
						onClick: dojo.hitch(this, this.unfollow),
						icon: pUnFollowcon,
					});
				}

				if (this.profile.isActive) {

					//share a file action
					// If viewed profile is external, we will not put the Share a File button on the actionbar.
					// If viewed profile is internal or logged in profile has the employee.extended role, we will put the Share a File button on the actionbar.
					if (this.isLoggedIn && !this.isMyProfile) {
						if (this.enabledFiles) {
						  if ((!profilesData.displayedUser.isVisitor) || (profilesData.loggedInUser.hasExtendedRole)) {
								this.addDataItem({
									id: this.ACTION_ID_PREFIX + "personCardShareFile",
									menu_text: this.strings["personCardShareFile"],
									onClick: dojo.hitch(this, this.shareFile),
									icon: shareIcon,
								});
							}
						}
					}

					//download vcard action
					this.addDataItem({
						id: this.ACTION_ID_PREFIX + "personCardDownloadVCard",
						menu_text: this.strings["personCardDownloadVCard"],
						onClick: dojo.hitch(this, this.downloadVCard),
						icon: vCardIcon,
					});
				}
				callback();
			},

			sendEmail: function sendEmail_$1() {
				profiles_goto("mailto:" + this.profile.email);
				return false;
			},



			openChat: function openChat_$2() {
				var pc = lconn.profiles.ProfilesCore;
				if (pc.isExtensionChatAvailable()) {
				  console.log('clicked on open extension chat!');
				  pc.openExtensionChat();
				  return;
				}
				var userinfo = pc.getSametimeUserInfo();
				if (pc.isSametimeAvailable()) {
					lconn.profiles.sametime.sametimeAwareness.openChat(userinfo);
				} else
				if (pc.isSametimeProxyAvailable()) {
					lconn.profiles.sametime.sametimeProxyAwareness.openChat(userinfo);
				} else
				if (pc.isSametimeCloudProxyAvailable()) {
					stproxy.openChat(userinfo);
				}
			},

			openWebmeeting: function openWebmeeting_$11() {
				var pc = lconn.profiles.ProfilesCore;
				if (pc.isExtensionWebmeetingAvailable()) {
				  console.log('clicked on open extension webmeeting!');
				  pc.openExtensionWebmeeting();
				  return;
				}
			},
			
			openVideoCall: function openVideoCall_$3() {
				var pc = lconn.profiles.ProfilesCore;
				pc.openExtensionVideoCall();
			},

			shareFile: function shareFile_$4() {
				if (dojo.exists("lconn.core.config.services.files")) {
					var filesService = dojo.getObject("lconn.core.config.services.files");

					//get the base xml structure to useed to put the request
					var xmlData = this.shareFileXmlTemplate.replace("{userid}", this.profile.userid);


					this._filesSharedRetryCount = 0; //reset the count of retries
					this._filesSharedCount = 0; //reset the count
					this._filesSharedTotal = 0; //reset the total
					this._filesSharedMessage = ""; // reset the message
					this._filesShareMessageType = null; //contains the type of message to show
					var showShareFileReturnMessage_ = dojo.hitch(this, function(fName, success) { //called from the callback of the xhrPut when all responses are completed.
						if (typeof success === "undefined") success = true;
						this._filesSharedCount++;

						if (!success) this._filesShareMessageType = "error";

						if (this._filesSharedMessage.length > 0) {
							this._filesSharedMessage += "<br/>&nbsp;";
						}
						this._filesSharedMessage += this.strings[(success)?"personCardShareFileSuccess":"personCardShareFileError"].replace("{0}", lconn.core.util.html.encodeHtml(fName)).replace("{1}", lconn.core.util.html.encodeHtml(profilesData.displayedUser.displayName));

						if (this._filesSharedTotal == this._filesSharedCount && this._filesSharedMessage.length > 0) { //we've gotten all of our responses back, go ahead and show the message.
							this.showMessage(this._filesSharedMessage, [], null, this._filesShareMessageType);
						}
					});

					//code calls the files api to add this person as a reader.
					var processShareFile_ = dojo.hitch(this, function(file) {
						var fileName = decodeURIComponent(file.getName());
						var fileShareUrl = 	(filesService.secureEnabled && filesService.secureUrl && lconn.core.url.parse(location.href).scheme.toLowerCase() === "https" ? filesService.secureUrl : filesService.url) +
											"/basic/cmis/repository/p!" + encodeURIComponent(profilesData.loggedInUser.userid) + "/acl/snx:file!" + file.getId();
						dojo.xhrPut({
							url: com.ibm.oneui.util.proxy(fileShareUrl),
							handleAs: "xml",
							headers: {
								"Content-Type": "application/cmisacl+xml"
							},
							preventCache: true,
							postData: xmlData,
							load: dojo.hitch(this, function(data, req) {
								if (dojo.config.isDebug && window.console) {
									console.log("PUT of new ACL for file " + fileName + " to cmis returned an http status of: " + req.xhr.status);
								}
								showShareFileReturnMessage_(fileName);

							}),
							error: dojo.hitch(this, function(error, req) {

								if (this._filesSharedRetryCount++ < 10 && req.xhr.status === 409) {  //if we getr a conflict, give it another try...
									setTimeout(function() {
										processShareFile_(file);
									}, 200);
								} else {
									if (dojo.config.isDebug && window.console) {
										console.error("An unexpected error occurred attempting to change ACL on file " + fileName + ": " + error);
										console.log(error);
									}
									showShareFileReturnMessage_(fileName, false);
								}
							})
						});

					});

					lconn.core.filepicker.open({
						filesService: filesService,
						publicOnly: false,
						externalOnly: false,
						shareableOnly: true,
						useCompact: true,
						onSave: dojo.hitch(this, function(files){
							this._filesSharedTotal = files.length;
							dojo.forEach(files, dojo.hitch(this, function(file) {
								processShareFile_(file);
							}));
						})
					});


				}

			},

			downloadVCard: function downloadVCard_$5() {
				showVcardExport();
				return false;
			},


			follow: function follow_$6() {
				if (this.connection.canFollow) {
					lconn.profiles.Following.followUserByKey(
						this.profile.key,
						dojo.hitch(this, function() {
							lconn.profiles.Following.displayFollowedMessage();

							this.refreshWidgets(["sand_thingsInCommon", "sand_socialPath"]);

							this.reloadActionBar({connection: {canFollow: false, canUnfollow: true}});
						})
					);
				}
			},

			unfollow: function unfollow_$7() {
				if (this.connection.canUnfollow) {
					lconn.profiles.Following.unfollowUserByKey(
						this.profile.key,
						dojo.hitch(this, function(){
							lconn.profiles.Following.displayUnfollowedMessage();

							this.refreshWidgets(["sand_thingsInCommon", "sand_socialPath"]);

							this.reloadActionBar({connection: {canFollow: true, canUnfollow: false}});
						})
					);
				}
				return false;
			},

			//network stuff
			addColleague: function addColleague_$8() {
				if (this.connection.canFriend) {

					lconn.profiles.invite.Invite.showDialog(
						this.appContext,
						true, // byKey?
						this.profile.displayName, // displayName
						this.profile.key, // targetKeyOrUserid
						lconn.profiles.ProfilesCore.getLoggedInUserKey(), // loggedInUserKeyOrUserid
						dojo.getObject('lconn.profiles.Friending.currentViewDomNode'),  // errorNodeId
						null, //inviteCallback_, // lconn.core.errorhandling.DefaultXHRErrorHandler // xhrErrorHandlerCallback
						null, //inviteCallback_, // lconn.core.errorhandling.DefaultErrorHandler // errorHandlerCallback
						dojo.hitch(this, function() {
							var abArgs = {connection: {}};
							var reloadWidgets = [];

							if (lconn.profiles.invite.Invite._status.invite == 1) { //send invitation
								abArgs.connection.status = this.connection.TYPES.PENDING;

								//update the indicator that this person is a pending contact
								if (dojo.byId("connectionIndicator")) {
									dojo.place(dojo.doc.createTextNode(this.strings["personCardPendingInv"]), dojo.byId("connectionIndicator"), "only");
								}

								this.showMessage("personCardInvitedContact");
							}

							if (lconn.profiles.invite.Invite._status.follow == 1 && this.connection.canFollow) { //also followed
								abArgs.connection.canUnfollow = true;
								abArgs.connection.canFollow = false;

								this.refreshWidgets(["sand_thingsInCommon", "sand_socialPath"]);
							}

							this.reloadActionBar(abArgs);

						}), // xhrDoneCallback
						null, // sendInviteCallback (optional)
						null, // cancelInviteCallback (optional)
						null  // canFollow
					);
				}
				return false;
			},

			removeColleague: function removeColleague_$9() {
				var dataUrl = this.appContext + '/atom2/forms/friends.xml?connectionIds=' + this.connection.id;
				lconn.profiles.xhrDelete({
					url: dataUrl,
					handleAs: 'xml',
					error: lconn.profiles.ProfilesCore.DefaultXHRErrorHandler,
					load: dojo.hitch(this, function(response, ioArgs) {
						try	{
							if (dojo.exists("profilesData.displayedUser")) {
								profilesData.displayedUser.inNetwork = false;
							}

							this.refreshWidgets(["sand_thingsInCommon", "sand_socialPath", "friends"]);

							this.reloadActionBar({connection: {status: "", id: ""}});

							//remove the indicator that this person is a contact
							if (dojo.byId("connectionIndicator")) {
								dojo.byId("connectionIndicator").innerHTML = "";
							}

							this.showMessage("personCardRemovedContact");
						}
						catch(exception) {
							lconn.profiles.ProfilesCore.DefaultErrorHandler('lconn.profiles.Friending.FriendRequestAction', exception);
						}
					}),
					checkAuthHeader: true
				});
				return false;
			},

			acceptColleague: function acceptColleague_$10() {
				lconn.profiles.Friending.acceptFriendRequest(
					this.connection.id,
					this.profile.key,
					dojo.hitch(this, function(response, ioArgs){
						try	{
							if (dojo.exists("profilesData.displayedUser")) {
								profilesData.displayedUser.inNetwork = true;
							}

							this.refreshWidgets(["sand_thingsInCommon", "sand_socialPath", "friends"]);

							this.reloadActionBar({connection: {status: this.connection.TYPES.CONNECTED}});

							//update the indicator that this person is a contact
							if (dojo.byId("connectionIndicator")) {
								if(window.ui && typeof window.ui._check_ui_enabled === 'function' && window.ui._check_ui_enabled()) {
									dojo.byId("connectionIndicator").innerHTML = "";
								} else {
									dojo.place(dojo.doc.createTextNode(this.strings["personCardInNetwork"]), dojo.byId("connectionIndicator"), "only");
								}
							}

							this.showMessage("personCardAddedContact");
						}
						catch(exception) {
							lconn.profiles.ProfilesCore.DefaultErrorHandler('lconn.profiles.Friending.FriendRequestAction', exception);
						}
					})
				);
				return false;

			}
		}
	);
})();
