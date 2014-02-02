package edu.uams.clara.webapp.protocol.businesslogic.irb.agenda.impl;

import javax.xml.parsers.ParserConfigurationException;

import edu.uams.clara.webapp.protocol.businesslogic.ProtocolBusinessObjectStatusHelper;

/***
 * Agenda Item only ties to Protocol, AgendaItemBusissnessObjectStatusHelperImpl extends ProtocolBusinessObjectStatusHelper to deal with status changes when an AgendaItem is assigned
 * to certain review queue
 * @author bianjiang
 *
 */
public class AgendaItemBusissnessObjectStatusHelperImpl extends
ProtocolBusinessObjectStatusHelper {

	public AgendaItemBusissnessObjectStatusHelperImpl()
			throws ParserConfigurationException {
		super();
	}
}
