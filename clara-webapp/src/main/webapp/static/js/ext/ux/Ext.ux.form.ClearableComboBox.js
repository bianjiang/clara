
Ext.ns('Ext.ux.form');

//--- Variation of ComboBox where first button is clear, instead of second to clear value of the ComboBox

Ext.ux.form.ClearableComboBox = Ext.extend(Ext.form.ComboBox, {
    initComponent: function() {
        this.triggerConfig = {
            tag:'span', cls:'x-form-twin-triggers', cn:[
            {tag: "img", src: Ext.BLANK_IMAGE_URL, cls: "x-form-trigger x-form-clear-trigger"},
            {tag: "img", src: Ext.BLANK_IMAGE_URL, cls: "x-form-trigger"}        
        ]};
        Ext.ux.form.ClearableComboBox.superclass.initComponent.call(this);
    },
    onTrigger1Click : function()
    {
        this.collapse();
        this.reset();                       // clear contents of combobox
        this.fireEvent('cleared');          // send notification that contents have been cleared
    },

    getTrigger: Ext.form.TwinTriggerField.prototype.getTrigger,
    initTrigger: Ext.form.TwinTriggerField.prototype.initTrigger,
    onTrigger2Click: Ext.form.ComboBox.prototype.onTriggerClick,
    trigger1Class: Ext.form.ComboBox.prototype.triggerClass,
    trigger2Class: Ext.form.ComboBox.prototype.triggerClass
});
