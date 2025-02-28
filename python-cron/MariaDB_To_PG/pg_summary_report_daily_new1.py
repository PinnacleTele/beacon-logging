import sys
import mysql.connector
import psycopg2
import pandas as pd

from datetime import datetime, timedelta
from dateutil.rrule import *

import configobj
import logging

def df_insert_data(date_str, pg_cursor, pg_tbl_name, pg_col_lst, df_data):
    pg_val_str = "(" + ",".join(["%s" for _ in pg_col_lst.split(',')]) + ")"
    pg_sql = "insert into " + pg_tbl_name + "(" + pg_col_lst + ") values "

    del_sql = "delete from " + pg_tbl_name + " where recv_date = %s"
    logging.info("Date: " + date_str)
    logging.info("Executing Delete SQL: " + del_sql)
    pg_cursor.execute(del_sql, (date_str,))

    row_ins_lst = []
    row_count = 0
    for rpt_row in df_data.itertuples(index=False):
        row = [date_str]
        for cval in rpt_row:
            row.append(cval)
        row_ins_lst.append(tuple(row))
        row_count += 1

    if row_count > 0:
        logging.info("Row Count: " + str(row_count))
        logging.info("Insert Summary data into the table: " + pg_tbl_name)
        val_txt = b",".join(pg_cursor.mogrify(pg_val_str, r) for r in row_ins_lst).decode("utf-8")
        pg_cursor.execute(pg_sql + val_txt)

def proc_date(date_val, pg_conn, df_usr, df_car_db):
    date_str = date_val.strftime("%Y-%m-%d")
    sql_tmplt = """select to_char(recv_time,'HH24') as recv_hour, cli_id, sub_route_id, 
    sub_sub_ori_sts_code, sub_msg_type, del_dn_ori_sts_code, sub_country, sub_smsc_id, del_carrier_sys_id, sub_billing_currency
    from smslog.sub_del_log_{}"""
    tbl_sfx = date_val.strftime("%Y%m%d")
    sql = sql_tmplt.format(tbl_sfx, )
    logging.info("Executing SQL: " + sql)
    pg_cursor = pg_conn.cursor()
    pg_cursor.execute(sql)
    rlst = pg_cursor.fetchall()
    if len(rlst) == 0:
        logging.info("No data found for the date: " + date_str)
        return

    cols = ['recv_hour', 'cli_id', 'route_id', 'sub_ori_sts_code', 'msg_type', 'dn_ori_sts_code', 'sub_country', 'sub_smsc_id', 'del_carrier_sys_id', 'sub_billing_currency']
    null_value = "~~NULL~~"
    df_sms_db = pd.DataFrame.from_records(rlst, columns=cols)
    df_sms_db['dn_ori_sts_code'] = df_sms_db['dn_ori_sts_code'].fillna(null_value)
    df_sms_db['submitted'] = df_sms_db['sub_ori_sts_code'].apply(lambda x: 1 if x == '400' else 0)
    df_sms_db['platform_reject'] = df_sms_db['submitted'].apply(lambda x: 1 if x == 0 else 0)
    df_sms_db['non_promo_sub'] = df_sms_db.apply(lambda r: 1 if (r['submitted'] == 1 and r['msg_type'] != 0) else 0,
                                                 axis=1)
    df_sms_db['delivery'] = df_sms_db['dn_ori_sts_code'].apply(lambda x: 1 if x == '600' else 0)
    df_sms_db['delivery_failed'] = df_sms_db['dn_ori_sts_code'].apply(
        lambda x: 1 if x != null_value and x > '600' else 0)

    df_sms_db_car = pd.merge(df_sms_db,
                                df_car_db,
                                left_on='route_id',
                                right_on='route_id',
                                left_index=False,
                                right_index=False,
                                how='inner',
                                suffixes=('', '_car')).reset_index(drop=True)

    group_cols = ['recv_hour', 'cli_id', 'carrier_name', 'sub_country', 'sub_smsc_id', 'del_carrier_sys_id', 'sub_billing_currency']
    df_sms_car_total = df_sms_db_car.groupby(by=group_cols).agg({
        'sub_ori_sts_code': 'count',
        'submitted': 'sum',
        'platform_reject': 'sum',
        'non_promo_sub': 'sum',
        'delivery': 'sum',
        'delivery_failed': 'sum'
    }).reset_index().rename(columns={'sub_ori_sts_code': 'received'})
    df_sms_car_total['nulldn'] = df_sms_car_total['non_promo_sub'] - (
                df_sms_car_total['delivery'] + df_sms_car_total['delivery_failed'])
    df_sms_car_total['delivery_pct'] = df_sms_car_total.apply(lambda r:
                                      (r['delivery'] / r['non_promo_sub']) * 100
                                         if r['non_promo_sub'] > 0 else 0.0, axis=1)
    #df_sms_total['delivery_pct'] = round((df_sms_total['delivery'] / df_sms_total['non_promo_sub']) * 100, 2)
    df_sms_car_total['delivery_pct'] = df_sms_car_total['delivery_pct'].fillna(0.0). \
                                                    astype(float).round(2)

    df_sms_car_usr_total = pd.merge(df_sms_car_total,
                                    df_usr,
                                    left_on='cli_id',
                                    right_on='cli_id',
                                    left_index=False,
                                    right_index=False,
                                    how='inner',
                                    suffixes=('', '_usr')).reset_index(drop=True)

    df_cols = ['recv_hour', 'cli_id', 'username', 'pu_id', 'username_pu', 'su_id', 'username_su',
               'company', 'carrier_name', 'received', 'submitted', 'non_promo_sub', 'delivery',
               'delivery_failed', 'platform_reject', 'nulldn', 'delivery_pct', 'sub_country','sub_smsc_id', 'del_carrier_sys_id', 'sub_billing_currency']

    pg_col_lst = 'recv_date, recv_hour, cli_id, username, pu_id, pu_username, ' \
                 'su_id, su_username, company, carrier_name, total_received, total_submitted, ' \
                 'non_promo_sub_count, delivery_count, failed_count, platform_reject, ' \
                 'nulldn, delivery_pct, sub_country, sub_smsc_id, del_carrier_sys_id, sub_billing_currency'
    pg_tbl_name = "summary.hourly_traffic_report_test"
    df_insert_data(date_str, pg_cursor, pg_tbl_name, pg_col_lst, df_sms_car_usr_total[df_cols])
    pg_conn.commit()

    daily_group_cols = ['cli_id', 'username', 'pu_id', 'username_pu', 'su_id', 'username_su',
                        'company', 'carrier_name', 'sub_country', 'sub_smsc_id', 'del_carrier_sys_id', 'sub_billing_currency']
    df_daily_total = df_sms_car_usr_total.groupby(by=daily_group_cols).agg({
        'received': 'sum',
        'submitted': 'sum',
        'platform_reject': 'sum',
        'non_promo_sub': 'sum',
        'delivery': 'sum',
        'delivery_failed': 'sum',
        'nulldn': 'sum'
    }).reset_index()
    #df_daily_total['delivery_pct'] = round((df_daily_total['delivery'] / df_daily_total['non_promo_sub']) * 100, 2)

    df_daily_total['delivery_pct'] = df_daily_total.apply(lambda r:
                                                  (r['delivery'] / r['non_promo_sub']) * 100
                                                  if r['non_promo_sub'] > 0 else 0.0, axis=1)
    df_daily_total['delivery_pct'] = df_daily_total['delivery_pct'].fillna(0). \
                                            astype(float).round(2)

    df_cols = ['cli_id', 'username', 'pu_id', 'username_pu', 'su_id', 'username_su',
               'company', 'carrier_name', 'received', 'submitted', 'non_promo_sub', 'delivery',
               'delivery_failed', 'platform_reject', 'nulldn', 'delivery_pct', 'sub_country','sub_smsc_id', 'del_carrier_sys_id', 'sub_billing_currency']

    pg_col_lst = 'recv_date, cli_id, username, pu_id, pu_username, ' \
                 'su_id, su_username, company, carrier_name, total_received, total_submitted, ' \
                 'non_promo_sub_count, delivery_count, failed_count, platform_reject, ' \
                 'nulldn, delivery_pct, sub_country, sub_smsc_id, del_carrier_sys_id, sub_billing_currency'
    pg_tbl_name = "summary.daily_traffic_report_test"
    df_insert_data(date_str, pg_cursor, pg_tbl_name, pg_col_lst, df_daily_total[df_cols])
    pg_conn.commit()
    pg_cursor.close()

def main():
    cfg_fn = sys.argv[1]
    cfg_val = configobj.ConfigObj(cfg_fn)
    log_fn_dtsfx = datetime.now().strftime("%Y%m%d_%H%M%S")

    log_fn = "./log/pg_daily_summary_report_1_" + log_fn_dtsfx + ".log"
    logging.basicConfig(filename=log_fn, level=logging.INFO,
                        format='%(asctime)s %(levelname)-8s %(funcName)-20s %(message)s')
    logging.info("Daily Summary Report generation started")
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
    usr_sql = "select cli_id, pu_id, su_id, user as username, company from accounts.user_config"
    my_cursor.execute(usr_sql)
    usr_rlst = my_cursor.fetchall()
    usr_cols = [c[0] for c in my_cursor.description]
    df_usr_db = pd.DataFrame.from_records(usr_rlst, columns=usr_cols)
    df_su_id = pd.DataFrame(df_usr_db['su_id'].unique(), columns=['su_id'])
    df_pu_id = pd.DataFrame(df_usr_db['pu_id'].unique(), columns=['pu_id'])
    df_su = pd.merge(df_su_id,
                     df_usr_db,
                     left_on='su_id',
                     right_on='cli_id',
                     left_index=False,
                     right_index=False,
                     how='inner', suffixes=('_su', ''))[['cli_id', 'username']].reset_index(drop=True).rename(
        columns={'cli_id': 'su_id'})
    df_pu = pd.merge(df_pu_id,
                     df_usr_db,
                     left_on='pu_id',
                     right_on='cli_id',
                     left_index=False,
                     right_index=False,
                     how='inner',
                     suffixes=('_pu', ''))[['cli_id', 'username']].reset_index(drop=True).rename(
        columns={'cli_id': 'pu_id'})
    df_usr = pd.merge(df_usr_db,
                      df_su,
                      left_on='su_id',
                      right_on='su_id',
                      left_index=False,
                      right_index=False,
                      how='inner', suffixes=('', '_su')).reset_index(drop=True)
    df_usr = pd.merge(df_usr,
                      df_pu,
                      left_on='pu_id',
                      right_on='pu_id',
                      how='inner', suffixes=('', '_pu')).reset_index(drop=True)

    car_sql = """select crm.route_id, cm.carrier_name 
    from carrier_handover.carrier_route_map crm
    inner join carrier_handover.carrier_master cm 
    on crm.carrier_id = cm.carrier_id """
    my_cursor.execute(car_sql)
    car_rlst = my_cursor.fetchall()
    car_cols = [c[0] for c in my_cursor.description]
    df_car_db = pd.DataFrame.from_records(car_rlst, columns=car_cols)
    my_cursor.close()
    my_conn.close()

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
        date_from = (datetime.now() - timedelta(days=2))
        date_to = (datetime.now() - timedelta(days=1))
    else:
        date_from = datetime.strptime(cfg_date_from, "%Y-%m-%d")
        date_to = datetime.strptime(cfg_val['RPT_DATE_TO'].strip(), "%Y-%m-%d")

    logging.info("Date From: " + date_from.strftime("%Y-%m-%d") + ", To: " + date_to.strftime("%Y-%m-%d"))

    if date_from == date_to:
        proc_date(date_from, pg_conn, df_usr, df_car_db)
    else:
        dt_lst = list(rrule(DAILY, interval=1, dtstart=date_from, until=date_to))
        for dt in dt_lst:
            proc_date(dt, pg_conn, df_usr, df_car_db)

    pg_conn.close()
    logging.info("Daily Summary Report generation completed")
    logging.shutdown()

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("Config file is missing")
        print("Usage : " + sys.argv[0] + " <CONFIG file>")
        sys.exit(1)
    main()
