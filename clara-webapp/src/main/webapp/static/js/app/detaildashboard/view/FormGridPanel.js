Ext.define('Clara.DetailDashboard.view.FormGridPanel', {
	extend: 'Ext.grid.GridPanel',
	alias: 'widget.formgridpanel',
	layout:'fit',
	border:true,
	viewConfig: {
		stripeRows: true,
		trackOver:false
	},
	loadMask:true,
	store: 'Clara.DetailDashboard.store.Forms',
	emptyText:'<div class="gridpanel-error">There was a problem finding the forms for this'+claraInstance.type+'. Please try refreshing this page in a few moments.</div>',
	dockedItems: [{
		dock: 'top',
		border:false,
		xtype: 'toolbar',
		items: [{
			iconCls:'icn-application--plus',
			text:'New Form..',
			id:'btnNewForm'
		}]
	}],
	
	initComponent: function() { 
		var me = this;
		
		me.columns = [{
			header: 'Form',
			flex:1,
			sortable: true, 
			dataIndex: 'formtype', 
			renderer:function(value, p, record){

	        	var locked = record.get("locked"),
	        	    url=record.get("url"),
	        	    formType = record.get("formtype"),
	        	    formIndex= (record.get("formIndex") > 0)?(" #"+record.get("formIndex")):"";
	        	    str = (locked === "true")?"<div class='form-row form-row-locked'>":"<div class='form-row'>";
	        	
	    		str += (record.get("studynature") != "")?("<span class='protocol-form-row-field protocol-form-type'>"+formType+formIndex+"</span><div class='studynature'>"+(record.get("studynature").replace("-"," "))+"</div>"):("<span class='protocol-form-row-field protocol-form-type'>"+formType+formIndex+"</span>");
	    		str += (locked === "true")?"<div style='font-weight:800;border:1px solid red; padding:4px;margin-top:2px;'>"+record.get("lockMessage")+"</div>":"";
	    		
	    		var dst = record.itemDetails();
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
			}
		},
        {header: 'Status', id:'protocol-forms-status-column', width:200, sortable: true, dataIndex: 'status', 
        	renderer:function(v,p,record){
        		var html = "<div class='wrap'><span class='protocol-form-row-field protocol-form-status'>"+v+"</span>";
        		if (record.get("agendaDate")) {
        			var dateClass = "label-info";
        			if (moment(record.get("agendaDate")).add('days',1) < moment(new Date()).add('days',0)) dateClass = "label-disabled";
        			html += "<div class='protocol-form-row-field label "+dateClass+"'>On IRB agenda: "+moment(record.get("agendaDate")).format('L');+"</div>";
        		}
        		return html+"</div>";
        	}
        },
		{header: 'Last Modified', width: 95, sortable: true, renderer: function(value) { return "<span class='protocol-form-row-field'>"+Ext.util.Format.date(value,'m/d/Y')+"</span>";}, dataIndex: 'statusModified'},
        {
			header : 'Assigned To',
			dataIndex : 'assignedReviewers',
			sortable : true,
			width : 230,
			renderer : function(v,p,r) {
				
				var st = r.assignedReviewers();
      		  var html = "<ul class='form-list-row form-assigned-reviewers'>";

      		  st.each(function(r){
      			  html += "<li class='form-assigned-reviewer'>"+Clara_HumanReadableRoleName(r.get("reviewerRoleName"))+": "+r.get("reviewerName") + "</li>";
      		  });

      		  return html + "</ul>";
			}
		}];

		me.callParent();

	}
});