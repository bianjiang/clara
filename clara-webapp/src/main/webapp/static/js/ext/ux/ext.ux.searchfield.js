/*!
 * Ext JS Library 3.2.1
 * Copyright(c) 2006-2010 Ext JS, Inc.
 * licensing@extjs.com
 * http://www.extjs.com/license
 */
Ext.ns('Ext.ux.form');

Ext.ux.form.SearchField = Ext.extend(Ext.form.TwinTriggerField, {
    initComponent : function(){
        Ext.ux.form.SearchField.superclass.initComponent.call(this);
        this.on('specialkey', function(f, e){
            if(e.getKey() == e.ENTER){
                this.onTrigger2Click();
            }
        }, this);
    },
    
    validationEvent:false,
    validateOnBlur:false,
    trigger1Class:'x-form-clear-trigger',
    trigger2Class:'x-form-search-trigger',
    hideTrigger1:true,
    width:180,
    hasSearch : false,
    paramName : 'query',
    reloadAllAsClear: false,
    params: {},
    beforeClear: function(){},
    afterClear: function(){},
    beforeSeach: function(){},
    afterSeach: function(){},
    
    onTrigger1Click : function(){
        if(this.hasSearch){
            this.el.dom.value = '';
            this.params = {start: 0};

            if(this.beforeClear) this.beforeClear();
            if(this.reloadAllAsClear){
            	this.store.reload({params:this.params, callback:this.afterClear});            	
            }else{
            	this.store.removeAll();
            	if(this.afterClear) this.afterClear();
            }                        
            
            this.triggers[0].hide();
            this.hasSearch = false;
        }
    },

    onTrigger2Click : function(){
    	var minLength = (typeof this.minLength != 'undefined')?this.minLength:1; 
        var v = this.getRawValue();
        if(v.length < minLength){
        	alert("Enter at least "+minLength+" character(s) to search");
            this.onTrigger1Click();
            return;
        }
        this.params[this.paramName] = v;
        this.params['start'] = 0;
        if(this.beforeSearch) {
        	if(!this.beforeSearch()) return;
        }
        //jbian@uams.edu: o extends params...to send extra params to server 
        this.store.reload({params:this.params, callback: this.afterSearch});
        this.hasSearch = true;
        this.triggers[0].show();
    }
});

Ext.reg('uxsearchfield',Ext.ux.form.SearchField);
