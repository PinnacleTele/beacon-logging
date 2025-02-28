#!/bin/bash
python3 pg_ui_summary_report_daily.py ./cfg/pg_ui_summary_report_03mar22.cfg  > ./exec_out/pg_ui_summary_daily_03mar22.out 2>&1
python3 pg_summary_report_daily.py ./cfg/pg_daily_summary_report_03mar22.cfg  > ./exec_out/pg_summary_daily_03mar22.out 2>&1
