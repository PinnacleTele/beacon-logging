import sys
import mysql.connector
import psycopg2
import pandas as pd

from datetime import datetime, timedelta
from dateutil.rrule import *
from dateutil.relativedelta import *

import configobj
import logging

null_value = "~~NULL~~"


def df_insert_data(date_str, pg_cursor, pg_tbl_name, pg_col_lst, df_data):
    global null_value
    pg_val_str = "(" + ",".join(["%s" for _ in pg_col_lst.split(',')]) + ")"
    pg_sql = "insert into " + pg_tbl_name + "(" + pg_col_lst + ") values "

    del_sql = "delete from " + pg_tbl_name + " where recv_date = %s"
    logging.info("Date: " + date_str)
    logging.info("Executing Delete SQL: " + del_sql)
    pg_cursor.execute(del_sql, (date_str,))
    row_ins_lst = []
    row_count = 0
    logging.info("Building row data from dataframe...")
    for rpt_row in df_data.itertuples(index=False):
        row = [rpt_row[0].strftime("%Y-%m-%d %H:%M:%S")]
        for cval in rpt_row[1:]:
            if cval == null_value:
                row.append(None)
            else:
                row.append(cval)
        row_ins_lst.append(tuple(row))
        row_count += 1

    logging.info("Row Count: " + str(row_count))
    val_txt = b",".join(pg_cursor.mogrify(pg_val_str, r) for r in row_ins_lst).decode("utf-8")
    logging.info("Insert Summary data into the table: " + pg_tbl_name)
    pg_cursor.execute(pg_sql + val_txt)
    logging.info("Insert Count: " + str(pg_cursor.rowcount))

def gen_ui_plat_lat_rpt(df_data):
    global null_value
    lat_buckets = [(0, 5499), (5500, 10499), (10500, 15499), (15500, 30499), (30500, 45499), (45500, 60499),
                   (60500, 120499)]
    lat_labels = ['lat_0_5_sec', 'lat_6_10_sec', 'lat_11_15_sec', 'lat_15_130_sec', 'lat_31_45_sec', 'lat_46_60_sec',
                  'lat_61_120_sec']

    df_ui_plat_lat = df_data[df_data['sub_sub_ori_sts_code'] == '400'][['recv_date', 'cli_id',
                                                                            'sub_cli_hdr', 'sub_country',
                                                                            'sub_sub_lat_sla_in_millis']].copy()
    d_agg = {'sub_sub_lat_sla_in_millis': 'count'}
    for t_idx, t_range in enumerate(lat_buckets):
        f_name = lat_labels[t_idx]
        df_ui_plat_lat[f_name] = df_ui_plat_lat['sub_sub_lat_sla_in_millis'].apply(lambda x: 1
                                                                            if (x >= t_range[0] and x <= t_range[1])
                                                                            else 0)
        d_agg[f_name] = 'sum'

    max_value = lat_buckets[-1][1]
    max_label = "lat_gt_120_sec"
    d_agg[max_label] = 'sum'
    df_ui_plat_lat[max_label] = df_ui_plat_lat['sub_sub_lat_sla_in_millis'].apply(lambda x: 1
                                                                                    if x > max_value else 0)


    ui_plat_lat_group_cols = ['recv_date', 'cli_id', 'sub_cli_hdr', 'sub_country']
    df_ui_plat_lat_count = df_ui_plat_lat.groupby(by=ui_plat_lat_group_cols).agg(d_agg).\
                                reset_index().rename(columns={"sub_sub_lat_sla_in_millis": "count"})
    return df_ui_plat_lat_count

def gen_ui_tel_lat_rpt(df_data):
    global null_value
    lat_buckets = [(0, 5499), (5500, 10499), (10500, 15499), (15500, 30499), (30500, 45499), (45500, 60499),
                   (60500, 120499)]
    lat_labels = ['lat_0_5_sec', 'lat_6_10_sec', 'lat_11_15_sec', 'lat_15_130_sec', 'lat_31_45_sec', 'lat_46_60_sec',
                  'lat_61_120_sec']

    df_ui_tel_lat = df_data[(df_data["del_dn_ori_sts_code"] != null_value) &
                                         (df_data["del_dn_ori_sts_code"] == "600")][['recv_date', 'cli_id',
                                                                            'del_dn_hdr', 'sub_country',
                                                                            'del_delv_lat_sla_in_millis']].copy()
    d_agg = {'del_delv_lat_sla_in_millis': 'count'}
    for t_idx, t_range in enumerate(lat_buckets):
        f_name = lat_labels[t_idx]
        df_ui_tel_lat[f_name] = df_ui_tel_lat['del_delv_lat_sla_in_millis'].apply(lambda x: 1
                                                                            if (x >= t_range[0] and x <= t_range[1])
                                                                            else 0)
        d_agg[f_name] = 'sum'

    max_value = lat_buckets[-1][1]
    max_label = "lat_gt_120_sec"
    d_agg[max_label] = 'sum'
    df_ui_tel_lat[max_label] = df_ui_tel_lat['del_delv_lat_sla_in_millis'].apply(lambda x: 1
                                                                                    if x > max_value else 0)


    ui_plat_lat_group_cols = ['recv_date', 'cli_id', 'del_dn_hdr', 'sub_country']
    df_ui_tel_lat_count = df_ui_tel_lat.groupby(by=ui_plat_lat_group_cols).agg(d_agg).\
                                reset_index().rename(columns={"del_delv_lat_sla_in_millis": "count"})

    return df_ui_tel_lat_count

def gen_ui_mix_rpt(df_data, df_err_cd):
    global null_value
    ui_mix_rpt_group_cols = ['recv_date', 'cli_id', 'sub_cli_hdr', 'sub_campaign_id', 'sub_campaign_name',
                             'sub_intf_type', 'sub_country', 'sub_billing_currency']
    df_ui_mix = df_data
    df_ui_mix_total_group = df_ui_mix.groupby(by=ui_mix_rpt_group_cols)
    df_ui_mix_total_count = df_ui_mix_total_group.agg({
                                'sub_sub_ori_sts_code': 'count',
                                'sms_rate': 'sum',
                                'dlt_rate': 'sum'
                            }).reset_index().rename(columns={"sub_sub_ori_sts_code": "count"})
    df_ui_mix_total_count["count"] = df_ui_mix_total_count["count"].astype(int)

    df_ui_mix_submit_group = df_ui_mix[df_ui_mix["sub_sub_ori_sts_code"] == "400"].groupby(by=ui_mix_rpt_group_cols)
    df_ui_mix_submit_count = df_ui_mix_submit_group["sub_sub_ori_sts_code"].agg(["count"]).reset_index()
    df_ui_mix_submit_count["count"] = df_ui_mix_submit_count["count"].astype(int)

    df_ui_mix_delivery_group = df_ui_mix[(df_ui_mix["del_dn_ori_sts_code"] != null_value) &
                                         (df_ui_mix["del_dn_ori_sts_code"] == "600")]. \
        groupby(by=ui_mix_rpt_group_cols)
    df_ui_mix_delivery_count = df_ui_mix_delivery_group["del_dn_ori_sts_code"].agg(["count"]).reset_index()
    df_ui_mix_delivery_count["count"] = df_ui_mix_delivery_count["count"].astype(int)

    dn_fail_err_cd = df_err_cd[(df_err_cd["category"] == "OPERATOR") &
                               (df_err_cd["status_flag"] == "Failed")]["error_code"]
    df_ui_mix_fail_group = df_ui_mix[(df_ui_mix["del_dn_ori_sts_code"] != null_value) &
                                     (df_ui_mix["del_dn_ori_sts_code"].isin(dn_fail_err_cd))]. \
        groupby(by=ui_mix_rpt_group_cols)
    df_ui_mix_fail_count = df_ui_mix_fail_group["del_dn_ori_sts_code"].agg(["count"]).reset_index()
    df_ui_mix_fail_count["count"] = df_ui_mix_fail_count["count"].astype(int)

    dn_expire_err_cd = df_err_cd[(df_err_cd["category"] == "OPERATOR") &
                                 (df_err_cd["status_flag"] == "Expired")]["error_code"]
    df_ui_mix_expire_group = df_ui_mix[(df_ui_mix["del_dn_ori_sts_code"] != null_value) &
                                       (df_ui_mix["del_dn_ori_sts_code"].isin(dn_expire_err_cd))]. \
        groupby(by=ui_mix_rpt_group_cols)

    df_ui_mix_expire_count = df_ui_mix_expire_group["del_dn_ori_sts_code"].agg(["count"]).reset_index()
    df_ui_mix_expire_count["count"] = df_ui_mix_expire_count["count"].astype(int)

    sub_reject_err_cd = df_err_cd[df_err_cd["category"].isin(["PLATFORM", "INTERFACE"])]["error_code"]
    df_ui_mix_reject_group = df_ui_mix[(df_ui_mix["sub_sub_ori_sts_code"] != "400") &
                                       (df_ui_mix["sub_sub_ori_sts_code"].isin(sub_reject_err_cd))]. \
        groupby(by=ui_mix_rpt_group_cols)
    df_ui_mix_reject_count = df_ui_mix_reject_group["sub_sub_ori_sts_code"].agg(["count"]).reset_index()
    df_ui_mix_reject_count["count"] = df_ui_mix_reject_count["count"].astype(int)

    df_ui_mix_oocredit_group = df_ui_mix[df_ui_mix["sub_sub_ori_sts_code"] == "445"]. \
        groupby(by=ui_mix_rpt_group_cols)
    df_ui_mix_oocredit_count = df_ui_mix_oocredit_group["sub_sub_ori_sts_code"].agg(["count"]).reset_index()
    df_ui_mix_oocredit_count["count"] = df_ui_mix_oocredit_count["count"].astype(int)

    df_dn_pending = df_ui_mix[(df_ui_mix["sub_msg_type"] != 0) &
                              (df_ui_mix["sub_sub_ori_sts_code"] == "400")].copy()

    #df_dn_pending["dn_check"] = df_dn_pending["del_dn_ori_sts_code"].apply(lambda x: 0 if x == null_value else 1)
    df_dn_pending["dn_check"] = df_dn_pending["del_dn_ori_sts_code"]. \
                                    apply(lambda x: 1 if x != null_value and x >= '600' else 0)

    df_ui_mix_dn_pending_group = df_dn_pending.groupby(by=ui_mix_rpt_group_cols)
    df_ui_mix_dn_pending_count = df_ui_mix_dn_pending_group.agg(
        {"sub_sub_ori_sts_code": "count", "dn_check": "sum"}).reset_index()

    df_ui_mix_dn_pending_count["dn_pending_count"] = df_ui_mix_dn_pending_count["sub_sub_ori_sts_code"] - \
                                                     df_ui_mix_dn_pending_count["dn_check"]
    df_ui_mix_dn_pending_count["dn_pending_count"] = df_ui_mix_dn_pending_count["dn_pending_count"].astype(int)
    df_ui_mix_dn_pending_count.drop(["sub_sub_ori_sts_code", "dn_check"], axis=1, inplace=True)

    df_ui_mix_rpt = pd.merge(df_ui_mix_total_count, df_ui_mix_submit_count,
                             how='left',
                             on=ui_mix_rpt_group_cols,
                             left_index=False,
                             right_index=False,
                             suffixes=["_total", "_submit"]).reset_index(drop=True)
    df_ui_mix_rpt["count_submit"].fillna(0, inplace=True)
    df_ui_mix_rpt["count_submit"] = df_ui_mix_rpt["count_submit"].astype(int)

    df_ui_mix_rpt = pd.merge(df_ui_mix_rpt, df_ui_mix_delivery_count,
                             how='left',
                             on=ui_mix_rpt_group_cols,
                             left_index=False,
                             right_index=False).reset_index(drop=True).rename(columns={"count": "count_delivery"})
    df_ui_mix_rpt["count_delivery"].fillna(0, inplace=True)
    df_ui_mix_rpt["count_delivery"] = df_ui_mix_rpt["count_delivery"].astype(int)

    df_ui_mix_rpt = pd.merge(df_ui_mix_rpt, df_ui_mix_fail_count,
                             how='left',
                             on=ui_mix_rpt_group_cols,
                             left_index=False,
                             right_index=False).reset_index(drop=True).rename(columns={"count": "count_dn_failed"})
    df_ui_mix_rpt["count_dn_failed"].fillna(0, inplace=True)
    df_ui_mix_rpt["count_dn_failed"] = df_ui_mix_rpt["count_dn_failed"].astype(int)

    df_ui_mix_rpt = pd.merge(df_ui_mix_rpt, df_ui_mix_expire_count,
                             how='left',
                             on=ui_mix_rpt_group_cols,
                             left_index=False,
                             right_index=False).reset_index(drop=True).rename(columns={"count": "count_dn_expired"})
    df_ui_mix_rpt["count_dn_expired"].fillna(0, inplace=True)
    df_ui_mix_rpt["count_dn_expired"] = df_ui_mix_rpt["count_dn_expired"].astype(int)

    df_ui_mix_rpt = pd.merge(df_ui_mix_rpt, df_ui_mix_reject_count,
                             how='left',
                             on=ui_mix_rpt_group_cols,
                             left_index=False,
                             right_index=False).reset_index(drop=True).rename(columns={"count": "count_rejected"})
    df_ui_mix_rpt["count_rejected"].fillna(0, inplace=True)
    df_ui_mix_rpt["count_rejected"] = df_ui_mix_rpt["count_rejected"].astype(int)

    df_ui_mix_rpt = pd.merge(df_ui_mix_rpt, df_ui_mix_oocredit_count,
                             how='left',
                             on=ui_mix_rpt_group_cols,
                             left_index=False,
                             right_index=False).reset_index(drop=True).rename(columns={"count": "count_out_of_credit"})
    df_ui_mix_rpt["count_out_of_credit"].fillna(0, inplace=True)
    df_ui_mix_rpt["count_out_of_credit"] = df_ui_mix_rpt["count_out_of_credit"].astype(int)

    df_ui_mix_rpt = pd.merge(df_ui_mix_rpt, df_ui_mix_dn_pending_count,
                             how='left',
                             on=ui_mix_rpt_group_cols,
                             left_index=False,
                             right_index=False).reset_index(drop=True).rename(
        columns={"dn_pending_count": "count_dn_pending"})
    df_ui_mix_rpt["count_dn_pending"].fillna(0, inplace=True)
    df_ui_mix_rpt["count_dn_pending"] = df_ui_mix_rpt["count_dn_pending"].astype(int)
    return df_ui_mix_rpt

def gen_ui_camp_rpt(df_data, df_err_cd):
    global null_value
    ui_camp_rpt_group_cols = ['recv_date', 'cli_id', 'sub_cli_hdr', 'sub_msg_tag', 'sub_msg_tag1', 'sub_msg_tag2',
                      'sub_msg_tag3', 'sub_msg_tag4', 'sub_msg_tag5', 'sub_campaign_id', 'sub_campaign_name',
                      'sub_file_id', 'sub_file_name', 'sub_intf_type', 'sub_country', 'sub_billing_currency']
    df_ui_camp = df_data
    df_ui_camp_total_group = df_ui_camp.groupby(by=ui_camp_rpt_group_cols)
    df_ui_camp_total_count = df_ui_camp_total_group.agg({
                                'sub_sub_ori_sts_code': 'count',
                                'sms_rate': 'sum',
                                'dlt_rate': 'sum'
                            }).reset_index().rename(columns={"sub_sub_ori_sts_code": "count"})
    df_ui_camp_total_count["count"] = df_ui_camp_total_count["count"].astype(int)

    df_ui_camp_submit_group = df_ui_camp[df_ui_camp["sub_sub_ori_sts_code"] == "400"].groupby(by=ui_camp_rpt_group_cols)
    df_ui_camp_submit_count = df_ui_camp_submit_group["sub_sub_ori_sts_code"].agg(["count"]).reset_index()
    df_ui_camp_submit_count["count"] = df_ui_camp_submit_count["count"].astype(int)

    df_ui_camp_delivery_group = df_ui_camp[(df_ui_camp["del_dn_ori_sts_code"] != null_value) &
                                           (df_ui_camp["del_dn_ori_sts_code"] == "600")]. \
        groupby(by=ui_camp_rpt_group_cols)
    df_ui_camp_delivery_count = df_ui_camp_delivery_group["del_dn_ori_sts_code"].agg(["count"]).reset_index()
    df_ui_camp_delivery_count["count"] = df_ui_camp_delivery_count["count"].astype(int)

    dn_fail_err_cd = df_err_cd[(df_err_cd["category"] == "OPERATOR") &
                               (df_err_cd["status_flag"] == "Failed")]["error_code"]
    df_ui_camp_fail_group = df_ui_camp[(df_ui_camp["del_dn_ori_sts_code"] != null_value) &
                                       (df_ui_camp["del_dn_ori_sts_code"].isin(dn_fail_err_cd))]. \
        groupby(by=ui_camp_rpt_group_cols)
    df_ui_camp_fail_count = df_ui_camp_fail_group["del_dn_ori_sts_code"].agg(["count"]).reset_index()
    df_ui_camp_fail_count["count"] = df_ui_camp_fail_count["count"].astype(int)

    dn_expire_err_cd = df_err_cd[(df_err_cd["category"] == "OPERATOR") &
                                 (df_err_cd["status_flag"] == "Expired")]["error_code"]
    df_ui_camp_expire_group = df_ui_camp[(df_ui_camp["del_dn_ori_sts_code"] != null_value) &
                                         (df_ui_camp["del_dn_ori_sts_code"].isin(dn_expire_err_cd))]. \
        groupby(by=ui_camp_rpt_group_cols)

    df_ui_camp_expire_count = df_ui_camp_expire_group["del_dn_ori_sts_code"].agg(["count"]).reset_index()
    df_ui_camp_expire_count["count"] = df_ui_camp_expire_count["count"].astype(int)

    sub_reject_err_cd = df_err_cd[df_err_cd["category"].isin(["PLATFORM", "INTERFACE"])]["error_code"]
    df_ui_camp_reject_group = df_ui_camp[(df_ui_camp["sub_sub_ori_sts_code"] != "400") &
                                         (df_ui_camp["sub_sub_ori_sts_code"].isin(sub_reject_err_cd))]. \
        groupby(by=ui_camp_rpt_group_cols)
    df_ui_camp_reject_count = df_ui_camp_reject_group["sub_sub_ori_sts_code"].agg(["count"]).reset_index()
    df_ui_camp_reject_count["count"] = df_ui_camp_reject_count["count"].astype(int)

    df_ui_camp_oocredit_group = df_ui_camp[df_ui_camp["sub_sub_ori_sts_code"] == "445"]. \
        groupby(by=ui_camp_rpt_group_cols)
    df_ui_camp_oocredit_count = df_ui_camp_oocredit_group["sub_sub_ori_sts_code"].agg(["count"]).reset_index()
    df_ui_camp_oocredit_count["count"] = df_ui_camp_oocredit_count["count"].astype(int)

    df_dn_pending = df_ui_camp[(df_ui_camp["sub_msg_type"] != 0) &
                               (df_ui_camp["sub_sub_ori_sts_code"] == "400")].copy()

    df_dn_pending["dn_check"] = df_dn_pending["del_dn_ori_sts_code"].apply(lambda x: 0 if x == null_value else 1)
    df_dn_pending["dn_check"] = df_dn_pending["del_dn_ori_sts_code"]. \
                                    apply(lambda x: 1 if x != null_value and x >= '600' else 0)
    df_ui_camp_dn_pending_group = df_dn_pending.groupby(by=ui_camp_rpt_group_cols)
    df_ui_camp_dn_pending_count = df_ui_camp_dn_pending_group.agg(
        {"sub_sub_ori_sts_code": "count", "dn_check": "sum"}).reset_index()

    df_ui_camp_dn_pending_count["dn_pending_count"] = df_ui_camp_dn_pending_count["sub_sub_ori_sts_code"] - \
                                                      df_ui_camp_dn_pending_count["dn_check"]
    df_ui_camp_dn_pending_count["dn_pending_count"] = df_ui_camp_dn_pending_count["dn_pending_count"].astype(int)
    df_ui_camp_dn_pending_count.drop(["sub_sub_ori_sts_code", "dn_check"], axis=1, inplace=True)

    df_ui_camp_rpt = pd.merge(df_ui_camp_total_count, df_ui_camp_submit_count,
                              how='left',
                              on=ui_camp_rpt_group_cols,
                              left_index=False,
                              right_index=False,
                              suffixes=["_total", "_submit"]).reset_index(drop=True)
    df_ui_camp_rpt["count_submit"].fillna(0, inplace=True)
    df_ui_camp_rpt["count_submit"] = df_ui_camp_rpt["count_submit"].astype(int)

    df_ui_camp_rpt = pd.merge(df_ui_camp_rpt, df_ui_camp_delivery_count,
                              how='left',
                              on=ui_camp_rpt_group_cols,
                              left_index=False,
                              right_index=False).reset_index(drop=True).rename(columns={"count": "count_delivery"})
    df_ui_camp_rpt["count_delivery"].fillna(0, inplace=True)
    df_ui_camp_rpt["count_delivery"] = df_ui_camp_rpt["count_delivery"].astype(int)

    df_ui_camp_rpt = pd.merge(df_ui_camp_rpt, df_ui_camp_fail_count,
                              how='left',
                              on=ui_camp_rpt_group_cols,
                              left_index=False,
                              right_index=False).reset_index(drop=True).rename(columns={"count": "count_dn_failed"})
    df_ui_camp_rpt["count_dn_failed"].fillna(0, inplace=True)
    df_ui_camp_rpt["count_dn_failed"] = df_ui_camp_rpt["count_dn_failed"].astype(int)

    df_ui_camp_rpt = pd.merge(df_ui_camp_rpt, df_ui_camp_expire_count,
                              how='left',
                              on=ui_camp_rpt_group_cols,
                              left_index=False,
                              right_index=False).reset_index(drop=True).rename(columns={"count": "count_dn_expired"})
    df_ui_camp_rpt["count_dn_expired"].fillna(0, inplace=True)
    df_ui_camp_rpt["count_dn_expired"] = df_ui_camp_rpt["count_dn_expired"].astype(int)

    df_ui_camp_rpt = pd.merge(df_ui_camp_rpt, df_ui_camp_reject_count,
                              how='left',
                              on=ui_camp_rpt_group_cols,
                              left_index=False,
                              right_index=False).reset_index(drop=True).rename(columns={"count": "count_rejected"})
    df_ui_camp_rpt["count_rejected"].fillna(0, inplace=True)
    df_ui_camp_rpt["count_rejected"] = df_ui_camp_rpt["count_rejected"].astype(int)

    df_ui_camp_rpt = pd.merge(df_ui_camp_rpt, df_ui_camp_oocredit_count,
                              how='left',
                              on=ui_camp_rpt_group_cols,
                              left_index=False,
                              right_index=False).reset_index(drop=True).rename(columns={"count": "count_out_of_credit"})
    df_ui_camp_rpt["count_out_of_credit"].fillna(0, inplace=True)
    df_ui_camp_rpt["count_out_of_credit"] = df_ui_camp_rpt["count_out_of_credit"].astype(int)

    df_ui_camp_rpt = pd.merge(df_ui_camp_rpt, df_ui_camp_dn_pending_count,
                              how='left',
                              on=ui_camp_rpt_group_cols,
                              left_index=False,
                              right_index=False).reset_index(drop=True).rename(
        columns={"dn_pending_count": "count_dn_pending"})
    df_ui_camp_rpt["count_dn_pending"].fillna(0, inplace=True)
    df_ui_camp_rpt["count_dn_pending"] = df_ui_camp_rpt["count_dn_pending"].astype(int)
    return df_ui_camp_rpt

def proc_date(date_val, pg_conn, df_err_cd):
    global null_value
    date_str = date_val.strftime("%Y-%m-%d")
    logging.info("Processing date: " + date_str)

    date_tbl_str = date_val.strftime("%Y%m%d")
    pg_tbl = "smslog.sub_del_log_" + date_tbl_str

    pg_cursor = pg_conn.cursor()

    sql = """select recv_date, cli_id, sub_cli_hdr, sub_msg_tag, 
            sub_msg_tag1, sub_msg_tag2, sub_msg_tag3, sub_msg_tag4, sub_msg_tag5, 
            sub_campaign_id, sub_campaign_name, sub_file_id, sub_file_name, sub_intf_type, 
            sub_country, sub_msg_type, sub_sub_ori_sts_code, del_dn_ori_sts_code, sub_billing_currency,
            (sub_billing_sms_rate + coalesce(del_billing_sms_rate,0)) as sms_rate,
            (sub_billing_add_fixed_rate + coalesce(del_billing_add_fixed_rate ,0)) as dlt_rate,
            del_dn_hdr, sub_sub_lat_sla_in_millis, del_delv_lat_sla_in_millis"""

    sql += " from " + pg_tbl + " s "
    sql += " where not exists(select 'x' from " + pg_tbl + " s2 "
    sql += " where s2.base_msg_id = s.base_msg_id and s2.sub_msg_part_no > s2.sub_total_msg_parts)"
    logging.info("Executing SQL: " + sql)

    pg_cursor.execute(sql)
    rlst = pg_cursor.fetchall()
    if len(rlst) == 0:
        logging.info("No Data found for the date: " + date_str)
        return

    cols = [c[0] for c in pg_cursor.description]
    cur_row_count = pg_cursor.rowcount

    logging.info("Cursor Row count: " + str(cur_row_count))
    logging.info("Row count: " + str(len(rlst)))

    df_data = pd.DataFrame.from_records(rlst, columns=cols)
    df_data.fillna(null_value, inplace=True)
    df_ui_camp = df_data[df_data['sub_intf_type'] == 'gui'].copy()

    if len(df_ui_camp.index) > 0:
        df_ui_camp_rpt = gen_ui_camp_rpt(df_ui_camp, df_err_cd)

        if len(df_ui_camp_rpt.index) > 0:
            pg_col_lst = "recv_date, cli_id, cli_hdr, msg_tag, msg_tag1, msg_tag2, msg_tag3, msg_tag4, msg_tag5, " + \
                     "campaign_id, campaign_name, file_id, file_name, intf_type, country,  billing_currency," + \
                     "tot_cnt, billing_sms_rate, billing_add_fixed_rate, submitted_cnt, delivered_cnt," + \
                     "dn_failed_cnt, dn_expired_cnt, rejected_cnt, out_of_credits_cnt, dn_pending_cnt"

            df_insert_data(date_str, pg_cursor, "summary.ui_camp_report", pg_col_lst, df_ui_camp_rpt)

            pg_conn.commit()

    df_ui_traffic_mix_rpt = gen_ui_mix_rpt(df_data, df_err_cd)

    if len(df_ui_traffic_mix_rpt.index) > 0:
        pg_col_lst = "recv_date, cli_id, cli_hdr, campaign_id, campaign_name, intf_type, country, billing_currency," + \
                     "tot_cnt, billing_sms_rate, billing_add_fixed_rate, submitted_cnt, delivered_cnt, " + \
                     "dn_failed_cnt, dn_expired_cnt, rejected_cnt, out_of_credits_cnt, dn_pending_cnt"

        df_insert_data(date_str, pg_cursor, "summary.ui_traffic_mix_report", pg_col_lst, df_ui_traffic_mix_rpt)

        pg_conn.commit()

        ui_traffic_rpt_group_cols = ['recv_date', 'cli_id', 'sub_cli_hdr', 'sub_intf_type', 'sub_country',
                                     'sub_billing_currency']
        df_ui_traffic_rpt = df_ui_traffic_mix_rpt.groupby(ui_traffic_rpt_group_cols).agg({
            'count_total': 'sum',
            'sms_rate': 'sum',
            'dlt_rate': 'sum',
            'count_submit': 'sum',
            'count_delivery': 'sum',
            'count_dn_failed': 'sum',
            'count_dn_expired': 'sum',
            'count_rejected': 'sum',
            'count_out_of_credit': 'sum',
            'count_dn_pending': 'sum'
        }).reset_index()

        if len(df_ui_traffic_rpt.index) > 0:
            pg_col_lst = "recv_date, cli_id, cli_hdr, intf_type, country, billing_currency," + \
                         "tot_cnt, billing_sms_rate, billing_add_fixed_rate, submitted_cnt, " + \
                         "delivered_cnt, dn_failed_cnt, dn_expired_cnt, rejected_cnt, out_of_credits_cnt, " + \
                         "dn_pending_cnt"
            df_insert_data(date_str, pg_cursor, "summary.ui_traffic_report", pg_col_lst, df_ui_traffic_rpt)

            pg_conn.commit()

        df_ui_plat_lat_rpt = gen_ui_plat_lat_rpt(df_data)
        if len(df_ui_plat_lat_rpt.index) > 0:
            pg_col_lst = "recv_date, cli_id, cli_hdr, country, tot_cnt, " \
                         "lat_0_5_sec_cnt, lat_6_10_sec_cnt, lat_11_15_sec_cnt, " \
                         "lat_16_30_sec_cnt, lat_31_45_sec_cnt, lat_46_60_sec_cnt, " \
                         "lat_61_120_sec_cnt, lat_gt_120_sec_cnt"
            df_insert_data(date_str, pg_cursor, "summary.ui_platform_latency_report", pg_col_lst, df_ui_plat_lat_rpt)

            pg_conn.commit()

        df_ui_tel_lat_rpt = gen_ui_tel_lat_rpt(df_data)
        if len(df_ui_tel_lat_rpt.index) > 0:
            pg_col_lst = "recv_date, cli_id, cli_hdr, country, tot_cnt, " \
                         "lat_0_5_sec_cnt, lat_6_10_sec_cnt, lat_11_15_sec_cnt, " \
                         "lat_16_30_sec_cnt, lat_31_45_sec_cnt, lat_46_60_sec_cnt, " \
                         "lat_61_120_sec_cnt, lat_gt_120_sec_cnt"
            df_insert_data(date_str, pg_cursor, "summary.ui_telco_latency_report", pg_col_lst, df_ui_tel_lat_rpt)

            pg_conn.commit()
    pg_cursor.close()

def main():
    cfg_fn = sys.argv[1]
    cfg_val = configobj.ConfigObj(cfg_fn)
    log_fn_dtsfx = datetime.now().strftime("%Y%m%d_%H%M%S")

    log_fn = "./log/pg_ui_summary_report_" + log_fn_dtsfx + ".log"
    logging.basicConfig(filename=log_fn, level=logging.INFO,
                        format='%(asctime)s %(levelname)-8s %(funcName)-20s %(message)s')
    logging.info("UI Summary Report generation started")
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
    err_cd_sql = "select error_code, category, ui_grouping, status_flag from configuration.error_code_mapping"
    my_cursor.execute(err_cd_sql)
    ecd_rlst = my_cursor.fetchall()
    ecd_cols = [c[0] for c in my_cursor.description]
    my_cursor.close()
    my_conn.close()

    df_err_cd = pd.DataFrame.from_records(ecd_rlst, columns=ecd_cols)

    cfg_date_from = cfg_val['RPT_DATE_FROM'].strip().upper()
    logging.info("Config Date From: " + cfg_date_from)
    if cfg_date_from == "DEFAULT":
        date_from = (datetime.now() - timedelta(days=2))
        date_to = (datetime.now() - timedelta(days=1))
    else:
        date_from = datetime.strptime(cfg_date_from, "%Y-%m-%d")
        date_to = datetime.strptime(cfg_val['RPT_DATE_TO'].strip(), "%Y-%m-%d")

    logging.info("Date From: " + date_from.strftime("%Y-%m-%d") + ", To: " + date_to.strftime("%Y-%m-%d"))

    pg_host = cfg_val['PG_HOST']
    pg_port = cfg_val['PG_PORT']
    pg_db = cfg_val['PG_DB']
    pg_user = cfg_val['PG_USER']
    pg_pass = cfg_val['PG_PASS']

    pg_con_tmplt = "host='{host}' port='{port}' dbname='{db}' user='{user}' password='{password}'"
    pg_con_str = pg_con_tmplt.format(host=pg_host, port=pg_port, db=pg_db, user=pg_user, password=pg_pass)
    logging.info("Connecting to Postgres DB: " + pg_host + "@" + pg_db)
    pg_conn = psycopg2.connect(pg_con_str)

    if date_from == date_to:
        proc_date(date_from, pg_conn, df_err_cd)
    else:
        dt_lst = list(rrule(DAILY, interval=1, dtstart=date_from, until=date_to))
        for dt in dt_lst:
            proc_date(dt, pg_conn, df_err_cd)

    pg_conn.close()
    logging.info("UI Summary Report generation completed")
    logging.shutdown()


if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("Config file is missing")
        print("Usage : " + sys.argv[0] + " <CONFIG file>")
        sys.exit(1)
    main()
