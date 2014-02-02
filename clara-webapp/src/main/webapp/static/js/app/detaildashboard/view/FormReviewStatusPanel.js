Ext.define('Clara.DetailDashboard.view.FormReviewStatusPanel',{
	extend: 'Ext.grid.GridPanel',
	alias: 'widget.formreviewstatuspanel',
	requires:['Clara.DetailDashboard.view.FormReviewStatusDetailWindow'],
	
	border:false,
	viewConfig: {
		stripeRows: true,
		trackOver:false,
		emptyText:'No status found for this form.'
	},
	loadMask:true,
	store: 'Clara.DetailDashboard.store.FormReviewStatus',
	showActions:true,
	form:null,
	initComponent: function(){
		var me = this;
		me.listeners = {
			afterrender:function(p){
				var st = Ext.data.StoreManager.lookup('Clara.DetailDashboard.store.FormReviewStatus');
				st.loadFormReviewStatus(me.form || Clara.Application.FormController.selectedForm);
			},
			itemdblclick:function(g,rec){
				Ext.create("Clara.DetailDashboard.view.FormReviewStatusDetailWindow",{
					form: (me.form || Clara.Application.FormController.selectedForm),
					statusRecord: rec
				}).show();
			}
		};
		
		me.columns = [{
			flex:1,
			dataIndex:'status',
			renderer: function(v,p,r){

				var row = "";
				var committeeHtml = (r.get("committee"))?("<span style='font-weight:100;'>"+r.get("committee")+" <span style='color:#6c6680'>&gt;</span></span> "):"";
				var status = (r.get("status") || "Unknown status");
				var ccStatus = r.get("priority").toLowerCase();
				row = "<div class='form-committeestatus-row'>";
				row +="<div class='form-committeestatus-date'>"+Ext.util.Format.date(r.get("modified"),'m/d/Y h:ia')+"</div>";
				row += "<div class='protocol-metadata'><div class='protocol-status protocol-status-"+ccStatus+"'>"+committeeHtml+status+"</div><div class='protocol-type'></div></div>";
				
				if (me.showActions){

					var dst = r.itemActions();
		      		  if (dst.count() > 0) {
		      			row += "<ul class='form-committeestatus-actions'>";
		      			 dst.each(function(rec){
		      				row += "<li><a href=\""+rec.get("url")+"\">"+rec.get("name")+"</a></li>";
		      			 });
		      			row += "</ul>";
		      		  }
					
				}
				clog("ROW",row);
				return row + "</div>";
			}
		}];
		
		me.callParent();
	}

	
});