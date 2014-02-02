var dispensingFeeStore;
var basicFeeStore;
var recurringFeeStore;

initializePharmacyStores();

function initializePharmacyStores(){

	dispensingFeeStore = new Ext.data.Store({
		header :{
	    'Accept': 'application/json'
	    },
	    autoLoad:true,
		proxy: new Ext.data.HttpProxy({
			url: appContext+"/static/xml/budget-fees.json",
			method:'GET'
		}),
		reader: new Ext.data.JsonReader({
			root:'DISP',
			idProperty: 'id'
		}, [
		    {name:'id'},
		    {name:'description'},
		    {name:'cost'},
		    {name:'editable'},
		    {name:'canaddmultiple'},
		    {name:'validMin'},
		    {name:'validMax'}
		])
	});
	
	basicFeeStore = new Ext.data.Store({
		header :{
	    'Accept': 'application/json'
	    },
	    autoLoad:true,
		proxy: new Ext.data.HttpProxy({
			url: appContext+"/static/xml/budget-fees.json",
			method:'GET'
		}),
		reader: new Ext.data.JsonReader({
			root:'SIMC',
			idProperty: 'id'
		}, [
		    {name:'id'},
		    {name:'description'},
		    {name:'cost'},
		    {name:'editable'},
		    {name:'validMin'},
		    {name:'validMax'}
		])
	});
	
	recurringFeeStore = new Ext.data.Store({
		header :{
	    'Accept': 'application/json'
	    },
	    autoLoad:true,
		proxy: new Ext.data.HttpProxy({
			url: appContext+"/static/xml/budget-fees.json",
			method:'GET'
		}),
		reader: new Ext.data.JsonReader({
			root:'ANNUAL',
			idProperty: 'id'
		}, [
		    {name:'id'},
		    {name:'description'},
		    {name:'cost'},
		    {name:'editable'},
		    {name:'validMin'},
		    {name:'validMax'}
		])
	});


}