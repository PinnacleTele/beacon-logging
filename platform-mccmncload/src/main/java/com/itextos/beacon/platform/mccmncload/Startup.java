package com.itextos.beacon.platform.mccmncload;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
public class Startup {

    public static final Log log = LogFactory.getLog(Startup.class);

	public static void main(String[] args){
		
		String jsonString=getJsonSTringFromFile();

		log.debug("jsonString : "+jsonString);
		
		List<Map<String, Object>> datalist=null;
		try {
			datalist = convertJsonStringToListMap(jsonString);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error(e);
		}
		
		log.debug("bean : "+datalist);

		persisttoDB(datalist);
		
		log.debug("persisted : ");

	}

	 private static void persisttoDB(List<Map<String, Object>> datalist) {
		 
		String sql="insert into carrier_handover.mccmnclist(type,countryname,countrycode,mcc,mnc,brand,operator,status,bands,notes) values(?,?,?,?,?,?,?,?,?,?)";
		
		Connection connection=null;
		PreparedStatement statement=null;
		
		try {
			
			connection=	DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.CARRIER_HANDOVER.getKey()));
			connection.setAutoCommit(false);
			statement=connection.prepareStatement(sql);
			
			for(int i=0;i<datalist.size();i++) {
				
				Map<String, Object> data=datalist.get(i);
				
				statement.setObject(1, data.get("type"));
				statement.setObject(2, data.get("countryName")==null?"":data.get("countryName"));
				statement.setObject(3, data.get("countryCode"));
				statement.setObject(4, data.get("mcc"));
				statement.setObject(5, data.get("mnc"));
				statement.setObject(6, data.get("brand"));
				statement.setObject(7, data.get("operator")==null?"":data.get("operator"));
				statement.setObject(8, data.get("status"));
				statement.setObject(9, data.get("bands"));
				statement.setObject(10, data.get("notes"));

				statement.addBatch();
			}
			
			statement.executeBatch();
			
			connection.commit();
			
		}catch(Exception e) {
			log.error(e);

		}finally {
			
			
			if(statement!=null) {
				
				try {
					statement.close();
				}catch(Exception f) {
					
				}
			}

			if(connection!=null) {
				
				try {
					connection.close();
				}catch(Exception f) {
					
				}
			}
		}
		
		
	}

	private static List<Map<String, Object>> convertJsonStringToListMap(String jsonString) throws IOException {
	        ObjectMapper objectMapper = new ObjectMapper();
	        return objectMapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {});
	    }
	 
	private static String getJsonSTringFromFile() {
		Path path = Paths.get("/mcc-mnc-list.json");
        byte[] bytes;
		try {
			bytes = Files.readAllBytes(path);
	        return new String(bytes);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error(e);

		}
        return "";

	}

}
