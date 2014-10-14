package edu.uams.clara.webapp.xml.processor;

import java.io.ByteArrayOutputStream;

public interface BudgetXmlExportService {
	public enum BudgetDocumentType {
		CALENDAR_ONLY("Budget Document (Calendar Only).xls", "budget-document-calendar-only", "Budget Document (Calendar Only)", true), 
		PRICE_ONLY("Budget Document (Price Only).xls", "budget-document-price-only","Budget Document (Price Only)", true), 
		ALL("Budget Document (All).xls", "budget-document","Budget Document (All)", false), 
		FULL("Budget Document (Full).xls", "budget-document-full","Budget Document (Full)", true),
		NO_NOTES("Budget Document (No Notes).xls", "budget-document-no-notes","Budget Document (No Notes)", true),
		NOTES_ONLY("Budget Document (Notes Only).xls", "budget-document-notes-only","Budget Document (Notes Only)", true);

		private BudgetDocumentType(String fileName, String category, String categoryDes,
				boolean active) {
			this.fileName = fileName;
			this.category = category;
			this.categoryDes = categoryDes;
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

		public String getCategoryDes() {
			return categoryDes;
		}

		public void setCategoryDes(String categoryDes) {
			this.categoryDes = categoryDes;
		}

		private String fileName;

		private String category;
		
		private String categoryDes;

		private boolean active;
	}

	ByteArrayOutputStream generateBudgetExcelDocument(String budgetXml,
			BudgetDocumentType budgetDocumentType, long protocolFormId);
}
