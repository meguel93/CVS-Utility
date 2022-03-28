package co.za.cvs.utility;

import co.za.cvs.utility.model.ModelDTO;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class ScriptGenerator {
    //The naming conversion I am going with is:
    //The first word will the be the query type (insert, update, delete, etc) and the last word will be the table name
    private static final String OUTPUT_FILE_NAME_FORMAT = "%s-%s.sql";
    //Copy the file into the resources folder
    private static final String FILE_LOCATION = "";
    private static final String PRODUCT = "";

    public static void main(String[] args) throws IOException {
        Sheet sheet = getSheet();
        List<ModelDTO> list = IntStream.range(1, sheet.getPhysicalNumberOfRows()).mapToObj(i -> translateRow(sheet.getRow(i))).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(list)) {
            log.warn("Not data found");
            return;
        }

        List<String> departments = list.stream().map(ModelDTO::getDepartment).distinct().collect(Collectors.toList());

        departments.forEach(department -> {
            String data = getData(department, list);
            writeDataToSQLFile(data);
        });
    }

    /**
     * Here we generate and actual database script and write the desired queries that were written in the getData method
     * @param data
     */
    private static void writeDataToSQLFile(String data) {
        if (data.length() == 0) return;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(String.format(OUTPUT_FILE_NAME_FORMAT)))) {
            writer.write(data);
        } catch (Exception e) {
            log.error(e.toString());
        }
    }


    /**
     * Read the list of the date we got from the excel/csv file and use it to generate a script of your choosing
     * This is where you create your query to do whatever you need to do with the data
     * Change this method according to your use case
     * @param department
     * @param dtoList
     * @return
     */
    private static String getData(String department, List<ModelDTO> dtoList) {
        StringBuilder builder = new StringBuilder();
        List<ModelDTO> modelDTOS = dtoList.stream()
                .filter(item -> item.getPosition().equalsIgnoreCase(""))
                .filter(item -> item.getDepartment().equalsIgnoreCase(item.getDepartment()))
                .collect(Collectors.toList());

        String queryFormat = "%s %s %s";
        modelDTOS.forEach(item -> builder.append(String.format(queryFormat, item.getDepartment(), item.getBuilding(), item.getEmployeeName())));

        return builder.toString();
    }

    /**
     * This method reads the rows you want to extract and stores them in the ModelDTO object.
     * Just remember to reference the coloumn that corresponds with the variable
     * For example if column 4 on the excel/csv file is position, getCell 4 and store the value on the position variable
     * @param row
     * @return
     */
    private static ModelDTO translateRow(Row row) {
        ModelDTO modelDTO = new ModelDTO();

        modelDTO.setBuilding(String.valueOf(row.getCell(1)));
        modelDTO.setDepartment(String.valueOf(row.getCell(2)));
        modelDTO.setEmployeeName(String.valueOf(row.getCell(3)));
        modelDTO.setPosition(String.valueOf(row.getCell(4)));

        return modelDTO;
    }

    /**
     * This method finds and converts your excel/csv file into an inputstream and return an object of type Sheet
     * Make sure the FILE_LOCATION string has the correct path to your file
     * @return
     * @throws IOException
     */
    private static Sheet getSheet() throws IOException {
        InputStream inputStream = ScriptGenerator.class.getClassLoader().getResourceAsStream(FILE_LOCATION);
        if (inputStream == null) throw new IllegalArgumentException("File not found");

        return new XSSFWorkbook(inputStream).getSheetAt(0);
    }
}
