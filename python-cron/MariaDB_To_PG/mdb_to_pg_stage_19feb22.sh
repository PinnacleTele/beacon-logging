#!/bin/bash
python3 pg_stage_to_sub_del_log_daily.py ./cfg/pg_stage_to_billing_19feb22.cfg > ./exec_out/pg_stage_to_billing_19feb22.out 2>&1
./daily_eod_stage_20feb22.sh
