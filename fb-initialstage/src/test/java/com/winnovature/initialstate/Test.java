package com.winnovature.initialstate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Test {

	public static void main(String[] args) throws Exception {
		//new Test().loadData();
		
		Map<String, List<Map<String, String>>> requestsList = new HashMap<String, List<Map<String, String>>>();
		
		List<Map<String, String>> data1 = new ArrayList<Map<String, String>>();
		Map<String, String> map1 = new HashMap<String, String>();
		map1.put("cm1", "camp1");
		map1.put("file", "cm1_file1");
		Map<String, String> map2 = new HashMap<String, String>();
		map2.put("cm1", "camp1");
		map2.put("file", "cm1_file2");
		data1.add(map1);
		data1.add(map2);
		requestsList.put("c1~cm1", data1);
		
		List<Map<String, String>> data2 = new ArrayList<Map<String, String>>();
		Map<String, String> map3 = new HashMap<String, String>();
		map3.put("cm2", "camp2");
		map3.put("file", "cm2_file1");
		Map<String, String> map4 = new HashMap<String, String>();
		map4.put("cm2", "camp2");
		map4.put("file", "cm2_file2");
		data2.add(map3);
		data2.add(map4);
		requestsList.put("c2~cm2", data2);
		
		List<Map<String, String>> data3 = new ArrayList<Map<String, String>>();
		Map<String, String> map5 = new HashMap<String, String>();
		map5.put("cm3", "camp3");
		map5.put("file", "cm3_file1");
		data3.add(map5);
		requestsList.put("c1~cm3", data3);
		
		System.out.println(requestsList);
		
		while(requestsList.size() > 0) {
			Iterator<String> it = requestsList.keySet().iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				List<Map<String, String>> data = requestsList.get(key);
				if (data.size() > 0) {
					Map<String, String> dt = data.remove(0);
					System.out.println(dt);
				} else {
					it.remove();
				}
			}
		}
		
		  
		
		System.out.println(requestsList);
		
	}
	
	public void loadData() throws Exception{
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = ConnectionFactory.getConnection();
			pstmt = con.prepareStatement("select * from custdb.txn_msg_patterns order by sno desc");
			rs = pstmt.executeQuery();
			while(rs.next()) {
				System.out.println("row -----> "+rs.getString("sno")+"  "+rs.getString("pattern"));
			}
		}catch(Exception e) {
			System.err.println(e);
		}finally {
			closeConnection(rs, pstmt, con);
		}
		
	}
	
	public void closeConnection(ResultSet rs, PreparedStatement ps,
			Connection con) throws SQLException {
		if (rs != null) {
			rs.close();
		}
		if (ps != null) {
			ps.close();
		}
		if (con != null) {
			con.close();
		}
	}

}
