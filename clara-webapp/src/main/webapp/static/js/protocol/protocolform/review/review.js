Ext.ns('Clara', 'Clara.Reviewer');
var viewport;
Clara.Reviewer.EditingForm = false;

Clara.Reviewer.MessageBus.addListener('editingform', function () {
    Clara.Reviewer.EditingForm = true;
    clog("Clara.Reviewer.MessageBus: editingform called.", Clara.Reviewer.EditingForm);
});

var commentTypeData = [
    ['NOTE', 'Note'],
    ['CONTINGENCY', 'Contingency'],
    ['COMMITTEE_PRIVATE_NOTE', 'Committee Private Note']
];

var contingencyTypeData = [
    ['FORMATTING', 'Formatting Issues'],
    ['OTHER', 'Other']
];

var rowExpander;

function popupProtocolFormSummary(protocolFormId) {
    var url = appContext + '/' + claraInstance.type + '/' + claraInstance.form.xmlDataId + '/summary';
    window
        .open(url, "Protocol Summary",
        'toolbar=no,status=no,width=1024,height=900,resizable=yes, scrollbars=yes');
}

function completeReview(fId, cId, username, password, xml) {
    jQuery.ajax({
        url: appContext + "/ajax/" + claraInstance.type + "s/" + claraInstance.id + "/" + claraInstance.type + "-forms/" + claraInstance.form.id + "/review/complete",
        type: "POST",
        async: false,
        data: {
            xmlData: '', // xml,
            committee: cId,
            username: username,
            password: password,
            userId: claraInstance.user.id
        },
        success: function (data) {
            clog(data);
            Ext.getCmp("winCompleteReview").close();
        },
        error: function () {
            alert("Oops something went wrong.");
        }
    });
}


Clara.Reviewer.OverviewPanel = Ext.extend(Ext.Panel, {
    id: 'clara-overviewpanel',
    frame: false,
    layout: 'fit',
    border: false,
    height: 350,
    agendaItem: {},
    viewAsAgendaItem: false,
    agenda: {},


    protocolInfoEl: "",
    constructor: function (config) {
        Clara.Reviewer.OverviewPanel.superclass.constructor.call(this, config);
    },

    initComponent: function () {
        var t = this;

        var iframeHtml = '<iframe style="overflow:auto;width:100%;height:100%;" frameborder="0" id="formOverview" src="' + appContext + '/' + claraInstance.type + 's/' + claraInstance.id + '/summary?noheader=true"></iframe>';
        var config = {
            html: iframeHtml,
            iconCls: 'icn-book',
            title: 'Overview',
            border: false
        };
        Ext.apply(this, Ext.apply(this.initialConfig, config));
        Clara.Reviewer.OverviewPanel.superclass.initComponent.apply(this, arguments);
    }
});
Ext.reg('claraoverviewpanel', Clara.Reviewer.OverviewPanel);


function renderFormReviewViewport() {


    var reviewTabItems = [{
        id: 'reviewer-othercontingencygrid-panel',
        xtype: 'reviewer-contingencygrid-panel',
        onReviewPage:true,
        autoLoadComments:true,
        committeeExcludeFilter: [claraInstance.user.committee]
    },{
            id: 'reviewer-contingencygrid-panel',
            xtype: 'reviewer-contingencygrid-panel',
            onReviewPage:true,
            autoLoadComments:false,
            committeeIncludeFilter: [claraInstance.user.committee]
        }
    ];

    if (claraInstance.user.committee == "PI") { // Hide "my committee
        reviewTabItems = [{
                id: 'reviewer-othercontingencygrid-panel',
                xtype: 'reviewer-contingencygrid-panel',
                autoLoadComments:true,
                committeeExcludeFilter: [claraInstance.user.committee],
                title: 'Review Committee Notes'
            }
        ];
    }



    var iframeHtml = '<iframe style="overflow:auto;width:100%;height:100%;" frameborder="0" id="formSummary" src="' + appContext + '/' + claraInstance.type + 's/' + claraInstance.id + '/' + claraInstance.type + '-forms/' + claraInstance.form.id + '/' + claraInstance.form.urlName + '/summary';
    iframeHtml = iframeHtml + '?noheader=true&committee=' + claraInstance.user.committee + '&review=' + (!claraInstance.form.readOnly) + '" />';

    var bbarItems = [];



    bbarItems.push('->');
    bbarItems.push({
        text:'Complete...',
        id: 'btnCompleteReview',
        height: 38,
        itemCls: 'review-toolbar-label',
        iconCls: 'icn-arrow',
        handler: function () {

            var finalReviewUrl = "";

            if (claraInstance.user.committee == 'IRB_REVIEWER') {
                finalReviewUrl = appContext + "/queues?fromQueue=" + fromQueue;
            } else {
                finalReviewUrl = appContext + "/" + claraInstance.type + "s/" + claraInstance.id + "/" + claraInstance.type + "-forms/" + claraInstance.form.id + "/review/complete?committee=" + claraInstance.user.committee + "&fromQueue=" + fromQueue;
            }


            //if (Clara.Reviewer.EditingForm === true || claraInstance.user.committee === "PI") {
                new Clara.Reviewer.FormErrorWindow({
                    success: function () {
                        location.href = finalReviewUrl;
                    }
                }).show();
            //} else {
           //     location.href = finalReviewUrl;
           // }


        }
    });

    var reviewMainTabItems = [{
            id: 'left-review-panel',
            xtype: 'panel',
            title: 'Form Summary',
            iconCls: 'icn-application-form',
            border: false,
            html: iframeHtml,
            defaults: {
                layout: 'fit'
            }
        }, {
            xtype: 'claradocumentpanel',
            title: 'Documents',
            iconCls: 'icn-folder-open-document-text',
            border: false,
            readOnly: false,
            allDocs: true,
            debug: true
        }, {
            id: 'form-status-panel',
            xtype: 'claraformstatusgridpanel',
            title: 'Status',
            formId: claraInstance.form.id,
            iconCls: 'icn-users',
            autoload: true,
            border: false,
            showActions: false
        }, {
            xtype: 'clarahistorypanel',
            type: claraInstance.type
        }
    ];

    if (claraInstance.type != "contract" && claraInstance.form.type != "HUMAN_SUBJECT_RESEARCH_DETERMINATION") { // Dont show protocol overview for "HUMAN_SUBJECT_RESEARCH_DETERMINATION"
    	reviewMainTabItems.splice(0,0, {
            xtype: 'claraoverviewpanel'
        });
    	reviewMainTabItems.splice(3,0, {
            xtype: 'clarasimpleprotocolformgridpanel',
            title:'Other Forms in this Study'
        });
    }

    viewport = new Ext.Viewport({
        // renderTo: 'protocol-form-review',
        layout: 'border',

        items: [{
                region: 'north',
                contentEl: 'clara-header',
                bodyStyle: {
                    backgroundColor: 'transparent'
                },
                height: 42,
                border: false,
                margins: '0 0 0 0'
            }, {
                xtype: 'panel',
                border: false,
                region: 'center',
                layout: 'border',
                bbar: new Ext.Toolbar({
                    id: 'review-statusbar',
                    height: 42,
                    items: bbarItems
                }),
                items: [{
                        xtype: 'container',
                        contentEl: 'summary-bar',
                        region: 'north',
                        border: false,
                        height: 40
                    }, {

                        id: 'left-review-tabpanel',
                        xtype: 'tabpanel',
                        activeItem: (claraInstance.type != "contract" && claraInstance.form.type != "HUMAN_SUBJECT_RESEARCH_DETERMINATION")?1:0,

                        split: true,
                        region: 'center',
                        margins: '6',
                        cmargins: '6',
                        items: [reviewMainTabItems]
                    }, {
                        xtype: 'panel',
                        margins: '6',
                        cmargins: '6',
                        region: 'east',
                        width: '35%',
                        border: true,
                        split: true,
                        
                        layout: 'fit',
                        
                        listeners: {
                            afterrender: function (p) {
                                var checkUrl = appContext + "/ajax/" + claraInstance.type + "s/" + claraInstance.id + "/" + claraInstance.type + "-forms/" + claraInstance.form.id + "/review/checklists/committee-checklist.xml?committee=" + claraInstance.user.committee + "&formType=" + claraInstance.form.type;
                                jQuery.ajax({
                                    url: checkUrl,
                                    type: "GET",
                                    dataType:'text',
                                    async: false,
                                    success: function (data) {
                        
                                        clog("check: CHECKLISTS?", data,(data == "<result>true</result>"));
                                        Ext.getCmp("btnShowChecklist").setVisible((data == "<result>true</result>"));
                                    }
                                });
                            }
                        },
                        disabled: (claraInstance.type == "contract"),
                        hidden: (claraInstance.type == "contract"),
                        tbar: [{
                                xtype: 'tbtext',
                                text: '<span style="font-weight:800;">Notes</span>'
                            }, '->', {
                                xtype: 'button',
                                hidden: true,
                                id: 'btnShowChecklist',
                                text: 'Show Checklist',
                                iconCls: 'icn-ui-check-boxes',
                                handler: function () {
                                    var url = appContext + "/" + claraInstance.type + "s/" + claraInstance.id + "/" + claraInstance.type + "-forms/" + claraInstance.form.id + "/review/checklists/committee-checklist.xml?committee=" + claraInstance.user.committee + "&formType=" + claraInstance.form.type;
                                    window.open(url);
                                }
                            }
                        ],
                        items: [{

                                id: 'right-review-panel',
                                xtype: 'tabpanel',
                                border: false,
                                activeItem: 0,
                                items: [reviewTabItems]

                            }
                        ]
                    }
                ]
            }
        ]
    });



}
