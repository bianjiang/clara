Ext.ns('Clara', 'Clara.NewSubmission');


Clara.NewSubmission.StaffResponsibilities = [{label:'Managing CLARA submission',inputValue:'Managing CLARA submission',help:'e.g., create, edit, and manage CLARA submission forms'},
                                             {label:'Recruiting Subjects',inputValue:'Recruiting Subjects',help:''},
                                             {label:'Obtaining Informed Consent',inputValue:'Obtaining informed consent',help:''},
                                             {label:'Performing non-invasive study activities',inputValue:'Performing non-invasive study activities',help:'e.g., recording observations, administering surveys, obtaining medical histories'},
                                             {label:'Performing invasive study activities',inputValue:'Perform invasive study activities',help:'e.g., drawing blood, collecting tissue samples'},
                                             {label:'Managing investigational product',inputValue:'Managing investigational product',help:'e.g., receiving, storing, dispensing, reconciling investigational product'},
                                             {label:'Managing data',inputValue:'Managing data',help:'e.g., entering CRFs, completing queries'},
                                             {label:'Receiving Confidential Information/Materials', inputValue:'Receiving Confidential Information Materials', help:'For CDAs and MTAs'},
                                             {label:'Providing Confidential Information/Materials', inputValue:'Providing Confidential Information Materials', help:'For CDAs and MTAs'},
                                             {label:'Receiving Limited Data Set under DUA', inputValue:'Receiving Limited Data Set under DUA', help:'For Data Use Agreements'},
                                             {label:'Providing Limited Data Set under DUA', inputValue:'Providing Limited Data Set under DUA', help:'For Data Use Agreements'},
                                             {label:'Performing regulatory duties',inputValue:'Performing regulatory duties',help:'e.g., assessing UPIRTOs, documenting deviations'},
                                             {label:'Advising student research',inputValue:'Advising student research',help:'e.g., guiding studies initiated by fellows, residents, post-docs'},
                                             {label:'Primary Budget Manager',inputValue:'Primary Budget Manager',help:'e.g. budget development, budget negotiation with Sponsor, completion of Sponsor\'s budget exhibit, terms and conditions with the Financial and Legal Units.'},
                                             {label:'Budget Manager',inputValue:'Budget Manager',help:''},
                                             {label:'Budget Administrator',inputValue:'Budget Administrator',help:'e.g. billing reconciliation, Invoicing the Sponsor for payment, managing the research study account.'},
                                             {label:'Data Analysis',inputValue:'Data Analysis',help:''},
                                             {label:'Research Not Involving Human Subjects',inputValue:'Research Not Involving Human Subjects',help:''},
                                             {label:'EMR Study Contact',inputValue:'EMR Study Contact',help:'EMR study contact information will auto-populate in UConnect (Epic) description field.  This contact information will display in the eMR system for studies requiring a CLARA budget.'}
                                             ];

Clara.NewSubmission.StaffRoles = [{label:'Principal Investigator',inputValue:'Principal Investigator',help:'The PI is the primary person responsible for the creation / management of the study.',responsibilityRequired:true},
                                  {label:'Co-Investigator',inputValue:'Co-Investigator',help:'',responsibilityRequired:true},
                                  {label:'Sub-Investigator',inputValue:'Sub-Investigator',help:'',responsibilityRequired:true},
                                  {label:'Study Coordinator',inputValue:'Study Coordinator',help:'',responsibilityRequired:true},
                                  {label:'Budget Manager',inputValue:'Budget Manager',help:'',responsibilityRequired:true},
                                  {label:'Budget Administrator',inputValue:'Budget Administrator',help:'',responsibilityRequired:true},
                                  {label:'Support Staff',inputValue:'Support Staff',help:'',responsibilityRequired:true},
                                  {label:'Mentor/Faculty Advisor',inputValue:'Mentor/Faculty Advisor',help:'',responsibilityRequired:true},
                                  {label:'Research Pharmacist',inputValue:'Research Pharmacist',help:'',responsibilityRequired:true},
                                  //{label:'Project Leader',inputValue:'Project Leader',help:'HSR Determination Only',responsibilityRequired:false},
                                  {label:'Treating Physician',inputValue:'Treating Physician',help:'HUD Only',responsibilityRequired:false},
                                  {label:'Research Administrator',inputValue:'Research Administrator',help:'No interaction with subjects or their data.',responsibilityRequired:false}
                                  ];

Clara.NewSubmission.ProtocolStaffSearchPanel = Ext.extend(Ext.FormPanel, {
        title: 'Search users',
        id:'protocol-staffwindow-search-panel',
        layout: 'border',
        constructor:function(config){           
                Clara.NewSubmission.ProtocolStaffSearchPanel.superclass.constructor.call(this, config);
        },
        initComponent: function() {
                var parentWindow = this.ownerCt.ownerCt;
                var t = this;
                var config = {
                                disabled:(parentWindow.editing)?true:false,
                                                items: [{ xtype:'uxsearchfield',
                                                        store:parentWindow.userStore,
                                                        region:'north',
                                                        id:'normal-procedure-searchfield',
                                                        title:'Search for the user account you wish to add..',
                                                        emptyText:'Search by email address or last name only',
                                                        paramName : 'keyword',
                                                        beforeSearch: function(){
                                                                return (jQuery.trim(this.getRawValue()).length > 2);
                                                        }},
                                                        {
                                                                xtype: 'grid',

                                                                viewConfig: {
                                                                        forceFit: true,
                                                                        rowOverCls:'',
                                                                        emptyText: 'No search results.',
                                                                        headersDisabled:true
                                                                },
                                                                listeners: {
                                                                        render: function(grid) {
                                                                                grid.getView().el.select('.x-grid3-header').setStyle('display',    'none');
                                                                        }                
                                                                },
                                                                disableSelection:true,
                                                                stripeRows:true,
                                                                loadMask:true,
                                                                region: 'center',
                                                                itemId: 'gpSearchResults',
                                                                border: false,
                                                                store:parentWindow.userStore,
                                                                id: 'gpSearchResults',
                                                                columns: [
                                                                          {
                                                                                  xtype: 'gridcolumn',
                                                                                  dataIndex: 'id',
                                                                                  header: 'Column',
                                                                                  sortable: true,
                                                                                  renderer: function(v,s,r){
                                                                                          var rid = r.id;

                                                                                          var linkText = (r.get("id")>0)?"Choose":"Create Clara account and choose";
                                                                                          var rowcls = (r.get("id")>0)?"staff-search-user-exists":"staff-search-user-not-exists";
                                                                                                  str = "<div class='staff-search-row "+rowcls+"'>";
                                                                                          str = str + "<div class='staff-name'>"+r.get("firstname")+" "+r.get("lastname")+"</div>";
                                                                                          str = str + "<div class='staff-email'>"+r.get("email")+"</div>";
                                                                                          str += "<a class='staff-choose' href='javascript:Ext.getCmp(\""+parentWindow.id+"\").chooseUser(\""+rid+"\");'>"+linkText+"</a></div><div style='clear:both;'></div></div>";

                                                                                          return str;
                                                                                  }

                                                                          }
                                                                          ]
                                                        },{
                                                        	xtype:'panel',
                                                        	border:false,
                                                        	region:'south',
                                                        	padding:6,
                                                        	height:80,
                                                        	iconCls:'icn-user--exclamation',
                                                        	title:'Not listed?',
                                                        	html:'If the staff member you would like to add is not listed here, please submit a CLARA Account Request to the IRB.'
                                                        }
                                                        ]
                };
                Ext.apply(this, Ext.apply(this.initialConfig, config));
                Clara.NewSubmission.ProtocolStaffSearchPanel.superclass.initComponent.apply(this, arguments);
                parentWindow.userStore.removeAll();
        }
});
Ext.reg('claraprotocolstaffsearchpanel', Clara.NewSubmission.ProtocolStaffSearchPanel);

Clara.NewSubmission.StaffCostPanel  = Ext.extend(Ext.FormPanel, {
        title: 'Salary/FTE Estimates',
        id:'protocol-staffwindow-cost-panel',
        layout: 'fit',
        selectedSalaryRec:{},
        constructor:function(config){           
                Clara.NewSubmission.StaffCostPanel.superclass.constructor.call(this, config);
        },
        initComponent: function() {
                var parentWindow = this.ownerCt.ownerCt;
                var t = this;
                
                // clear salary store, then populate from object.
                if (parentWindow.editing) {
                        clog("editing.. costs",parentWindow.staff.costs);
                        parentWindow.salaryStore.removeAll();
                        parentWindow.salaryStore.loadData(parentWindow.staff.costs);
                        clog(parentWindow.salaryStore);
                }
                var config = {
                                disabled:(parentWindow.editing)?true:false,
                                                tbar: [{text:'Add time period',iconCls:'icn-money--plus',handler:function(){
                                                        var grid = Ext.getCmp("gpStaffSalary");
                                        // access the Record constructor through the grid's store
                                        var Row = parentWindow.salaryStore.recordType;
                                        var r = new Row({
                                            startdate: (new Date()).clearTime(),
                                            enddate: (new Date()).clearTime(),
                                            fte:0,
                                            salary:(Ext.getCmp("winStaff").user.annualSalary)?Ext.getCmp("winStaff").user.annualSalary:0
                                        });
                                        grid.stopEditing();
                                        parentWindow.salaryStore.insert(0, r);
                                        grid.startEditing(0, 0);
                                                }},'-',
                                                {text:'Remove..',id:'btnRemoveStaffSalaryRow',disabled:true,iconCls:'icn-money--minus',handler:function(){
                                                        var grid = Ext.getCmp("gpStaffSalary");
                                                        grid.stopEditing();
                                        parentWindow.salaryStore.remove(t.selectedSalaryRec);
                                        Ext.getCmp("btnRemoveStaffSalaryRow").setDisabled(true);
                                                }}],
                                                items: [
                                                        {
                                                                xtype: 'editorgrid',

                                                                viewConfig: {
                                                                        forceFit: true,
                                                                        rowOverCls:'',
                                                                        emptyText: 'No salary / FTE data entered.',
                                                                        headersDisabled:false
                                                                },
                                                                selModel:new Ext.grid.RowSelectionModel({
                                                                singleSelect:true,
                                                                listeners: {
                                                                        rowselect:function(sm,idx,r){
                                                                                t.selectedSalaryRec = r;
                                                                                        Ext.getCmp("btnRemoveStaffSalaryRow").setDisabled(false);
                                                                                }
                                                                }
                                                        }),
                                                                disableSelection:true,
                                                                stripeRows:true,
                                                                loadMask:true,
                                                                itemId: 'gpSearchResults',
                                                                border: false,
                                                                store:parentWindow.salaryStore,
                                                                id: 'gpStaffSalary',
                                                                columns: [
                                                                          {
                                                                                  xtype: 'gridcolumn',
                                                                                  dataIndex: 'startdate',
                                                                                  header: 'Start Date',
                                                                                  sortable: true,
                                                                                  editable:true,
                                                                                  format:'m/d/Y',
                                                                                  editor:{xtype:'datefield'},
                                                                                  renderer: Ext.util.Format.dateRenderer('m/d/Y')

                                                                          },{
                                                                                  xtype: 'gridcolumn',
                                                                                  dataIndex: 'enddate',
                                                                                  header: 'End Date',
                                                                                  sortable: true,
                                                                                  editable:true,
                                                                                  format:'m/d/Y',
                                                                                  editor:{xtype:'datefield'},
                                                                                  renderer: Ext.util.Format.dateRenderer('m/d/Y')

                                                                          },{
                                                                                  xtype: 'gridcolumn',
                                                                                  dataIndex: 'salary',
                                                                                  header: 'Salary',
                                                                                  sortable: true,
                                                                                  editable:true,
                                                                                  editor:{xtype:'numberfield'},
                                                                                  renderer: function(v,s,r){
                                                                                          return v;
                                                                                  }

                                                                          },{
                                                                                  xtype: 'gridcolumn',
                                                                                  dataIndex: 'fte',
                                                                                  header: 'FTE (percent)',
                                                                                  sortable: true,
                                                                                  editable:true,
                                                                                  editor:{xtype:'numberfield'},
                                                                                  renderer: function(v,s,r){
                                                                                          return v;
                                                                                  }

                                                                          },{
                                                                                  xtype: 'gridcolumn',
                                                                                  dataIndex: 'id',
                                                                                  header: 'FTE (amount)',
                                                                                  sortable: true,
                                                                                  editable:false,
                                                                                  renderer: function(v,s,r){
                                                                                          return ( parseFloat(r.get("salary")) * (parseFloat(r.get("fte"))/100));
                                                                                  }

                                                                          }
                                                                          ]
                                                        }
                                                        ]
                };
                Ext.apply(this, Ext.apply(this.initialConfig, config));
                Clara.NewSubmission.StaffCostPanel.superclass.initComponent.apply(this, arguments);
                parentWindow.userStore.removeAll();
        }
});
Ext.reg('claraprotocolstaffcostpanel', Clara.NewSubmission.StaffCostPanel);

Clara.NewSubmission.ProtocolStaffMiscPanel = Ext.extend(Ext.FormPanel,{
        title:"Notifications & Conflicts",
        id:'protocol-staffwindow-misc-panel',
        constructor:function(config){           
                Clara.NewSubmission.ProtocolStaffMiscPanel.superclass.constructor.call(this, config);
        },
        initComponent: function() {
                var parentWindow = this.ownerCt.ownerCt;
                var t = this;
                var config = {
                		labelWidth:250,
                		listeners:{
                			show: function(p){
                				var uid = (parentWindow.staff && parentWindow.staff.userid)?parentWindow.staff.userid:parentWindow.user.userid;
                				Ext.getCmp("gpStaffCoiList").store.proxy.setUrl(appContext + '/ajax/users/'+uid+'/coi/list');
                                Ext.getCmp("gpStaffCoiList").store.load();
                			}
                		},
                                items:[
                                        new Ext.form.RadioGroup({   
                                        	
                                            fieldLabel: "<div class='staffwindow-role'><div class='staffwindow-role-label'>Receive Notifications</div><div class='staffwindow-role-help'>This staff member will receive status notifications from Clara via email about this study.</div></div>",
                                            id:'staff-notify',
                                            labelSeparator: '',
                                            columns:[50,50],
                                            items:[
                                                   {boxLabel:'Yes',inputValue:'y',name: 'staff-notify',width:50, checked:(parentWindow.editing && parentWindow.staff.notify)},
                                                   {boxLabel:'No',inputValue:'n',name: 'staff-notify',width:50, checked:(parentWindow.editing && !parentWindow.staff.notify)}
                                                   ]
                                        }),
                                        {
                                                id:'coi-group-panel',
                                                iconCls:'icn-user-detective',
                                                labelWidth:450,
                                                title:'Study COI',
                                                xtype:'fieldset',
                                                labelSeparator: '',
                                                style:'margin-top:16px;',
                                                border:false,
                                                items:[
                                                       new Ext.form.RadioGroup({        
                                                           fieldLabel: 'Does this staff member have a conflict of interest that may impact this study?<br/><strong style="font-weight:800;color:red;">(If you are not sure, leave this question blank)</strong>',
                                                           id:'staff-coi',
                                                           columns:[50,50],
                                                           items:[
                                                                  {boxLabel:'Yes',inputValue:'true',name: 'conflict-of-interest',width:50, checked:(parentWindow.editing && parentWindow.staff.conflictofinterest == "true")},
                                                                  {boxLabel:'No',inputValue:'false',name: 'conflict-of-interest',width:50, checked:(parentWindow.editing && parentWindow.staff.conflictofinterest) == "false"},
                                                                  {boxLabel:'N/A',inputValue:'na',name: 'conflict-of-interest',width:50, checked:(parentWindow.editing && parentWindow.staff.conflictofinterest) == "na"}
                                                                  ]
                                                       })
                                                       ]
                                        },
                                        new Ext.form.TextArea({ 
                                            fieldLabel: 'Please describe the conflict:',
                                            width:350,
                                            name: 'conflict-of-interest-description',
                                            value: (parentWindow.editing)?parentWindow.staff.conflictofinterestdesc:""
                                        }),
                                        new Ext.form.Label({     
                                            html:'<strong>Important Note: </strong>This question does <strong>NOT</strong> replace your responsibility to keep the information in <strong>ClickCommerce COI</strong> current.'
                                        }),{
                                        	xtype:'grid',
                                        	id:'gpStaffCoiList',
                                        	iconCls:'icn-user-detective',
                                        	viewConfig:{
                                        		emptyText:'No COI information found in ClickCommerce for this user.'
                                        	},
                                        	autoScroll: true,
                                            border: false,
                                            stripeRows: true,
                                            title:'COI (via ClickCommerce)',
                                            height:100,
                                        	columns:[
{header: 'Desc', sortable: true, dataIndex: 'disclosureName'},
{header: 'Submitted', sortable: true, dataIndex: 'discDateLastSubmitted', xtype: 'datecolumn', format: 'm/d/Y'},
{header: 'Value', sortable: true, dataIndex: 'disclosureStates',width:190},
{header: 'Expires', sortable: true, dataIndex: 'expirationDate', xtype: 'datecolumn', format: 'm/d/Y'}
        ],
                                        	store:parentWindow.coiStore
                                        	
                                        }]
                };
                Ext.apply(this, Ext.apply(this.initialConfig, config));
                Clara.NewSubmission.ProtocolStaffMiscPanel.superclass.initComponent.apply(this, arguments);
        }

});
Ext.reg('claraprotocolstaffmiscpanel', Clara.NewSubmission.ProtocolStaffMiscPanel);



Clara.NewSubmission.ProtocolStaffTrainingPanel = Ext.extend(Ext.Panel,{
        title:"Training",
        layout:'fit',
        id:'protocol-staffwindow-training-panel',
        constructor:function(config){           
                Clara.NewSubmission.ProtocolStaffTrainingPanel.superclass.constructor.call(this, config);
        },
        initComponent: function() {
                var parentWindow = this.ownerCt.ownerCt;
                var t = this;
                var config = {
                		labelWidth:250,
                		listeners:{
                			show: function(p){
                				var uid = (parentWindow.staff && parentWindow.staff.userid)?parentWindow.staff.userid:parentWindow.user.userid;
                				Ext.getCmp("gpStaffTrainingList").store.proxy.setUrl(appContext + '/ajax/users/'+uid+'/citimember');
                                Ext.getCmp("gpStaffTrainingList").store.load();
                			}
                		},
                                items:[{
                                        	xtype:'grid',
                                        	id:'gpStaffTrainingList',
                                        	iconCls:'icn-certificate',
                                        	viewConfig:{
                                        		emptyText:'No training information found for this user.',
                                        		getRowClass: function(r, index) {
                                                    var c = r.get('dateCompletionExpires');
                                                    var dt = new Date();
                                                    if (c < dt) {
                                                        return 'summary-bar-status-WARN';
                                                    }
                                                }
                                        	},
                                        	autoScroll: true,
                                            border: false,
                                            stripeRows: true,
                                            title:'Citi Training Details',
                                        	columns:[
{header: 'Name', sortable: true, dataIndex: 'nameOfCurriculum'},
{header: 'Group', sortable: true, dataIndex: 'group'},
{header: 'Desc', sortable: true, dataIndex: 'stageDescription'}  ,
{header: 'Date Earned', sortable: true, dataIndex: 'dateCompletionEarned', xtype: 'datecolumn', format: 'm/d/Y'},
{header: 'Score', sortable: true, dataIndex: 'learnerScore',renderer:function(v,p,r){ return r.get("learnerScore")+" ("+r.get("passingScore")+" to pass)"; }},
{header: 'Expires', sortable: true, dataIndex: 'dateCompletionExpires', xtype: 'datecolumn', format: 'm/d/Y'}
        ],
                                        	store:parentWindow.trainingStore
                                        	
                                        }]
                };
                Ext.apply(this, Ext.apply(this.initialConfig, config));
                Clara.NewSubmission.ProtocolStaffTrainingPanel.superclass.initComponent.apply(this, arguments);
        }

});
Ext.reg('claraprotocolstafftrainingpanel', Clara.NewSubmission.ProtocolStaffTrainingPanel);

Clara.NewSubmission.ProtocolStaffRoleRespPanel = Ext.extend(Ext.FormPanel, {
        title: 'Roles',
        layout: 'form',
        listType:'role',
        constructor:function(config){           
                Clara.NewSubmission.ProtocolStaffRoleRespPanel.superclass.constructor.call(this, config);
        },

        getResponsibilityCheckboxGroup: function(){
                var t = this;
                var parentWindow = this.ownerCt.ownerCt;
                var items = [];
                for (var i=0;i<Clara.NewSubmission.StaffResponsibilities.length;i++){
                        var resp = Clara.NewSubmission.StaffResponsibilities[i];
                        items.push({
                                hideLabel:true,
                                fieldClass:'staffwindow-role-cb',
                                itemCls:'staffwindow-role-row',
                                boxLabel:"<div class='staffwindow-role'><div class='staffwindow-role-label'>"+resp.label+"</div><div class='staffwindow-role-help'>"+resp.help+"</div></div>",
                                inputValue:resp.inputValue,
                                name:'/responsibilities/responsibility',
                                checked:(parentWindow.editing && jQuery.inArray(resp.inputValue,parentWindow.staff.responsibilities) > -1)
                        });                      
                }

                return new Ext.form.CheckboxGroup({
                        id:'resp-group-cbgroup',
                        hideLabel:true,
                        xtype:'checkboxgroup',
                        name:'resplist',
                        columns:1,
                        items:items});

        },

        getRoleCheckboxGroup: function(){
                var t = this;
                var parentWindow = this.ownerCt.ownerCt;
                var items = [];
                
                var studyHasPI = false,studyHasPL = false,studyHasTP = false;
                var staffPanel = Ext.getCmp("protocol-staff-panel");
                if (typeof staffPanel != "undefined" && staffPanel){
                	
                	clog("CHECKING EXISTING ROLES")
                	staffPanel.getStore().findBy(function(r,id){
                		clog(r.get("roles"));
                		for (var i=0, l=r.data.roles.length;i<l; i++) {
	                  		if (jQuery.browser.msie){
	                  			studyHasPI = studyHasPI || (r.data.roles[i].node.text == "Principal Investigator");
	                  			//studyHasPL = studyHasPL || (r.data.roles[i].node.text == "Project Leader");
	                  			studyHasTP = studyHasTP || (r.data.roles[i].node.text == "Treating Physician");
	                  		}
	                  		else {
	                  			studyHasPI = studyHasPI || (r.data.roles[i].node.textContent == "Principal Investigator");
	                  			//studyHasPL = studyHasPL || (r.data.roles[i].node.textContent == "Project Leader");
	                  			studyHasTP = studyHasTP || (r.data.roles[i].node.textContent == "Treating Physician");
	                  		}
	                  	}
                	});
                }
                
                for (var i=0;i<Clara.NewSubmission.StaffRoles.length;i++){
                        var role = Clara.NewSubmission.StaffRoles[i];
                        
                        // check for existing PI. if so, skip it.
                        if (
                        		(studyHasPI && role.inputValue == "Principal Investigator" && (!parentWindow.editing || !(parentWindow.editing && jQuery.inArray("Principal Investigator",parentWindow.staff.roles) > -1)))
                        			//|| (studyHasPL && role.inputValue == "Project Leader" && (!parentWindow.editing || !(parentWindow.editing && jQuery.inArray("Project Leader",parentWindow.staff.roles) > -1)))
                        			|| (studyHasTP && role.inputValue == "Treating Physician" && (!parentWindow.editing || !(parentWindow.editing && jQuery.inArray("Treating Physician",parentWindow.staff.roles) > -1)))
                        	){
                        	// nothing
                        } else {
                        	items.push({
                                hideLabel:true,
                                fieldClass:'staffwindow-role-cb',
                                itemCls:'staffwindow-role-row',
                                boxLabel:"<div class='staffwindow-role'><div class='staffwindow-role-label'>"+role.label+"</div><div class='staffwindow-role-help'>"+role.help+"</div></div>",
                                inputValue:role.inputValue,
                                name:'/roles/role',
                                checked:(parentWindow.editing && jQuery.inArray(role.inputValue,parentWindow.staff.roles) > -1)
                        	}); 
                        }
                        
                                             
                }

                return new Ext.form.CheckboxGroup({
                        id:'role-group-cbgroup',
                        hideLabel:true,
                        xtype:'checkboxgroup',
                        name:'rolelist',
                        columns:1,
                        items:items});

        },

        initComponent: function() {
                var parentWindow = this.ownerCt.ownerCt;
                var t = this;
                var config = {
                                items: [(t.listType == "role")?t.getRoleCheckboxGroup():t.getResponsibilityCheckboxGroup()]
                };
                Ext.apply(this, Ext.apply(this.initialConfig, config));
                Clara.NewSubmission.ProtocolStaffRoleRespPanel.superclass.initComponent.apply(this, arguments);
                parentWindow.userStore.removeAll();
        }
});
Ext.reg('claraprotocolstaffroleresppanel', Clara.NewSubmission.ProtocolStaffRoleRespPanel);

Clara.NewSubmission.ProtocolStaffWindow = Ext.extend(Ext.Window, {
        id:'winStaff',
        title: 'Staff',
        width: 640,
        height: 500,
        layout: 'fit',
        user:null,
        staff:null,
        editing:false,

        createAccount: function(user){
        	var username = user.get("username");
        	jQuery.ajax({
        		url: appContext + "/ajax/users/createuseraccount",
        		type: "GET",
        		dataType: "json",
        		data: "username="+username,
        		async: false,
        		success: function(data){
        			data.userid = data.userId;				// We're no longer getting a user object like from the search results.
        			clog("createAccount: saved rec",data);
        			user.set("id",data.id);
        			user.set("userid",data.id);
        			user.commit();
        		}
        	});
        },
        
        chooseUser: function(recordIndex){
                clog(recordIndex);
                var rec = this.userStore.getById(recordIndex);
                
                if (rec.get('id') == 0){
                	this.createAccount(rec);
                }
                
                var p = rec.data;
                var t = this;
                t.setTitle("New Staff: "+p.firstname+" "+p.lastname);
                t.user = p;
                clog(t.user);
                Ext.getCmp("btnCloseStaffWindow").setDisabled(false);
                Ext.getCmp("protocol-staffwindow-role-panel").setDisabled(false);
                Ext.getCmp("protocol-staffwindow-tabpanel").setActiveTab(1);
                Ext.getCmp("protocol-staffwindow-resp-panel").setDisabled(false);
                Ext.getCmp("protocol-staffwindow-misc-panel").setDisabled(false);
                Ext.getCmp("protocol-staffwindow-training-panel").setDisabled(false);
                Ext.getCmp("protocol-staffwindow-cost-panel").setDisabled(false);
                
        },

        saveStaff: function(){
                var t =  this;

                if (!t.editing && !t.staff) t.staff = new Clara.NewSubmission.Staff({});

                this.staff.costs.splice(0,this.staff.costs.length);
                t.salaryStore.each(function(r){
                        t.staff.costs.push([Ext.util.Format.date(r.get("startdate"),'m/d/Y'),Ext.util.Format.date(r.get("enddate"),'m/d/Y'),r.get("salary"),r.get("fte")]);
                });
                
                clog("ROLES RESPS",t.staff.roles,t.staff.responsibilities);
                
                this.staff.roles.splice(0,this.staff.roles.length);
                jQuery.each(Ext.getCmp("role-group-cbgroup").getValue(), function(idx, cb){
                        clog("PUSHING ROLE",cb.inputValue);
                        t.staff.roles.push(cb.inputValue);
                });

                this.staff.responsibilities.splice(0,this.staff.responsibilities.length);
                jQuery.each(Ext.getCmp("resp-group-cbgroup").getValue(), function(idx, cb){
                        clog("PUSHING RESP",cb.inputValue);
                        t.staff.responsibilities.push(cb.inputValue);
                });

                clog("ROLES RESPS NOTIFY",t.staff.roles,t.staff.responsibilities, jQuery("input:radio[name=staff-notify]:checked").val());
                
                var roleOk = (t.staff.roles.length > 0);
                var respOk = true;
                var notifyOk = (typeof jQuery("input:radio[name=staff-notify]:checked").val() != 'undefined' && jQuery("input:radio[name=staff-notify]:checked").val().length > 0);
                
                var respNeeded = false;
                
                for (var i=0;i<t.staff.roles.length;i++){
                        for (var j=0;j<Clara.NewSubmission.StaffRoles.length;j++){
                                if (t.staff.roles[i] == Clara.NewSubmission.StaffRoles[j].inputValue){
                                        if (Clara.NewSubmission.StaffRoles[j].responsibilityRequired == true && t.staff.responsibilities.length > 0){
                                                respNeeded = true;
                                                respOk = true;
                                        } else if (Clara.NewSubmission.StaffRoles[j].responsibilityRequired == true && t.staff.responsibilities.length == 0){
                                                respNeeded = true;
                                                respOk = false;
                                        } else if (Clara.NewSubmission.StaffRoles[j].responsibilityRequired == false && respNeeded == false){
                                                respOk = true;
                                        }
                                }
                        }
                }
                
                if (respOk && roleOk && notifyOk){

                        if (!t.editing){
                                // For new staff
                                if (!t.staff) t.staff = new Clara.NewSubmission.Staff({});
                                if (t.user.userid == 0) {
                                        // For new users (must save user record in Clara)
                                        var url = appContext + '/ajax/users/createuseraccount';
                                        jQuery.ajax({
                                                async: false,
                                                url: url,
                                                type: "GET",
                                                dataType: "json",
                                                data: "username="+t.user.username,
                                                success: function(savedUser){
                                                        t.user.id = savedUser.id;

                                                }
                                        });
                                }

                                t.staff.userid = t.user.userid;
                                t.staff.firstname = t.user.firstname;
                                t.staff.lastname = t.user.lastname;
                                t.staff.email = t.user.email;
                                t.staff.sap = t.user.sap;
                                t.staff.phone = t.user.workphone;

                        }
                        t.staff.notify = (jQuery("input:radio[name=staff-notify]:checked").val() == 'y');
                        t.staff.conflictofinterest = jQuery("input:radio[name=conflict-of-interest]:checked").val();
                        t.staff.conflictofinterestdesc = jQuery("textarea[name=conflict-of-interest-description]").val();


                        clog(t.staff);
                        if (t.editing) t.staff.update();
                        else t.staff.save();
                        Ext.getCmp("protocol-staff-panel").loadStaff();
                        t.close();
                } else {
                        var txt = (roleOk == false)?"* Choose at least one role.\n":"";
                        txt += (respOk == false)?"* Choose at least one responsibility.\n":"";
                        txt += (!notifyOk)?"* Select whether this staff member should recieve notifications.":"";
                        alert(txt);
                }
        },

        trainingStore: new Ext.data.Store({
			autoLoad: false,
			header :{
		           'Accept': 'application/json'
		       },
			proxy: new Ext.data.HttpProxy({
				url: appContext + '/ajax/users/ID/citimember', //dynamically set later
				method:'GET'
			}),
			reader: new Ext.data.JsonReader({
                root: 'data',
                idProperty: 'id'
			},
			[
			    {name:'id'},
				{name:'registrationDate', type: 'date'},
				{name:'nameOfCurriculum'},
				{name:'group',mapping:'group'},
				{name:'stageNumber'},
				{name:'stageDescription'},
				{name:'completionReportNumber'},
				{name:'dateCompletionEarned', type: 'date'},
				{name:'learnerScore'},
				{name:'passingScore'},
				{name:'dateCompletionExpires', type: 'date'}
			])
		}),
        
        userStore:      new Ext.data.Store({
                header :{
                        'Accept': 'application/json'
                },
                proxy: new Ext.data.HttpProxy({
                        url: appContext + '/ajax/users/persons/search',
                        method:'GET'
                }),
                reader: new Ext.data.JsonReader({
                        root: 'persons',
                        idProperty: 'username'
                }, [
                    {name:'id'},
                    {name:'userid',mapping:'userId'},
                    {name:'sap'},
                    {name:'username'},
                    {name:'annualSalary'},
                    {name:'firstname'},
                    {name:'lastname'},
                    {name:'email'},
                    {name:'workphone'}
                    ])
        }),
        
        coiStore: new Ext.data.Store({
    		autoLoad: false,
    		header :{
    	           'Accept': 'application/json'
    	       },
    		proxy: new Ext.data.HttpProxy({
    			url: '/what/ever',
    			method:'GET',
    			reader: new Ext.data.JsonReader({
    				idProperty:'id',
    				root:'data'
    			},[
    		    {name:'id'},
    		    {name:'discDateLastSubmitted', type: 'date',convert: function(v,rec){
    		    	return new Date(v);
    		    }},
    		    {name:'expirationDate', type: 'date',convert: function(v,rec){
    		    	return new Date(v);
    		    }},
    			{name:'nameOfCurriculum'},
    			{name:'disclosureStates'},
    			{name:'disclosureName'},
    			{name:'sapId'},
    			{name:'firstName'},
    			{name:'lastName'}
    		])
    		})
    	}),

        salaryStore: new Ext.data.Store({
                reader: new Ext.data.ArrayReader({},[{name:'startdate',type:'date', dateFormat:'m/d/Y'},{name:'enddate',type:'date', dateFormat:'m/d/Y'},{name:'salary',type:'float'},{name:'fte',type:'float'}]),
                autoLoad:false,
                sortInfo:{field:'startdate', direction:'ASC'}
        }),

        initComponent: function() {
        
                var t = this;
            	t.salaryStore.removeAll();
                this.editing = (this.staff)?true:false;
                
                clog("StaffWindow: Editing? "+this.editing,t.staff,t.user);
                
                this.buttons = [{
                        id:'btnCloseStaffWindow',
                        text:'Save and Close',
                        disabled:!this.editing,
                        handler: function(){t.saveStaff();}
                }];
                this.title = (this.editing)?("Editing Staff '"+this.staff.firstname+" "+this.staff.lastname+"'.."):"New Staff";

                this.items = [{xtype:'tabpanel',id: "protocol-staffwindow-tabpanel",deferredRender:false,border:false,items:[{xtype:'claraprotocolstaffsearchpanel'},
                                                                                                        {xtype:'claraprotocolstaffroleresppanel',id: "protocol-staffwindow-role-panel", listType:'role',disabled:true},
                                                                                                        {xtype:'claraprotocolstaffroleresppanel',id: "protocol-staffwindow-resp-panel", title:'Responsibilities',listType:'resp',disabled:true},
                                                                                                        {xtype:'claraprotocolstaffmiscpanel',disabled:true},
                                                                                                        {xtype:'claraprotocolstaffcostpanel',disabled:true},
                                                                                                        {xtype:'claraprotocolstafftrainingpanel',disabled:true}
                                                                                                        ]}];



                Clara.NewSubmission.ProtocolStaffWindow.superclass.initComponent.call(this);  
                Ext.getCmp("protocol-staffwindow-role-panel").setDisabled(!this.editing);
                Ext.getCmp("protocol-staffwindow-resp-panel").setDisabled(!this.editing);
                Ext.getCmp("protocol-staffwindow-misc-panel").setDisabled(!this.editing);
                Ext.getCmp("protocol-staffwindow-cost-panel").setDisabled(!this.editing);
                Ext.getCmp("protocol-staffwindow-training-panel").setDisabled(!this.editing);
                Ext.getCmp("protocol-staffwindow-tabpanel").setActiveTab((this.editing)?1:0);
                clog("window init dont. staff",t.staff);
        }
});
