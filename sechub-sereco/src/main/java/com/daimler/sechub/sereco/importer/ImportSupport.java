// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer;

import com.daimler.sechub.sereco.ImportParameter;

/**
 * Support to check if an importer can handle JSON or XML content. It simply
 * checks if content contains one wellknown part
 *
 * @author Albert Tregnaghi
 *
 */
public class ImportSupport {

	private static final char BOM = 65279;

	private String productId;

	private String contentIdentifier;

	private boolean checkXML;

	private boolean checkJSON;

	/**
	 * Create support
	 *
	 * @param productIdParts
	 *            - the parts to identify the productId
	 */
	private ImportSupport() {
	}

	public static ImportSupportBuilder builder() {
		return new ImportSupportBuilder();
	}

	public static class ImportSupportBuilder {
		String productId;
		String contentIdentifier;

		private boolean checkXML;

		private boolean checkJSON;

		private ImportSupportBuilder() {

		}

		public ImportSupportBuilder productId(String productId) {
			if (productId == null) {
				this.productId = null;
			} else {
				this.productId = productId.toLowerCase();
			}
			return this;
		}

		public ImportSupportBuilder contentIdentifiedBy(String contentIdentifier) {
			this.contentIdentifier = contentIdentifier;
			return this;
		}

		public ImportSupportBuilder mustBeXML() {
			this.checkXML = true;
			return this;
		}

		public ImportSupportBuilder mustBeJSON() {
			this.checkJSON = true;
			return this;
		}

		public ImportSupport build() {
			validate();
			ImportSupport support = new ImportSupport();

			support.checkJSON = checkJSON;
			support.checkXML = checkXML;
			support.contentIdentifier = contentIdentifier;
			support.productId = productId;

			return support;
		}

		private void validate() {
			if (checkJSON && checkXML) {
				throw new IllegalStateException("You have defined xml and JSON - this makes no sense!");
			}
		}
	}

	/**
	 * Checks if the given import parameter can be imported or not
	 * @param parameter
	 * @return
	 */
	public ProductImportAbility isAbleToImport(ImportParameter parameter) {
		if (!isProductIdentified(parameter.getProductId())) {
			return ProductImportAbility.NOT_ABLE_TO_IMPORT;
		}
		if (isEmpty(parameter.getImportData())) {
			return ProductImportAbility.PRODUCT_FAILED;
		}
		if (!isContentIdentified(parameter.getImportData())) {
			return ProductImportAbility.NOT_ABLE_TO_IMPORT;
		}
		if (checkXML && !isXML(parameter.getImportData())) {
			return ProductImportAbility.NOT_ABLE_TO_IMPORT;
		}
		if (checkJSON && !isJSON(parameter.getImportData())) {
			return ProductImportAbility.NOT_ABLE_TO_IMPORT;
		}
		return ProductImportAbility.ABLE_TO_IMPORT;

	}

	private boolean isProductIdentified(String productId) {
		if (this.productId == null) {
			return true; // when not set we accept all
		}
		if (productId == null) {
			return false;
		}
		String lowerCased = productId.trim().toLowerCase();
		return lowerCased.equals(this.productId);
	}

	private boolean isContentIdentified(String content) {
		if (content == null) {
			return false;
		}
		if (contentIdentifier == null) {
			return true; /* always.. */
		}
		return content.contains(contentIdentifier);
	}

	boolean isEmpty(String data) {
		if (data==null) {
			return true;
		}
		if (data.length()==0) {
			return true;
		}
		return false;
	}

	boolean isJSON(String data) {
		return createSubStringWithoutBOMandLowercased(data, 1).startsWith("{");
	}

	boolean isXML(String data) {
		return createSubStringWithoutBOMandLowercased(data, 5).startsWith("<?xml");
	}

	private String createSubStringWithoutBOMandLowercased(String origin, int size) {
		if (origin == null) {
			return "";
		}
		int pos = 0;
		int length = origin.length();
		StringBuilder sb = new StringBuilder();

		while (sb.length() <= size && pos < length) {
			char c = origin.charAt(pos++);
			if (c != BOM) {
				sb.append(c);
			}
		}
		return sb.toString().toLowerCase();

	}

}
