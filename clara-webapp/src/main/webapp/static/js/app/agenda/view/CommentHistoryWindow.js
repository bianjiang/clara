Ext.define('Clara.Agenda.view.CommentHistoryWindow', {
	extend: 'Ext.window.Window',
	alias: 'widget.commenthistorywindow',
	layout: 'fit',
	title: 'Comment History',
	modal:true,
	width:550,
	height:450,

    listeners:{
    	show: function(w){
    		clog("show listener start");
			var controller = Clara.Agenda.app.getController("Agenda");
    		w.down("grid").getStore().loadHistory(controller.selectedAgendaItem.get("id"));
    	}
    },
	items: [{
		xtype:'grid',
		hideHeaders: false,
		title:null,
		emptyText:"No comments found.",
		store:'Clara.Agenda.store.CommentHistory',
		border:false,
		viewConfig: {
			stripeRows: true,
			trackOver:false
		},
		loadMask:true,
		columns: [
			    {
		            header: 'Date', width: 135,fixed:true, dataIndex: 'timestampDate', xtype: 'datecolumn', format: 'm/d/Y h:ia'
		        },
		        {
		        	header: 'Note', dataIndex: 'desc',flex:1,renderer:function(v,p,r){
		        		return"<div class='wrap'>"+v+"</div>";
		        	}
		        }]
			    	
			    
		}]
		
	
});