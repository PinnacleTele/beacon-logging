import sys
import psycopg2
import pandas as pd

from datetime import datetime
from dateutil.relativedelta import relativedelta

import configobj
import logging
import os
import http.client
import json
import base64

def send_rpt_email(cfg_val, rpt_dt, xls_fn_lst):
    rpt_dt_str = rpt_dt.strftime("%B-%Y")
    email_subject = "Traffic Reports for the month: " + rpt_dt_str
    html_msg = "<html><body>Traffic Reports for the month: " + rpt_dt_str + "</body></html>"
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

def create_excel(cfg_val, rpt_dt, pg_conn):

    rpt_month = rpt_dt.strftime("%m")
    rpt_year = rpt_dt.strftime("%Y")
    sql = """select to_char(t.recv_date,'dd/mm/yyyy') as "Date", to_char(t.recv_date,'Month') as "Month", 
    t.recv_date, t.cli_id::varchar(20) as cli_id, t.username, t.company, t.carrier_name,
t.total_received, t.total_submitted, t.delivery_count, t.failed_count, t.platform_reject, 
t.nulldn, t.delivery_pct, t.non_promo_sub_count
from config.calendar_info ci
inner join summary.daily_traffic_report t on t.recv_date = ci.date_value 
where ci.month = %s and ci.year = %s
and substr(t.cli_id::varchar(20),1,1) not in  ('1', '2', '5', '6', '7')
order by t.recv_date, t.cli_id"""

    pg_cursor = pg_conn.cursor()
    pg_cursor.execute(sql, (rpt_month, rpt_year))
    rlst = pg_cursor.fetchall()


    if len(rlst) == 0:
        logging.info("No data found for the month: " + rpt_year + "-" + rpt_month)
        return None, None

    cols = [c[0] for c in pg_cursor.description]
    pg_cursor.close()
    df_oper = pd.DataFrame.from_records(rlst, columns=cols)
    xls_ren_cols = {'cli_id': 'Client Id',
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
    df_oper = df_oper.rename(columns=xls_ren_cols)
    df_oper['Delivery %'] = df_oper['Delivery %'].astype(float).round(2)

    oper_xls_cols = ['Date', 'Month', 'Client Id', 'Username', 'Customer Name', 'Operator',
                'Total Received Count', 'Submitted Count', 'Delivery Count', 'Delivery Failed Count',
                'Platform Reject Count', 'Null DN Count', 'Delivery %']
    oper_xls_fn = "./xls_monthly/Monthly_Operator_Traffic_" + rpt_dt.strftime("%b_%Y") + ".xlsx"
    df_oper[oper_xls_cols].to_excel(oper_xls_fn, index=False, sheet_name=rpt_dt.strftime("%b%Y"))

    user_group_cols = ['Date', 'Month', 'Client Id', 'Username', 'Customer Name']
    user_xls_cols = ['Date', 'Month', 'Client Id', 'Username', 'Customer Name',
               'Total Received Count', 'Submitted Count', 'Delivery Count', 'Delivery Failed Count',
               'Platform Reject Count', 'Null DN Count', 'Delivery %']
    df_user = df_oper.groupby(by=user_group_cols).agg({
        'Total Received Count': 'sum',
        'Submitted Count': 'sum',
        'Delivery Count': 'sum',
        'Delivery Failed Count': 'sum',
        'Platform Reject Count': 'sum',
        'Null DN Count': 'sum',
        'non_promo_sub_count': 'sum'}).reset_index()
    df_user['Delivery %'] = df_user.apply(lambda r:
                              (r['Delivery Count'] / r['non_promo_sub_count']) * 100
                              if r['non_promo_sub_count'] > 0 else 0.0, axis=1)
    df_user['Delivery %'] = df_user['Delivery %'].fillna(0.0)
    df_user['Delivery %'] = df_user['Delivery %'].astype(float).round(2)

    user_xls_fn = "./xls_monthly/Monthly_User_Traffic_" + rpt_dt.strftime("%b_%Y") + ".xlsx"
    df_user[user_xls_cols].to_excel(user_xls_fn, index=False, sheet_name=rpt_dt.strftime("%b%Y"))

    return oper_xls_fn, user_xls_fn

def main():
    cfg_fn = sys.argv[1]
    cfg_val = configobj.ConfigObj(cfg_fn)
    log_fn_dtsfx = datetime.now().strftime("%Y%m%d_%H%M%S")

    log_fn = "./log/monthly_summary_email_" + log_fn_dtsfx + ".log"
    logging.basicConfig(filename=log_fn, level=logging.INFO,
                        format='%(asctime)s %(levelname)-8s %(funcName)-20s %(message)s')
    logging.info("Monthly Traffic Email started")

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
    dt_day = dt_cur.strftime("%d")
    if dt_day == "02":
        rpt_dt = dt_cur - relativedelta(months=1)
    else:
        rpt_dt = dt_cur - relativedelta(days=1)

    xls_fn = create_excel(cfg_val, rpt_dt, pg_conn)
    if xls_fn[0] is not None:
        logging.info("Operator Excel File: " + xls_fn[0])
        logging.info("User Excel File: " + xls_fn[0])
        send_rpt_email(cfg_val, rpt_dt, xls_fn)
    logging.info("Monthly Traffic Email completed")
    logging.shutdown()

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("Config file is missing")
        print("Usage : " + sys.argv[0] + " <CONFIG file>")
        sys.exit(1)
    main()
