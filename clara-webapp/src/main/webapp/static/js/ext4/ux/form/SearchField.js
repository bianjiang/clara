Ext.define('Ext.ux.form.SearchField', {
    extend: 'Ext.form.field.Trigger',

    alias: 'widget.searchfield',

    trigger1Cls: Ext.baseCSSPrefix + 'form-clear-trigger',

    trigger2Cls: Ext.baseCSSPrefix + 'form-search-trigger',

    hasSearch : false,
    paramName : 'query',
    reloadAllAsClear: false,
    params: {},
    beforeClear: function(){},
    afterClear: function(){},
    beforeSeach: function(){},
    afterSeach: function(){},

    initComponent: function() {
        var me = this;

        me.callParent(arguments);
        me.on('specialkey', function(f, e){
            if (e.getKey() == e.ENTER) {
                me.onTrigger2Click();
            }
        });

        // We're going to use filtering
        me.store.remoteFilter = true;

        // Set up the proxy to encode the filter in the simplest way as a name/value pair

        // If the Store has not been *configured* with a filterParam property, then use our filter parameter name
        if (!me.store.proxy.hasOwnProperty('filterParam')) {
            me.store.proxy.filterParam = me.paramName;
        }
        me.store.proxy.encodeFilters = function(filters) {
            return filters[0].value;
        }
    },

    afterRender: function(){
        this.callParent();
        this.triggerCell.item(0).setDisplayed(false);
    },

    onTrigger1Click : function(){
        var me = this;

        if (me.hasSearch) {
        	
        	if(this.beforeClear) this.beforeClear();
            if(this.reloadAllAsClear){
            	this.store.reload({params:this.params, callback:this.afterClear});            	
            }else{
            	this.store.removeAll();
            	if(this.afterClear) this.afterClear();
            }  
        	
            me.setValue('');
            me.store.clearFilter();
            me.hasSearch = false;
            me.triggerCell.item(0).setDisplayed(false);
            me.updateLayout();
        }
    },

    onTrigger2Click : function(){
    	var minLength = (typeof this.minLength != 'undefined')?this.minLength:0; 
        var me = this,
            value = me.getValue();

        if(value.length < minLength){
        	alert("Enter at least "+minLength+" character(s) to search");
            this.onTrigger1Click();
            return;
        } else {
            // Param name is ignored here since we use custom encoding in the proxy.
            // id is used by the Store to replace any previous filter
            
            
            
            me.params[me.paramName] = value;
            me.params['start'] = 0;
            if(me.beforeSearch) {
            	if(!me.beforeSearch()) return;
            }
clog("PARAMS",me.params);
            //jbian@uams.edu: o extends params...to send extra params to server 
            me.store.load({params:me.params, callback: me.afterSearch});
            /*
             me.store.filter({
                id: me.paramName,
                property: me.paramName,
                value: value
            });
            
            */
            me.hasSearch = true;
            
            me.triggerCell.item(0).setDisplayed(true);
            me.updateLayout();
        }
    }
});