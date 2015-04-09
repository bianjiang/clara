Ext.define('Clara.Common.view.ContractGridPanel', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.commoncontractgridpanel',
	autoScroll: true,
	cls:'contractgridpanel',
	stripeRows: true,
	hideHeaders:false,
	pageSize:25,
	clickableRows:false,
	includeQueueStatus:false,
	store: 'Clara.Common.store.Contracts',
	features: [{ftype:'grouping', collapsible:false, groupHeaderTpl:'<div></div>'}],
	initComponent: function() { 
		var me = this;
		me.viewConfig = {
				trackOver:false
		};
		me.border=true;
		me.columns = [
{
	id: 'contract-col-id',
	hidden:true,
	dataIndex: 'contractIdentifier'
},
{
	resizable:true,
	flex:2,
	header:'Contract',
	sortable:true,
	dataIndex: 'contractType',
	renderer:function(v,p,r){
		if (r.get("contractType") == "New Contract"){
			var s = (me.clickableRows)?("<a href='"+appContext+"/contracts/"+r.get("id")+"/dashboard' style='font-weight:800;'>"+r.get("identifier")+"</a>"):r.get("identifier");
			s += " "+Clara_HumanReadableType(r.get("contractEntityTypeDesc").replace(/-/gi," "));
			return "<div class='wrap'>"+s+"</div>";
		} else if (r.get("contractType") == "Amendment"){
			var s = (me.clickableRows)?("<a href='"+appContext+"/contracts/"+r.get("id")+"/dashboard' >"+r.get("identifier")+"</a>"):r.get("identifier");
			return "<div class='contractgridpanel-amendment'>"+v + " " +r.get("formIndex")+" for "+s+"</div>";
		} 
		else {
			return v;
		}
	}
},
{
	resizable:true,
	header:'IRB #',
	width:65,
	sortable:true,
	dataIndex: 'studyIdentifier',
	renderer:function(v,p,r){
		return "<a href='"+appContext+"/protocols/"+v+"/dashboard' style='font-weight:100;'>"+v+"</a>";
	}
},
{
	resizable:true,
	width:100,
	sortable:true,
	header:'PI',
	renderer:function(v,p,r){
		var sm = r.staffMembers();
		var html = "";
			sm.each(function(rec){
				var isPI = rec.isPI();
				if (isPI) html+= "<div class='protocol-pi'><strong>"+rec.get("firstname")+" "+rec.get("lastname")+"</strong></div>";
			});
			
			return html;
	}
},
{
	resizable:true,
	flex:1,
	sortable:true,
	header:'Entity',
	renderer:function(v,p,r){
		var sm = r.sponsors();
		var html = "";
		if (sm.getCount() > 0){
			html = "<div class='wrap'><ul>";
			sm.each(function(rec){
				html+= "<li><strong>"+rec.get("company")+"</strong>"+((rec.get("name"))?(": "+rec.get("name")):"")+"</strong></li>";
			});
			return html + "</ul></div>";
		}
			
			
			return html;
	}
},
{
	resizable:true,
	sortable:true,
	width:100,
	header:'Status',
	dataIndex: 'status',
	renderer: function(v,p,r){
		return (r.get("status") || r.get("formCommitteeStatus") || "Unknown status")+"";
	}
},
{
	resizable:true,
	sortable:true,
	header:'Created',
	dataIndex: 'created',
	renderer:function(v){
		return Ext.util.Format.date(v, 'm/d/Y');
	}
},
{
	header : 'Assigned To',
	sortable : false,
	hidden:false,
	flex:1,
	renderer : function(v,p,r) {
		
		var cm = r.assignedCommittees();
		var html = "";
		if (cm.getCount() > 0){
			html = "<div class='wrap'><ul class='form-list-row form-assigned-reviewers'>";
			cm.each(function(rec){
				var sm = rec.assignedReviewers();
				sm.each(function(reviewerrec){
					html+= "<li class='form-assigned-reviewer'><strong>"+Clara_HumanReadableRoleName(reviewerrec.get("reviewerRoleName"))+"</strong>: "+reviewerrec.get("reviewerName")+"</li>";
				});
			});
			return html + "</ul></div>";
		} else return html;
		
	}
}
		              ];
		me.listeners = {
				added: function(){
					me.getStore().load({
						params:{
							start: 0, 
							limit: me.pageSize, 
							searchCriterias:null
						}
					});
				}	
		};
		me.callParent();

	}
});