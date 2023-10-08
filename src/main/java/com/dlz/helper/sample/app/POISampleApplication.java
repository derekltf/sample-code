package com.dlz.helper.sample.app;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class POISampleApplication {

	private static final String[] TYPE_ALIAS = { "A", "B", "C", "D", "E", "F", "G" };
	private static final String TEMPLATE_DELETE = "DELETE FROM ::REF_TABLE:: A WHERE ::WHERE_STATEMENT::";
	private static final String TEMPLATE_EXISTS = "EXISTS (SELECT 1 FROM ::ROOT_TABLE:: ::ROOT_TABLE_ALIAS:: WHERE ::WHERE_STATEMENT:: )";

	public void test() {
		System.out.println("Hello");
	}

	public void generateSql() {
		System.out.println("Called!");
		try (InputStream inp = new FileInputStream("X:\\Temp\\sqlData.xlsx");
				XSSFWorkbook workbook = new XSSFWorkbook(inp);) {
			XSSFSheet s1 = workbook.getSheetAt(0);
			List<Row> rowList = IteratorUtils.toList(s1.iterator());

			for (int i = 0; i < rowList.size(); i++) {

				if (i == 0) {
					// skip
					continue;
				}

				XSSFRow r = (XSSFRow) rowList.get(i);

				int level = (int) r.getCell(1).getNumericCellValue();
				String rootTable = r.getCell(2).getStringCellValue();
				String rootTableKey = r.getCell(4).getStringCellValue();
				String refTable = r.getCell(5).getStringCellValue();
				String refTableKey = r.getCell(7).getStringCellValue();
				int currAlias = 0;
				int nextAlias = 1;

				String sql = "";
				if (level == 1) {
					sql = "DELETE FROM " + refTable + " " + TYPE_ALIAS[currAlias] + " WHERE " + TYPE_ALIAS[currAlias]
							+ ".HSE_SRVC_APLY_KEY IN (???)";
				} else {
					boolean isCompleted = false;
					int loopCnt = 0;
					int curLevel = level;
					sql = TEMPLATE_DELETE.replace("::REF_TABLE::", refTable);

					while (!isCompleted) {

						if (loopCnt == 0) {
							String subSql = generateExistsStatement(rootTable, rootTableKey, refTableKey, currAlias,
									nextAlias);
							sql = sql.replace("::WHERE_STATEMENT::", subSql);
						} else {
							Row nextRow = findRootRow(rowList, i, rootTable);
							rootTable = nextRow.getCell(2).getStringCellValue();
							rootTableKey = nextRow.getCell(4).getStringCellValue();
							refTableKey = nextRow.getCell(7).getStringCellValue();

							String subSql = generateExistsStatement(rootTable, rootTableKey, refTableKey, currAlias,
									nextAlias);

							sql = sql.replace("::WHERE_STATEMENT::", "AND " + subSql);

							curLevel--;
						}

						if (curLevel == 2) {
							sql = sql.replace("::WHERE_STATEMENT::",
									"AND " + TYPE_ALIAS[nextAlias] + ".HSE_SRVC_APLY_KEY IN (???)");
							isCompleted = true;
						}

						loopCnt++;
						currAlias++;
						nextAlias++;
					}

				}

				System.out.println("Row: " + i + " : " + sql);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("End!");
	}

	private XSSFRow findRootRow(List<Row> rowList, int currInd, String rootTable) {
		for (int i = currInd; i >= 0; i--) {
			Row r = rowList.get(i);
			String refTable = r.getCell(5).getStringCellValue();

			if (refTable.trim().equals(rootTable.trim()))
				return (XSSFRow) r;

		}

		return null;
	}

	private String generateExistsStatement(String rootTable, String rootRefKey, String refRefKey, int currAlias,
			int nextAlias) throws Exception {

		String[] rootKeySplit = rootRefKey.split(",");
		String[] refKeySplit = rootRefKey.split(",");
		String where = "";

		if (rootKeySplit.length != refKeySplit.length) {
			throw new Exception("Key length not match!");
		}

		for (int i = 0; i < rootKeySplit.length; i++) {
			if (i != 0)
				where += " AND ";
			where += TYPE_ALIAS[currAlias] + "." + rootKeySplit[i].trim() + " = " + TYPE_ALIAS[nextAlias] + "."
					+ refKeySplit[i].trim();
		}

		String sql = TEMPLATE_EXISTS.replace("::ROOT_TABLE::", rootTable)
				.replace("::ROOT_TABLE_ALIAS::", TYPE_ALIAS[nextAlias])
				.replace("::WHERE_STATEMENT::", where + " ::WHERE_STATEMENT:: ");

		return sql;
	}

}
