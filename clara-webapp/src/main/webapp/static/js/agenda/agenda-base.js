Ext.ns('Clara.Agenda');

Clara.Agenda.MessageBus = new Ext.util.Observable();
Clara.Agenda.MessageBus.addEvents('agendarosterupdated','agendarosteritemselected','rosteritemselected','error','errorassignreviewer','afterassignreviewer','beforeagendaload','agendareviewerselected','agendaitemreviewerselected','agendaloaded','agendaremoved','agendaselected','agendainfoupdated','onagendaloaderror','onagendasaveerror','beforeagendasave','afteragendasave','itemadded','itemremoved','itemupdated');

Clara.Agenda.SelectedAgenda = {};
Clara.Agenda.SelectedAgendaItem = {};

Clara.Agenda.AgendaDate = function(o){
	this.id=				(o.id || '');	
	this.agendaDate=		(o.agendaDate || '');
	this.irbRoster=			(o.irbRoster || '');
	
	this.save = function(){
		Clara.Agenda.MessageBus.fireEvent('beforeagendasave', this);
		var url = appContext+"/ajax/agendas/create";
		var a = this.agendaDate;
		var i = this.irbRoster;
		jQuery.ajax({
			  type: 'GET',
			  async:false,
			  url: url,
			  data: {agendaDate: a, irbRoster:i, userId: claraInstance.user.id},
			  success: function(){
				  Clara.Agenda.MessageBus.fireEvent('afteragendasave', this);  
			  },
			  error: function(){
				  Clara.Agenda.MessageBus.fireEvent('onagendasaveerror', this);  
			  }
		});
	};
};

Clara.Agenda.AgendaItem = function(o){
	this.id=				(o.id || '');	
	this.agendaDate=		(o.agendaDate || '');
	this.protocolFormId=	(o.protocolFormId || 0);
	this.agendaItemCategory=(o.agendaItemCategory || '');
	this.userId=			(o.userId || 0);	
};



