Ext.grid.WordWrapColumnModel = Ext.extend(Ext.grid.ColumnModel, {
 
   initComponent: function(){
      Ext.grid.WordWrapColumnModel.superclass.initComponent.apply(this, arguments);
   },
	
   getRenderer : function(col){
      if(!this.config[col].renderer){
         if (typeof(this.config[col].wordWrap)=='undefined' || this.config[col].wordWrap==true){
            return Ext.grid.ColumnModel.wordWrapRenderer;
         } else {
            return Ext.grid.ColumnModel.defaultRenderer;
         }
      }
      return this.config[col].renderer;
   },
 
   onRender: function(){
      Ext.grid.WordWrapColumnModel.superclass.onRender.apply(this, arguments);
   }
});

Ext.grid.ColumnModel.wordWrapRenderer = function(value){
    return '<div style="white-space:normal !important;">' + value + '</div>';
};

Ext.reg('wordwrapcolumnmodel', Ext.grid.WordWrapColumnModel);