Ext.ns('Clara.IRBMeeting');

//Set up HTML encoder/decoder for XML
Encoder.EncodeType = "entity";

Clara.IRBMeeting.ServerNotifyInProgress = false;
Clara.IRBMeeting.SaveInProgress = false;

Clara.IRBMeeting.MessageBus.addListener('beforemeetingsave', function(){
	clog("MESSAGEBUS: beforemeetingsave");
	Clara.IRBMeeting.SaveInProgress = true;
	Ext.getBody().mask('Saving, please wait...', 'x-mask-loading');
});
Clara.IRBMeeting.MessageBus.addListener('aftermeetingsave', function(){
	clog("MESSAGEBUS: aftermeetingsave");
	Clara.IRBMeeting.SaveInProgress = false;
	if (!Clara.IRBMeeting.ServerNotifyInProgress) Ext.getBody().unmask();
});
Clara.IRBMeeting.MessageBus.addListener('error', function(){
	clog("MESSAGEBUS: error");
	Ext.getBody().unmask();
});

Clara.IRBMeeting.MessageBus.addListener('beforenotifyserver', function(){
	clog("MESSAGEBUS: beforenotifyserver");
	Clara.IRBMeeting.ServerNotifyInProgress = true;
	Ext.getBody().mask('Changing meeting status, please wait...', 'x-mask-loading');
});
Clara.IRBMeeting.MessageBus.addListener('afternotifyserver', function(){
	clog("MESSAGEBUS: afternotifyserver");
	Clara.IRBMeeting.ServerNotifyInProgress = false;
	if (!Clara.IRBMeeting.SaveInProgress) Ext.getBody().unmask();
});


Clara.IRBMeeting.MessageBus.on('quorummet', function(){
	clog("Got 'quroummet' message");
	Clara.IRBMeeting.quorumMet = true;
	meeting.activity.push(new Clara.IRBMeeting.Activity({
		   type:'QUORUM_MET',
		   timestamp:Clara.IRBMeeting.GetTime()
	   }));

	   Clara.IRBMeeting.MessageBus.fireEvent('meetingchanged',this);
});

Clara.IRBMeeting.MessageBus.on('quorumlost', function(){
	clog("Got 'quroumlost' message");
	Clara.IRBMeeting.quorumMet = false;
	meeting.activity.push(new Clara.IRBMeeting.Activity({
		   type:'QUORUM_NOT_MET',
		   timestamp:Clara.IRBMeeting.GetTime()
	   }));

	   Clara.IRBMeeting.MessageBus.fireEvent('meetingchanged',this);
});

Clara.IRBMeeting.MessageBus.on('aftermeetingload', function(){ 
	if (meeting.status == "COMPLETE") canEditMeeting = false;
	
	clog("Meeting loaded. Check for existing in-progress meeting.."); 
	Clara.IRBMeeting.quorumMet = (meeting.getQuorumStatus() == "QUORUM_MET");
	clog("Clara.IRBMeeting.quorumMet",meeting.getQuorumStatus(),Clara.IRBMeeting.quorumMet);
	
}, this);


Clara.IRBMeeting.MessageBus.on('motionadded', function(){ Clara.IRBMeeting.MessageBus.fireEvent('meetingchanged'); }, this);
Clara.IRBMeeting.MessageBus.on('motionsupdated', function(){ Clara.IRBMeeting.MessageBus.fireEvent('meetingchanged'); }, this);
Clara.IRBMeeting.MessageBus.on('meetingchanged', function(){ clog("Meeting change event. Saving to XML.."); meeting.save(); 
	if (Clara.IRBMeeting.allLettersSent()) {
		Ext.getCmp("btn-meeting-chair-send-transcriber").setDisabled(false);
	} else {
		Ext.getCmp("btn-meeting-chair-send-transcriber").setDisabled(true);
	}
}, this);

Clara.IRBMeeting.MessageBus.on('agendaitemlettersent', function(agendaItemRec){ 
	clog("agendaitemlettersent event.",agendaItemRec);
	jQuery(".meeting-agenda-item-actions").html('');	// remove "create letter" link
	Ext.getCmp("agendaItemPanel").getView().refresh();	// add mail icon to agenda item row on left panel
	
	Clara.IRBMeeting.MessageBus.fireEvent('meetingchanged');	// to save the meeting XML
}, this);

Clara.IRBMeeting.MessageBus.on('startmeeting', function(updateUIOnly){ 
	clog("Start meeting event. Only update UI?",updateUIOnly);
	if (!updateUIOnly){
		meeting.status="IN_PROGRESS";
		if (meeting.starttime == '') meeting.starttime = new Date().toString();
		Clara.IRBMeeting.MessageBus.fireEvent('meetingchanged');
		Clara.IRBMeeting.NotifyServer('meeting-start');
	}
	Ext.TaskMgr.start(Clara.IRBMeeting.ClockTask);
    
    Ext.getCmp("btn-meeting-start").setDisabled(true);
	if (Ext.getCmp("agendaItemPanel")) {
		Ext.getCmp("agendaItemPanel").setDisabled(false);
		Ext.getCmp("agendaItemPanel").expand();
	}
	Ext.getCmp("btn-meeting-send-chair").setVisible(false);
	if (Ext.getCmp("btn-meeting-chair-send-letter")) Ext.getCmp("btn-meeting-chair-send-letter").setVisible(false);
	Ext.getCmp("btn-meeting-chair-send-transcriber").setVisible(false);
	if (canEditMeeting && isIrbOffice) Ext.getCmp("btn-attendance").setDisabled(false);
	if (canEditMeeting && isIrbOffice) Ext.getCmp("btn-meeting-stop").setDisabled(false);
	if (!canEditMeeting) Ext.getCmp("btn-meeting-start").setDisabled(true);
    
}, this);

Clara.IRBMeeting.MessageBus.on('stopmeeting', function(updateUIOnly){ 
	clog("Stop meeting event. Only update UI?",updateUIOnly);
	if (!updateUIOnly){
		meeting.status="STOPPED";
		meeting.endtime = new Date().toString();
		Clara.IRBMeeting.MessageBus.fireEvent('meetingchanged');
		Clara.IRBMeeting.NotifyServer('meeting-stop');
	}
	Ext.TaskMgr.stopAll();//Clara.IRBMeeting.ClockTask);

	
    if (!isChair) Ext.getCmp("btn-meeting-start").setDisabled(false);
    else  Ext.getCmp("btn-meeting-start").setDisabled(true);
	if (Ext.getCmp("agendaItemPanel")) {
		Ext.getCmp("agendaItemPanel").setDisabled(false);
		Ext.getCmp("agendaItemPanel").expand();
	}
	if (canEditMeeting && isIrbOffice) Ext.getCmp("btn-meeting-send-chair").setVisible(true);
	if (Ext.getCmp("btn-meeting-chair-send-letter")) Ext.getCmp("btn-meeting-chair-send-letter").setVisible(false);
	Ext.getCmp("btn-meeting-chair-send-transcriber").setVisible(false);
	Ext.getCmp("btn-attendance").setDisabled(true);
	Ext.getCmp("btn-meeting-stop").setDisabled(true);
	var dt = new Date(meeting.endtime).toDateString();
	var stm = new Date(meeting.starttime).format("g:ia");
	var etm = new Date(meeting.endtime).format("g:ia");
	jQuery("#meeting-time").removeClass("meeting-not-started").removeClass("meeting-in-progress").addClass("meeting-ended");
	jQuery("#meeting-time").text("Meeting over. "+dt+" from "+stm+" to "+etm+".");
}, this);


Clara.IRBMeeting.MessageBus.on('senttochair', function(updateUIOnly){ 
	clog("Meeting senttochair event. Only update UI?",updateUIOnly);
	if (!updateUIOnly){
		meeting.status="SENT_TO_CHAIR";
		Clara.IRBMeeting.MessageBus.fireEvent('meetingchanged');
		Clara.IRBMeeting.NotifyServer('meeting-complete', {actor:'IRB_OFFICE'}, true);
		//location.reload();	// to fix "canedit" ui stuff.. should fix w/o reload later
	}

	// disable things chair cannot use
	Ext.getCmp("btn-meeting-start").setDisabled(true);
	Ext.getCmp("btn-meeting-send-chair").setVisible(false);
	Ext.getCmp("btn-attendance").setDisabled(true);
	Ext.getCmp("btn-meeting-stop").setDisabled(true);
	
	
	//enable things chair can use
	Ext.getCmp("btnMakeMotion").setDisabled(!isChair);
	Ext.getCmp("btnRemoveMotion").setDisabled(!isChair);
	
	if (Ext.getCmp("agendaItemPanel")) {
		Ext.getCmp("agendaItemPanel").setDisabled(false);
		Ext.getCmp("agendaItemPanel").expand();
	}
	if (Ext.getCmp("btn-meeting-chair-send-letter")) Ext.getCmp("btn-meeting-chair-send-letter").setVisible(isChair);
	Ext.getCmp("btn-meeting-chair-send-transcriber").setDisabled(!isChair);
	Ext.getCmp("btn-meeting-chair-send-transcriber").setVisible(isChair);


	// update bar info
	var dt = new Date(meeting.endtime).toDateString();
	var stm = new Date(meeting.starttime).format("g:ia");
	var etm = new Date(meeting.endtime).format("g:ia");
	jQuery("#meeting-time").removeClass("meeting-not-started").removeClass("meeting-in-progress").addClass("meeting-ended");
	jQuery("#meeting-time").text("Meeting over (and sent to chair). "+dt+" from "+stm+" to "+etm+".");
	
}, this);

Clara.IRBMeeting.MessageBus.on('senttotranscriber', function(updateUIOnly){ 
	clog("Meeting senttotranscriber event. Only update UI?",updateUIOnly);
	if (!updateUIOnly){
		meeting.status="SENT_TO_TRANSCRIBER";
		Clara.IRBMeeting.MessageBus.fireEvent('meetingchanged');
		Clara.IRBMeeting.NotifyServer('meeting-complete', {actor:'IRB_CHAIR'},true);
		//location.reload();	// to fix "canedit" ui stuff.. should fix w/o reload later
	}

	Ext.getCmp("btnAddComment").setDisabled(true);
	Ext.getCmp("btnRemoveComment").setDisabled(true);
	
	Ext.getCmp("btnMakeMotion").setDisabled(true);
	Ext.getCmp("btnRemoveMotion").setDisabled(true);
	
	Ext.getCmp("btn-meeting-start").setDisabled(true);
	Ext.getCmp("btn-meeting-send-chair").setVisible(false);
	Ext.getCmp("btn-meeting-chair-send-transcriber").setVisible(false);
	if (Ext.getCmp("agendaItemPanel")) {
		Ext.getCmp("agendaItemPanel").setDisabled(false);
		Ext.getCmp("agendaItemPanel").expand();
	}
	if (Ext.getCmp("btn-meeting-chair-send-letter")) Ext.getCmp("btn-meeting-chair-send-letter").setVisible(isChair);
	
	Ext.getCmp("btn-meeting-complete").setDisabled(!isIrbOffice);
	Ext.getCmp("btn-meeting-complete").setVisible(isIrbOffice);

	Ext.getCmp("btn-attendance").setDisabled(true);
	Ext.getCmp("btn-meeting-stop").setDisabled(true);
	var dt = new Date(meeting.endtime).toDateString();
	var stm = new Date(meeting.starttime).format("g:ia");
	var etm = new Date(meeting.endtime).format("g:ia");
	jQuery("#meeting-time").removeClass("meeting-not-started").removeClass("meeting-in-progress").addClass("meeting-ended");
	jQuery("#meeting-time").text("Meeting over (and sent to IRB office from chair). "+dt+" from "+stm+" to "+etm+".");
	
}, this);

Clara.IRBMeeting.MessageBus.on('completemeeting', function(updateUIOnly){ 
	clog("Meeting completemeeting event. Only update UI?",updateUIOnly);
	if (!updateUIOnly){
		meeting.status="COMPLETE";
		Clara.IRBMeeting.MessageBus.fireEvent('meetingchanged');
		Clara.IRBMeeting.NotifyServer('meeting-close', {actor:'IRB_OFFICE'},true);
		//location.reload();	// to fix "canedit" ui stuff.. should fix w/o reload later
	}
	
	Ext.getCmp("btn-meeting-start").setDisabled(true);
	Ext.getCmp("btn-meeting-send-chair").setVisible(false);
	Ext.getCmp("btn-meeting-chair-send-transcriber").setVisible(false);
	Ext.getCmp("btn-meeting-complete").setVisible(false);
	if (Ext.getCmp("agendaItemPanel")) {
		Ext.getCmp("agendaItemPanel").setDisabled(false);
		Ext.getCmp("agendaItemPanel").expand();
	}
	if (Ext.getCmp("btn-meeting-chair-send-letter")) Ext.getCmp("btn-meeting-chair-send-letter").setVisible(false);

	Ext.getCmp("btn-attendance").setDisabled(true);
	Ext.getCmp("btn-meeting-stop").setDisabled(true);
	var dt = new Date(meeting.endtime).toDateString();
	var stm = new Date(meeting.starttime).format("g:ia");
	var etm = new Date(meeting.endtime).format("g:ia");
	jQuery("#meeting-time").removeClass("meeting-not-started").removeClass("meeting-in-progress").addClass("meeting-ended");
	jQuery("#meeting-time").text("Meeting completed. "+dt+" from "+stm+" to "+etm+".");
	
}, this);

Clara.IRBMeeting.allLettersSent = function(){
	// Check actionable items.. if all of them have letters sent, then endable the "send to IRB" button
	var lettercount = 0,
	actionableitemcount = 0;
	
	for (var i=0, l=meeting.activity.length;i<l;i++){
		var item = meeting.activity[i];
		
		
		if (item.agendaitemid > 0 && item.type !=="MINUTES"){
			actionableitemcount++;
			if (item.lettersent == true) lettercount++;
		}
	}
	
	clog("actionableitemcount: "+actionableitemcount+" lettercount: "+lettercount)
	var allLettersSent = (lettercount !== 0 && lettercount == actionableitemcount);
	return allLettersSent;
	
}

Clara.IRBMeeting.OpenCurrentItemLetterWindow= function(committee){
	committee = committee || "IRB_OFFICE"; // "ROLE_IRB_CHAIR";
	var ai = Clara.IRBMeeting.CurrentAgendaItemRecord.data;
		// Generate a currentFormReviewSession var
	var currentFormReviewSession = {
			agendaId:Clara.IRBMeeting.AgendaId,
			agendaItemId:Clara.IRBMeeting.CurrentAgendaItemRecord.get('id'),
			userId:	claraInstance.user.id?claraInstance.user.id:null
			
	};

	if (meeting.hasMotions(Clara.IRBMeeting.CurrentAgendaItemRecord.get('id'))){
			var message = new Clara.Mail.MessageWindow({
				sendFunction:Clara.Mail.SignAndSubmit,
				onSuccess:function(){
					meeting.setAgendaItemLetterSent(Clara.IRBMeeting.CurrentAgendaItemRecord.get('id'),true);
					Clara.IRBMeeting.MessageBus.fireEvent("agendaitemlettersent",Clara.IRBMeeting.CurrentAgendaItemRecord);
				},
				templateUrl:appContext+'/ajax/agendas/'+Clara.IRBMeeting.AgendaId+'/agenda-items/'+Clara.IRBMeeting.CurrentAgendaItemRecord.get('id')+'/create-letter',
				sendUrl:appContext+'/ajax/agendas/'+Clara.IRBMeeting.AgendaId+'/agenda-items/'+Clara.IRBMeeting.CurrentAgendaItemRecord.get('id')+'/send-letter',
				delayedSend:false,
				requireSignature:true,
				metadata:currentFormReviewSession,
				title:'Send Letter',
				modal:true,
				iconCls:'icn-mail--pencil'
			});
			message.show();	
		
	} else {
		alert("This agenda item has no motions.");
	}

};

Clara.IRBMeeting.MessageBus.on('agendaitemchosen',function(){
	var d = Clara.IRBMeeting.CurrentAgendaItemRecord.data;
	clog("AGENDA DATA",d);
	var htmlLetter = "";
	if ((meeting.getStatus() == "SENT_TO_CHAIR") && (Clara.IRBMeeting.CurrentAgendaItemRecord.get("category") == "FULL_BOARD") && claraInstance.HasAnyPermissions(['ROLE_IRB_CHAIR','ROLE_SYSTEM_ADMIN']) && (meeting.hasLetterBeenSent(Clara.IRBMeeting.CurrentAgendaItemRecord) == false)){
		htmlLetter += "<div class='meeting-agenda-item-actions'><a href='javascript:;' onClick='Clara.IRBMeeting.OpenCurrentItemLetterWindow();'>Create Letter..</a></div>";
	}
	
	var s = d.pi;
	var pi = "<div class='agenda-list-row-pi' style='text-align:left;float:left;'><ul class='form-assigned-reviewers'>";
	if (s && s.length > 0) pi += "<li class='form-assigned-pi' style='font-size:14px;margin-top: 2px;padding-top: 2px;margin-right:12px;'>PI: <span style='color:black;font-weight:800;'>"+s[0].get("firstname")+" "+s[0].get("lastname")+"</span></li>";
	pi+="</ul></div>";
	var html = "";
	if (d.category == "MINUTES") html = "<div class='meeting-agenda-item-status'><div class='meeting-agenda-item-title'><span class='meeting-agenda-item-irbnumber'>Previous Meeting's Minutes</span></div><div class='meeting-agenda-item-type'><a href='"+appContext+(d.minutesurl?d.minutesurl:"")+"' target='_blank'>Open in new window.</a></div>"+htmlLetter+"<div style='clear:both;'></div></div>";
	else html = "<div class='meeting-agenda-item-status'><div class='meeting-agenda-item-title'><span class='meeting-agenda-item-irbnumber'>"+d.protocolId+"</span>: "+d.protocolTitle+"</div>"+pi+"<div class='meeting-agenda-item-type'>"+d.protocolFormType+" <a href='"+appContext+"/agendas/"+Clara.IRBMeeting.AgendaId+"/agenda-items/"+d.id+"/view' target='_blank'>Open in new window.</a></div>"+htmlLetter+"<div style='clear:both;'></div></div>";
	jQuery("#meeting-agenda-item-info").html(html);
	Ext.getCmp("meeting-metadata-panel").setHeight(jQuery(".meeting-agenda-item-status").height()+12);
	Ext.getCmp("btnSaveTranscriptionNotes").setDisabled(true);
	Ext.getCmp("clara-irbmeeting-page-panel").doLayout();
});


Clara.IRBMeeting.ClockTask = {
	run: function(){
		// do math against Clara.IRBMeeting.StartTime here..
		var seconds = Math.floor((new Date() - new Date(meeting.starttime)) / 1000);
		var minutes = Math.floor(seconds / 60);
		var h_minutes = minutes % 60;
		var hours = Math.floor(seconds / 3600);
		var minPad = (h_minutes < 10)?"0":"";
		var str = "<span id='meeting-time' class='meeting-in-progress'><strong id='meeting-status'>In progress</strong> "+hours+":"+minPad+h_minutes+"</span>";
		
		jQuery('#irbmeetingclock').html(str);
	},
	interval:30000 // 30 sec
};

Clara.IRBMeeting.GetStatusText = function(enumStatus){
	if (enumStatus == null || typeof enumStatus == 'Undefined') return "Unknown";
	else return enumStatus.charAt(0).toUpperCase() + enumStatus.slice(1).toLowerCase().replace(/_/g," ");
};

Clara.IRBMeeting.GetAgendaRosterStore = function() {
	
	return new Ext.data.JsonStore({
		xtype:'jsonstore',
		url: appContext + "/ajax/agendas/"+Clara.IRBMeeting.AgendaId+"/agenda-irb-reviewers/list",
		autoLoad:false,
		fields: [
					{name:'id'},
					{name:'status'},
					{name:'reason', mapping:'reason'},
					{name:'irbreviewerid', mapping:'irbReviewer.id'},
					{name:'userid',mapping:'irbReviewer.user.id'},
					{name:'username', mapping:'irbReviewer.user.username'},
					{name:'fname', mapping:'irbReviewer.user.person.firstname'},
					{name:'lname', mapping:'irbReviewer.user.person.lastname'},
					{name:'phone', mapping:'irbReviewer.user.person.workphone'},
					{name:'alternativeMember', mapping:'irbReviewer.alternativeMember'},
					{name:'affiliated', mapping:'irbReviewer.affiliated'},
					{name:'degree', mapping:'irbReviewer.degree'},
					{name:'irbRoster',mapping:'irbReviewer.irbRoster'},
					{name:'comment', mapping:'irbReviewer.comment'},
					{name:'type', mapping:'irbReviewer.type'},
					
					{name:'altirbreviewerid',convert:function(v,node){
						if (node.alternateIRBReviewer){
							return node.alternateIRBReviewer.id;
						} else return null;
					}},
					{name:'altirbrevieweruserid',convert:function(v,node){ 
						if (node.alternateIRBReviewer){
							return node.alternateIRBReviewer.user.id;
						} else return null;
					}},
					{name:'altirbreviewerfname',convert:function(v,node){ 
						if (node.alternateIRBReviewer){
							return node.alternateIRBReviewer.user.person.firstname;
						} else return null;
					}},
					{name:'altirbreviewerlname',convert:function(v,node){ 
						if (node.alternateIRBReviewer){
							return node.alternateIRBReviewer.user.person.lastname;
						} else return null;
					}},
					{name:'altirbreviewerphone',convert:function(v,node){ 
						if (node.alternateIRBReviewer){
							return node.alternateIRBReviewer.user.person.workphone;
						} else return null;
					}}
				]
	});
	
};

Clara.IRBMeeting.AgendaRenderer = function(v,p,r){
	var t = "<div class='agenda-list-row'>";
	var d = r.data;
	t += "<div class='agenda-row-status'><div class='agenda-row-irbnumber'>#"+d.protocolId+"</div><div class='agenda-row-type'>"+d.protocolFormType+"</div><div style='clear:both;'></div></div><div class='agenda-row-title'>"+d.protocolTitle+"</div>";
	return t+"</div>";
};

Clara.IRBMeeting.AgendaGridPanel = Ext.extend(Ext.grid.GridPanel,{
		constructor:function(config){		
			Clara.IRBMeeting.AgendaGridPanel.superclass.constructor.call(this, config);
			Clara.IRBMeeting.MessageBus.on('aftermeetingload', this.onAfterMeetingLoad, this);
			Clara.IRBMeeting.MessageBus.on('agendaitemchosen', this.onAgendaItemChosen, this);
			Clara.IRBMeeting.MessageBus.on('motionadded', this.onMotionChanged, this);
			Clara.IRBMeeting.MessageBus.on('motionsupdated', this.onMotionChanged, this);
		},
		onAgendaItemChosen: function(){
			var ai = Clara.IRBMeeting.CurrentAgendaItemRecord.data;
			clog("Clara.IRBMeeting.AgendaGridPanel.onAgendaItemChosen()",ai);
			clog("getActivityForItem",meeting.getActivityForItem(ai.id));
			if (ai.category == "FULL_BOARD" || ai.category == "MINUTES"){
				if (ai != null && meeting.getActivityForItem(ai.id) == null){
					meeting.activity.push(new Clara.IRBMeeting.Activity({
						timestamp:		Clara.IRBMeeting.GetTime(),
						type:			(ai.protocolFormType === "")?ai.category:ai.protocolFormType,
						agendaitemid:	ai.id,
						notes:			ai.notes,
						protocolid:		ai.protocolId,
						protocolformid:	ai.protocolFormId
					}));
				}
			} else {
				//xxxx
			}
		},

		
		onMotionChanged: function(){
			Clara.AgendaItem.AgendaStore.removeAll();
	   		Clara.AgendaItem.AgendaStore.proxy.setUrl(appContext + "/ajax/agendas/"+Clara.IRBMeeting.AgendaId+"/agenda-items/list");
	   		Clara.AgendaItem.AgendaStore.load();
		},
		
		initComponent: function(){
			var t = this;
			var config = {
					border:false,
					loadMask:true,
					autoExpandColumn:'col-agenda-item-row-info',
					store: Clara.AgendaItem.AgendaStore,
					view: new Ext.grid.GridView({
						rowOverCls:'',
						selectedRowClass:'meeting-selected-agenda-item',
						rowSelectorDepth:20,
			    		getRowClass: function(record, index){
			    			var act = meeting.getActivityForItem(record.get("id"));
			    			var visitedClass = ((act && act.motionCount() > 0) || record.get("category") =="REPORTED")?"agenda-list-row-visited":"agenda-list-row-notvisited";
			    			return visitedClass;
			    		},
			    		emptyText:'<h1>No agenda items found.</h1>'
			    	}),
			    	hideHeaders:true,
			    	columns: [
					        {header: 'Category', renderer: function(v){ return Clara.IRBMeeting.GetStatusText(v); }, sortable: true, hidden:true, dataIndex: 'category',id:'col-agenda-item-row-category'},
					        {header:'#',dataIndex:'protocolId',menuDisabled:true, sortable:true,width:55,renderer:function(v,p,r){
					        	return "<span class='agenda-list-row-id'>"+v+"</span>";
					        }},
					        {id:'col-agenda-item-row-info',header: 'Info / Reviewer(s)',menuDisabled:true,sortable: true,dataIndex: 'id',renderer:function(v,p,r)
					        	{
					        	
					        		
					        		var letterSentClass = (meeting.hasLetterBeenSent(r) == true)?" agenda-list-row-lettersent":"";
					        		
					        		var html = "<div class='agenda-list-row "+letterSentClass+"'><div class='agenda-list-row-desc'>";
					        		
					        		if (r.get("category") == "MINUTES") html += "<div class='agenda-list-row-type' style='text-align:left;'>Previous Meeting's Minutes</div>";
					        		
					        		else if (r.get("category") == "REPORTED") html += "<div class='agenda-list-row-type' style='text-align:left;font-style:italic;'>Reported: "+r.get("protocolFormType")+"</div>";
					        		
					        		else html += "<div class='agenda-list-row-type' style='text-align:left;'>"+r.get("protocolFormType")+"</div>";
					        		html += "<div class='agenda-list-row-pi' style='text-align:left;'>";
					        		var s = r.get("staffs");
					        
					        		for (i=0,l=s.length;i<l;i++){
					        			var st = s[i];
					        	
					        				if (st.get("isPI")){
					        					html += "<div class='form-assigned-pi'>PI: <span style='color:black;font-weight:800;'>"+st.get("firstname")+" "+st.get("lastname")+"</span></div>";
					        				}
					        		
					        		}
					        		
						        	// if (s) html += "<div class='form-assigned-pi'>PI: <span style='color:black;font-weight:800;'>"+s.get("firstname")+" "+s.get("lastname")+"</span></div>";
					        		html+="</div>";
					        		if (r.get("reviewers").length > 0){
					        			html += "<div style='margin-top:2px;border-top:1px dotted #eee;padding-top:2px;'><ul class='form-assigned-reviewers'>";
						        		for (var i=0;i<r.get("reviewers").length;i++){
						        			html += "<li class='form-assigned-reviewer'>"+r.get("reviewers")[i].get("name")+"</li>";
						        		}
						        		html += "</ul></div>";
					        		}
					        		
					        		html += "</div></div>";
					        		return html;
					        	}
					        }
					        ],
					listeners: {
						rowclick: function(grid, rowI, event)   {
					    	if (!Ext.getCmp("clara-meeting-listwindow")){
								Clara.IRBMeeting.CurrentAgendaItemRecord = grid.getStore().getAt(rowI);
								clog(Clara.IRBMeeting.CurrentAgendaItemRecord);
								Clara.IRBMeeting.MessageBus.fireEvent('agendaitemchosen');  
					    	}
					    },
					    rowdblclick: function(grid, rowI, event)   {
					    	if (Ext.getCmp("clara-meeting-listwindow")){
								Clara.IRBMeeting.CurrentAgendaItemRecord = grid.getStore().getAt(rowI);
								clog(Clara.IRBMeeting.CurrentAgendaItemRecord);
								Clara.IRBMeeting.MessageBus.fireEvent('agendaitemchosen');  
								Ext.getCmp("clara-meeting-listwindow").close();
					    	}
					    }
							},
					tbar:[{
						xtype:'textfield',
						enableKeyEvents : true,
						id:'meeting-agenda-items-filter',
						style:'font-size:18px;',
						listeners:{
							keyup:function(f,e){
								var v = f.getValue().toLowerCase();
								if (v.trim() == '')  Clara.AgendaItem.AgendaStore.clearFilter(); 
								else  {
									Clara.AgendaItem.AgendaStore.filter('protocolFormType',v);
									Clara.AgendaItem.AgendaStore.filterBy(function(r){
										var staff = "";
										var reviewers = "";
										if (r.get("staffs").length > 0){
											for (var j=0;j<r.get("staffs").length;j++){
												if (r.get("staffs")[j].get("isPI") == true) staff += " "+r.get("staffs")[j].get("firstname")+" "+r.get("staffs")[j].get("lastname");
												clog("FILTER STAFF IS PI",r.get("staffs")[j].get("isPI"), staff);
											}
										}
										if (r.get("reviewers").length > 0){
											for (j=0;j<r.get("reviewers").length;j++){
												reviewers += " "+r.get("reviewers")[j].get("name");
											}
										}
									
										return (r.get("protocolFormType").toLowerCase().search(v) > -1 ||
												r.get("protocolId").toLowerCase().search(v) > -1 ||
												staff.toLowerCase().search(v) > -1 ||
												reviewers.toLowerCase().search(v) > -1
												);
									});
								}
							}
						}
					},{
						xtype:'button',
						iconCls:'icn-cross',
						handler:function(){
							Ext.getCmp("meeting-agenda-items-filter").setValue('');
							Clara.AgendaItem.AgendaStore.clearFilter(); 
						}
					}],
			};
			Ext.apply(this, Ext.apply(this.initialConfig, config));
			Clara.IRBMeeting.AgendaGridPanel.superclass.initComponent.apply(this, arguments);
		}
});
Ext.reg('claraagendaitemgridpanel', Clara.IRBMeeting.AgendaGridPanel);

Clara.IRBMeeting.AgendaListWindow = Ext.extend(Ext.Window, {
	id: 'clara-meeting-listwindow',
	title:'Choose an agenda item..',
	layout:'fit',
	agenda:{},
	constructor:function(config){		
		Clara.IRBMeeting.AgendaListWindow.superclass.constructor.call(this, config);
	},
	initComponent: function(){
		var t = this;
		var config = {
				items:[{xtype:'claraagendaitemgridpanel'}],
				modal:true,
				width:720,
				height:450,
				buttons:[{
					text:'Close',
					handler:function(){t.close();}
				}]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.IRBMeeting.AgendaListWindow.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claraagendaitemlistwindow', Clara.IRBMeeting.AgendaListWindow);

Clara.IRBMeeting.PagePanel = Ext.extend(Ext.Panel, {
	id: 'clara-irbmeeting-page-panel',
	frame:false,
	layout:'border',
	border:false,
	height:350,
	agenda:{},
	protocolInfoEl:"",

	constructor:function(config){		
		Clara.IRBMeeting.PagePanel.superclass.constructor.call(this, config);
		Clara.IRBMeeting.MessageBus.on('agendaitemselected', this.onAgendaItemSelected(), this);
	},
	onAgendaItemSelected: function(){
		Ext.getCmp("meeting-tab-panel").setDisabled(false);
		this.updateAgendaItemView();
	},
	updateAgendaItemView: function(){
		var ai = Clara.IRBMeeting.CurrentAgendaItemRecord;
		if(ai.data){
			clog("Updating view for agenda item..");
			clog(ai);
			var html = "<div id='meeting-agenda-item-title'>"+ai.get("protocolTitle")+"</div>";
		
			jQuery("#meeting-agenda-item-info").html(html);
		}
	},
	bbar:new Ext.ux.StatusBar({
        id: 'irbmeeting-status',
        statusAlign: 'right',

        items: ['->',{xtype:'tbtext',id:'irbmeeting-clock',text:'<span id="irbmeetingclock"><span id="meeting-time" class="meeting-not-started"><strong id="meeting-status">Not started yet</strong></span></span>'}]
    }),
	tbar: new Ext.Toolbar({
		style:'background-image:none;background-color:#ecf0f1; border:0px !important;',
		items:[{
				xtype:'panel',
				html:'<div id="meeting-irb-logo">IRB Meeting</div>',
				padding:4,
				unstyled:true,
				bodyStyle:'font-size:24px;background:transparent;',
				border:false
			   },'->',{ 
					xtype:'button',
					text:'Notify chair..',
					iconAlign:'top',
					id:'btn-meeting-send-chair',
					hidden:true,
					iconCls:'icn-thumb-up',
					handler:function(){
						if (isIrbOffice){
							Ext.Msg.show({
								title:'Notify chair that agenda is complete?',
								msg:'You will not be able to make any additional changes to the meeting. Are you sure you want to do this?',
								buttons:Ext.Msg.YESNO,
								fn:function(btn){
									if (btn == 'yes'){
										meeting.status = "SENT_TO_CHAIR";
										Clara.IRBMeeting.MessageBus.fireEvent('senttochair');
									}
								},
							    icon: Ext.MessageBox.QUESTION
							});
						} else {
							alert("Only the IRB office can do this.");
						}
				   	}
				
			},{ 
				xtype:'button',
				text:'Send to IRB office',
				iconAlign:'top',
				id:'btn-meeting-chair-send-transcriber',
				disabled:!isChair,
				hidden:true,
				iconCls:'icn-application-share',
				handler:function(){
					if (Clara.IRBMeeting.allLettersSent()){
						Ext.Msg.show({
							title:'Send to IRB Office?',
							msg:'You will not be able to make any additional changes to the meeting. Are you sure you want to do this?',
							buttons:Ext.Msg.YESNO,
							fn:function(btn){
								if (btn == 'yes'){
									Clara.IRBMeeting.MessageBus.fireEvent('senttotranscriber');
								}
							},
						    icon: Ext.MessageBox.QUESTION
						});
					} else {
						alert("You cannot send this meeting to the IRB office until ALL letters have been created.");
					}
			   	}
			
		},{ 
			xtype:'button',
			text:'Complete Meeting',
			iconAlign:'top',
			id:'btn-meeting-complete',
			disabled:!canEditMeeting,
			hidden:true,
			iconCls:'icn-door',
			handler:function(){
				Ext.Msg.show({
					title:'Complete meeting?',
					msg:'You will not be able to make any additional changes to the meeting. Are you sure you want to do this?',
					buttons:Ext.Msg.YESNO,
					fn:function(btn){
						if (btn == 'yes'){
							Clara.IRBMeeting.MessageBus.fireEvent('completemeeting');
						}
					},
				    icon: Ext.MessageBox.QUESTION
				});
		   	}
		
	},{xtype: 'tbspacer', width: 30},{
					xtype:'button',
					text:'Attendance',
					id:'btn-attendance',
					iconAlign:'top',
					disabled:!canEditMeeting,
					iconCls:'icn-users',
					handler:function(){
				   		new Clara.IRBMeeting.AttendanceWindow({agenda:{id:Clara.IRBMeeting.AgendaId}}).show();
			   		}},{
						text:'Announcements',
						iconCls:'icn-newspaper--exclamation',
						iconAlign:'top',
						id:'btn-meeting-announcements',
						handler:function(){
							Ext.Msg.show({
								   title:'Meeting announcements',
								   msg: 'These notes and announcements will appear in the minutes for the whole meeting.',
								   buttons: Ext.Msg.OK,
								   multiline:true,
								   modal:true,
								   value:meeting.notes,
								   fn: function(btn,v){
									   var readOnlyStatuses = ["SENT_TO_CHAIR","COMPLETE","SENT_TO_TRANSCRIBER"];
									   if(!readOnlyStatuses.hasValue(meeting.status) && claraInstance.HasAnyPermissions(['ROLE_IRB_MEETING_OPERATOR','ROLE_IRB_CHAIR','ROLE_SYSTEM_ADMIN'])){
										   meeting.notes = v;
										   Clara.IRBMeeting.MessageBus.fireEvent('meetingchanged',this);  
									   } else {
										   alert("You cannot save announcements at this time.");
									   }
								   },
								   animEl: 'elId',
								   icon: Ext.MessageBox.QUESTION
								});
						}
					},
			   		{xtype: 'tbspacer', width: 30},{
			xtype:'button',
			text:'Start Meeting',
			id:'btn-meeting-start',
			iconAlign:'top',
			iconCls:'icn-control',
			handler:function(){
				if (Clara.IRBMeeting.quorumMet) Clara.IRBMeeting.MessageBus.fireEvent('startmeeting');
				else alert("Quorum has not yet been met. Check 'Attendence' and try again.");
		   	}
		},{
			xtype:'button',
			text:'Stop Meeting',
			id:'btn-meeting-stop',
			iconAlign:'top',
			disabled:true,
			iconCls:'icn-control-stop-square',
			handler:function(){
				Clara.IRBMeeting.MessageBus.fireEvent('stopmeeting');
		   	}
		},{
			text:'Back to Clara',
			iconCls:'icn-arrow-curve-180-left',
			iconAlign:'top',
			handler:function(){
				location.href=appContext+"/ajax/agendas/"+Clara.IRBMeeting.AgendaId+"/unlock";
			}
		}
           	]}),
	initComponent: function(){
		var t = this;
		var config = {
				
				items:[
				       
				       		{
				       			xtype:'claraagendaitemgridpanel',
				       			region:'west',
				       			width:290,
				       			split:true,
				       			collapsed:true,
				       			disabled:true,
				       			collapsible:true,
				       			title:'<div class="meeting-agenda-list-title">Agenda for <span class="meeting-agenda-date">'+Clara.IRBMeeting.AgendaDate+'</span></div>',
				       			id:'agendaItemPanel',
				       			border:false,
				       			style:'border-right:1px solid #99bbe8;',
				       			listeners:{
				       				beforeexpand:function(p){
				       					if (meeting.status == "NEW" || meeting.status == ""){
				       						alert("You need to take attendance and start the meeting before viewing agenda items.");
				       						return false
				       					} else return true;
				       				}
				       			}
				       			
				       		},
				       		{
				       			xtype:'panel',
				       			region:'center',
				       			layout:'border',
				       			border:false,
				       			style:'border-left:1px solid #99bbe8;',
				       			items:[{
				       				xtype:'panel',id:'meeting-metadata-panel',html:'<div id="meeting-agenda-item-info"></div>',region:'north',border:false
				       			},
					       		
					       		{
					       			xtype:'tabpanel',region:'center',border:false,style:'background-color:white;',activeTab:0,id:'meeting-tab-panel', disabled:true,
					       			items:[{xtype:'meeting-contingencygrid-panel',title:'Contingencies / Notes',loadMask:true},{xtype:'clarairbmotionpanel', title:'Motions'},{xtype:'clarairbmeetingnotespanel', title:'Transcription'}]
					       		}]
				       			
				       		}
				       ]
		};
		
   		Clara.AgendaItem.AgendaStore.removeAll();
   		Clara.AgendaItem.AgendaStore.proxy.setUrl(appContext + "/ajax/agendas/"+Clara.IRBMeeting.AgendaId+"/agenda-items/list");
   		Clara.AgendaItem.AgendaStore.load({callback:function(recs){
   			clog("Clara.AgendaItem.AgendaStore LOADED",recs);
   		}});
   		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.IRBMeeting.PagePanel.superclass.initComponent.apply(this, arguments);
		
		
	}
});
Ext.reg('clarairbmeetingpagepanel', Clara.IRBMeeting.PagePanel);

Clara.IRBMeeting.NotesPanel = Ext.extend(Ext.form.FormPanel,{
	id: 'clara-irbmeeting-notes-panel',
	frame:false,
	border:false,
	constructor:function(config){		
		Clara.IRBMeeting.NotesPanel.superclass.constructor.call(this, config);
		Clara.IRBMeeting.MessageBus.on('agendaitemchosen', this.onAgendaItemSelected, this);
	},
	onAgendaItemSelected: function(){
		var agendaitem = Clara.IRBMeeting.CurrentAgendaItemRecord;
		clog("Clara.IRBMeeting.NotesPanel: HEARD AGENDAITEM SELECT. Filling Notes:",agendaitem.data);
		Ext.getCmp("fldAgendaItemNotes").setValue(meeting.getNotesForAgendaItem(agendaitem.get("id")));
	},
	initComponent: function(){
		var t = this;
		var config = {
				labelAlign:'top',
				padding:6,
				//fbar:[],
				items: [{xtype:'textarea',id:'fldAgendaItemNotes',fieldLabel:'Item Notes',anchor:'100% 60%',style:'font-size:24px;line-height:28px;',
					enableKeyEvents:true,
					listeners:{
						keydown:function(ta){
							Ext.getCmp("btnSaveTranscriptionNotes").setDisabled(!canEditMeeting);
						}
					}
				
				},{xtype:'button',anchor:'100% 10%',disabled:true,id:'btnSaveTranscriptionNotes',text:'<span style="font-size:24px;font-weight:800;">Save</span>',handler:function(){
					//var act = meeting.getActivityForItem(Clara.IRBMeeting.CurrentAgendaItemRecord.get("id"));
					//act.notes = Ext.getCmp("fldAgendaItemNotes").getValue();
					if (canEditMeeting) { 
						if(typeof Clara.IRBMeeting.CurrentAgendaItemRecord != "undefined" && typeof Clara.IRBMeeting.CurrentAgendaItemRecord.data != "undefined" && Clara.IRBMeeting.CurrentAgendaItemRecord != null) {
							meeting.setNotesForAgendaItem(Clara.IRBMeeting.CurrentAgendaItemRecord.get("id"), Ext.getCmp("fldAgendaItemNotes").getValue());
							Clara.IRBMeeting.MessageBus.fireEvent('meetingchanged',this);
							Ext.getCmp("btnSaveTranscriptionNotes").setDisabled(true);
						}
						else alert("Select an agenda item first.");
					}
				}}]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.IRBMeeting.NotesPanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('clarairbmeetingnotespanel',Clara.IRBMeeting.NotesPanel);

Clara.IRBMeeting.AgendaPanel = Ext.extend(Ext.grid.GridPanel, {
	id: 'clara-irbmeeting-agenda-panel',
	frame:false,
	border:false,
	trackMouseOver:false,
	protocolFormXmlData:{},
	agenda:{},
	constructor:function(config){		
		Clara.IRBMeeting.AgendaPanel.superclass.constructor.call(this, config);
		Clara.IRBMeeting.MessageBus.on('agendaitemselected', this.onAgendaItemSelected, this);
	},
	onAgendaItemSelected: function(agendaitem){
		clog("HEARD AGENDAITEM SELECT YOYO");
	},
	initComponent: function(){
		var t = this;
		clog(t.agenda);
		var config = {
				loadMask:true,
				store: new Ext.data.Store({
		    		proxy: new Ext.data.HttpProxy({
		    			url: appContext + "/ajax/agendas/"+t.agenda.id+"/agenda-items/list",
		    			method:"GET",
		    			headers:{'Accept':'application/xml;charset=UTF-8'}
		    		}),
					autoLoad:true,
					//groupField: 'category',
					reader: new Ext.data.XmlReader({
						record:'agenda-item',
						root: 'list',
						fields: [
							{name:'id', mapping:'@id'},
							{name:'category', mapping:'@category'},
							{name:'protocolFormType', mapping:'protocol-form>protocol-form-type'},
							{name:'protocolFormTypeId', mapping:'protocol-form>protocol-form-type>@id'},
							{name:'protocolFormId', mapping:'protocol-form>@id'},
							{name:'protocolId', mapping:'protocol-form>protocol-meta>protocol>@id'},
							{name:'protocolTitle', mapping:'protocol-form>protocol-meta>protocol>title'},
							{name:'protocolFormStatus', mapping:'protocol-form>protocol-form-meta>status'},
							//{name:'protocolFormStatusDate', mapping:'protocol-form>status>modified', type: 'date', dateFormat: 'Y-m-d H:i:s.u'},
							{name:'reviewers', mapping:'reviewers', convert:function(v,node){
								return new Ext.data.XmlReader({
									record: 'reviewer',
									fields: [{name:'name', mappgin:'name'}]
								}).readRecords(node).records; 
							}}
							
						]
					})
				}),
				view: new Ext.grid.GridView({
					forceFit:true,
		    		getRowClass: function(record, index){
		    			return (record.get('reviewers').length == 0 && record.get('category') == 'FULL_BOARD')?'agenda-item-row-noreviewers':'';
		    		},
		    		emptyText:'<h1>No agenda items found.</h1>'
		    	}),
		    	columns: [
		    	          new Ext.grid.RowNumberer(),
				        {header: 'Agenda Item', sortable: false, dataIndex: 'protocolId',renderer:Clara.IRBMeeting.AgendaRenderer}
				        ],
				listeners: {
						    rowclick: function(grid, rowI, event)   {
								var record = grid.getStore().getAt(rowI);
								clog("CLICK ITEM",record);
								Clara.IRBMeeting.MessageBus.fireEvent('agendaitemselected');  
						    },
						    render: function(grid) {
			                    grid.getView().el.select('.x-grid3-header').setStyle('display',    'none');
			                }
						}
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.IRBMeeting.AgendaPanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('clarairbmeetingagendapanel', Clara.IRBMeeting.AgendaPanel);