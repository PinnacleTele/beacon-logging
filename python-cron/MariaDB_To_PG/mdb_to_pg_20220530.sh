#!/bin/bash
cd /home/teamwork/scripts/python/MariaDB_To_PG
python3 mariadb_to_pg_stage_daily_20220530.py ./cfg/billing_mariadb_to_pg_stage_20220530.cfg > ./exec_out/mariadb_to_pg_20220530.out 2>&1




