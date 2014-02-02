Ext.ns('Clara.NavigationWizard');

Clara.NavigationWizard.OptionGridPanel = Ext
		.extend(
				Ext.Panel,
				{
					title : "",
					gridStore : {},
					constructor : function(config) {
						Clara.NavigationWizard.OptionGridPanel.superclass.constructor
								.call(this, config);
					},
					initComponent : function() {

						var config = {
							autoScroll : false,
							border : false,
							layout : 'fit',
							items : [ {
								xtype : 'grid',
								border : false,
								split : true,
								store : this.gridStore,
								hideHeaders : true,
								viewConfig : {
									forceFit : true,
									scrollOffset : 0
								},
								columns : [ {
									dataIndex : 'formtype',
									renderer : function(v, p, record) {
										return "<div class='row-newform row-newform-"
												+ record.data.formtype
												+ "'><div class='newform-description'><h3 class='newform-shortdesc'><a href='"
												+ record.data.url
												+ "'>"
												+ record.data.shortdesc
												+ "</a></h3><span class='newform-longdesc'>"
												+ record.data.longdesc
												+ "</span></div></div>";
									}
								} ]
							} ]
						};

						// apply config
						Ext.apply(this, Ext.apply(this.initialConfig, config));

						// call parent
						Clara.NavigationWizard.OptionGridPanel.superclass.initComponent
								.apply(this, arguments);

					}

				});

Ext.reg('navigation-wizard-option-grid-panel',
		Clara.NavigationWizard.OptionGridPanel);

Clara.NavigationWizard.GroupPanel = Ext.extend(Ext.TabPanel, {
	groupItems : [],
	activeItem:0,
	constructor : function(config) {
		Clara.NavigationWizard.GroupPanel.superclass.constructor.call(this,
				config);
	},
	initComponent : function() {
		var  t= this;
		var config = {
			activeItem:t.activeItem,
			items : this.groupItems
		};

		// apply config
		Ext.apply(this, Ext.apply(this.initialConfig, config));

		// call parent
		Clara.NavigationWizard.GroupPanel.superclass.initComponent.apply(this,
				arguments);

	}
});

Ext.reg('navigation-wizard-grouppanel', Clara.NavigationWizard.GroupPanel);

Clara.NavigationWizard.MainWindow = Ext
		.extend(
				Ext.Window,
				{
					id : 'navigation-wizard-window',
					title : 'Welcome to Clara',
					height : 500,
					activeTab:0,
					modal : true,
					width : 750,
					layout : 'border',
					margins : '3 3 3 3',
					cmargins : '3 3 3 3',
					constructor : function(config) {
						Clara.NavigationWizard.MainWindow.superclass.constructor
								.call(this, config);
					},
					initComponent : function() {
						var t = this;
						var config = {

							items : [
									{
										html : "<div class='window-header' style='background-color:#dce6f5;border-bottom:0px;'><h1 class='window-header-title'>What do you want to do?</h1></div>",
										border : false,
										region : 'north'
									},
									new Clara.NavigationWizard.GroupPanel(
											{
												border : false,
												region : 'center',
												activeItem:t.activeTab,
												groupItems : [
														{
															xtype : 'navigation-wizard-option-grid-panel',
															iconCls:'icn-book-open-list',
															title : "<h1 class='header-title'>I want to submit a(n)...</h1>",
															gridStore : Clara.Config.NewProtocolOptionStore
														},
														{
															xtype : 'navigation-wizard-option-grid-panel',
															iconCls:'icn-certificate',
															title : "<h1 class='header-title'>Create a New Contract</h1>",
															gridStore : Clara.Config.NewContractOptionStore
														},
														{
															xtype : 'navigation-wizard-option-grid-panel',
															iconCls:'icn-question',
															title : "<h1 class='header-title'>Find Help</h1>",
															gridStore : Clara.Config.HelpOptionStore
														} ]
											}),
									{
										xtype : 'panel',
										region : 'south',
										height : 32,
										border : false,
										padding : 6,
										layout : 'fit',
										items : [ {
											xtype : 'checkbox',
											boxLabel : 'Do not show this window the next time I log in.',
											listeners : {
												check : function(t, val) {
													if (val == true)
														Ext.util.Cookies
																.set(
																		"claraHideNewWizard",
																		"yes");
													else
														Ext.util.Cookies
																.set(
																		"claraHideNewWizard",
																		"no");
												}
											}
										} ]
									} ],
							buttons : [ {
								text : 'Close',
								handler : function() {
									t.close();
								}
							} ]
						};
						Ext.apply(this, Ext.apply(this.initialConfig, config));
						Clara.NavigationWizard.MainWindow.superclass.initComponent
								.apply(this, arguments);
					}
				});

Ext.reg('navigation-wizard-mainwindow', Clara.NavigationWizard.MainWindow);