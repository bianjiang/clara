Ext.define('Clara.Meeting.view.AgendaItemGridPanel', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.agendaitemgridpanel',
	requires:['Ext.grid.*','Ext.data.*','Ext.dd.*'],
	autoScroll: true,
    border: false,
    stripeRows: true,
    hideHeaders:true,
    store: 'Clara.Meeting.store.ActivityItems',
    multiSelect: false,

	initComponent: function() { 
		var me = this;
		me.viewConfig = {
			getRowClass: function(record, index){
				return (record.assignedReviewers().count() == 0 && record.get('category') == 'FULL_BOARD')?'agenda-item-row-noreviewers':'';
			},
			emptyText:'<div class="wrap" style="padding:8px;"><h1>There are no agenda items assigned to this date.</h1>Items are assigned by the IRB office once they have been approved by the appropirate committees.</div>'
		};
        me.columns = [
			{header:'#',dataIndex:'protocolId',menuDisabled:true, sortable:true,width:55,renderer:function(v,p,r){
					return "<span class='agenda-list-row-id'>"+v+"</span>";
				}
			},
			
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
	        },
			
			
			
			{header: 'Category', renderer: function(v){ return "<div class='agenda-list-row agenda-category'>"+v+"</div>"; }, sortable: true, dataIndex: 'categoryLongStatusDesc',id:'col-agenda-item-row-category'},
			{header: 'Type', width:245, sortable: true, dataIndex: 'protocolFormType',renderer:function(v,p,r){
				var str = "<div class='agenda-list-row'><h1>"+v+"</h1><h2>"+(Clara_NameMappings.Protocol.studyNature[r.get("studyNature")] || "")+"</h2>";

					var dst = r.itemDetails();
					if (dst.count() > 0) {
						str += "<dl class='protocol-form-row-details'>";
					
						dst.each(function(rec){
							if (i > 0) str+="<br/>";
							i++;
							str += "<dt>" + rec.get("detailName") + "</dt>";
							str += "<dd>" + rec.get("detailValue") + "</dd>";
						});
						str += "</dl>";
					}
				return str+"</div>";
			}},
			{header: 'IRB #', sortable: true, width:60, dataIndex: 'protocolId',renderer:function(v,p,r){return "<div class='agenda-list-row'>"+(v?v:"")+"</div>";}},
			{header: 'Protocol Name',flex:1, sortable: true, dataIndex: 'protocolTitle',id:'col-agenda-item-row-title',renderer:function(v,p,r){
				if (r.get("xmlTitle") && r.get("xmlTitle") != "") return "<div class='agenda-list-row'><h1>"+r.get("xmlTitle")+"</h2></div>";
				else return (r.get("protocolFormStatus") != 'Assigned to an IRB Agenda')?("<div class='agenda-list-row wrap'><h1>"+v+"</h1><h2>Status: "+r.get("protocolFormStatus")+"</h2></div>"):("<div class='agenda-list-row'><h1>"+v+"</h1></div>");
				}
			},
			{header: 'Reviewers', hidden:!(claraInstance.HasAnyPermissions(['ROLE_RESEARCH_COMPLIANCE','ROLE_ACHRI_REVIEWER','ROLE_IRB_REVIEWER','ROLE_IRB_OFFICE','ROLE_IRB_CHAIR','ROLE_SYSTEM_ADMIN','ROLE_IRB_COMMITTEE_CHAIR'])),width:190, sortable: true, dataIndex:'protocolFormStatus', renderer:function(value, p, record){
				var st = record.assignedReviewers();
				var html = "<div class='agenda-list-row agenda-row-reviewers'>";
				if (st.count() == 0){
					if (record.get('category') == 'FULL_BOARD') html += "<span style='font-weight:800;color:red;'>No reviewers assigned.</span>";
					html += "</div>";
				} else {
					var i=0;
					st.each(function(r){
						if (i > 0) html+="<br/>";
						i++;
						html += "<span class='agenda-row-reviewer'>"+r.get("name")+"</span>";
					});
				}
				return html + "</div>";
			}
			},{
				header:'',
				dataIndex:'id',
				renderer: function(v,p,r){
					if (claraInstance.HasAnyPermissions(['VIEW_AGENDA_ONLY','ROLE_SYSTEM_ADMIN','ROLE_IRB_COMMITTEE_CHAIR','VIEW_AGENDA_ITEM'])){
						url = appContext+"/agendas/"+Clara.Agenda.app.getController("Agenda").selectedAgenda.get("id")+"/agenda-items/"+v+"/view";
						if (r.get("xmlUrl") && r.get("xmlUrl") != "") url = appContext+r.get("xmlUrl");
						return "<div class='icn-arrow' style='background-repeat:no-repeat;padding-left:20px;font-size:12px;'><a href='"+url+"' target='_blank'>View</a></div>";
					}
				}
			}
		];

		me.callParent();
		
	}
});