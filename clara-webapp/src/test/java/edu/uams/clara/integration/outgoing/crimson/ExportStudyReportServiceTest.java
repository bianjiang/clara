package edu.uams.clara.integration.outgoing.crimson;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.integration.outgoing.crimson.impl.ExportStudyReportServiceImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/integration/outgoing/crimson/ExportStudyReportServiceTest-context.xml" })

public class ExportStudyReportServiceTest {

	private ExportStudyReportServiceImpl exportStudyReportServiceImpl;

	@Test
	public void exportRport() throws IOException {
		exportStudyReportServiceImpl.exportCSVFiles();
	}

	public ExportStudyReportServiceImpl getExportStudyReportServiceImpl() {
		return exportStudyReportServiceImpl;
	}

	@Autowired(required = true)
	public void setExportStudyReportServiceImpl(
			ExportStudyReportServiceImpl exportStudyReportServiceImpl) {
		this.exportStudyReportServiceImpl = exportStudyReportServiceImpl;
	}

}
