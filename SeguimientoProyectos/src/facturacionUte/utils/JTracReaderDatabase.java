package facturacionUte.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JTracReaderDatabase {

	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException{

		Class.forName("org.hsqldb.jdbcDriver");
		//Class.forName("com.mysql.jdbc.Driver");

		List<String> tableNames = new ArrayList<String>();
		
		Connection conn1 = DriverManager.getConnection("jdbc:hsqldb:file:C:/DESA/backups/jtrac/jtrac", "SA", "");
		//Connection conn1 = DriverManager.getConnection("jdbc:hsqldb:file:X:/jtrac", "sa", "");
		//Connection conn2 = DriverManager.getConnection("jdbc:mysql://localhost/jtrac", "root", "");
		
		DatabaseMetaData md1 = conn1.getMetaData();
		
		ResultSet rs_0 = md1.getSchemas();
		while (rs_0.next()) {			
			String schemaName = rs_0.getString(1);
			System.out.println("schemaName: " + schemaName);
			ResultSet rs_00 = md1.getTables(null, schemaName, null, null);			
			while (rs_00.next()) {
			    tableNames.add(schemaName.concat(".").concat(rs_00.getString("TABLE_NAME")));
			}
		}
		Statement stmt1 = conn1.createStatement();
		//Statement stmt11 = conn2.createStatement();
		//stmt11.executeUpdate("delete from user_space_roles");
		//stmt11.executeUpdate("delete from users");
		
		FileOutputStream fout = new FileOutputStream(new java.io.File ("C:\\DESA\\salidaTablasJTrac.sql"));
		
		for (String tableName : tableNames) {

			fout.write("\n".getBytes());
		    fout.write("\n".getBytes());

			ResultSet rs = stmt1.executeQuery("select * from " + tableName);
		    ResultSetMetaData md = rs.getMetaData();
		    String cols = "";
		    for (int i = 1; i <= md.getColumnCount(); i++) {
		        cols = cols + md.getColumnName(i);
		        if (i != md.getColumnCount()) {
		            cols = cols + ", ";		            
		        }
		    }
		    List<String> vals = new ArrayList<String>();
		    while (rs.next()) {
		    	String valOfRow = "";
		    	for (int i = 1; i <= md.getColumnCount(); i++) {
		    		Object columnValue = rs.getObject(i);
		    		valOfRow = valOfRow + (columnValue== null? "null" : columnValue.toString());
		    		if (i != md.getColumnCount()) {
		    			valOfRow = valOfRow + ", ";
		    		}
		    	}
		    	String ins = "INSERT INTO " + tableName + " (" + cols + ") VALUES (" + valOfRow + ")";		    	
		    	fout.write(ins.getBytes());
		    	fout.write("\n".getBytes());
			    //System.out.println(ins);
		    	vals.add(valOfRow);  
		    }
		    //Types.DOUBLE
		    /*PreparedStatement stmt2 = conn2.prepareStatement(ins);
		    while (rs.next()) {
		        for (int i = 1; i <= md.getColumnCount(); i++) {
		            if (md.getColumnType(i) == Types.TIMESTAMP) {
		                stmt2.setTimestamp(i, rs.getTimestamp(i));
		            } else {
		                stmt2.setString(i, rs.getString(i));
		            }
		        }        
		        stmt2.execute();
		    } */   
		}
		
		fout.flush();
		fout.close();
		conn1.close();
		//conn2.close();
		
	}
	
	
}
