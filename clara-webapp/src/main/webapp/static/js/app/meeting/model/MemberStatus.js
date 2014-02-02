Ext.define('Clara.Meeting.model.MemberStatus', {
    extend: 'Ext.data.Model',
    fields: [
             {name:'timestamp',mapping:'@ts', type:'timestamp'},
             {name:'value', mapping:'@value'},
             {name:'note', mapping:'@note'}
     	     ]
});