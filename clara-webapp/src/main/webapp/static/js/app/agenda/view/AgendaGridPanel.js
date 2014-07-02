Ext.define('Clara.Agenda.view.AgendaGridPanel', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.agendagridpanel',
	autoScroll: true,
    border: true,
    stripeRows: true,
    hideHeaders:true,
    store: 'Agendas',
	initComponent: function() { 
        this.columns = [
                        {
                        	header:'',sortable:false,dataIndex:'irbRoster',width:14,renderer:function(v,p,r){
                        		return "<span style='color:#999;' data-qtip='Agenda "+Ext.util.Format.date(r.get("date"), 'm/d/y')+", Committee "+v.replace("WEEK_","")+"'>"+v.replace("WEEK_","")+"</span>";
                        	}
                        },
			{
				header: 'Date', sortable: true, dataIndex: 'date', flex:1, renderer:function(value, p, rec){
					return "<div class='agenda-list-row'><div class='wrap'><h1>"+Ext.util.Format.date(rec.get("date"), 'm/d/y')+" <span class='agengaStatus_"+rec.get("agendaStatus")+"'>"+rec.get("agendaLongStatusDesc")+"</span></h1></div><div style='clear:both;'></div>";
				}
			}
        ];
        this.listeners = {
        	added: function(me){
        		me.getStore().load();
        	}	
        };
		this.callParent();
		
	}
});