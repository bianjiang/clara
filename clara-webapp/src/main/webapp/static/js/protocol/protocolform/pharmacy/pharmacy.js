Ext.ns('Clara.Pharmacy', 'Clara', 'Clara.NewSubmission');

Encoder.EncodeType = "entity";

var multipleFee = 15.00;


Clara.Pharmacy.MessageBus.addListener('afterpharmacysave', function(p){
	clog("saved. reloading.");
	Ext.getCmp("feepanel").getStore().loadData(pharmacy.getArray());
	var waiveClass = (pharmacy.waived)?"waived-total":"";
	jQuery("#pharmacy-total").html("Total (initiation costs only): "+pharmacy.getDisplayTotal());
	Ext.getCmp("btnRemoveExpense").setDisabled(true);
});

Clara.Pharmacy.MessageBus.addListener('waivechanged', function(v){
	clog("waive changed, editing totals / saving..");
	pharmacy.updateTotal();
	pharmacy.save();
});

Clara.Pharmacy.Viewport = Ext.extend(Ext.Viewport, {

	initComponent:function(){
		var config = {
			layout:'border',
			border:false,
			items: [	{
				    region: 'north',
				    contentEl:'clara-header',
				    bodyStyle:{ backgroundColor:'transparent' },
				    height:40,
				    border: false,
				    margins: '0 0 0 0'
				}, 
				{
					region:'center',
					id:'feepanel',
					border: true,
					margins:'1 1 1 1',
			        cmargins:'1 1 1 1',
			        xtype:'pharmacyfeepanel'
				}
			]
		};
		// apply config
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		  
		// call parent
		Clara.Pharmacy.Viewport.superclass.initComponent.apply(this, arguments);
	}
});
	

jQuery("#cbWaiveFee").click(function(){
	updateTotals(true);
});
