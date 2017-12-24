/**
 * Created by Dhawal Parmar on 15-04-2017.
 */
public class ErrorMessage {
    static int errCode;

    public static String getErrorMessage(int errCode){

        switch(errCode){
            case 1004:
                return "Keyword TABLE is missing from the query. Please create the table using the syntax "
                        + "CREATE TABLE table_name (column_name column_type);";

            case 1005:
                return "Keyword INTO is missing from the SQL query. Please insert into a table using the syntax "
                        + "INSERT INTO TABLE (column_list) table_name VALUES (value1,value2,value3,…);";

            case 1006:
                return "Number of attributes in column_list and values are not same. Please insert into a table using the syntax "
                        + "INSERT INTO TABLE (column_list) table_name VALUES (value1,value2,value3);";

            case 1007:
                return "Data type mentioned is not correct. Please enter valid data type";

            case 1008:
                return "Keyword FROM is missing from the SQL Query. Please use the following syntax: SELECT * FROM table_name WHERE column_name operator value;";

            case 1009:
                return "Table name consists of special character '('. Please create the table using the syntax "
                        + "CREATE TABLE table_name (column_name column_type);";
            case 1010:
                return "Please enter the missing column as it has a constraint \"NOT NULL\"";

            case 1011:
                return "Error: Column_Name_Mismatch. One of the column names does not match. Please check the column names and insert again";

            case 1012:
                return "Error: Operator Undefined. Please check the operator used in where clause";


            case 1013:
                return "Error: Wrong datatype. First column has to be of INT datatype";

            case 1014:
                return "Error: Primary Constraint missing. First column has to be the primary key.";

            case 1015:
                return "Error: Table already Created. The table already exists in the directory.";

            case 1016:
                return "Error: File does not exist.";

            case 1017:
                return "Error: Wrong Operator. This operator cannot be applied to this datatype";

            case 1018:
                return "Error: String cannot be compared with Int datatype. Please check the query.";

            case 1019:
                return "Error: Keyword Missing. Keyword FROM is missing from the SQL query. Please follow the syntax "
                        + "DELETE FROM TABLE table_name WHERE row_id = key_value;";

            case 1020:
                return "Error: Keyword Missing. Keyword TABLE is missing from the SQL query. Please follow the syntax "
                        + "DELETE FROM TABLE table_name WHERE row_id = key_value;";

            case 1021:
                return "Error: Keyword Missing. Keyword WHERE is missing from the SQL query. Please follow the syntax "
                        + "DELETE FROM TABLE table_name WHERE row_id = key_value;";

            case 1022:
                return "Error: Column_Name_Mismatch. The column name does not exist in the table";


            case 1023:
                return "Error: Primary_Column Constraint. The column is not a primary column";


            case 1024:
                return "Error: Value DataType Mismatch. Value should be of Integer datatype.";

            case 1025:
                return "No results found.";

            case 1026:
                return "Error: Keyword Missing. Keyword SET is missing from the SQL query. Please follow the syntax "
                        + "UPDATE table_name SET column_name = value WHERE primary_key = key_value;";
            case 1027:
                return "Error: Keyword Missing. Keyword WHERE is missing from the SQL query. Please follow the syntax "
                        + "UPDATE table_name SET column_name = value WHERE primary_key = key_value;";

            case 1028:
                return "Error: Duplicate Primary Key Constraint. Please insert unique primary key value.";

            case 1029:
                return "Error: No value found. The input primary key value does not exist.";

            default:
                return null;
        }
    }


}
