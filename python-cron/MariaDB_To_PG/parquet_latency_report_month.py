import sys
import pandas as pd
from datetime import datetime, timedelta
from dateutil.rrule import *
from dateutil.relativedelta import *
import glob
import os

import configobj
import logging
import http.client
import json
import base64


def send_rpt_email(cfg_val, rpt_dt, xls_fn_lst):
    rpt_dt_str = rpt_dt.strftime("%b-%Y")
    email_subject = "Latency Reports for the month: " + rpt_dt_str
    html_msg = "<html><body>Latency Reports for the Month: " + rpt_dt_str + "</body></html>"
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


def gen_lat_rpt(cfg_val, parq_folder, date_from, date_to):
    dt_lst = list(rrule(DAILY, interval=1, dtstart=date_from, until=date_to))
    xls_fn_lst = []
    tbl_folder = 'smslog/sub_del_log'

    lat_mill_lst = [1000, 2000, 3000, 4000, 5000, 10000, 30000, 60000, 120000, 300000, 600000]
    lat_lbl_lst = ['lte_1_second', 'lte_2_second', 'lte_3_second', 'lte_4_second', 'lte_5_second', 'lte_10_second',
                   'lte_30_second', 'lte_1_minute', 'lte_2_minute', 'lte_5_minute', 'lte_10_minute']

    d_lat_agg = {cn: "sum" for cn in lat_lbl_lst}
    d_lat_agg["gt_10_minute"] = "sum"
    d_lat_agg["del_delv_lat_ori_in_millis"] = "count"

    rpt_cols = ['recv_month', 'del_carrier_sys_id', 'total_count']
    rpt_cols.extend(lat_lbl_lst)
    rpt_cols.append('gt_10_minute')

    pq_cols = ['del_carrier_sys_id', 'del_delv_lat_ori_in_millis', 'del_dn_ori_sts_code']

    df_lst = []
    for rpt_dt in dt_lst:
        rpt_dt_str = rpt_dt.strftime("%Y-%m-%d")
        logging.info("Processing Date: " + rpt_dt_str)
        rpt_dt_folder = rpt_dt.strftime("%Y/%m/%d")
        rpt_dt_pq_folder = parq_folder + "/" + rpt_dt_folder + "/" + tbl_folder
        dt_pq_fn_lst = glob.glob(rpt_dt_pq_folder + "/*.parquet")
        if len(dt_pq_fn_lst) == 0:
            logging.info("No Parquet Files found for the Date: " + rpt_dt_str)

        for pq_fn in dt_pq_fn_lst:
            df_pq = pd.read_parquet(pq_fn, columns=pq_cols)
            df_pq = df_pq[(df_pq['del_carrier_sys_id'].notnull()) &
                          (df_pq['del_carrier_sys_id'] != "") &
                          (df_pq["del_dn_ori_sts_code"] != "642") &
                          (df_pq["del_dn_ori_sts_code"] >= "600") &
                          (~df_pq["del_dn_ori_sts_code"].isin(['-998', '-787']))]
            df_pq['recv_month'] = rpt_dt.strftime("%m/%Y")
            df_pq = df_pq[['recv_month', 'del_carrier_sys_id', 'del_delv_lat_ori_in_millis']]

            p_lmil = 0
            for lidx, lmil in enumerate(lat_mill_lst):
                lat_lbl = lat_lbl_lst[lidx]
                if p_lmil == 0:
                    df_pq[lat_lbl] = (df_pq['del_delv_lat_ori_in_millis'] <= lmil).astype(int)
                else:
                    df_pq[lat_lbl] = ((df_pq['del_delv_lat_ori_in_millis'] > p_lmil) &
                                      (df_pq['del_delv_lat_ori_in_millis'] <= lmil)).astype(int)
                p_lmil = lmil

            df_pq['gt_10_minute'] = (df_pq['del_delv_lat_ori_in_millis'] > lat_mill_lst[-1]).astype(int)

            df_lat_rpt = df_pq.groupby(by=['recv_month', 'del_carrier_sys_id']).agg(d_lat_agg). \
                                reset_index().rename(columns={"del_delv_lat_ori_in_millis": "total_count"})

            df_lst.append(df_lat_rpt[rpt_cols])


    if len(df_lst) == 0:
        logging.info("No data found for the Month: " + date_from.strftime("%Y-%m"))
        return

    logging.info("Merging Parquet dataframes...")
    df_lat_all = pd.concat(df_lst, ignore_index=True)
    df_cols = list(df_lat_all.columns)
    count_cols = df_cols[2:]
    group_cols = ['recv_month', 'del_carrier_sys_id']
    d_agg = {}
    for cn in count_cols:
        d_agg[cn] = 'sum'

    logging.info("Generating Latency Count Report...")
    df_lat = df_lat_all.groupby(by=group_cols).agg(d_agg).reset_index()
    xls_fn = "./xls_monthly/Latency_Count_Report_" + date_from.strftime("%b_%Y") + ".xlsx"
    df_lat.to_excel(xls_fn, index=False, sheet_name=date_from.strftime("%b-%Y"))
    xls_fn_lst.append(xls_fn)

    logging.info("Generating Latency Percentage Report...")
    df_lat_pct = df_lat.copy()

    for cn in count_cols[1:]:
        pct_cn = cn + '_pct'
        df_lat_pct[pct_cn] = (df_lat_pct[cn] / df_lat_pct['total_count']) * 100
        df_lat_pct[pct_cn] = df_lat_pct[pct_cn].astype(float).round(2)

    df_lat_pct = df_lat_pct.drop(columns=count_cols)
    xls_fn = "./xls_monthly/Latency_Percentage_Report_" + date_from.strftime("%b_%Y") + ".xlsx"
    df_lat_pct.to_excel(xls_fn, index=False, sheet_name=date_from.strftime("%b-%Y"))
    xls_fn_lst.append(xls_fn)
    send_rpt_email(cfg_val, date_from, xls_fn_lst)


def main():
    cfg_fn = sys.argv[1]
    cfg_val = configobj.ConfigObj(cfg_fn)
    log_fn_dtsfx = datetime.now().strftime("%Y%m%d_%H%M%S")

    log_fn = "./log/parquet_latency_report_email_month_" + log_fn_dtsfx + ".log"
    logging.basicConfig(filename=log_fn, level=logging.INFO,
                        format='%(asctime)s %(levelname)-8s %(funcName)-20s %(message)s')
    logging.info("Parquet Latency Report Month Email started")

    cfg_rpt_month = cfg_val['RPT_MONTH'].strip().upper()
    logging.info("Config Report Month: " + cfg_rpt_month)
    date_from = datetime.strptime(cfg_rpt_month + "-01", "%Y-%m-%d")
    date_to = (date_from + relativedelta(months=1)) - timedelta(days=1)

    parq_folder = cfg_val['PARQUET_FOLDER'].strip()
    if not os.path.isdir(parq_folder):
        logging.error("Invalid Parquet Folder: " + parq_folder)
        sys.exit(1)

    logging.info("Date From: " + date_from.strftime("%Y-%m-%d") + ", To: " + date_to.strftime("%Y-%m-%d"))
    gen_lat_rpt(cfg_val, parq_folder, date_from, date_to)
    logging.info("Parquet Latency Report Month Email completed")


if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("Config file is missing")
        print("Usage : " + sys.argv[0] + " <CONFIG file>")
        sys.exit(1)
    main()
