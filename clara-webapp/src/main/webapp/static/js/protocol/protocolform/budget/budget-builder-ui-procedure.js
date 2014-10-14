Ext.ns('Clara.BudgetBuilder');

Clara.BudgetBuilder.ShowSubprocedures = function(id){
	var epoch = Ext.getCmp("budget-tabpanel").activeEpoch;
	var proc = epoch.getProcedureById(id);
	new Clara.BudgetBuilder.ProcedureWindow({readOnly:!Clara.BudgetBuilder.canEdit(), procedure:proc}).openToTab(3);
};


Clara.BudgetBuilder.RequestCodeWindow = Ext.extend(Ext.Window, {
    title: 'Request code update..',
    width: 300,
    height: 250,
    layout: 'form',
    modal:true,
    resizable: false,
	constructor:function(config){		
		Clara.BudgetBuilder.RequestCodeWindow.superclass.constructor.call(this, config);
	},
	padding:8,
	initComponent: function() {
		
		var t = this;
		
		var config = {
				buttons:[{
					text:'Submit',
					handler:function(){
						var url=appContext+"/ajax/budget-request-code";
						jQuery.ajax({
							  type: 'POST',
							  async:false,
							  url: url,
							  data:{
								  cpt: Ext.getCmp("fldRequestCPTCode").getValue(),
								  //cdm: Ext.getCmp("fldRequestCDM").getValue(),
								  description: Ext.getCmp("fldRequestCodeDescription").getValue(),
								  //cost: Ext.getCmp("fldRequestCodeCost").getValue(),
								  //offer: Ext.getCmp("fldRequestCodeOffer").getValue(),
								  //price: Ext.getCmp("fldRequestCodePrice").getValue(),
								  note: Ext.getCmp("fldRequestCodeNote").getValue(),
								  userId: claraInstance.user.id,
								  committee: claraInstance.user.committee,
								  protocolId: claraInstance.id,
								  formId: claraInstance.form.id,
								  budgetId: claraInstance.budget.id
							  },
							  success: function(data){
								  t.close();
							  },
							  error: function(){
								  t.close();
							  }
						});
					}
				}],
				items:[{
					xtype:'textfield',
					fieldLabel:'CPT Code',
					id:'fldRequestCPTCode'
				},{
					xtype:'textarea',
					fieldLabel:'Description',
					id:'fldRequestCodeDescription'
				},{
					xtype:'textarea',
					fieldLabel:'Note',
					id:'fldRequestCodeNote'
				}]
		};

		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.BudgetBuilder.RequestCodeWindow.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('clarabudgetrequestcodewindow', Clara.BudgetBuilder.RequestCodeWindow);





Clara.BudgetBuilder.EditVisitProcedurePopup = Ext.extend(Ext.Window, {
    title: 'Edit Visit-Procedure',
    width: 180,
    height: 90,
    layout: 'absolute',
    modal:true,
    vid:0,
    pid:0,
    editType:'vp',
    cid:'',
    vp:{},
    vtype:'',
    vreps:1,
    resizable: false,
	constructor:function(config){		
		Clara.BudgetBuilder.EditVisitProcedurePopup.superclass.constructor.call(this, config);
	},
	initComponent: function() {
		this.title = (this.editType == "vp")?"Edit Visit-Procedure":"Set all for column..";
		var jqselector = (this.editType == "vp")?"#":".";
		this.x = jQuery(jqselector+this.cid).offset().left-83;
		this.y = jQuery(jqselector+this.cid).offset().top-93;		
		
		// For visit-procedure edits
		if (this.editType == "vp"){ //pid > 0){
			this.vp=budget.getVisitProcedure(this.vid,this.pid);
		
			if (!this.vp){
				this.vp = new Clara.BudgetBuilder.VisitProcedure({
					procedureid:this.pid,
					visitid: this.vid
				});
			}

		} 
		
		var epoch = Ext.getCmp("budget-tabpanel").activeEpoch;
		var proc = epoch.getProcedureById(this.pid);
		var visit = epoch.getVisitById(this.vid);
		var th = this;
		
		clog(proc);
		
		var allowableTypes = [];
        if (proc.type == 'outside'){
        	allowableTypes.push(["O"]);
        } else if (proc.type == 'misc' && proc.cptCode+"" == "123") {
        	allowableTypes.push(["COR"]);
        } else {
        	allowableTypes = [['R'],['C'],['I'],['O'],['RNS'],['CNMS'],['CL']];
        }

		
		var config = {
				
				items: [
				        {
			                xtype: 'combo',
			                x: 65,
			                y: 5,
			                width: 50,
			                typeAhead: false,
			                editable:false,
                            triggerAction: 'all',
                            store: new Ext.data.SimpleStore({
                               fields:['type'],
                               data: allowableTypes
                            }),
                            lazyRender: true,
                            displayField:'type',
                            mode:'local',
                            selectOnFocus:true,
                            value:(this.editType == "vp")?this.vp.type:'',
                            listeners:{
			            		scope:this,
			            		change:function(f,v,ov){
			            			if (this.editType == "vp") this.vp.type = v;
			            			else this.vtype = v;
			            		}
			            	},
                            listClass: 'x-combo-list-small'
			            },
			            {
			                xtype: 'label',
			                text: 'Category',
			                x: 5,
			                y: 5,
			                style: 'font-size:14px;'
			            },
			            {
			                xtype: 'label',
			                text: '#',
			                x: 120,
			                y: 5,
			                style: 'font-size:14px;',
			                width: 20
			            },
			            {
			            	scope:this,
			                xtype: 'numberfield',
			                x: 135,
			                y: 5,
			                width: 25,
			                enableKeyEvents:true,
			                value:(this.editType == "vp")?this.vp.repetitions:1,
			                listeners:{
			            		scope:this,
			            		change:function(f,v,ov){
					            	if (this.editType == "vp")
			            				this.vp.repetitions = v;
			            			else
			            				this.vreps = v;
			            		},keydown:function(f,e){
			            			if (this.editType == "vp")
			            				this.vp.repetitions = f.getValue();
			            			else
			            				this.vreps = f.getValue();
			            		}
			            	}
			            },
			            {
			            	scope:this,
			                xtype: 'button',
			                text: 'Edit name',
			                hidden:(this.editType == "vp"),
			                handler:function(b,e){
			            	    var epoch = Ext.getCmp("budget-tabpanel").activeEpoch;
			            	    
			            	    var newName = "";
			           
			            	    
			            	    Ext.Msg.prompt("Edit visit name", "Enter new visit name:", function(btn,text){
									newName = text;
									if (jQuery.trim(newName)!="" && newName != budget.getVisit(th.vid).name){
					            	    Clara.BudgetBuilder.SaveAction = "Edit Visit Name";
					            	    budget.getVisit(th.vid).name = newName;
					            		budget.addOrUpdateVisitProcedure(th.vp);
					            		budget.save();
					            		Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', epoch);
					            		Clara.BudgetBuilder.MessageBus.fireEvent('procedurechanged', proc);
					            		th.close();
				            	    }
								},this,true,budget.getVisit(th.vid).name);
			            	    
			            	    
			            	},
			                x: 5,
			                y: 32,
			                width: 52
			            },
			            {
			            	scope:this,
			                xtype: 'button',
			                x: 80,
			                y: 32,
			                width: 38,
			                text:'Clear',
			                handler:function(b,e){
			            		var t =this;
			                	Ext.Msg.show({
			                		title:"WARNING: About to clear visit",
			                		msg:"This will remove all billing categories for all procedures for this visit. Are you sure you want to do this?", 
			                		buttons:Ext.Msg.YESNOCANCEL,
			                		icon:Ext.MessageBox.WARNING,
			                		fn: function(btn){
			                			if (btn == 'yes'){
			                				var epoch = Ext.getCmp("budget-tabpanel").activeEpoch;
						            		if (t.editType == "vp") {
						            			////cdebug(this.vp);
						            			Clara.BudgetBuilder.SaveAction = "Clear Single Visit";
						            			budget.removeVisitProcedure(t.vp.visitid, t.vp.procedureid);
						            		} else {
						            			Clara.BudgetBuilder.SaveAction = "Clear Visit Column";
						            			budget.getVisit(t.vid).setAllVisitProcedures();
						            		}
						            		budget.save();
						            		Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', epoch);
						            		t.close();
			                			}
			                		}
			                		
			                	});
			                	
			            		
			            	}
			            },
			            {
			            	scope:this,
			                xtype: 'button',
			                text: (this.editType == "vp")?'Save':'Set all',
			                handler:function(b,e){
			            	    var epoch = Ext.getCmp("budget-tabpanel").activeEpoch;
				            	if (this.editType == "vp") {
					            	if (this.vp.repetitions && this.vp.type && this.vp.type != "" && this.vp.repetitions > 0)
					            	{
					            		Clara.BudgetBuilder.SaveAction = "Edit Single Visit";
					            		budget.addOrUpdateVisitProcedure(this.vp);
					            		budget.save();
					            		Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', epoch);
					            		this.close();
					            	}else{
					            		Ext.Msg.show({
					            			title:'Missing input...',
					            			width:350,
					            			msg:'WARNING: You need to specify the type of the procedure and number of occurrence!',
					            			buttons:Ext.Msg.OK,
					            		    icon: Ext.MessageBox.WARNING
					            		});
					            	}
				            	} else {
				            		
				            		if (th.vreps && th.vtype && th.vtype != "" && th.vreps > 0)
					            	{
				            			Clara.BudgetBuilder.SaveAction = "Edit Visit Column";
				            			budget.getVisit(th.vid).addAllProceduresToVisit(epoch.procedures, th.vtype, th.vreps, true);
				            			budget.save();
				            			Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', epoch);
				            			this.close();
					            	}else{
					            		Ext.Msg.show({
					            			title:'Missing input',
					            			width:350,
					            			msg:'You need to specify the type of the procedure and number of occurrences.',
					            			buttons:Ext.Msg.OK,
					            		    icon: Ext.MessageBox.WARNING
					            		});
					            	}
				            	}
			            	},
			                x: 120,
			                y: 32,
			                width: 45
			            }
					 ]
		};
		
		if(this.editType == "vp" && proc && visit.hasOfficeVisitItemSubprocedures(proc)){
			
			this.height = 332;
			
			var officeVisitItemsGrid = {
	                xtype: 'grid',
	                x: 5,
	                y: 65,           
	                width: 156,
	                height: 228,
	                border: true,
	                store:new Ext.data.Store({
	    				reader: new Ext.data.ArrayReader({},[
	    					'procid','desc','repetitions'
	    				]),
	    				autoLoad:false,
	    				remoteSort:false
	    			}),                
	                columns: [
	                    {
	                        xtype: 'gridcolumn',
	                        header: '(#) Office Visit Item',
	                        dataIndex: 'desc',
	                        sortable: true,
	                        width:150,
	                        renderer:function(v,p,r){
	                        	return "<div class='budget-office-visit-row wrap'><span style='font-weight:800;color:#000;font-size:11px;'>("+r.get("repetitions")+")</span> "+v+"</div>";
	                        }
	                    }
	                ]
				};
			
			if(proc.subprocedures && proc.subprocedures.length > 0){
				
				////cdebug(visit);
				var arrayData = [];
				var vp = {};
				
				for(var i = 0; i < proc.subprocedures.length; i ++){
					if(visit.hasSubProcedureMarked(proc.subprocedures[i])){
						vp = budget.getVisitProcedure(visit.id,proc.subprocedures[i].id);
						arrayData.push([proc.subprocedures[i].id, proc.subprocedures[i].description, vp.repetitions]);
					}
				}
				//cdebug(arrayData);
				officeVisitItemsGrid.store.loadData(arrayData);
			}
			
			////cdebug(proc.subprocedures);
			
			config.items.push(officeVisitItemsGrid);
			
		}
		
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.BudgetBuilder.EditVisitProcedurePopup.superclass.initComponent.apply(this, arguments);
		////cdebug(this);
	}
});
Ext.reg('claraeditvppopup', Clara.BudgetBuilder.EditVisitProcedurePopup);




//
//
// Subprocedure panel
//
//

Clara.BudgetBuilder.SubProcedurePanel = Ext.extend(Ext.Panel, {
    title: 'Related Procedures',
    layout: 'border',
    selectedSubprocedureId:0,
	constructor:function(config){		
		Clara.BudgetBuilder.SubProcedurePanel.superclass.constructor.call(this, config);
	},
	refreshSubprocedures: function(){
		var t = this;
		var parentWindow = this.ownerCt.ownerCt;
		if(parentWindow.procedure){
			var isOfficeVisit = parentWindow.procedure.isOfficeVisit();
			var readOnly = parentWindow.readOnly;
			////cdebug("refresh subproc panel "+t.id+" .. isOfficeVisit? "+isOfficeVisit);
			////cdebug(t);
			var gpSubprocs = Ext.getCmp(t.id+'_gpSubprocedures');
			
			gpSubprocs.getColumnModel().setColumnHeader(0, ((isOfficeVisit)?'Office Visit Item':'Related Procedure'));
			gpSubprocs.getColumnModel().setHidden(1, isOfficeVisit); //findColumnIndex('cost')
			gpSubprocs.getColumnModel().setHidden(2, isOfficeVisit);
			gpSubprocs.getColumnModel().setHidden(3, isOfficeVisit);
			gpSubprocs.getColumnModel().setHidden(4, isOfficeVisit);
			
			Ext.getCmp(t.id+'_btnAddSubProcedure').setVisible(!isOfficeVisit && !readOnly);
			Ext.getCmp(t.id+'_btnMakePrimary').setVisible(!isOfficeVisit && !readOnly);
			Ext.getCmp(t.id+'_btnAddOfficeVisitProcedure').setVisible(isOfficeVisit && !readOnly);
			
			gpSubprocs.getStore().loadData(parentWindow.procedure.getSubprocedureArray());
		}
	},
	initComponent: function() {
		var parentWindow = this.ownerCt.ownerCt;
		var t = this;
		var isOfficeVisit = (parentWindow.procedure && parentWindow.procedure.isOfficeVisit());
		var readOnly = parentWindow.readOnly;
		////cdebug("i'm here");
		////cdebug("isOfficeVisit: " + isOfficeVisit);
		var config = {
				id:parentWindow.id+"_tSubprocedures",
			    disabled:(t && t.procedure)?false:true,
				items: [{
						xtype:'panel',
						unstyled:true,
						border:false,
						region:'north',
						padding:6,
						html:'<h2>Assigning related procedures</h2><span>You can assign related procedures to use in place of this procedure. If a related procedure has a higher price than this procedure, it will take the place of the current procedure.</span>'
							},
					 {
	                    xtype: 'grid',
	                    region:'center',
	                    border: false,
	                    store:new Ext.data.Store({
	        				reader: new Ext.data.ArrayReader({},[
	        					'procid','code','desc','cost','sponsor','price','residual','note'
	        				]),
	        				autoLoad:false,
	        				remoteSort:false
	        			}),
	                    id: t.id+'_gpSubprocedures',
	                    columns: [
	                        {
	                            xtype: 'gridcolumn',
	                            header: (isOfficeVisit)?'Office Visit Item':'Related Procedure',
	                            dataIndex: 'desc',
	                            sortable: true,
	                            menuDisabled:true,
	                            width: 300,
	                            renderer: function(v,p,r){
	                            	var html = "<div class='wrap related-procedure-row'>";
	                            	html += "<strong>"+v+"</strong>";
	                            	if (r.get("note") != ""){
	                            		html += "<div class='procedure-note'>"+r.get("note")+"</div>";
	                            	}
	                            	return html;
	                            }
	                        },{
	                            xtype: 'numbercolumn',
	                            dataIndex: 'cost',
	                            header: 'Cost',
	                            hidden:isOfficeVisit,
	                            menuDisabled:true,
	                            width:60,
	                            renderer: Ext.util.Format.usMoney,
	                            sortable: true
	                        },{
	                            xtype: 'numbercolumn',
	                            dataIndex: 'sponsor',
	                            header: 'Sponsor',
	                            menuDisabled:true,
	                            hidden:isOfficeVisit,
	                            width:60,
	                            renderer: Ext.util.Format.usMoney,
	                            sortable: true
	                        },{
	                            xtype: 'numbercolumn',
	                            dataIndex: 'price',
	                            hidden:isOfficeVisit,
	                            menuDisabled:true,
	                            width:60,
	                            renderer: Ext.util.Format.usMoney,
	                            header: 'Price',
	                            sortable: true
	                        },{
	                            xtype: 'numbercolumn',
	                            dataIndex: 'residual',
	                            hidden:isOfficeVisit,
	                            menuDisabled:true,
	                            width:60,
	                            renderer: Ext.util.Format.usMoney,
	                            header: 'Residual',
	                            sortable: true
	                        }
	                    ],
	                    listeners:{
							rowclick:function(g,rowIndex,e){
								Ext.getCmp(t.id+'_btnRemoveSubProcedure').setDisabled(readOnly);
								Ext.getCmp(t.id+'_btnMakePrimary').setDisabled(readOnly);
			        			var d =  g.getSelectionModel().getSelected().data;
			        			t.selectedSubprocedureId = d.procid;
			        			////cdebug(t.selectedSubprocedureId);
			        		},
			        		rowdblclick:function(g,rowIndex,e){
	                			var d =  g.getSelectionModel().getSelected().data;
	                			t.selectedSubprocedureId = d.procid;
	                			var sproc = parentWindow.procedure.getSubprocedure(d.procid);
	                			new Clara.BudgetBuilder.ProcedureWindow({readOnly:readOnly,parentProcedureWindow:parentWindow, parentProcedure:parentWindow.procedure, procedure:sproc, isSubprocedure:true, modal:true}).show();
	                		}
						},
						tbar:[{
								id: t.id+'_btnAddOfficeVisitProcedure',
								hidden:!isOfficeVisit,
								iconCls:'icn-door-open-in',
								text: '<span style="font-weight:800;">Add Office Visit Item</span>',
								handler: function(){
							   		new Clara.BudgetBuilder.ProcedureWindow({proceduretype:'officevisit',parentProcedureWindow:parentWindow,parentProcedure:parentWindow.procedure, isSubprocedure:true, modal:true, proceduretype:'officevisit'}).show();
								}
							},
						      {
									id: t.id+'_btnAddSubProcedure',
									disabled:false,
									hidden:isOfficeVisit,
									iconCls:'icn-zones',
									text: '<span style="font-weight:800;">Add Related Procedure</span>',
									menu: [
											{
												   id: t.id+'_btnUamsMiscProcedure',
												   text:'UAMS Procedure',
												   iconCls:'icn-zone-select',
												   handler: function(){
												   		new Clara.BudgetBuilder.ProcedureWindow({proceduretype:'normal',parentProcedureWindow:parentWindow, parentProcedure:parentWindow.procedure, isSubprocedure:true, modal:true}).show();
													}
											},
											{
													id: t.id+'_btnAddMiscProcedure',
													iconCls:'icn-zone',
													text: 'Misc Procedure',
													handler: function(){
												   		new Clara.BudgetBuilder.ProcedureWindow({proceduretype:'misc',parentProcedureWindow:parentWindow,parentProcedure:parentWindow.procedure, isSubprocedure:true, modal:true, proceduretype:'misc'}).show();
													}
											},
											{
													id: t.id+'_btnAddOutsideProcedure',
													iconCls:'icn-zone--arrow',
													text: 'Outside Procedure',
													handler: function(){
												   		new Clara.BudgetBuilder.ProcedureWindow({proceduretype:'outside',parentProcedureWindow:parentWindow,parentProcedure:parentWindow.procedure, isSubprocedure:true, modal:true, proceduretype:'outside'}).show();
													}
											}
									       ]
									
								},'->',{
									id: t.id+'_btnMakePrimary',
									iconCls:'icn-task-select-first',
									text: 'Make Primary',
									disabled:true,
									hidden:isOfficeVisit || readOnly,
									handler:function(){
										var sproc = parentWindow.procedure.getSubprocedure(t.selectedSubprocedureId);
	                            		parentWindow.procedure.swapProcedureInformation(sproc);
	                            		t.refreshSubprocedures();
	                            		parentWindow.refreshProcedure();
	                            		Ext.getCmp(t.id+'_btnMakePrimary').setDisabled(true);
	                            		Ext.getCmp(t.id+'_btnRemoveSubProcedure').setDisabled(true);
									}
								},{
									id: t.id+'_btnRemoveSubProcedure',
									iconCls:'icn-minus-button',
									text: 'Remove..',
									disabled:true,
									handler:function(){
										Ext.Msg.show({
	                            			title:'Remove related procedure?',
	                            			width:350,
	                            			msg:'Are you sure you want to remove this related procedure?',
	                            			buttons:Ext.Msg.YESNO,
	                            			fn:function(btn){
	                            				if (btn == 'yes'){
	                            					////cdebug("remove");
	                            					parentWindow.procedure.removeSubprocedure(t.selectedSubprocedureId);
	                            					t.refreshSubprocedures();
	                            					Ext.getCmp(t.id+'_btnMakePrimary').setDisabled(true);
	                            					Ext.getCmp(t.id+'_btnRemoveSubProcedure').setDisabled(true);
	                            				}
	                            			},
	                            		    icon: Ext.MessageBox.WARNING
	                            		});
									}
								}
					]
					 }
					 ]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.BudgetBuilder.SubProcedurePanel.superclass.initComponent.apply(this, arguments);
		////cdebug("Related procedure tab '"+t.id+"' created");
		t.refreshSubprocedures();
		
	}
});
Ext.reg('clarasubprocedurepanel', Clara.BudgetBuilder.SubProcedurePanel);



//-------------------------------------------------
//
// Procedure Tabs (normal,misc,outside)
//
//-------------------------------------------------


Clara.BudgetBuilder.NormalProcedurePanel = Ext.extend(Ext.FormPanel, {
    title: 'UAMS Procedure',
    height: 340,
    layout: 'border',
	constructor:function(config){		
		Clara.BudgetBuilder.NormalProcedurePanel.superclass.constructor.call(this, config);
	},
	initComponent: function() {
		var parentWindow = this.ownerCt.ownerCt;
		var t = this;
		t.id = parentWindow.id + "_normal-procedure-panel";
		var config = {
				disabled:(parentWindow.editing)?true:false,

				items: [{ xtype:'uxsearchfield',
					 store:parentWindow.procedureStore,
					 region:'north',
					 id:'normal-procedure-searchfield',
					 tabIndex:100,	
					 title:'Search for the procedure you wish to add..',
					 emptyText:'Search by keyword or CPT code',
					 paramName : 'keyword',
					 listeners:{
						render:function(f)
								 {
								    f.focus(true,400);
								 }
					 },
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
					     store:parentWindow.procedureStore,
					     id: 'gpSearchResults',
					     columns: [
					         {
					             xtype: 'gridcolumn',
					             dataIndex: 'id',
					             header: 'Column',
					             sortable: true,
					             renderer: function(v,s,r){
					         		var p = r.data;
					         		var recid = r.id;

					         		str = "<div class='proc-search-row'>";
					         		str = str + "<div class='proc-cptcode'>"+p.cptCode+"</div>";
					         		str = str + "<div class='proc-shortdesc'>"+p.cptShortDesc+"</div><div style='clear:both;'></div>";
					         		str = str + "<div class='proc-longdesc'>"+p.cptLongDesc+"</div>";
					         		str = str + "<div class='proc-locations'>";
					         		var rowclass = "";

					         		if (p.hospProcId != null){
					         			rowclass = rowclass + ((p.hospProcOnly == true)?" proc-hosp-only":" proc-hosp");
					         			rowclass = rowclass + ((p.physProcs.length < 1)?" last-row":"");
					        				str = str + "<div class='proc-location-row"+rowclass+"'><div class='proc-location-lcol'>";
					     				str = str + "<span class='proc-hosp-location-name'>"+p.hospProcDesc+"</span></div><div class='proc-location-rcol'><span class='proc-location-cost'>$"+p.hospProcCost+"</span>";
					     				if (p.hospProcOnly == true){
					     					str = str + "<a class='proc-choose' href='javascript:Ext.getCmp(\""+parentWindow.id+"\").chooseHospitalProcedure(\""+recid+"\""+");'>Choose</a>";
					     				}
					     				str=str+"</div><div style='clear:both;'></div></div>";
					         		}
					         		
					         		for (var i=0; i<p.physProcs.length; i++){
					         			var rowclass = (i == p.physProcs.length - 1)?" last-row":"";
					         			rowclass = rowclass + ((p.physProcs[i].data.physProcOnly == true)?" proc-phys-only":"");

					         			if (p.physProcs[i].data.physProcId != null){
					         				str = str + "<div class='proc-location-row"+rowclass+"'><div class='proc-location-lcol'>";
					         				str = str + "<span class='proc-location-name'>"+p.physProcs[i].data.physLocationDesc+"</span></div><div class='proc-location-rcol'><span class='proc-location-cost'>$"+p.physProcs[i].data.physProcCost+"</span>";
					         				str = str + "<a class='proc-choose' href='javascript:Ext.getCmp(\""+parentWindow.id+"\").choosePhysicianProcedure(\""+recid+"\","+i+");'>Choose</a></div><div style='clear:both;'></div></div>";
					         			}
					         		}

					         		str = str + "</div>";
					         		return str + "</div>";
					         	}
					             
					         }
					     ]
					    }
					 ]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.BudgetBuilder.NormalProcedurePanel.superclass.initComponent.apply(this, arguments);
		parentWindow.procedureStore.removeAll();
	}
});
Ext.reg('claranormalprocedurepanel', Clara.BudgetBuilder.NormalProcedurePanel);



Clara.BudgetBuilder.OfficeVisitProcedurePanel = Ext.extend(Ext.Panel, {
    title: 'Office Visit Items',
    height: 340,
    layout: 'border',
	constructor:function(config){		
		Clara.BudgetBuilder.OfficeVisitProcedurePanel.superclass.constructor.call(this, config);
	},
	initComponent: function() {
		var parentWindow = this.ownerCt.ownerCt;
		var readOnly = parentWindow.readOnly;
		var t = this;
		t.id = parentWindow.id + "_officevisit-procedure-panel";
		var config = {
				disabled:(parentWindow.editing == true)?true:false,
				items: [{
					     xtype: 'grid',
					     viewConfig: {
					         forceFit: true,
					         rowOverCls:'',
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
					     store:parentWindow.officevisitProcedureStore,
					     id: 'gpSearchResults',
					     columns: [
					         {
					             xtype: 'gridcolumn',
					             dataIndex: 'code',
					             header: 'Column',
					             sortable: true,
					             renderer: function(v,s,r){
					         		var p = r.data;

					         		str = "<div class='proc-search-row'>";
					         		str = str + "<div class='proc-shortdesc'>"+p.description+"</div><div style='float:right;'><span class='proc-location-cost'><a class='proc-choose' href='javascript:Ext.getCmp(\""+parentWindow.id+"\").chooseOfficeVisitProcedure(\""+p.code+"\");'>Choose</a></span></div><div style='clear:both;'></div>";
					         		return str + "</div>";
					         	}
					             
					         }
					     ]
					    }
					 ]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.BudgetBuilder.OfficeVisitProcedurePanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claraofficevisitprocedurepanel', Clara.BudgetBuilder.OfficeVisitProcedurePanel);



Clara.BudgetBuilder.MiscProcedurePanel = Ext.extend(Ext.Panel, {
    title: 'Misc. Procedure',
    height: 340,
    layout: 'border',
	constructor:function(config){		
		Clara.BudgetBuilder.MiscProcedurePanel.superclass.constructor.call(this, config);
	},
	initComponent: function() {
		var parentWindow = this.ownerCt.ownerCt;
		var t = this;
		t.id = parentWindow.id + "_misc-procedure-panel";
		var config = {
				disabled:(parentWindow.editing == true)?true:false,
				items: [{
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
					     store:parentWindow.miscProcedureStore,
					     id: 'gpSearchResults',
					     columns: [
					         {
					             xtype: 'gridcolumn',
					             dataIndex: 'code',
					             header: 'Column',
					             sortable: true,
					             renderer: function(v,s,r){
					         		var p = r.data;

					         		str = "<div class='proc-search-row'>";
					         		str = str + "<div class='proc-shortdesc'>"+p.description+"</div><div style='float:right;'><span class='proc-location-cost'>$"+p.cost+" <a class='proc-choose' href='javascript:Ext.getCmp(\""+parentWindow.id+"\").chooseMiscProcedure(\""+p.code+"\");'>Choose</a></span></div><div style='clear:both;'></div>";
					         		return str + "</div>";
					         	}
					             
					         }
					     ]
					    }
					 ]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.BudgetBuilder.MiscProcedurePanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claramiscprocedurepanel', Clara.BudgetBuilder.MiscProcedurePanel);

Clara.BudgetBuilder.OutsideProcedurePanel = Ext.extend(Ext.Panel, {
	xtype: 'panel',
    title: 'Outside Procedure',
    height: 340, 
    layout: 'absolute',
	constructor:function(config){		
		Clara.BudgetBuilder.OutsideProcedurePanel.superclass.constructor.call(this, config);
	},
	initComponent: function() {
		var parentWindow = this.ownerCt.ownerCt;//Ext.getCmp("winProcedure");
		var t = this;
		t.id = parentWindow.id + "_outside-procedure-panel";
		clog("PARENTWIND",parentWindow);
		var config = {
				// disabled:(parentWindow.editing == true)?true:false,
				
				items: [
				        {
				            xtype: 'textfield',
				            name:t.id+'_fldOutsideProcName',
				            id:t.id+'_fldOutsideProcName',
				            allowBlank:false,
				            value: (parentWindow.editing)?parentWindow.procedure.description:"",
				            x: 40,
				            y: 70,
				            width: 390,
				            listeners: {
				            	change: function(op,nv){
				            		if (op.validate() && jQuery.trim(op.getValue()) != ''){
				            			Ext.getCmp(parentWindow.id+'_lblProcedureName').setText(jQuery.trim(op.getValue()));
				            			if (parentWindow.editing) {
				            				parentWindow.procedure.description = (jQuery.trim(op.getValue()));
				            			}
				            		}
				            	}
				            }
				        },
				        {
				            xtype: 'label',
				            text: 'Outside Procedure Name',
				            x: 40,
				            y: 50,
				            style: 'font-size:14px;',
				            width: 180
				        },
				        {
				            xtype: 'button',
				            text: 'Continue',
				            x: 440,
				            y: 70,
				            hidden: parentWindow.editing,
				            listeners:{
				            	click:function(){
				            		var op = Ext.getCmp(t.id+"_fldOutsideProcName");
				            		if (op.validate() && jQuery.trim(op.getValue()) != ''){
				            			parentWindow.chooseOutsideProcedure(jQuery.trim(op.getValue()));
				            		}
				            	}
				            }
				        },
				        {
				            xtype: 'button',
				            text: 'Rename',
				            x: 440,
				            y: 70,
				            hidden: !parentWindow.editing,
				            listeners:{
				            	click:function(){
				            		var op = Ext.getCmp(t.id+"_fldOutsideProcName");
				            		if (op.validate() && jQuery.trim(op.getValue()) != ''){
				            			parentWindow.procedure.description = (jQuery.trim(op.getValue()));
				            			Ext.getCmp(parentWindow.id+"_tpProcedure").setActiveTab(1);
				            	    	Ext.getCmp(parentWindow.id+"_fldSponsor").focus(true,10);
				            		}
				            	}
				            }
				        }
					 ]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.BudgetBuilder.OutsideProcedurePanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claraoutsideprocedurepanel', Clara.BudgetBuilder.OutsideProcedurePanel);





//-------------------------------------------------
//
// STORES
//
//-------------------------------------------------

Clara.BudgetBuilder.MiscProcedureStore = new Ext.data.Store({
	autoLoad:true,
	header :{
  'Accept': 'application/json'
  },
	proxy: new Ext.data.HttpProxy({
		url: appContext+"/static/xml/budget-misc.json",
		method:'GET'
	}),
	reader: new Ext.data.JsonReader({
		idProperty: 'code'
	}, [
		{name:'code'},
		{name:'description'},
		{name:'cost'}
	])
});

Clara.BudgetBuilder.OfficeVisitProcedureStore = new Ext.data.Store({
	autoLoad:true,
	header :{
  'Accept': 'application/json'
  },
	proxy: new Ext.data.HttpProxy({
		url: appContext+"/static/xml/budget-officevisit.json",
		method:'GET'
	}),
	reader: new Ext.data.JsonReader({
		idProperty: 'code'
	}, [
		{name:'code'},
		{name:'cost'},
		{name:'description'}
	])
});

Clara.BudgetBuilder.NormalProcedureStore = new Ext.data.Store({
	header :{
		'Accept': 'application/json'
	},
	proxy: new Ext.data.HttpProxy({
		url: appContext+"/ajax/protocols/budgets/procedures/find-by-keyword",
		method:'GET'
	}),
	reader: new Ext.data.JsonReader({
		fields: [
		{name:'id', mapping:'cptCode.id'},
		{name:'cptCode', mapping:'cptCode.code'},
		{name:'cptCategoryCode', convert:function(v,r){
			if (typeof r.cptCode.categoryCode == 'undefined' || r.cptCode.categoryCode == null){
				return null;
			} else {
				return r.cptCode.categoryCode.description;
			}
		}},
		{name:'cptShortDesc', mapping:'cptCode.shortDescription'},
		{name:'cptLongDesc', mapping:'cptCode.longDescription'},
		{name:'cptRetired', mapping:'cptCode.retired'},
		{name:'hospProcId', convert:function(v,r){
			if (typeof r.hospitalProcedure == 'undefined' || r.hospitalProcedure == null){
				return null;
			} else {
				return r.hospitalProcedure.id;
			}
		}},
		{name:'hospProcCost', convert:function(v,r){
			if (typeof r.hospitalProcedure == 'undefined' || r.hospitalProcedure == null){
				return null;
			} else {
				return r.hospitalProcedure.cost;
			}
		}},
		{name:'hospProcDesc', convert:function(v,r){
			if (typeof r.hospitalProcedure == 'undefined' || r.hospitalProcedure == null){
				return null;
			} else {
				return r.hospitalProcedure.description;
			}
		}},
		{name:'hospProcOnly', convert:function(v,r){
			if (typeof r.hospitalProcedure == 'undefined' || r.hospitalProcedure == null){
				return null;
			} else {
				return r.hospitalProcedure.hospitalOnly;
			}
		}},
		{name:'physProcs', mapping:'physicianProcedures', convert:function(v,node){ 
     	 var locReader = new Ext.data.JsonReader({
  			root: 'physicianProcedures',
  			fields: [
  			         {name:'physProcId', mapping:'id'},
  			         {name:'physProcDesc', mapping:'description'},
  			         {name:'physProcDate', mapping:'effectiveDate', type: 'date', dateFormat: 'm/d/Y H:i:s'},
  			         {name:'physProcCost', mapping:'cost'},
  			         {name:'physProcOnly', mapping:'physicianOnly'},
  			         {name:'physLocationCode', mapping:'locationCode.code'},
  			         {name:'physLocationDesc', mapping:'locationCode.description'},
  			         {name:'physLocationId', mapping:'locationCode.id'}
  			         ]
  		});
  	 return locReader.readRecords(node).records; 
		}}
		
	]})});



Clara.BudgetBuilder.ProcedureWindow = Ext.extend(Ext.Window, {
    title: 'Procedure',
    width: 598,
    height: 507,
    layout: 'border',
    isSubprocedure:false,		// True for subprocedure windows (hides related procedures tab)
    isOtherMiscProcedure:false,
    proceduretype:'normal',		// can be normal, misc, drug, drugdispensing, officevisit or outside
    procedure:null,
    parentProcedure:null,		// for subprocedures (to update parent and to copy visits)
    parentProcedureWindow:null,
    editing:false,
    readOnly:false,
    modal:true,
    visitProcedures:[],
    visitList:[],
    drugStore:	Clara.BudgetBuilder.DrugStore,
    pharmStore: Clara.BudgetBuilder.PharmacyFeeStore,
    getAllowableBillingCategories: function(){
    	var a = [];
    	var t = this;
        if (t.proceduretype == 'outside'){
        	a.push(["O"]);
        } else if (t.proceduretype == 'misc' && t.procedure && t.procedure.cptCode+"" == "123") {
        	a.push(["COR"]);
        } else {
        	a = [['R'],['C'],['I'],['O'],['RNS'],['CNMS'],['CL']];
        }
        return a;
    },
    openToTab: function(index){
		this.show();
		Ext.getCmp(this.id+"_tpProcedure").setActiveTab(index);
	},
	isCPTCodeExist: function(cptCode) {
		var epoch = Ext.getCmp("budget-tabpanel").activeEpoch;

		for(var i = 0; i < epoch.procedures.length; i ++){
			var proc = epoch.procedures[i];
			if(proc.cptCode == cptCode){
				return true;
			}
		}
		return false;
	},
	refreshResidual: function(){
		var t = this;
		jQuery("#"+t.id+"_fldResidual").text(Ext.util.Format.usMoney(t.procedure.cost.getResidual()));
	},
	setDefaultCosts: function(){
		var t=this;
		var studyType = claraInstance.budget.studyType || '';
    	var total = t.procedure.cost.getTotal();
    	t.procedure.cost.sponsor = ((studyType == 'cooperative-group'||studyType == 'investigator-initiated')?0:total);
    	t.procedure.cost.price = total;
    	
    	Ext.getCmp(t.id+"_fldSponsor").setValue((studyType == 'cooperative-group'||studyType == 'investigator-initiated')?0:total);
    	// misc procedures: fill "cost" field
		if (t.procedure.type == 'misc' || t.procedure.type == 'drugdispensing'){
			Ext.getCmp(t.id+"_fldMiscCost").setValue(total);
		}
    	Ext.getCmp(t.id+"_fldPrice").setValue(total);
    	t.refreshResidual();
	},
    refreshProcedure: function(){
    	
		var idprefix = this.id;
		var t = this;
		var procCode = (this.procedure.cptCode && this.procedure.cptCode != "" && this.procedure.type != "misc" && this.procedure.type != "outside")?(this.procedure.cptCode+": "):"";
		Ext.getCmp(t.id+'_lblProcedureName').setText(procCode+this.procedure.description);
		Ext.getCmp(t.id+'_fldOtherMiscProcDesc').setValue(this.procedure.description);
		

		Ext.getCmp(t.id+"_fldHospCost").setValue(this.procedure.hosp.cost);
		Ext.getCmp(t.id+"_fldPhysCost").setValue(this.procedure.phys.cost);
		

    	jQuery("#"+t.id+"_fldSponsor").val(this.procedure.cost.sponsor);
    	jQuery("#"+t.id+"_fldPrice").val(this.procedure.cost.price);
    	jQuery("#"+t.id+"_fldTotal").text(Ext.util.Format.usMoney(this.procedure.cost.getTotal()));
    	t.refreshResidual();
    	jQuery("#"+t.id+"_fldNotes").val(this.procedure.notes);
    	jQuery("#"+t.id+"_fldCoverageNotes").val(this.procedure.coverageNotes);
    	jQuery("#"+t.id+"_fldClinicalNotes").val(this.procedure.clinicalNotes);
    	
    	var prodcedureToUse = (this.isSubprocedure)?this.parentProcedure:this.procedure;
    	Ext.getCmp(t.id+"_fldExpenseCategory").setValue(prodcedureToUse.expensecategory);
    	
    	if (prodcedureToUse.expensecategory == "Communication Order") {
    		Ext.getCmp(t.id+"_fldExpenseCategory").setDisabled(true);
    		if(Ext.getCmp(t.id+"_tSubprocedures")) Ext.getCmp(t.id+"_tSubprocedures").setDisabled(true);
    		Ext.getCmp(t.id+"_fldSetAllType").getStore().loadData(t.getAllowableBillingCategories());
    	}

    	if (typeof Ext.getCmp(t.id+"_tSubprocedures")!= 'undefined') Ext.getCmp(t.id+"_tSubprocedures").refreshSubprocedures();
    	if (!t.isSubprocedure || t.proceduretype == "officevisit") {
    		Ext.getCmp(t.id+"_gpProcedureVisits").getStore().loadData(this.visitList);
    		t.refreshVisitProcedures(Ext.getCmp(t.id+"_gpProcedureVisits").getStore().data.items);
    	}
    },
    chooseHospitalProcedure: function(recordIndex){
    	
    	var rec = this.procedureStore.getById(recordIndex);
    	var p = rec.data;
    	
    	if(this.isCPTCodeExist(p.cptCode)){
    		Ext.Msg.show({
    			title:'Duplicate Procedure',
    			width:350,
    			msg:'WARNING: This procedure (CPT: ' + p.cptCode + ') already exist in the matrix!',
    			buttons:Ext.Msg.OK,
    		    icon: Ext.MessageBox.WARNING
    		});
    	}
    	var t = this;
    	t.procedure = new Clara.BudgetBuilder.Procedure({
    		id:budget.newId(),
    		type:'normal',
    		description: p.cptShortDesc,
    		cptCode: p.cptCode,
    		category: p.cptCategoryCode,
    		location:'',
    		phys: { cost: null,
				only: false,
				locationCode:null,
				locationDesc:null
			},
    		hosp: {
    			id:p.hospProcId,
    			only:p.hospProcOnly,
    			description:p.hospProcDesc,
    			cost:p.hospProcCost,
    			locationCode:null,	// deprecated
    			locationDesc:null	// deprecated
    		},
    		notes:null,
    		clinicalNotes:null,
    		coverageNotes:null,
    		cost:{
    			misc:null,
    			sponsor:null,
    			price:null
    		}
    		
    	});
    	t.procedure.initWithDefaults();

		Ext.getCmp(t.id+"_fldOtherMiscProcDesc").setVisible(false);
		Ext.getCmp(t.id+"_lblOtherMiscProcDesc").setVisible(false);
		if (Ext.getCmp(t.id+"_tCodes")) Ext.getCmp(t.id+"_tCodes").setDisabled(false);
    	Ext.getCmp(t.id+"_tCosts").setDisabled(false);
    	Ext.getCmp(t.id+"_btnCloseProcedureWindow").setDisabled(false);
    	if (typeof Ext.getCmp(t.id+"_tVisits") != 'undefined') Ext.getCmp(t.id+"_tVisits").setDisabled(t.isSubprocedure);
    	Ext.getCmp(t.id+"_tpProcedure").setActiveTab(1);
    	    	
    	Ext.getCmp(t.id+"_fldSponsor").focus(true,10);
    	
    	if(Ext.getCmp(t.id+"_tSubprocedures")) Ext.getCmp(t.id+"_tSubprocedures").setDisabled(false);
    	this.refreshProcedure();
    	this.setDefaultCosts();
    },
    choosePhysicianProcedure: function(recordIndex, physProcId){
    	var rec = this.procedureStore.getById(recordIndex);
    	var p = rec.data;
    	if(this.isCPTCodeExist(p.cptCode)){
    		Ext.Msg.show({
    			title:'Duplicate Procedure',
    			width:350,
    			msg:'WARNING: This procedure (CPT: ' + p.cptCode + ') already exist in the matrix!',
    			buttons:Ext.Msg.OK,
    		    icon: Ext.MessageBox.WARNING
    		});
    	}
    	var t = this;
    	this.procedure = new Clara.BudgetBuilder.Procedure({
    		id:budget.newId(),
    		type:'normal',
    		description: p.cptShortDesc,
    		cptCode: p.cptCode,
    		category: p.cptCategoryCode,
    		location:'',
    		phys: { id: p.physProcs[physProcId].data.physProcId,
    			cost: p.physProcs[physProcId].data.physProcCost,
    				only: p.physProcs[physProcId].data.physProcOnly,
    				locationCode:p.physProcs[physProcId].data.physLocationCode,
    				locationDesc:p.physProcs[physProcId].data.physLocationDesc
    		},
    		hosp: (rec.data.physProcs[physProcId].data.physProcOnly)?{
    			only:false,
    			description:null,
    			cost:null,
    			locationCode:null,
    			locationDesc:null
    		}:{
    			id:p.hospProcId,
    			only:p.hospProcOnly,
    			description:p.hospProcDesc,
    			cost:p.hospProcCost,
    			locationCode:null,	// chosen on second tab
    			locationDesc:null	// chosen on second tab
    		},
    		notes:null,
    		clinicalNotes:null,
    		coverageNotes:null,
    		cost:{
    			misc:null,
    			sponsor:null,
    			price:null
    		}
    		
    	});
    	t.procedure.initWithDefaults();

		Ext.getCmp(t.id+"_fldOtherMiscProcDesc").setVisible(false);
		Ext.getCmp(t.id+"_lblOtherMiscProcDesc").setVisible(false);
		if (Ext.getCmp(t.id+"_tCodes")) Ext.getCmp(t.id+"_tCodes").setDisabled(false);
    	Ext.getCmp(t.id+"_tCosts").setDisabled(false);
    	Ext.getCmp(t.id+"_btnCloseProcedureWindow").setDisabled(false);
    	if (typeof Ext.getCmp(t.id+"_tVisits") != 'undefined') Ext.getCmp(t.id+"_tVisits").setDisabled(t.isSubprocedure);
    	Ext.getCmp(t.id+"_tpProcedure").setActiveTab(1);
    	Ext.getCmp(t.id+"_fldSponsor").focus(true,10);
    	if(Ext.getCmp(t.id+"_tSubprocedures")) Ext.getCmp(t.id+"_tSubprocedures").setDisabled(false);
    	this.refreshProcedure();
    	this.setDefaultCosts();
    	
    },
    chooseDrug: function(drugType, name, recordIndex){
    	var t = this;
    	
    	Ext.getCmp(t.id+"_fldOtherMiscProcDesc").setVisible(false);
    	Ext.getCmp(t.id+"_lblOtherMiscProcDesc").setVisible(false);
    	
    	var rec = (drugType == 'investigational' )?t.drugStore.getAt(recordIndex):null;
    	rec = (drugType == 'dispensing')?t.pharmStore.getAt(recordIndex):rec;
    	var p = (drugType == 'investigational')?rec.data:null;
    	var cost = (drugType == 'dispensing')?rec.data.cost:null;
    	
    	this.procedure = new Clara.BudgetBuilder.Procedure({
    		id:budget.newId(),
    		type:'drug',
    		description: name,
    		cptCode: (p && p.drugId)?p.drugId:null,
    	    category: 'DRUG - '+drugType,
    		phys: { cost: null,
    				only: false,
    				locationCode:null,
    				locationDesc:null
    		},
    		hosp:{
    			only:false,
    			description:null,
    			cost:null,
    			locationCode:null,
    			locationDesc:null
    		},
    		notes:null,
    		clinicalNotes:null,
    		coverageNotes:null,
    		cost:{
    			misc:cost,
    			sponsor:null,
    			price:cost
    		}
    	});
    	t.procedure.initWithDefaults();
       	
        
    	
    	//if (Ext.getCmp(t.id+"_tCodes")) Ext.getCmp(t.id+"_tCodes").setDisabled(true);
    	Ext.getCmp(t.id+"_tCosts").setDisabled(false);
    	//Ext.getCmp(t.id+"_tSubprocedures").setDisabled(true);
    	Ext.getCmp(t.id+"_btnCloseProcedureWindow").setDisabled(false);
    	if (typeof Ext.getCmp(t.id+"_tVisits") != 'undefined') Ext.getCmp(t.id+"_tVisits").setDisabled(t.isSubprocedure);
    	Ext.getCmp(t.id+"_tpProcedure").setActiveTab(1);
    	Ext.getCmp(t.id+"_fldSponsor").focus(true,10);
    	this.refreshProcedure();
    	this.setDefaultCosts();
    	
    },
    chooseMiscProcedure: function(recordIndex){
    	var rec = this.miscProcedureStore.getById(recordIndex);
    	var p = rec.data;
    	var t = this;
    	clog("MISC: Chose record",rec);
    	
    	Ext.getCmp(t.id+"_fldOtherMiscProcDesc").setVisible(false);
		Ext.getCmp(t.id+"_lblOtherMiscProcDesc").setVisible(false);
		
		this.procedure = new Clara.BudgetBuilder.Procedure({
    		id:budget.newId(),
    		type:'misc',
    		description: p.description,
    		cptCode: p.code,
    		category: 'Misc.',
    		phys: { cost: null,
    				only: false,
    				locationCode:null,
    				locationDesc:null
    		},
    		hosp:{
    			only:false,
    			description:null,
    			cost:null,
    			locationCode:null,
    			locationDesc:null
    		},
    		notes:null,
    		clinicalNotes:null,
    		coverageNotes:null,
    		cost:{
    			misc:p.cost,
    			sponsor:null,
    			price:null
    		}
    	});
		t.procedure.initWithDefaults();
		
    	if (p.code == 107) {
    		//	OTHER: allow description editing
    		t.isOtherMiscProcedure = true;
    		Ext.getCmp(t.id+"_fldOtherMiscProcDesc").setVisible(true);
    		Ext.getCmp(t.id+"_lblOtherMiscProcDesc").setVisible(true);
    	} else if (p.code == 123) {
    		// COR: Communication order. Set Expense category to "communication order" and only allow "COR" billing category
    		// Ext.getCmp(t.id+"_fldExpenseCategory").setValue("Communication Order");
    		t.procedure.expensecategory = "Communication Order";
    		Ext.getCmp(t.id+"_fldExpenseCategory").setDisabled(true); 
    	}
    	
    	if (Ext.getCmp(t.id+"_tCodes")) Ext.getCmp(t.id+"_tCodes").setDisabled(false);
    	Ext.getCmp(t.id+"_tCosts").setDisabled(false);
    	Ext.getCmp(t.id+"_btnCloseProcedureWindow").setDisabled(false);
    	if (typeof Ext.getCmp(t.id+"_tVisits") != 'undefined') Ext.getCmp(t.id+"_tVisits").setDisabled(t.isSubprocedure);
    	Ext.getCmp(t.id+"_tpProcedure").setActiveTab(1);
    	Ext.getCmp(t.id+"_fldSponsor").focus(true,10);
    	if(Ext.getCmp(t.id+"_tSubprocedures")) Ext.getCmp(t.id+"_tSubprocedures").setDisabled(false);
    	this.refreshProcedure();
    	this.setDefaultCosts();
    },
    chooseOfficeVisitProcedure: function(recordIndex){
    	var rec = this.officevisitProcedureStore.getById(recordIndex);
    	var p = rec.data;
    	var t = this;
    	this.procedure = new Clara.BudgetBuilder.Procedure({
    		id:budget.newId(),
    		type:'officevisit',
    		description: p.description,
    		cptCode: p.code,
    		category: 'Misc.',
    		phys: { cost: null,
    				only: false,
    				locationCode:null,
    				locationDesc:null
    		},
    		hosp:{
    			only:false,
    			description:null,
    			cost:p.cost,
    			locationCode:null,
    			locationDesc:null
    		},
    		notes:null,
    		clinicalNotes:null,
    		coverageNotes:null,
    		cost:{
    			sponsor:null,
    			price:null
    		}
    	});
    	t.procedure.initWithDefaults();

		Ext.getCmp(t.id+"_fldOtherMiscProcDesc").setVisible((this.procedure.cptCode == 'ov-23'));
		Ext.getCmp(t.id+"_lblOtherMiscProcDesc").setVisible((this.procedure.cptCode == 'ov-23'));
		if (Ext.getCmp(t.id+"_tCodes")) Ext.getCmp(t.id+"_tCodes").setDisabled(false);
    	Ext.getCmp(t.id+"_tCosts").setDisabled(false);
    	Ext.getCmp(t.id+"_btnCloseProcedureWindow").setDisabled(false);
    	if (typeof Ext.getCmp(t.id+"_tVisits") != 'undefined') Ext.getCmp(t.id+"_tVisits").setDisabled(false);
    	Ext.getCmp(t.id+"_tpProcedure").setActiveTab(2);
    	this.refreshProcedure();
    },
    chooseOutsideProcedure: function(name){

    	var t = this;
    	this.procedure = new Clara.BudgetBuilder.Procedure({
    		id:budget.newId(),
    		type:'outside',
    		description: name,
    		cptCode: 0,
    		category: 'Outside Procedure',
    		phys: { cost: null,
    				only: false,
    				locationCode:null,
    				locationDesc:null
    		},
    		hosp:{
    			only:false,
    			description:null,
    			cost:null,
    			locationCode:null,
    			locationDesc:null
    		},
    		notes:null,
    		clinicalNotes:null,
    		coverageNotes:null,
    		cost:{
    			sponsor:null,
    			price:null
    		}
    	});
    	t.procedure.initWithDefaults();

		Ext.getCmp(t.id+"_fldOtherMiscProcDesc").setVisible(false);
		Ext.getCmp(t.id+"_lblOtherMiscProcDesc").setVisible(false);
    	
		Ext.getCmp(t.id+"_tCosts").setDisabled(false);
		if (Ext.getCmp(t.id+"_tCodes")) Ext.getCmp(t.id+"_tCodes").setDisabled(false);
    	Ext.getCmp(t.id+"_btnCloseProcedureWindow").setDisabled(false);
    	if (typeof Ext.getCmp(t.id+"_tVisits") != 'undefined') Ext.getCmp(t.id+"_tVisits").setDisabled(t.isSubprocedure);
    	Ext.getCmp(t.id+"_tpProcedure").setActiveTab(1);
    	Ext.getCmp(t.id+"_fldSponsor").focus(true,10);
    	this.refreshProcedure();
    	this.setDefaultCosts();
    	
    },
    procedureStore:			Clara.BudgetBuilder.NormalProcedureStore,
    miscProcedureStore:		Clara.BudgetBuilder.MiscProcedureStore,
    officevisitProcedureStore:	Clara.BudgetBuilder.OfficeVisitProcedureStore,
    refreshVisitProcedures: function(d){
    	var t = this;
		var vps = t.visitProcedures;                           		
		
		var proc = t.procedure;
		vps.splice(0,vps.length);

		
		////cdebug(d);
		for (var i=0;i<d.length;i++){
			var r = d[i].data;
			////cdebug(r);
			var ct = (t.isSubprocedure)?'X':r.chargetype;
			if (ct != "" && parseInt(r.repetitions) > 0 ){
				var vp = new Clara.BudgetBuilder.VisitProcedure({
					visitid:r.visitid,		// store this here so we can add them to the right visit when we save
					procedureid:proc.id,
					type:r.chargetype,
					repetitions:r.repetitions
				});
				vps.push(vp);
			}
		}
    },
    initComponent: function() {
    		var t = this;
    		clog("initing Procedure window for type",t.proceduretype,t.procedure);
    		t.iconCls = (t.isSubprocedure)?"icn-chain":"";
    		this.editing = (this.procedure)?true:false;
    		this.proceduretype = (this.procedure)?this.procedure.type:this.proceduretype;
    		
    		if (this.procedure && this.procedure.cptCode && this.procedure.cptCode == 107 && (t.proceduretype != "drug" && t.proceduretype != "drugdispensing")){
    	    		//	OTHER: allow description editing
    	    		t.isOtherMiscProcedure = true;
    	    		
    	    	} else {
    	    		t.isOtherMiscProcedure = false;
    	    		
    	    	}
    		
    		    		
    		this.visitList = Ext.getCmp("budget-tabpanel").activeEpoch.getProcedureVisitArray(this.procedure, true);
    		    		
    		
    		if(this.isSubprocedure) {
    			if(this.proceduretype == "officevisit") {
    				
    				Ext.getCmp(this.parentProcedureWindow.id+'_gpProcedureVisits').fireEvent("afteredit",{grid:Ext.getCmp(this.parentProcedureWindow.id+'_gpProcedureVisits')});	            	

    				var parentVisitProcedures = (this.parentProcedureWindow.visitProcedures)? this.parentProcedureWindow.visitProcedures : []; 
    				var visitList = this.visitList;
    				
    				var vps = [];
    				
    				for(var i = 0; i < visitList.length; i ++) {
    					var found = false;
    					for(var j = 0; j < parentVisitProcedures.length; j ++) {
    						
        					if(visitList[i][2] == parentVisitProcedures[j].visitid && parentVisitProcedures[j].type != '') {
        						found = true;
        						break;
        					}
        				}
    					
    					if(found) {
    						vps.push(visitList[i]);
    					}
    				}
    				
    				this.visitList = vps;
    				
    			}else{// for non-officevisit sub procedure nothing
    				
    			}
    			
    		}
   
    		var itemName = (this.proceduretype == "drug" || this.proceduretype == "drugdispensing" || (this.procedure && (this.procedure.type == "drug" || this.procedure.type == "drugdispensing")))?"Drug":"Procedure";
    		this.title = (this.isSubprocedure)?(this.editing?"Edit Related "+itemName:"Add Related "+itemName):(this.editing?"Edit "+itemName:"Add "+itemName);

    		this.saveProcedure = function(){
    			var e = Ext.getCmp("budget-tabpanel").activeEpoch;
    			//var epoch = Ext.getCmp("budget-tabpanel").activeEpoch;
				
				if (!t.isSubprocedure || t.proceduretype == "officevisit"){
					////cdebug("attach vps");
					Ext.getCmp(t.id+'_gpProcedureVisits').fireEvent("afteredit",{grid:Ext.getCmp(t.id+'_gpProcedureVisits')});	            	
					
											
					var newVps = t.visitProcedures;
					var oldVps = budget.getVisitProceduresByEpochAndProcedure(e,t.procedure);
					
					// Do difference to find deleted VPS.
					
					/*
					var onlyInNew = newVps.filter(function(current){
					    return oldVps.filter(function(current_old){
					        return current_old.procedureid == current.procedureid && current_old.visitid == current.visitid
					    }).length == 0
					});
					*/
					
					var onlyInOld = oldVps.filter(function(current_old){
					    return newVps.filter(function(current){
					        return current.procedureid == current_old.procedureid && current.visitid == current_old.visitid
					    }).length == 0
					});
					
					clog("DIFF:",oldVps, newVps,onlyInOld);

					// Delete vps that were removed in this window
					for (var i=0;i<onlyInOld.length;i++){
						clog("Removing old VP ",onlyInOld[i].visitid, onlyInOld[i].procedureid);
						budget.removeVisitProcedure(onlyInOld[i].visitid, onlyInOld[i].procedureid);
					}
					
					for (var i=0;i<newVps.length;i++){
						budget.addOrUpdateVisitProcedure(newVps[i]);	// MUST include visitid in object or it will not work.
					}
				}
				
				////cdebug(budget);
				
				
				if (!t.editing){
					if (t.isSubprocedure){
						Clara.BudgetBuilder.SaveAction = "Added related "+itemName;
						t.parentProcedure.addSubprocedure(t.procedure);
						e.updateProcedure(t.parentProcedure);
					} else {
						Clara.BudgetBuilder.SaveAction = "Added "+itemName;
						e.procedures.push(t.procedure);
					}
				} else {
					if (t.isSubprocedure){
						t.parentProcedure.updateSubprocedure(t.procedure);
						e.updateProcedure(t.parentProcedure);
						
					} else {
						Clara.BudgetBuilder.SaveAction = "Edited "+itemName;
						e.updateProcedure(t.procedure);
					}
				}
				
				
				Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', e);
				Clara.BudgetBuilder.MessageBus.fireEvent('procedurechanged', t.procedure);

				budget.save();
				
				t.close();
				// if editing subprocedure, refresh parent's subprocedure list
				if (t.isSubprocedure){
					Ext.getCmp(t.parentProcedureWindow.id+"_tSubprocedures").refreshSubprocedures();
				}
    		};
    		
 			this.buttons = [{
				id:t.id+'_btnCloseProcedureWindow',
				text:t.readOnly?'Close':'Save and Close',
				disabled:!this.editing,
				handler: function(){
					

					clog("about to save.. t.visitProcedures",t.visitProcedures);
					
					if (t.visitProcedures.length < 1) {
						Ext.Msg.show({
							   title:'No billing category defined',
							   msg: 'You haven\'t defined billing categories for any visits in this phase. Are you sure you want to save?',
							   buttons: Ext.Msg.YESNOCANCEL,
							   buttonText:{cancel: "Cancel", yes: "Save "+itemName, no: "Add Billing Categories to Visits"},
							   fn: function(btn){
								    if (btn == 'yes'){
								    	t.saveProcedure();
								    } else {
								    	var tp = Ext.getCmp(t.id+'_tpProcedure');
								    	tp.setActiveTab(t.id+'_tVisits');
								    }
								},
							   animEl: 'elId',
							   icon: Ext.MessageBox.WARNING
							});
					}else {
						
						if (Ext.getCmp(t.id+'_fldExpenseCategory').validate()){
							t.saveProcedure();
						}else {
							alert("Choose an expense category for this "+itemName);
						}
						
						
					}
				}
			}];
 			
            this.items = [
                {	
                    xtype: 'tabpanel',
                    activeTab: (t.procedure)?1:0,
                    border: false,
                    region: 'center',
                    id: t.id+'_tpProcedure',
                    items: [
                        {xtype:'clara'+t.proceduretype+'procedurepanel'},
                        {
                            xtype: 'panel',
                            id: t.id+'_tCosts',
                            title: 'Costs / Notes',
                            layout: 'border',                           
                            disabled:(t.procedure)?false:true,
                            items: [
                                    {
                                    	xtype:'form',
                                    	region:'north',
                                    	height:180,
                                    	layout:'absolute',
                                    	border:false,
                                    	items: [
												{
												    xtype: 'checkbox',
												    id:t.id+'_fldProcCRC',
												    value:'CRC',
												    boxLabel: '<div style="line-height:10px !important;position:relative;left:20px;top:-16px;"><span style="font-weight:800;font-size:11px;">Performed at TRI CRSC</span><br/><span style="font-size:11px;">Translational Research Institute (TRI) Clinical Research Services Core (CRSC)</span></div>',
												    x: 10,
												    y: 110,
												    ctCls:'proc-cb-crc',
												    tabIndex:211,
												    width: 320,
												    disabled:t.readOnly,
												    hidden: (t.isSubprocedure)?true:false,
												    checked:((t.procedure && t.procedure.location == 'CRC') || (t.parentProcedure && t.parentProcedure.location == 'CRC'))?true:false,
												    listeners: {
                            			        		'check': function(fld,checked){
                            			        			t.procedure.location = (checked)?"CRC":"";
                            			        		}
                            			        	}
												},
												{
												    xtype: 'label',
												    id:t.id+'_lblOtherMiscProcDesc',
												    text: '"OTHER" Procedure description:',
												    x: 50,
												    y: 60,
												    style: 'font-size:14px;text-align:left;',
												    width: 180,
												    hidden: (!t.isOtherMiscProcedure)
												},{
												    xtype: 'textfield',
												    x: 50,
												    y: 80,
												    tabIndex:199,
												    id:t.id+'_fldOtherMiscProcDesc',
												    style: 'font-size:14px;text-align:left;',
												    disabled:t.readOnly,
												    width: 180,
												    hidden: (!t.isOtherMiscProcedure),
												    		listeners:{
							    			            		change:function(f,v,ov){
							    			            			////cdebug("setting to "+v);
							    			            			t.procedure.description = v;
							    			            		}
							    			            	},
							    			            	value:(t.procedure && t.procedure.description)?t.procedure.description:""
												},{
												    xtype: 'label',
												    text: 'Expense Category:',
												    x: 50,
												    y: 10,
												    style: 'font-size:14px;text-align:left;',
												    width: 180,
												    
												    hidden: (t.isSubprocedure)?true:false
												},
												{

					    			                xtype: 'combo',
					    			                emptyText:'Choose..',
					    			                disabled:t.readOnly || (t.proceduretype && (t.proceduretype == "drug" || t.proceduretype == "drugdispensing")),
					    			                id:t.id+'_fldExpenseCategory',
					    			                x:50,
					    			                y:30,
					    			                width: 180,
					    			                tabIndex:210,
					    			                hidden: (t.isSubprocedure)?true:false,
					    			                typeAhead: true,
					                                triggerAction: 'all',
					                                store: new Ext.data.JsonStore({
					                                   fields:['id','desc'],
					                                   autoLoad:true,
					                                   url:(t.proceduretype == "outside")?(appContext+'/static/xml/budget-expense-categories-outside.json'):(appContext+'/static/xml/budget-expense-categories.json'),
					                                   sortInfo:{
													    	field:'desc',
													    	direction:'ASC'
													    }
					                                }),
					                                lazyRender: true,
					                                displayField:'desc',
					                                value:(t.proceduretype && (t.proceduretype == "drug" || t.proceduretype == "drugdispensing"))?"None":null,
					                                allowBlank:false,
					                                mode:'local',
					                                selectOnFocus:true,
					                                listeners:{
					    			            		change:function(f,v,ov){
					    			            			////cdebug("setting to "+v);
					    			            			t.procedure.expensecategory = v;
					    			            		}
					    			            	},
					                                listClass: 'x-combo-list-small'
					    			            
												},
												{
												    xtype: 'label',
												    text: 'Cost',
												    x: 350,
												    y: 50,
												    style: 'font-size:14px;text-align:right;',
												    width: 85,
												    hidden:(t.proceduretype != "misc" || (t.isSubprocedure && t.proceduretype == 'officevisit'))?true:false
												},
                                    	        {
                                    	        	xtype: 'numberfield',
                                    	            value:(t.procedure && t.procedure.cost)?t.procedure.cost.misc:0,
                                    	            x: 455,
                                    	            y: 50,
                                    	            width: 110,
                                    	            id: t.id+'_fldMiscCost',
                                    	            tabIndex:200,
                                    	            disabled:t.readOnly,
                                    	            enableKeyEvents:true,
												    hidden:(t.proceduretype != "misc" ||  (t.isSubprocedure && t.proceduretype == 'officevisit'))?true:false,
                            			            listeners: {
                            			        		'change': function(fld,newv,oldv){
                            			        			t.procedure.cost.misc = jQuery("#"+t.id+"_fldMiscCost").val();
                            			        			Ext.getCmp(t.id+"_fldResidual").getEl().update(Ext.util.Format.usMoney(t.procedure.cost.getResidual()));
                            			        		},
                            			        		'keyup':function(fld,e){
                            			        			t.procedure.cost.misc = jQuery("#"+t.id+"_fldMiscCost").val();
                            			        			Ext.getCmp(t.id+"_fldResidual").getEl().update(Ext.util.Format.usMoney(t.procedure.cost.getResidual()));
                            			        		}
                            			        	}
                                    	        },
                                    	        {
												    xtype: 'checkbox',
												    id:t.id+'_fldProcConditional',
												    value:'Conditional',
												    boxLabel: '<span style="font-size:11px;color:#777;">This is a conditional procedure</span>',
												    x: 375,
												    y: 150,
												    tabIndex:203,
												    ctCls:'proc-cb-conditional',
												    width: 235,
												    disabled:t.readOnly,
												    hidden: (t.isSubprocedure)?true:false,
												    checked:((t.procedure && t.procedure.conditional == true) || (t.parentProcedure && t.parentProcedure.conditional == true))?true:false,
												    listeners: {
                            			        		'check': function(fld,checked){
                            			        			t.procedure.conditional = (checked)?true:false;
                            			        		}
                            			        	}
												},
                                    	        {
                                    	            xtype: 'numberfield',
                                    	            x: 455,
                                    	            y: 100,
                                    	            width: 110,
                                    	            tabIndex:201,
                                    	            id: t.id+'_fldPrice',
                                    	            disabled:t.readOnly,
                                    	            enableKeyEvents:true,
                                    	            value:(t.procedure && t.procedure.cost)?t.procedure.cost.price:"",
                            			            listeners: {
                            			        		'change': function(fld,newv,oldv){
                            			        			t.procedure.cost.price = jQuery("#"+t.id+"_fldPrice").val();
                            			        			Ext.getCmp(t.id+"_fldResidual").getEl().update(Ext.util.Format.usMoney(t.procedure.cost.getResidual()));
                            			        		},
                            			        		'keyup':function(fld,e){
                            			        			t.procedure.cost.price = jQuery("#"+t.id+"_fldPrice").val();
                            			        			Ext.getCmp(t.id+"_fldResidual").getEl().update(Ext.util.Format.usMoney(t.procedure.cost.getResidual()));
                            			        			
                            			        		}
                            			        	},
                            			        	hidden: (t.isSubprocedure && t.proceduretype == 'officevisit')?true:false
                                    	        },
                                    	        {
                                    	            xtype: 'numberfield',
                                    	            x: 455,
                                    	            y: 75,
                                    	            width: 110,
                                    	            id: t.id+'_fldSponsor',
                                    	            disabled:t.readOnly,
                                    	            tabIndex:200,
                                    	            enableKeyEvents:true,
                                    	            value:(t.procedure && t.procedure.cost)?t.procedure.cost.sponsor:"",
                            			            listeners: {
                            			        		'change': function(fld,newv,oldv){
                            			        			t.procedure.cost.sponsor = jQuery("#"+t.id+"_fldSponsor").val();
                            			        			var studyType = claraInstance.budget.studyType || '';
                            			        			clog("change: studytpye",studyType);
                            			        			//if (studyType == 'industry-sponsored' && jQuery("#"+t.id+"_fldPrice").val() == 0) {
                            			        			//	t.procedure.cost.price = jQuery("#"+t.id+"_fldSponsor").val();        			        				
                            			        			//	jQuery("#"+t.id+"_fldPrice").val(jQuery("#"+t.id+"_fldSponsor").val());
                            			        			//	Ext.getCmp(t.id+"_fldResidual").getEl().update(Ext.util.Format.usMoney(t.procedure.cost.getResidual()));
                            			        			//}
                            			        		},
                            			        		'keyup':function(fld,e){
                            			        			t.procedure.cost.sponsor = jQuery("#"+t.id+"_fldSponsor").val();
                            			        		}
                            			        	},
                            			        	hidden: (t.isSubprocedure && t.proceduretype == 'officevisit')?true:false
                                    	        },
                                    	        {
                                    	            xtype: 'label',
                                    	            text: 'Price',
                                    	            x: 350,
                                    	            y: 100,
                                    	            style: 'font-size:14px;text-align:right;',
                                    	            width: 85,
                                    	            hidden: (t.isSubprocedure && t.proceduretype == 'officevisit')?true:false
                                    	        },
                                    	        {
                                    	            xtype: 'label',
                                    	            text: 'Sponsor offer',
                                    	            x: 355,
                                    	            y: 75,
                                    	            style: 'font-size:14px;text-align:right;',
                                    	            hidden: (t.isSubprocedure && t.proceduretype == 'officevisit')?true:false
                                    	        },
                                    	        {
                                    	            xtype: 'label',
                                    	            text: 'Physician cost',
                                    	            x: 350,
                                    	            y: 30,
                                    	            style: 'font-size:14px;text-align:right;',
												    hidden:(t.proceduretype == "misc" || t.proceduretype == 'outside' || (t.isSubprocedure && t.proceduretype == 'officevisit'))?true:false
                                    	        },
                                    	        {
                                    	            xtype: 'label',
                                    	            text: 'Hospital cost',
                                    	            x: 350,
                                    	            y: 10,
                                    	            style: 'font-size:14px;text-align:right;',
                                    	            width: 85,
												    hidden:(t.proceduretype == "misc" || t.proceduretype == 'outside' || (t.isSubprocedure && t.proceduretype == 'officevisit'))?true:false
                                    	        },
                                    	        {
                                    	            xtype: 'label',
                                    	            text: 'Total',
                                    	            x: 350,
                                    	            y: 50,
                                    	            style: 'font-size:14px;text-align:right;',
                                    	            width: 85,
												    hidden:(t.proceduretype == "misc" || t.proceduretype == 'outside' || (t.isSubprocedure && t.proceduretype == 'officevisit'))?true:false
                                    	        },
                                    	        {
                                    	            xtype: 'label',
                                    	            text: 'Residual',
                                    	            x: 350,
                                    	            y: 125,
                                    	            style: 'font-size:14px;text-align:right;',
                                    	            width: 85,
                                    	            hidden: (t.proceduretype == 'outside' || (t.isSubprocedure && t.proceduretype == 'officevisit'))?true:false
                                    	        },
                                    	        {
                                    	            xtype: 'numberfield',
                                    	            value:((t.procedure && t.procedure.phys)?t.procedure.phys.cost:0),
                                    	            x: 455,
                                    	            y: 30,
                                    	            style: 'font-size:14px;text-align:right;color:#666;',
                                    	            width: 105,
                                    	            id: t.id+'_fldPhysCost',
                                    	            disabled:true, //(claraInstance.id > 199999),
                                    	            listeners:{
                                    	            	'change': function(fld,newv,oldv){
                            			        			t.procedure.phys.cost = jQuery("#"+t.id+"_fldPhysCost").val();
                            			        			Ext.getCmp(t.id+"_fldResidual").getEl().update(Ext.util.Format.usMoney(t.procedure.cost.getResidual()));
                            			        		},
                            			        		'keyup':function(fld,e){
                            			        			t.procedure.phys.cost = jQuery("#"+t.id+"_fldPhysCost").val();
                            			        			Ext.getCmp(t.id+"_fldResidual").getEl().update(Ext.util.Format.usMoney(t.procedure.cost.getResidual()));
                            			        			
                            			        		}
                                    	            },
												    hidden:(t.proceduretype == "misc" || t.proceduretype == 'outside' || (t.isSubprocedure && t.proceduretype == 'officevisit'))?true:false
                                    	        },
                                    	        {
                                    	            xtype: 'numberfield',
                                    	            value:((t.procedure && t.procedure.hosp)?t.procedure.hosp.cost:0),
                                    	            x: 455,
                                    	            y: 10,
                                    	            style: 'font-size:14px;text-align:right;color:#666;',
                                    	            width: 105,
                                    	            id: t.id+'_fldHospCost',
                                    	            listeners:{
                                    	            	'change': function(fld,newv,oldv){
                            			        			t.procedure.hosp.cost = jQuery("#"+t.id+"_fldHospCost").val();
                            			        			Ext.getCmp(t.id+"_fldResidual").getEl().update(Ext.util.Format.usMoney(t.procedure.cost.getResidual()));
                            			        		},
                            			        		'keyup':function(fld,e){
                            			        			t.procedure.hosp.cost = jQuery("#"+t.id+"_fldHospCost").val();
                            			        			Ext.getCmp(t.id+"_fldResidual").getEl().update(Ext.util.Format.usMoney(t.procedure.cost.getResidual()));
                            			        			
                            			        		}
                                    	            },
                                    	            disabled:true, //(claraInstance.id > 199999),
												    hidden:(t.proceduretype == "misc" || t.proceduretype == 'outside' || (t.isSubprocedure && t.proceduretype == 'officevisit'))?true:false
                                    	        },
                                    	        {
                                    	            xtype: 'label',
                                    	            text:Ext.util.Format.usMoney((t.procedure && t.procedure.cost)?t.procedure.cost.getTotal():''),
                                    	            x: 455,
                                    	            y: 50,
                                    	            style: 'font-size:14px;text-align:right;color:#666;',
                                    	            width: 105,
                                    	            id: t.id+'_fldTotal',
												    hidden:(t.proceduretype == "misc" || t.proceduretype == 'outside' || (t.isSubprocedure && t.proceduretype == 'officevisit'))?true:false
                                    	        },
                                    	        {
                                    	            xtype: 'label',
                                    	            text:Ext.util.Format.usMoney((t.procedure && t.procedure.cost)?t.procedure.cost.getResidual():''),
                                    	            x: 455,
                                    	            y: 125,
                                    	            style: 'font-size:14px;text-align:right;color:#666;',
                                    	            width: 105,
                                    	            id: t.id+'_fldResidual',
                                    	            hidden: (t.proceduretype == 'outside' || (t.isSubprocedure && t.proceduretype == 'officevisit'))?true:false                                                	        
                                    	        }
                                    	    ]
                                    },{
                                    	xtype:'tabpanel',
                                    	region:'center',
                                    	height:170,
                                    	defaults: {flex: 1, border: false},
                                    	activeTab:0,
                                    	
                                    	
                                    	items:[{
                                	      	title:'Billing Notes',
                                       		xtype: 'textarea',
										    id:t.id+'_fldNotes',
										    readOnly:t.readOnly,
										    emptyText:'Enter billing notes here...',
										    labelStyle: 'font-weight:bold;',
										    labelSeparator:'',
										    height:140,
                            	            tabIndex:220,
										    value:(t.procedure && t.procedure.notes)?t.procedure.notes:'',
	                			            listeners: {
	                			        		'change': function(fld,newv,oldv){
	                			        			t.procedure.notes = jQuery("#"+t.id+"_fldNotes").val();
	                			        		},
	                			        		'keyup':function(fld,e){
	                			        			t.procedure.notes = jQuery("#"+t.id+"_fldNotes").val();
	                			        		}
	                        	        	}
										},{
                                	      	title:'Budget / Coverage Notes',
                                       		xtype: 'textarea',
										    id:t.id+'_fldCoverageNotes',
										    readOnly:t.readOnly || !(claraInstance.HasAnyPermissions(['ROLE_BUDGET_REVIEWER','ROLE_COVERAGE_REVIEWER'])),
										    emptyText:'Enter budget / coverage notes here...',
										    labelStyle: 'font-weight:bold;',
										    labelSeparator:'',
										    height:140,
                            	            tabIndex:220,
										    value:(t.procedure && t.procedure.coverageNotes)?t.procedure.coverageNotes:'',
	                			            listeners: {
	                			        		'change': function(fld,newv,oldv){
	                			        			t.procedure.coverageNotes = jQuery("#"+t.id+"_fldCoverageNotes").val();
	                			        		},
	                			        		'keyup':function(fld,e){
	                			        			t.procedure.coverageNotes = jQuery("#"+t.id+"_fldCoverageNotes").val();
	                			        		}
	                        	        	}
										},{
                                    	       title:'Clinical Notes',
                                           		xtype: 'textarea',
    										    id:t.id+'_fldClinicalNotes',
    										    readOnly:t.readOnly,
                                	            tabIndex:225,
                                	            emptyText:'Enter clinical notes here...',
                                	            labelStyle: 'font-weight:bold;',
                                	            labelSeparator:'',
    										    height:140,
    										    value:(t.procedure && t.procedure.clinicalNotes)?t.procedure.clinicalNotes:'',
    	                			            listeners: {
    	                			        		'change': function(fld,newv,oldv){
    	                			        			t.procedure.clinicalNotes = jQuery("#"+t.id+"_fldClinicalNotes").val();
    	                			        		},
    	                			        		'keyup':function(fld,e){
    	                			        			t.procedure.clinicalNotes = jQuery("#"+t.id+"_fldClinicalNotes").val();
    	                			        		}
    	                        	        	}
    									
    										}],
										padding: 10,
                                    	border:false
                                    }
                            ]
                        }
                    ]
                },
                {
                    xtype: 'panel',
                    region: 'north',
                    width: 100,
                    height: 44,
                    border: false,
                    unstyled: true,
                    layout: 'absolute',
                    items: [
                        {
                            xtype: 'label',
                            text: (t.procedure)?t.procedure.description:'Choose a '+itemName,
                            style: 'font-size:18px;',
                            x: 10,
                            y: 10,
                            width: 560,
                            id: t.id+'_lblProcedureName'
                        }
                    ]
                }
            ];

            Clara.BudgetBuilder.ProcedureWindow.superclass.initComponent.call(this);
            
   
            
            if(!t.isSubprocedure || (t.isSubprocedure && t.proceduretype == "officevisit") || (t.editing && t.procedure.type == "officevisit" )){
            	Ext.getCmp(t.id+'_tpProcedure').add({
                    xtype: 'panel',
                    id:t.id+'_tVisits',
                    title: 'Billing Category',
                    disabled:(t.procedure)?false:true,
                    layout: 'fit',
                    tbar:[{
						iconCls:'icn-question-white',
						text:'Category Help',
						handler:function(){
							OpenHelpPage('billingcodes');
						}
					},'->',{
                    		xtype:'tbtext',
                    		text:'Set all visits to'
                    	},{
		                xtype: 'combo',
		                emptyText:'Category',
		                id:t.id+'_fldSetAllType',
		                width: 70,
		                typeAhead: false,
		                editable:false,
		                hidden:t.isSubprocedure,
		                disabled:t.readOnly,
                        triggerAction: 'all',
                        store: new Ext.data.SimpleStore({
                           fields:['type'],
                           data:  t.getAllowableBillingCategories()
                        }),
                        lazyRender: true,
                        displayField:'type',
                        mode:'local',
                        selectOnFocus:true,
                        listClass: 'x-combo-list-small'
		            },{
		                xtype: 'textfield',
		                id:t.id+'_fldSetAllCount',
		                disabled:t.readOnly,
		                width: 30,
		                value:'1'
		            },{
		                xtype: 'button',
		                text:'Set',
		                iconCls:'icn-arrow-270',
		                disabled:t.readOnly,
		                handler:function(){
		            		var tp = (t.isSubprocedure)?'X':Ext.getCmp(t.id+'_fldSetAllType').getValue();
		            		var ct = Ext.getCmp(t.id+'_fldSetAllCount').getValue();                            		
                    		var st = Ext.getCmp(t.id+'_gpProcedureVisits').getStore();
                    		st.each(function(rec){
                    			rec.beginEdit();
                    			rec.set('chargetype',tp);
                    			rec.set('repetitions',ct);
                    			rec.commit();
                    			rec.endEdit();
                    		});
                    		Ext.getCmp(t.id+'_gpProcedureVisits').fireEvent("afteredit",{grid:Ext.getCmp(t.id+'_gpProcedureVisits')});
		            	}
		            }],
                    items: [
                        {
                            xtype: 'editorgrid',
                            clicksToEdit:1,
                            view: new Ext.grid.GroupingView({
                                forceFit:true,
                                showGroupName:false,
                                groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "visits in this arm" : "Visit in this arm"]})'
                            }),
                            border: false,
                            store:new Ext.data.GroupingStore({
                				reader: new Ext.data.ArrayReader({},[
                					'armid','cycleid','visitid','arm','cyclevisitname','chargetype','repetitions'
                				]),
                				autoLoad:false,
                				remoteSort:true,
                				groupField:'arm'
                			}),
                            id: t.id+'_gpProcedureVisits',
                            columns: [
                                {
                                    xtype: 'gridcolumn',
                                    dataIndex: 'arm',
                                    menuDisabled:true,
                                    sortable: false,
                                    visible:false,
                                    hidden:true,
                                    width: 0
                                },{
                                    xtype: 'gridcolumn',
                                    dataIndex: 'cyclevisitname',
                                    menuDisabled:true,
                                    header: (t.isSubprocedure)?'From parent procedure':'Arms',
                                    sortable: false,
                                    width: 450
                                },
                                {
                                    xtype: 'gridcolumn',
                                    dataIndex: 'chargetype',
                                    hidden:t.isSubprocedure,
                                    menuDisabled:true,
                                    header: 'Category',
                                    sortable: false,
                                    css:'border-left:1px dotted #888;border-right:1px dotted #888;',
                                    width: 70,
                                    editor: !t.readOnly?new Ext.form.ComboBox({
                                        typeAhead: true,
                                        triggerAction: 'all',
                                        store: new Ext.data.SimpleStore({
                                        	fields:['type'],
                                        	data: t.getAllowableBillingCategories()
                                        }),
                                        lazyRender: true,
                                        editable:false,
                                        forceSelection:true,
                                        displayField:'type',
                                        mode:'local',
                                        selectOnFocus:true,
                                        listeners:{
                                        	beforeshow: function(combo){
                                        		// force check on getAllowableBillingCategories for new procedures, so determine which billing categories can be used here.
                                        		clog("Beforeshow: combostore",combo.getStore().count, combo.getStore());
                                        		combo.getStore().loadData(t.getAllowableBillingCategories());
                                        	},
                                        	select:function(combo,rec,idx){
                                        		
                                        		if (Ext.isIE){
                                        			// combo.fireEvent("change", combo, rec.get("type"),"");
                                        			// rec.beginEdit();
                                        			rec.commit();
                                        			rec.endEdit();
                                        		}
                                        	}
                                        },
                                        listClass: 'x-combo-list-small'
                                    }):null
                                },
                                {
                                    xtype: 'gridcolumn',
                                    dataIndex: 'repetitions',
                                    menuDisabled:true,
                                    css:'border-left:1px dotted #888;border-right:1px dotted #888;',
                                    header: '#',
                                    sortable: false,
                                    width: 35,
                                    editor: !t.readOnly?new Ext.form.NumberField({
                                        allowBlank: true,
                                        allowNegative: false,
                                        maxValue: 100000
                                    }):null
                                },
                                {

                                	xtype:'actioncolumn',
                                	hidden:t.readOnly,
                                	menuDisabled:true,
                                	width:35,
                                	items:[{
                                		icon:appContext+'/static/images/icn/minus-white.png',
                                		tooltip:'Delete this visit',
                                		handler: function(grid,rowIndex,colIndex){
                                			if(colIndex == 4) { //del is hit...
                                        		var record = grid.getStore().getAt(rowIndex);
                                        		
                                        		record.data.chargetype = "";
                                        		record.data.repetitions = "";
                                        		record.commit();
                                        		
                                        		grid.fireEvent("afteredit",{grid:grid});
                        		            	
                                        	}
                                		}
                                	}]
                                }
                            ],
                            listeners:{

                            	beforeedit:function(e){
                            		e.record.data.repetitions = (e.field == 'chargetype' && (e.record.data.repetitions <=0 || e.record.data.repetitions =="" || e.record.data.repetitions == null))?1:e.record.data.repetitions;
                            		e.record.commit();
                            	},
                            	afteredit:function(e){
                            		var d = e.grid.getStore().data.items;
                            		t.refreshVisitProcedures(d);
                            	}
                            }
                        }
                    ]
                });
            }
            
            if (!t.isSubprocedure && (t.proceduretype != "drug" && t.proceduretype !="drugdispensing")){ //&& (t.proceduretype != 'misc' && t.proceduretype != 'outside')
            	Ext.getCmp(t.id+"_tpProcedure").add({id:t.id+"_tSubprocedures",xtype:'clarasubprocedurepanel'});
            }
            
            //Alt. codes panel
            if (t.proceduretype != "drug" && t.proceduretype !="drugdispensing" && claraInstance.HasAnyPermissions(['VIEW_BUDGET_OTHER_PROCEDURE_CODES'])) Ext.getCmp(t.id+'_tpProcedure').add({id:t.id+"_tCodes",xtype:'claraCodePanel',disabled:(t.procedure)?false:true});
            
            // For editing procedures
            if (t.editing){
            	Ext.getCmp(t.id+"_tpProcedure").setActiveTab(1);
            	t.refreshProcedure();
            	if (!t.isSubprocedure){
            		var subProcedurePanel = Ext.getCmp(t.id+"_tSubprocedures");
            		if (subProcedurePanel) {
            			if ((t.proceduretype == "misc" && t.procedure.expensecategory == "Communication Order")) subProcedurePanel.setDisabled(true);
            			else {
            				subProcedurePanel.setDisabled(false);
            				subProcedurePanel.refreshSubprocedures();
            			}
            		}
            	}
            }
        }
    });