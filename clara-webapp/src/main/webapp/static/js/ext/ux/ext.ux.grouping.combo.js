Ext.define('Ext.ux.view.GroupingList', {
    extend: 'Ext.view.View',
    alias: 'widget.groupinlist',
    requires: ['Ext.layout.component.BoundList', 'Ext.toolbar.Paging'],
	
    pageSize: 0,
	
    autoScroll: true,
    baseCls: Ext.baseCSSPrefix + 'boundlist',
    listItemCls: '',
    shadow: false,
    trackOver: true,
    refreshed: 0,

    ariaRole: 'listbox',

    componentLayout: 'boundlist',

    renderTpl: ['<div class="list-ct"></div>'],

    initComponent: function() {
        var me = this,
            baseCls = me.baseCls,
            itemCls = baseCls + '-item';
        me.itemCls = itemCls;
        me.selectedItemCls = baseCls + '-selected';
        me.overItemCls = baseCls + '-item-over';
        me.itemSelector = "." + itemCls;

        if (me.floating) {
            me.addCls(baseCls + '-floating');
        }
		
        var tpl = [
        	'<ul>',
        		'<tpl for=".">'
        ];
        
        var padding = 1;
        
        if (Ext.isArray(me.groupField)) {        	
        	padding = me.groupField.length;        	
        	for (var i = 0; i < me.groupField.length; i++) {        		
        		tpl.push(
	        		'<tpl if="xindex == 1 || parent[xindex - 2][\'' + me.groupField[i] + '\'] != values[\'' + me.groupField[i] + '\']">',
						'<li class="x-combo-list-group" style="padding-left:' + (i * 16) + 'px;">{[values["' + me.groupField[i] + '"]]}</li>',
					'</tpl>'
	        	);
        	}
        }        
        else {        	
        	tpl.push(
        		'<tpl if="xindex == 1 || parent[xindex - 2][\'' + me.groupField + '\'] != values[\'' + me.groupField + '\']">',
					'<li class="x-combo-list-group">{[values["' + me.groupField + '"]]}</li>',
				'</tpl>'
        	);
        }        
        tpl.push(
        			'<li role="option" class="' + itemCls + '" style="padding-left:' + (padding * 16) + 'px;">{[values["' + me.displayField + '"]]}</li>',
            	'</tpl>',
            '</ul>'
       	);
        
        me.tpl = Ext.create('Ext.XTemplate', tpl);

        if (me.pageSize) {
            me.pagingToolbar = me.createPagingToolbar();
        }

        me.callParent();

        Ext.applyIf(me.renderSelectors, {
            listEl: '.list-ct'
        });
    },

    createPagingToolbar: function() {
        return Ext.widget('pagingtoolbar', {
            pageSize: this.pageSize,
            store: this.store,
            border: false
        });
    },

    onRender: function() {
        var me = this,
            toolbar = me.pagingToolbar;
        me.callParent(arguments);
        if (toolbar) {
            toolbar.render(me.el);
        }
    },

    bindStore : function(store, initial) {
        var me = this,
            toolbar = me.pagingToolbar;
        me.callParent(arguments);
        if (toolbar) {
            toolbar.bindStore(store, initial);
        }
    },

    getTargetEl: function() {
        return this.listEl || this.el;
    },

    getInnerTpl: function(displayField) {
        return '{' + displayField + '}';
    },

    refresh: function() {
        var me = this;
        me.callParent();
        if (me.isVisible()) {
            me.refreshed++;
            me.doComponentLayout();
            me.refreshed--;
        }
    },
    
    initAria: function() {
        this.callParent();
        
        var selModel = this.getSelectionModel(),
            mode     = selModel.getSelectionMode(),
            actionEl = this.getActionEl();
        
        if (mode !== 'SINGLE') {
            actionEl.dom.setAttribute('aria-multiselectable', true);
        }
    },

    onDestroy: function() {
        Ext.destroyMembers(this, 'pagingToolbar', 'listEl');
        this.callParent();
    }
});


Ext.define('Ext.ux.form.GroupingComboBox', {
	extend: 'Ext.form.field.ComboBox',
    requires: ['Ext.ux.view.GroupingList'],
    alias: ['widget.groupingcombobox', 'widget.groupingcombo'],
    
    initComponent: function() {
    	var me = this;
    	if (!me.displayTpl) {
    		var display = [],
    			tpl = '<tpl for=".">{0}</tpl>';
    		if (Ext.isArray(me.groupField)) {
    			for (var i = 0; i < me.groupField.length; i++) {
	        		display.push('{[values["' + me.groupField[i] + '"]]}');
	        	}
    		}
    		else {
    			display.push('{[values["' + me.groupField + '"]]}');
    		}
    		display.push('{[values["' + me.displayField + '"]]}');
    		me.displayTpl = Ext.String.format(tpl, display.join(this.displaySeparator || ' '));
    	}
    	me.callParent();
    },
    
    createPicker: function() {
        var me = this,
            picker,
            menuCls = Ext.baseCSSPrefix + 'menu',
            opts = Ext.apply({
                selModel: {
                    mode: me.multiSelect ? 'SIMPLE' : 'SINGLE'
                },
                floating: true,
                hidden: true,
                ownerCt: me.ownerCt,
                cls: me.el.up('.' + menuCls) ? menuCls : '',
                store: me.store,
                groupField: me.groupField,
                displayField: me.displayField,
                focusOnToFront: false,
                pageSize: me.pageSize
            }, me.listConfig, me.defaultListConfig);
		
       	//picker = me.picker = Ext.create('Ext.view.BoundList', opts);
 		picker = me.picker = Ext.create('Ext.ux.view.GroupingList', opts);

        me.mon(picker, {
            itemclick: me.onItemClick,
            refresh: me.onListRefresh,
            scope: me
        });

        me.mon(picker.getSelectionModel(), 'selectionchange', me.onListSelectionChange, me);

        return picker;
    }
});

