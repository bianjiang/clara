Ext.define('Clara.Admin.view.NewOffCampusUserWindow', {
	extend: 'Ext.window.Window',
	alias: 'widget.newoffcampususerwindow',
	layout: 'form',
	title: 'Add Off-Campus User',
	modal:true,
	width:450,
	padding: 6,
	bodyPadding:6,
	modal: true,
	user:{},
	initComponent: function() {
		var me = this;
		var user = me.user;

		me.buttons = [
		              {
		            	  text: 'Save',
		            	  handler: function(){

		            		  var verify = true;
		            		  var url = appContext+"/ajax/users/";
		            		  me.items.each(function(item){
		            			  verify = verify && item.validate();
		            		  });

		            		  if (verify == true){
		            			  if (user && user.id){
		            				  // edit
		            				  url += "editoffcampususer";
		            			  } else {
		            				  // new
		            				  url += "createoffcampususer";
		            			  }

		            			  jQuery.ajax({
		            				  type: 'POST',
		            				  async:false,
		            				  url: url,
		            				  data: {
		            					  username:Ext.getCmp("fldOCUsername").getValue(),
		            					  firstname:Ext.getCmp("fldOCFirstName").getValue(),
		            					  middlename:Ext.getCmp("fldOCMiddleName").getValue(),
		            					  lastname:Ext.getCmp("fldOCLastName").getValue(),
		            					  phone:Ext.getCmp("fldOCPhone").getValue(),
		            					  email:Ext.getCmp("fldOCEmail").getValue(),
		            					  department:Ext.getCmp("fldOCDepartment").getValue()
		            				  },
		            				  success: function(){
		            					  me.close();  
		            				  },
		            				  error: function(x,t,e){
		            					  alert("Error saving user");
		            					  clog("ERROR",x,t,e);
		            				  }
		            			  });

		            		  } else {
		            			  alert("Missing values. Check fields and try again.");
		            		  }

		            	  }
		              }
		              ];
		me.items = [
		            {
		            	xtype: 'textfield',
		            	id:'fldOCUsername',
		            	fieldLabel: 'Username',
		            	allowBlank:false,
		            	anchor: '100%',
		            	value:(user && user.username)?user.username:""
		            },{
		            	xtype: 'textfield',
		            	id:'fldOCFirstName',
		            	fieldLabel: 'First Name',
		            	allowBlank:false,
		            	anchor: '100%',
		            	value:(user && user.firstname)?user.firstname:""
		            },{
		            	xtype: 'textfield',
		            	id:'fldOCMiddleName',
		            	fieldLabel: 'Middle Name',
		            	anchor: '100%',
		            	value:(user && user.middlename)?user.middlename:""
		            },{
		            	xtype: 'textfield',
		            	id:'fldOCLastName',
		            	fieldLabel: 'Last Name',
		            	allowBlank:false,
		            	anchor: '100%',
		            	value:(user && user.lastname)?user.lastname:""
		            },{
		            	xtype: 'textfield',
		            	id:'fldOCPhone',
		            	fieldLabel: 'Phone',
		            	anchor: '100%',
		            	value:(user && user.phone)?user.phone:""
		            },{
		            	xtype: 'textfield',
		            	id:'fldOCEmail',
		            	fieldLabel: 'Email',
		            	allowBlank:false,
		            	anchor: '100%',
		            	value:(user && user.email)?user.email:""
		            },{
		            	xtype: 'textfield',
		            	id:'fldOCDepartment',
		            	fieldLabel: 'Department',
		            	anchor: '100%',
		            	value:(user && user.department)?user.department:""
		            }
		            ];

		me.callParent();
	}
});