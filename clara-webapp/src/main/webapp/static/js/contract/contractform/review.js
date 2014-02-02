var reviewStore;
var reviewPanel;
var signFormPanel;
var messageCount = 0;

function renderPage(){
	initializeStore();
	createSignSubmissionForm();
	signFormPanel.render('sign-and-submit');
	
}

function createSignSubmissionForm(){
	
	var baseUrl = appContext + "/contracts/" + claraInstance.id + "/contract-forms/" + claraInstance.form.id + "/"+claraInstance.form.urlName+"/contract-form-xml-datas/" + claraInstance.form.xmlDataId + "/";
	
	signFormPanel = new Ext.FormPanel({
		width:600,
		standardSubmit: true,
        labelWidth: 70, // label settings here cascade unless overridden
        url: baseUrl + "sign",
        id:'signSubmitForm',
        frame:true,
        title: 'By entering your username and password below, you agree to the text above.',
        bodyStyle:'padding:5px 5px 0',
        defaults: {
            width: 190
         },
        items: [
    	        {
    	        	itemCls:'review-username',
    	        	xtype: 'textfield',
    			    fieldLabel: 'Username',
    			    name: 'username',
    			    allowBlank:false
    			},
    	        {
    				itemCls:'review-password',
    	        	xtype: 'textfield',
    	        	inputType:'password',
    			    fieldLabel: 'Password',
    			    name: 'password',
    			    allowBlank:false
    			}
        ],
        buttons: [{
            text: 'Sign and Submit for Review',
	        formBind:true,
            handler:function(){ 
        		signFormPanel.getForm().submit(); 
            }
        }]
    });


}

function initializeStore(formXmlDataId){
	
	var ajaxBaseUrl = appContext+"/ajax/contracts/" + claraInstance.id + "/contract-forms/" + claraInstance.form.id + "/"+claraInstance.form.urlName+"/contract-form-xml-datas/" + claraInstance.form.xmlDataId + "/";
	clog(ajaxBaseUrl);
	reviewStore = new Ext.data.Store({
		autoLoad:true,
		header :{
        	'Accept': 'application/json'
    	},
		proxy: new Ext.data.HttpProxy({
			url: ajaxBaseUrl +  "validate",
			method:"GET"
		}),
		reader: new Ext.data.JsonReader({}, [
			{name:'pagename', mapping:'additionalData.pagename'},
			{name:'pageref', mapping:'additionalData.pageref'},
			{name:'constraintLevel', mapping:'constraint.constraintLevel'},
			{name:'errorMessage', mapping:'constraint.errorMessage'}
		]),
		listeners: {
			'load': function(store,records,opts){
    			var hasErrors = (store.find("constraintLevel", "ERROR") > -1)?true:false;
    			if (store.getCount() > 0){
    				clog("store.getCount() = " + store.getCount());
    				createReviewPanel();
    			}
    			clog("store",store,"errors",hasErrors,"comm",claraInstance.user.committee);
    				if (claraInstance.user.committee == 'PI' && hasErrors == false && usingNoHeaderTemplate == false){
    					jQuery('#review-no-messages').show();
    					signFormPanel.render('sign-and-submit');
    				} else if (claraInstance.user.committee != 'PI' || usingNoHeaderTemplate == true){
    					jQuery('#review-no-messages-othercommittee').show();
    				}
    			
    		}
    	}

	});

}

function renderMessage(value, p, record){

	clog(record.data);
	var outHTML='<div class="review-row">';

	outHTML = outHTML + '<div class="review-row-icon-'+record.data.constraintLevel+'">'+record.data.constraintLevel+'</div><div class="review-row-message"><h3 class="review-row-message-page">'+record.data.pagename+'</h3>';
	outHTML = outHTML + '<span class="review-row-message-description">'+record.data.errorMessage+'</span></div>';

	return outHTML+'</div>';

}

function createReviewPanel(){
	
	reviewPanel = new Ext.grid.GridPanel({
    	frame:false,
    	trackMouseOver:false,
    	renderTo: 'review-list',
        store: reviewStore,
        title:'Please pay attention to the following messages',
        sm: new Ext.grid.RowSelectionModel({singleSelect: true}),
        loadMask: new Ext.LoadMask(Ext.getBody(), {msg:"Performing a final check. Please wait..."}),
        height:250,
        viewConfig: {
    		forceFit:true
    	},
    	listeners:{
            'rowdblclick':{
                fn: function (gridObj, rowIdx, e) {
                    var row = gridObj.getStore().getAt(rowIdx);
                    if (typeof row != 'undefined' && row.data.pagename != ''){
                    	var nextPageUrl = appContext + "/contracts/"+claraInstance.id+"/contract-forms/" + claraInstance.form.id +"/" + claraInstance.form.urlName +"/contract-form-xml-datas/" + claraInstance.form.xmlDataId + "/" + row.data.pageref.toLowerCase();
         
                    	location.href = nextPageUrl;
                    }
                }
            }
        },
        columns: [
                  {
                	  	dataIndex:'constraintLevel',
                	  	sortable:true,
                	  	renderer:renderMessage,
                	  	width:680
                  }
        ]
    });
}


