Ext.define('Clara.Dashboard.controller.Dashboard', {
    extend: 'Ext.app.Controller',
    models: ['Clara.Dashboard.model.AvailableSearchField','Clara.Reports.model.ComboCriteria','Clara.Common.model.Protocol','Clara.Common.model.Contract','Clara.Dashboard.model.Bookmark'],
    stores: ['Clara.Dashboard.store.AvailableSearchFields','Clara.Reports.store.ComboCriterias','Clara.Common.store.Protocols','Clara.Common.store.Contracts','Clara.Dashboard.store.Bookmarks'],
    
    refs: [
           {ref: 'bookmarkPanel', selector: 'bookmarkpanel'},
           {ref: 'bookmarkWindow', selector: 'bookmarkwindow'},
           {ref: 'dashboardGridPanel', selector: '#dashboardgridpanel'},
           {ref: 'removeBookmarkButton', selector: '#btnRemoveBookmark'},
           {ref: 'addBookmarkCriteriaButton', selector: '#btnAddBookmarkCriteria'},
           {ref: 'availableSearchFieldCombo', selector: '#fldAvailableSearchFieldCombo'}
    ],
   
    loadingMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),

    init: function() {
    	var me = this;
    	
    	me.control({
    		'bookmarkpanel':{
        		itemclick:me.onBookmarkSelect
        	},
        	'#dashboardgridpanel':{
        		itemdblclick:me.onDashboardItemDoubleClick
        	},
        	'#btnAddBookmark':{
        		click:me.onAddBookmark
        	},
        	'#fldAvailableSearchFieldCombo':{
        		select:me.onBookmarkSearchFieldSelect
        	},
        	'#btnAddBookmarkCriteria':{
        		click:me.onBookmarkSearchCriteriaAdd
        	},
        	'#btnSaveBookmark':{
        		click:me.onBookmarkSave
        	},
        	'#btnRemoveBookmark':{
        		click:me.onBookmarkRemove
        	},
        	'#btnDashboardNewSubmission':{
        		click:me.onCreateSubmission
        	},
        	'#btnDashboardNewContract':{
        		click:me.onCreateContract
        	},
        	'#btnRunBookmarkWithoutSaving':{
        		click:me.onRunBookmark
        	}
        	
        });
    },
    
    onCreateSubmission: function(){
    	if (piwik_enabled()){
			_paq.push(['trackEvent', 'DASHBOARD', 'Create Submission Window']);
		}
    	Ext.create('Clara.Dashboard.view.CreateSubmissionWindow',{}).show();
    },
    
    onCreateContract: function(){
    	if (piwik_enabled()){
			_paq.push(['trackEvent', 'DASHBOARD', 'Create Contract Window']);
		}
	    	Ext.create('Clara.Dashboard.view.CreateSubmissionWindow',{
	    		activeTab:1
	    	}).show();
    },
    
    onDashboardItemDoubleClick: function(g,rec){
    	location.href = appContext+"/"+claraInstance.type+"s/"+rec.get("id")+"/dashboard";
    },
    
    /*
     *  BOOKMARK FUNCTIONS
     * 
     */
    
    onRunBookmark: function(){
    	this.saveBookmark(true);
    	if (piwik_enabled()){
			_paq.push(['trackEvent', 'DB_BOOKMARK', 'Running search without save']);
		}
    },
    
    onBookmarkSave: function(){
    	this.saveBookmark(false);
    },
    
    onBookmarkSelect: function(gp,rec,item){
    	clog("Dashboard Controller: onBookmarkSelect",rec,item);
    	if (piwik_enabled()){
			_paq.push(['trackEvent', 'DB_BOOKMARK', 'Selected: '+rec.get("name")]);
		}
    	var me = this;
    	
    	// Disable deleting for first 3 bookmarks (protocols) or first 1 (contracts)
    	me.getRemoveBookmarkButton().setDisabled(gp.getStore().indexOf(rec) < ((claraInstance.type == "protocol")?3:1));
    	
    	var st = me.getDashboardGridPanel().getStore();
    	Ext.apply(st.proxy.extraParams, {
			keyword: '',
			searchCriterias: rec.get("searchCriterias")
		});
    	st.load({
			params:{
				start: 0, 
				limit: me.getDashboardGridPanel().pageSize
			}
		});
	},
	onBookmarkSearchCriteriaAdd: function(){
		var me = this;
		var sf = Ext.getCmp("fldAvailableSearchFieldCombo"),
            so = Ext.getCmp("fldBookmarkSearchOperator"),
            sk = Ext.getCmp("fldBookmarkSearchField");

        var fieldValue = (typeof(sk.getComboValue) == "function")?sk.getComboValue():sk.getValue();
        var fieldRawValue = (typeof(sk.getComboRawValue) == "function")?sk.getComboRawValue():sk.getRawValue();

    	var newRow = [[sf.getValue(), sf.getRawValue(), so.getValue(), so.getRawValue(), fieldValue, fieldRawValue]];
    	clog("Add",newRow);
    	var st = me.getBookmarkWindow().down("grid").getStore();
    	// var newRecord = st.recordType(newRow);
    	st.add(newRow);

    	if (piwik_enabled()){
			_paq.push(['trackEvent', 'DB_BOOKMARK', 'Added criteria']);
		}
	},
	onBookmarkSearchFieldSelect: function(cb,recs){
		var sField = recs[0];
		
		if (Ext.getCmp("fldBookmarkSearchField")){
			Ext.getCmp("fldBookmarkSearchField").destroy();
		}
		
		var type = sField.get("xtype");
		clog("onBookmarkSearchFieldSelect",type);
		var searchField = null;
		var searchFieldOptions = {
				flex:1,
                name: 'fldBookmarkSearchField',
                id: 'fldBookmarkSearchField'
			};
		
		if (type == "textfield"){
			searchField = Ext.create("Ext.form.field.Text", searchFieldOptions);
			Ext.getCmp("fldBookmarkSearchOperator").setValue("CONTAINS");
			Ext.getCmp("fldBookmarkSearchOperator").setDisabled(false);
		}
		else if (type == "clarafield.combo.user"){
			searchField = Ext.create("Clara.Common.ux.ClaraUserField", searchFieldOptions); //new Ext.form.ComboBox(searchFieldOptions);
			Ext.getCmp("fldBookmarkSearchOperator").setValue("EQUALS");
			Ext.getCmp("fldBookmarkSearchOperator").setDisabled(true);
		}
		else if (type == "clarafield.college"){
			searchField = Ext.create("Clara.Common.ux.ClaraCollegeField", searchFieldOptions); //new Ext.form.ComboBox(searchFieldOptions);
			Ext.getCmp("fldBookmarkSearchOperator").setValue("EQUALS");
			Ext.getCmp("fldBookmarkSearchOperator").setDisabled(true);
		}
		else if (type == "clarafield.combo.fundingsource"){
				searchField = Ext.create("Clara.Common.ux.ClaraFundingSourceField", searchFieldOptions); //new Ext.form.ComboBox(searchFieldOptions);
				Ext.getCmp("fldBookmarkSearchOperator").setValue("CONTAINS");
				Ext.getCmp("fldBookmarkSearchOperator").setDisabled(false);

		} else if (type == "clarafield.combo.criteria"){
			searchFieldOptions.comboId = sField.get("comboId");
            searchFieldOptions.multiSelect = false;
            searchFieldOptions.maxHeight = 24;
			searchFieldOptions.store = 'Clara.Reports.store.ComboCriterias';
			searchField = Ext.create("Clara.Reports.ux.ComboCriteriaField", searchFieldOptions);
			Ext.getCmp("fldBookmarkSearchOperator").setValue("EQUALS");
			Ext.getCmp("fldBookmarkSearchOperator").setDisabled(true);
		}
		Ext.getCmp("fldBookmarkCriteriaContainer").add(searchField);
		Ext.getCmp("fldBookmarkCriteriaContainer").doLayout();
		
		Ext.getCmp("btnAddBookmarkCriteria").setDisabled(false);
	},
	
	onBookmarkRemove: function(){
		var me = this;
		var bookmarkPanel = me.getBookmarkPanel();
		Ext.Msg.confirm('Remove bookmark?','<strong>Are you sure you want to remove this bookmark?</strong> (NOTE: This will not delete any of your data)',
				function(b){
					if (b == "yes"){

					    var rec = bookmarkPanel.getSelectionModel().getSelection()[0];
						var url = appContext+"/ajax/protocols/search-bookmarks/"+rec.get("id")+"/remove";
						var data = {userId: claraInstance.user.id || 0};
						
						Ext.Ajax.request({
						    url: url,
						    method: 'POST',
						    params: data,
						    success: function(response){
						    	if (piwik_enabled()){
									_paq.push(['trackEvent', 'DB_BOOKMARK', 'Removed: '+rec.get("name")]);
								}
						        var text = response.responseText;
						        clog("seccess remove bm",text,response);
						        Ext.data.StoreManager.lookup('Clara.Dashboard.store.Bookmarks').load();
						        me.getRemoveBookmarkButton().setDisabled(true);
						        
						    }
						});
						
					}
				}
		);
	},
	
	saveBookmark: function(runOnly){
		runOnly = runOnly || false;
		var me = this;
		var criteriaStore = me.getBookmarkWindow().down("grid").getStore();
		var name = Ext.getCmp("fldBookmarkName").getValue();
		if (!runOnly && name == ""){
			alert("Enter a bookmark name");
		} else {
			if (criteriaStore.getCount() < 1) {
				alert("Enter at least one search criteria.");
			} else {
				var criteria = [];
				for (var i = 0; i < criteriaStore.getCount(); i++)
				{
					criteria.push({"searchField":criteriaStore.getAt(i).data.searchFieldValue, "searchOperator":criteriaStore.getAt(i).data.searchOperatorValue, "keyword":criteriaStore.getAt(i).data.searchKeyword });
				}
				clog(Ext.JSON.encode(criteria));
				var url = appContext+"/ajax/"+claraInstance.type+"s/search-bookmarks/save";
				var data = {userId: claraInstance.user.id || 0, name:name, searchCriterias:Ext.JSON.encode(criteria)};
				
				if (runOnly){
					var st = me.getDashboardGridPanel().getStore();
			    	Ext.apply(st.proxy.extraParams, {
						keyword: '',
						searchCriterias: data.searchCriterias
					});
			    	st.load({
						params:{
							start: 0, 
							limit: me.getDashboardGridPanel().pageSize
						}
					});
			    	me.getBookmarkWindow().close();
				} else {
				
					Ext.Ajax.request({
					    url: url,
					    method: 'POST',
					    params: data,
					    success: function(response){
					        var text = response.responseText;
					        clog("seccess save",text,response);
					        if (piwik_enabled()){
								_paq.push(['trackEvent', 'DB_BOOKMARK', 'Saved: '+name]);
							}
					        Ext.data.StoreManager.lookup('Clara.Dashboard.store.Bookmarks').load();
					        criteriaStore.removeAll();
					        me.getRemoveBookmarkButton().setDisabled(true);
					        me.getBookmarkWindow().close();
					    }
					});
				}
				
			}
		}
	},
	
    onAddBookmark: function(){
    	if (piwik_enabled()){
			_paq.push(['trackEvent', 'DB_BOOKMARK', 'Open New Bookmark window']);
		}
    	Ext.create("Clara.Dashboard.view.BookmarkWindow",{modal:true}).show();
    }
    
});
