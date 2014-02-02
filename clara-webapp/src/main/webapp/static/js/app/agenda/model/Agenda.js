Ext.define('Clara.Agenda.model.Agenda', {
    extend: 'Ext.data.Model',
    fields: [{name:'id', mapping: 'id'},
     	    {name:'agendaStatus', mapping: 'agendaStatus'},
    	    {name:'agendaStatusDesc', mapping: 'agendaStatusDesc'},
    	    {name:'agendaLongStatusDesc', mapping: 'agendaStatusDesc', convert: function(enumStatus){
    	    	var st = enumStatus || "Unknown";
    	    	if (st != 'Unknown') st = enumStatus.charAt(0).toUpperCase() + enumStatus.slice(1).toLowerCase().replace(/_/g," ");
    	    	if (st == 'Agenda incomplete') st = "New agenda";
    	    	else if (st == 'Agenda approved') st = "Approved by chair";
    	    	return st;
    	    }},
    	    {name:'date', mapping: 'date', type:'date', dateFormat:'Y-m-d'},
    	    {name:'irbRoster', mapping: 'irbRoster'}],
    proxy: {
        type: 'ajax',
        url: appContext + '/ajax/agendas/list',
        reader: {
            type: 'json',
			idProperty: 'id',
			root:'data'
        }
    }
});