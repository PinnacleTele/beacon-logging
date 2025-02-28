#!/bin/bash
python3 pg_stage_to_sub_del_log_daily.py ./cfg/pg_stage_to_billing_18feb22.cfg > ./exec_out/pg_stage_to_billing_18feb22.out 2>&1
python3 pg_ui_summary_report_daily.py ./cfg/pg_ui_summary_report_18feb22.cfg  > ./exec_out/pg_ui_summary_daily_18feb22.out 2>&1
python3 pg_summary_report_daily.py ./cfg/pg_daily_summary_report_18feb22.cfg  > ./exec_out/pg_summary_daily_18feb22.out 2>&1
