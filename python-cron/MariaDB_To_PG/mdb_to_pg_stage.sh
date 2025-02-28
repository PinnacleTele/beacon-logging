#!/bin/bash
cd /home/teamwork/scripts/python/MariaDB_To_PG
python3 mariadb_to_pg_stage_daily.py ./cfg/billing_mariadb_to_pg_stage.cfg > ./exec_out/mariadb_to_pg_stage.out 2>&1
#echo "daily eod stage"
if [ $? -eq 0 ]
then 
   python3 pg_stage_to_sub_del_log_daily.py ./cfg/pg_stage_to_billing.cfg > ./exec_out/pg_stage_to_billing.out 2>&1
   if [ $? -eq 0 ]
   then 
     python3 pg_ui_summary_report_daily.py ./cfg/pg_ui_summary_report.cfg  > ./exec_out/pg_ui_summary.out 2>&1
     python3 pg_summary_report_daily.py ./cfg/pg_daily_summary_report.cfg  > ./exec_out/pg_summary.out 2>&1
   fi
fi
