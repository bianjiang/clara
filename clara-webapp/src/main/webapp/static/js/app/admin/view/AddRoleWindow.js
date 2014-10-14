Ext.define('Clara.Admin.view.AddRoleWindow', {
	extend: 'Ext.window.Window',
	alias: 'widget.addrolewindow',
	layout: 'form',
	width:450,
	padding: 6,
	bodyPadding:6,
	modal: true,
	title:'Add Role',
	user:{},
	selectedRole:{},
	selectedCollege:{},
	selectedDept:{},
	selectedSubDept:{},
	isDelegate:false,
	isBusinessAdmin:false,
	
	initComponent: function() {
		var t = this;
		var roleStore = Ext.data.StoreManager.lookup('Roles');
		var collegeStore = Ext.data.StoreManager.lookup('Clara.Common.store.Colleges');
		var deptStore = Ext.data.StoreManager.lookup('Clara.Common.store.Departments');
		var subdeptStore = Ext.data.StoreManager.lookup('Clara.Common.store.Subdepartments');
		clog("USER",t.user);
		t.title = "Add Role for "+t.user.get("username");
		this.buttons = [
		                {
		                	text: 'Add',
		                	id:'btnSaveRole',
		                	disabled:true,
		                	handler: function(){

								var deptId = null;
								
								if (Ext.getCmp("fldCollege").isVisible() && typeof t.selectedCollege.get != "function"){
									alert("Choose a college before saving.");
								} else if (Ext.getCmp("fldDept").isVisible() && typeof t.selectedDept.get != "function") {
									alert("Choose a department before saving.");
								} else {
								
								
									clog("SAVING WITH DEPT LEVEL "+t.selectedRole.get("departmentLevel"));
									if (t.selectedRole.get("departmentLevel") == "SUB_DEPARTMENT"){
										deptId = t.selectedSubDept.get("id");
									} else if (t.selectedRole.get("departmentLevel") == "DEPARTMENT"){
										deptId = t.selectedDept.get("id");
									} else if (t.selectedRole.get("departmentLevel") == "COLLEGE"){
										clog("SAVING WITH COLLEGE LEVEL ");
										clog(t.selectedCollege);
										deptId = t.selectedCollege.get("id");
									}
									clog(t.selectedCollege);
									
									var rdata = {roleId:t.selectedRole.get("id")};
									if (deptId != null){
										rdata.dId = deptId;
									}
									rdata.isDelegate = t.isDelegate;
									rdata.isBusinessAdmin = t.isBusinessAdmin;
									clog("rdata",rdata);
									
									jQuery.ajax({
							    		url: appContext + "/ajax/users/"+t.user.get("userid")+"/user-roles/create",
							    		type: "GET",
							    		async: false,
							    		data: rdata,
							    		success: function(data){
							    			t.fireEvent('userroleadded', t.user);	// function is in controller
							    			t.close();
							    		}
							    	});
								}
						
		                	}
		                }
		                ];
		this.items = [{
			xtype: 'combo',
			flex: 1,
			scope:this,
			fieldLabel:'Role',
			store:roleStore,
			typeAhead:false,
			forceSelection:true,
			displayField:'displayName', 
			editable:false,
			allowBlank:false,
			mode:'local', 
			triggerAction:'all',
			name: 'fldRole',
			id: 'fldRole',
			listeners:{
				'select': function(cmb,recs,idx){
					t.selectedRole = recs[0];			            			

					clog("SELECTED ROLE",this.selectedRole);
					Ext.getCmp('btnSaveRole').enable();
					Ext.getCmp('fldDept').clearValue();
					Ext.getCmp('fldSubDept').clearValue();
					Ext.getCmp('fldDept').disable();
					Ext.getCmp('fldDept').hide();

					Ext.getCmp('fldSubDept').disable();
					Ext.getCmp('fldSubDept').hide();


					Ext.getCmp('fldIsDelegate').setVisible(false);
					Ext.getCmp('fldIsBusinessAdmin').setVisible(false);
					
					if (t.selectedRole.get("departmentLevel") != null){

						Ext.getCmp('fldCollege').show();
						
						Ext.getCmp('fldCollege').clearValue();
						collegeStore.getProxy().url = appContext + '/ajax/colleges/list';
						collegeStore.load();
						Ext.getCmp('fldCollege').enable();
						Ext.getCmp('fldIsDelegate').setVisible(true);
						Ext.getCmp('fldIsBusinessAdmin').setVisible(true);
					}
				}
			}
		},{
			xtype: 'combo',
			scope:this,
			flex: 1,
			hidden:true,
			itemId: 'fldCollege',
			fieldLabel:'College',
			name: 'fldCollege',
			disabled: true,
			store:collegeStore,
			id: 'fldCollege',
			typeAhead:false,
			forceSelection:true,
			displayField:'name', 
			editable:false,
			allowBlank:false,
			mode:'local', 
			triggerAction:'all',
			listeners:{
				'select': function(cmb,recs,idx){
					t.selectedCollege = recs[0];
					clog("SELECTED COLLEGE",t.selectedCollege, "FOR ROLE",t.selectedRole);
					Ext.getCmp('fldDept').clearValue();
					Ext.getCmp('fldSubDept').clearValue();
					Ext.getCmp('fldSubDept').disable();
					clog("t.selectedRole.departmentLevel",t.selectedRole.get("departmentLevel"));
					if (t.selectedRole.get("departmentLevel") == "SUB_DEPARTMENT" || t.selectedRole.get("departmentLevel") == "DEPARTMENT"){

						deptStore.getProxy().url = appContext + '/ajax/colleges/'+t.selectedCollege.get("id")+'/departments/list';
						deptStore.load();
						Ext.getCmp('fldDept').show();

						Ext.getCmp('fldDept').enable();
					}
				}
			}
		},{
			xtype: 'combo',
			flex: 1,
			hidden:true,
			itemId: 'fldDept',
			name: 'fldDept',
			fieldLabel:'Department',
			typeAhead:false,
			forceSelection:true,
			displayField:'name', 
			editable:false,
			allowBlank:false,
			mode:'local', 
			triggerAction:'all',
			store: deptStore,
			disabled: true,
			id: 'fldDept',
			listeners:{
				'select': function(cmb,recs,idx){
					t.selectedDept = recs[0];
					clog(t.selectedDept);
					Ext.getCmp('fldSubDept').clearValue();
					clog("t.selectedRole.departmentLevel",t.selectedRole.get("departmentLevel"));
					if (t.selectedRole.get("departmentLevel") == "SUB_DEPARTMENT"){

						subdeptStore.getProxy().url = appContext + '/ajax/colleges/'+t.selectedCollege.get("id")+'/departments/'+t.selectedDept.get("id")+'/sub-departments/list';
						subdeptStore.load();
						Ext.getCmp('fldSubDept').show();

						Ext.getCmp('fldSubDept').enable();
					}
				}
			}

		},{

			xtype: 'combo',
			flex: 1,
			hidden:true,
			itemId: 'fldSubDept',
			fieldLabel:'Subdepartment',
			name: 'fldSubDept',
			typeAhead:false,
			forceSelection:true,
			displayField:'name', 
			editable:false,
			allowBlank:false,
			mode:'local', 
			triggerAction:'all',
			store: subdeptStore,
			disabled: true,
			id: 'fldSubDept',
			listeners:{
				'select': function(cmb,recs,idx){
					t.selectedSubDept = recs[0];
					clog(t.selectedSubDept);
				}
			}

		},
		{
            xtype: 'checkbox',
            flex:1,
            label:'Act as delegate',
            boxLabel: 'Delegate',
            hidden:true,
            name: 'fldIsDelegate',
            itemId: 'fldIsDelegate',
            id: 'fldIsDelegate',
            listeners:{
            	change: function(cb,v,ov){
            		clog("check",v);
            		t.isDelegate = v;
            	}
            }
        },
		{
            xtype: 'checkbox',
            flex:1,
            label:'Business Admin',
            boxLabel: 'Business Admin',
            hidden:true,
            name: 'fldIsBusinessAdmin',
            itemId: 'fldIsBusinessAdmin',
            id: 'fldIsBusinessAdmin',
            listeners:{
            	change: function(cb,v,ov){
            		clog("check",v);
            		t.isBusinessAdmin = v;
            	}
            }
        }];

		this.callParent();
	}
});