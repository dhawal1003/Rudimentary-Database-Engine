# Rudimentary-Database-Engine

This project is a rudimentary database engine that is loosely based on a hybrid between MySQL and SQLite. The program operates entirely from the command line and API calls (no GUI). Each database table is physically stored as a separate file. Each table file is subdivided into logical sections of fixed equal size call pages. Therefore, each table file size is exact increments of the global page_size attribute, i.e. all data files must share the same page_size attribute. The page_size can be a configurable attribute, but by default it supports a page size of 512 Bytes.

Supported Commands:
 
1) Show tables;
	Displays all the tables in the database.
2) create table table_name (id int primary key, name text not null);
	Creates a new table with the given attributes and constraints.
3) insert into table_name (id, name) values (value, value); OR insert into table_name values (value, value);
	Insert a new row in the table.
4) select * from table_name;
	Displays all the rows in the given table.
5) select * from table_name where id = value; 
	Displays record whose id is matched to the value.
	Operators supported for integer are: =, >, >=, <, <=, !=
6) select column_name from table_name where id = value;
	Displays specific column of the record whose id is matched to the value.
7) update table_name set column_name=value where id= value;
	Update the specific column of the table whose record has a id equal to the value.
8) delete from table table_name where id= value;
	Deletes the record whose id is matched to the value.
9) drop table table_name;
	Drops the entire table.
10) help;
	Displays the valid commands list.

How to Execute:
1) Copy the files InitPrompt and ErrorMessage in your folder.
2) Open the command prompt from that folder
3) javac InitPrompt.java
4) java InitPrompt

Once the program is executed, davisbase_tables and davisbase_columns tables will be created in the below mentioned folder structure: 
-	Data
	- catalog
	  - davisbase_tables
      - davisbase_columns
	- user_data
	  - tablename1
	  - tablename2
	  
All the tables created by the user will be inside the user_data folder.
