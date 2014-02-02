Ext.define('Clara.Meeting.model.Vote', {
    extend: 'Ext.data.Model',
    fields: [{name:'userId', mapping: '@userid'},
             {name:'userFullName', mapping: '@name'},
             {name:'value', mapping:'@value'},
             {name:'note', mapping:'@note'}
     	     ]
});