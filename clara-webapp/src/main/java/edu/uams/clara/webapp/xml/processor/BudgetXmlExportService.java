package edu.uams.clara.webapp.xml.processor;

import java.io.ByteArrayOutputStream;

public interface BudgetXmlExportService {
	public enum BudgetDocumentType {
		CALENDAR_ONLY("Budget Document (Calendar Only).xls", "budget-document-calendar-only", true), 
		PRICE_ONLY("Budget Document (Price Only).xls", "budget-document-price-only", true), 
		ALL("Budget Document (All).xls", "budget-document", false), 
		FULL("Budget Document (Full).xls", "budget-document-full", true),
		NO_NOTES("Budget Document (No Notes).xls", "budget-document-no-notes", true),
		NOTES_ONLY("Budget Document (Notes Only).xls", "budget-document-notes-only", true);

		private BudgetDocumentType(String fileName, String category,
				boolean active) {
			this.fileName = fileName;
			this.category = category;
			this.active = active;
		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getCategory() {
			return category;
		}

		public void setCategory(String category) {
			this.category = category;
		}

		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}

		private String fileName;

		private String category;

		private boolean active;
	}

	ByteArrayOutputStream generateBudgetExcelDocument(String budgetXml,
			BudgetDocumentType budgetDocumentType, long protocolFormId);
}
