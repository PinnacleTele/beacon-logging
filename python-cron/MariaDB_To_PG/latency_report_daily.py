import sys
import psycopg2
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
    email_subject = "Latency Reports for the date: " + rpt_dt_str
    html_msg = "<html><body>Latency Reports for the date: " + rpt_dt_str + "</body></html>"
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

def gen_lat_rpt(cfg_val, rpt_dt, pg_conn):
    pg_cursor = pg_conn.cursor()
    xls_fn_lst = []
    lat_sql_count = """    select
    	to_char(recv_date,'dd/mm/yyyy') as recv_date,
    	del_carrier_sys_id,
    	count(*) total_count,
    	SUM(case when del_delv_lat_ori_in_millis <= 1000 then 1 else 0 end) as LTE_1_SECOND,
    	SUM(case when del_delv_lat_ori_in_millis >1000 and del_delv_lat_ori_in_millis <= 2000 then 1 else 0 end) as LTE_2_SECOND,
    	SUM(case when del_delv_lat_ori_in_millis >2000 and del_delv_lat_ori_in_millis <= 3000 then 1 else 0 end) as LTE_3_SECOND,
    	SUM(case when del_delv_lat_ori_in_millis >3000 and del_delv_lat_ori_in_millis <= 4000 then 1 else 0 end) as LTE_4_SECOND,
    	SUM(case when del_delv_lat_ori_in_millis >4000 and del_delv_lat_ori_in_millis <= 5000 then 1 else 0 end) as LTE_5_SECOND,
    	SUM(case when del_delv_lat_ori_in_millis >5000 and del_delv_lat_ori_in_millis <= 10000 then 1 else 0 end) as LTE_10_SECOND,
    	SUM(case when del_delv_lat_ori_in_millis >10000 and del_delv_lat_ori_in_millis <= 30000 then 1 else 0 end) as LTE_30_SECOND,
    	SUM(case when del_delv_lat_ori_in_millis >30000 and del_delv_lat_ori_in_millis <= 60000 then 1 else 0 end) as LTE_1_MINUTE,
    	SUM(case when del_delv_lat_ori_in_millis >60000 and del_delv_lat_ori_in_millis <= 120000 then 1 else 0 end) as LTE_2_MINUTE,
    	SUM(case when del_delv_lat_ori_in_millis >120000 and del_delv_lat_ori_in_millis <= 300000 then 1 else 0 end) as LTE_5_MINUTE,
    	SUM(case when del_delv_lat_ori_in_millis >300000 and del_delv_lat_ori_in_millis <= 600000 then 1 else 0 end) as LTE_10_MINUTE,
    	SUM(case when del_delv_lat_ori_in_millis > 600000 then 1 else 0 end) as GT_10_MINUTE
    from
    	smslog.sub_del_log_{}
    where
    	del_delv_lat_ori_in_millis is not null
    	and del_dn_ori_sts_code >= '600'
    	and del_dn_ori_sts_code != '642'
    	and del_car_sts_code not in ('-998', '-787')
    group by to_char(recv_date,'dd/mm/yyyy'), del_carrier_sys_id
    order by recv_date, del_carrier_sys_id"""

    rpt_dt_str = rpt_dt.strftime("%d/%m/%Y")
    tbl_dt_sfx = rpt_dt.strftime("%Y%m%d")
    sql = lat_sql_count.format(tbl_dt_sfx)
    logging.info("Table Date suffix: " + tbl_dt_sfx)
    logging.info("Executing SQL: " + sql)
    pg_cursor.execute(sql)
    rlst = pg_cursor.fetchall()
    pg_cursor.close()
    if len(rlst) == 0:
        pg_cursor.close()
        logging.info("No data found for the Date : " + rpt_dt_str)
        return

    lat_cols = [c[0] for c in pg_cursor.description]
    pg_cursor.close()
    df_lat = pd.DataFrame.from_records(rlst, columns=lat_cols)
    xls_fn = "./xls_daily/Latency_Count_Report_" + rpt_dt.strftime("%d_%b_%Y") + ".xlsx"
    df_lat.to_excel(xls_fn, index=False, sheet_name=rpt_dt.strftime("%d%b%Y"))
    xls_fn_lst.append(xls_fn)

    df_lat_pct = df_lat.copy()
    count_cols = lat_cols[2:]
    for cn in count_cols[1:]:
        pct_cn = cn + '_pct'
        df_lat_pct[pct_cn] = (df_lat_pct[cn] / df_lat_pct['total_count']) * 100
        df_lat_pct[pct_cn] = df_lat_pct[pct_cn].astype(float).round(2)

    df_lat_pct = df_lat_pct.drop(columns=count_cols)
    xls_fn = "./xls_daily/Latency_Percentage_Report_" + rpt_dt.strftime("%d_%b_%Y") + ".xlsx"
    df_lat_pct.to_excel(xls_fn, index=False, sheet_name=rpt_dt.strftime("%d%b%Y"))
    xls_fn_lst.append(xls_fn)
    send_rpt_email(cfg_val, rpt_dt, xls_fn_lst)

def main():
    cfg_fn = sys.argv[1]
    cfg_val = configobj.ConfigObj(cfg_fn)
    log_fn_dtsfx = datetime.now().strftime("%Y%m%d_%H%M%S")

    log_fn = "./log/latency_report_email_" + log_fn_dtsfx + ".log"
    logging.basicConfig(filename=log_fn, level=logging.INFO,
                        format='%(asctime)s %(levelname)-8s %(funcName)-20s %(message)s')
    logging.info("Latency Report Email started")

    pg_host = cfg_val['PG_HOST']
    pg_port = cfg_val['PG_PORT']
    pg_db = cfg_val['PG_DB']
    pg_user = cfg_val['PG_USER']
    pg_pass = cfg_val['PG_PASS']

    pg_con_tmplt = "host='{host}' port='{port}' dbname='{db}' user='{user}' password='{password}'"
    pg_con_str = pg_con_tmplt.format(host=pg_host, port=pg_port, db=pg_db, user=pg_user, password=pg_pass)
    logging.info("Connecting to Postgres DB: " + pg_host + "@" + pg_db)
    pg_conn = psycopg2.connect(pg_con_str)

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
        gen_lat_rpt(cfg_val, date_from, pg_conn)
    else:
        dt_lst = list(rrule(DAILY, interval=1, dtstart=date_from, until=date_to))
        for dt in dt_lst:
            gen_lat_rpt(cfg_val, dt, pg_conn)

    pg_conn.close()
    logging.info("Latency Report Email completed")

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("Config file is missing")
        print("Usage : " + sys.argv[0] + " <CONFIG file>")
        sys.exit(1)
    main()
