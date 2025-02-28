#!/bin/bash
cd /home/teamwork/scripts/python/MariaDB_To_PG
python3 mariadb_to_pg_sub_del_log_daily.py ./cfg/billing_mariadb_to_pg_default.cfg > ./exec_out/mariadb_to_pg_daily.out 2>&1
if [ $? -eq 0 ]
then
    python3 pg_ui_summary_report_daily.py ./cfg/pg_ui_summary_report_default.cfg  > ./exec_out/pg_ui_summary_daily.out 2>&1
fi
