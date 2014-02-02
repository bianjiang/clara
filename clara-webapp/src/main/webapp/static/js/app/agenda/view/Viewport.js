Ext.define('Clara.Agenda.view.Viewport',{
	extend: 'Ext.container.Viewport',
	border:false,
	layout:'fit',
	defaults:{
		split:true,
		border:false,
		collapsible:false
	},
	initComponent: function(){
		this.items={
				layout:'border',
				items:[{
						dock:'top',
						xtype:'panel',
						contentEl:'clara-header',
						region:'north',
						bodyCls:'background-normal',
						border:0
					},{
						xtype:'panel',
						layout:'border',
						border:false,
						region:'center',
						items:[{
							xtype:'agendagridpanel',
							region:'west',
							width:280,
							split:true,
							dockedItems: [{
								dock: 'top',
								border:false,
								xtype: 'toolbar',
								items: [{
									text:'New Agenda..',
									id:'btnNewAgenda',
									iconCls:'icn-plus-button',
									hidden:!(claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN']))
								}
								]
							}]
							
						
						},{
							xtype:'agendaitemgridpanel',
							region:'center',
							dockedItems: [{
								dock:'bottom',
								border:false,
								xtype:'toolbar',
								items:[{   xtype:'tbtext',text:'<div style="width:100%;text-align:center;"><span id="agenda-item-count"></span><span id="agenda-item-details">No agenda selected.</span></div>',flex:1},{
									xtype:'checkbox',
									boxLabel:'Include "Reported" items <strong>(may be very slow)</strong>',
									value:false,
									id:'cbShowReportedItems'
								}]
							},{
								dock: 'top',
								border:false,
								xtype: 'toolbar',
								items: [
									{
										
										id:'btnStartMeeting',
										iconCls:'icn-projection-screen-presentation',
										action:'show_meeting_page',
										text:'<strong>Show Meeting Page</strong>',
										hidden:true
									
									},{
								
										id:'btnShowSummary',
										iconCls:'icn-document-list',
										action:'show_agenda',
										text:'<strong>Summary</strong>',
										hidden:true
									},{
								
										id:'btnShowMinutes',
										iconCls:'icn-report',
										action:'show_minutes',
										text:'<strong>Minutes</strong>',
										hidden:true
									},
									'->',{
											xtype:'button',
											id:'btnAgendaMenu',
											hidden:!(claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN'])),
											iconCls:'icn-calendar-select',
											disabled:true,
											text:'Agenda',
											menu:[
											      
									{
									id:'btnAddMinutesAgenda',
									iconCls:'icn-report--plus',
									text:'<strong>Add last meeting\'s minutes</strong>',
									hidden:!(claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN'])),
									disabled:true
									},{
									id:'btnSendAgenda',
									iconCls:'icn-mail-send',
									text:'<strong>Send Agenda..</strong>',
									disabled:true
									},'-',
											      
											      
											{
									
												id:'btnManageAgendaRoster',
												iconCls:'icn-users',
												text:'Manage Roster..',
												disabled:true,
												hidden:!(claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN']))
											},{
									
												id:'btnCancelAgenda',
												iconCls:'icn-calendar--minus',
												text:'Cancel..',
												disabled:true
											},{
									
												id:'btnRemoveAgenda',
												iconCls:'icn-minus-button',
												text:'Remove..',
												disabled:true
											}]
										},{
											xtype:'button',
											id:'btnAgendaItemMenu',
											hidden:!(claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN','ROLE_IRB_OFFICE_CHAIR'])),
											iconCls:'icn-calendar-task',
											disabled:true,
											text:'Agenda Item',
											menu:[{
													id:'btnAssignReviewers',
													hidden:!(claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN','ROLE_IRB_OFFICE_CHAIR','ROLE_IRB_CHAIR'])),
													iconCls:'icn-user--plus',
													text:'Assign reviewers',
													disabled:true
												},{
													id:'btnRemoveItem',
													iconCls:'icn-calendar--minus',
													hidden:!(claraInstance.HasAnyPermissions(['ROLE_IRB_OFFICE','ROLE_SYSTEM_ADMIN'])),
													text:'Remove item from agenda..',
													disabled:true
												}
												]
										},{
											xtype:'button',
											id:'btnApproveAgenda',
											iconCls:'icn-thumb-up',
											hidden:true,
											text:'<span style="font-weight:800;">Approve Agenda...</span>'
										},'-',{
											xtype:'button',
											id:'btnPrintAgenda',
											tooltip:'Print list (opens new window)',
											tooltipType:'title',
											iconCls:'icn-printer',
											disabled:true
										}
								]
							}]
						}]
					}]
				
		};
		this.callParent();
	}

});