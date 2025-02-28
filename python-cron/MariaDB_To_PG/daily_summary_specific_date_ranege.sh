#!/bin/bash
python3 pg_ui_summary_report_daily.py ./cfg/pg_ui_summary_report.cfg  > ./exec_out/pg_ui_summary_daily_feb24.out 2>&1
python3 pg_summary_report_daily.py ./cfg/pg_daily_summary_report.cfg  > ./exec_out/pg_summary_daily_feb24.out 2>&1
python3 latency_report_daily.py ./cfg/pg_latency_report.cfg > ./exec_out/pg_latency_feb24.out 2>&1
