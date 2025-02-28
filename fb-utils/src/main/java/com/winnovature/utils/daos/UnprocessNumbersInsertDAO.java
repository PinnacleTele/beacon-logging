package com.winnovature.utils.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.utils.dtos.UnprocessRow;
import com.winnovature.utils.singletons.ConnectionFactoryForCMDB;
import com.winnovature.utils.utils.Constants;


public class UnprocessNumbersInsertDAO {

	static Log log = LogFactory.getLog(Constants.UtilsLogger);

	private static String className = " [ExcludeNumbersInsertDAO] ";
	
	public void insertUnProcessNumbers(int batchcount, List<UnprocessRow> lstUnprocessRow) throws Exception {
		String logName = className + " [insertExcludeNumbers]";

		if (log.isDebugEnabled())
			log.debug(logName + " batchcount:" + lstUnprocessRow.toString());

		Connection unprocessCon = null;
		PreparedStatement unprocessPstmt = null;

		try {
			String unprocessSql = "insert into unprocess_data (esmeaddr, file_id , mobile,reason,ctime) values (?,?,?,?,now())";

			int unprocessBatchCount = 0;
			// unprocessCon = ConnectionFactoryForLogDBofUtils.getInstance().getConnection();
			unprocessCon = ConnectionFactoryForCMDB.getInstance().getConnection();
			unprocessCon.setAutoCommit(false);
			unprocessPstmt = unprocessCon.prepareStatement(unprocessSql);

			for (int i = 0; i < lstUnprocessRow.size(); i++) {
				UnprocessRow unprocessRow = lstUnprocessRow.get(i);
				unprocessPstmt.setString(1, unprocessRow.getEsmeaddr());
				unprocessPstmt.setString(2, unprocessRow.getFileId());
				// trim the mobile column value to 100 chars(table column size) as it may have bigger strings in case of wrong delimiter
				if(unprocessRow.getMobile()!=null && unprocessRow.getMobile().length() > 100) {
					unprocessPstmt.setString(3, unprocessRow.getMobile().substring(0, 99));
				}else if(unprocessRow.getMobile()!=null) {
					unprocessPstmt.setString(3, unprocessRow.getMobile());
				}
				
				unprocessPstmt.setString(4, unprocessRow.getReason());
				unprocessPstmt.addBatch();
				unprocessBatchCount++;
				if (unprocessBatchCount >= batchcount) {
					unprocessPstmt.executeBatch();
					unprocessBatchCount = 0;
				}
			}
			if (unprocessBatchCount > 0) {
				unprocessPstmt.executeBatch();
				unprocessBatchCount = 0;
			}

			if (unprocessCon != null) {
				unprocessCon.commit();
			}

		} catch (Exception e) {
			log.error(logName + " Exception:", e);
			throw e;
		} finally {
			if (unprocessPstmt != null) {
				unprocessPstmt.close();
			}
			if (unprocessCon != null) {
				unprocessCon.close();
			}
		}

	}


}
