import sys
import psycopg2
import pandas as pd

from datetime import datetime, timedelta

import configobj
import logging
import os
import http.client
import json
import base64

def send_rpt_email(cfg_val, rpt_dt, xls_fn):
    rpt_dt_str = rpt_dt.strftime("%d/%m/%Y")
    email_subject = "Daily Traffic Report for the date: " + rpt_dt_str
    html_msg = "<html><body>Daily Traffic Report for the date: " + rpt_dt_str + "</body></html>"
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

    xls_f = open(xls_fn, 'rb')
    xls_encode = base64.b64encode(xls_f.read()).decode('UTF-8')
    xls_f.close()
    attach_fn = os.path.basename(xls_fn)
    conn = http.client.HTTPSConnection("rapidemail.rmlconnect.net")
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
            "attachments": [
                {
                    "type": 'application/octet-stream',
                    "name": attach_fn,
                    "content": xls_encode
                }
            ],
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

def create_excel(cfg_val, rpt_dt, pg_conn):

    rpt_dt_str = rpt_dt.strftime("%Y-%m-%d")
    sql = """select cli_id::varchar(20) as cli_id, username, company, recv_hour, carrier_name,
total_received, total_submitted, delivery_count, failed_count, platform_reject, 
nulldn, delivery_pct
from summary.hourly_traffic_report
where recv_date = %s
and substr(cli_id::varchar(20),1,1) not in  ('1', '2', '5', '6', '7')"""

    pg_cursor = pg_conn.cursor()
    pg_cursor.execute(sql, (rpt_dt_str,))
    rlst = pg_cursor.fetchall()
    pg_cursor.close()

    if len(rlst) == 0:
        logging.info("No data found for the date: " + rpt_dt_str)
        return None

    cols = [c[0] for c in pg_cursor.description]
    df_data = pd.DataFrame.from_records(rlst, columns=cols)
    xls_ren_cols = {'recv_hour': 'Hour (Submit)',
                    'cli_id': 'Client Id',
                    'username': 'Username',
                    'company': 'Customer Name',
                    'carrier_name': 'Operator',
                    'total_received': 'Total Received Count',
                    'total_submitted': 'Submitted Count',
                    'delivery_count': 'Delivery Count',
                    'failed_count': 'Delivery Failed Count',
                    'platform_reject': 'Platform Reject Count',
                    'nulldn': 'Null DN Count',
                    'delivery_pct': 'Delivery %'
                    }
    df_data = df_data.rename(columns=xls_ren_cols)
    df_data['Date'] = rpt_dt.strftime("%d/%m/%Y")
    df_data['Month'] = rpt_dt.strftime("%B")

    xls_cols = ['Date', 'Month', 'Client Id', 'Username', 'Customer Name', 'Hour (Submit)', 'Operator',
                'Total Received Count', 'Submitted Count', 'Delivery Count', 'Delivery Failed Count',
                'Platform Reject Count', 'Null DN Count', 'Delivery %']
    xls_fn = "./xls_daily/Daily_Traffic_" + rpt_dt.strftime("%d_%b_%Y") + ".xlsx"
    df_data[xls_cols].to_excel(xls_fn, index=False)
    return xls_fn

def main():
    cfg_fn = sys.argv[1]
    cfg_val = configobj.ConfigObj(cfg_fn)
    log_fn_dtsfx = datetime.now().strftime("%Y%m%d_%H%M%S")

    log_fn = "./log/daily_summary_email_" + log_fn_dtsfx + ".log"
    logging.basicConfig(filename=log_fn, level=logging.INFO,
                        format='%(asctime)s %(levelname)-8s %(funcName)-20s %(message)s')
    logging.info("Daily Traffic Email started")

    pg_host = cfg_val['PG_HOST']
    pg_port = cfg_val['PG_PORT']
    pg_db = cfg_val['PG_DB']
    pg_user = cfg_val['PG_USER']
    pg_pass = cfg_val['PG_PASS']

    pg_con_tmplt = "host='{host}' port='{port}' dbname='{db}' user='{user}' password='{password}'"
    pg_con_str = pg_con_tmplt.format(host=pg_host, port=pg_port, db=pg_db, user=pg_user, password=pg_pass)
    logging.info("Connecting to Postgres DB: " + pg_host + "@" + pg_db)
    pg_conn = psycopg2.connect(pg_con_str)
    dt_cur = datetime.now()
    rpt_dt = dt_cur - timedelta(days=1)

    xls_fn = create_excel(cfg_val, rpt_dt, pg_conn)
    if xls_fn is not None:
        logging.info("Excel File: " + xls_fn)
        send_rpt_email(cfg_val, rpt_dt, xls_fn)
    logging.info("Daily Traffic Email completed")
    logging.shutdown()

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("Config file is missing")
        print("Usage : " + sys.argv[0] + " <CONFIG file>")
        sys.exit(1)
    main()
