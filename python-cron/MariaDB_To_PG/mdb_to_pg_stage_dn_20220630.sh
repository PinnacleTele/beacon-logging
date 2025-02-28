#!/bin/bash
cd /home/teamwork/scripts/python/MariaDB_To_PG
python3 mariadb_to_pg_stage_daily_20220630.py ./cfg/billing_mariadb_to_pg_stage_20220630.cfg  > ./exec_out/billing_mariadb_to_pg_stage_30Jun2022.out  2>&1
