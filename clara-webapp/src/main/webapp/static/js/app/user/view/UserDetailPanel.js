Ext.define('Clara.User.view.UserDetailPanel', {
	extend: 'Ext.panel.Panel',
	alias: 'widget.userdetailpanel',
	border:false,
	layout: 'fit',
	initComponent: function() { 
		this.dockedItems = [{
			dock: 'bottom',
			border:false,
			xtype: 'toolbar',
			items: ['->', {
				xtype: 'button',
				id:'btnSaveUserDetails',
				text: 'Save details',
				disabled:true,
				iconCls:'icn-disk',
				handler: function(){

					var url = appContext+"/ajax/users/editoffcampususer";
					jQuery.ajax({
		  				  type: 'POST',
		  				  async:false,
		  				  url: url,
		  				  data: {
		  					  id:parseInt(profile.id),
		  					firstname:Ext.getCmp("fldUserFirstName").getValue(),
		  					middlename:Ext.getCmp("fldUserMiddleName").getValue(),
		  					lastname:Ext.getCmp("fldUserLastName").getValue(),
		  					phone:Ext.getCmp("fldUserPhone").getValue(),
		  					email:Ext.getCmp("fldUserEmail").getValue(),
		  					department:Ext.getCmp("fldUserDepartment").getValue(),
		  					//jobtitle:Ext.getCmp("fldUserTitle").getValue()
		  				  },
		  				  success: function(){
		  					url = appContext + "/ajax/users/"+profile.id+"/saveuserprofile";
							
							var data = "<metadata>";
							data += "<is-trained>" + Ext.getCmp("fldUserTrained").getValue() + "</is-trained>";
							if (claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN'])){
								data += "<citi-id>" + Ext.getCmp("fldCitiId").getValue() + "</citi-id>"+
								"<citi-training-expiredate>" + Ext.getCmp("fldCitiTrainingExpires").getValue() + "</citi-training-expiredate>"+
								"<citi-notes>" + Ext.getCmp("fldCitiNotes").getValue() + "</citi-notes>";
							}
							var altEmail = Ext.getCmp("fldAlternateEmail").getValue();
							if (jQuery.trim(altEmail).length > 0) data += "<alternate-email>" + altEmail + "</alternate-email>";
							data += "<nationality-changed>" + Ext.getCmp("fldUserNationalityChanged").getValue() + "</nationality-changed>";
							data += "<citizenship-status>" + Ext.getCmp("fldUserCitizenship").getValue() + "</citizenship-status>";
							data += "</metadata>";
										
							 jQuery.ajax({
								url:url,
								type: "POST",
								data: {profile: data, userId:profile.id},
								async: false,
								dataType:'xml',
								complete:function(){
									if (Ext.getCmp("fldCVFile").isDirty()){
										var fp = Ext.getCmp("userProfileForm");
						                if(fp.getForm().isValid()){
						                	fp.getForm().submit({
						                		headers : {
						                			"Accept":"text/html"
						                		},
							                    url: appContext + '/fileserver/fileUpload',
							                    waitMsg: 'Uploading your document...',
							                    success: function(request, response){
						                			clog("success:");
						                			clog(response);
						                		// For some reason, the response shows up as a failure...
						                	   },
						                	   // the failure method will handle the json response from MyFormResult controller
						                	   failure: function(request, response){
						                			clog("failure:");
						                			clog(response.result);
						                			   var fileObj = response.result;
						                			   if (typeof fileObj == 'undefined' || fileObj == null){
						                				   Ext.Msg.alert('Error', 'There was a problem uploading the file. Please try again later.');
						                			   } else {
						                				    var d = {};
						                				    d.id = fileObj.id;
						                				    d.hashid = fileObj.identifier;
						                				    d.title = "CV";
						                				    d.category = "User Document";
						                				    clog("SENDING METADATA FOR DOC:");
						                				    clog(d);
						                				    var url = appContext + "/ajax/user/savefilemetadata";
						                					jQuery.ajax({
						                						url: url,
						                						type: "POST",
						                						async: false,
						                						data: {
						                							"userId": profile.id, //claraInstance.user.id,
						                							"title": d.title,
						                							"uploadedFileId": d.id,
						                							"uploadedFileHashId": d.hashid,
						                							"category": d.category
						                						},
						                						success: function(data){
						                							clog(data.data);
						                							profile.cvFilePath = data.data.uploadedFile.path;
						                							profile.cvFileId = data.data.uploadedFile.identifier;
						                							profile.cvFileExtension = data.data.uploadedFile.extension;
						                							Ext.getCmp("fldCVFileDisplay").setValue((profile.cvFileId != "")?("<a target='_blank' href='" + fileserverURL + profile.cvFilePath  + profile.cvFileId+"."+profile.cvFileExtension+"'>Download file</a> <span style='color:#777;'>created "+profile.cvFileModified+"</span>"):"No file uploaded.");
						                						}
						                					});
						                				    alert("Saved.");
						                			   }
						                		   
						                	   }
							                });
						                }
									} else {
										alert("Saved.");
									}
									
									
								}
							}); 
		  				  },
		  				  error: function(x,t,e){
		  					  alert("Error saving user details");
		  					  clog("ERROR",x,t,e);
		  				  }
		        		});
					
					
					
				}
			}]
		}];
		this.items = [{
			xtype:'form',
			id:'userProfileForm',
			bodyPadding:8,autoScroll:true,
			
			border:false,
			items:[
			       {xtype:'fieldset',
			    	   title:'Contact / Dept. Info',
			    	   bodyStyle: 'padding:8px',
			    	   border:true,
			    	   collapsible:false,
			    	   autoHeight:true,
			    	   defaultType:'textfield',

			    	   
			    	   items:[{
			    		   fieldLabel:'Username',
			    		   name:'fldUsername',
			    		   id:'fldUsername',
			    		   flex:1,
			    		   readOnly:true
			    	   },
			    	   {
			    		   fieldLabel:'Email',
			    		   name:'fldUserEmail',
			    		   id:'fldUserEmail',
			    		   flex:1,
			    		   readOnly:!(parseInt(profile.id) == parseInt(claraInstance.user.id) || claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN']))
			    	   },
			    	   {
			    		   fieldLabel:'First Name',
			    		   name:'fldUserFirstName',
			    		   id:'fldUserFirstName',
			    		   flex:1,
			    		   readOnly:!(parseInt(profile.id) == parseInt(claraInstance.user.id) || claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN']))
			    	   },{
			    		   fieldLabel:'Middle Name',
			    		   name:'fldUserMiddleName',
			    		   id:'fldUserMiddleName',
			    		   flex:1,
			    		   readOnly:!(parseInt(profile.id) == parseInt(claraInstance.user.id) || claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN']))
			    	   },{
			    		   fieldLabel:'Last Name',
			    		   name:'fldUserLastName',
			    		   id:'fldUserLastName',
			    		   flex:1,
			    		   readOnly:!(parseInt(profile.id) == parseInt(claraInstance.user.id) || claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN']))
			    	   },
			    	   {
			    		   fieldLabel:'Department',
			    		   name:'fldUserDepartment',
			    		   id:'fldUserDepartment',
			    		   flex:1,
			    		   readOnly:!(parseInt(profile.id) == parseInt(claraInstance.user.id) || claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN']))
			    	   },/*
			    	   {
			    		   fieldLabel:'Job Title',
			    		   name:'fldUserTitle',
			    		   id:'fldUserTitle',
			    		   flex:1,
			    		   readOnly:!(parseInt(profile.id) == parseInt(claraInstance.user.id) || claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN']))
			    	   },*/
			    	   {
			    		   fieldLabel:'Phone',
			    		   name:'fldUserPhone',
			    		   id:'fldUserPhone',
			    		   flex:1,
			    		   readOnly:!(parseInt(profile.id) == parseInt(claraInstance.user.id) || claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN']))
			    	   }]
			       },{xtype:'fieldset',
			    	   border:true,
			    	   bodyStyle: 'padding:8px;',
			    	   title:'From Clara',
			    	   collapsible:false,
			    	   autoHeight:true,
			    	   items:[{
			    		   fieldLabel:'Alternate Email (for notifications)',
			    		   xtype:'textfield',
			    		   name:'fldAlternateEmail',
			    		   id:'fldAlternateEmail'
			    	   },{
			    		   fieldLabel:'Trained in Clara?',
			    		   xtype:'checkbox',
			    		   name:'fldUserTrained',
			    		   id:'fldUserTrained',
			    		   readOnly:true
			    	   },{
			    		   fieldLabel:'Nationality changed?',
			    		   xtype:'checkbox',
			    		   name:'fldUserNationalityChanged',
			    		   id:'fldUserNationalityChanged'
			    	   },
			    	   {
			    		   xtype: 'combo',
			    		   fieldLabel:'Citizenship status',
			    		   name:'fldUserCitizenship',
			    		   id:'fldUserCitizenship',
			    		   typeAhead: true,
			    		   triggerAction: 'all',
			    		   store: new Ext.data.SimpleStore({
			    			   fields:['type'],
			    			   data: [['U.S. Citizen'],['Permanent Resident']]
			    		   }),
			    		   lazyRender: true,
			    		   displayField:'type',
			    		   mode:'local',
			    		   selectOnFocus:true,
			    		   listeners:{
			    			   change:function(f,v,ov){
			    			   }
			    		   },
			    		   listClass: 'x-combo-list-small'
			    	   },{
			    		   xtype:'button',
			    		   text:'Change password (non-UAMS only)..',
			    		   handler:function(){
			    			   Ext.create("Clara.User.view.UserPasswordWindow",{}).show();
			    		   }
			    	   }]
			       },{
			    	   xtype:'fieldset',
			    	   border:true,
			    	   bodyStyle: 'padding:8px;',
			    	   title:'Admin Use Only',
			    	   collapsible:false,
			    	   disabled:!claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN']),
			    	   autoHeight:true,
			    	   items:[{
			    		   xtype:'textfield',
			    		   fieldLabel:'Citi ID',
			    		   name:'fldCitiId',
			    		   id:'fldCitiId',
			    		   flex:1,
			    		   readOnly:!claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN'])
			    	   },{
			    		   xtype:'datefield',
			    		   fieldLabel:'Training expiration date',
			    		   name:'fldCitiTrainingExpires',
			    		   id:'fldCitiTrainingExpires',
			    		   format:'m/d/Y',
			    			rawToValue: function(rawInput){
			    				// clog(rawInput);
			    				return rawInput;
			    				// return (rawInput.getMonth() + 1) + '/' + rawInput.getDate() +'/'+rawInput.getFullYear();
			    			},
			    		   flex:1,
			    		   readOnly:!claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN'])
			    	   },{
			    		   xtype:'textarea',
			    		   fieldLabel:'Citi Notes',
			    		   name:'fldCitiNotes',
			    		   id:'fldCitiNotes',
			    		   flex:1,
			    		   readOnly:!claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN'])
			    	   }]
			       
			       },{
			    	   xtype:'fieldset',
			    	   border:true,
			    	   bodyStyle: 'padding:8px;',
			    	   title:'Upload CV',
			    	   collapsible:false,
			    	   autoHeight:true,
			    	   items:[{
			    	        xtype: 'displayfield',
			    	        name: 'file',
			    	        id:'fldCVFileDisplay',
			    	        fieldLabel: 'Current CV File',
			    	        anchor: '100%',
			    	        value:(profile.cvFileId != "")?("<a target='_blank' href='" + fileserverURL + profile.cvFilePath + profile.cvFileId+"."+profile.cvFileExtension+"'>Download file</a> <span style='color:#777;'>created "+profile.cvFileModified+"</span>"):"No file uploaded.",
			    	    },{
			    	        xtype: 'filefield',
			    	        name: 'file',
			    	        id:'fldCVFile',
			    	        fieldLabel: 'New CV File',
			    	        disabled:!(parseInt(profile.id) == parseInt(claraInstance.user.id) || claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN'])),
			    	        msgTarget: 'side',
			    	        anchor: '100%',
			    	        buttonText: 'Select...'
			    	    }]
			       
			       }

			       ]
		}];
		this.callParent();
		
		
		clog("PROFILE",profile);
		// set fields from profile variable
		Ext.getCmp("fldUsername").setValue(profile.username);
		Ext.getCmp("fldUserEmail").setValue(profile.email);
		Ext.getCmp("fldUserPhone").setValue(profile.phone);
		Ext.getCmp("fldUserDepartment").setValue(profile.department);
		//Ext.getCmp("fldUserTitle").setValue(profile.title);
		Ext.getCmp("fldUserFirstName").setValue(profile.firstname);
		Ext.getCmp("fldUserMiddleName").setValue(profile.middlename);
		Ext.getCmp("fldUserLastName").setValue(profile.lastname);
		

		
		jQuery.ajax({
    		url:appContext + "/ajax/users/"+profile.id+"/profile",
    		type: "GET",
    		async: false,
			dataType:'xml',
    		success: function(data){
				var cs = jQuery(data).find("citizenship-status").text();
				var ist= (jQuery(data).find("is-trained").text()=="true")?true:false;
				var citiid= jQuery(data).find("citi-id").text();
				var citidate = jQuery(data).find("citi-training-expiredate").text();
				//var cititrainingcomplete= (jQuery(data).find("citi-training-complete").text()=="true")?true:false;
				var citinotes= jQuery(data).find("citi-notes").text();
				var altEmail = jQuery(data).find("alternate-email").text();
				var nc= (jQuery(data).find("nationality-changed").text()=="true")?true:false;
				Ext.getCmp("fldUserTrained").setValue(ist);
				Ext.getCmp("fldCitiId").setValue(citiid);
				if (citidate && Ext.String.trim(citidate) != "") Ext.getCmp("fldCitiTrainingExpires").setValue(new Date(citidate));
				//Ext.getCmp("fldCitiTrainingComplete").setValue(cititrainingcomplete);
				Ext.getCmp("fldCitiNotes").setValue(citinotes);
				Ext.getCmp("fldUserCitizenship").setValue(cs);
				Ext.getCmp("fldUserNationalityChanged").setValue(nc);
				Ext.getCmp("fldAlternateEmail").setValue(altEmail);
				if (parseInt(profile.id) == parseInt(claraInstance.user.id) || claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN'])) Ext.getCmp("btnSaveUserDetails").setDisabled(false);
    		}
    	});
		
	}
});