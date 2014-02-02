Ext.ns('Clara','Clara.History');

Clara.NewFormListWindow = Ext
		.extend(
				Ext.Window,
				{
					id : 'new-form-window',
					title : 'New form..',
					height : 500,
					activeTab : 0,
					modal : true,
					width : 750,
					layout : 'border',
					margins : '3 3 3 3',
					cmargins : '3 3 3 3',
					constructor : function(config) {
						Clara.NewFormListWindow.superclass.constructor.call(this,
								config);
					},
					initComponent : function() {
						var t = this;
						var url =  appContext+"/ajax/"+claraInstance.type+"s/"+claraInstance.id+"/new-form-types.xml";
						var config = {
							
							items : [
									{
										html : "<div class='window-header' style='background-color:#dce6f5;border-bottom:0px;'><h1 class='window-header-title'>What kind of form do you want to create?</h1></div>",
										border : false,
										region : 'north'
									},
									{
										xtype : 'grid',
										region:'center',
										viewConfig: {
									        forceFit: true,
									        emptyText:'You cannot add forms to this '+claraInstance.type+' at this time (check its status, and make sure you are a staff member on the study).'
										},
										columns:[{

											dataIndex : 'id',
											renderer : function(v, p, record) {
												return "<div class='row-newform row-newform-"
														+ record.get("id")
														+ "'><div class='newform-description'><h3 class='newform-shortdesc'><a href='"
														+ appContext + "/" + record.get("type") + "s/"+ claraInstance.id + "/" + record.get("type") + "-forms/" + record.get("id") + "/create"
														+ "'>"
														+ record.get("title")
														+ "</a></h3><span class='newform-longdesc'>"
														+ record.get("description")
														+ "</span></div></div>";
											}
										
											
										}],
										store : new Ext.data.Store(
												{
													proxy : new Ext.data.HttpProxy(
															{
																url : url,
																method : "GET",
																headers : {
																	'Accept' : 'application/xml;'
																}
															}),
													autoLoad : true,
													reader : new Ext.data.XmlReader(
															{
																record : 'form',
																root : 'forms',
																fields : [
																		{
																			name : 'id',
																			mapping : '@id'
																		},
																		{
																			name : 'type',
																			mapping : '@type'
																		},
																		{
																			name : 'title',
																			mapping : '@title'
																		},
																		{
																			name : 'description',
																			mapping : 'description'
																		} ]
															})
												})
									} ],
							buttons : [ {
								text : 'Close',
								handler : function() {
									t.close();
								}
							} ]
						};
						Ext.apply(this, Ext.apply(this.initialConfig, config));
						Clara.NewFormListWindow.superclass.initComponent.apply(
								this, arguments);
					}
				});
Ext.reg('claraformlistwindow', Clara.NewFormListWindow);


Clara.HistoryPanel = Ext.extend(Ext.grid.GridPanel, {
	height:350,
	title:'History',
	iconCls:'icn-clock-history',
	layout:'fit',

	constructor:function(config){		
		Clara.HistoryPanel.superclass.constructor.call(this, config);
	},	
	
	initComponent: function(){
		var t=this;
		var objType = (this.type=="contract")?"edu.uams.clara.webapp.contract.domain.Contract":"edu.uams.clara.webapp.protocol.domain.Protocol";
		var config = {
				view: new Ext.grid.GroupingView({
			        forceFit: true,
					headersDisabled: true,
					enableGroupingMenu:false,
			        groupTextTpl: '{[values.rs[0].data["formTypeDesc"]]} (Form ID# {[values.rs[0].data["parentFormId"]]})'
			    }),
				listeners:{
					activate:function(p){
						p.getStore().load();
					}
				},
				tbar:[{
					enableToggle: true,
					iconCls:'icn-ui-check-box-uncheck',
					text:'Group by Form',
					pressed: false,
			            toggleHandler: function(item, pressed){
			            	var b = this;
	    	        		if (pressed){
	    	        			t.store.groupBy("parentFormId");
	    	        			b.setIconClass('icn-ui-check-box');
	    	        		} else {
	    	        			t.store.clearGrouping();
	    	        			b.setIconClass('icn-ui-check-box-uncheck');
	    	        		}
		 	    		}	
				},'->','-',{
		    		xtype:'button',
		    		tooltip:'Print list (opens new window)',
		    		tooltipType:'title',
		    		iconCls:'icn-printer',
					handler: function(){
						Ext.ux.Printer.print(t,{ keepWindowOpen:false, title:"History log for "+claraInstance.type+" #"+claraInstance.id });
					}
		    	}],
	        	border:false,
	        	loadMask:true,
				store: new Ext.data.GroupingStore({
					autoLoad:false,
					groupField: '',
					remoteGroup: false,
					sortInfo:{
						field:'timestamp',
						direction:'DESC'
					},
						proxy: new Ext.data.HttpProxy({
							url:appContext+"/ajax/history/history.xml?id="+claraInstance.id+"&type="+objType,
							method:'GET',
							headers:{'Accept':'application/xml;charset=UTF-8'}
						}),
						reader: new Ext.data.XmlReader({
							record:'log',
							root: 'logs',
						fields: [{name:'id', mapping:'@id'},
						    {name:'desc', mapping:''},
							{name:'actor', mapping:'@actor'},
							{name:'formId', mapping:'@form-id'},
							{name:'formType', mapping:'@form-type'},
							{name:'formTypeDesc', mapping:'@form-type', convert:function(v){return v.toHumanReadable();}},
							{name:'parentFormId', mapping:'@parent-form-id'},
							{name:'eventType', mapping:'@event-type'},
							{name:'timestamp', mapping:'@timestamp', type:'timestamp'},//'m/d/Y g:i:s'},
							// {name:'creationDateString', mapping:'@timestamp', convert: function(v){ return Ext.util.Format.date(Date(v), "m/d/Y g:ia");}},
							{name:'datetime', mapping:'@date-time'}
						]})
					}),
					
					colModel: new Ext.grid.ColumnModel({
				        defaults: {
				            width: 120,
				            sortable: true
				        },
				        columns: [{
					    	header:'Parent Form ID',
					    	dataIndex: 'parentFormId',
					    	hidden: true,
					    	menuDisabled:true
					    },{
					    	header:'Form ID',
					    	dataIndex: 'formId',
					    	hidden: true,
					    	menuDisabled:true
					    },
						            {
						                header: 'Date', width: 135,fixed:true, dataIndex: 'datetime',
						                xtype: 'datecolumn', format: 'm/d/Y h:ia'
						            }
						            ,
						            {header: 'Note', dataIndex: 'desc',renderer:function(v,p,r){
						            	var html = "<div>"+v+"</div>";
						            	
						            	var logNote = jQuery(html).find("span.log-committee-note-body");
						            	
						            	if (logNote.length > 0 && jQuery.trim(jQuery(logNote).text()) == "") {
						            		
						            		html = jQuery(html).clone().find(".log-committee-note").remove().end();
						            		
						            	} 
						            	var h = "<div class='history-note'>";
						            	h += "<div class='history-note-body'>"+jQuery(html).html()+"</div>";
						            	h += "<div class='history-note-meta'>";
						            	h += "<div class='history-meta-formevent'><span class='formType'>"+r.get("formType")+"</span>: <span class='eventType'>"+r.get("eventType")+"</span> by <span class='actor'>"+r.get("actor")+"</span></div>";
						            	return h + "</div></div>";
						            }},{
								    	header:'Form',
								    	dataIndex: 'formType',
								    	hidden: false,
								    	width:150,
								    	fixed:true,
								    	menuDisabled:true,
								    	renderer:function(v,p,r){
								    		
								    		var html = v.toHumanReadable();
								    		var linkableEvents = ["STUDY_SUBMITTED_TO_STAFFS","CONTINUING_REVIEW_RESPONSE_TO_MAJOR_CONTINGENCIES_SUBMITTED_TO_IRB_OFFICE","CONTINUING_REVIEW_RESPONSE_TO_MINOR_CONTINGENCIES_SUBMITTED","HUD_RENEWAL_RESPONSE_TO_MAJOR_CONTINGENCIES_SUBMITTED_TO_IRB_OFFICE","HUD_RENEWAL_RESPONSE_TO_MINOR_CONTINGENCIES_SUBMITTED","NEW_HUMAN_SUBJECT_RESEARCH_DETERMINATION_SUBMITTED","MODIFICATION_RESPONSE_TO_MAJOR_CONTINGENCIES_SUBMITTED_TO_IRB_OFFICE","MODIFICATION_RESPONSE_TO_MINOR_CONTINGENCIES_SUBMITTED","NEW_SUBMISSION_RESPONSE_TO_MAJOR_CONTINGENCIES_SUBMITTED_TO_IRB_OFFICE","NEW_SUBMISSION_RESPONSE_TO_MINOR_CONTINGENCIES_SUBMITTED","REPORTABLE_NEW_INFORMATION_RESPONSE_TO_MAJOR_CONTINGENCIES_SUBMITTED_TO_IRB_OFFICE","REPORTABLE_NEW_INFORMATION_RESPONSE_TO_MINOR_CONTINGENCIES_SUBMITTED"];
								    		
								    		if (linkableEvents.hasValue(r.get("eventType"))){
								    			var summaryUrl = appContext+"/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/"+r.get("formId")+"/"+r.get("formType").toUrlEncodedName()+"/summary?noheader=true&review=false&historyid="+r.get("id");
								    			html += "<a href='"+summaryUrl+"' parent='_blank'><div class='icon-rowaction icn-blue-document-invoice' style='padding-left:20px;'>View summary</div></a>";
								    		}
								    		
								    		return html;
								    	}
								    }
				        ]
				    }),
				    viewConfig: {
				        forceFit: true
		        	}
		        
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.HistoryPanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('clarahistorypanel', Clara.HistoryPanel);