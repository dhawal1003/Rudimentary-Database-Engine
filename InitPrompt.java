/**Created by Dhawal Parmar on 15-04-2017.*/

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class InitPrompt {

    static String prompt = "dhawalsql> ";
    static String version = "v1.0";
    static String copyright = "2017 Dhawal Deepak Parmar";
    static boolean isExit = false;
    static int pageSize = 512;
    static Scanner scanner = new Scanner(System.in).useDelimiter(";");
    static HashMap<String, Integer> dataTypeCode;
    static HashMap<Integer, Integer> dataTypeValue;
    static List<String> valuesListInsert;
    static List<String> columnsListInsert;
    static List<String> columnsListCreate;

    private static void displayScreen() {

        System.out.println(line("-", 80));
        System.out.println("Welcome to DavisBaseLite"); // Display the string.
        System.out.println("DavisBaseLite Version " + getVersion());
        System.out.println(getCopyright());
        System.out.println("\nType \"help;\" to display supported commands.");
        System.out.println(line("-", 80));
    }

    private static String line(String string, int num) {
        StringBuffer a = new StringBuffer();

        for (int i = 0; i < num; i++) {
            a.append(string);
        }
        return a.toString();
    }

    /** Help: Display supported commands */
    public static void help() {
        System.out.println(line("*", 80));
        System.out.println("SUPPORTED COMMANDS");
        System.out.println("All commands below are case insensitive");
        System.out.println();
        System.out.println("\tSHOW tables;                                                                              Display all the tables in the database.");
        System.out.println("\tSELECT * FROM table_name;                                                                 Display all records in the table.");
        System.out.println("\tSELECT * FROM table_name WHERE rowid = <value>;                                           Display records whose rowid is <id>.");
        System.out.println("\tCREATE TABLE table_name (column_name1 INT PRIMARY KEY,column_name2 datatype [NOT NULL]);  Creates the table.");
        System.out.println("\tINSERT INTO table_name (column_list) VALUES (value1,value2,value3);                       Inserts the record in the table.");
        System.out.println("\tDELETE FROM TABLE table_name WHERE row_id = key_value;                                    Deletes the record whose rowid is <key_value>.");
        System.out.println("\tUPDATE table_name SET column_name=value WHERE row_id=key_value;                           Updates the record whose rowid is <key_value>.");
        System.out.println("\tDROP TABLE table_name;                                                                    Remove table data and its schema.");
        System.out.println("\tVERSION;                                                                                  Show the program version.");
        System.out.println("\tHELP;                                                                                     Show this help information");
        System.out.println("\tEXIT;                                                                                     Exit the program");
        System.out.println();
        System.out.println();
        System.out.println(line("*", 80));
    }

    /** return the DavisBase version */
    public static String getVersion() {
        return version;
    }

    public static String getCopyright() {
        return copyright;
    }

    public static void displayVersion() {
        System.out.println("DavisBaseLite Version " + getVersion());
        System.out.println(getCopyright());
    }

    /**------------------------------------------------------------------------B Tree---------------------------------------------------------------------------------*/

    public static int[] getBtreePageLocation(RandomAccessFile tableFile, int primaryKey){

        int pageLocation = 0;
        byte pageType;
        int noOfRecords = 0;
        short lastRecLocation = 0;
        int rightLocation = 0;
        int keyValue = 0;
        boolean foundDataNode = false;
        int prevPointer = -1;

        try {
            while(!foundDataNode) {
                prevPointer = pageLocation;
                tableFile.seek(pageLocation);
                pageType = tableFile.readByte();
                if (pageType == 0x05) {
                    noOfRecords = tableFile.readByte();
                    lastRecLocation = tableFile.readShort();
                    rightLocation = tableFile.readInt();
                    // Skip 4 bytes of the root page as it contains rowid
                    if(pageLocation == 0){
                        if(primaryKey == -1){
                           primaryKey = tableFile.readInt() + 1;
                        }
                        else tableFile.skipBytes(4);
                    }
                    int i = 0;
                    for (; i < noOfRecords; i++) {
                        pageLocation = tableFile.readInt();
                        keyValue = tableFile.readInt();
                        if (keyValue >= primaryKey) {break;}
                    }
                    if(i ==  noOfRecords){ pageLocation = rightLocation; }
                } else if (pageType == 0x0D) {
                    foundDataNode = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new int[] {pageLocation,prevPointer};
    }

    /**------------------------------------------------------------------------B Tree Ends----------------------------------------------------------------------------*/


    /**----------------------------------------------------------------------Parse User Command-----------------------------------------------------------------------*/
    public static void parseUserCommand(String userCommand) {

		/*
		 * commandTokens is an array of Strings that contains one token per
		 * array element The first token can be used to determine the type of
		 * command The other tokens can be used to pass relevant parameters to
		 * each command-specific method inside each case statement
		 */
        // String[] commandTokens = userCommand.split(" ");

        ArrayList<String> commandTokens = new ArrayList<String>(Arrays.asList(userCommand.split(" ")));

		/*
		 * This switch handles a very small list of hardcoded commands of known
		 * syntax. You will want to rewrite this method to interpret more
		 * complex commands.
		 */
        switch (commandTokens.get(0)) {
            case "select":
                parseQueryString(userCommand, "terminal");
                break;
            case "drop":
                dropTable(userCommand);
                break;
            case "create":
                if (!commandTokens.get(1).equalsIgnoreCase("TABLE")) {
                    String errMessage = ErrorMessage.getErrorMessage(1004);
                    System.out.println(errMessage);
                    break;
                }
                parseCreateString(userCommand, "user");
                break;
            case "insert":
                parseInsertString(userCommand);
                break;
            case "delete":
                parseDeleteString(userCommand, "terminal");
                break;
            case "update":
                updateTable(userCommand);
                break;
            case "help":
                help();
                break;
            case "version":
                displayVersion();
                break;
            case "show":
                showTables();
                break;
            case "exit":
                isExit = true;
                break;
            case "quit":
                isExit = true;
            default:
                System.out.println("I didn't understand the command: \"" + userCommand + "\"");
                break;
        }
    }

    /**----------------------------------------------------------------------Parse User Command Ends------------------------------------------------------------------ */


    /**----------------------------------------------------------------------Show Tables-------------------------------------------------------------------------------*/
    public static void showTables(){
        String queryString = "select * from davisbase_tables";
        parseQueryString(queryString, "terminal");
    }
    /**----------------------------------------------------------------------Show Tables ends-------------------------------------------------------------------------*/



    /**---------------------------------------------------------------------Select Query-------------------------------------------------------------------------------*/
    /** Stub method for executing queries (SELECT Query) */

    public static ArrayList<String> parseQueryString(String queryString, String inputType) {

        ArrayList<String> colValues = new ArrayList<>();
        ArrayList<String> colValues1 = new ArrayList<>();
        ArrayList<String> dataSet;

        ArrayList<String> operators = new ArrayList<>(Arrays.asList("=", "<", ">", "<=", ">=", "!="));

        boolean whereClause = false;
        boolean fetchColClause = false;
        String searchColName = null;
        String operator = null;
        String searchValue = "";
        String[] fetchColNames = null;

        if (inputType.equalsIgnoreCase("terminal")) {
            System.out.println();
            System.out.println("STUB: Calling parseQueryString(String s) to process queries");
            System.out.println("Parsing the string:\"" + queryString + "\"");
            System.out.println();
        }

        ArrayList<String> selectTableTokens = new ArrayList<>(Arrays.asList(queryString.split(" ")));

        // Validate if the from clause is missing from the input query
        if (queryString.indexOf("from") == -1) {
            System.out.println(ErrorMessage.getErrorMessage(1008));
            return null;
        }

        // Check if there is a where clause in the input query
        if (queryString.indexOf("where") != -1) {
            whereClause = true;
            String[] whereValues = queryString.substring(queryString.indexOf("where") + 5).trim().replaceAll("\"","").split(" ");
            searchColName = whereValues[0];
            operator = whereValues[1];
            if (whereValues.length > 3) {
                for (int i = 2; i < whereValues.length; i++)
                    searchValue = searchValue + whereValues[i] + " ";
            } else {
                searchValue = whereValues[2];
            }
            searchValue = searchValue.trim();
            if (!operators.contains(operator)) {
                System.out.println(ErrorMessage.getErrorMessage(1012));
                return null;
            }
        }

        // Check if the query has a wildcard * or column names are specified
        if (queryString.indexOf('*') == -1) {
            fetchColClause = true;
            fetchColNames = queryString.substring(queryString.indexOf("select") + 6, queryString.indexOf("from")).trim().split(",");
        }

		/* Define table file name */
        String tableFileName = selectTableTokens.get(3) + ".tbl";

        if (tableFileName.equalsIgnoreCase("davisbase_tables.tbl") || tableFileName.equalsIgnoreCase("davisbase_columns.tbl")) {
            tableFileName = "data/catalog/" + tableFileName;
        } else {
            tableFileName = "data/user_data/" + tableFileName;
        }

        // Check if the table exists
        if(!new File(tableFileName).exists()){
            System.out.println(ErrorMessage.getErrorMessage(1016));
            return null;
        }

        try {

            // Fetch data from davisbase columns
            dataSet = fetchColumnNames(selectTableTokens.get(3));

            String colNames = dataSet.get(0);
            String colTypes = dataSet.get(1);
            String colNullable = dataSet.get(2);
            String colPrimary = dataSet.get(3);

            colValues.add(colNames.toString());

            if (whereClause) {
                if (colNames.indexOf(searchColName) != -1) {
                } else {
                    System.out.println(ErrorMessage.getErrorMessage(1022));
                    return null;
                }
            }

            // Fetch column data from the given table
            RandomAccessFile tableFile = new RandomAccessFile(tableFileName, "rw");

            int pageType;
            int noOfRecords;
            short readLocation;
            int currentPage = 0;
            int pageLocation = pageSize * currentPage;
            int currentRecord = 1;
            int index = 0;
            int cols ;
            int colLength ;
            StringBuffer data;
            boolean valueMatched = false;
            String[] columnNames = colNames.toString().split(" ");
            int noOfColumns = columnNames.length;
            int key ;
            short lastRecordLocation;
            int rightSibling;
            int prevPointer = -1;

            // Check if the where clause exists and if the search is based on primary key
            if(whereClause && searchColName.equalsIgnoreCase(columnNames[0]) && (operators.indexOf(operator) == 0))
                key = Integer.parseInt(searchValue);
            else
                key = 0;

            // Btree page location depending on the given primary key value
            int btreeinfo[] = getBtreePageLocation(tableFile,key);
            pageLocation = btreeinfo[0];
            prevPointer = btreeinfo[1];

            tableFile.seek(pageLocation);
            pageType = tableFile.read();
            noOfRecords = tableFile.read();
            lastRecordLocation = tableFile.readShort();
            rightSibling = tableFile.readInt();

            while(pageLocation != 0xFFFFFFFF){
                //System.out.println("PageL " + pageLocation + " Right " + rightSibling);
                currentRecord = 1;
                while (currentRecord <= noOfRecords) {
                    data = new StringBuffer();
                    tableFile.seek(pageLocation + 6 + 2 * currentRecord);
                    readLocation = tableFile.readShort();
                    tableFile.seek(readLocation + 2 + 4);
                    cols = tableFile.read();
                    long columnPointer = tableFile.getFilePointer();
                    tableFile.seek(columnPointer + cols);
                    long dataPointer = tableFile.getFilePointer();
                    try {
                        while (index < noOfColumns) {
                            tableFile.seek(columnPointer);
                            int colDatatype = tableFile.read();
                            // Text
                            if (colDatatype > 0x0C) {
                                colLength = colDatatype - 12;
                                columnPointer = tableFile.getFilePointer();
                                byte[] col_name = new byte[colLength];
                                tableFile.seek(dataPointer);
                                tableFile.readFully(col_name);
                                data.append(new String(col_name).replaceAll(" ", "~") + " ");
                                dataPointer = tableFile.getFilePointer();
                                if (whereClause) {
                                    if (columnNames[index].equalsIgnoreCase(searchColName)) {
                                        int op = operators.indexOf(operator);
                                        switch (op) {
                                            case 0: // Equal
                                                String s = new String(col_name).trim();
                                                if (searchValue.equalsIgnoreCase(s)) {
                                                    valueMatched = true;
                                                }
                                                break;
                                            default:
                                                System.out.println(ErrorMessage.getErrorMessage(1017));
                                                return null;
                                        }
                                    }
                                }
                            }
                            // Null Text
                            else if (colDatatype == 0x0C) {
                                columnPointer = tableFile.getFilePointer();
                                tableFile.seek(dataPointer);
                                data.append("NULL" + " ");
                                dataPointer = tableFile.getFilePointer();
                            }
                            // TinyInt
                            else if (colDatatype == 0x04) {
                                columnPointer = tableFile.getFilePointer();
                                tableFile.seek(dataPointer);
                                int val = tableFile.read();
                                data.append(val + " ");
                                dataPointer = tableFile.getFilePointer();
                                if (whereClause) {
                                    if (columnNames[index].equalsIgnoreCase(searchColName)) {
                                        int op = operators.indexOf(operator);
                                        switch (op) {
                                            case 0: // Equal
                                                if (Integer.parseInt(searchValue) == val)
                                                    valueMatched = true;
                                                break;
                                            case 1: // Less than
                                                if (val < Integer.parseInt(searchValue))
                                                    valueMatched = true;
                                                break;
                                            case 2: // Greater than
                                                if (val > Integer.parseInt(searchValue))
                                                    valueMatched = true;
                                                break;
                                            case 3: // Less than equal to
                                                if (val <= Integer.parseInt(searchValue))
                                                    valueMatched = true;
                                                break;
                                            case 4: // Greater than equal to
                                                if (val >= Integer.parseInt(searchValue))
                                                    valueMatched = true;
                                                break;
                                            case 5: // Not equal to
                                                if (val != Integer.parseInt(searchValue))
                                                    valueMatched = true;
                                                break;
                                        }

                                    }
                                }
                            }
                            // Small Int
                            else if (colDatatype == 0x05) {
                                columnPointer = tableFile.getFilePointer();
                                tableFile.seek(dataPointer);
                                short val = tableFile.readShort();
                                data.append(val + " ");
                                dataPointer = tableFile.getFilePointer();
                                if (whereClause) {
                                    if (columnNames[index].equalsIgnoreCase(searchColName)) {
                                        int op = operators.indexOf(operator);
                                        switch (op) {
                                            case 0: // Equal
                                                if (Integer.parseInt(searchValue) == val) {
                                                    valueMatched = true;
                                                }
                                                break;
                                            case 1: // Less than
                                                if (val < Integer.parseInt(searchValue)) {
                                                    valueMatched = true;
                                                }
                                                break;
                                            case 2: // Greater than
                                                if (val > Integer.parseInt(searchValue)) {
                                                    valueMatched = true;
                                                }
                                                break;
                                            case 3: // Less than equal to
                                                if (val <= Integer.parseInt(searchValue)) {
                                                    valueMatched = true;
                                                }
                                                break;
                                            case 4: // Greater than equal to
                                                if (val >= Integer.parseInt(searchValue)) {
                                                    valueMatched = true;
                                                }
                                                break;
                                            case 5: // Not equal to
                                                if (val != Integer.parseInt(searchValue)) {
                                                    valueMatched = true;
                                                }
                                                break;
                                        }
                                    }
                                }
                            }
                            // Int
                            else if (colDatatype == 0x06) {
                                columnPointer = tableFile.getFilePointer();
                                tableFile.seek(dataPointer);
                                int val = tableFile.readInt();
                                data.append(val + " ");
                                dataPointer = tableFile.getFilePointer();
                                if (whereClause) {
                                    if (columnNames[index].equalsIgnoreCase(searchColName)) {
                                        int op = operators.indexOf(operator);
                                        switch (op) {
                                            case 0: // Equal
                                                if (Integer.parseInt(searchValue) == val) {
                                                    valueMatched = true;
                                                }
                                                break;
                                            case 1: // Less than
                                                if (val < Integer.parseInt(searchValue)) {
                                                    valueMatched = true;
                                                }
                                                break;
                                            case 2: // Greater than
                                                if (val > Integer.parseInt(searchValue)) {
                                                    valueMatched = true;
                                                }
                                                break;
                                            case 3: // Less than equal to
                                                if (val <= Integer.parseInt(searchValue)) {
                                                    valueMatched = true;
                                                }
                                                break;
                                            case 4: // Greater than equal to
                                                if (val >= Integer.parseInt(searchValue)) {
                                                    valueMatched = true;
                                                }
                                                break;
                                            case 5: // Not equal to
                                                if (val != Integer.parseInt(searchValue)) {
                                                    valueMatched = true;
                                                }
                                                break;
                                        }
                                    }
                                }
                            }
                            // BigInt
                            else if (colDatatype == 0x07) {
                                columnPointer = tableFile.getFilePointer();
                                tableFile.seek(dataPointer);
                                long val = tableFile.readLong();
                                data.append(val + " ");
                                dataPointer = tableFile.getFilePointer();
                                if (whereClause) {
                                    if (columnNames[index].equalsIgnoreCase(searchColName)) {
                                        int op = operators.indexOf(operator);
                                        switch (op) {
                                            case 0: // Equal
                                                if (Long.parseLong(searchValue) == val) {
                                                    valueMatched = true;
                                                }
                                                break;
                                            case 1: // Less than
                                                if (val < Long.parseLong(searchValue)) {
                                                    valueMatched = true;
                                                }
                                                break;
                                            case 2: // Greater than
                                                if (val > Long.parseLong(searchValue)) {
                                                    valueMatched = true;
                                                }
                                                break;
                                            case 3: // Less than equal to
                                                if (val <= Long.parseLong(searchValue)) {
                                                    valueMatched = true;
                                                }
                                                break;
                                            case 4: // Greater than equal to
                                                if (val >= Long.parseLong(searchValue)) {
                                                    valueMatched = true;
                                                }
                                                break;
                                            case 5: // Not equal to
                                                if (val != Long.parseLong(searchValue)) {
                                                    valueMatched = true;
                                                }
                                                break;
                                        }
                                    }
                                }
                            }
                            // Real
                            else if (colDatatype == 0x08) {
                                columnPointer = tableFile.getFilePointer();
                                tableFile.seek(dataPointer);
                                float val = tableFile.readFloat();
                                data.append(val + " ");
                                dataPointer = tableFile.getFilePointer();
                                if (whereClause) {
                                    if (columnNames[index].equalsIgnoreCase(searchColName)) {
                                        int op = operators.indexOf(operator);
                                        switch (op) {
                                            case 0: // Equal
                                                if (Float.parseFloat(searchValue) == val) {
                                                    valueMatched = true;
                                                }
                                                break;
                                            case 1: // Less than
                                                if (val < Float.parseFloat(searchValue)) {
                                                    valueMatched = true;
                                                }
                                                break;
                                            case 2: // Greater than
                                                if (val > Float.parseFloat(searchValue)) {
                                                    valueMatched = true;
                                                }
                                                break;
                                            case 3: // Less than equal to
                                                if (val <= Float.parseFloat(searchValue)) {
                                                    valueMatched = true;
                                                }
                                                break;
                                            case 4: // Greater than equal to
                                                if (val >= Float.parseFloat(searchValue)) {
                                                    valueMatched = true;
                                                }
                                                break;
                                            case 5: // Not equal to
                                                if (val != Float.parseFloat(searchValue)) {
                                                    valueMatched = true;
                                                }
                                                break;
                                        }
                                    }
                                }
                            }
                            // Double
                            else if (colDatatype == 0x09) {
                                columnPointer = tableFile.getFilePointer();
                                tableFile.seek(dataPointer);
                                double val = tableFile.readDouble();
                                data.append(val + " ");
                                dataPointer = tableFile.getFilePointer();
                                if (whereClause) {
                                    if (columnNames[index].equalsIgnoreCase(searchColName)) {
                                        int op = operators.indexOf(operator);
                                        switch (op) {
                                            case 0: // Equal
                                                if (Double.parseDouble(searchValue) == val) {
                                                    valueMatched = true;
                                                }
                                                break;
                                            case 1: // Less than
                                                if (val < Double.parseDouble(searchValue)) {
                                                    valueMatched = true;
                                                }
                                                break;
                                            case 2: // Greater than
                                                if (val > Double.parseDouble(searchValue)) {
                                                    valueMatched = true;
                                                }
                                                break;
                                            case 3: // Less than equal to
                                                if (val <= Double.parseDouble(searchValue)) {
                                                    valueMatched = true;
                                                }
                                                break;
                                            case 4: // Greater than equal to
                                                if (val >= Double.parseDouble(searchValue)) {
                                                    valueMatched = true;
                                                }
                                                break;
                                            case 5: // Not equal to
                                                if (val != Double.parseDouble(searchValue)) {
                                                    valueMatched = true;
                                                }
                                                break;
                                        }
                                    }
                                }
                            }
                            // DateTime
                            else if (colDatatype == 0x0A) {
                                columnPointer = tableFile.getFilePointer();
                                tableFile.seek(dataPointer);
                                data.append(tableFile.readLong() + " ");
                                dataPointer = tableFile.getFilePointer();
                            }
                            // Date
                            else if (colDatatype == 0x0B) {
                                columnPointer = tableFile.getFilePointer();
                                tableFile.seek(dataPointer);
                                data.append(tableFile.readLong() + " ");
                                dataPointer = tableFile.getFilePointer();
                            }
                            // TinyInt Null
                            else if (colDatatype == 0x01) {
                                columnPointer = tableFile.getFilePointer();
                                tableFile.seek(dataPointer);
                                tableFile.skipBytes(1);
                                data.append("NULL" + " ");
                                dataPointer = tableFile.getFilePointer();
                            }
                            // Small Int Null
                            else if (colDatatype == 0x02) {
                                columnPointer = tableFile.getFilePointer();
                                tableFile.seek(dataPointer);
                                tableFile.skipBytes(2);
                                data.append("NULL" + " ");
                                dataPointer = tableFile.getFilePointer();
                            }
                            // Int Null
                            else if (colDatatype == 0x03) {
                                columnPointer = tableFile.getFilePointer();
                                tableFile.seek(dataPointer);
                                tableFile.skipBytes(4);
                                data.append("NULL" + " ");
                                dataPointer = tableFile.getFilePointer();
                            }
                            // Double Null
                            else if (colDatatype == 0x04) {
                                columnPointer = tableFile.getFilePointer();
                                tableFile.seek(dataPointer);
                                tableFile.skipBytes(8);
                                data.append("NULL" + " ");
                                dataPointer = tableFile.getFilePointer();
                            } else {
                            }
                            index++;
                        }
                    } // try
                    catch (Exception e) {
                        System.out.println(ErrorMessage.getErrorMessage(1018));
                        tableFile.close();
                        return null;
                    }
                    if (!whereClause) {
                        colValues.add(data.toString());
                    }
                    if (valueMatched) {
                        colValues.add(data.toString());
                    }
                    index = 0;
                    currentRecord++;
                    valueMatched = false;
                }

                pageLocation = rightSibling;

                if(pageLocation != 0xFFFFFFFF){
                    tableFile.seek(pageLocation);
                    pageType = tableFile.read();
                    noOfRecords = tableFile.read();
                    lastRecordLocation = tableFile.readShort();
                    rightSibling = tableFile.readInt();
                }
            }
            tableFile.close();
            String headRow = colValues.get(0);
            String[] headCol = headRow.split(" ");
            StringBuffer data1 = null;

            if (fetchColClause) {
                for (int i = 0; i < colValues.size(); i++) {
                    data1 = new StringBuffer();
                    String[] str = colValues.get(i).split(" ");
                    for (int j = 0; j < fetchColNames.length; j++) {
                        for (int k = 0; k < str.length; k++) {
                            if (headCol[k].equalsIgnoreCase(fetchColNames[j])) {
                                data1.append(str[k] + " ");
                            }
                        }
                    }
                    colValues1.add(data1.toString());
                }
            }
            if (fetchColClause) {
                colValues = colValues1;
            }
            if (inputType.equalsIgnoreCase("terminal") && colValues.size() > 1) {
                String noOfLines = line("-", 22 * noOfColumns);
                String head = colValues.get(0);
                String[] str1 = head.split(" ");
                System.out.println(noOfLines);
                for (String a : str1)
                    System.out.format("%-22s", a);
                System.out.println();
                System.out.println(noOfLines);
                for (int i = 1; i < colValues.size(); i++) {
                    String[] str = colValues.get(i).split(" ");
                    for (String a : str)
                        System.out.format("%-22s", a.replaceAll("~", " "));
                    System.out.println();
                }
                System.out.println(noOfLines);
                System.out.println(" " + (colValues.size()-1) + " records displayed.");
                System.out.println();
                return null;
            } else if(inputType.equalsIgnoreCase("terminal") && colValues.size() == 1) {
                System.out.println("No results found.");
            } else	{
                return colValues;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**---------------------------------------------------------------------Select Query Ends--------------------------------------------------------------------------*/



    /**---------------------------------------------------------------------Create Query-------------------------------------------------------------------------------*/
    /** Stub method for creating new tables */

    public static void parseCreateString(String createTableString, String tableType) {

        if(!tableType.equalsIgnoreCase("catalog")){
            System.out.println();
            System.out.println("STUB: Calling your method to create a table");
            System.out.println("Parsing the string:\"" + createTableString + "\"");
            System.out.println();
        }

        ArrayList<String> createTableTokens = new ArrayList<String>(Arrays.asList(createTableString.split(" ")));

		/* Define table file name */
        String tableFileName = createTableTokens.get(2) + ".tbl";

        // Check if the table name consists of parenthesis
        if (tableFileName.contains("(")) {
            System.out.println(ErrorMessage.getErrorMessage(1009));
            return;
        }

        // Check if the table already exists
        if (!tableType.equalsIgnoreCase("catalog")) {
            if ((new File("data/user_data/" + tableFileName)).exists()) {
                System.out.println(ErrorMessage.getErrorMessage(1015));
                return;
            }
        }
        String columns = createTableString.substring(createTableString.indexOf('(') + 1, createTableString.indexOf(')'));

        columnsListCreate = new ArrayList<>(Arrays.asList(columns.split(",")));

        // Check and validate the primary key and not null values
        for (int i = 0; i < columnsListCreate.size(); i++) {

            String[] col = columnsListCreate.get(i).trim().split(" ");
            String column_name = col[0];
            String data_type = col[1];

            if (i == 0) {
                if (!data_type.equalsIgnoreCase("int")) {
                    System.out.println(ErrorMessage.getErrorMessage(1013));
                    return;
                }
                if (col.length < 3 || (col.length >= 3 && col.length < 4)) {
                    System.out.println(ErrorMessage.getErrorMessage(1014));
                    return;
                }
                if (!(col[2].equals("primary") && col[3].equals("key"))) {
                    System.out.println(ErrorMessage.getErrorMessage(1014));
                    return;
                }
            }
            if (!dataTypeCode.containsKey(data_type)) {
                System.out.println(ErrorMessage.getErrorMessage(1007));
                return;
            }
        }

		/* Code to create a .tbl file to contain table data */
        try
        {
            if (tableType.equalsIgnoreCase("catalog"))
                tableFileName = "data/catalog/" + tableFileName;
            else
                tableFileName = "data/user_data/" + tableFileName;

            RandomAccessFile tableFile = new RandomAccessFile(tableFileName, "rw");

			/* Initialize the file size to be zero bytes */
            tableFile.setLength(0);
			/* Display information about the zero-size file */
            //	System.out.println("The file is now " + tableFile.length() + " bytes long");
            //	System.out.println("The file is now " + tableFile.length() / pageSize + " pages long\n");

			/* Increase the file size to be 512B, i.e. One page, 1 x 512B */
            tableFile.setLength(pageSize);

            //	System.out.println("The file is now " + tableFile.length() + " bytes long");
            //	System.out.println("The file is now " + tableFile.length() / pageSize + " pages long\n");

            tableFile.seek(0);
            tableFile.writeByte(13);
            tableFile.writeByte(0);
            tableFile.writeShort(pageSize);
            tableFile.writeByte(255);
            tableFile.writeByte(255);
            tableFile.writeByte(255);
            tableFile.writeByte(255);
            tableFile.close();

            if (!tableType.equalsIgnoreCase("catalog")) {
                addTableToMetadata(createTableTokens.get(2));
                addColumnsToMetadata(createTableTokens.get(2), columnsListCreate);
            }

            if (!tableType.equalsIgnoreCase("catalog"))
                System.out.println("The table " + createTableTokens.get(2) + " is created successfully.");

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**---------------------------------------------------------------------Create Query Ends--------------------------------------------------------------------------*/

    /**---------------------------------------------------------------------Insert Syntax Check------------------------------------------------------------------------*/
    public static void checkInsertSyntax(String insertTableString) {


        // Tokenize the input query
        ArrayList<String> insertTableTokens = new ArrayList<>(Arrays.asList(insertTableString.split(" ")));
		/* Define table file name */
        String tableFileName = insertTableTokens.get(2) + ".tbl";

        // No column list mentioned
        if(insertTableTokens.get(3).equalsIgnoreCase("values")){
            ArrayList<String> dataSet = fetchColumnNames(insertTableTokens.get(2));
            columnsListInsert = new ArrayList<>(Arrays.asList(dataSet.get(0).split(" ")));
        } else {
            insertTableString = insertTableString.replaceAll("\\s{2,}", " ");
            String columns = insertTableString.substring(insertTableString.indexOf('(') + 1, insertTableString.indexOf(')'));
            columnsListInsert = new ArrayList<>(Arrays.asList(columns.replaceAll(" ","").split(",")));
        }


        insertTableString = insertTableString.replaceAll("\\s{2,}", " ");
        String values = insertTableString.substring(insertTableString.lastIndexOf("values (") + 8, insertTableString.lastIndexOf(')'));

        StringBuffer sb = new StringBuffer(values);
        valuesListInsert = new ArrayList<>();
        boolean insideDoubleQuotes = false;
        boolean add = false;
        StringBuffer field = new StringBuffer();

        for (int i = 0; i < sb.length(); i++) {
            if ((sb.charAt(i) == '"' || sb.charAt(i) == '\'') && !insideDoubleQuotes) {
                insideDoubleQuotes = true;
            } else if ((sb.charAt(i) == '"' || sb.charAt(i) == '\'') && insideDoubleQuotes) {
                insideDoubleQuotes = false;
                valuesListInsert.add(field.toString());
                field.setLength(0);
                add = true;
            } else if ((sb.charAt(i) == ',' && !insideDoubleQuotes) || (i == sb.length() - 1)) {
                // ignore the comma after double quotes.
                if (!add) {
                    if (i == sb.length() - 1) {
                        field.append(sb.charAt(i));
                    }
                    valuesListInsert.add(field.toString().trim());
                }
                add = false;
                // clear the field for next word
                field.setLength(0);
            } else if (sb.charAt(i) == 40 && insideDoubleQuotes) {
                field.append(sb.charAt(i));
            } else if (sb.charAt(i) == 41 && insideDoubleQuotes) {
                field.append(sb.charAt(i));
            } else if (sb.charAt(i) == 40) {

            } else if (sb.charAt(i) == ' ' && !insideDoubleQuotes){

            }
            else {
                field.append(sb.charAt(i));
            }
        }
        //System.out.println(columnsListInsert.toString());

        //System.out.println(valuesListInsert.toString());
    }

    /**---------------------------------------------------------------------Ends---------------------------------------------------------------------------------------*/

    /**---------------------------------------------------------------------Insert Query-------------------------------------------------------------------------------*/
    /** Stub method for inserting new rows into tables */

    public static void parseInsertString(String insertTableString) {

        // Storing column set from davisbase columns
        ArrayList<String> dataSet;

        System.out.println();
        System.out.println("STUB: Calling your method to insert a row into a table");
        System.out.println("Parsing the string:\"" + insertTableString + "\"");
        System.out.println();

        // Tokenize the input query
        ArrayList<String> insertTableTokens = new ArrayList<>(Arrays.asList(insertTableString.split(" ")));

		/* Define table file name */
        String tableFileName = insertTableTokens.get(2) + ".tbl";

        // Check if the table does not exist
        if (!(new File("data/user_data/" + tableFileName).exists())) {
            System.out.println(ErrorMessage.getErrorMessage(1016));
            return;
        }

        // Check if the insert query has the keyword "into"
        if (!insertTableTokens.get(1).equalsIgnoreCase("into")) {
            System.out.println(ErrorMessage.getErrorMessage(1005));
            return;
        }

        // Check the insert syntax
        checkInsertSyntax(insertTableString);

        // Check if the column list and values list are matching
        if (valuesListInsert.size() != columnsListInsert.size()) {
            System.out.println(ErrorMessage.getErrorMessage(1006));
            return;
        }

        try {
            // Fetch data from davisbase columns
            dataSet = fetchColumnNames(insertTableTokens.get(2));
            String colNames = dataSet.get(0);
            String colTypes = dataSet.get(1);
            String colNullable = dataSet.get(2);
            String colPrimary = dataSet.get(3);
            String colOrdinalPosition = dataSet.get(4);

            String[] names = colNames.toString().split(" ");
            String[] types = colTypes.toString().split(" ");
            String[] nullable = colNullable.toString().split(" ");
            String[] ordinal = colOrdinalPosition.toString().split(" ");
            String[] primary = colPrimary.toString().split(" ");

            // Check if the given column names and the davisbase_columns column names are matching
            int matchedColumns = 0;
            int index = 0;
            int noOfColumns = names.length;
            int[] header = new int[noOfColumns];
            int recLength = 0;
            ArrayList<String> finalValues = new ArrayList<>();
            boolean primaryValueExists = false;

            for (int i = 0; i < names.length; i++) {
                if (columnsListInsert.contains(names[i])) {
                    index = columnsListInsert.indexOf(names[i]);
                    finalValues.add(valuesListInsert.get(index));
                    matchedColumns++;
                    if (types[i].equals("text")) {
                        header[i] = valuesListInsert.get(index).length() + 12;
                        recLength += valuesListInsert.get(index).length();
                    } else {
                        header[i] = dataTypeCode.get(types[i]);
                        recLength += dataTypeValue.get(dataTypeCode.get(types[i]));
                    }
                } else {
                    // Generate error as the column is not null
                    if (nullable[i].equalsIgnoreCase("no")) {
                        System.out.println(ErrorMessage.getErrorMessage(1010));
                        return;
                    } else {
                        finalValues.add("");
                        if (types[i].equals("text")) {
                            header[i] = 12;
                        } else if (types[i].equals("int") || types[i].equals("real")) {
                            header[i] = dataTypeCode.get("intnull");
                            recLength += dataTypeValue.get(header[i]);
                        } else if (types[i].equals("smallint")) {
                            header[i] = dataTypeCode.get("smallnull");
                            recLength += dataTypeValue.get(header[i]);
                        } else if (types[i].equals("tinyint")) {
                            header[i] = dataTypeCode.get("tinynull");
                            recLength += dataTypeValue.get(header[i]);
                        } else if (types[i].equals("double") || types[i].equals("datetime")
                                || types[i].equals("date")) {
                            header[i] = dataTypeCode.get("doublenull");
                            recLength += dataTypeValue.get(header[i]);
                        }
                    }
                }
            }

            // Payload length
            int recordSize = recLength + 1 + noOfColumns;

            if (columnsListInsert.size() != matchedColumns) {
                System.out.println(ErrorMessage.getErrorMessage(1011));
                return;
            }

            // Checking for duplicate primary key
            String queryString = "select " + names[0] + " from " + insertTableTokens.get(2);
            ArrayList<String> primaryKeys = parseQueryString(queryString, "inside");
            String[] priKeys = primaryKeys.toString().substring(primaryKeys.toString().indexOf('[')+1,primaryKeys.toString().indexOf(']')).split(",");

            for (int i = 1; i < priKeys.length; i++) {
                if(Integer.parseInt(priKeys[i].trim()) == Integer.parseInt(finalValues.get(0))){
                    primaryValueExists = true;
                }
            }

            if(primaryValueExists){
                System.out.println(ErrorMessage.getErrorMessage(1028));
                return;
            }

            // Check at which position to insert the new value
            RandomAccessFile tableFile = new RandomAccessFile("data/user_data/"+tableFileName, "rw");

            int noOfRecords;
            short writeLocation;
            int pageType;
            short readLocation;
            int pageLocation = 0;
            int currentRecord = 1;
            index = 0;
            int cols = 0;
            ArrayList<Short> data = new ArrayList<>();
            boolean found = false;
            int insertIndex = 0;
            int rowid = 0;
            long indexPointer = 0;
            int prevPointer;
            int rightPointer;

            tableFile.seek(0);
            pageType = tableFile.readByte();
            // Row id
            if(pageType != 0x0D) {
                tableFile.seek(8);
                rowid = tableFile.readInt();
            }

            // Btree page location depending on the given primary key value
            int btreeinfo[] = getBtreePageLocation(tableFile,Integer.parseInt(finalValues.get(0)));
            pageLocation = btreeinfo[0];
            prevPointer = btreeinfo[1];


            // Check if the data page is full
            tableFile.seek(pageLocation);
            pageType = tableFile.readByte();
            noOfRecords = tableFile.readByte();
            writeLocation = tableFile.readShort();
            rightPointer = tableFile.readInt();
            ArrayList<Short> headPointers = new ArrayList<>();
            int[] pKeys = new int[noOfRecords];

            for(currentRecord = 0; currentRecord < noOfRecords ; currentRecord++){
                tableFile.seek(pageLocation + 8 + 2*currentRecord);
                headPointers.add(tableFile.readShort());
                indexPointer = tableFile.getFilePointer();
                tableFile.seek(headPointers.get(currentRecord) + 2 + 4 + 1 + noOfColumns);
                pKeys[currentRecord] = tableFile.readInt();
            }

            if (rowid != 0)
                rowid++;
            else if (noOfRecords == 0)
                rowid = 1;
            else {
                tableFile.seek(writeLocation + 2);
                rowid = tableFile.readInt();
                rowid++;
            }
            noOfRecords++;

            boolean rootSplit = false;
            LinkedHashMap<Integer,Integer> rootPrimaryIndexes = new LinkedHashMap<>();

            // Split the page as its full
            if((writeLocation - recordSize - 6 ) < (indexPointer + 0x10)){
                //Root split
                int size = (int) new File("data/user_data/"+tableFileName).length(); //512
                if(pageLocation == 0){
                  //  System.out.println("Root page splits");
                    rootSplit = true;
                    byte[] wholePageData = new byte[size];
                    tableFile.seek(0);
                    tableFile.readFully(wholePageData);
                    byte[] newData = new byte[wholePageData.length + pageSize];
                    System.arraycopy(wholePageData,0,newData, pageSize, wholePageData.length);
                    tableFile.setLength(newData.length);
                    tableFile.seek(0);
                    tableFile.write(newData);
                    size = (int) new File("data/user_data/"+tableFileName).length(); //1024
                    pageLocation = pageSize;

                    tableFile.seek(pageLocation + 1);
                    int num_rec = tableFile.readByte();
                    int lastWriteLoc = tableFile.readShort() + pageSize;
                    tableFile.seek(pageLocation + 2);
                    tableFile.writeShort(lastWriteLoc);
                    tableFile.skipBytes(4);
                    ArrayList<Short> temp = new ArrayList<>();
                    for(int i = 0 ; i < num_rec ; i++)
                        temp.add((short)(headPointers.get(i) + 512));
                    headPointers = temp;
                    for (int i = 0; i < num_rec; i++)
                        tableFile.writeShort(headPointers.get(i));

                    //write root page contents
                    tableFile.seek(0);
                    tableFile.writeByte(0x05);
                    tableFile.writeByte(1);
                    tableFile.skipBytes(2);
                    tableFile.writeInt(size);
                    tableFile.writeInt(rowid);
                    tableFile.writeInt(pageSize);
                    tableFile.writeInt(pKeys[pKeys.length/2-1]);
                    long pointer = tableFile.getFilePointer();
                    tableFile.seek(2);
                    tableFile.writeShort((short)pointer);
                    rootPrimaryIndexes.put(pageSize,pKeys[pKeys.length/2-1]);

                }

                tableFile.seek(1);
                int rec = tableFile.readByte();
                tableFile.skipBytes(10);
                for(int k = 0 ; k < rec ; k++){
                    int loc = tableFile.readInt();
                    int p = tableFile.readInt();
                    rootPrimaryIndexes.put(loc,p);
                }

                // Normal Data Page Split
                tableFile.setLength(size + pageSize);
                size = size + pageSize; //1536

                int rightpage = size - pageSize;

                boolean rightPageSplits = false;
                // IF the right page splits
                if(rightPointer == 0xFFFFFFFF && !rootSplit){
                    tableFile.seek(0 + 4);
                    tableFile.writeInt(size - pageSize);
                    int newPkey = pKeys[pKeys.length/2-1];
                    rootPrimaryIndexes.put(pageLocation,newPkey);
                    tableFile.seek(0 + 12);

                    for(Map.Entry<Integer,Integer> entry : rootPrimaryIndexes.entrySet()){
                        tableFile.writeInt(entry.getKey());
                        tableFile.writeInt(entry.getValue());
                    }

                    tableFile.seek(1);
                    int n_rec = tableFile.readByte();
                    tableFile.seek(1);
                    tableFile.writeByte(++n_rec);
                    rightPageSplits = true;
                }

                // Left Page
                tableFile.seek(pageLocation+4);
                int oldRightPointer = tableFile.readInt();
                //tableFile.skipBytes(4);
                ArrayList<byte[]> left = new ArrayList<>();
                ArrayList<byte[]> right = new ArrayList<>();
                ArrayList<Integer> leftKeys = new ArrayList<>();
                ArrayList<Integer> rightKeys = new ArrayList<>();
                for(int i = 0; i < pKeys.length ; i++){
                    short readLoc = tableFile.readShort();
                    long nxtPointer = tableFile.getFilePointer();
                    tableFile.seek(readLoc);
                    int payload = tableFile.readShort();
                    byte[] b = new byte[payload + 6];
                    tableFile.seek(readLoc);
                    tableFile.readFully(b);
                    if( i <= (pKeys.length/2 - 1)) {
                        left.add(b);
                        leftKeys.add(pKeys[i]);
                    }
                    else {
                        right.add(b);
                        rightKeys.add(pKeys[i]);
                    }
                    tableFile.seek(nxtPointer);
                }

                //System.out.println(leftKeys.toString());
                //System.out.println(rightKeys.toString());
                //System.out.println("Left page " + pageLocation);

                short lastRecLocation = (short) (pageLocation + pageSize);
                // clear the left page
                tableFile.seek(pageLocation+8);
                byte[] cl = new byte[pageSize - 8];
                tableFile.write(cl);
                for (int i = 0; i < left.size(); i++) {
                    tableFile.seek(lastRecLocation - left.get(i).length);
                    lastRecLocation = (short)tableFile.getFilePointer();
                    tableFile.write(left.get(i));
                    tableFile.seek(pageLocation + 2);
                    tableFile.writeShort(lastRecLocation);
                    tableFile.seek(pageLocation + 8  + 2 * i);
                    tableFile.writeShort(lastRecLocation);
                }
                tableFile.seek(pageLocation + 1);
                tableFile.writeByte(leftKeys.size());
                tableFile.writeShort(lastRecLocation);
                tableFile.writeInt(rightpage);

                //Right page
                tableFile.seek(rightpage);
                tableFile.writeByte(0x0D);
                tableFile.writeByte(rightKeys.size());
                tableFile.writeShort(size);
                tableFile.writeInt(oldRightPointer);
                lastRecLocation = (short)size;
                for (int i = 0; i < right.size(); i++) {
                    tableFile.seek(lastRecLocation - right.get(i).length);
                    lastRecLocation = (short)tableFile.getFilePointer();
                    tableFile.write(right.get(i));
                    tableFile.seek(rightpage + 2);
                    tableFile.writeShort(lastRecLocation);
                    tableFile.seek(rightpage + 8  + 2 * i);
                    tableFile.writeShort(lastRecLocation);
                }

                // Add the primary key to the root page if left page splits
                if(!rootSplit && !rightPageSplits){
                    int newPkey = pKeys[pKeys.length/2-1];
                    int oldPkey = rootPrimaryIndexes.get(pageLocation);
                    rootPrimaryIndexes.put(pageLocation,newPkey);
                    rootPrimaryIndexes.put(rightpage,oldPkey);
                    tableFile.seek(0 + 12);
                    for(Map.Entry<Integer,Integer> entry : rootPrimaryIndexes.entrySet()){
                        tableFile.writeInt(entry.getKey());
                        tableFile.writeInt(entry.getValue());
                    }
                    tableFile.seek(1);
                    int n_rec = tableFile.readByte();
                    tableFile.seek(1);
                    tableFile.writeByte(++n_rec);
                }

                if(Integer.parseInt(finalValues.get(0)) <= leftKeys.get(leftKeys.size()-1))
                    pageLocation = pageLocation;
                else
                    pageLocation = rightpage;
            }


            // Insert data and sort the pointers on the new page if splitted
            currentRecord = 1;
            tableFile.seek(pageLocation);
            pageType = tableFile.read();
            noOfRecords = tableFile.read();
            while (currentRecord <= noOfRecords) {
                tableFile.seek(pageLocation +  6 + 2 * currentRecord);
                readLocation = tableFile.readShort();
                data.add(readLocation);
                tableFile.seek(readLocation + 2 + 4);
                cols = tableFile.read();
                long columnPointer = tableFile.getFilePointer();
                tableFile.seek(columnPointer + cols);
                int val = tableFile.readInt();
                if((val > Integer.parseInt(finalValues.get(0))) && !found){
                    insertIndex = currentRecord;
                    found = true;
                }
                currentRecord++;
            }
            // Write the data in the table file
            recordSize = 0;
            writeLocation = 0;
            int currentCol = 0;
            recordSize = recLength + 1 + noOfColumns;
            tableFile.seek(pageLocation);
            pageType = tableFile.read();
            noOfRecords = tableFile.read();
            writeLocation = tableFile.readShort();
            noOfRecords++;
            writeLocation = (short) (writeLocation - recordSize - 4 - 2);
            if(insertIndex == 0)
                data.add(writeLocation);
            else
                data.add(insertIndex-1, writeLocation);
            tableFile.seek(pageLocation);
            tableFile.writeByte(pageType);
            tableFile.writeByte(noOfRecords);
            tableFile.writeShort(writeLocation);
            tableFile.skipBytes(4);
            for (int i = 0; i < data.size(); i++)
                tableFile.writeShort(data.get(i));
            tableFile.seek(writeLocation);
            tableFile.writeShort(recordSize);
            tableFile.writeInt(rowid);
            tableFile.writeByte(noOfColumns);

            long columnPointer = tableFile.getFilePointer();
            tableFile.seek(columnPointer + noOfColumns);
            long dataPointer = tableFile.getFilePointer();

            while (currentCol < noOfColumns) {
                tableFile.seek(columnPointer);
                tableFile.writeByte(header[currentCol]);
                int colDatatype = header[currentCol];
                // Text
                if (colDatatype > 0x0C) {
                    columnPointer = tableFile.getFilePointer();
                    tableFile.seek(dataPointer);
                    tableFile.writeBytes(finalValues.get(currentCol));
                    dataPointer = tableFile.getFilePointer();
                    // System.out.println(finalValues.get(currentCol));
                }
                // Null Text
                else if (colDatatype == 0x0C) {
                    columnPointer = tableFile.getFilePointer();
                    tableFile.seek(dataPointer);
                    dataPointer = tableFile.getFilePointer();
                }
                // TinyInt
                else if (colDatatype == 0x04) {
                    columnPointer = tableFile.getFilePointer();
                    tableFile.seek(dataPointer);
                    tableFile.writeByte(Integer.parseInt(finalValues.get(currentCol)));
                    dataPointer = tableFile.getFilePointer();
                }
                // Small Int
                else if (colDatatype == 0x05) {
                    columnPointer = tableFile.getFilePointer();
                    tableFile.seek(dataPointer);
                    tableFile.writeShort(Integer.parseInt(finalValues.get(currentCol)));
                    dataPointer = tableFile.getFilePointer();
                }
                // Int
                else if (colDatatype == 0x06) {
                    columnPointer = tableFile.getFilePointer();
                    tableFile.seek(dataPointer);
                    tableFile.writeInt(Integer.parseInt(finalValues.get(currentCol)));
                    dataPointer = tableFile.getFilePointer();
                    // System.out.println(finalValues.get(currentCol));
                }
                // BigInt
                else if (colDatatype == 0x07) {
                    columnPointer = tableFile.getFilePointer();
                    tableFile.seek(dataPointer);
                    tableFile.writeDouble(Long.parseLong(finalValues.get(currentCol)));
                    dataPointer = tableFile.getFilePointer();
                }
                // Real
                else if (colDatatype == 0x08) {
                    columnPointer = tableFile.getFilePointer();
                    tableFile.seek(dataPointer);
                    tableFile.writeFloat(Float.parseFloat(finalValues.get(currentCol)));
                    dataPointer = tableFile.getFilePointer();
                }
                // Double
                else if (colDatatype == 0x09) {
                    columnPointer = tableFile.getFilePointer();
                    tableFile.seek(dataPointer);
                    tableFile.writeDouble(Double.parseDouble(finalValues.get(currentCol)));
                    dataPointer = tableFile.getFilePointer();
                }
                // DateTime
                else if (colDatatype == 0x0A) {
                    columnPointer = tableFile.getFilePointer();
                    tableFile.seek(dataPointer);
                    tableFile.writeLong(Long.parseLong(finalValues.get(currentCol)));
                    dataPointer = tableFile.getFilePointer();
                }
                // Date
                else if (colDatatype == 0x0B) {
                    columnPointer = tableFile.getFilePointer();
                    tableFile.seek(dataPointer);
                    tableFile.writeLong(Long.parseLong(finalValues.get(currentCol)));
                    dataPointer = tableFile.getFilePointer();
                }
                // TinyInt Null
                else if (colDatatype == 0x01) {
                    columnPointer = tableFile.getFilePointer();
                    tableFile.seek(dataPointer);
                    tableFile.skipBytes(1);
                    dataPointer = tableFile.getFilePointer();
                }
                // Small Int Null
                else if (colDatatype == 0x02) {
                    columnPointer = tableFile.getFilePointer();
                    tableFile.seek(dataPointer);
                    tableFile.skipBytes(2);
                    dataPointer = tableFile.getFilePointer();
                }
                // Int Null
                else if (colDatatype == 0x03) {
                    columnPointer = tableFile.getFilePointer();
                    tableFile.seek(dataPointer);
                    tableFile.skipBytes(4);
                    dataPointer = tableFile.getFilePointer();
                }
                // Double Null
                else if (colDatatype == 0x04) {
                    columnPointer = tableFile.getFilePointer();
                    tableFile.seek(dataPointer);
                    tableFile.skipBytes(8);
                    dataPointer = tableFile.getFilePointer();
                } else {
                }
                currentCol++;
            }

            tableFile.seek(0);
            pageType = tableFile.readByte();
            if(pageType == 0x05) {
                tableFile.seek(8);
                tableFile.writeInt(rowid);
            }
            tableFile.close();

            System.out.println("1 record inserted successfully.");

        } catch ( Exception e) {
            e.printStackTrace();
        }
    }

    /**---------------------------------------------------------------------Insert Query Ends---------------------------------------------------------------------------*/



    /**---------------------------------------------------------------------Delete Query-------------------------------------------------------------------------------*/
    /** Stub method for deleting rows from the tables */
    public static void parseDeleteString(String deleteString, String inputType) {

        ArrayList<String> dataSet ;
        String searchColName ;
        String operator = null;
        String searchValue ;

        if(inputType.equalsIgnoreCase("terminal")){
            System.out.println();
            System.out.println("STUB: Calling your method to delete a row from a table");
            System.out.println("Parsing the string:\"" + deleteString + "\"");
            System.out.println();
        }

        ArrayList<String> deleteTableTokens = new ArrayList<>(Arrays.asList(deleteString.split(" ")));

		/* Define table file name */
        String tableFileName = deleteTableTokens.get(3) + ".tbl";

        if(inputType.equalsIgnoreCase("inside"))
            tableFileName = "data/catalog/" + tableFileName;
        else
            tableFileName = "data/user_data/" + tableFileName;


        // Check if the delete query has the keyword "from"
        if (!deleteTableTokens.get(1).equalsIgnoreCase("from")) {
            System.out.println(ErrorMessage.getErrorMessage(1019));
            return;
        }

        // Check if the delete query has the keyword "table"
        if (!deleteTableTokens.get(2).equalsIgnoreCase("table")) {
            System.out.println(ErrorMessage.getErrorMessage(1020));
            return;
        }

        // Check if the delete query has the keyword "where"
        if (deleteString.indexOf("where") == -1) {
            System.out.println(ErrorMessage.getErrorMessage(1021));
            return;
        }

        // Check if the table exists
        if (!(new File(tableFileName).exists())) {
            System.out.println(ErrorMessage.getErrorMessage(1016));
            return;
        }

        String[] val1 = deleteString.substring(deleteString.indexOf("where")+5).trim().replaceAll(" ","").split("=");
        searchColName = val1[0];
        searchValue = val1[1];
        operator = "=";

       /* searchColName = deleteTableTokens.get(5);
        operator = deleteTableTokens.get(6);
        searchValue = deleteTableTokens.get(7);
*/
        try {
            Integer.parseInt(searchValue);
        } catch (Exception e) {
            System.out.println(ErrorMessage.getErrorMessage(1024));
            return;
        }

        try {

            // Fetch data from davisbase columns
            dataSet = fetchColumnNames(deleteTableTokens.get(3));

            String colNames = dataSet.get(0);
            String colTypes = dataSet.get(1);
            String colNullable = dataSet.get(2);
            String colPrimary = dataSet.get(3);
            String colOrdinalPosition = dataSet.get(4);

            String[] names = colNames.toString().trim().split(" ");
            String[] types = colTypes.toString().split(" ");
            String[] nullable = colNullable.toString().split(" ");
            String[] ordinal = colOrdinalPosition.toString().split(" ");
            String[] primary = colPrimary.toString().split(" ");

            // Check if the given column name exists in the davisbase_columns

            boolean columnExistsPri = false;
            boolean columnExists = false;

            for (int i = 0; i < names.length; i++) {
                if (searchColName.equalsIgnoreCase(names[i]) && i == 0)
                    columnExistsPri = true;
                if (searchColName.equalsIgnoreCase(names[i]))
                    columnExists = true;
            }

            if (!columnExistsPri && columnExists) {
                System.out.println(ErrorMessage.getErrorMessage(1023));
                return;
            } else if (!columnExists) {
                System.out.println(ErrorMessage.getErrorMessage(1022));
                return;
            } else {
                // System.out.println("Success");
            }

            // Fetch column data from the given table and check which data needs to be deleted
            RandomAccessFile tableFile = new RandomAccessFile(tableFileName, "rw");

            int noOfRecords;
            int pageType;
            short readLocation;
            int currentPage = 0;
            int pageLocation = pageSize * currentPage;
            int currentRecord = 1;
            int noOfColumns = names.length;
            int index = 0;
            int cols = 0;
            ArrayList<Short> data = new ArrayList<>();
            int valueMatched = 0;
            boolean valueExists = false;
            short deleteLocation = 0;

            // Btree page location depending on the given primary key value
            int btreeinfo[] = getBtreePageLocation(tableFile,Integer.parseInt(searchValue));
            pageLocation = btreeinfo[0];
            int prevPointer = btreeinfo[1];

            tableFile.seek(pageLocation);
            pageType = tableFile.read();
            noOfRecords = tableFile.read();
            while (currentRecord <= noOfRecords) {
                tableFile.seek(pageLocation + 6 + 2 * currentRecord);
                readLocation = tableFile.readShort();
                data.add(readLocation);
                tableFile.seek(readLocation + 2 + 4);
                cols = tableFile.read();
                long columnPointer = tableFile.getFilePointer();
                tableFile.seek(columnPointer + cols);
                int val = tableFile.readInt();
                if (val == Integer.parseInt(searchValue)) {
                    valueMatched = currentRecord;
                    deleteLocation = readLocation;
                    valueExists = true;
                }
                currentRecord++;
            }

            if (!valueExists) {
                System.out.println(ErrorMessage.getErrorMessage(1025));
            } else {
                data.remove(valueMatched - 1);
                noOfRecords--;
                tableFile.seek(pageLocation);
                tableFile.writeByte(pageType);
                tableFile.writeByte(noOfRecords);
                tableFile.skipBytes(2+4);
                for (int i = 0; i < data.size(); i++)
                    tableFile.writeShort(data.get(i));
                tableFile.writeShort(0);
                tableFile.close();
                if(inputType.equalsIgnoreCase("terminal"))
                    System.out.println("Record with " + searchColName + " = " + searchValue + " is deleted successfully.");
            }

        } catch (Exception e) {
            // TODO: handle exception
            System.out.println(e);
        }

    }

    /**---------------------------------------------------------------------Delete Query Ends---------------------------------------------------------------------------*/


    /**---------------------------------------------------------------------Drop Query----------------------------------------------------------------------------------*/
    /** Stub method for dropping tables */

    public static void dropTable(String dropString) {

        System.out.println();
        System.out.println("STUB: Calling your method to drop the table");
        System.out.println("Parsing the string:\"" + dropString + "\"");
        System.out.println();

        ArrayList<String> rowId = new ArrayList<>();
        ArrayList<String> dropTableTokens = new ArrayList<>(Arrays.asList(dropString.split(" ")));

		/* Define table file name */
        String tableFileName = dropTableTokens.get(2) + ".tbl";

        // Check if the delete query has the keyword "table"
        if (!dropTableTokens.get(1).equalsIgnoreCase("table")) {
            System.out.println(ErrorMessage.getErrorMessage(1020));
            return;
        }

        // Check if the table exists
        if (!(new File("data/user_data/" + tableFileName).exists())) {
            System.out.println(ErrorMessage.getErrorMessage(1016));
            return;
        }

        // Delete table information from davisbase columns

        String queryString = "select rowid from davisbase_columns where table_name = " + dropTableTokens.get(2);
        rowId = parseQueryString(queryString, "inside");

        for (int i = 1; i < rowId.size(); i++) {
            queryString = "delete from table davisbase_columns where rowid = " + rowId.get(i);
            parseDeleteString(queryString, "inside");
        }

        // Delete table information from davisbase tables

        queryString = "select rowid from davisbase_tables where table_name = " + dropTableTokens.get(2);
        rowId = parseQueryString(queryString, "inside");

        for (int i = 1; i < rowId.size(); i++) {
            queryString = "delete from table davisbase_tables where rowid = " + rowId.get(i);
            parseDeleteString(queryString, "inside");
        }

        // Delete the file from the user directory
        try{
            Files.delete(Paths.get("data/user_data/" + tableFileName));
        }catch(IOException e){
            e.printStackTrace();
        }
        System.out.println( "The table " + dropTableTokens.get(2) + " is dropped successfully.");

    }

    /**---------------------------------------------------------------------Drop Query Ends-----------------------------------------------------------------------------*/


    /**---------------------------------------------------------------------Update Query--------------------------------------------------------------------------------*/
    /** Stub method for updating rows in the tables */

    public static void updateTable(String updateString){

        ArrayList<String> dataSet;
        String searchColName;
        String operator;
        String searchValue;
        String updateColName;
        String updateValue;

        System.out.println();
        System.out.println("STUB: Calling your method to update a row in a table");
        System.out.println("Parsing the string:\"" + updateString + "\"");
        System.out.println();

        ArrayList<String> updateTableTokens = new ArrayList<>(Arrays.asList(updateString.split(" ")));

		/* Define table file name */
        String tableFileName = updateTableTokens.get(1) + ".tbl";

        // Check if the update query has the keyword "set"
        if (!updateTableTokens.get(2).equalsIgnoreCase("set")) {
            System.out.println(ErrorMessage.getErrorMessage(1026));
            return;
        }

        // Check if the update query has the keyword "where"
        if (updateString.indexOf("where") == -1) {
            System.out.println(ErrorMessage.getErrorMessage(1027));
            return;
        }

        // Check if the table exists
        if (!(new File("data/user_data/"+tableFileName).exists())) {
            System.out.println(ErrorMessage.getErrorMessage(1016));
            return;
        }

        String[] val2 = updateString.substring(updateString.indexOf("set") + 3,updateString.indexOf("where")-1).trim().replaceAll(" ","").replaceAll("\'","").replaceAll("\"","").split("=");
        updateColName = val2[0];
        updateValue = val2[1];

        String[] val1 = updateString.substring(updateString.indexOf("where")+5).trim().replaceAll(" ","").split("=");
        searchColName = val1[0];
        searchValue = val1[1];
        operator = "=";

//        updateColName = updateTableTokens.get(3);
//        updateValue = updateTableTokens.get(5);
//        searchColName = updateTableTokens.get(7);
//        operator = updateTableTokens.get(8);
//        searchValue = updateTableTokens.get(9);

        // Check if the primary key value is integer
        try {
            Integer.parseInt(searchValue);
        } catch (Exception e) {
            System.out.println(ErrorMessage.getErrorMessage(1024));
            return;
        }

        try {

            // Fetch data from davisbase columns
            dataSet = fetchColumnNames(updateTableTokens.get(1));

            String colNames = dataSet.get(0);
            String colTypes = dataSet.get(1);
            String colNullable = dataSet.get(2);
            String colPrimary = dataSet.get(3);
            String colOrdinalPosition = dataSet.get(4);

            String[] names = colNames.toString().trim().split(" ");
            String[] types = colTypes.toString().split(" ");
            String[] nullable = colNullable.toString().split(" ");
            String[] ordinal = colOrdinalPosition.toString().split(" ");
            String[] primary = colPrimary.toString().split(" ");

            // Check if the given column name exists in the davisbase_columns
            boolean columnExistsPri = false;
            boolean columnExists = false;

            for (int i = 0; i < names.length; i++) {
                if (searchColName.equalsIgnoreCase(names[i]) && i == 0)
                    columnExistsPri = true;
                if (updateColName.equalsIgnoreCase(names[i]) && i > 0)
                    columnExists = true;
            }

            if (!columnExistsPri) {
                System.out.println(ErrorMessage.getErrorMessage(1023));
                return;
            } else if (!columnExists) {
                System.out.println(ErrorMessage.getErrorMessage(1022));
                return;
            } else {
            }

		/*---------------------------------------------------------------------------------------------------------------------------------------------------*/

            // Fetch column data from the given table and check which data needs to be updated
            RandomAccessFile tableFile = new RandomAccessFile("data/user_data/"+tableFileName, "rw");

            int noOfRecords;
            int recordSize = 0;
            short lastRecLocation;
            int pageType;
            short readLocation;
            int currentPage = 0;
            int pageLocation = pageSize * currentPage;
            int currentRecord = 1;
            int noOfColumns = names.length;
            int index = 0;
            int cols = 0;
            ArrayList<Short> data = new ArrayList<>();
            int valueMatched = 0;
            boolean valueExists = false;
            short updateLocation = 0;
            int prevPointer = -1;
            String[] columnNames = colNames.toString().split(" ");


            // Btree page location depending on the given primary key value
            int btreeinfo[] = getBtreePageLocation(tableFile,Integer.parseInt(searchValue));
            pageLocation = btreeinfo[0];
            prevPointer = btreeinfo[1];

//            System.out.println(pageLocation);

            tableFile.seek(pageLocation);
            pageType = tableFile.read();
            noOfRecords = tableFile.read();

            while (currentRecord <= noOfRecords) {

                tableFile.seek(pageLocation + 6 + 2 * currentRecord);
                readLocation = tableFile.readShort();
                data.add(readLocation);
                tableFile.seek(readLocation + 2 + 4);
                cols = tableFile.read();
                long columnPointer = tableFile.getFilePointer();
                tableFile.seek(columnPointer + cols);
                int val = tableFile.readInt();

                if (val == Integer.parseInt(searchValue)) {
                    updateLocation = readLocation;
                    valueExists = true;
                }
                currentRecord++;
            }

            if(!valueExists){
                System.out.println(ErrorMessage.getErrorMessage(1029));
                tableFile.close();
                return;
            }
		/*---------------------------------------------------------------------------------------------------------------------------------------------------*/
            // Update the specified column

            int colLength = 0;
            currentRecord = 1;
            index = 0;
            cols = 0;

            tableFile.seek(pageLocation);
            pageType = tableFile.read();
            noOfRecords = tableFile.read();
            lastRecLocation = tableFile.readShort();

            tableFile.seek(updateLocation);
            recordSize = tableFile.readShort();

            tableFile.seek(updateLocation + 2 + 4);
            cols = tableFile.read();
            long columnPointer = tableFile.getFilePointer();
            tableFile.seek(columnPointer + cols);
            long dataPointer = tableFile.getFilePointer();

            while (index < noOfColumns) {
                tableFile.seek(columnPointer);
                int colDatatype = tableFile.read();
                // Text
                if (colDatatype > 0x0C) {
                    if (columnNames[index].equalsIgnoreCase(updateColName)) {
                        colLength = colDatatype - 12;
                        columnPointer = tableFile.getFilePointer();
                        if(colLength == updateValue.length()){
                            tableFile.seek(dataPointer);
                            tableFile.writeBytes(updateValue);
                            dataPointer = tableFile.getFilePointer();
                        } else if(colLength > updateValue.length()){
                            int l = colLength - updateValue.length();
                            tableFile.seek(dataPointer);
                            tableFile.writeBytes(updateValue);
                            for (int i = 0; i < l; i++)
                                tableFile.writeByte(0);
                            dataPointer = tableFile.getFilePointer();
                        } else {

                            // calculate the difference in length of strings
                            int diff = updateValue.length() - colLength;

                            //update new string length and pay load
                            colLength = updateValue.length() + 12;
                            recordSize = recordSize + diff;

                            // copy previous data
                            byte[] lastData = new byte[(int) (dataPointer-lastRecLocation)];
                            tableFile.seek(lastRecLocation);
                            tableFile.readFully(lastData);

                            // write the data
                            lastRecLocation = (short) (lastRecLocation - diff);
                            tableFile.seek(lastRecLocation);
                            tableFile.writeBytes(new String(lastData));

                            ArrayList<Short> locations = new ArrayList<>();
                            // Iterate the location and minus the differences
                            for (int i = 0; i < data.size(); i++) {
                                if(data.get(i) <= updateLocation){
                                    short newLocation = (short) (data.get(i) - diff);
                                    locations.add(newLocation);
                                } else {
                                    locations.add(data.get(i));
                                }
                            }

                            //write the lastRecord location
                            tableFile.seek(pageLocation + 2);
                            tableFile.writeShort(lastRecLocation);

                            // write the updated locations
                            tableFile.seek(pageLocation + 8);
                            for (int i = 0; i < locations.size(); i++) {
                                tableFile.writeShort(locations.get(i));
                            }

                            // write the updated value
                            tableFile.seek(dataPointer - diff);
                            tableFile.writeBytes(updateValue);
                            dataPointer = tableFile.getFilePointer();

                            columnPointer = columnPointer - diff;
                            tableFile.seek(columnPointer - 1);
                            tableFile.writeByte(colLength);

                            tableFile.seek(updateLocation - diff);
                            tableFile.writeShort(recordSize);
                        }
                    } else {
                        colLength = colDatatype - 12;
                        columnPointer = tableFile.getFilePointer();
                        byte[] col_name = new byte[colLength];
                        tableFile.seek(dataPointer);
                        tableFile.readFully(col_name);
                        dataPointer = tableFile.getFilePointer();
                    }
                }
                // Null Text
                else if (colDatatype == 0x0C) {
                    columnPointer = tableFile.getFilePointer();
                    tableFile.seek(dataPointer);
                    dataPointer = tableFile.getFilePointer();
                }
                // TinyInt
                else if (colDatatype == 0x04) {
                    columnPointer = tableFile.getFilePointer();
                    tableFile.seek(dataPointer);
                    if (columnNames[index].equalsIgnoreCase(updateColName))
                        tableFile.write(Integer.parseInt(updateValue));
                    else
                        tableFile.read();
                    dataPointer = tableFile.getFilePointer();
                }
                // Small Int
                else if (colDatatype == 0x05) {
                    columnPointer = tableFile.getFilePointer();
                    tableFile.seek(dataPointer);
                    if (columnNames[index].equalsIgnoreCase(updateColName))
                        tableFile.writeShort(Integer.parseInt(updateValue));
                    else
                        tableFile.readShort();
                    dataPointer = tableFile.getFilePointer();
                }
                // Int
                else if (colDatatype == 0x06) {
                    columnPointer = tableFile.getFilePointer();
                    tableFile.seek(dataPointer);
                    if (columnNames[index].equalsIgnoreCase(updateColName))
                        tableFile.writeInt(Integer.parseInt(updateValue));
                    else
                        tableFile.readInt();
                    dataPointer = tableFile.getFilePointer();
                }
                // BigInt
                else if (colDatatype == 0x07) {

                    columnPointer = tableFile.getFilePointer();
                    tableFile.seek(dataPointer);
                    if (columnNames[index].equalsIgnoreCase(updateColName)) {
                        tableFile.writeLong(Long.parseLong(updateValue));
                    } else {
                        tableFile.readLong();
                    }
                    dataPointer = tableFile.getFilePointer();

                }
                // Real
                else if (colDatatype == 0x08) {
                    columnPointer = tableFile.getFilePointer();
                    tableFile.seek(dataPointer);
                    if (columnNames[index].equalsIgnoreCase(updateColName)) {
                        tableFile.writeFloat(Float.parseFloat(updateValue));
                    } else {
                        tableFile.readFloat();
                    }
                    dataPointer = tableFile.getFilePointer();
                }
                // Double
                else if (colDatatype == 0x09) {
                    columnPointer = tableFile.getFilePointer();
                    tableFile.seek(dataPointer);
                    if (columnNames[index].equalsIgnoreCase(updateColName)) {
                        tableFile.writeDouble(Double.parseDouble(updateValue));
                    } else {
                        tableFile.readDouble();
                    }
                    dataPointer = tableFile.getFilePointer();
                }
                // DateTime
                else if (colDatatype == 0x0A) {
                    columnPointer = tableFile.getFilePointer();
                    tableFile.seek(dataPointer);
                    dataPointer = tableFile.getFilePointer();
                }
                // Date
                else if (colDatatype == 0x0B) {
                    columnPointer = tableFile.getFilePointer();
                    tableFile.seek(dataPointer);
                    dataPointer = tableFile.getFilePointer();
                }
                // TinyInt Null
                else if (colDatatype == 0x01) {
                    columnPointer = tableFile.getFilePointer();
                    tableFile.seek(dataPointer);
                    tableFile.skipBytes(1);
                    dataPointer = tableFile.getFilePointer();
                }
                // Small Int Null
                else if (colDatatype == 0x02) {
                    columnPointer = tableFile.getFilePointer();
                    tableFile.seek(dataPointer);
                    tableFile.skipBytes(2);
                    dataPointer = tableFile.getFilePointer();
                }
                // Int Null
                else if (colDatatype == 0x03) {
                    columnPointer = tableFile.getFilePointer();
                    tableFile.seek(dataPointer);
                    tableFile.skipBytes(4);
                    dataPointer = tableFile.getFilePointer();
                }
                // Double Null
                else if (colDatatype == 0x04) {
                    columnPointer = tableFile.getFilePointer();
                    tableFile.seek(dataPointer);
                    tableFile.skipBytes(8);
                    dataPointer = tableFile.getFilePointer();
                } else {
                }
                index++;
            }
            tableFile.close();

            System.out.println("Record updated successfully.");
        }
        catch(Exception e){
            System.out.println(e);
        }

    }

    /**---------------------------------------------------------------------Update Query Ends----------------------------------------------------------------------------*/



    /**---------------------------------------------------------------------Fetch Columns--------------------------------------------------------------------------------*/

    public static ArrayList<String> fetchColumnNames(String tableName) {

        ArrayList<String> dataSet = new ArrayList<>();

        int pageType;
        int noOfRecords;
        short readLocation;
        int currentPage = 0;
        int pageLocation = pageSize * currentPage;
        int currentRecord = 1;
        int noOfColumns = 0;
        int key = 0;
        short lastRecordLocation;
        int rightSibling;

        // Fetch column names from davisbase columns
        int tableLength;
        int colLength;
        int datatypeLength;
        int isnullableLength;
        int priLength;
        int prevPointer = -1;
        String tblName;


        StringBuffer colNames = new StringBuffer();
        StringBuffer colTypes = new StringBuffer();
        StringBuffer colNullable = new StringBuffer();
        StringBuffer colPrimary = new StringBuffer();
        StringBuffer colOrdinalPosition = new StringBuffer();

        RandomAccessFile colPointer;
        try {

            colPointer = new RandomAccessFile("data/catalog/davisbase_columns.tbl", "rw");

            // Btree page location depending on the given primary key value
            int btreeinfo[] = getBtreePageLocation(colPointer,key);
            pageLocation = btreeinfo[0];
            prevPointer = btreeinfo[1];

            colPointer.seek(pageLocation);
            pageType = colPointer.read();
            noOfRecords = colPointer.read();
            lastRecordLocation = colPointer.readShort();
            rightSibling = colPointer.readInt();

            while(pageLocation != 0xFFFFFFFF){
                currentRecord = 1;
                while (currentRecord <= noOfRecords) {
                    colPointer.seek(pageLocation + 6 + 2 * currentRecord);
                    readLocation = colPointer.readShort();

                    colPointer.seek(readLocation + 7 + 1);
                    tableLength = colPointer.read() - 12;
                    colLength = colPointer.read() - 12;
                    datatypeLength = colPointer.read() - 12;
                    colPointer.read();
                    priLength = colPointer.read() - 12;
                    isnullableLength = colPointer.read() - 12;
                    colPointer.skipBytes(4);

                    byte[] t = new byte[tableLength];
                    colPointer.readFully(t);
                    tblName = new String(t);

                    if (tblName.equals(tableName)) {

                        byte[] col_name = new byte[colLength];
                        colPointer.readFully(col_name);
                        colNames.append(new String(col_name) + " ");

                        byte[] col_type = new byte[datatypeLength];
                        colPointer.readFully(col_type);
                        colTypes.append(new String(col_type) + " ");

                        colOrdinalPosition.append(colPointer.read() + " ");

                        byte[] col_pri = new byte[priLength];
                        colPointer.readFully(col_pri);
                        colPrimary.append(new String(col_pri) + " ");

                        byte[] col_isnull = new byte[isnullableLength];
                        colPointer.readFully(col_isnull);
                        colNullable.append(new String(col_isnull) + " ");

                        noOfColumns++;
                    }
                    currentRecord++;
                }

                pageLocation = rightSibling;

                if(pageLocation != 0xFFFFFFFF){
                    colPointer.seek(pageLocation);
                    pageType = colPointer.read();
                    noOfRecords = colPointer.read();
                    lastRecordLocation = colPointer.readShort();
                    rightSibling = colPointer.readInt();
                }
            }

            colPointer.close();
            dataSet.add(colNames.toString());
            dataSet.add(colTypes.toString());
            dataSet.add(colNullable.toString());
            dataSet.add(colPrimary.toString());
            dataSet.add(colOrdinalPosition.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataSet;
    }

    /**---------------------------------------------------------------------Fetch Column Ends---------------------------------------------------------------------------*/

    /**---------------------------------------------------------------------Table Metadata------------------------------------------------------------------------------*/


    public static void addTableToMetadata(String tableName) {

        int noOfRecords;
        int recordSize = 1 + 2 + 4 + tableName.length();
        int noOfColumns = 2;
        short writeLocation;
        int currentPage = 0;
        int pageLocation = pageSize * currentPage;
        int pageType;
        int rowid = 0;
        int prevPointer = -1;
        int rightPointer;

        try {

            LinkedHashMap<Integer,Integer> rootPrimaryIndexes = new LinkedHashMap<>();
            RandomAccessFile tableFile = new RandomAccessFile("data/catalog/davisbase_tables.tbl", "rw");
            // Check if data page or node page
            tableFile.seek(0);
            pageType = tableFile.readByte();

            // Row id
            if(pageType != 0x0D) {
                tableFile.seek(8);
                rowid = tableFile.readInt();
            }
            // Btree page location depending on the given primary key value
            int btreeinfo[] = getBtreePageLocation(tableFile,-1);
            pageLocation = btreeinfo[0];
            prevPointer = btreeinfo[1];

            tableFile.seek(pageLocation);
            pageType = tableFile.read();
            noOfRecords = tableFile.read();
            writeLocation = tableFile.readShort();
            rightPointer = tableFile.readInt();
            ArrayList<Short> headPointers = new ArrayList<>();
            int[] pKeys = new int[noOfRecords];

            long indexPointer = 0;
            for(int currentRecord = 0; currentRecord < noOfRecords ; currentRecord++){
                tableFile.seek(pageLocation + 8 + 2*currentRecord);
                headPointers.add(tableFile.readShort());
                indexPointer = tableFile.getFilePointer();
                tableFile.seek(headPointers.get(currentRecord) + 2 + 4 + 1 + noOfColumns);
                pKeys[currentRecord] = tableFile.readInt();
            }

            if (rowid != 0)
                rowid++;
            else if (noOfRecords == 0)
                rowid = 1;
            else {
                tableFile.seek(writeLocation + 2);
                rowid = tableFile.readInt();
                rowid++;
            }

            noOfRecords++;

            boolean rootSplit = false;

            // Split the page as its full
            if((writeLocation - recordSize - 6 ) < (indexPointer + 0x10)) {
                //Root split
                int size = (int) new File("data/catalog/davisbase_tables.tbl").length(); //512
                if (pageLocation == 0) {
                    //  System.out.println("Root page splits");
                    rootSplit = true;
                    byte[] wholePageData = new byte[size];
                    tableFile.seek(0);
                    tableFile.readFully(wholePageData);
                    byte[] newData = new byte[wholePageData.length + pageSize];
                    System.arraycopy(wholePageData, 0, newData, pageSize, wholePageData.length);
                    tableFile.setLength(newData.length);
                    tableFile.seek(0);
                    tableFile.write(newData);
                    size = (int) new File("data/catalog/davisbase_tables.tbl").length();
                    pageLocation = pageSize;

                    tableFile.seek(pageLocation + 1);
                    int num_rec = tableFile.readByte();
                    int lastWriteLoc = tableFile.readShort() + pageSize;
                    tableFile.seek(pageLocation + 2);
                    tableFile.writeShort(lastWriteLoc);
                    tableFile.skipBytes(4);
                    ArrayList<Short> temp = new ArrayList<>();
                    for (int j = 0; j < num_rec; j++)
                        temp.add((short) (headPointers.get(j) + 512));
                    headPointers = temp;
                    for (int j = 0; j < num_rec; j++)
                        tableFile.writeShort(headPointers.get(j));

                    //write root page contents
                    tableFile.seek(0);
                    tableFile.writeByte(0x05);
                    tableFile.writeByte(1);
                    tableFile.skipBytes(2);
                    tableFile.writeInt(size);
                    tableFile.writeInt(rowid);
                    tableFile.writeInt(pageSize);
                    tableFile.writeInt(pKeys[pKeys.length / 2 - 1]);
                    long pointer = tableFile.getFilePointer();
                    tableFile.seek(2);
                    tableFile.writeShort((short) pointer);
                    rootPrimaryIndexes.put(pageSize, pKeys[pKeys.length / 2 - 1]);

              //      System.out.println("HaashMap entry" + pageSize + "" + pKeys[pKeys.length / 2 - 1]);

                }

                tableFile.seek(1);
                int rec = tableFile.readByte();
                tableFile.skipBytes(10);
                for(int k = 0 ; k < rec ; k++){
                    int loc = tableFile.readInt();
                    int p = tableFile.readInt();
                    rootPrimaryIndexes.put(loc,p);
                }

                // Normal Data Page Split
                tableFile.setLength(size + pageSize);
                size = size + pageSize;

                int rightpage = size - pageSize;

                boolean rightPageSplits = false;
                // IF the right page splits
                if(rightPointer == 0xFFFFFFFF && !rootSplit){
                    tableFile.seek(0 + 4);
                    tableFile.writeInt(size - pageSize);
                    int newPkey = pKeys[pKeys.length/2-1];
                    rootPrimaryIndexes.put(pageLocation,newPkey);
            //        System.out.println("HashMap entry" + pageLocation + "" + newPkey);
                    tableFile.seek(0 + 12);
                    for(Map.Entry<Integer,Integer> entry : rootPrimaryIndexes.entrySet()){
                        tableFile.writeInt(entry.getKey());
                        tableFile.writeInt(entry.getValue());
                    }

                    tableFile.seek(1);
                    int n_rec = tableFile.readByte();
                    tableFile.seek(1);
                    tableFile.writeByte(++n_rec);
                    rightPageSplits = true;
                }

                // Left Page
                tableFile.seek(pageLocation+4);
                int oldRightPointer = tableFile.readInt();
                //tableFile.skipBytes(4);
                ArrayList<byte[]> left = new ArrayList<>();
                ArrayList<byte[]> right = new ArrayList<>();
                ArrayList<Integer> leftKeys = new ArrayList<>();
                ArrayList<Integer> rightKeys = new ArrayList<>();
                for(int j = 0; j < pKeys.length ; j++){
                    short readLoc = tableFile.readShort();
                    long nxtPointer = tableFile.getFilePointer();
                    tableFile.seek(readLoc);
                    int payload = tableFile.readShort();
                    byte[] b = new byte[payload + 6];
                    tableFile.seek(readLoc);
                    tableFile.readFully(b);
                    if( j <= (pKeys.length/2 - 1)) {
                        left.add(b);
                        leftKeys.add(pKeys[j]);
                    }
                    else {
                        right.add(b);
                        rightKeys.add(pKeys[j]);
                    }
                    tableFile.seek(nxtPointer);
                }

              //  System.out.println(leftKeys.toString());
              //  System.out.println(rightKeys.toString());
              //  System.out.println("Left page " + pageLocation);

                short lastRecLocation = (short) (pageLocation + pageSize);
                // clear the left page
                tableFile.seek(pageLocation+8);
                byte[] cl = new byte[pageSize - 8];
                tableFile.write(cl);
                for (int j = 0; j < left.size(); j++) {
                    tableFile.seek(lastRecLocation - left.get(j).length);
                    lastRecLocation = (short)tableFile.getFilePointer();
                    tableFile.write(left.get(j));
                    tableFile.seek(pageLocation + 2);
                    tableFile.writeShort(lastRecLocation);
                    tableFile.seek(pageLocation + 8  + 2 * j);
                    tableFile.writeShort(lastRecLocation);
                }
                tableFile.seek(pageLocation + 1);
                tableFile.writeByte(leftKeys.size());
                tableFile.writeShort(lastRecLocation);
                tableFile.writeInt(rightpage);

                //Right page
                tableFile.seek(rightpage);
                tableFile.writeByte(0x0D);
                tableFile.writeByte(rightKeys.size());
                tableFile.writeShort(size);
                tableFile.writeInt(oldRightPointer);
                lastRecLocation = (short)size;
                for (int j = 0; j < right.size(); j++) {
                    tableFile.seek(lastRecLocation - right.get(j).length);
                    lastRecLocation = (short)tableFile.getFilePointer();
                    tableFile.write(right.get(j));
                    tableFile.seek(rightpage + 2);
                    tableFile.writeShort(lastRecLocation);
                    tableFile.seek(rightpage + 8  + 2 * j);
                    tableFile.writeShort(lastRecLocation);
                }

                // Add the primary key to the root page if left page splits
                if(!rootSplit && !rightPageSplits){
                    int newPkey = pKeys[pKeys.length/2-1];
                    int oldPkey = rootPrimaryIndexes.get(pageLocation);
                    rootPrimaryIndexes.put(pageLocation,newPkey);
                    rootPrimaryIndexes.put(rightpage,oldPkey);
                    tableFile.seek(0 + 12);
                    for(Map.Entry<Integer,Integer> entry : rootPrimaryIndexes.entrySet()){
                        tableFile.writeInt(entry.getKey());
                        tableFile.writeInt(entry.getValue());
                    }
                    tableFile.seek(1);
                    int n_rec = tableFile.readByte();
                    tableFile.seek(1);
                    tableFile.writeByte(++n_rec);
                }

                if(rowid <= leftKeys.get(leftKeys.size()-1))
                    pageLocation = pageLocation;
                else
                    pageLocation = rightpage;
            }

            tableFile.seek(pageLocation);
            pageType = tableFile.read();
            noOfRecords = tableFile.read();
            writeLocation = tableFile.readShort();
            noOfRecords++;
           // Minus 2 and 4 for payload and rowid
            // Plus 12 is adding 0x0C for text
            tableFile.seek(writeLocation - recordSize - 2 - 4);
            tableFile.writeShort(recordSize);
            tableFile.writeInt(rowid);
            tableFile.writeByte(2);
            tableFile.writeByte(dataTypeCode.get("int"));
            tableFile.writeByte(tableName.length() + 12);
            tableFile.writeInt(rowid);
            tableFile.writeBytes(tableName);

            tableFile.seek(pageLocation + 1);
            tableFile.writeByte(noOfRecords);
            tableFile.writeShort(writeLocation - recordSize - 6);

            tableFile.seek(pageLocation + 6 + 2 * noOfRecords);
            tableFile.writeShort(writeLocation - recordSize - 6);

            tableFile.seek(0);
            pageType = tableFile.readByte();

            if(pageType == 0x05) {
                tableFile.seek(8);
                tableFile.writeInt(rowid);
            }
            tableFile.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**---------------------------------------------------------------------Table Metadata Ends-------------------------------------------------------------------------*/


    /**---------------------------------------------------------------------Column Metadata-----------------------------------------------------------------------------*/


    private static void addColumnsToMetadata(String tableName, List<String> columnsList) {

        int noOfRecords;
        int noOfColumns = 7;
        int recordSize;
        short writeLocation;
        int currentPage = 0;
        int pageLocation = pageSize * currentPage;
        int pageType;
        int ordinalPosition = 0;
        String is_nullable = "YES";
        String column_name;
        String data_type;
        String pri = "NO";
        int rowid = 0;

        List<String> updatedColumnsList = new ArrayList<String>();

        // validate if the data type are correct and if the not null key is mentioned correctly
        for (int i = 0; i < columnsList.size(); i++) {
            String[] columns = columnsList.get(i).trim().split(" ");
            column_name = columns[0];
            data_type = columns[1];
            if (i == 0) {
                if (!data_type.equals("int")) {
                    System.out.println(ErrorMessage.getErrorMessage(1013));
                    return;
                }
                if (!(columns[2].equals("primary") && columns[3].equals("key"))) {
                    System.out.println(ErrorMessage.getErrorMessage(1014));
                    return;
                }
                pri = "PRI";
                is_nullable = "NO";
            }
            if (!dataTypeCode.containsKey(data_type)) {
                System.out.println(ErrorMessage.getErrorMessage(1007));
                return;
            }
            if (columns.length > 2 && (i > 0)) {
                is_nullable = (columns[2].equals("not") && columns[3].equals("null")) ? "NO" : "YES";
            }
            updatedColumnsList.add(column_name + " " + data_type + " " + pri + " " + is_nullable);
            is_nullable = "YES";
            pri = "NO";
        }

        //	System.out.println(updatedColumnsList.toString());
        try {
            int prevPointer = -1;
            int rightPointer;
            LinkedHashMap<Integer,Integer> rootPrimaryIndexes = new LinkedHashMap<>();

            RandomAccessFile tableFile = new RandomAccessFile("data/catalog/davisbase_columns.tbl", "rw");

            for (int i = 0; i < updatedColumnsList.size(); i++) {
                String[] columns = updatedColumnsList.get(i).split(" ");
                column_name = columns[0];
                data_type = columns[1];
                pri = columns[2];
                is_nullable = columns[3];
                ordinalPosition++;
                recordSize = 4 + tableName.length() + column_name.length() + data_type.length() + 1 + is_nullable.length() + pri.length() + 1 + noOfColumns;
                // Check if data page or node page
                tableFile.seek(0);
                pageType = tableFile.readByte();
                // Row id
                if(pageType != 0x0D) {
                    tableFile.seek(8);
                    rowid = tableFile.readInt();
                }
                // Btree page location depending on the given primary key value
                int btreeinfo[] = getBtreePageLocation(tableFile,-1);
                pageLocation = btreeinfo[0];
                prevPointer = btreeinfo[1];

                // Seek to the start position
                tableFile.seek(pageLocation);
                pageType = tableFile.read();
                noOfRecords = tableFile.read();
                writeLocation = tableFile.readShort();
                rightPointer = tableFile.readInt();
                ArrayList<Short> headPointers = new ArrayList<>();
                int[] pKeys = new int[noOfRecords];

                long indexPointer = 0;
                for(int currentRecord = 0; currentRecord < noOfRecords ; currentRecord++){
                    tableFile.seek(pageLocation + 8 + 2*currentRecord);
                    headPointers.add(tableFile.readShort());
                    indexPointer = tableFile.getFilePointer();
                    tableFile.seek(headPointers.get(currentRecord) + 2 + 4 + 1 + noOfColumns);
                    pKeys[currentRecord] = tableFile.readInt();
                }

                if (rowid != 0)
                    rowid++;
                else if (noOfRecords == 0)
                    rowid = 1;
                else {
                    tableFile.seek(writeLocation + 2);
                    rowid = tableFile.readInt();
                    rowid++;
                }
                noOfRecords++;

                boolean rootSplit = false;

                // Split the page as its full
                if((writeLocation - recordSize - 6 ) < (indexPointer + 0x10)) {
                    //Root split
                    int size = (int) new File("data/catalog/davisbase_columns.tbl").length(); //512
                    if (pageLocation == 0) {
                        //  System.out.println("Root page splits");
                        rootSplit = true;
                        byte[] wholePageData = new byte[size];
                        tableFile.seek(0);
                        tableFile.readFully(wholePageData);
                        byte[] newData = new byte[wholePageData.length + pageSize];
                        System.arraycopy(wholePageData, 0, newData, pageSize, wholePageData.length);
                        tableFile.setLength(newData.length);
                        tableFile.seek(0);
                        tableFile.write(newData);
                        size = (int) new File("data/catalog/davisbase_columns.tbl").length(); //1024
                        pageLocation = pageSize;

                        tableFile.seek(pageLocation + 1);
                        int num_rec = tableFile.readByte();
                        int lastWriteLoc = tableFile.readShort() + pageSize;
                        tableFile.seek(pageLocation + 2);
                        tableFile.writeShort(lastWriteLoc);
                        tableFile.skipBytes(4);
                        ArrayList<Short> temp = new ArrayList<>();
                        for (int j = 0; j < num_rec; j++)
                            temp.add((short) (headPointers.get(j) + 512));
                        headPointers = temp;
                        for (int j = 0; j < num_rec; j++)
                            tableFile.writeShort(headPointers.get(j));

                        //write root page contents
                        tableFile.seek(0);
                        tableFile.writeByte(0x05);
                        tableFile.writeByte(1);
                        tableFile.skipBytes(2);
                        tableFile.writeInt(size);
                        tableFile.writeInt(rowid);
                        tableFile.writeInt(pageSize);
                        tableFile.writeInt(pKeys[pKeys.length / 2 - 1]);
                        long pointer = tableFile.getFilePointer();
                        tableFile.seek(2);
                        tableFile.writeShort((short) pointer);
                        rootPrimaryIndexes.put(pageSize, pKeys[pKeys.length / 2 - 1]);

                    }

                    tableFile.seek(1);
                    int rec = tableFile.readByte();
                    tableFile.skipBytes(10);
                    for(int k = 0 ; k < rec ; k++){
                        int loc = tableFile.readInt();
                        int p = tableFile.readInt();
                        rootPrimaryIndexes.put(loc,p);
                    }

                    // Normal Data Page Split
                    tableFile.setLength(size + pageSize);
                    size = size + pageSize; //1536

                    int rightpage = size - pageSize;

                    boolean rightPageSplits = false;
                    // IF the right page splits
                    if(rightPointer == 0xFFFFFFFF && !rootSplit){
                        tableFile.seek(0 + 4);
                        tableFile.writeInt(size - pageSize);
                        int newPkey = pKeys[pKeys.length/2-1];
                        rootPrimaryIndexes.put(pageLocation,newPkey);
                        tableFile.seek(0 + 12);
                        for(Map.Entry<Integer,Integer> entry : rootPrimaryIndexes.entrySet()){
                            tableFile.writeInt(entry.getKey());
                            tableFile.writeInt(entry.getValue());
                        }

                        tableFile.seek(1);
                        int n_rec = tableFile.readByte();
                        tableFile.seek(1);
                        tableFile.writeByte(++n_rec);
                        rightPageSplits = true;
                    }

                    // Left Page
                    tableFile.seek(pageLocation+4);
                    int oldRightPointer = tableFile.readInt();
                    //tableFile.skipBytes(4);
                    ArrayList<byte[]> left = new ArrayList<>();
                    ArrayList<byte[]> right = new ArrayList<>();
                    ArrayList<Integer> leftKeys = new ArrayList<>();
                    ArrayList<Integer> rightKeys = new ArrayList<>();
                    for(int j = 0; j < pKeys.length ; j++){
                        short readLoc = tableFile.readShort();
                        long nxtPointer = tableFile.getFilePointer();
                        tableFile.seek(readLoc);
                        int payload = tableFile.readShort();
                        byte[] b = new byte[payload + 6];
                        tableFile.seek(readLoc);
                        tableFile.readFully(b);
                        if( j <= (pKeys.length/2 - 1)) {
                            left.add(b);
                            leftKeys.add(pKeys[j]);
                        }
                        else {
                            right.add(b);
                            rightKeys.add(pKeys[j]);
                        }
                        tableFile.seek(nxtPointer);
                    }

                //    System.out.println(leftKeys.toString());
                //    System.out.println(rightKeys.toString());
                //    System.out.println("Left page " + pageLocation);

                    short lastRecLocation = (short) (pageLocation + pageSize);
                    // clear the left page
                    tableFile.seek(pageLocation+8);
                    byte[] cl = new byte[pageSize - 8];
                    tableFile.write(cl);
                    for (int j = 0; j < left.size(); j++) {
                        tableFile.seek(lastRecLocation - left.get(j).length);
                        lastRecLocation = (short)tableFile.getFilePointer();
                        tableFile.write(left.get(j));
                        tableFile.seek(pageLocation + 2);
                        tableFile.writeShort(lastRecLocation);
                        tableFile.seek(pageLocation + 8  + 2 * j);
                        tableFile.writeShort(lastRecLocation);
                    }
                    tableFile.seek(pageLocation + 1);
                    tableFile.writeByte(leftKeys.size());
                    tableFile.writeShort(lastRecLocation);
                    tableFile.writeInt(rightpage);

                    //Right page
                    tableFile.seek(rightpage);
                    tableFile.writeByte(0x0D);
                    tableFile.writeByte(rightKeys.size());
                    tableFile.writeShort(size);
                    tableFile.writeInt(oldRightPointer);
                    lastRecLocation = (short)size;
                    for (int j = 0; j < right.size(); j++) {
                        tableFile.seek(lastRecLocation - right.get(j).length);
                        lastRecLocation = (short)tableFile.getFilePointer();
                        tableFile.write(right.get(j));
                        tableFile.seek(rightpage + 2);
                        tableFile.writeShort(lastRecLocation);
                        tableFile.seek(rightpage + 8  + 2 * j);
                        tableFile.writeShort(lastRecLocation);
                    }

                    // Add the primary key to the root page if left page splits
                    if(!rootSplit && !rightPageSplits){
                        int newPkey = pKeys[pKeys.length/2-1];
                        int oldPkey = rootPrimaryIndexes.get(pageLocation);
                        rootPrimaryIndexes.put(pageLocation,newPkey);
                        rootPrimaryIndexes.put(rightpage,oldPkey);
                        tableFile.seek(0 + 12);
                        for(Map.Entry<Integer,Integer> entry : rootPrimaryIndexes.entrySet()){
                            tableFile.writeInt(entry.getKey());
                            tableFile.writeInt(entry.getValue());
                        }
                        tableFile.seek(1);
                        int n_rec = tableFile.readByte();
                        tableFile.seek(1);
                        tableFile.writeByte(++n_rec);
                    }

                    if(rowid <= leftKeys.get(leftKeys.size()-1))
                        pageLocation = pageLocation;
                    else
                        pageLocation = rightpage;
                }

                tableFile.seek(pageLocation);
                pageType = tableFile.read();
                noOfRecords = tableFile.read();
                writeLocation = tableFile.readShort();
                noOfRecords++;
                writeLocation = (short) (writeLocation - recordSize - 4 - 2);
                // Plus 12 is adding 0x0C for text
                tableFile.seek(writeLocation);
                tableFile.writeShort(recordSize);
                tableFile.writeInt(rowid);
                tableFile.writeByte(noOfColumns);
                tableFile.writeByte(dataTypeCode.get("int"));
                tableFile.writeByte(tableName.length() + 12);
                tableFile.writeByte(column_name.length() + 12);
                tableFile.writeByte(data_type.length() + 12);
                tableFile.writeByte(dataTypeCode.get("tinyint"));
                tableFile.writeByte(pri.length() + 12);
                tableFile.writeByte(is_nullable.length() + 12);
                tableFile.writeInt(rowid);
                tableFile.writeBytes(tableName);
                tableFile.writeBytes(column_name);
                tableFile.writeBytes(data_type);
                tableFile.writeByte(ordinalPosition);
                tableFile.writeBytes(pri);
                tableFile.writeBytes(is_nullable);

                tableFile.seek(pageLocation);
                tableFile.writeByte(pageType);
                tableFile.writeByte(noOfRecords);
                tableFile.writeShort(writeLocation);
                tableFile.seek(pageLocation + 6 + 2 * noOfRecords);
                tableFile.writeShort(writeLocation);

                tableFile.seek(0);
                pageType = tableFile.readByte();
                if(pageType == 0x05) {
                    tableFile.seek(8);
                    tableFile.writeInt(rowid);
                }

            }

            tableFile.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }



    /**---------------------------------------------------------------------Column Metadata Ends------------------------------------------------------------------------*/


    /**---------------------------------------------------------------------Main----------------------------------------------------------------------------------------*/
    public static void main(String[] args) {

        dataTypeCode = new HashMap<>();
        dataTypeCode.put("tinynull", 0x00);
        dataTypeCode.put("smallnull", 0x01);
        dataTypeCode.put("intnull", 0x02);
        dataTypeCode.put("doublenull", 0x03);
        dataTypeCode.put("tinyint", 0x04);
        dataTypeCode.put("smallint", 0x05);
        dataTypeCode.put("int", 0x06);
        dataTypeCode.put("bigint", 0x07);
        dataTypeCode.put("real", 0x08);
        dataTypeCode.put("float", 0x08);
        dataTypeCode.put("double", 0x09);
        dataTypeCode.put("datetime", 0x0A);
        dataTypeCode.put("date", 0x0B);
        dataTypeCode.put("text", 0x0C);

        dataTypeValue = new HashMap<>();
        dataTypeValue.put(0x00, 1);
        dataTypeValue.put(0x01, 2);
        dataTypeValue.put(0x02, 4);
        dataTypeValue.put(0x03, 8);
        dataTypeValue.put(0x04, 1);
        dataTypeValue.put(0x05, 2);
        dataTypeValue.put(0x06, 4);
        dataTypeValue.put(0x07, 8);
        dataTypeValue.put(0x08, 4);
        dataTypeValue.put(0x09, 8);
        dataTypeValue.put(0x0A, 8);
        dataTypeValue.put(0x0B, 8);

		/* Variable to collect user input from the prompt */
        String userCommand = "";

        File f = new File("data/catalog/davisbase_tables.tbl");

        if (!f.exists()) {
            userCommand = "CREATE TABLE davisbase_tables (rowid int primary key, table_name text );";
            try {
                Files.createDirectory(Paths.get("data"));
                Files.createDirectory(Paths.get("data/catalog"));
                Files.createDirectory(Paths.get("data/user_data"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            parseCreateString(userCommand, "catalog");
            addTableToMetadata("davisbase_tables");
        }

        f = new File("data/catalog/davisbase_columns.tbl");

        if (!f.exists()) {
            userCommand = "CREATE TABLE davisbase_columns (rowid int primary key, table_name text, column_name text, data_type text, ordinal_position tinyint, column_key text, is_nullable text );";
            parseCreateString(userCommand, "catalog");

            addTableToMetadata("davisbase_columns");

            ArrayList<String> davisbaseTablesColumns = new ArrayList<>();
            davisbaseTablesColumns.add("rowid int primary key");
            davisbaseTablesColumns.add("table_name text not null");

            addColumnsToMetadata("davisbase_tables", davisbaseTablesColumns);

            ArrayList<String> davisbaseColumnsColumns = new ArrayList<>();
            davisbaseColumnsColumns.add("rowid int primary key");
            davisbaseColumnsColumns.add("table_name text not null");
            davisbaseColumnsColumns.add("column_name text not null");
            davisbaseColumnsColumns.add("data_type text not null");
            davisbaseColumnsColumns.add("ordinal_position tinyint not null");
            davisbaseColumnsColumns.add("column_key text not null");
            davisbaseColumnsColumns.add("is_nullable text not null");

            addColumnsToMetadata("davisbase_columns", davisbaseColumnsColumns);
        }

        displayScreen();

        while (!isExit) {
            System.out.print(prompt);
			/* toLowerCase() renders command case insensitive */
            userCommand = scanner.next().replace("\n", "").replace("\r", "").trim().toLowerCase();

            parseUserCommand(userCommand);
        }
        System.out.println("Exiting...");

    }

    /**---------------------------------------------------------------------Main Ends-----------------------------------------------------------------------------------*/

}

