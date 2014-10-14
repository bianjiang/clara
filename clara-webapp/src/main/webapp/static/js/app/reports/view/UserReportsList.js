Ext.define('Clara.Reports.view.UserReportsList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.userreportslist',
    store: 'UserReports',
    hideHeaders: false,
    stripeRows:true,
    initComponent: function() {
        this.columns = [{
        	header:'Created',
        	dataIndex:'created',
        	renderer:function(v,m,r){
            	return Ext.Date.format(v, "n/j/Y");
        	},
        	width:100
        },{
        	header:'Status',
        	dataIndex:'status',
        	width:80,
        	renderer:function(v){
        		return (v == "NOT_READY")?"Pending":"Complete";
        	}
        },{
            dataIndex: 'description',
            header:'Name',
            renderer:function(v,m,r){
            	var html = "<div class='reportlist-row'>"+"<h1>"+v+"</h1>",
            		criteria = r.userReportCriteria(),
            		str = "<dl class='reportlist-criteria'>";
            	
				if (criteria.count() > 0) {

					criteria.each(function(rec){
						if (i > 0) str+="<br/>";
						i++;
						str += "<dt>" + rec.get("fieldlabel") + "</dt>";
						str += "<dd><span class='criteria-operator'>" + rec.get("operator") + "</span> <span class='criteria-value'>'" + rec.get("displayvalue") + "'</span></dd>";
					});
					
				} else {
					str += "<dt class='no-criteria'>No criteria selected</dt>";
				}
            	
				str += "</dl>";
				
				return html + str + "</div>";
            },
            flex: 1
        },{
        	dataIndex: 'id',
        	header:'Actions',
        	
        	renderer:function(v,m,r){
        		// var html = ((r.get("status") != "NOT_READY")?"<a href='"+appContext+"/reports/"+r.get("id")+"/view' target='_blank'><span style='font-weight:800;'>Open</span></a> | ":"");
        		var html = (r.get("status") != "NOT_READY")?("<a id='openaction-"+r.get("id")
        				+"' class='openaction' style='font-weight:800;' href='javascript:;' onclick='Clara.Reports.app.getController(\"UserReport\").showResultsWindowForReport("
        				+r.get("id")+");'>Show Results</a> | "):"";
        		html += "<a id='deleteaction-"+r.get("id")+"' class='deleteaction' href='javascript:;' onclick='Clara.Reports.app.getController(\"Report\").deleteReport("+r.get("id")+");'>Remove</a>";
        		return html;
        	},
        	
        	width:200
        }];
        
        this.viewConfig = {
            getRowClass: function(r){return 'user-report-row user-report-row-'+r.get("status");}
        };
        
        this.dockedItems = [{
            dock: 'top',
            xtype: 'toolbar',
            items: [{
                xtype: 'button',
                text: 'Create Report..',
                action: 'generate-report',
                iconCls:'icn-gear--plus'
            },'->',{
                xtype: 'button',
                text: 'Refresh',
                action: 'refresh',
                iconCls:'icn-arrow-circle'
            } ]
        }];
        
        this.callParent();
    }
});