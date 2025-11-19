package com.utility;

import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;

public class ExcelReader {

	private static final Logger log = LoggerHelper.getLogger(ExcelReader.class);

	public static void main(String[] args) throws IOException {
		// Specify the path to your Excel file
		FileInputStream fis = new FileInputStream(ConfigReader.getProperty("excelPath"));

		// Create a Workbook instance
		Workbook workbook = new XSSFWorkbook(fis);

		// Get the first sheet from the workbook
		Sheet sheet = workbook.getSheetAt(0);

		// Iterate through each row in the sheet
		for (Row row : sheet) {
			StringBuilder rowData = new StringBuilder();
			// Iterate through each cell in the row
			for (Cell cell : row) {
				// Append the cell's value
				rowData.append(cell.toString()).append("\t");
			}
			log.debug(rowData.toString());
		}

		// Close the workbook
		workbook.close();
		fis.close();
	}
}
