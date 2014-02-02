Ext.override(Ext.view.Table, { 
  /* 
    Temporary fix for bug in ExtJS 4.2.1. See: sencha.com/forum/showthread.php?264657-Exception-When-Selecting-First-Grid-Row 
  */ 
  getRowStyleTableElOriginal: Ext.view.Table.prototype.getRowStyleTableEl, 
  getRowStyleTableEl: function() { 
    var el = this.getRowStyleTableElOriginal.apply(this, arguments); 
    if (!el) { 
      el = { 
        addCls: Ext.emptyFn, 
        removeCls: Ext.emptyFn, 
        tagName: {} 
      } 
    } 
    return el; 
  } 
});

Ext.define('Ext.Window.IE8Fix',{
	override: 'Ext.Window',
	// IE8 fix (http://www.sencha.com/forum/archive/index.php/t-241500.html?s=15ad65f757fb7325aa20735e3226faab)
	style:'z-index:-1;'
});

Ext.form.field.Base.override({
    setLabel: function (text) {
        if (this.rendered) {
            Ext.get(this.labelEl.id).update(text);
        }
        this.fieldLabel = text;
    }
});

Ext.define('Ext.form.SubmitFix', {
    override: 'Ext.ZIndexManager',

    register : function(comp) {
        var me = this,
            compAfterHide = comp.afterHide;
        
        if (comp.zIndexManager) {
            comp.zIndexManager.unregister(comp);
        }
        comp.zIndexManager = me;

        me.list[comp.id] = comp;
        me.zIndexStack.push(comp);
        
        // Hook into Component's afterHide processing
        comp.afterHide = function() {
            compAfterHide.apply(comp, arguments);
            me.onComponentHide(comp);
        };
    },

    /**
     * Unregisters a {@link Ext.Component} from this ZIndexManager. This should not
     * need to be called. Components are automatically unregistered upon destruction.
     * See {@link #register}.
     * @param {Ext.Component} comp The Component to unregister.
     */
    unregister : function(comp) {
        var me = this,
            list = me.list;
        
        delete comp.zIndexManager;
        if (list && list[comp.id]) {
            delete list[comp.id];
            
            // Relinquish control of Component's afterHide processing
            delete comp.afterHide;
            Ext.Array.remove(me.zIndexStack, comp);

            // Destruction requires that the topmost visible floater be activated. Same as hiding.
            me._activateLast();
        }
    }
});