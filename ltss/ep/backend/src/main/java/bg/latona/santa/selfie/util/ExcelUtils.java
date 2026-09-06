package bg.latona.santa.selfie.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static bg.latona.santa.selfie.constant.electricityInvoiceRelated.ElectricityInvoiceConstants.*;


public class ExcelUtils {

    public static void fillExcelInvoiceTemplate(
            double[] doubleValues,
            String[] stringValues,
            String outputFilePath
    ) throws IOException {

        FileInputStream fileInputStream = new FileInputStream(INVOICE_EXCEL_TEMPLATE_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(fileInputStream);

        Sheet sheet = workbook.getSheetAt(0);

        // Set Double Cells
        //---------------------------------------------------------------------------------------------

        for (int i = 0; i < INVOICE_TEMPLATE_DOUBLE_CELL_POSITIONS.length; i++) {
            int rowNum = INVOICE_TEMPLATE_DOUBLE_CELL_POSITIONS[i][0];
            int colNum = INVOICE_TEMPLATE_DOUBLE_CELL_POSITIONS[i][1];

            Row row = sheet.getRow(rowNum);
            if (row == null) {
                row = sheet.createRow(rowNum);
            }

            Cell cell = row.getCell(colNum);

            if (cell == null) {
                cell = row.createCell(colNum);
            }

            cell.setCellValue(doubleValues[i]);
        }

        // Set String Cells
        //---------------------------------------------------------------------------------------------

        for (int i = 0; i < INVOICE_TEMPLATE_STRING_CELL_POSITIONS.length; i++) {
            int rowNum = INVOICE_TEMPLATE_STRING_CELL_POSITIONS[i][0];
            int colNum = INVOICE_TEMPLATE_STRING_CELL_POSITIONS[i][1];

            Row row = sheet.getRow(rowNum);
            if (row == null) {
                row = sheet.createRow(rowNum);
            }

            Cell cell = row.getCell(colNum);

            if (cell == null) {
                cell = row.createCell(colNum);
            }

            cell.setCellValue(stringValues[i]);
        }

        //---------------------------------------------------------------------------------------------

        fileInputStream.close();

        FileOutputStream fileOutputStream = new FileOutputStream(outputFilePath + ".xlsx");

        workbook.write(fileOutputStream);

        fileOutputStream.close();
        workbook.close();
    }
}
