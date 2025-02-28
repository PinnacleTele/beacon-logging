import sys
import mysql.connector
import psycopg2
import pandas as pd
from datetime import datetime, timedelta
import configobj
import logging
import http.client
import json
import io
import traceback

def gen_html(rpt_date, em_col_width, rpt_header, df_hr_rpt):
    html_str_io = io.StringIO()

    html_header = """<html>
    <head>
    <style>
        table {
			font-family: arial, sans-serif;
			border-collapse: collapse;
			width: 100%;
        }

        th {
			border: 1px solid #808080;
			background-color: #dddddd;
			padding: 8px;
        }
		td {
			border: 1px solid #808080;
			padding: 8px;
		}
    </style>
    </head>
    <body>"""
    html_str_io.write(html_header + "\n")
    html_str_io.write('<h2 style="text-align: center;font-family: arial, sans-serif;">{}</h2>'.format(rpt_header) + "\n")
    html_str_io.write("<table> <tr>" + "\n")

    for cn in df_hr_rpt.columns:
        html_str_io.write("<th " + em_col_width[cn] + ">" + cn + "</th>" + "\n")
    html_str_io.write("</tr>" + "\n")

    row_count = 0

    for rpt_row in df_hr_rpt.itertuples(index=False):
        row_count += 1
        # if (row_count % 2) == 0:
        #     html_str_io.write('<tr style="background-color: #b3ecff; border-width: 1px 0 0 1px;">\n')
        # else:
        #     html_str_io.write('<tr style="background-color: #ebebe0; border-width: 1px 0 0 1px;">\n')
        html_str_io.write('<tr>\n')
        rpt_col = 0
        if not rpt_date:
            # Hourly report, first column is recv_hour, center aligned
            html_str_io.write('<td style="text-align: center;">' + str(rpt_row[0]) + '</td>' + "\n")
            rpt_col += 1

        for _ in range(3):
            html_str_io.write('<td style="text-align: left;">' + str(rpt_row[rpt_col]) + '</td>' + "\n")
            rpt_col += 1

        for cval in rpt_row[rpt_col:-1]:
            html_str_io.write('<td style="text-align: right;">' + str(cval) + '</td>' + "\n")
        html_str_io.write('<td style="text-align: right;">' + "{:.2f}".format(rpt_row[-1]) + '</td>' + "\n")
        html_str_io.write('</tr>' + "\n")

    html_str_io.write("</table> </body> </html>" + "\n")
    return html_str_io.getvalue()

def send_rpt_email(cfg_val, rpt_date, em_col_width, email_subject, rpt_header, df_hr_rpt):
    html_msg = gen_html(rpt_date, em_col_width, rpt_header, df_hr_rpt)

    html_f = open('./html/hourly_summary_report.html', 'w')
    html_f.write(html_msg)
    html_f.close()

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
            "attachments": [],
            "images": []
        }
    })
    headers = {
        'Content-Type': 'application/json'
    }
    logging.info("Posting email data...")
    conn.request("POST", "/v1.0/messages/sendMail", payload, headers)
    res = conn.getresponse()
    data = res.read()
    logging.info(data.decode("utf-8"))

def main():
    cfg_fn = sys.argv[1]
    cfg_val = configobj.ConfigObj(cfg_fn)

    dt_now = datetime.now()
    db_dt = dt_now
    rpt_date = True
    dt_now_hr = dt_now.strftime("%H")
    rpt_date_hr_lst = cfg_val["DATE_RPT_HOURS"]
    if dt_now_hr not in rpt_date_hr_lst:
        return

    log_fn_dtsfx = datetime.now().strftime("%Y%m%d_%H%M%S")

    log_fn = "./log/Summary_Report_Hourly_" + log_fn_dtsfx + ".log"
    logging.basicConfig(filename=log_fn, level=logging.INFO,
                        format='%(asctime)s %(levelname)-8s %(funcName)-20s %(message)s')
    logging.info("Hourly Summary Report generation started")
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
    my_cursor = my_conn.cursor()
    usr_sql = """select cli_id, pu_id, su_id, user as username, company 
                from accounts.user_config
                where  left(cli_id, 1)  not in  ('1', '2', '5', '6', '7')"""
    my_cursor.execute(usr_sql)
    usr_rlst = my_cursor.fetchall()
    usr_cols = [c[0] for c in my_cursor.description]
    df_usr_db = pd.DataFrame.from_records(usr_rlst, columns=usr_cols)
    df_su_db = df_usr_db[df_usr_db['cli_id'] == df_usr_db['su_id']]\
                        [['su_id', 'username', 'company']].reset_index()

    df_su = pd.merge(df_su_db,
                     df_usr_db,
                     left_on='su_id',
                     right_on='su_id',
                     left_index=False,
                     right_index=False,
                     how='inner', suffixes=('_su', ''))\
                    [['cli_id', 'su_id', 'username_su', 'company_su']].\
                    reset_index(drop=True).\
                    rename(columns={'username_su': 'username', 'company_su': 'company'})

    db_sfx = ""
    tbl_sfx = ""

    db_month_suffix = cfg_val["DB_MONTH_SUFFIX"].lower()
    if db_month_suffix == "true":
        db_sfx = "_" + db_dt.strftime("%Y%m")
        tbl_sfx = "_" + db_dt.strftime("%Y%m%d")

    recv_tm = dt_now.strftime("%Y-%m-%d %H:00:00")
    sub_sql_tmplt = """select s.cli_id, s.sub_ori_sts_code, s.msg_type
                from billing{}.submission{} s
                where s.recv_time < '{}'
                and left(s.cli_id, 1)  not in  ('1', '2', '5', '6', '7')"""

    sub_sql = sub_sql_tmplt.format(db_sfx, tbl_sfx, recv_tm)
    logging.info("Executing Submission SQL: " + sub_sql)
    my_cursor.execute(sub_sql)
    sub_rlst = my_cursor.fetchall()
    sub_cols = [c[0] for c in my_cursor.description]
    df_sub_db = pd.DataFrame.from_records(sub_rlst, columns=sub_cols)

    del_sql_tmplt = """select d.cli_id, d.dn_ori_sts_code 
                from billing{}.deliveries{} d
                where d.recv_time < '{}'
                and left(d.cli_id, 1)  not in  ('1', '2', '5', '6', '7')"""
    del_sql = del_sql_tmplt.format(db_sfx, tbl_sfx, recv_tm)
    logging.info("Executing Deliveries SQL: " + del_sql)
    my_cursor.execute(del_sql)
    del_rlst = my_cursor.fetchall()
    del_cols = [c[0] for c in my_cursor.description]
    df_del_db = pd.DataFrame.from_records(del_rlst, columns=del_cols)

    my_cursor.close()
    my_conn.close()
    group_cols = ['cli_id']

    df_sub_db['submitted'] = df_sub_db['sub_ori_sts_code'].apply(lambda x: 1 if x == '400' else 0)
    df_sub_db['platform_reject'] = df_sub_db['submitted'].apply(lambda x: 1 if x == 0 else 0)
    df_sub_db['non_promo_sub'] = df_sub_db.apply(lambda r: 1 if (r['submitted'] == 1 and r['msg_type'] != 0) else 0,
                                                 axis=1)

    df_sub_total = df_sub_db.groupby(by=group_cols).agg({
        'sub_ori_sts_code': 'count',
        'submitted': 'sum',
        'platform_reject': 'sum',
        'non_promo_sub': 'sum'
    }).reset_index().rename(columns={'sub_ori_sts_code': 'total_received'})

    df_del_db['delivery_credit'] = df_del_db['dn_ori_sts_code'].apply(lambda x: 1 if x == '600' else 0)
    df_del_db['failed_credit'] = df_del_db['dn_ori_sts_code'].apply(lambda x: 1 if x > '600' else 0)

    df_del_total = df_del_db.groupby(by=group_cols).agg({
        'dn_ori_sts_code': 'count',
        'delivery_credit': 'sum',
        'failed_credit': 'sum'
    }).reset_index().rename(columns={'dn_ori_sts_code': 'total_delivery'})

    df_hr_rpt = pd.merge(df_sub_total,
                         df_del_total,
                         left_on='cli_id',
                         right_on='cli_id',
                         left_index=False,
                         right_index=False,
                         how='left',
                         suffixes=('_sub', '_del')).reset_index(drop=True)
    df_hr_rpt['total_delivery'] = df_hr_rpt['total_delivery'].fillna(0).astype(int)
    df_hr_rpt['delivery_credit'] = df_hr_rpt['delivery_credit'].fillna(0).astype(int)
    df_hr_rpt['failed_credit'] = df_hr_rpt['failed_credit'].fillna(0).astype(int)
    df_hr_rpt['nulldn'] = df_hr_rpt['non_promo_sub'] - (df_hr_rpt['delivery_credit'] + df_hr_rpt['failed_credit'])
    # df_hr_rpt['delivery_pct'] = round((df_hr_rpt['delivery_credit'] / df_hr_rpt['non_promo_sub']) * 100, 2)
    # df_hr_rpt['delivery_pct'] = df_hr_rpt['delivery_pct'].fillna(0)

    df_hr_rpt['delivery_pct'] = df_hr_rpt.apply(lambda r:
                                      (r['delivery_credit'] / r['non_promo_sub']) * 100
                                      if r['non_promo_sub'] > 0 else 0.0, axis=1)

    df_hr_rpt['delivery_pct'] = df_hr_rpt['delivery_pct'].fillna(0.0)
    df_hr_rpt['delivery_pct'] = df_hr_rpt['delivery_pct'].astype(float).round(2)

    df_hr_rpt_usr = pd.merge(df_hr_rpt,
                             df_su,
                             left_on='cli_id',
                             right_on='cli_id',
                             left_index=False,
                             right_index=False,
                             how='inner',
                             suffixes=('_rpt', '_usr')).reset_index(drop=True)

    # em_cols = ['recv_hour', 'cli_id', 'username', 'total_received', 'submitted', 'delivery_credit',
    #            'failed_credit', 'platform_reject', 'nulldn', 'delivery_pct']
    #em_cols = em_cols[1:]
    #group_cols = ['cli_id', 'username']

    group_cols = ['su_id', 'company', 'username']
    df_rpt = df_hr_rpt_usr.groupby(by=group_cols).agg({
        'total_received': 'sum',
        'submitted': 'sum',
        'delivery_credit': 'sum',
        'failed_credit': 'sum',
        'platform_reject': 'sum',
        'non_promo_sub': 'sum',
        'nulldn': 'sum',
        'delivery_pct': 'sum'
    }).reset_index().rename(columns={'su_id': 'cli_id', 'username_su': 'username'})
    #df_rpt['delivery_pct'] = round((df_rpt['delivery_credit'] / df_rpt['non_promo_sub']) * 100, 2)
    df_rpt['delivery_pct'] = df_rpt.apply(lambda r:
                                      (r['delivery_credit'] / r['non_promo_sub']) * 100
                                      if r['non_promo_sub'] > 0 else 0.0, axis=1)
    df_rpt['delivery_pct'] = df_rpt['delivery_pct'].fillna(0.0)
    df_rpt['delivery_pct'] = df_rpt['delivery_pct'].astype(float).round(2)
    ren_cols = {'cli_id': 'Client Id',
                    'username': 'Username',
                    'company': 'Customer Name',
                    'total_received': 'Total Received',
                    'submitted': 'Submitted',
                    'delivery_credit': 'Delivered',
                    'failed_credit': 'Failed',
                    'platform_reject': 'Platform Rejected',
                    'nulldn': 'Null DN',
                    'delivery_pct': 'Delivery %'
                }
    df_rpt = df_rpt.rename(columns=ren_cols)

    em_cols = ['Client Id', 'Customer Name', 'Username',
                'Total Received', 'Submitted', 'Delivered', 'Failed',
                'Platform Rejected', 'Null DN', 'Delivery %']
    em_col_width = {
        "Client Id": "valign ='top' width='15%'",
        "Customer Name": "valign ='top' width='20%'",
        "Username": "valign ='top' width='9%'",
        "Total Received": "valign ='top' width='8%'",
        "Submitted": "valign ='top' width='8%'",
        "Delivered": "valign ='top' width='8%'",
        "Failed": "valign ='top' width='8%'",
        "Platform Rejected": "valign ='top' width='8%'",
        "Null DN": "valign ='top' width='8%'",
        "Delivery %": "valign ='top' width='8%'"
    }
    rpt_dt = (db_dt - timedelta(hours=1))
    rpt_dt_str = rpt_dt.strftime("%Y-%m-%d")
    rpt_hr_str = rpt_dt.strftime("%H:59:59")
    rpt_header = "Summary Report on {} up to {}".format(rpt_dt_str, rpt_hr_str)
    sort_cols = ['Customer Name', 'Username']
    df_rpt = df_rpt.sort_values(by=sort_cols)[em_cols]

    csv_dir = cfg_val["CSV_FOLDER"]
    csv_fn = csv_dir + "/summary_report_" + rpt_dt.strftime("%Y%m%d_%H%M%S") + ".csv"
    df_rpt.to_csv(csv_fn, index=False, header=True)


    #email_subject = "Daily Summary Report: " + rpt_dt_str
    email_subject = rpt_header
    try:
        logging.info("Sending Report in email")
        send_rpt_email(cfg_val, rpt_date, em_col_width, email_subject, rpt_header, df_rpt)
    except Exception as e:
        logging.error(str(e))
        logging.error(traceback.format_exc())

    # else:
    #     rpt_hr = dt_now - timedelta(hours=1)
    #     rpt_dt_str = rpt_hr.strftime("%Y-%m-%d %H:00:00")
    #     rpt_header = "Summary Report for the Hour: {}".format(rpt_dt_str)
    #     rpt_hr_str = rpt_hr.strftime("%H")
    #     df_rpt = df_hr_rpt_usr[df_hr_rpt_usr['recv_hour'] == rpt_hr_str][em_cols]
    #     email_subject = "Hourly Summary Report: " + rpt_dt_str

    logging.info("Hourly Summary Report generation completed")
    logging.shutdown()

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("Config file is missing")
        print("Usage: " + sys.argv[0] + " <CONFIG File>")
        sys.exit(1)
    main()
