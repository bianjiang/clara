Ext.ns('Clara.Contract');

Clara.Contract.StudyDetailPanel = Ext.extend(Ext.Container, {
    title: 'Study Details',
    layout: 'fit',
    protocolId:null,
	constructor:function(config){		
		Clara.Contract.StudyDetailPanel.superclass.constructor.call(this, config);
	},
	listeners:{
		activate:function(c){
			if (c.protocolId && c.protocolId > 0){
				var protocolId = c.protocolId;
				var studyUrl = "/protocols/"+protocolId+"/dashboard";
				
			var url = appContext+"/ajax/protocols/"+protocolId+"/metadata";
				jQuery.ajax({
					  type: 'GET',
					  async:false,
					  url: url,
					  success: function(xml){
						  
						  var protocol = jQuery(xml).find("protocol");
						  var title = protocol.find("title").text();
						  var status = protocol.find("status").text();
						  var studyType = protocol.find("study-type").text();
	
						  if (studyType.indexOf('investigator-initiated') !== -1){
							  studyType = 'Investigator Initiated';
						  } else if (studyType.indexOf('industry') !== -1){
							  studyType = 'Industry';
						  } else if (studyType.indexOf('cooperative-group') !== -1){
							  studyType = 'Cooperative Group';
						  }
						  
						  var principleInvestigator = protocol.find("staffs staff user roles role:contains('Principal Investigator')").parent().parent().find('lastname').text();
						  principleInvestigator += ',' + protocol.find("staffs staff user roles role:contains('Principal Investigator')").parent().parent().find('firstname').text();
						  
						  var studyContact = protocol.find("staffs staff user roles role:contains('Study Coordinator')").parent().parent().find('lastname').text();
						  studyContact += ',' + protocol.find("staffs staff user roles role:contains('Study Coordinator')").parent().parent().find('firstname').text();
	
						  var funding = (protocol.find("funding funding-source").length != 0)?'Yes':'No';
						  
						  var department = protocol.find("funding funding-source[type='None']").attr('department');
						  var fund = protocol.find("funding funding-source[type='None']").attr('name');
						  var costCenter = protocol.find("funding funding-source[type='None']").attr('entityname');
						  
						  var html = '<ul style="list-style: disc outside none;">';
						  html += '<li class="bulletitem"><b>Study Title:</b> ' + title + "</li>";
						  html += '<li class="bulletitem"><b>IRB Number:</b> ' + protocolId + "</li>";
						  html += '<li class="bulletitem"><b>Study Type:</b> ' + studyType + "</li>";
						  html += '<li class="bulletitem"><b>Principal Investigator:</b> ' + principleInvestigator + "</li>";
						  html += '<li class="bulletitem"><b>Study Contact:</b> ' + studyContact + "</li>";
						  html += '<li class="bulletitem"><b>Does this study have funding?</b> ' + funding + "</li>";
						  html += '<li class="bulletitem"><b>Department:</b> ' + department + "</li>";
						  html += '<li class="bulletitem"><b>Fund:</b> ' + fund + "</li>";
						  html += '<li class="bulletitem"><b>Cost Center:</b> ' + costCenter + "</li>";
						  html += '<li class="bulletitem"><b>Status:</b> ' + status + "</li>";
						  html += "</ul>";
						  jQuery("#study-details-wrapper").html(html);
	
					  },
					  error: function(){
						  alert("Error loading study information.");
					  },
					  dataType: 'xml'
				});
			} else {
				jQuery("#study-details-wrapper").html("<h2>There is not study attached to this protocol.</h2>");
			}
		}
	},
	initComponent: function() {
		var t = this;
		var config = {
				disabled:(this.protocolId == null)?true:false,
				html:'<div id="study-details-wrapper">Loading study information..</div>'
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Contract.StudyDetailPanel.superclass.initComponent.apply(this, arguments);
		
	}
});
Ext.reg('claracontractstudydetailpanel', Clara.Contract.StudyDetailPanel);