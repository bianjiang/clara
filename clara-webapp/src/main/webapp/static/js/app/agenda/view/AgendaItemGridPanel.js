Ext.define('Clara.Agenda.view.AgendaItemGridPanel', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.agendaitemgridpanel',
	requires:['Ext.grid.*','Ext.data.*','Ext.dd.*'],
	autoScroll: true,
    border: true,
    stripeRows: true,
    hideHeaders:false,
    store: 'AgendaItems',
    multiSelect: false,

	initComponent: function() { 
		var me = this;
		me.viewConfig = {
				plugins: {
		            ptype: 'gridviewdragdrop',
		            dragText: 'Drag and drop to reorganize'
		        },
		        listeners: {
		            drop: function(node, data, dropRec, dropPosition) {
		                me.fireEvent("itemOrderChanged");
		            }
		        },
			getRowClass: function(record, index){
				return (record.assignedReviewers().count() == 0 && record.get('category') == 'FULL_BOARD')?'agenda-item-row-noreviewers':'';
			},
			emptyText:'<div class="wrap" style="padding:8px;"><h1>There are no agenda items assigned to this date.</h1>Items are assigned by the IRB office once they have been approved by the appropirate committees.</div>'
		};
        me.columns = [
		new Ext.grid.RowNumberer({width:48}),
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
				if (r.get("xmlTitle") && r.get("xmlTitle") != "") return "<div class='agenda-list-row wrap'><h1>"+r.get("xmlTitle")+"</h2></div>";
				else return (r.get("protocolFormStatus") != 'Assigned to an IRB Agenda')?("<div class='agenda-list-row wrap'><h1>"+v+"</h1><h2>Status: "+r.get("protocolFormStatus")+"</h2></div>"):("<div class='agenda-list-row wrap'><h1>"+v+"</h1></div>");
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