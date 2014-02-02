function openBudgetBuilder(){
	var url = appContext + "/protocols/"+claraInstance.id+"/protocol-forms/"+claraInstance.form.id+"/budgets/budgetbuilder";
	window.open(url, '','toolbar=no,status=no,width=950,height=650,resizable=yes');
}

function renderIrbFeePanel(){
	var url = appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/"+claraInstance.type+"-form-xml-datas/" + claraInstance.form.xmlDataId + "/";
	
	irbFeeStore = new Ext.data.XmlStore({
		proxy: new Ext.data.HttpProxy({
			url: url + "xml-elements/list",
			method:"GET",
			headers:{'Accept':'application/xml;charset=UTF-8'}
		}),
		record: 'category', 
		autoLoad:true,
		root:'list',
		baseParams:{listPath:'/'+claraInstance.form.xmlBaseTag+'/irb-fees/category'},
		fields: [
		    {name:'id', mapping:'name'},
		    {name:'name', mapping:'name'},
		    {name:'fee', mapping:'fee'}
		]
	});
	
	var canEditIrbFees = claraInstance.HasAnyPermissions('EDIT_IRB_FEE');
	
	irbFeePanel = new Ext.grid.EditorGridPanel({
		frame:false,
		iconCls: 'icn-calculator',
		title:'IRB fees associated with this study',
		renderTo: 'budget-irb-fee-panel',
		width:400,
		height:110,
		store:irbFeeStore,
		listeners:{
			afteredit:function(e){
				var st = e.grid.getStore();
				var xml = "<protocol><irb-fees>";
				st.each(function(rec){
					xml += "<category><name>"+rec.get("name")+"</name><fee>"+rec.get("fee")+"</fee></category>";
				});
				xml += "</irb-fees></protocol>";
				clog(xml);
				var updateUrl = url+"update";
				jQuery.ajax({
					  type: 'POST',
					  async:false,
					  url: updateUrl,
					  data: {
						  pagefragment:xml
					  },
					  success: function(data){
						  clog("update fee: returned",data);
					  },
					  error: function(){
						  cerr("error saving fee xml");
					  },
					  dataType: 'xml'
				});
			}
		},
		colModel: new Ext.grid.ColumnModel({
	        defaults: {
	            width: 120,
	            sortable: true
	        },
	        columns: [
	            {id: 'type', header: 'Fee Type', width: 200, sortable: true, dataIndex: 'name',editable:false},
	            {header: 'Amount', renderer: Ext.util.Format.usMoney, dataIndex: 'fee',editable:canEditIrbFees,
	            	editor: {
                        xtype: 'numberfield',
                        allowBlank: false
                    }
	            }
	        ]
	    }),
	    viewConfig: {
	        forceFit: true
	    }
	});
}