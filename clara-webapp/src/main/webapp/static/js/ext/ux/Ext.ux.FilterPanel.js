Ext.ux.FilterPanel = Ext.extend(Ext.BoxComponent,
{
    constructor: function(cfg) 
    {
        Ext.ux.FilterPanel.superclass.constructor.apply(this, arguments);

        if (!this.filters) 
        {
            this.filters = [];
        }

        if (!this.appliedFilters) 
        {
            this.appliedFilters = [];
        }
        
        if (!this.filterNames)
        {
            this.filterNames = this.getFilterNames();
        }

        // FilterPanel Listener Events
        this.addEvents(

        /**
        * @event filterAdded
        * Fires right after a new filter object has been added to this.appliedFilters array
        * @param {object} name : 'filter'
        *                 value : this.appliedFilters[this.appliedFilters.length - 1]  // last filter added
        *                 description : filter will contain the following properties:
        *                      - filterID
        *                      - value
        *                      - component in this.filterWindow corresponding to the filter object
        */
            'filteradded',

        /**
        * @event filterModified
        * Fires right after a filter object's value has been modified in this.appliedFilters array
        * @param {object} name : 'filter'
        *                 value : this.appliedFilters[k]
        *                 description : if ctrl.filterID exists in this.appliedFilters when
        *                               this.setFilterValue is being called, it is assumed that 
        *                               the value of this.appliedFilters[k] is being modified. 
        * @param {String} name : 'previousValue'
        *                 value : var previousValue = this.appliedFilters[k].value
        *                 description : previousValue is set prior to setting the value of 
        *                               this.appliedFilters[k].value = value;
        */
            'filtermodified',

        /**
        * @event filterRemoved
        * Fires right before the filter object is removed from this.appliedFilters array
        * @param {object} name : 'filter'
        *                 value : this.appliedFilters[k]
        *                 description : filter object to be removed from this.appliedFilters
        *                               including the component the filter is linked to
        */
            'filterremoved',

        /**
        * @event beforeFiltersApplied
        * Fires when 'btnApplyFilters' has been clicked before this.setFilterValue has been called
        * @param {Array} name : 'filters'
        *                value : this.appliedFilters
        *                description : array of filter objects before the current changes have been applied
        */
            'beforefiltersapplied',

        /**
        * @event filterApplied
        * Fires when a list item is selected
        * @param {Array} name : 'filters'
        *                value : this.appliedFilters
        *                description : array of filter objects after the current changes have been applied 
        */
            'filtersapplied',

        /**
        * @event beforeStoreUpdated
        * Fires when a list item is selected
        */
            'beforestoreupdated'
        );
    },

    // Properties to copy to appliedFilter object when adding a filter value
    FILTER_PROPERTIES: 'filterTitle, filterID, url, params',

    // FilterPanel Overridable Properties
    autoWidth: false,
    filterMode: 'remote',
    linkText: 'Filters',
    wndWidth: 350,
    wndLabelWidth: 100,
    wndAutoHeight: true,
    wndClosable: false,
    wndButtonAlign: 'center',


    onRender: function(ct, position) 
    {
        if (!this.tpl) 
        {
            // Breadcrumb Template - Loop through each of the applied filters to build a breadcrumb
            this.tpl = new Ext.XTemplate(
                '<div class="x-filter-strip x-tab-strip">',
                    '<a href="#" class="x-filter-title">',
                        this.linkText,
                    ':</a>',
                    '<tpl for=".">',
                        ' > <span id={filterID} class="x-filter-item x-tab-strip-closable"> {filterText}',
                        '<a class="x-filter-item-close x-tab-strip-close" onclick="return false;"></a></span>',
                    '</tpl>',
                '> ... </div>'
            );
        }

        Ext.ux.FilterPanel.superclass.onRender.call(this, ct, position);

        this.buildBreadcrumb();
    },

    initComponent: function() 
    {
        var filterCount = this.filters.length;
        for (var i = 0; i < filterCount; i++) 
        {
            var comp = this.filters[i];

            // If filter has already been created and is passed to the filters collection
            // by it's ID property, set the filterType based on the component's XType property
            if (Ext.isString(comp)) 
            {
                comp = Ext.ComponentMgr.get(comp);
                switch (comp.getXType()) 
                {
                    case 'combo':
                        comp.filterType = 'select';
                        break;
                    case 'datefield':
                        comp.filterType = 'date';
                        break;
                    default:
                        comp.filterType = 'text';
                        break;
                }
            }
            else if (!comp.events) 
            {
                switch (comp.filterType.toLowerCase()) 
                {
                    case 'select':
                        comp = new Ext.ux.SelectFilter(comp);
                        break;
                    case 'date':
                        comp = new Ext.ux.DateFilter(comp);
                        break;
                    default:
                        comp = new Ext.ux.TextFilter(comp);
                        break;
                }
            }

            if (comp.events) 
            {
                this.filters[i] = comp;  // Map the created control to the corresponding filter object in this.filters
            }
        }
    },

    createFilterWindow: function() 
    {
        this.filterWindow = new Ext.Window({
            autoHeight: this.wndAutoHeight,
            width: this.wndWidth,
            layout: 'form',
            closable: this.wndClosable,
            labelWidth: this.wndLabelWidth,
            buttonAlign: this.wndButtonAlign,
            items: this.filters,
            defaultButton: 0,
            listeners: {
                scope: this,
                'deactivate': this.hide
            },
            buttons: [
                {
                    ref: 'btnApplyFilters',
                    text: 'Apply Filters',
                    listeners: {
                        scope: this,
                        'click': this.applyFilters
                    }
                },
                {
                    ref: 'btnClearFilters',
                    text: 'Clear Filters',
                    listeners: {
                        scope: this,
                        'click': this.btnClearFilters_Click
                    }
                },
                {
                    ref: 'btnCancelFilters',
                    text: 'Cancel',
                    listeners: {
                        scope: this,
                        'click': this.hide
                    }
                }
            ],
            keys: [
                {
                    key: Ext.EventObject.ENTER,
                    fn: this.applyFilters,
                    scope: this
                },
                {
                    key: Ext.EventObject.ESC,
                    fn: this.hide,
                    scope: this
                }
            ]
        });
    },

    setFilterValue: function(ctrl) 
    {
        var modified = false;
        Ext.each(this.filters, function(filter) 
        {
            if (filter.filterID === ctrl.filterID) 
            {
                var value = ctrl.getValue();
                if (value && value !== '') 
                {
                    // Determine whether the applied filter value needs to be added or modified in this.appliedFilters
                    Ext.each(this.appliedFilters, function(appliedFilter) 
                    {
                        if (appliedFilter.filterID === ctrl.filterID) 
                        {
                            // Update Modified Filter Value
                            var previousValue = appliedFilter.value;
                            appliedFilter.value = value;
                            appliedFilter.text = filter.getTextValue();

                            // Update Filter Template
                            var tpl = new Ext.Template(filter.filterTpl);
                            appliedFilter.filterText = tpl.apply(appliedFilter);

                            this.fireEvent('filtermodified', appliedFilter, previousValue);
                            modified = true;

                            return false;
                        }
                    }, this);

                    if (!modified) 
                    {
                        // Add Applied Filter
                        this.addFilter(filter, value);
                        var newFilter = this.appliedFilters[this.appliedFilters.length - 1];
                        this.fireEvent('filteradded', newFilter);
                    }
                }
                else 
                {
                    Ext.each(this.appliedFilters, function(appliedFilter) 
                    {
                        if (appliedFilter.filterID === ctrl.filterID && appliedFilter.value != value) 
                        {
                            // Remove Applied Filter
                            this.fireEvent('filterremoved', appliedFilter);
                            this.appliedFilters.remove(appliedFilter);
                            return false;
                        }
                    }, this);
                }
                return false;
            }
        }, this);
    },

    show: function(event, el, config) 
    {
        if (!this.filterWindow) 
        {
            this.createFilterWindow();
        }

        this.loadFilters();
        this.filterWindow.show();

        var windowRightEdge = event.xy[0] + this.filterWindow.width;
        var browserWindowWidth = Ext.getBody().getViewSize().width;
        var xOverlap = windowRightEdge - browserWindowWidth;

        var windowBottomEdge = event.xy[1] + this.filterWindow.getHeight();
        var browserWindowHeight = Ext.getBody().getViewSize().height;
        var yOverlap = windowBottomEdge - browserWindowHeight;

        this.filterWindow.setPosition(
            event.xy[0] - (xOverlap > 0 ? xOverlap + 10 : 0),
            event.xy[1] - (yOverlap > 0 ? yOverlap + (browserWindowHeight - event.xy[1]) : 0));

        if (this.appliedFilters.length === 0 && this.filters.length > 0) 
        {
            this.filters[0].focus(true, true);
        }
        else 
        {
            Ext.each(this.filters, function(filter) 
            {
                var id = el.id.length > 0 ? el.id : el.parentElement.id;
                if (id == filter.filterID) 
                {
                    filter.focus(true, true);
                    return false;
                }
            });
        }
    },

    hide: function() 
    {
        if (this.filterWindow) 
        {
            this.filterWindow.setVisible(false);
        }
    },

    loadFilters: function() 
    {
        this.reset();
        var appliedFilterCount = this.appliedFilters.length;
        for (var i = 0, filterCount = this.filters.length; i < filterCount; i++) 
        {
            for (var k = 0; k < appliedFilterCount; k++) 
            {
                var filter = this.filters[i];
                if (filter.filterID == this.appliedFilters[k].filterID) 
                {
                    this.filters[i].setValue(this.appliedFilters[k].value);
                    break;
                }
            }
        }
    },

    resetAll: function() 
    {
        for (var i = 0, filterCount = this.filters.length; i < filterCount; i++) 
        {
            this.filters[i].reset();
            delete this.filters[i].lastQuery;
        }
    },

    reset: function() 
    {
        for (var i = 0, filterCount = this.filters.length; i < filterCount; i++) 
        {
            this.filters[i].reset();
        }
    },

    // 'Apply Filters' button clicked
    applyFilters: function() 
    {
        this.fireEvent('beforefiltersapplied', this.appliedFilters, this);
        Ext.each(this.filters, this.setFilterValue, this);
        this.fireEvent('filtersapplied', this.appliedFilters, this.getFilterValues());

        this.hide();
        this.buildBreadcrumb();

        if (this.store) 
        {
            this.updateStore();
        }
    },
    
    btnClearFilters_Click: function(btn, e)
    {
        this.clear(false);
    },

    clear: function(skipReload) 
    {
        var appliedFilterCount = this.appliedFilters.length;
        this.appliedFilters = [];
        this.hide();
        this.resetAll();
        this.buildBreadcrumb();
        this.fireEvent('filtersapplied', this.appliedFilters, this.getFilterValues());

        if (this.store && appliedFilterCount > 0) 
        {
            if (this.store.lastOptions && this.store.baseParams) 
            {
                for (var property in this.store.baseParams) 
                {
                    if (this.filterNames.indexOf(property.toLowerCase()) >= 0) 
                    {
                        this.store.baseParams[property] = null;
                        
                        if (this.store.lastOptions.params)
                        {
                            this.store.lastOptions.params[property] = null;
                        }
                    }
                }
            }

            if (!skipReload) 
            {
                this.updateStore();
            }
        }
    },

    addFilter: function(filter, value) 
    {
        var copiedFilter = Ext.copyTo({}, filter, this.FILTER_PROPERTIES);
        copiedFilter.comp = filter;

        var tpl = new Ext.Template(filter.filterTpl);
        var appliedFilter = copiedFilter;
        appliedFilter.value = value;
        appliedFilter.text = filter.getTextValue();
        appliedFilter.filterText = tpl.apply(copiedFilter);
        this.appliedFilters.push(appliedFilter);
    },

    removeFilter: function(e, el, obj) 
    {
        var property = '';
        Ext.each(this.appliedFilters, function(appliedFilter) 
        {
            if (appliedFilter.filterID == el.parentElement.id) 
            {
                Ext.each(this.filters, function(filter) 
                {
                    if (filter.filterID == appliedFilter.filterID) 
                    {
                        property = filter.filterID;
                        filter.reset();
                    }
                }, this);

                this.appliedFilters.remove(appliedFilter);
                this.fireEvent('filterremoved', appliedFilter);
                this.fireEvent('filtersapplied', this.appliedFilters);
                this.buildBreadcrumb();
                return false;
            }
        }, this);

        if (this.store) 
        {
            if (this.store.baseParams) 
            {
                this.store.baseParams[property] = null;
                
                if (this.store.lastOptions.params)
                {
                    this.store.lastOptions.params[property] = null;
                }
            }

            this.updateStore();
        }
    },

    // Return dictionary of values from this.appliedFilters
    getFilterValues: function() 
    {
        var filterValues = {};

        Ext.each(this.appliedFilters, function(appliedFilter) 
        {
            filterValues[appliedFilter.filterID] = appliedFilter.comp.getValue();
        });

        return filterValues;
    },
    
    getFilterNames: function()
    {
        var filterIDs = [];
    
        Ext.each(this.filters, function(filter)
        {
            filterIDs.push(filter.filterID.toLowerCase());
        });
        
        return filterIDs.join();
    },

    updateStore: function() 
    {
        this.fireEvent("beforestoreupdated");
    
        if (this.filterMode === 'local') 
        {
            this.store.filterBy(this.filterStore, this);
        }
        else 
        {   
            var baseParams = this.store.baseParams || {};
            var filterValues = this.getFilterValues();
            Ext.apply(baseParams, filterValues);
            
            if (this.store.lastOptions && this.store.lastOptions.params)
            {
                this.store.lastOptions.params.start = 0;
                Ext.apply(this.store.lastOptions.params, filterValues);
            }
            
            this.store.reload();
        }
    },

    filterLocal: function(record, id) {
        var matched = true;
        Ext.each(this.appliedFilters, function(filter) 
        {
            var field = filter.filterID;
            var value = record.data[field];
            if (value) 
            {
                if (typeof value == 'string' && value.toLowerCase().indexOf(filter.value.toString().toLowerCase()) === 0) 
                {
                    return true;
                }
                else if (value != filter.value) 
                {
                    matched = false;
                    return false;
                }
            }
        }, this);

        return matched;
    },

    buildBreadcrumb: function() 
    {
        this.tpl.overwrite(this.el, this.appliedFilters);
        this.ownerCt.doLayout();
        this.addClickEvents();
    },

    // Add click listeners to this.tpl components
    addClickEvents: function(add) {
        // Remove Filter Button   
        var els = Ext.select(".x-filter-item-close", this.el.dom);
        els.on('click', this.removeFilter, this);

        // Filters Link
        var fLink = Ext.select(".x-filter-title", this.el.dom);
        fLink.on('click', this.show, this);

        // Filter Title
        var fTitle = Ext.select(".x-filter-item", this.el.dom);
        fTitle.on('click', this.show, this);
    },

    // Remove click listeners from this.tpl components
    removeClickEvents: function() {
        // Remove Filter Button   
        var els = Ext.select(".x-filter-item-close", this.el.dom);
        els.un('click', this.removeFilter, this);

        // Filters Link
        var fLink = Ext.select(".x-filter-title", this.el.dom);
        fLink.un('click', this.show, this);

        // Filter Title
        var fTitle = Ext.select(".x-filter-item", this.el.dom);
        fTitle.un('click', this.show, this);
    }
});

// Register Control
Ext.reg('filterpanel', Ext.ux.FilterPanel);


// TextField Filter
Ext.ux.TextFilter = Ext.extend(Ext.form.TextField,
{
    // Overridable Properties
    filterTpl: '{filterTitle} contains <b>{text}</b>',
    width: 200,

    constructor: function(config)
    {
        Ext.ux.TextFilter.superclass.constructor.apply(this, arguments);
        
        // Non-Overridable Properties
        this.fieldLabel = this.filterTitle;
        this.selectOnFocus = true;
    },
    
    onRender : function(ct, position)
    {
        Ext.ux.TextFilter.superclass.onRender.call(this, ct, position);
    },
    
    getTextValue : function()
    {
        return this.getValue();
    }
});

// ComboBox Filter
Ext.ux.SelectFilter = Ext.extend(Ext.form.ComboBox,
{
    // Overridable Properties
    filterTpl: '{filterTitle} is <b>{text}</b>',
    displayField: 'Text',
    valueField: 'Value',
    mode: 'remote',
    resizable: true,
    typeAhead: true,
    triggerAction: 'all',
    selectOnFocus: true,

    constructor: function(config)
    {
        Ext.ux.SelectFilter.superclass.constructor.apply(this, arguments);
        
        // Non-Overridable Properties
        this.tpl = this.tplID ? Ext.getCmp(this.tplID) : this.tpl;
        this.fieldLabel = this.filterTitle;
        this.emptyText = '-- Select ' + this.filterTitle + ' --';
        this.store = new Ext.data.JsonStore({
            fields: [this.displayField, this.valueField],
            baseParams : this.params,
            proxy : new Ext.data.HttpProxy({
                method : 'POST',
                url : this.url
            })
        });
    },    
    
    onRender : function(ct, position)
    {
        Ext.ux.SelectFilter.superclass.onRender.call(this, ct, position);
    },
    
    getTextValue : function()
    {
        return this.getRawValue();
    }
});

// DateField Filter
Ext.ux.DateFilter = Ext.extend(Ext.form.DateField,
{
    // Overridable Properties
    filterTpl: '{filterTitle} is <b>{text}</b>',
    width: 150,
    format: 'Y-m-d',

    constructor: function(config)
    {
        Ext.ux.DateFilter.superclass.constructor.apply(this, arguments);
        
        // Non-Overridable Properties
        this.fieldLabel = this.filterTitle;
    },
    
    onRender : function(ct, position)
    {
        Ext.ux.DateFilter.superclass.onRender.call(this, ct, position);
    },
    
    getTextValue : function()
    {
        return Ext.util.Format.date(this.getValue(), this.format);
    }
});