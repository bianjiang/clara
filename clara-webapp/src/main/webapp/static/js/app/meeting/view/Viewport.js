Ext.define('Clara.Meeting.view.Viewport',{
	extend: 'Ext.container.Viewport',
	border:false,
	requires:[],
	layout:'fit',

	initComponent: function(){
		this.items = [
		              {
		            	  xtype:'panel',
		            	  layout:'border',
		            	  boder:false,
		            	  items:[{
		            		  xtype:'tabpanel',
		            		  region:'center',
		            		  split:false,
		            		  border:false,
		            		  id:'meetingTabPanel',
		            		  items: [{
		            			  title: 'Contingencies / Notes'
		            		  }, {
		            			  title: 'Motions'
		            		  }, {
		            			  title: 'Transcription'
		            		  }]

		            	  },
		            	  {
		            		  xtype:'panel',
		            		  region:'west',
		            		  collapsible:true,
		            		  title:'Agenda Items',
		            		  split:true,
		            		  width:295,
		            		  id:'agendaItemsPanel'
		            	  }],
		            	  dockedItems: [{
		            		  xtype:'toolbar',
		            		  dock:'top',
		            		  items:[{
		            			  xtype:'button',
		            			  id:'btnBackToClara',
		            			  iconCls:'icn-sticky-note',
		            			  text:'CLARA'
		            		  },'->',
		            		  {
		            			  xtype : 'button',
		            			  text : 'Attendance',
		            			  id : 'btnAttendance',
		            			  iconCls : 'icn-book',
		            			  disabled : true
		            		  },
		            		  {
		            			  xtype : 'button',
		            			  text : 'Announcements',
		            			  id : 'btnAnnouncements',
		            			  iconCls : 'icn-clipboard-search-result'
		            		  },
		            		  {
		            			  xtype : 'button',
		            			  text : 'Start Meeting',
		            			  id : 'btnStartMeeting',
		            			  iconCls : 'icn-user--arrow',
		            			  disabled : true
		            		  },{
		            			  xtype:'button',
		            			  text:'Stop Meeting',
		            			  id:'btnStopMeeting',
		            			  iconCls:'icn-calendar-next',
		            			  disabled : true
		            		  },'-',{
		            			  xtype:'tbtext',
		            			  id:'txtMeetingStatus',
		            			  text:'Not started yet.'
		            		  }]
		            	  }],
		              }
		              ];
		this.callParent();
	}


});