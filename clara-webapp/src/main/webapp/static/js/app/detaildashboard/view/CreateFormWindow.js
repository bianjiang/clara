Ext.define('AvailableForms',{
	extend: 'Ext.data.Model',
	fields : [
				{
					name : 'id',
					mapping : '@id'
				},
				{
					name : 'type',
					mapping : '@type'
				},
				{
					name : 'title',
					mapping : '@title'
				},
				{
					name : 'description',
					mapping : 'description'
				} ]
});

Ext.define('Clara.DetailDashboard.view.CreateFormWindow', {
	extend: 'Ext.window.Window',
	requires:[],
	alias: 'widget.createformwindow',
	title: 'What kind of form do you want to create?',
	width:650,
	modal:true,
	height:500,

	layout: {
		type: 'fit'
	},

	initComponent: function() {
		var me = this;

		me.items = [{
			xtype:'grid',
			border:false,
			viewConfig: {
				stripeRows: true,
				trackOver:false,
				emptyText:'<h1 style="font-size:2em;">You cannot create a new form at this time.</h1><span>This usually happens when the '+claraInstance.type+' has a form that is currently under review.</span>'
			},
			hideHeaders:true,
			store : new Ext.data.Store(
					{
						proxy : {
									url : appContext+"/ajax/"+claraInstance.type+"s/"+claraInstance.id+"/new-form-types.xml",
									method : "GET",
									type:'ajax',
									headers : {
										'Accept' : 'application/xml;'
									},
									reader: {
										 type:'xml',
										 record:'form',
										 root:'forms'
									 }
								},
						model:'AvailableForms',
						autoLoad : true
						
					}),
			columns: [{
		    	dataIndex: 'formType',
		    	hidden: false,
		    	flex:1,
		    	menuDisabled:true,
		    	renderer:function(v,p,r){
		    		return "<div class='row-newform row-newform-"
					+ r.get("id")
					+ "'><div class='newform-description'><h3 class='newform-shortdesc'>"
					+ r.get("title")
					+ "</h3><span class='newform-longdesc'>"
					+ r.get("description")
					+ "</span></div></div>";
		    	}
		    },{
		    	dataIndex:'id',
		    	width:150,
		    	align:'right',
		    	renderer: function(v,p,r){
		    		return "<div class='row-newform'>"+
		    		"<a class='new-form-button button green' href='"+appContext + "/" + r.get("type") + "s/"+ claraInstance.id + "/" + r.get("type") + "-forms/" + r.get("id") + "/create' onClick='clickAndDisable(this);'>Create</a></div>";
		    	}
		    }
		    
		    ]
		}];
		me.buttons = [{text:'Close', handler:function(){me.close();}}];

		me.callParent();
	}
});