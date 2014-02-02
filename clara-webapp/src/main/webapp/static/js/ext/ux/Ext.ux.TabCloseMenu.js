/*!
 * Ext JS Library 3.3.0
 * Copyright(c) 2006-2010 Ext JS, Inc.
 * licensing@extjs.com
 * http://www.extjs.com/license
 */
/**
 * @class Ext.ux.TabCloseMenu
 * @extends Object 
 * Plugin (ptype = 'tabclosemenu') for adding a close context menu to tabs. Note that the menu respects
 * the closable configuration on the tab. As such, commands like remove others and remove all will not
 * remove items that are not closable.
 * 
 * @constructor
 * @param {Object} config The configuration options
 * @ptype tabclosemenu
 */
Clara.ArmTabCloseMenu = Ext.extend(Object, {
    /**
     * @cfg {String} closeTabText
     * The text for closing the current tab. Defaults to <tt>'Remove Arm'</tt>.
     */
    closeTabText: 'Remove Arm',
    
    constructor : function(config){
        Ext.apply(this, config || {});
    },

    //public
    init : function(tabs){
        this.tabs = tabs;
        tabs.on({
            scope: this,
            contextmenu: this.onContextMenu,
            destroy: this.destroy
        });
    },
    
    destroy : function(){
        Ext.destroy(this.menu);
        delete this.menu;
        delete this.tabs;
        delete this.active;    
    },

    // private
    onContextMenu : function(tabs, item, e){
        this.active = item;
        var m = this.createMenu(),
            disableAll = true,
            disableOthers = true,
            closeAll = m.getComponent('closeall');
        
        m.getComponent('removearm').setDisabled(!item.closable);
        tabs.items.each(function(){
            if(this.closable){
                disableAll = false;
                if(this != item){
                    disableOthers = false;
                    return false;
                }
            }
        });
        m.getComponent('closeothers').setDisabled(disableOthers);
        if(closeAll){
            closeAll.setDisabled(disableAll);
        }
        
        e.stopEvent();
        m.showAt(e.getPoint());
    },
    
    createMenu : function(){
        if(!this.menu){
            var items = [{
                itemId: 'editarm',
                text: 'Edit',
                scope: this,
                handler: this.onEditArm
            }];
            if(this.showCloseAll){
                items.push('-');
            }
            items.push({
                itemId: 'removearm',
                text: 'Remove',
                scope: this,
                handler: this.onRemoveArm
            });
            items.push({
                itemId: 'duplicatearm',
                text: 'Duplicate Arm...',
                scope: this,
                handler: this.onDuplicateArm
            });
            this.menu = new Ext.menu.Menu({
                items: items
            });
        }
        return this.menu;
    },
    
    onRemoveArm : function(){
    	clog("REMOVING ARM");
        //this.tabs.remove(this.active);
    },
    
    onEditArm : function(){
    	clog("EDITING ARM");
    },
    
    onDuplicateArm : function(){
    	clog("DUPING ARM");
    }
});

Ext.preg('armtabclosemenu', Clara.ArmTabCloseMenu);