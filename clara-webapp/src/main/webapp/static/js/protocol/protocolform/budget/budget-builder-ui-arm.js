Ext.ns('Clara.BudgetBuilder');

Clara.BudgetBuilder.ArmGridPanel = Ext.extend (Ext.grid.EditorGridPanel,{
	initComponent: function() {
		var t = this;
		var config = {
				title: 'Arms',
				view: new Ext.grid.GridView({
					emptyText: '<h1>No arms defined for this study phase.</h1><h2>Click "Add" above to create a new arm.</h2>',
					getRowClass: function(row,index){
						var cls='';
						var armId = row.data.id;
						var epoch = budget.getEpoch(Ext.getCmp('winBudgetStructure').selectedEpoch.id);

						if (epoch.getArm(armId).validate().length > 0){
							return 'error-incomplete';
						}
						return cls;
					}
				}),
				ddGroup: 'ddArms',
				ddText: 'Drag arm to new position to re-order.',
				enableDragDrop: true,
				region:'west',
				width:348,
				margins: '0 0 0 5',
				itemId: 'gpArms',
				
				store: new Ext.data.ArrayStore({fields:['id','index','name','subjectcount','notes']}),
				selModel:new Ext.grid.RowSelectionModel({
					singleSelect:true,
					listeners: {
						rowselect:function(sm,idx,r){
							var d =  r.data;
							Clara.BudgetBuilder.MessageBus.fireEvent('armselected', budget.getArm(d.id));
						}
					}
				}),

				armErrorTooltipRowRenderer:function(value, metadata, rec){
					var error = false;
					var msg = "";

					var armId = rec.get('id');
					var epoch = budget.getEpoch(Ext.getCmp('winBudgetStructure').selectedEpoch.id);

					if (epoch.getArm(armId).validate().length > 0){
						msg = 'There are no cycles or visits assigned to this arm. Add at least one cycle (with at least on visit in that cycle) in order to continue.';
					}


					metadata.attr = 'ext:hide="user" ext:qtip="' + msg + '"';
					return value;
				},

				listeners:{
					render: function(grid){
						var ddrow = new Ext.dd.DropTarget(grid.container, {
							ddGroup : 'ddArms',
							copy:false,
							notifyDrop : function(dd, e, data){
								var ds = grid.store;
								// move arm rows in grid's store first..
								var sm = grid.getSelectionModel();
								var rows = sm.getSelections();
								if(dd.getDragData(e)) {
									var cindex=dd.getDragData(e).rowIndex;
									if(typeof(cindex) != "undefined") {
										for(i = 0; i <  rows.length; i++) {
											ds.remove(ds.getById(rows[i].id));
										}
										ds.insert(cindex,data.selections);
										//sm.clearSelections();
									}
									// SORT, THEN UPDATE EPOCH HERE.
									var armIndex=0;
									ds.each(function(rec){
										budget.getArm(rec.get("id")).setIndex(armIndex++);
									});
									budget.sortAllArms();
									Clara.BudgetBuilder.MessageBus.fireEvent('epochcontentupdated', Ext.getCmp('winBudgetStructure').selectedEpoch);

								}

							}
						});
					},
					afteredit:function(e){
						var d = e.record.data;
						clog("afteredit",d);
						budget.updateArm(new Clara.BudgetBuilder.Arm({id:d.id, index:d.index, name:d.name, notes:d.notes}));
					}
				},
				columns: [
				          {
				        	  xtype: 'gridcolumn',
				        	  header: 'Name',
				        	  menuDisabled:true,
				        	  id:'name',
				        	  dataIndex:'name',
				        	  sortable: false,
				        	  width: 140,
				        	  renderer:t.armErrorTooltipRowRenderer,
				        	  editor: {
				        		  xtype: 'textfield',
				        		  allowBlank: true,
				        		  validator: function(v) {
				        			  var invalids = "/\*'?[]:";
				        			  for (var i=0, l=v.length;i<l;i++){
				        				  if (invalids.indexOf(v[i]) > -1) return false;
				        			  }
				        		      return true;
				        		  }
				        	  }
				          },
				          {
				        	  xtype: 'gridcolumn',
				        	  header: 'Notes',
				        	  menuDisabled:true,
				        	  dataIndex:'notes',
				        	  sortable: false,
				        	  width: 200,
				        	  renderer:function(v,p,r){
				        		  return "<div class='wrap arm-row-notes'>"+v+"</div>";
				        	  },
				        	  editor: {
				        		  xtype: 'textarea',
				        		  allowBlank: true,
				        		  validator: function(v) {
				        			  var invalids = "/\*'?[]:";
				        			  for (var i=0, l=v.length;i<l;i++){
				        				  if (invalids.indexOf(v[i]) > -1) return false;
				        			  }
				        		      return true;
				        		  }
				        	  }
				          }
				          ],
				          tbar: {
				        	  xtype: 'toolbar',
				        	  items: [
				        	          {
				        	        	  xtype: 'button',
				        	        	  text: 'Add',
				        	        	  iconCls:'icn-plus-button',
				        	        	  itemId: 'btnAddArm',
				        	        	  id: 'btnAddArm',
				        	        	  handler:function(){
				        	        		  var id = budget.newId();
				        	        		  var a = new Clara.BudgetBuilder.Arm({
				        	        			  id:id,
				        	        			  parentEpoch:Ext.getCmp('winBudgetStructure').selectedEpoch,
				        	        			  name:'Unnamed Arm',
				        	        			  index:id
				        	        		  });
				        	        		  budget.addArm(Ext.getCmp('winBudgetStructure').selectedEpoch, a);
				        	        	  }
				        	          },
				        	          {
				        	        	  xtype: 'tbseparator'
				        	          },
				        	          {
				        	        	  xtype: 'button',
				        	        	  text: 'Remove',
				        	        	  iconCls:'icn-minus-button',
				        	        	  disabled:true,
				        	        	  itemId: 'btnRemoveArm',
				        	        	  id: 'btnRemoveArm',
				        	        	  handler:function(){
				        	        		  // Get ID of selected arm
				        	        		  var selectedRecord = Ext.getCmp('gpArms').getSelectionModel().getSelected();  
				        	        		  Ext.Msg.show({
				        	        			  title:'Remove arm?',
				        	        			  msg: 'Are you sure you want to remove this arm?',
				        	        			  buttons: Ext.Msg.YESNOCANCEL,
				        	        			  fn: function(btn){
				        	        				  if (btn == 'yes'){

				        	        					  budget.removeArm({id:selectedRecord.get('id')});	
				        	        				  }
				        	        			  },
				        	        			  animEl: 'elId',
				        	        			  icon: Ext.MessageBox.WARNING
				        	        		  });

				        	        	  }
				        	          },
				        	          {
				        	        	  xtype: 'tbseparator'
				        	          },
				        	          {
				        	        	  xtype: 'button',
				        	        	  text: 'Copy',
				        	        	  disabled:true,
				        	        	  iconCls:'icn-layers',
				        	        	  itemId: 'btnCopyArm',
				        	        	  id: 'btnCopyArm',
				        	        	  handler:function(){
				        	        		  var selectedRecord = Ext.getCmp('gpArms').getSelectionModel().getSelected();
				        	        		  budget.addArm(Ext.getCmp('winBudgetStructure').selectedEpoch, Ext.getCmp('winBudgetStructure').selectedArm.copy());
				        	        	  }
				        	          }
				        	          ]
				          }
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.BudgetBuilder.ArmGridPanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claraBudgetArmGridPanel', Clara.BudgetBuilder.ArmGridPanel);