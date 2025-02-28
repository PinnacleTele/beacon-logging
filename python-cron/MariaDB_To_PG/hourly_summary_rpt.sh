#!/bin/bash
cd /home/teamwork/scripts/python/MariaDB_To_PG
python3 summary_report_hourly.py ./cfg/hourly_summary_report.cfg > ./exec_out/hourly_summary.out 2>&1
