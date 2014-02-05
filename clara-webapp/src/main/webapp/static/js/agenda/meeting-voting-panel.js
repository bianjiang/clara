Ext.ns('Clara.IRBMeeting');

Clara.IRBMeeting.MotionPanel = Ext.extend(Ext.grid.GridPanel, {
	id: 'clara-irbmeeting-motion-panel',
	frame:false,
	border:false,
	trackMouseOver:false,
	constructor:function(config){		
		Clara.IRBMeeting.MotionPanel.superclass.constructor.call(this, config);
		if(Clara.IRBMeeting.MessageBus){
			Clara.IRBMeeting.MessageBus.on('agendaitemchosen', this.onAgendaItemChosen, this);
			Clara.IRBMeeting.MessageBus.on('motionadded', this.onMotionAdded, this);
			Clara.IRBMeeting.MessageBus.on('motionchosen', this.onMotionChosen, this);
			Clara.IRBMeeting.MessageBus.on('motionsupdated', this.onMotionsUpdated, this);
		}
	},
	onMotionChosen: function(){
		var t = this;
		clog("Motion Chosen.",Clara.IRBMeeting.CurrentMotionRecord,"Canedit?",canEditMeeting);
		if (canEditMeeting && !(isIrbOffice && meeting.status == "SENT_TO_CHAIR") && /* !(isChair && meeting.status == "SENT_TO_CHAIR") && */ !(isIrbOffice && meeting.status == "SENT_TO_TRANSCRIBER")) Ext.getCmp("btnRemoveMotion").setDisabled(false);
	},
	onMotionsUpdated: function(){
		var t = this;
		Ext.getCmp("btnRemoveMotion").setDisabled(true);
		t.reloadMotionGridPanel();
	},
	onMotionAdded: function(){
		var t = this;
		Ext.getCmp("btnRemoveMotion").setDisabled(true);
		t.reloadMotionGridPanel();
	},
	onAgendaItemChosen: function(){
		clog("MOTION AI CHOSE",Clara.IRBMeeting.CurrentAgendaItemRecord.get("category"));
		var t = this;
		if (Clara.IRBMeeting.CurrentAgendaItemRecord.data && Clara.IRBMeeting.CurrentAgendaItemRecord.get("category") == "REPORTED") Ext.getCmp("btnMakeMotion").setDisabled(true);
		else {
			if (canEditMeeting && !(isIrbOffice && meeting.status == "SENT_TO_CHAIR") && /* !(isChair && meeting.status == "SENT_TO_CHAIR") && */ !(isIrbOffice && meeting.status == "SENT_TO_TRANSCRIBER")) Ext.getCmp("btnMakeMotion").setDisabled(false);
		}
		Ext.getCmp("btnRemoveMotion").setDisabled(true);
		t.reloadMotionGridPanel();
	},
	reloadMotionGridPanel: function(){
		var t = this;
		clog("reloadMotionGridPanel: Clara.IRBMeeting.CurrentAgendaItemRecord",Clara.IRBMeeting.CurrentAgendaItemRecord);
		if (Clara.IRBMeeting.CurrentAgendaItemRecord.data) {
			var a = meeting.getActivityForItem(Clara.IRBMeeting.CurrentAgendaItemRecord.data.id);
			if (a != null) t.reconfigure(a.getMotionStore(),t.getColumnModel());
			else t.getStore().removeAll();
		}
	},
	tbar:{
		xtype:'toolbar',
		items:['->',{
        	xtype:'button',
        	text:'Make Motion..',
        	id:'btnMakeMotion',
        	iconCls:'icn-plus-button',
        	disabled:true,
        	handler:function(){
    			new Clara.IRBMeeting.VoteWindow({editing:false}).show();
    		}
        },{
        	xtype:'button',
        	text:'Remove..',
        	id:'btnRemoveMotion',
        	iconCls:'icn-minus-button',
        	disabled:true,
        	handler:function(){
        	Ext.Msg.show({
    			title:'Remove',
    			width:350,
    			msg:'<h1>Are you sure you want to remove the selected motion?</h1>',
    			buttons:Ext.Msg.YESNOCANCEL,
    			fn:function(btn,reason){
    				if (btn == 'yes'){
    					clog(Clara.IRBMeeting.CurrentMotionRecord);
    					meeting.removeMotionByTimestamp(Clara.IRBMeeting.CurrentMotionRecord.data.timestamp);
    					Clara.IRBMeeting.MessageBus.fireEvent('motionsupdated', this);  
    				}
    			},
    		    icon: Ext.MessageBox.QUESTION
    		});
    		}
        }]
	},
	initComponent: function(){
		var t = this;
		clog(t.agenda);
		var config = {
				loadMask:true,
				store: new Ext.data.ArrayStore({
					idIndex:0,
					fields:[{name: 'timestamp', type: 'date', dateFormat: 'n/j h:ia'},
					        {name: 'motion', type: 'string'},
					        {name: 'adultrisk', type: 'string'},
					        {name: 'pediatricrisk', type: 'string'},
					        {name: 'reviewtype', type: 'string'},
					        {name: 'reviewperiod', type: 'float'},
					        {name: 'madeby', type: 'string'},
					        {name: 'secondedby', type: 'string'},
					        {name: 'madebyid'},
					        {name: 'secondedbyid'},
					        {name: 'yesvotes', type: 'float'},
					        {name: 'novotes', type: 'float'},
					        {name: 'abstainvotes', type: 'float'},
					        {name: 'notvotingvotes', type: 'float'},
					        {name: 'consentwaived', type: 'string'},
					        {name: 'consentdocumentationwaived', type: 'string'},
					        {name: 'hipaa', type: 'string'},
					        {name: 'hipaawaived', type: 'string'},
					        {name:'ncdetermination',type:'string'},
					        {name:'ncreportable',type:'string'},
					        {name:'UPIRTSO',type:'string'}
					        
					       // {name: 'risk', type: 'string'},
					       // {name: 'reviewperiod', type: 'float'},

					        ]

				}),
				view: new Ext.grid.GridView({
					forceFit:true,
		    
		    		emptyText:'<h1>There are no motions made on this item yet.</h1>'
		    	}),
		    	columns: [
		    	          new Ext.grid.RowNumberer(),
		    	          {header:'Motion Details', sortable:false, dataIndex:'motion', renderer:function(v,p,r){
		    	        	  
		    	        	 var html = "<div class='motion-details'><div class='motion-detail-header'><table class='motion-votes'><thead><tr><th>Yes</th><th>No</th><th>Abstain</th><th>Absent</th></thead><tbody><tr>"
			    	        		 + "<td>"+r.get("yesvotes")+"</td>"
			    	        		 + "<td>"+r.get("novotes")+"</td>"
			    	        		 + "<td>"+r.get("abstainvotes")+"</td>"
			    	        		  + "<td>"+r.get("notvotingvotes")+"</td></table>";
		    	        	 html += "<h1>"+v+"</h1><div style='clear:both;'></div></div>";
	
		    	        	 var adultRisk = r.get("adultrisk");
		    	        	 var pedRisk = r.get("pediatricrisk");
		    	        	 
		    	        	 if (Clara.IRBMeeting.CurrentAgendaItemRecord.get("category") !="MINUTES") {
		    	        		 html +="<div class='motion-values'><dl>"   	
			    	        	 + "<dt>Adult Risk</dt><dd>"+((adultRisk == "RISK_ADULT_1")?"Minimal":((adultRisk == "RISK_ADULT_2")?"Greater than Minimal":"N/A"))+"</dd>"
			    	        	 + "<dt>Pediatric Risk</dt><dd>"+(pedRisk?pedRisk:"Not specified")+"</dd>"
			    	        	 + "<dt>Next Review Type</dt><dd>"+(r.get("reviewtype") || "Not specified")+"</dd>"
			    	        	 + "<dt>Review Period</dt><dd>"+r.get("reviewperiod")+"</dd>"
			    	        	 + "<dt>Consent Waived</dt><dd>"+r.get("consentwaived")+"</dd>"
			    	        	 + "<dt>Consent Doc. Waived</dt><dd>"+r.get("consentdocumentationwaived")+"</dd>"
			    	        	 + "<dt>HIPAA Applicable</dt><dd>"+r.get("hipaa")+"</dd>"
			    	        	 + "<dt>HIPAA Waived</dt><dd>"+r.get("hipaawaived")+"</dd>"
			    	        	 + "<dt>Noncompliance Determination</dt><dd>"+(r.get("ncdetermination")|| "Not specified")+"</dd>"
			    	        	 + "<dt>Reportable to OHRP/etc</dt><dd>"+(r.get("ncreportable")|| "Not specified")+"</dd>"
			    	        	 + "<dt>UPIRTSO?</dt><dd>"+(r.get("UPIRTSO")|| "Not specified")+"</dd></dl></div>"
		    	        	 }

		    	        	 html += "<table class='motion-makers'><thead><tr><th>Made by</th><th>Seconded by</th></thead><tbody><tr>"
		    	        		  + "<td>"+r.get("madeby")+"</td>"
		    	        		  + "<td>"+r.get("secondedby")+"</td></table>";
		    	        	 
		    	        	 
		    	        	 return html+"</div>";
		    	          }}
				        ],
				listeners: {
							activate:function(){
								t.reloadMotionGridPanel();
							},
						    rowclick: function(grid, rowI, event)   {
								var record = grid.getStore().getAt(rowI);
								Clara.IRBMeeting.CurrentMotionRecord = record;
								Clara.IRBMeeting.MessageBus.fireEvent('motionchosen');  
						    },
						    rowdblclick: function(grid, rowI, event)   {
								var record = grid.getStore().getAt(rowI);
								clog(record);
								Clara.IRBMeeting.CurrentMotionRecord = record;
								Clara.IRBMeeting.MessageBus.fireEvent('motionchosen');
								if (canEditMeeting /* !(isChair && meeting.status == "SENT_TO_CHAIR") */ && !(isIrbOffice && meeting.status == "SENT_TO_TRANSCRIBER")) new Clara.IRBMeeting.VoteWindow({editing:true, motionrec:record}).show();
						    }
						}
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.IRBMeeting.MotionPanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('clarairbmotionpanel', Clara.IRBMeeting.MotionPanel);

Clara.IRBMeeting.ChangePersonStatusWindow = Ext.extend(Ext.Window, {
	id: 'clara-meeting-statuswindow',
	title:'Change status',
	layout:'form',
    width: 400,
    height: 205,
	border:false,
	padding:6,
	modal:true,
	userid:null,
	person:null,
	plain:true,
	constructor:function(config){		
		Clara.IRBMeeting.ChangePersonStatusWindow.superclass.constructor.call(this, config);
	},
	initComponent: function(){
		var t = this;
		t.person = meeting.getAttendingPerson(this.userid);
		var config = {
				buttons:[{
					xtype:'button',
					text:'Change',
					handler:function(){
						t.person.changeStatus(Ext.getCmp("fldStatus").getValue(),Ext.getCmp("fldNote").getValue());
						Clara.IRBMeeting.MessageBus.fireEvent('attendancechanged',this);
						t.close();
					}
				}],
				items:[{
	                xtype: 'displayfield',
	                value: this.person.fname+" "+this.person.lname,
	                fieldLabel: 'Person',
	                id:'fldPerson',
	                anchor: '100%'
	            },
	            {
	                xtype: 'combo',
	                fieldLabel: 'Status',
	                value:this.person.currentStatus().value,
	                typeAhead: true,
	                allowBlank:false,
                    triggerAction: 'all',
                    store: new Ext.data.SimpleStore({
                       fields:['statustext','value'],
                       data: [['Present','PRESENT'],['Out of room','OUT_OF_ROOM'],['Absent','ABSENT']]
                    }),
                    lazyRender: true,
                    displayField:'statustext',
                    valueField:'value',
                    mode:'local',
	                id:'fldStatus',
	                anchor: '100%'
	            },
	            {
	                xtype: 'textarea',
	                anchor: '100%',
	                id:'fldNote',
	                value:this.person.currentStatus().note,
	                fieldLabel: 'Note'
	            }]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.IRBMeeting.ChangePersonStatusWindow.superclass.initComponent.apply(this, arguments);
		
	}
});
Ext.reg('clarairbstatuswindow', Clara.IRBMeeting.ChangePersonStatusWindow);

Clara.IRBMeeting.AttendanceWindow = Ext.extend(Ext.Window, {
	id: 'clara-meeting-attendancewindow',
	title:'Attendance',
	layout:'fit',
    width: 500,
    height: 500,
	border:false,
	modal:true,
	plain:true,
	constructor:function(config){		
		Clara.IRBMeeting.AttendanceWindow.superclass.constructor.call(this, config);
		Clara.IRBMeeting.MessageBus.on('attendancechanged', this.onAttendanceChanged);
	},
	onAttendanceChanged:function(){
		Ext.getCmp("attendance-grid").reconfigure(meeting.getAttendanceStore(), Ext.getCmp("attendance-grid").getColumnModel());
	},
	setAllStatus: function(status){
		var person = {};
		var st = meeting.getAttendanceStore();
		st.each(function(rec){
			person = meeting.getAttendingPerson(rec.data.userid);
			person.changeStatus(status,'');
		});
		Clara.IRBMeeting.MessageBus.fireEvent('attendancechanged',this);
	},
	initComponent: function(){
		var t = this;
		var config = {
				tbar:{
					xtype:'toolbar',
					items:[{disabled:!canEditMeeting,xtype:'button',text:'Set all..',iconCls:'icn-status',menu:{
						items:[{text:'Present', iconCls:'icn-status', handler:function(){t.setAllStatus("PRESENT");}},
						       {text:'Out of Room', iconCls:'icn-status-away', handler:function(){t.setAllStatus("OUT_OF_ROOM");}},
						       {text:'Absent', iconCls:'icn-status-busy', handler:function(){t.setAllStatus("ABSENT");}}
						       ]
					}},'->',{
						xtype:'button',
						text:'Load roster',
						disabled: (!canEditMeeting || meeting.getStatus() != "NEW"),
						iconCls:'icn-database-export',
						handler:function(){
							Ext.Msg.show({
							     title:'Load roster?',
							     msg: 'This will clear any existing attendance information. Continue?',
							     buttons: Ext.Msg.YESNOCANCEL,
							     icon: Ext.Msg.QUESTION,
							     fn: function(btn){
							    	 if (btn == 'yes'){
							    		 meeting.clearAttendance();
											var lMask = new Ext.LoadMask(Ext.getBody(), {msg:"Loading roster..."});
											lMask.show();
											var rs = Clara.IRBMeeting.GetAgendaRosterStore();
											rs.load({callback:function(r,o,s){
												var t = this;
												t.filterBy(function(record){
													return (record.status != 'REMOVED');
												});
												t.sort('lname');
												
												var at = {};
												
												t.each(function(rec){
													if (rec.data.status == 'NORMAL' || rec.data.status == 'ADDITIONAL'){
														at = new Clara.IRBMeeting.Attendance({
															userid:rec.get("userid"),
															fname:rec.get("fname"),
															lname:rec.get("lname"),
															status:[new Clara.IRBMeeting.MemberStatus({
																timestamp: Clara.IRBMeeting.GetTime(),
																value:'ABSENT',
																note:''
															})]
														});
													} else if (rec.data.status == 'REPLACED'){
														at = new Clara.IRBMeeting.Attendance({
															userid:rec.get("altirbrevieweruserid"),
															fname:rec.get("altirbreviewerfname"),
															lname:rec.get("altirbreviewerlname"),
															status:[new Clara.IRBMeeting.MemberStatus({
																timestamp: Clara.IRBMeeting.GetTime(),
																value:'ABSENT',
																note:'Replacement for '+rec.data.fname+' '+rec.data.lname
															})]
														});
													}
													
													if (at != {}) meeting.attendance.push(at);
												});
												clog("Done populating agenda roster.");
												Clara.IRBMeeting.MessageBus.fireEvent('meetingchanged',this);
												lMask.hide();
												Ext.getCmp("clara-meeting-attendancewindow").close();
											}});
							    	 }
							     }
							});
							
						}
					},'-',
					       {disabled:!canEditMeeting,xtype:'button',text:(meeting.getQuorumStatus() == "QUORUM_MET")?'Quorum is MET':'Quorum is NOT MET',iconCls:'icn-users',enableToggle:true,
							pressed:(meeting.getQuorumStatus() == "QUORUM_MET"),
							listeners:{
					    	   toggle: function(t,v){
					    		   if (v){
					    			   t.setText('Quorum is MET');
					    			   Clara.IRBMeeting.MessageBus.fireEvent('quorummet', this);  
					    		   } else {
					    			   t.setText('Quorum is NOT MET');
					    			   Clara.IRBMeeting.MessageBus.fireEvent('quorumlost', this);  
					    		   }
					    		   
					    	   }
					       }}
					       ]
				},
				buttons:[{xtype:'button',text:'Close',handler:function(){ if (canEditMeeting) Clara.IRBMeeting.MessageBus.fireEvent('meetingchanged',this);t.close();}}],
				items:[{
						xtype:'grid',
						id:'attendance-grid',
						border:false,
						autoExpandColumn:'attendance-column-name',
						store:meeting.getAttendanceStore(),
						columns:[{xtype:'gridcolumn', dataIndex:'userid', hidden:true},
						         //{header: 'Agenda Roster', sortable: true, dataIndex: 'id',renderer:Clara.Agenda.RenderAgendaRosterRow}
						         {xtype:'gridcolumn', header:'Member',id:'attendance-column-name', dataIndex:'fname',renderer:function(v,p,r){
									var d=r.data;
									return "<span class='roster-status-row'>"+d.fname+" "+d.lname+"</span>";
						         }},
						         {xtype:'gridcolumn', width:250,header:'Current Status', dataIndex:'currentstatus', renderer:function(v,p,r){
										var d=r.data;
										var h = "<h1 class='attend-row-status'>"+d.currentstatus+"</h1>";
										if (d.currentnote != '') return h+"<h2 class='attend-row-note'>Note: <span>"+d.currentnote+"</span></h2>";
										else return h;
						         }},
								 {xtype:'gridcolumn', dataIndex:'isavailable', hidden:true}],
						listeners:{
							rowdblclick: function(grid, rowI, event)   {
								var record = grid.getStore().getAt(rowI);
								// Show status change window
								if (canEditMeeting) new Clara.IRBMeeting.ChangePersonStatusWindow({userid:record.data.userid}).show();
						    }
						}
					}]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.IRBMeeting.AttendanceWindow.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('clarairbattendancewindow', Clara.IRBMeeting.AttendanceWindow);



Clara.IRBMeeting.VoteWindow = Ext.extend(Ext.Window, {
	id: 'clara-meeting-votewindow',
	title:(this.editing)?"Edit Motion":"Make Motion",
	layout:'border',
    width: 500,
    height: 600,
	border:false,
	plain:true,
	editing:false,
	motion:{},
	motionrec:{},
	irbDeterminations:{},
	constructor:function(config){		
		Clara.IRBMeeting.VoteWindow.superclass.constructor.call(this, config);
		Clara.IRBMeeting.MessageBus.on('attendancechanged', this.onAttendanceChanged);
		Clara.IRBMeeting.MessageBus.on('votechanged', this.onVoteChanged);
	},
	onAttendanceChanged:function(){
		clog("attendance changed.", meeting.attendance);
		if (typeof Ext.getCmp("clara-meeting-votewindow") != 'undefined'){
			var mt = Ext.getCmp("clara-meeting-votewindow").motion;
			Ext.getCmp("votes-grid").reconfigure(mt.getVoteStore(meeting.attendance), Ext.getCmp("votes-grid").getColumnModel());
		}
	},
	onVoteChanged:function(){
		var mt = Ext.getCmp("clara-meeting-votewindow").motion;
		Ext.getCmp("votes-grid").reconfigure(mt.getVoteStore(meeting.attendance), Ext.getCmp("votes-grid").getColumnModel());
	},
	initComponent: function(){
		var t = this;
		t.motion = (t.editing)?meeting.getMotionFromTimestamp(t.motionrec.get("timestamp")):new Clara.IRBMeeting.Motion({timestamp:Clara.IRBMeeting.GetTime()});
		clog("motion",t.motion);
		clog("motionrec",t.motionrec);
		t.irbDeterminations = {};
		if (!t.editing && Clara.IRBMeeting.CurrentAgendaItemRecord.get("category")=="FULL_BOARD"){
			jQuery.ajax({
				  type: 'GET',
				  async:false,
				  url: appContext+"/ajax/protocols/"+Clara.IRBMeeting.CurrentAgendaItemRecord.get("protocolId")+"/metadata",
				  success: function(xml){
					  var irb = jQuery(xml).find("protocol").find("summary:first").find("irb-determination:first");					  
					  t.irbDeterminations.reviewPeriod = irb.find("review-period").text();
					  t.irbDeterminations.adultRisk = irb.find("adult-risk").text();
					  t.irbDeterminations.pediatricRisk = irb.find("ped-risk").text();
					  t.irbDeterminations.consentWaived = irb.find("consent-waived").text();
					  t.irbDeterminations.consentDocumentWaived = irb.find("consent-document-waived").text();
					  t.irbDeterminations.hipaaWaived = irb.find("hipaa-waived").text();
					  t.irbDeterminations.hipaaApplicable = irb.find("hipaa-applicable").text();
					  t.irbDeterminations.nextReviewType = irb.find("suggested-next-review-type").text();
					  clog("AJAX, DETERMINATIONS ",t.irbDeterminations);
				  },
				  error: function(){
					  alert("Error loading irb determination information. Cannot make motion at this time.");
				  },
				  dataType: 'xml'
			});
		}
		
		var config = {
				items:[
			            {
			                xtype: 'panel',
			                id:'vote-form',
			                region: 'west',
			                layout: 'form',
			                split:true,
			                width: 330,
			                title:'Motion details',
			                padding: 6,
			                labelWidth: 140,
			                items: [
			                    {
			                        xtype: 'combo',
			                        id:'vote-form-motion',
			                        fieldLabel: 'Motion',
			                        typeAhead: true,
			                        allowBlank:false,
		                            triggerAction: 'all',
		                            value:(t.editing)?t.motionrec.get("motion"):null,
		                            store: new Ext.data.SimpleStore({
		                               fields:['motion'],
		                               data: [['Approve'],['Defer with minor contingencies'],['Defer with major contingencies'],['Decline'],['Table'],['Acknowledge'],['Administratively Remove'],['Suspended for Cause'],['Terminated for Cause']]
		                            }),
		                            lazyRender: true,
		                            displayField:'motion',
		                            mode:'local',
		                            selectOnFocus:true,
		                            listeners:{
					            		change:function(f,v,ov){
					            		}
					            	},
			                        anchor: '100%'
			                    },
			                    {
			                        xtype: 'combo',
			                        fieldLabel: 'Made By',
			                        id:'vote-form-madeby',
			                        anchor: '100%',
					    	    	   	typeAhead:false,
							        	forceSelection:true,
							        	displayField:'username', 
							        	valueField:'userid',
							        	value:(t.editing)?t.motionrec.get("madebyid"):null,
							        	mode:'local', 
							        	triggerAction:'all',
							        	editable:false,
							        	allowBlank:false,
							        	tpl: '<tpl for=".">'
							                + '<div class="x-combo-list-item"><div><b>{fname} {lname}</b></div></div>'
							                + '</tpl>',
										store:Clara.IRBMeeting.IRBReviewerStore
									
			                    },
			                    {
			                        xtype: 'combo',
			                        fieldLabel: 'Seconded By',
			                        id:'vote-form-secondedby',
			                        anchor: '100%',
					    	    	   	typeAhead:false,
							        	forceSelection:true,
							        	displayField:'username', 
							        	valueField:'userid',
							        	value:(t.editing)?t.motionrec.get("secondedbyid"):null,
							        	mode:'local', 
							        	triggerAction:'all',
							        	editable:false,
							        	allowBlank:false,
							        	tpl: '<tpl for=".">'
							                + '<div class="x-combo-list-item"><div><b>{fname} {lname}</b></div></div>'
							                + '</tpl>',
										store:Clara.IRBMeeting.IRBReviewerStore
									
			                    },
			                    
			                   {
			                        xtype: 'combo',
			                        fieldLabel: 'Adult Risk',
			                        id:'vote-form-adult-risk',
			                        typeAhead: true,
			                        value:(t.editing)?t.motionrec.get("adultrisk"):(typeof t.irbDeterminations.adultRisk != "undefined" && t.irbDeterminations.adultRisk != "")?t.irbDeterminations.adultRisk:null,
			                        allowBlank:false,
		                            triggerAction: 'all',
		                            store: new Ext.data.SimpleStore({
		                               fields:['risk','id'],
		                               data: [['Minimal','RISK_ADULT_1'],['Greater than minimal','RISK_ADULT_2'],['N/A','RISK_ADULT_NA']]
		                            }),
		                            lazyRender: true,
		                            displayField:'risk',
		                            valueField:'id',
		                            mode:'local',
		                            selectOnFocus:true,
			                        anchor: '100%'
			                    },
			                     
			                    {
			                        xtype: 'combo',
			                        fieldLabel: 'Pediatric Risk',
			                        id:'vote-form-ped-risk',
			                        value:(t.editing)?t.motionrec.get("pediatricrisk"):(typeof t.irbDeterminations.pediatricRisk != "undefined" && t.irbDeterminations.pediatricRisk != "")?t.irbDeterminations.pediatricRisk:null,
			                        typeAhead: true,
			                        allowBlank:false,
		                            triggerAction: 'all',
		                            store: new Ext.data.SimpleStore({
		                               fields:['risk','id'],
		                               data: [['1','RISK_PED_1'],['2','RISK_PED_2'],['3','RISK_PED_3'],['4','RISK_PED_4'],['N/A','RISK_PED_NA']]
		                            }),
		                            lazyRender: true,
		                            displayField:'risk',
		                            valueField:'id',
		                            mode:'local',
		                            selectOnFocus:true,
			                        anchor: '100%'
			                    },
			                    {
			                        xtype: 'combo',
			                        fieldLabel: 'Next Review Type',
			                        id:'vote-form-reviewtype',
			                        // disabled:(Clara.IRBMeeting.CurrentAgendaItemRecord.get("protocolFormTypeId") != "continuing-review"),
			                        typeAhead: true,
			                        value:(t.editing)?t.motionrec.get("reviewtype"):(typeof t.irbDeterminations.nextReviewType != "undefined" && t.irbDeterminations.nextReviewType != "")?t.irbDeterminations.nextReviewType:"",
			                        allowBlank:true,
		                            triggerAction: 'all',
		                            store: new Ext.data.SimpleStore({
		                               fields:['type','id'],
		                               data: [['Full Board','FULL_BOARD'],['Expedited','EXPEDITED'],['Exempt','EXEMPT']]
		                            }),
		                            lazyRender: true,
		                            displayField:'type',
		                            valueField:'id',
		                            mode:'local',
		                            selectOnFocus:true,
			                        anchor: '100%'
			                    },
			                    {
			                        xtype: 'combo',
			                        fieldLabel: 'Review Period (months)',
			                        id:'vote-form-reviewperiod',
			                        typeAhead: true,
			                        value:(t.editing)?t.motionrec.get("reviewperiod"):(typeof t.irbDeterminations.reviewPeriod != "undefined" && t.irbDeterminations.reviewPeriod != "")?parseInt(t.irbDeterminations.reviewPeriod):null,
			                        allowBlank:false,
		                            triggerAction: 'all',
		                            store: new Ext.data.SimpleStore({
		                               fields:['period'],
		                               data: [[3],[6],[9],[12]]
		                            }),
		                            lazyRender: true,
		                            displayField:'period',
		                            mode:'local',
		                            selectOnFocus:true,
			                        anchor: '100%',
			                        listeners:{
			                        	change:function(t,v){
			                        		if (!isNumber(v) || parseInt(v) > 12){
			                        			t.setValue(12);
			                        		}
			                        	}
			                        }
			                    },
			                    {
			                        xtype: 'combo',
			                        fieldLabel: 'Consent Waived',
			                        id:'vote-form-consentwaived',
			                        value:(t.editing)?t.motionrec.get("consentwaived"):(typeof t.irbDeterminations.consentWaived != "undefined" && t.irbDeterminations.consentWaived != "")?t.irbDeterminations.consentWaived:null,
			                        typeAhead: true,
			                        allowBlank:false,
		                            triggerAction: 'all',
		                            store: new Ext.data.SimpleStore({
		                               fields:['d','id'],
		                               data: [['N/A','na'],['Yes','yes'],['No','no']]
		                            }),
		                            lazyRender: true,
		                            displayField:'d',
		                            valueField:'id',
		                            mode:'local',
		                            selectOnFocus:true,
			                        anchor: '100%'
			                    },{
			                        xtype: 'combo',
			                        fieldLabel: 'Consent Documentation Waived',
			                        id:'vote-form-consentdocumentationwaived',
			                        value:(t.editing)?t.motionrec.get("consentdocumentationwaived"):(typeof t.irbDeterminations.consentDocumentWaived != "undefined" && t.irbDeterminations.consentDocumentWaived != "")?t.irbDeterminations.consentDocumentWaived:null,
			                        typeAhead: true,
			                        allowBlank:false,
		                            triggerAction: 'all',
		                            store: new Ext.data.SimpleStore({
		                               fields:['d','id'],
		                               data: [['N/A','na'],['Yes','yes'],['No','no']]
		                            }),
		                            lazyRender: true,
		                            displayField:'d',
		                            valueField:'id',
		                            mode:'local',
		                            selectOnFocus:true,
			                        anchor: '100%'
			                    },{
			                        xtype: 'combo',
			                        fieldLabel: 'HIPAA Applicable',
			                        id:'vote-form-hipaa',
			                        value:(t.editing)?t.motionrec.get("hipaa"):(typeof t.irbDeterminations.hipaaApplicable != "undefined" && t.irbDeterminations.hipaaApplicable != "")?t.irbDeterminations.hipaaApplicable:null,
			                        typeAhead: true,
			                        allowBlank:false,
		                            triggerAction: 'all',
		                            store: new Ext.data.SimpleStore({
		                               fields:['d','id'],
		                               data: [['Yes','yes'],['No','no']]
		                            }),
		                            lazyRender: true,
		                            displayField:'d',
		                            valueField:'id',
		                            mode:'local',
		                            selectOnFocus:true,
			                        anchor: '100%'
			                    },{
			                        xtype: 'combo',
			                        fieldLabel: 'HIPAA Authorization Waived',
			                        id:'vote-form-hipaawaived',
			                        value:(t.editing)?t.motionrec.get("hipaawaived"):(typeof t.irbDeterminations.hipaaWaived != "undefined" && t.irbDeterminations.hipaaWaived != "")?t.irbDeterminations.hipaaWaived:null,
			                        typeAhead: true,
			                        allowBlank:false,
		                            triggerAction: 'all',
		                            store: new Ext.data.SimpleStore({
		                               fields:['d','id'],
		                               data: [['Yes','yes'],['No','no']]
		                            }),
		                            lazyRender: true,
		                            displayField:'d',
		                            valueField:'id',
		                            mode:'local',
		                            selectOnFocus:true,
			                        anchor: '100%'
			                    },
			                    {
			                        xtype:'fieldset',
			                        title: 'Non-Compliance Assessment',
			                        iconCls:'icn-auction-hammer--exclamation',
			                        collapsible: false,
			                        autoHeight:true,
			                        defaultType: 'combo',
			                        items :[{
			                                fieldLabel: 'Determination',
			                                id:'vote-form-ncdetermination',
					                        value:(t.editing)?t.motionrec.get("ncdetermination"):'NA',
					                        typeAhead: true,
					                        allowBlank:false,
				                            triggerAction: 'all',
				                            store: new Ext.data.SimpleStore({
				                               fields:['d','id'],
				                               data: [['N/A','na'],['No Evidence of Non-Compliance','no'],['Minor Non-Compliance','yes'],['Continuing Non-Compliance','yes_continuing'],['Serious Non-Compliance','yes_serious'],['Serious and Continuing Non-Compliance','yes_serious_continuing']]
				                            }),
				                            lazyRender: true,
				                            displayField:'d',
				                            valueField:'id',
				                            mode:'local',
				                            selectOnFocus:true,
					                        anchor: '100%'
			                            }, {
			                                fieldLabel: 'Reportable to OHRP/etc.',
			                                id:'vote-form-ncreportable',
					                        value:(t.editing)?t.motionrec.get("ncreportable"):'NA',
					                        typeAhead: true,
					                        allowBlank:false,
				                            triggerAction: 'all',
				                            store: new Ext.data.SimpleStore({
				                               fields:['d','id'],
				                               data: [['N/A','na'],['Yes','yes'],['No','no']]
				                            }),
				                            lazyRender: true,
				                            displayField:'d',
				                            valueField:'id',
				                            mode:'local',
				                            selectOnFocus:true,
					                        anchor: '100%'
			                            },
			                            {
			                                fieldLabel: 'UPIRTSO?',
			                                id:'vote-form-UPIRTSO',
					                        value:(t.editing)?t.motionrec.get("UPIRTSO"):'NA',
					                        typeAhead: true,
					                        allowBlank:false,
				                            triggerAction: 'all',
				                            store: new Ext.data.SimpleStore({
				                               fields:['d','id'],
				                               data: [['N/A','na'],['Yes','yes'],['No','no']]
				                            }),
				                            lazyRender: true,
				                            displayField:'d',
				                            valueField:'id',
				                            mode:'local',
				                            selectOnFocus:true,
					                        anchor: '100%'
			                            }
			                        ]
			                    }
			                ]
			            },
			            {
			                xtype: 'editorgrid',
			                id:'votes-grid',
			                title: 'Votes',
			                region: 'center',
			                autoExpandColumn:'vote-row-note',
			                store: t.motion.getVoteStore(meeting.attendance),
			                viewConfig: {
			                    getRowClass: function(record, index) {
			                        var c = record.get('value');
			                        if (c == Clara.IRBMeeting.Types.Vote.NOT_VOTING) {
			                            return 'vote-row-not-voting';
			                        } else {
			                            return 'vote-row-voting';
			                        }
			                    }
			                },
			                columns: [
			                    {
			                        xtype: 'gridcolumn',
			                        header: 'Name',
			                        dataIndex:'name',
			                        sortable: true,
			                        width: 200
			                    },
			                    {
			                        xtype: 'combocolumn',
			                        header: 'Vote',
			                        sortable: true,
			                        editable:canEditMeeting,
			                        dataIndex:'value',
			                        width: 100,
			                        editor: {
			                            xtype: 'combo',
				                        typeAhead: true,
			                            triggerAction: 'all',
			                            store: new Ext.data.SimpleStore({
			                               fields:['description','vote'],
			                               data: [['Yes',Clara.IRBMeeting.Types.Vote.YES],['No',Clara.IRBMeeting.Types.Vote.NO],['Abstain',Clara.IRBMeeting.Types.Vote.ABSTAIN],['Not Voting',Clara.IRBMeeting.Types.Vote.NOT_VOTING]]
			                            }),
			                            lazyRender: true,
			                            displayField:'description',
			                            valueField:'vote',
			                            mode:'local',
			                            selectOnFocus:true,
				                        anchor: '100%'
			                        }
			                    },
			                    {
			                        xtype: 'gridcolumn',
			                        dataIndex: 'string',
			                        id:'vote-row-note',
			                        header: 'Note',
			                        sortable: true,
			                        dataIndex:'note',
			                        editor: {
			                            xtype: 'textfield'
			                        }
			                    }
			                ],
			                tbar: {
			                    xtype: 'toolbar',
			                    items: [
			                        {
			                            xtype: 'button',
			                            text: 'Mark all..',
			                            disabled:!canEditMeeting,
			                            menu: {
			                                xtype: 'menu',
			                                items: [
			                                    {
			                                        xtype: 'menuitem',
			                                        text: 'Yes',
			                                        iconCls:'icon-review-met',
			                                        handler:function(){
			                                    		t.motion.setAllVotes(Clara.IRBMeeting.Types.Vote.YES);
			                                    	}
			                                    },
			                                    {
			                                        xtype: 'menuitem',
			                                        text: 'No',
			                                        iconCls:'icon-review-not-met',
			                                        handler:function(){
			                                    	t.motion.setAllVotes(Clara.IRBMeeting.Types.Vote.NO);
		                                    	}
			                                    },
			                                    {
			                                        xtype: 'menuitem',
			                                        text: 'Abstain',
			                                        iconCls:'icon-review-done',
			                                        handler:function(){
			                                    	t.motion.setAllVotes(Clara.IRBMeeting.Types.Vote.ABSTAIN);
		                                    	}
			                                    },
			                                    {
			                                        xtype: 'menuitem',
			                                        text: 'Not Voting',
			                                        iconCls:'icon-review-done',
			                                        handler:function(){
			                                    	t.motion.setAllVotes(Clara.IRBMeeting.Types.Vote.NOT_VOTING);
		                                    	}
			                                    }
			                                ]
			                            }
			                        },
			                        {
			                            xtype: 'tbfill'
			                        },
			                        {
			                            xtype: 'button',
			                            iconCls:'icn-users',
			                            text: 'Attendance..',
			                            hidden:!canEditMeeting,
			                            handler:function(){
			                        		new Clara.IRBMeeting.AttendanceWindow().show();
			                        	}
			                        }
			                    ]
			                }
			            }
			        ],
				modal:true,
				width:720,
				height:550,
				buttons:[{xtype:'button',text:'Save',handler:function(){
					// Sanity checks first..
					if (canEditMeeting && Ext.getCmp("vote-form-motion").isValid() && Ext.getCmp("vote-form-adult-risk").isValid() && Ext.getCmp("vote-form-ped-risk").isValid() && Ext.getCmp("vote-form-madeby").isValid() && Ext.getCmp("vote-form-secondedby").isValid()  && (Ext.getCmp("vote-form-consentwaived").isValid() || Ext.getCmp("vote-form-consentdocumentationwaived").isValid()) && Ext.getCmp("vote-form-hipaawaived").isValid() && Ext.getCmp("vote-form-hipaa").isValid() ){
						clog("Valid form. Checking for votes..");
						t.motion.setVotesFromStore(Ext.getCmp("votes-grid").getStore());
						if (t.motion.votes.length > 0) {
							clog("Saving..");
							
							t.motion.value = Ext.getCmp("vote-form-motion").getValue();
							t.motion.reviewtype = Ext.getCmp("vote-form-reviewtype").getValue();
							t.motion.reviewperiod = Ext.getCmp("vote-form-reviewperiod").getValue();
							t.motion.madebyid = Ext.getCmp("vote-form-madeby").getValue();
							t.motion.madebyname = Ext.getCmp("vote-form-madeby").getRawValue();
							t.motion.secondbyid = Ext.getCmp("vote-form-secondedby").getValue();
							t.motion.secondbyname = Ext.getCmp("vote-form-secondedby").getRawValue();
							t.motion.adultrisk = Ext.getCmp("vote-form-adult-risk").getValue();
							t.motion.pediatricrisk = Ext.getCmp("vote-form-ped-risk").getValue();
							t.motion.consentwaived = Ext.getCmp("vote-form-consentwaived").getValue();
							t.motion.consentdocumentationwaived = Ext.getCmp("vote-form-consentdocumentationwaived").getValue();
							t.motion.hipaawaived = Ext.getCmp("vote-form-hipaawaived").getValue();
							t.motion.hipaa = Ext.getCmp("vote-form-hipaa").getValue();
							t.motion.ncdetermination = Ext.getCmp("vote-form-ncdetermination").getValue();
							t.motion.ncreportable = Ext.getCmp("vote-form-ncreportable").getValue();
							t.motion.UPIRTSO = Ext.getCmp("vote-form-UPIRTSO").getValue();
							var ai = Clara.IRBMeeting.CurrentAgendaItemRecord.data;
							var act = meeting.getActivityForItem(ai.id);
							if (act != null){
								if (t.editing){
									act.replaceMotion(t.motion);
								} else {
									act.motions.push(t.motion);
								}
								Clara.IRBMeeting.MessageBus.fireEvent('motionadded');
								t.close();
							} else {
								clog("Error finding action for agenda item record:");
								clog(ai);
							}
						} else {
							alert("Please enter votes before saving.");
						}
					} else {
						alert("Check motion before saving.");
					}
				}}]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.IRBMeeting.VoteWindow.superclass.initComponent.apply(this, arguments);

	}
});
Ext.reg('clarairbvotewindow', Clara.IRBMeeting.VoteWindow);