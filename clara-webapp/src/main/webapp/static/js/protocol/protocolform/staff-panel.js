Ext.ns('Clara.NewSubmission');
var xmlo = {};
Clara.NewSubmission.StaffXMLString = "";
Clara.NewSubmission.StaffFromData = function(staffdata){
	var value="";
	var staff = new Clara.NewSubmission.Staff({
		id:staffdata.id,
		userid:staffdata.userid,
		sap:staffdata.sap,
		phone:staffdata.phone,
		lastname:staffdata.lastname,
		firstname:staffdata.firstname,
		email:staffdata.email,
		conflictofinterestdesc:staffdata.conflictofinterestdesc,
		notify:(staffdata.notify == "true")?true:false,
		conflictofinterest:staffdata.conflictofinterest,
		roles:[],
		responsibilities:[],
		costs:[]
	});
	
	for (var i=0; i<staffdata.roles.length;i++){
		if (jQuery.browser.msie)
			value = staffdata.roles[i].node.text;
		else
			value = staffdata.roles[i].node.textContent;
		staff.roles.push(value);
	}
	for (var i=0; i<staffdata.responsibilities.length;i++){
		if (jQuery.browser.msie)
			value = staffdata.responsibilities[i].node.text;
		else
			value = staffdata.responsibilities[i].node.textContent;
		staff.responsibilities.push(value);
	}
	
	for (var i=0; i<staffdata.costs.length;i++){
		staff.costs.push([staffdata.costs[i].get("startdate"),staffdata.costs[i].get("enddate"),staffdata.costs[i].get("salary"),staffdata.costs[i].get("fte")]);
	}
	return staff;
};

Clara.NewSubmission.ProtocolStaffPanel = Ext.extend(Ext.grid.GridPanel, {
	id: 'protocol-staff-panel',
	frame:false,
	trackMouseOver:false,
	height:250,
	selectedStaff:{},
	stripeRows:true,
	constructor:function(config){		
		Clara.NewSubmission.ProtocolStaffPanel.superclass.constructor.call(this, config);
	},	
	
	loadStaff:function(){
		clog("loading staff");
		this.getStore().removeAll();
		this.getStore().load({params:{listPath:'/'+ claraInstance.form.xmlBaseTag +'/staffs/staff'}});
		Ext.getCmp("btnRemoveStaff").setDisabled(true);
	},
	
	removeSelectedStaff:function(){
		var ajaxBaseUrl = appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/"+claraInstance.type+"-form-xml-datas/" + claraInstance.form.xmlDataId + "/";

		var staffID = this.selectedStaff.id;
    	if (staffID != ""){
    		url = ajaxBaseUrl + "xml-elements/delete";
    		data = {	
    				listPath: "/" + claraInstance.form.xmlBaseTag + "/staffs/staff",
    				elementId: staffID
    		};
    		
    		jQuery.ajax({
    			async: false,
    			url: url,
    			type: "POST",
    			dataType: 'xml',
    			data: data
    		});
    		
    	}
    	this.loadStaff();
	},
	
	filterStaffTemplateXML: function(xmlstr){
		// xml is loaded from template, need to reject staff from template that would overwrite staff thats entered.
		var fxml = "";
		var st = this.getStore();
		var uids = st.collect("userid");
		clog(uids);
		clog("About to filter staff");
		xml = XMLStringToObject("<root>"+(xmlstr)+"</root>");
		jQuery(xml).find("staffs").children().each(function(){
			var st = this;
			clog("looking at staff",st, typeof st);
			if (uids.hasValue(jQuery(st).find("user").attr("id"))) {
				clog("INFO: User "+jQuery(this).find("user").attr("id")+" already in store, skipping");
				// skip
			} else {
				clog("Adding user "+jQuery(st).find("user").attr("id")+" to store");
				//clog("THIS",typeof jQuery(this),jQuery(this).parent().html());
				clog("ADDING ",jQuery(st).attr("id"));
				fxml += XMLObjectToString(st);
				// dont skip
			}
		});
		
		// return merged xml..
		fxml = fxml + Clara.NewSubmission.StaffXMLString;
		
		
		fxml = fxml.replace("<list>","");
		fxml = fxml.replace("</list>","");
		fxml = fxml.replace("<list/>","");
		clog("merged.",fxml);
		return fxml;
	},
	
	initComponent: function() {
		var url = appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/"+claraInstance.type+"-form-xml-datas/" + claraInstance.form.xmlDataId + "/";
		var t = this;
		var staffRoleReader = new Ext.data.XmlReader({
			record: 'role',
			fields: [{name:'role'}]
		});
		
		var staffRespReader = new Ext.data.XmlReader({
			record: 'responsibility',
			fields: [{name:'responsibility'}]
		});
		
		staffCostReader = new Ext.data.XmlReader({
			record: 'cost',
			fields: [{name:'startdate', mapping:'@startdate', type:'date', dateFormat:'m/d/Y'},{name:'enddate', mapping:'@enddate', type:'date', dateFormat:'m/d/Y'},{name:'salary', mapping:'@salary', type:'float'},{name:'fte', mapping:'@fte', type:'float'}]
		});
		
		var config = {
				store:new Ext.data.XmlStore({
					proxy: new Ext.data.HttpProxy({
						url: url + "xml-elements/list",
						method:"GET",
						headers:{'Accept':'application/xml;charset=UTF-8'}
					}),
			        listeners: {
						load: function(s){
							Clara.NewSubmission.StaffXMLString = XMLObjectToString(s.reader.xmlData);
						},
			            exception: function(dp,type,action,opt,resp,arg) {
			            	cerr("load failed",dp,type,action,opt,resp,arg);
			                alert('load failed -- '+type+' .. '+action);
			            }
					},
					baseParams:{listPath: '/'+ claraInstance.form.xmlBaseTag +'/staffs/staff'},
					record:'staff',
					root:'list',
					autoLoad:true,
					fields: [
						{name:'id', mapping:'@id'},
						{name:'userid',mapping:'user@id'},
						{name:'sap',mapping:'user@sap'},
						{name:'phone',mapping:'user@phone'},
						{name:'lastname', mapping:'user>lastname'},
						{name:'firstname',mapping:'user>firstname'},
						{name:'email',mapping:'user>email'},
						{name:'notify',mapping:'notify'},
						{name:'conflictofinterest',mapping:'conflict-of-interest'},
						{name:'conflictofinterestdesc',mapping:'conflict-of-interest-description'},
						{name:'costs',convert:function(v,node){ return staffCostReader.readRecords(node).records; }},
						{name:'roles',convert:function(v,node){ return staffRoleReader.readRecords(node).records; }},
						{name:'responsibilities',convert:function(v,node){ return staffRespReader.readRecords(node).records; }}
					]
				}),
				viewConfig: {
					forceFit:true
				},
				tbar: new Ext.Toolbar({
					items:[{
				    	text: 'Add Staff',
				    	iconCls:'icn-user--plus',
				    	handler: function(){
							var dw = new Clara.NewSubmission.ProtocolStaffWindow();
							dw.show();
				    	}},'-',
				    	{
					    	text: 'Remove Staff',
					    	id:'btnRemoveStaff',
					    	disabled:true,
					    	iconCls:'icn-user--minus',
					    	handler: function(){
				    			Ext.Msg.show({
				    			   title:'Remove staff?',
				    			   msg: 'Are you sure you want to remove this staff member?',
				    			   buttons: Ext.Msg.YESNOCANCEL,
				    			   fn: function(btn){
				    					if (btn == 'yes'){
				    						t.removeSelectedStaff();
				    					}
				    				},
				    			   animEl: 'elId',
				    			   icon: Ext.MessageBox.WARNING
				    			});
					    }},'->',{
					    	text: 'Staff Groups',
					    	iconCls:'icn-wrench',
					    	menu:[{
				    			iconCls:'icn-script-import',
				    			text:'Load Staff Group..',
								handler:function(){
						    		new Clara.TemplateLoadWindow({
						    			templateStore:Clara.NewSubmission.StaffTemplateStore,
						    			loadTemplateCallback: function(xml){
						    				clog("Window: loadTemplateCallback XML:");
						    				var xmlstr = XMLObjectToString(xml);
						    				clog(xmlstr);
						    				// Filter out xml that matches existing staff
						    				// TODO finish function
						    				var fxml = t.filterStaffTemplateXML(xmlstr);
						    				var url = appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/"+claraInstance.type+"-form-xml-datas/" + claraInstance.form.xmlDataId + "/update";
						    				var data = {
						    						pagefragment: "<"+claraInstance.type+"><staffs>"+fxml+"</staffs></"+claraInstance.type+">"
						    					};

						    				 jQuery.ajax({
						    					async: false,
						    					url: url,
						    					type: "POST",
						    					dataType: 'text',
						    					data: data,
						    					success: function(d){
						    						clog(d);
						    						clog("RELOADING AFTER TEMPLATE LOAD..");
						    						Ext.getCmp("protocol-staff-panel").loadStaff();
						    					}
						    				}); 
						    			
						    			}
						    		}).show();
								}
				    		},{
				    			iconCls:'icn-script-export',
				    			text:'Save Staff Group..',
								handler:function(){
				    				new Clara.TemplateSaveWindow({templateStore:Clara.NewSubmission.StaffTemplateStore, xml:"<staffs>"+Clara.NewSubmission.StaffXMLString+"</staffs>"}).show();
								}
				    		}]
					    	
					    }
					]
				}),
				sm: new Ext.grid.RowSelectionModel({singleSelect: true}),
		        loadMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),
		        columns: [
{
  	dataIndex:'notify',
  	sortable:true,
  	renderer:function(value, p, record){
  		  return (value == "true")?"<div class='icn-mail-send' style='background-repeat:no-repeat;width:20px;height:20px;'></div>":"";
	    },
  	width:20
},
		                  {
		                	  	header:'Staff Member',
		                	  	dataIndex:'lastname',
		                	  	sortable:true,
		                	  	renderer:function(value, p, record){
		                      		  return "<div class='staff-name'>"+record.get("firstname")+" "+record.get("lastname")+" (<a href='"+appContext+"/user/"+record.get("userid")+"/profile' target='_blank'>profile</a>)</div><div class='staff-email'>"+record.get("email")+"</div>";
		                  	    },
		                	  	width:150
		                  },
		                  
		                  {
		                	  	header:'Roles',
		                	  	dataIndex:'roles',
		                	  	sortable:false,
		                	  	renderer:function(value, p, records){
				                  	var outHTML='<ul>';
				                  	for (var i=0; i<records.data.roles.length; i++) {
				                  		if (jQuery.browser.msie)
				                  			outHTML = outHTML + "<li class='staff-row-role'>"+records.data.roles[i].node.text+"</li>";
				                  		else
				                  			outHTML = outHTML + "<li class='staff-row-role'>"+records.data.roles[i].node.textContent+"</li>";
				                  	}
				                  	return outHTML+'</ul>';
				                  },
		                	  	width:150
		                    },
		                    {
		                  	  	header:'Responsibilities',
		                  	  	dataIndex:'responsibilities',
		                  	  	sortable:false,
		                  	  	renderer:function renderResp(value, p, records){
				                    	var outHTML='<ul>';
				                    	for (var i=0; i<records.data.responsibilities.length; i++) {
				                    		if (jQuery.browser.msie)
				                    			outHTML = outHTML + "<li class='staff-row-responsibility'>"+records.data.responsibilities[i].node.text+"</li>";
				                    		else
				                    			outHTML = outHTML + "<li class='staff-row-responsibility'>"+records.data.responsibilities[i].node.textContent+"</li>";
				                    	}
				                    	return outHTML+'</ul>';
				                },
		                  	  	width:250
		                      }],
			    listeners:{
				    rowdblclick: function(grid, rowI, event)   {
						clog("dblclick!!");
						var staffdata = grid.getStore().getAt(rowI).data;
						var staff = Clara.NewSubmission.StaffFromData(staffdata);
						new Clara.NewSubmission.ProtocolStaffWindow({editing:true, staff:staff}).show();
						
				    },
				    rowclick: function(grid, rowI, event)   {
						var staffdata = grid.getStore().getAt(rowI).data;
						var staff = Clara.NewSubmission.StaffFromData(staffdata);
						Ext.getCmp("btnRemoveStaff").setDisabled(false);
						Ext.getCmp("protocol-staff-panel").selectedStaff = staff;
				    }
			    }
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));		
		Clara.NewSubmission.ProtocolStaffPanel.superclass.initComponent.apply(this, arguments);
		
	}
	

});
Ext.reg('claraprotocolstaffpanel', Clara.NewSubmission.ProtocolStaffPanel);