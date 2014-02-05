Ext.ns('Clara.IRBMeeting');

var requiredRoles = {
		editMeeting : [ "ROLE_IRB_CHAIR","ROLE_IRB_OFFICE","ROLE_IRB_MEETING_OPERATOR" ]
	};

Clara.IRBMeeting.quorumMet = false;
canEditMeeting = claraInstance.HasAnyPermissions(requiredRoles.editMeeting);
isChair = claraInstance.HasAnyPermissions(["IRB_CHAIR","ROLE_IRB_CHAIR"]);
isIrbOffice = claraInstance.HasAnyPermissions(["ROLE_IRB_OFFICE","ROLE_IRB_MEETING_OPERATOR"]);

Clara.IRBMeeting.MessageBus = new Ext.util.Observable();
Clara.IRBMeeting.MessageBus.addEvents('beforenotifyserver','afternotifyserver','quorummet','quorumlost','agendaitemlettersent','motionadded','attendancechanged','votechanged','motionchosen','agendaitemchosen','contingencychosen','contingenciesupdated','continuemeeting','startmeeting','restartmeeting','stopmeeting','beforemeetingsave','aftermeetingsave','error','beforemeetingload','aftermeetingload','meetingchanged');

Clara.IRBMeeting.StartTime = new Date();
Clara.IRBMeeting.TimeDelta = 0;

Clara.IRBMeeting.GetTime = function(){
	//ajax/system/get-current-time
	var dt = new Date();
	if (Clara.IRBMeeting.TimeDelta == 0){
		jQuery.ajax({
			  type: 'GET',
			  async:false,
			  url: appContext+"/ajax/system/get-current-time",
			  success: function(data){
				  Clara.IRBMeeting.TimeDelta = data-dt.valueOf();
				  clog("System time: "+data+", Client time: "+dt.valueOf()+", Delta: "+Clara.IRBMeeting.TimeDelta);
			  },
			  error: function(){
				  Clara.IRBMeeting.MessageBus.fireEvent('error', this);  
			  },
			  dataType: 'text'
		});
	} else {
		return dt.valueOf()+Clara.IRBMeeting.TimeDelta;
	}
};

Clara.IRBMeeting.NotifyServer = function(url,data, reloadOnSuccess){
	Clara.IRBMeeting.MessageBus.fireEvent('beforenotifyserver', this); 
	reloadOnSuccess = reloadOnSuccess || false;
	if (!data || typeof data == 'undefined'){
		data = { userId:claraInstance.user.id };
	} else {
		data.userId = claraInstance.user.id;
	}
	jQuery.ajax({
		  type: 'GET',
		  async:true,
		  data:data,
		  url: appContext+"/ajax/agendas/"+Clara.IRBMeeting.AgendaId+"/"+url,
		  success: function(data){
			  clog("server notified: "+url,data);
			  Clara.IRBMeeting.MessageBus.fireEvent('afternotifyserver', this); 
			  if (reloadOnSuccess == true) {
				  clog("reloadOnSuccess, RELOADING!");
				  location.reload();
			  }
		  },
		  error: function(){
			  Clara.IRBMeeting.MessageBus.fireEvent('error', this);  
		  },
		  dataType: 'text'
	});
};

Clara.IRBMeeting.CurrentAgendaItemRecord = {};
Clara.IRBMeeting.CurrentCommentRecord = {};
Clara.IRBMeeting.CurrentMotionRecord = {};

Clara.IRBMeeting.Types = {
	Vote: {
		YES: 'Y',
		NO: 'N',
		ABSTAIN: 'A',
		NOT_VOTING: 'NV'
	}
};



Clara.IRBMeeting.Meeting = function(o){
	this.starttime = 			(o.starttime || '');
	this.endtime = 				(o.endtime || '');
	this.status = 				(o.status || 'NEW');
	this.senttochair = 			(o.senttochair || false);
	this.closed = 				(o.closed || false);
	this.notes = 				(o.notes || '');
	this.attendance = 			(o.attendance || []);
	this.activity = 			(o.activity || []);
	
	this.clearAttendance = function(){
		this.attendance = [];
	};

	this.getStatus = function(){
		return this.status;
	};
	
	this.getQuorumStatus = function(){
		var t = this;
		var q = "QUORUM_NOT_MET";
		
		t.activity.sort(function(a,b){return a.timestamp-b.timestamp;});
		
		for (var i=0;i<t.activity.length;i++){
			if (t.activity[i].type == 'QUORUM_MET' || t.activity[i].type == 'QUORUM_NOT_MET'){
				q = t.activity[i].type;
			}
		}
		return q;
	};
	
	this.getMotionFromTimestamp = function(ts){
		clog("getMotionFromTimestamp",ts);
		var t = this;
		for (var i=0;i<t.activity.length;i++){
			for (var j=0;j<t.activity[i].motions.length;j++){
				if (t.activity[i].motions[j].timestamp == ts){
					return t.activity[i].motions[j];
				}
			}
		}
	};
	
	this.removeMotionByTimestamp = function(ts){
		var t = this;
		for (var i=0;i<t.activity.length;i++){
			for (var j=0;j<t.activity[i].motions.length;j++){
				if (t.activity[i].motions[j].timestamp == ts){
					clog("Removing with "+t.activity[i].motions[j].timestamp);
					t.activity[i].motions.splice(j,1);
				}
			}
		}
	};
	
	this.getActivityForItem= function(agendaItemId){
		var t = this;
		for (var i=0;i<t.activity.length;i++){
			if (t.activity[i].agendaitemid == agendaItemId){
				return t.activity[i];
			}
		}
		return null;
	};
	
	this.getAttendingPerson = function(userid){
		for (var i=0; i<this.attendance.length;i++){
			a = this.attendance[i];
			if (a.userid == userid) return a;
		}
		return null;
	};
	
	this.getAttendanceStore = function(){
		var data = [];
		var a = {};
		for (var i=0; i<this.attendance.length;i++){
			a = this.attendance[i];
			data.push([a.userid,a.fname,a.lname,a.currentStatus().value,a.currentStatus().note,a.isAvailable()]);
		}
		return new Ext.data.ArrayStore({
			idIndex: 0,
			fields: ['userid','fname','lname','currentstatus','currentnote','isavailable'],
			data: data
		});
	};
	
	this.hasMotions = function(agendaItemId){
		var t=this;
		for (var i=0;i<t.activity.length;i++){
			if (t.activity[i].agendaitemid == agendaItemId){
				return t.activity[i].motions.length > 0;
			}
		}
		return false;
	};
	
	this.hasLetterBeenSent = function(agendaItemRec){
		clog("hasLetterBeenSent called");
		var t = this;
		var id = agendaItemRec.get("id");
		for (var i=0;i<t.activity.length;i++){
			if (t.activity[i].agendaitemid == id){
				clog("sent?",t.activity[i].lettersent);
				return t.activity[i].lettersent;
			}
		}
	};
	
	this.setAgendaItemLetterSent = function(id,set){
		var t = this;
		for (var i=0;i<t.activity.length;i++){
			if (t.activity[i].agendaitemid == id){
				t.activity[i].lettersent = set;
			}
		}
	};
	
	this.setNotesForAgendaItem = function(id, notes){
		var t = this;
		for (var i=0;i<t.activity.length;i++){
			if (t.activity[i].agendaitemid == id){
				t.activity[i].notes = notes;
			}
		}
	};
	
	this.getNotesForAgendaItem = function(id){
		var t = this;
		for (var i=0;i<t.activity.length;i++){
			if (t.activity[i].agendaitemid == id){
				return t.activity[i].notes;
			}
		}
	};
	
	this.updateUI = function(){
		return;
		clog("updateui");
		if (this.getStatus() == 'NEW'){
			Ext.getCmp("btn-meeting-start").setDisabled(false);
			if (Ext.getCmp("agendaItemPanel")) Ext.getCmp("agendaItemPanel").setDisabled(true);
			Ext.getCmp("btn-meeting-send-chair").setDisabled(true);
			if (Ext.getCmp("btn-meeting-chair-send-letter")) Ext.getCmp("btn-meeting-chair-send-letter").setVisible(false);
			Ext.getCmp("btn-meeting-chair-send-transcriber").setVisible(false);
			Ext.getCmp("btn-attendance").setDisabled(false);
			Ext.getCmp("btn-meeting-stop").setDisabled(true);
			jQuery("#meeting-time").removeClass("meeting-in-progress").removeClass("meeting-ended").addClass("meeting-not-started");
		} else if (this.getStatus() == 'IN_PROGRESS'){
			Ext.getCmp("btn-meeting-start").setDisabled(true);
			if (Ext.getCmp("agendaItemPanel")) Ext.getCmp("agendaItemPanel").setDisabled(false);
			Ext.getCmp("btn-meeting-send-chair").setDisabled(true);
			if (Ext.getCmp("btn-meeting-chair-send-letter")) Ext.getCmp("btn-meeting-chair-send-letter").setVisible(false);
			Ext.getCmp("btn-meeting-chair-send-transcriber").setVisible(false);
			Ext.getCmp("btn-attendance").setDisabled(false);
			Ext.getCmp("btn-meeting-stop").setDisabled(false);
			Ext.TaskMgr.start(Clara.IRBMeeting.ClockTask);
		} else if (this.getStatus() == 'STOPPED'){
			Ext.getCmp("btn-meeting-start").setDisabled(false);
			if (Ext.getCmp("agendaItemPanel")) Ext.getCmp("agendaItemPanel").setDisabled(false);
			Ext.getCmp("btn-meeting-send-chair").setDisabled(false);
			if (Ext.getCmp("btn-meeting-chair-send-letter")) Ext.getCmp("btn-meeting-chair-send-letter").setVisible(false);
			Ext.getCmp("btn-meeting-chair-send-transcriber").setVisible(false);
			//Ext.getCmp("btn-agenda-items").setDisabled(false);
			Ext.getCmp("btn-attendance").setDisabled(true);
			Ext.getCmp("btn-meeting-stop").setDisabled(true);
			var dt = new Date(this.endtime).toDateString();
			var stm = new Date(this.starttime).format("h:i");
			var etm = new Date(this.endtime).format("h:i");
			jQuery("#meeting-time").removeClass("meeting-not-started").removeClass("meeting-in-progress").addClass("meeting-ended").text("Meeting complete. "+dt+" from "+stm+" to "+etm+".");
		} else if (this.getStatus() == 'SENT_TO_CHAIR'){
			clog("SENT_TO_CHAIR");
			Ext.getCmp("btn-meeting-start").setDisabled(true);
			if (Ext.getCmp("agendaItemPanel")) Ext.getCmp("agendaItemPanel").setDisabled(false);
			Ext.getCmp("btn-meeting-send-chair").setDisabled(true);
			Ext.getCmp("btn-meeting-send-chair").setVisible(!claraInstance.HasAnyPermissions(['ROLE_IRB_CHAIR']));
			if (Ext.getCmp("btn-meeting-chair-send-letter")) Ext.getCmp("btn-meeting-chair-send-letter").setVisible(claraInstance.HasAnyPermissions(['ROLE_IRB_CHAIR','ROLE_SYSTEM_ADMIN']));
			Ext.getCmp("btn-meeting-chair-send-transcriber").setVisible(claraInstance.HasAnyPermissions(['ROLE_IRB_CHAIR','ROLE_SYSTEM_ADMIN']));
			//Ext.getCmp("btn-agenda-items").setDisabled(!claraInstance.HasAnyPermissions(['ROLE_IRB_CHAIR','ROLE_SYSTEM_ADMIN']));
			Ext.getCmp("btn-attendance").setDisabled(true);
			Ext.getCmp("btn-meeting-stop").setDisabled(true);
			var dt = new Date(this.endtime).toDateString();
			var stm = new Date(this.starttime).format("h:i");
			var etm = new Date(this.endtime).format("h:i");
			jQuery("#meeting-time").removeClass("meeting-not-started").removeClass("meeting-in-progress").addClass("meeting-ended");
			jQuery("#meeting-time").text("Meeting complete and sent to chair. "+dt+" from "+stm+" to "+etm+".");
		} else if (this.getStatus() == 'SENT_TO_TRANSCRIBER'){
			Ext.getCmp("btn-meeting-start").setDisabled(true);
			if (Ext.getCmp("agendaItemPanel")) Ext.getCmp("agendaItemPanel").setDisabled(false);
			Ext.getCmp("btn-meeting-send-chair").setDisabled(true);
			Ext.getCmp("btn-meeting-send-chair").setVisible(!claraInstance.HasAnyPermissions(['ROLE_IRB_CHAIR']));
			if (Ext.getCmp("btn-meeting-chair-send-letter")) Ext.getCmp("btn-meeting-chair-send-letter").setVisible(claraInstance.HasAnyPermissions(['ROLE_IRB_CHAIR','ROLE_SYSTEM_ADMIN']));
			Ext.getCmp("btn-meeting-chair-send-transcriber").setVisible(claraInstance.HasAnyPermissions(['ROLE_IRB_CHAIR','ROLE_SYSTEM_ADMIN']));
			//Ext.getCmp("btn-agenda-items").setDisabled(!claraInstance.HasAnyPermissions(['ROLE_IRB_CHAIR','ROLE_SYSTEM_ADMIN']));
			Ext.getCmp("btn-attendance").setDisabled(true);
			Ext.getCmp("btn-meeting-stop").setDisabled(true);
			var dt = new Date(this.endtime).toDateString();
			var stm = new Date(this.starttime).format("h:i");
			var etm = new Date(this.endtime).format("h:i");
			jQuery("#meeting-time").removeClass("meeting-not-started").removeClass("meeting-in-progress").addClass("meeting-ended");
			jQuery("#meeting-time").text("Meeting complete and sent to chair. "+dt+" from "+stm+" to "+etm+".");
		} else if (this.getStatus() == 'CLOSED'){
			Ext.getCmp("btn-meeting-start").setDisabled(true);
			if (Ext.getCmp("agendaItemPanel")) Ext.getCmp("agendaItemPanel").setDisabled(false);
			Ext.getCmp("btn-meeting-send-chair").setDisabled(true);
			if (Ext.getCmp("btn-meeting-chair-send-letter")) Ext.getCmp("btn-meeting-chair-send-letter").setVisible(false);
			Ext.getCmp("btn-meeting-chair-send-transcriber").setDisabled(true);
			//Ext.getCmp("btn-agenda-items").setDisabled(true);
			Ext.getCmp("btn-attendance").setDisabled(true);
			Ext.getCmp("btn-meeting-stop").setDisabled(true);
			var dt = new Date(this.endtime).toDateString();
			var stm = new Date(this.starttime).format("h:i");
			var etm = new Date(this.endtime).format("h:i");
			jQuery("#meeting-time").removeClass("meeting-not-started").removeClass("meeting-in-progress").addClass("meeting-ended");
			jQuery("#meeting-time").text("Meeting closed to changes. "+dt+" from "+stm+" to "+etm+".");
		}
				
		
	};
	
	this.load= function(){
		Clara.IRBMeeting.MessageBus.fireEvent('beforemeetingload', this);
		var url = appContext+"/ajax/agendas/"+Clara.IRBMeeting.AgendaId+"/load-meeting-xml-data";
		var m = this;
		jQuery.ajax({
			  type: 'GET',
			  async:false,
			  url: url,
			  success: function(data){
				  m.fromXML(data);
				  Clara.IRBMeeting.MessageBus.fireEvent('aftermeetingload', this);  
			  },
			  error: function(){
				  Clara.IRBMeeting.MessageBus.fireEvent('error', this);  
			  },
			  dataType: 'xml'
		});
	};

	this.save= function(xmlstring){
		clog("SAVING!!!");
		Clara.IRBMeeting.MessageBus.fireEvent('beforemeetingsave', this);
		var url = appContext+"/ajax/agendas/"+Clara.IRBMeeting.AgendaId+"/save-meeting-xml-data";
		var data = (xmlstring)?xmlstring:this.toXML();
		jQuery.ajax({
			  type: 'POST',
			  async:true,
			  url: url,
			  data: {xmlData: data},
			  success: function(){
				  Clara.IRBMeeting.MessageBus.fireEvent('aftermeetingsave', this);  
			  },
			  error: function(){
				  Clara.IRBMeeting.MessageBus.fireEvent('error', this);  
			  },
			  dataType: 'xml'
		});
	};
	

	
	this.fromXML= function(xml){
		var maxid = 0;
		var start = this.starttime;
		var end = this.endtime;
		var t = this;
		jQuery(xml).find("meeting").each(function(){
			t.starttime = Encoder.htmlDecode(jQuery(this).attr('start'));
			t.endtime = Encoder.htmlDecode(jQuery(this).attr('end'));
			t.status = (typeof jQuery(this).attr('status') == "undefined")?"NEW":jQuery(this).attr('status');
			t.notes = (jQuery(this).find('notes:first').text()).toString();
		});
		
		var atts = [];		// attendance rows

		jQuery(xml).find("attendance").find("member").each(function(){

			var att = new Clara.IRBMeeting.Attendance({
				userid:		parseFloat(jQuery(this).attr('uid')),
				lname:		Encoder.htmlDecode(jQuery(this).attr('lname')),
				fname:		Encoder.htmlDecode(jQuery(this).attr('fname'))
			});

			var sts = [];
			jQuery(this).find("statuses").find("status").each(function(){
				var st = new Clara.IRBMeeting.MemberStatus({
					timestamp: Encoder.htmlDecode(jQuery(this).attr('ts')),
					value: Encoder.htmlDecode(jQuery(this).attr('value')),
					note: Encoder.htmlDecode(jQuery(this).attr('note'))
				});
				att.status.push(st);
			});
			atts.push(att);
		});
		
		t.attendance = atts;
			
		var acts = [];
		jQuery(xml).find("activity").find("item").each(function(){
			var act = new Clara.IRBMeeting.Activity({
				type:Encoder.htmlDecode(jQuery(this).attr('type')),
				lettersent:	(jQuery(this).attr('lettersent') == 'true')?true:false,
				timestamp:Encoder.htmlDecode(jQuery(this).attr('ts')),
				notes:Encoder.htmlDecode(jQuery(this).find('notes').text()),
				agendaitemid:parseFloat(jQuery(this).attr('agendaitemid')),
				protocolid:parseFloat(jQuery(this).attr('protocolid')),
				protocolformid:parseFloat(jQuery(this).attr('protocolformid'))
			});
			
			var mts = [];
			jQuery(this).find("motions").find("motion").each(function(){
				var mt = new Clara.IRBMeeting.Motion({
					timestamp:Encoder.htmlDecode(jQuery(this).attr('ts')),
					value:Encoder.htmlDecode(jQuery(this).attr('value')),
					reviewperiod:parseFloat(jQuery(this).attr('reviewperiod')),
					reviewtype:jQuery(this).attr('reviewtype'),
					adultrisk:Encoder.htmlDecode(jQuery(this).attr('adultrisk')),
					pediatricrisk:Encoder.htmlDecode(jQuery(this).attr('pedrisk')),
					madebyid:parseFloat(jQuery(this).attr('mid')),
					madebyname:Encoder.htmlDecode(jQuery(this).attr('mname')),
					secondbyid:parseFloat(jQuery(this).attr('sid')),
					secondbyname:Encoder.htmlDecode(jQuery(this).attr('sname')),
					consentwaived:Encoder.htmlDecode(jQuery(this).attr('consentwaived')),
					consentdocumentationwaived:Encoder.htmlDecode(jQuery(this).attr('consentdocumentationwaived')),
					hipaawaived:Encoder.htmlDecode(jQuery(this).attr('hipaawaived')),
					hipaa:Encoder.htmlDecode(jQuery(this).attr('hipaa')),
					ncdetermination:Encoder.htmlDecode(jQuery(this).attr('ncdetermination')),
					ncreportable:Encoder.htmlDecode(jQuery(this).attr('ncreportable')),
					UPIRTSO:Encoder.htmlDecode(jQuery(this).attr('UPIRTSO'))
				});
				var vts = [];
				jQuery(this).find("votes").find("vote").each(function(){
					var vt = new Clara.IRBMeeting.Vote({
						userid: parseFloat(jQuery(this).attr('userid')),
						timestamp:Encoder.htmlDecode(jQuery(this).attr('timestamp')),
						name:Encoder.htmlDecode(jQuery(this).attr('name')),
						value:Encoder.htmlDecode(jQuery(this).attr('value')),
						note:Encoder.htmlDecode(jQuery(this).attr('note'))
					});
					vts.push(vt);
				});
				mt.votes = vts;
				mts.push(mt);
			});
			act.motions = mts;
			acts.push(act);
		});
		
		t.activity = acts;

	};
	
	this.toXML= function(){
		var xml = "<meeting status='"+this.status+"' start='"+this.starttime+"' end='"+this.endtime+"'><notes>"+Encoder.htmlEncode(this.notes)+"</notes>";
		if (this.attendance.length > 0){
			xml = xml + "<attendance>";
			for (var i=0; i<this.attendance.length;i++){
				xml = xml + this.attendance[i].toXML();
			}
			xml = xml + "</attendance>";
		}
		if (this.activity.length > 0){
			xml = xml + "<activity>";
			for (var i=0; i<this.activity.length;i++){
				xml = xml + this.activity[i].toXML();
			}
			xml = xml + "</activity>";
		}
		return xml + "</meeting>";
	};
};

Clara.IRBMeeting.Activity = function(o){
	this.type = 				(o.type || '');
	this.timestamp = 			(o.timestamp || '');
	this.notes = 				(o.notes || '');
	this.agendaitemid = 		(o.agendaitemid || 0);
	this.protocolid = 			(o.protocolid || 0);
	this.protocolformid = 		(o.protocolformid || 0);
	this.motions= 				(o.motions || []);
	this.lettersent=			(o.lettersent || false);
	
	this.motionCount = function(){
		return this.motions.length || 0;
	};
	
	this.replaceMotion = function(motion){
		var t = this;
		for (var j=0;j<t.motions.length;j++){
			if (t.motions.timestamp == motion.timestamp){	//TODO: have an ID, DONT use timestamp as identifier for motions..
				t.motions.splice(j,1,motion);
			}
		}
	};
	
	this.getMotionStore = function(){
		clog("Clara.IRBMeeting.Activity.getMotionStore()",this,this.motions);
		var t = this;
		var data = [];
		for (var j=0;j<t.motions.length;j++){
			var m = t.motions[j];
			var vy=0,vn=0,va=0,vnv=0;
			
			for (var k=0;k<m.votes.length;k++){
				if (m.votes[k].value == Clara.IRBMeeting.Types.Vote.YES) vy++;
				if (m.votes[k].value == Clara.IRBMeeting.Types.Vote.NO) vn++;
				if (m.votes[k].value == Clara.IRBMeeting.Types.Vote.ABSTAIN) va++;
				if (m.votes[k].value == Clara.IRBMeeting.Types.Vote.NOT_VOTING) vnv++;
			}
			
			data.push([m.timestamp,m.value,m.adultrisk,m.pediatricrisk,m.reviewtype,m.reviewperiod,m.madebyname,m.secondbyname,m.madebyid,m.secondbyid,vy,vn,va,vnv,m.consentwaived,m.consentdocumentationwaived,m.hipaa,m.hipaawaived,m.ncdetermination,m.ncreportable,m.UPIRTSO]);
			vy = 0;
			vn = 0;
			va = 0;
			vnv = 0;
		}
		clog("Clara.IRBMeeting.Activity.getMotionStore(): returning data",data);
		return new Ext.data.ArrayStore({
			idIndex: 0,
			fields: ['timestamp','motion','adultrisk','pediatricrisk','reviewtype','reviewperiod','madeby','secondedby','madebyid','secondedbyid','yesvotes','novotes','abstainvotes','notvotingvotes','consentwaived','consentdocumentationwaived','hipaa','hipaawaived','ncdetermination','ncreportable','UPIRTSO'],
			data: data
		});
	};
	
	this.toXML= function(){
		var xml = "<item ts='"+this.timestamp+"' agendaitemid='"+this.agendaitemid+"' type='"+this.type+"' lettersent='"+this.lettersent+"' protocolid='"+this.protocolid+"' protocolformid='"+this.protocolformid+"'>";
		xml += "<notes>"+Encoder.htmlEncode(this.notes)+"</notes>";
		if (this.motions.length > 0){
			xml = xml + "<motions>";
			for (var i=0; i<this.motions.length;i++){
				xml = xml + this.motions[i].toXML();
			}
			xml = xml + "</motions>";
		}
		return xml + "</item>";
	};
};

Clara.IRBMeeting.Motion = function(o){
	this.timestamp = 			(o.timestamp || '');
	this.value = 				(o.value || '');
	this.reviewperiod = 		(o.reviewperiod || 0);
	this.reviewtype = 			(o.reviewtype || '');
	this.adultrisk = 			(o.adultrisk || '');
	this.pediatricrisk = 		(o.pediatricrisk || '');
	this.consentwaived = 		(o.consentwaived || '');
	this.consentdocumentationwaived = 		(o.consentdocumentationwaived || '');
	this.hipaawaived =	 		(o.hipaawaived || '');
	this.hipaa =	 			(o.hipaa || '');
	this.ncdetermination=		(o.ncdetermination || '');
	this.ncreportable=			(o.ncreportable || '');
	this.UPIRTSO=				(o.UPIRTSO || '');
	this.madebyid = 			(o.madebyid || 0);
	this.madebyname = 			(o.madebyname || '');
	this.secondbyid = 			(o.secondbyid || 0);
	this.secondbyname = 		(o.secondbyname || '');
	this.votes = 				(o.votes || []);
	
	this.setVotesFromStore = function(store){
		var th = this;
		th.votes = [];
		store.each(function(rec){
			// [v.userid, v.name,v.value,v.note]
			var tm = Clara.IRBMeeting.GetTime();
			th.votes.push(new Clara.IRBMeeting.Vote({
				userid:		rec.data.userid,
				timestamp:	tm,
				name:		rec.data.name,
				value:		rec.data.value,
				note:		rec.data.note
			}));
		});
		Clara.IRBMeeting.MessageBus.fireEvent('votechanged', this);  
	};
	
	this.setAllVotes= function(vote){

		this.votes = [];
		
		var tm = Clara.IRBMeeting.GetTime();
		
		for (var j=0;j<meeting.attendance.length;j++){
			var a = meeting.attendance[j];
			if (a.isAvailable()){
				this.votes.push(new Clara.IRBMeeting.Vote({
					userid:		a.userid,
					timestamp:	tm,
					name:		a.fname+" "+a.lname,
					value:		vote
				}));
			} else {
				this.votes.push(new Clara.IRBMeeting.Vote({
					userid:		a.userid,
					timestamp:	tm,
					name:		a.fname+" "+a.lname,
					value:		Clara.IRBMeeting.Types.Vote.NOT_VOTING,
					note:		a.currentStatus().note
				}));
			}
		}
		
		 Clara.IRBMeeting.MessageBus.fireEvent('votechanged', this);  
	};
	
	this.getVoteStore= function(attendance){
		
		var data = [];
		
		if (this.votes.length == 0){
			// Create array of available voters
			for (var j=0;j<attendance.length;j++){
				var a = attendance[j];
				if (a.isAvailable()){
					data.push([a.userid, a.fname+" "+a.lname,'','']);
				} else {
					data.push([a.userid, a.fname+" "+a.lname, Clara.IRBMeeting.Types.Vote.NOT_VOTING,a.currentStatus().note]);
				}
			}
		} else {
			for (var i=0; i<this.votes.length;i++){
				var v = this.votes[i];
				data.push([v.userid, v.name,v.value,v.note]);
			}
		}
		return new Ext.data.ArrayStore({
			idIndex: 0,
			fields: ['userid','name','value','note'],
			data: data
		});
	};
	
	this.toXML= function(){
		var xml = "<motion ts='"+this.timestamp+"' value='"+Encoder.htmlEncode(this.value)+"' reviewtype='"+this.reviewtype+"' UPIRTSO='"+this.UPIRTSO+"' ncdetermination='"+this.ncdetermination+"' ncreportable='"+this.ncreportable+"' reviewperiod='"+this.reviewperiod+"' adultrisk='"+this.adultrisk+"' pedrisk='"+this.pediatricrisk+"' mid='"+this.madebyid+"' consentwaived='"+this.consentwaived+"' consentdocumentationwaived='"+this.consentdocumentationwaived+"' hipaawaived='"+this.hipaawaived+"' hipaa='" + this.hipaa + "' mname='"+Encoder.htmlEncode(this.madebyname)+"' sid='"+this.secondbyid+"' sname='"+Encoder.htmlEncode(this.secondbyname)+"'>";
		if (this.votes.length > 0){
			xml = xml + "<votes>";
			for (var i=0; i<this.votes.length;i++){
				xml = xml + this.votes[i].toXML();
			}
			xml = xml + "</votes>";
		}
		return xml + "</motion>";
	};
};

Clara.IRBMeeting.Vote = function(o){
	this.userid = 				(o.userid || 0);
	this.timestamp = 			(o.timestamp || '');
	this.name = 				(o.name || '');
	this.value = 				(o.value || '');
	this.note = 				(o.note || '');
	
	this.toXML= function(){
		return "<vote userid='"+this.userid+"' name='"+this.name+"' ts='"+this.timestamp+"' value='"+this.value+"' note='"+Encoder.htmlEncode(this.note)+"'/>";
	};
};


Clara.IRBMeeting.Attendance = function(o){
	this.userid = 				(o.userid || 0);
	this.lname = 				(o.lname || '');
	this.fname = 				(o.fname || '');
	this.status = 				(o.status || []);
	
	
	// MODIFIFED wasPresent to check LAST status of either PRESENT or ABSENT. Ignores OUT_OF_ROOM.
	this.wasPresent= function(){
		var present = false;
		for (var i=0; i<this.status.length;i++){
			if (this.status[i].value == "PRESENT"){
				present = true;
			} else if (this.status[i].value == "ABSENT"){
				present = false;
			}
		}
		return present;
	};
	
	this.isAbsent= function(){
		return !this.wasPresent();
	};
	
    this.currentStatus= function(){
		var maxi = 0;
		var maxts = 0;
		for (var i=0; i<this.status.length;i++){
			var s = this.status[i];
			if (s.timestamp > maxts){
				maxts = s.timestamp;
				maxi = i;
			}
		}
		return this.status[maxi];
	};
	
	this.changeStatus= function(status,note){
		this.status.push(new Clara.IRBMeeting.MemberStatus({timestamp:Clara.IRBMeeting.GetTime(), value:status, note:note}));
	};
	
	this.isAvailable= function(){
		return (this.currentStatus().value == 'PRESENT');
	};
	
	this.toXML= function(){
		var meetingstatus = (this.wasPresent())?"PRESENT":"ABSENT";
		var xml = "<member meetingstatus='"+meetingstatus+"' uid='"+this.userid+"' fname='"+Encoder.htmlEncode(this.fname)+"' lname='"+Encoder.htmlEncode(this.lname)+"'>";
		if (this.status.length > 0){
			xml = xml + "<statuses>";
			for (var i=0; i<this.status.length;i++){
				xml = xml + this.status[i].toXML();
			}
			xml = xml + "</statuses>";
		}
		return xml + "</member>";
	};
};

Clara.IRBMeeting.MemberStatus = function(o){
	// "value" can be: PRESENT, ABSENT, AWAY
	this.timestamp = 			(o.timestamp || '');
	this.value = 				(o.value || '');
	this.note = 				(o.note || '');
	this.toXML= function(){
		return "<status ts='"+this.timestamp+"' value='"+this.value+"' note='"+Encoder.htmlEncode(this.note)+"'/>";
	};
};

