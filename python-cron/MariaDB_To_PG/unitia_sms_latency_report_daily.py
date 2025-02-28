import sys
import mysql.connector
import pandas as pd
from datetime import datetime, timedelta
from dateutil.rrule import *

import configobj
import logging
import os
import http.client
import json
import base64

def send_rpt_email(cfg_val, rpt_dt, xls_fn_lst):
    rpt_dt_str = rpt_dt.strftime("%d/%m/%Y")
    email_subject = "Unitia Latency Reports for the date: " + rpt_dt_str
    html_msg = "<html><body>Unitia Latency Reports for the date: " + rpt_dt_str + "</body></html>"
    cfg_email_to = cfg_val["EMAIL_TO"]
    email_to_lst = []
    for em_d in cfg_email_to:
        e_name, e_id = em_d.split("|")
        d_email = {
                    "email": e_id,
                    "name": e_name,
                    "type": "to"
                    }
        email_to_lst.append(d_email)

    if "EMAIL_CC" in cfg_val.keys():
        cfg_email_cc = cfg_val["EMAIL_CC"]
        for em_d in cfg_email_cc:
            e_name, e_id = em_d.split("|")
            d_email = {
                "email": e_id,
                "name": e_name,
                "type": "cc"
            }
            email_to_lst.append(d_email)

    attach_lst = []

    for xls_fn in xls_fn_lst:
        xls_f = open(xls_fn, 'rb')
        xls_encode = base64.b64encode(xls_f.read()).decode('UTF-8')
        xls_f.close()
        attach_fn = os.path.basename(xls_fn)
        attach_lst.append(
        {
            "type": 'application/octet-stream',
            "name": attach_fn,
            "content": xls_encode
        })

    #conn = http.client.HTTPSConnection("rapidemail.rmlconnect.net")
    conn = http.client.HTTPSConnection("api.unifiedrml.com")
    payload = json.dumps({
        "owner_id": "99141915",
        "token": "RaDzCbIP63Ctexeco7XqBXXo",
        "smtp_user_name": "smtp79522111",
        "message": {
            "html": html_msg,
            "subject": email_subject,
            "from_email": "alerts@winnovature.com",
            "from_name": "Alerts",
            "to": email_to_lst,
            "headers": {
                "Reply-To": "alerts@winnovature.com",
                "X-Unique-Id": "fastify.nanoid()"
            },
            "attachments": attach_lst,
            "images": []
        }
    })
    headers = {
        'Content-Type': 'application/json'
    }
    logging.info("Sending Report in email")
    conn.request("POST", "/v1.0/messages/sendMail", payload, headers)
    res = conn.getresponse()
    data = res.read()
    logging.info(data.decode("utf-8"))

def gen_lat_rpt(cfg_val, rpt_dt, my_conn):
    my_cursor = my_conn.cursor()
    xls_fn_lst = []
    tbl_name = "billing.reportlog_delivery"

    dt_prv_str = (datetime.now() - timedelta(days=1)).strftime("%Y-%m-%d")
    dt_prv = datetime.strptime(dt_prv_str, "%Y-%m-%d")

    if rpt_dt < dt_prv:
        tbl_sfx = rpt_dt.strftime("%b%y").lower()
        tbl_name = "billing_backup.reportlog_delivery_" + tbl_sfx


    lat_sql_count = """select
	date_format(rtime, '%d/%m/%Y') as recv_date,
	smscid,
	sum(credit) total_count,
	SUM(case when sms_latency_slap_org <= 1 then credit else 0 end) as LTE_1SECOND,
	SUM(case when sms_latency_slap_org > 1 and sms_latency_slap_org <= 2 then credit else 0 end) as LTE_2SECOND,
	SUM(case when sms_latency_slap_org >2 and sms_latency_slap_org <= 3 then credit else 0 end) as LTE_3SECOND,
	SUM(case when sms_latency_slap_org >3 and sms_latency_slap_org <= 4 then credit else 0 end) as LTE_4SECOND,
	SUM(case when sms_latency_slap_org >4 and sms_latency_slap_org <= 5 then credit else 0 end) as LTE_5SECOND,
	SUM(case when sms_latency_slap_org >5 and sms_latency_slap_org <= 10 then credit else 0 end) as LTE_10SECOND,
	SUM(case when sms_latency_slap_org >10 and sms_latency_slap_org <= 20 then credit else 0 end) as LTE_20SECOND,
	SUM(case when sms_latency_slap_org >20 and sms_latency_slap_org <= 30 then credit else 0 end) as LTE_30SECOND,
	SUM(case when sms_latency_slap_org >30 and sms_latency_slap_org <= 40 then credit else 0 end) as LTE_40SECOND,
	SUM(case when sms_latency_slap_org >40 and sms_latency_slap_org <= 60 then credit else 0 end) as LTE_1MINUTE,
	SUM(case when sms_latency_slap_org >60 and sms_latency_slap_org <= 120 then credit else 0 end) as LTE_2MINUTE,
	SUM(case when sms_latency_slap_org >120 and sms_latency_slap_org <= 180 then credit else 0 end) as LTE_3MINUTE,
	SUM(case when sms_latency_slap_org >180 and sms_latency_slap_org <= 240 then credit else 0 end) as LTE_4MINUTE,
	SUM(case when sms_latency_slap_org >240 and sms_latency_slap_org <= 300 then credit else 0 end) as LTE_5MINUTE,
	SUM(case when sms_latency_slap_org >300 and sms_latency_slap_org <= 600 then credit else 0 end) as LTE_10MINUTE,
	SUM(case when sms_latency_slap_org >600 and sms_latency_slap_org <= 900 then credit else 0 end) as LTE_15MINUTE,
	SUM(case when sms_latency_slap_org >900 and sms_latency_slap_org <= 1200 then credit else 0 end) as LTE_20MINUTE,
	SUM(case when sms_latency_slap_org >1200 and sms_latency_slap_org <= 2400 then credit else 0 end) as LTE_40MINUTE,
	SUM(case when sms_latency_slap_org > 2400 then credit else 0 end) as GT_40MINUTE
from {}
where rtime >= '{}'
    and rtime < '{}'
	and (statusid = '000' or statusid > '200')
group by date_format(rtime, '%d/%m/%Y'), smscid"""

    rpt_dt_str = rpt_dt.strftime("%Y-%m-%d")
    rpt_dt2_str = (rpt_dt + timedelta(days=1)).strftime("%Y-%m-%d")
    logging.info("Table Name: " + tbl_name)
    logging.info("Date From: {}, Date To: {}".format(rpt_dt_str, rpt_dt2_str))
    sql = lat_sql_count.format(tbl_name, rpt_dt_str, rpt_dt2_str)

    my_cursor.execute(sql)
    rlst = my_cursor.fetchall()

    if len(rlst) == 0:
        my_cursor.close()
        logging.info("No data found for the Date : " + rpt_dt_str)
        return

    lat_cols = [c[0] for c in my_cursor.description]
    my_cursor.close()
    df_lat = pd.DataFrame.from_records(rlst, columns=lat_cols)
    count_cols = ['LTE_1SECOND', 'LTE_2SECOND', 'LTE_3SECOND', 'LTE_4SECOND', 'LTE_5SECOND', 'LTE_10SECOND',
                  'LTE_20SECOND', 'LTE_30SECOND', 'LTE_40SECOND', 'LTE_1MINUTE', 'LTE_2MINUTE', 'LTE_3MINUTE',
                  'LTE_4MINUTE', 'LTE_5MINUTE', 'LTE_10MINUTE', 'LTE_15MINUTE', 'LTE_20MINUTE', 'LTE_40MINUTE',
                  'GT_40MINUTE']
    for cn in count_cols:
        df_lat[cn] = df_lat[cn].astype(int)

    xls_fn = "./xls_daily/Unitia_Latency_Count_Report_" + rpt_dt.strftime("%d_%b_%Y") + ".xlsx"
    df_lat.to_excel(xls_fn, index=False, sheet_name=rpt_dt.strftime("%d%b%Y"))
    xls_fn_lst.append(xls_fn)

    df_lat_pct = df_lat.copy()

    for cn in count_cols:
        pct_cn = cn + '_PCT'
        df_lat_pct[pct_cn] = (df_lat_pct[cn] / df_lat_pct['total_count']) * 100
        df_lat_pct[pct_cn] = df_lat_pct[pct_cn].astype(float).round(2)
    count_cols.append('total_count')
    df_lat_pct = df_lat_pct.drop(columns=count_cols)
    xls_fn = "./xls_daily/Unitia_Latency_Percentage_Report_" + rpt_dt.strftime("%d_%b_%Y") + ".xlsx"
    df_lat_pct.to_excel(xls_fn, index=False, sheet_name=rpt_dt.strftime("%d%b%Y"))
    xls_fn_lst.append(xls_fn)
    send_rpt_email(cfg_val, rpt_dt, xls_fn_lst)

def main():
    cfg_fn = sys.argv[1]
    cfg_val = configobj.ConfigObj(cfg_fn)
    log_fn_dtsfx = datetime.now().strftime("%Y%m%d_%H%M%S")

    log_fn = "./log/unitia_latency_report_email_" + log_fn_dtsfx + ".log"
    logging.basicConfig(filename=log_fn, level=logging.INFO,
                        format='%(asctime)s %(levelname)-8s %(funcName)-20s %(message)s')
    logging.info("Unitia Latency Report Email started")

    mysql_host = cfg_val['MYSQL_HOST']
    mysql_port = int(cfg_val['MYSQL_PORT'])
    mysql_db = cfg_val['MYSQL_DB']
    mysql_user = cfg_val['MYSQL_USER']
    mysql_pass = cfg_val['MYSQL_PASS']

    logging.info("Connecting to MySQL DB: " + mysql_host + "@" + mysql_db)
    my_conn = mysql.connector.connect(host=mysql_host,
                                      port=mysql_port,
                                      database=mysql_db,
                                      user=mysql_user,
                                      password=mysql_pass)

    cfg_date_from = cfg_val['RPT_DATE_FROM'].strip().upper()
    logging.info("Config Date From: " + cfg_date_from)
    if cfg_date_from == "DEFAULT":
        date_from = (datetime.now() - timedelta(days=1))
        date_to = date_from
    else:
        date_from = datetime.strptime(cfg_date_from, "%Y-%m-%d")
        date_to = datetime.strptime(cfg_val['RPT_DATE_TO'].strip(), "%Y-%m-%d")

    logging.info("Date From: " + date_from.strftime("%Y-%m-%d") + ", To: " + date_to.strftime("%Y-%m-%d"))
    if date_from == date_to:
        gen_lat_rpt(cfg_val, date_from, my_conn)
    else:
        dt_lst = list(rrule(DAILY, interval=1, dtstart=date_from, until=date_to))
        for dt in dt_lst:
            gen_lat_rpt(cfg_val, dt, my_conn)
    logging.info("Unitia Latency Report Email completed")

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("Config file is missing")
        print("Usage : " + sys.argv[0] + " <CONFIG file>")
        sys.exit(1)
    main()
