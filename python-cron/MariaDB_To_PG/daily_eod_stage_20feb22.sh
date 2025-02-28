#!/bin/bash
cd /home/teamwork/scripts/python/MariaDB_To_PG
     python3 pg_ui_summary_report_daily.py ./cfg/pg_ui_summary_report_default.cfg  > ./exec_out/pg_ui_summary_daily.out 2>&1
     python3 pg_summary_report_daily.py ./cfg/pg_daily_summary_report_default.cfg  > ./exec_out/pg_summary_daily.out 2>&1
     python3 daily_summary_email.py ./cfg/daily_summary_email.cfg  > ./exec_out/daily_summary_email.out 2>&1
     python3 latency_report_daily.py ./cfg/pg_latency_report_default.cfg > ./exec_out/pg_latency.out 2>&1
     python3 unitia_latency_report_daily.py ./cfg/unitia_latency_report_default.cfg > ./exec_out/unitia_latency.out 2>&1
