#!/bin/bash
cd /home/teamwork/scripts/python/MariaDB_To_PG
python3 mariadb_to_pg_stage_daily.py ./cfg/billing_mariadb_to_pg_stage_25May2022.cfg  > ./exec_out/billing_mariadb_to_pg_stage_25May2022.out  2>&1
if [ $? -eq 0 ]
then 
   python3 pg_stage_to_sub_del_log_daily.py ./cfg/pg_stage_to_billing_25May2022.cfg > ./exec_out/pg_stage_to_billing_25May2022.out 2>&1
fi 
