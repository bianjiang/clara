Ext.define('Clara.Reports.view.ReportResultsWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.reportresultswindow',
    title: 'Results',
    width:400,
    height:400,
    
    style : 'z-index: -1;', // IE8 fix (http://www.sencha.com/forum/archive/index.php/t-241500.html?s=15ad65f757fb7325aa20735e3226faab)
    
    iconCls:'icn-report',
    layout: {
        type: 'fit'
    },
    
    initComponent: function() {
    	var t = this;
    	t.listeners = {
    			afterrender:function(){
    			    this.modal = true;
    			}
    	};
    	t.items = [{
        	xtype:'grid',
        	border:false,
        	store:t.resultStore,
        	columns: [
        	          { text: 'Completed',  dataIndex: 'created',flex: 2, renderer:function(v){
        	        	  return moment(v).format('MM/DD/YYYY h:mm a');
        	          } },
        	          { text: 'Action', dataIndex: 'id', flex:2, renderer:function(v){
        	        	  var html = "<a href='javascript:;' onClick='window.open(\""+appContext+"/reports/results/"+v+"/view\");'>View</a>";
        	        	  html += " - <a href='"+appContext+"/reports/results/"+v+"/download.xls'>Download Excel</a>";
        	        	  return html;
        	          }}
        	      ],
        }];
        t.buttons = [{
        	text:'Close',
        	handler:function(){
        		t.close();
        	}
        }];
        t.callParent();
        t.resultStore.load();
    }
});