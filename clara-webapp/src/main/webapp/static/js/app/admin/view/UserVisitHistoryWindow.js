Ext.define('Clara.Admin.view.UserVisitHistoryWindow', {
	extend: 'Ext.window.Window',
	alias: 'widget.uservisithistorywindow',
	layout: 'border',
	width:950,
	height:550,
	padding: 6,
    modal: true,
    user:{},
    ipAddress:'',
    initComponent: function() {
    	var t = this;
    	if (t.ipAddress !== ''){
    		t.title = "Visit history for "+t.ipAddress;
    	} else {
    		t.title = "Visit history for "+t.user.get("username");
    	}
    	var vStore = Ext.data.StoreManager.lookup('Clara.Admin.store.PiwikUserVisits') || Ext.data.StoreManager.lookup('PiwikUserVisits');
    	
		this.dockedItems = [{
			dock: 'top',
			xtype: 'toolbar',
	    		items:['->',
	    		       {
	           	    	id: 'btnDate',
		           	 	iconCls:'icn-arrow-circle-315',
		           	 	text: 'Refresh',
		           	 	disabled:false,
		           	 	handler: function(){
		           	 		vStore.loadPiwikUserVisits(t.user.get("username"));
		           	 		clog(vStore);
	    				}
		           	 }
		           	]
		}];
    	
		this.buttons = [
		    {
		        text: 'Close',
		        handler: function(){
		        	t.close();
		        }
		    }
		];
        this.items = [{
        	region:'north',
        	id:'piwikuservisitpanel',
        	height:200,
        	split:true,
        	xtype:'grid',
        	border:false,
        	store:vStore,
        	columns: [
				{ header: 'Date', sortable: true, dataIndex: 'visitDateTime', id:'visit-date',width:150 },
				{ header: 'IP', sortable: true, dataIndex: 'ip',width:100 },
				{ header: 'User', hidden:false, sortable: true, dataIndex: 'username',width:100 },
				{ header: 'Duration', sortable: true, dataIndex: 'visitDurationPretty',width:100 },
				{ header: 'Accessed from', sortable: true, dataIndex: 'osName',flex:1, renderer: function(v,p,r){
					return v+" ("+r.get("browser")+") from "+r.get("provider");
				} }
			]
        },{
        	id:'piwikuservisitdetailpanel',
        	region:'center',
        	split:true,
        	xtype:'grid',
        	emptyText:'Click on a visit above to see details',
        	viewConfig: { deferEmptyText:false},
        	border:false,
        	columns: [
				{ header: 'Time', sortable: false, dataIndex: 'timePretty', width:120, renderer: function(v,p,r){
					if (r.get("timeSpentPretty") && r.get("timeSpentPretty") != "") return "<div class='wrap'>"+v+" ("+r.get("timeSpentPretty")+")"+"</div>";
					else return v;
				} },
				{ header: 'Page', sortable: false, dataIndex: 'url',flex:2},
				{ header: 'Event', sortable: false, dataIndex: 'eventCategory',flex:1, renderer:function(v,p,r){
					return "<div class='wrap'>"+v+": "+r.get("eventAction")+"</div>";
				}}
			]
        }];
        
        if (t.ipAddress !== ''){
        	vStore.loadPiwikIPAddressVisits(t.ipAddress);
    	} else {
    		vStore.loadPiwikUserVisits(t.user.get("username"));
    	}
        
        this.callParent();
    }
});