Ext.define('Clara.Reports.controller.UserReport', {
    extend: 'Ext.app.Controller',
    models: ['UserReport'],
    refs: [{
        ref: 'userReportsList',
        selector: 'userreportslist'
    },{
        ref: 'userReportWindow',
        selector:'userreportwindow'
    },{
        ref:'userReportDisplayFieldsGrid',
        selector:'reportdisplayfieldgridpanel'
    }],



    stores: ['UserReportDisplayFields','ComboCriterias','UserReportResults','UserReports','ReportTypes','Clara.Common.store.Users','Clara.Common.store.Colleges','Clara.Common.store.Departments','Clara.Common.store.Subdepartments','Protocols'],

    selectedUserReport:null,

    loadingMask : new Ext.LoadMask(Ext.getBody(), {
        msg : "Please wait..."
    }),

    showResultsWindowForReport: function(id){
        var store = Ext.data.StoreManager.lookup('UserReportResults');
        store.getProxy().url = appContext+'/ajax/reports/'+id+'/list-results';
        var win = Ext.create('Clara.Reports.view.ReportResultsWindow', {resultStore:store,title: "Results"});
        win.show();
    },

    init: function() {
        var me = this;

        me.on("displayFieldsUpdated", function() {
            me.onDisplayFieldsUpdated();
        });

        // Start listening for events on views
        this.control({
            'reportdisplayfieldgridpanel' : {
                itemOrderChanged : me.reorderDisplayFields
            },
            '#btnAddDisplayFields':{
                click: function(){
                    var reportDFStore = Ext.StoreMgr.get('ReportDisplayFields');
                    reportDFStore.getProxy().url = appContext+'/ajax/reports/'+me.selectedUserReport.get("id")+'/list-available-displayfields';
                    reportDFStore.load();
                    Ext.create('Clara.Reports.view.AddDisplayFieldWindow', {modal: true}).show();
                }
            }
        });
    },

    onDisplayFieldsUpdated: function(){
        Ext.StoreMgr.get('ReportDisplayFields').load();
    },

    reorderDisplayFields : function() {
        var me = this;
        var displayFieldIds = [];

        me.getUserReportDisplayFieldsGrid().getStore().each(function(rec) {
            displayFieldIds.push(rec.get("id"));
        });
        clog("Order array", displayFieldIds.join(", "));
        
        
        me.loadingMask.show();
        Ext.Ajax.request({
            method : 'POST',
            url : appContext + "/ajax/reports/"
                + me.selectedUserReport.get("id")
                + "/set-displayfield-order",
            params : {
                displayFieldIds : displayFieldIds
            },
            success : function(response) {
                clog('reorderDisplayFields: Ext.Ajax success',
                    response);
                me.loadingMask.hide();
                me.fireEvent("displayFieldsUpdated");
            },
            failure : function(error) {
                cwarn('reorderDisplayFields: Ext.Ajax failure',
                    error);
                me.loadingMask.hide();
            }
        });
        

    }

});
