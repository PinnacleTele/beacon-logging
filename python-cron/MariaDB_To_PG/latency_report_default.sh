#!/bin/bash
python3 latency_report_daily.py ./cfg/pg_latency_report_default.cfg > ./exec_out/pg_latency.out 2>&1
python3 unitia_latency_report_daily.py ./cfg/unitia_latency_report_default.cfg > ./exec_out/unitia_latency.out 2>&1
