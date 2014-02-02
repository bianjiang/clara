Ext.ns('Clara', 'Clara.Reviewer');
Clara.Reviewer.SimpleFormGridPanel = Ext.extend(Ext.grid.GridPanel, {
	id: 'clara-protocol-db-simpleformgridpanel',
	constructor:function(config){		
		Clara.Reviewer.SimpleFormGridPanel.superclass.constructor.call(this, config);
	},	
    selectedFormId:null,
    iconCls:'icn-book-open',
	initComponent: function(){
		var t=this;
		t.selectedFormId = claraInstance.form.id || null;
		var config = {
				listeners: {
					show:function(gp){
						gp.store.load();
					}
				},
				store: new Ext.data.XmlStore({
					scope:this,
					proxy: new Ext.data.HttpProxy({
						scope:this,
						url: appContext + "/ajax/protocols/"+claraInstance.id+"/protocol-forms/list.xml", //
						method:"GET",
						headers:{'Accept':'application/xml;charset=UTF-8'}
					}),
					record: 'protocol-form', 
					autoLoad:false,
					root:'list',
					fields: [
					    {name:'protocolFormId', mapping: '@protocolFormId'},
						{name:'formtype', mapping:'protocol-form-type'},
						{name:'url',mapping:'url'},
						{name:'studynature', mapping:'details>study-nature'},
						{name:'editurl',mapping:'editurl'},
						{name:'locked',mapping:'status>lock@value'},
						{name:'lockUserId',mapping:'status>lock@userid'},
						{name:'lockModified',mapping:'status>lock@modified', type: 'date', dateFormat: 'Y-m-d H:i:s.u'},
						{name:'lockMessage',mapping:'status>lock@message'},
						{name:'status', mapping:'status>description'},
						{name:'agendaDate', mapping:'status>agenda>assigned-date', type: 'date', dateFormat: 'm/d/Y'},
						{name:'statusModified',mapping:'status>modified', type: 'date', dateFormat: 'Y-m-d H:i:s.u'},
						{name:'details',convert:function(v,node){ return new Ext.data.XmlReader({record: 'value',fields: [{name:'detailName', mapping:'@name'},{name:'detailValue',mapping:''}]}).readRecords(node).records; }},
						{
							name : 'assignedReviewers',
							mapping:'assigned-reviewers',
							convert : function(v, node) {
								var recs = assignedReviewerXmlReader.readRecords(node).records;
							    return recs;
							}
						}
						
					]
				}),
		        viewConfig: {
		    		forceFit:true,
		    		loadMask:true	
		    	},
		        columns: [

		            {header: 'Form', width: 270, sortable: true, dataIndex: 'formtype', renderer:function(value, p, record){
		            	var locked = record.get("locked");
		        		var url=record.data.url;
		        		var summaryurl=appContext+"/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/"+record.get("protocolFormId")+"/"+record.get("formtype")+"/summary?noheader=true";
		        		var formType = record.data.formtype;
		        		var str = (locked === "true")?"<div class='form-row form-row-locked'>":"<div class='form-row'>";
		        		str += (record.get("studynature") != "")?("<span class='protocol-form-row-field protocol-form-type'>"+formType+"</span><div class='studynature'>"+Clara.Protocols.NameMappings.studyNature[record.get("studynature")]+"</div>"):("<span class='protocol-form-row-field protocol-form-type'>"+formType+"</span>");
		        		str += (locked === "true")?"<div style='font-weight:800;border:1px solid red; padding:4px;margin-top:2px;'>"+record.get("lockMessage")+"</div>":"";
		        		str += (t.selectedFormId != null && t.selectedFormId == record.get("protocolFormId"))?"<div style='font-weight:800;border:1px solid blue; background:#99ccff;padding:4px;margin-top:2px;'>You are reviewing this form.</div>":"<div style='font-size:12px;font-weight:800;margin-top:2px;'><a href='"+summaryurl+"' target='_blank'>View summary</a></div>";
		        		if (record.get("details").length > 0){
		        			str += "<dl class='protocol-form-row-details'>";
		        			var a = record.get("details");
		        			clog("actions:",a);
		        			for (var i=0; i<a.length; i++) {
		        				str += "<dt>" + a[i].get("detailName") + "</dt>";
		        				str += "<dd>" + a[i].get("detailValue") + "</dd>";
		        			}
		        			str += "</dl>";
		        		}
		        		return str+"</div>";
		            }},
		            {header: 'Last Modified', width: 95, sortable: true, renderer: function(value) { return "<span class='protocol-form-row-field'>"+Ext.util.Format.date(value,'m/d/Y')+"</span>";}, dataIndex: 'statusModified'},
		            {header: 'Status', id:'protocol-forms-status-column', width: 215, sortable: true, dataIndex: 'status', 
		            	renderer:function(v,p,record){
		            		var html = "<div class='wrap'><span class='protocol-form-row-field protocol-form-status'>"+v+"</span>";
		            		if (record.get("agendaDate")) {
		            			var dateClass = "label-info";
		            			if (moment(record.get("agendaDate")).add('days',1) < moment(new Date()).add('days',0)) dateClass = "label-disabled";
		            			html += "<div class='protocol-form-row-field label "+dateClass+"'>On IRB agenda: "+moment(record.get("agendaDate")).format('L');+"</div>";
		            		}
		            		return html+"</div>";
		            	}
		            },
					{
						header : 'Assigned To',
						dataIndex : 'assignedReviewers',
						sortable : true,
						width : 230,
						renderer : function(v) {
							var h = "<ul class='form-list-row form-assigned-reviewers'>";
								for(var i=0;i<v.length;i++) {
									clog(v[i]);
									h +="<li class='form-assigned-reviewer'>"+Clara.HumanReadableRoleName(v[i].data.reviewerRoleName)+": "+v[i].data.reviewerName + "</li>";
								}
								h += "</ul>";
									
							return h;
						}
					}
		        ]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Reviewer.SimpleFormGridPanel.superclass.initComponent.apply(this, arguments);

		
	}
});
Ext.reg('clarasimpleprotocolformgridpanel', Clara.Reviewer.SimpleFormGridPanel);