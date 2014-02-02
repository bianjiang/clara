Ext.define('Clara.Common.store.MessagePosts', {
    extend: 'Ext.data.Store',
	//extend: 'Ext.data.ArrayStore',
	requires: 'Clara.Common.model.MessagePost',    
    model: 'Clara.Common.model.MessagePost',
    autoLoad: true 
    // data: [[1,'"NCT Number Required" for ClinicalTrial.gov studies',123123,null,'<b><span style="background-color: rgb(255, 255, 153);">Note to PIs and reviewers</span><span style="background-color: rgb(255, 255, 255);">:</span></b><span style="background-color: rgb(255, 255, 255);"> If your study requires registration at ClinicalTrials.gov, you must enter an NCT number in order to complete your submission or modification. Without an NCT number, your protocol may be held up for review until you receive one. The regulations regarding NCT umbers can be found at&nbsp;</span><a href="http://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNMattersArticles/Downloads/MM8401.pdf" style="background-color: rgb(255, 255, 255);">http://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNMattersArticles/Downloads/MM8401.pdf</a>','INFO']]
});