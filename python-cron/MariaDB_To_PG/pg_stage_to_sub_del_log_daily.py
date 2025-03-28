import sys
import mysql.connector
import psycopg2
import pandas as pd

from datetime import datetime, timedelta
from dateutil.rrule import *
from dateutil.relativedelta import *

from collections import Counter

import configobj
import logging
#import Util

def proc_date(date_val, cfg_val, pg_conn, m_col_lst, pg_col_lst, pg_val_str):
    insert_sub_del_log(date_val, cfg_val, pg_conn, m_col_lst, pg_col_lst, pg_val_str)
    insert_fmsg_info(date_val, cfg_val, pg_conn)
    insert_full_message(date_val, cfg_val, pg_conn)

def vacuum_table(cfg_val, pg_tbl):
    pg_host = cfg_val['PG_HOST']
    pg_port = cfg_val['PG_PORT']
    pg_db = cfg_val['PG_DB']
    pg_user = cfg_val['PG_USER']
    pg_pass = cfg_val['PG_PASS']

    pg_con_tmplt = "host='{host}' port='{port}' dbname='{db}' user='{user}' password='{password}'"
    pg_con_str = pg_con_tmplt.format(host=pg_host, port=pg_port, db=pg_db, user=pg_user, password=pg_pass)
    logging.info("Connecting to Postgres DB: " + pg_host + "@" + pg_db)
    pg_conn = psycopg2.connect(pg_con_str)

    vacum_sql = "VACUUM (FULL, ANALYZE) " + pg_tbl
    pg_conn.set_session(autocommit=True)
    pg_vac_cursor = pg_conn.cursor()
    logging.info("Executing Vacuum SQL: " + vacum_sql)
    pg_vac_cursor.execute(vacum_sql)
    pg_vac_cursor.close()
    pg_conn.close()

def insert_fmsg_info(date_val, cfg_val, pg_conn):
    fetch_limit = int(cfg_val["FETCH_LIMIT"])
    log_prog_count = int(cfg_val["LOG_PROG_COUNT"])
    pg_schema = cfg_val["PG_SCHEMA"]

    date_tbl_str = date_val.strftime("%Y%m%d")

    pg_tbl = pg_schema + ".sub_del_log_" + date_tbl_str
    pg_fmsg_tbl = pg_schema + ".sub_del_log_fmsg_info_" + date_tbl_str
    pg_fmsg_col_lst = "base_msg_id, recv_date, recv_time, cli_id, sub_total_msg_parts, " + \
                 "sub_success, sub_failed, dn_success, dn_failed, " + \
                 "billing_currency, billing_sms_rate, billing_add_fixed_rate"

    pg_fmsg_val_str = "(" + ",".join(["%s" for _ in pg_fmsg_col_lst.split(',')]) + ")"
    pg_fmsg_ins_sql = "insert into " + pg_fmsg_tbl + "(" + pg_fmsg_col_lst + ") values "
    pg_del_cursor = pg_conn.cursor()
    del_sql = "delete from " + pg_fmsg_tbl
    logging.info("Executing Delete SQL: " + del_sql)
    pg_del_cursor.execute(del_sql)
    pg_del_cursor.close()

    #pg_ins_cursor = pg_conn.cursor()
    pg_ins_cursor = pg_conn.cursor()
    #pg_cursor = pg_conn.cursor()
    pg_cursor = pg_conn.cursor('smslog_' + date_tbl_str + '_bad', withhold=True)

    fmsg_msg_part_bad_sql = " select base_msg_id from " + pg_tbl
    fmsg_msg_part_bad_sql += " where sub_msg_part_no > sub_total_msg_parts"
    logging.info("Executing Delete SQL: " + fmsg_msg_part_bad_sql)
    pg_cursor.execute(fmsg_msg_part_bad_sql)
    bmsg_id_ign_rlst = pg_cursor.fetchall()
    bmsg_id_ign = set([r[0] for r in bmsg_id_ign_rlst])

    pg_cursor.close()
    pg_cursor = pg_conn.cursor('smslog_' + date_tbl_str + '_1', withhold=True)
    fmsg_sql = """ select base_msg_id, recv_date, recv_time, cli_id, sub_total_msg_parts::numeric(3),
       (case when sub_sub_ori_sts_code = '400' then 1 else 0 end),
       (case when sub_sub_ori_sts_code = '400' then 0 else 1 end),
       (case when del_dn_ori_sts_code is not null and del_dn_ori_sts_code = '600' then 1 else 0 end),
       (case when del_dn_ori_sts_code is null or del_dn_ori_sts_code = '600' then 0 else 1 end),
       sub_billing_currency as billing_currency,
       (sub_billing_sms_rate + coalesce(del_billing_sms_rate,0)) as sms_rate,
       (sub_billing_add_fixed_rate + coalesce(del_billing_add_fixed_rate ,0)) as dlt_rate """
    fmsg_sql += " from " + pg_tbl + " where sub_total_msg_parts = 1"
    logging.info("Executing Full Message Info SQL for Single Part: " + fmsg_sql)

    fmi_row_count = 0
    log_batch_count = 0

    pg_cursor.execute(fmsg_sql)
    fmsg_rlst = pg_cursor.fetchmany(fetch_limit)

    while fmsg_rlst:
        ins_rlst = []
        for r in fmsg_rlst:
            bmsg_id = r[0]
            if bmsg_id in bmsg_id_ign:
                continue
            ins_rlst.append(r)

        val_txt = b",".join(pg_ins_cursor.mogrify(pg_fmsg_val_str, r) for r in ins_rlst).decode("utf-8")
        pg_ins_cursor.execute(pg_fmsg_ins_sql + val_txt)
        pg_conn.commit()
        ins_count = len(fmsg_rlst)
        fmi_row_count += ins_count
        log_batch_count += ins_count
        if log_batch_count >= log_prog_count:
            logging.info("Full Message Info Single Part Row Count: " + str(fmi_row_count))
            log_batch_count = 0

        fmsg_rlst = pg_cursor.fetchmany(fetch_limit)

    pg_cursor.close()
    pg_conn.commit()
    logging.info("Full Message Info Single Part Total Row Count: " + str(fmi_row_count))

    pg_cursor = pg_conn.cursor('smslog_' + date_tbl_str + '_2', withhold=True)

    fmsg_sql2 = """select sub_msg_part_no, sub_total_msg_parts, sub_sub_ori_sts_code, del_dn_ori_sts_code,
           base_msg_id, recv_date, recv_time, cli_id, sub_billing_currency as billing_currency, 
           (sub_billing_sms_rate + coalesce(del_billing_sms_rate,0)) as sms_rate,
           (sub_billing_add_fixed_rate + coalesce(del_billing_add_fixed_rate ,0)) as dlt_rate"""
    fmsg_sql2 += " from " + pg_tbl + " where sub_total_msg_parts > 1"
    logging.info("Executing Full Message Info SQL for Multi Part: " + fmsg_sql2)

    data_lst = []
    fmi_row_count = 0
    log_batch_count = 0

    pg_cursor.execute(fmsg_sql2)
    cols = ["sub_msg_part_no", "sub_total_msg_parts", "sub_sub_ori_sts_code", "del_dn_ori_sts_code",
            "base_msg_id", "recv_date", "recv_time", "cli_id", "billing_currency", "sms_rate", "dlt_rate"]
    fmsg_rlst2 = pg_cursor.fetchmany(fetch_limit)

    while fmsg_rlst2:
        ins_rlst = []
        for r in fmsg_rlst2:
            bmsg_id = r[4]
            if bmsg_id in bmsg_id_ign:
                continue
            ins_rlst.append(r)

        data_lst.extend(ins_rlst)
        #data_lst.extend(fmsg_rlst2)
        df_fmsg = pd.DataFrame.from_records(data_lst, columns=cols)
        df_fmsg["sub_msg_part_no"] = df_fmsg["sub_msg_part_no"].astype(int)
        df_fmsg["sub_total_msg_parts"] = df_fmsg["sub_total_msg_parts"].astype(int)
        df_fmsg["sms_rate"] = df_fmsg["sms_rate"].astype(float)
        df_fmsg["dlt_rate"] = df_fmsg["dlt_rate"].astype(float)
        df_bmsg_count = df_fmsg[["base_msg_id", "sub_total_msg_parts"]].\
                                groupby(by=["base_msg_id", "sub_total_msg_parts"])["base_msg_id"].\
                                agg(["count"]).reset_index()

        bmsg_id_insert = df_bmsg_count[
                                df_bmsg_count["sub_total_msg_parts"] == df_bmsg_count["count"]
                         ]["base_msg_id"].values
        if len(bmsg_id_insert) > 0:
            df_fmsg_pending = df_fmsg[~df_fmsg["base_msg_id"].isin(bmsg_id_insert)].copy()
            if len(df_fmsg_pending.index) == 0:
                data_lst = []
            else:
                data_lst = list(df_fmsg_pending.itertuples(index=False, name=None))

            df_fmsg_sort = df_fmsg[df_fmsg["base_msg_id"].isin(bmsg_id_insert)].copy(). \
                                    sort_values(by=['base_msg_id', 'sub_msg_part_no'], ascending=True). \
                                    reset_index(drop=True)

            row_ins_lst = []
            row = []
            sub_success = 0
            sub_failed = 0
            del_success = 0
            del_failed = 0
            sms_rate = 0.0
            dlt_rate = 0.0

            for rpt_row in df_fmsg_sort.itertuples(index=False):
                mpart_no = rpt_row[0]
                mtot_parts = rpt_row[1]
                sub_sts_code = rpt_row[2]
                del_sts_code = rpt_row[3]
                bill_curr = rpt_row[8]
                sms_rate += rpt_row[9]
                dlt_rate += rpt_row[10]

                if sub_sts_code == "400":
                    sub_success = 1
                else:
                    sub_failed = 1

                if del_sts_code is not None:
                    if del_sts_code == "600":
                        del_success = 1
                    else:
                        del_failed = 1

                if mpart_no == mtot_parts:
                    row.extend(rpt_row[4:8])
                    row.extend([mtot_parts, sub_success, sub_failed, del_success, del_failed, bill_curr, sms_rate, dlt_rate])
                    row_ins_lst.append(tuple(row))
                    row = []
                    sub_success = 0
                    sub_failed = 0
                    del_success = 0
                    del_failed = 0
                    sms_rate = 0.0
                    dlt_rate = 0.0

            val_txt = b",".join(pg_ins_cursor.mogrify(pg_fmsg_val_str, r) for r in row_ins_lst).decode("utf-8")
            pg_ins_cursor.execute(pg_fmsg_ins_sql + val_txt)
            pg_conn.commit()
            ins_count = len(row_ins_lst)
            fmi_row_count += ins_count
            log_batch_count += ins_count
            if log_batch_count >= log_prog_count:
                logging.info("Full Message Info Multi Part Row Count: " + str(fmi_row_count))
                log_batch_count = 0

        fmsg_rlst2 = pg_cursor.fetchmany(fetch_limit)

    if len(data_lst) > 0:
        df_fmsg = pd.DataFrame.from_records(data_lst, columns=cols)
        df_fmsg["sub_msg_part_no"] = df_fmsg["sub_msg_part_no"].astype(int)
        df_fmsg["sub_total_msg_parts"] = df_fmsg["sub_total_msg_parts"].astype(int)
        df_fmsg["sms_rate"] = df_fmsg["sms_rate"].astype(float)
        df_fmsg["dlt_rate"] = df_fmsg["dlt_rate"].astype(float)
        df_fmsg_sort = df_fmsg.sort_values(by=['base_msg_id', 'sub_msg_part_no'], ascending=True). \
                        reset_index(drop=True)

        row_ins_lst = []
        row = []
        sub_success = 0
        sub_failed = 0
        del_success = 0
        del_failed = 0
        sms_rate = 0.0
        dlt_rate = 0.0
        prv_bmsg_id = ""
        mpart_count = 0
        mtot_parts = 0

        for rpt_row in df_fmsg_sort.itertuples(index=False):
            mtot_parts = rpt_row[1]
            sub_sts_code = rpt_row[2]
            del_sts_code = rpt_row[3]
            bmsg_id = rpt_row[4]
            bill_curr = rpt_row[8]
            sms_rate += rpt_row[9]
            dlt_rate += rpt_row[10]

            if mpart_count > 0 and prv_bmsg_id != bmsg_id:
                row.extend([mtot_parts, sub_success, sub_failed, del_success, del_failed, bill_curr, sms_rate, dlt_rate])
                row_ins_lst.append(tuple(row))
                mpart_count = 0

                row = []
                sub_success = 0
                sub_failed = 0
                del_success = 0
                del_failed = 0
                sms_rate = 0.0
                dlt_rate = 0.0

            if len(row) == 0:
                row.extend(rpt_row[4:8])

            if sub_sts_code == "400":
                sub_success = 1
            else:
                sub_failed = 1

            if del_sts_code is not None:
                if del_sts_code == "600":
                    del_success = 1
                else:
                    del_failed = 1

            mpart_count += 1
            prv_bmsg_id = bmsg_id

        if len(row) > 0:
            row.extend([mtot_parts, sub_success, sub_failed, del_success, del_failed, bill_curr, sms_rate, dlt_rate])
            row_ins_lst.append(tuple(row))

        val_txt = b",".join(pg_ins_cursor.mogrify(pg_fmsg_val_str, r) for r in row_ins_lst).decode("utf-8")
        pg_ins_cursor.execute(pg_fmsg_ins_sql + val_txt)
        #pg_conn.commit()
        fmi_row_count += len(row_ins_lst)

    pg_cursor.close()
    pg_ins_cursor.close()
    pg_conn.commit()
    logging.info("Full Message Info Multi Part Total Row Count: " + str(fmi_row_count))

    vacuum_table(cfg_val, pg_fmsg_tbl)

def insert_full_message(date_val, cfg_val, pg_conn):
    fetch_limit = int(cfg_val["FETCH_LIMIT"])
    log_prog_count = int(cfg_val["LOG_PROG_COUNT"])
    pg_schema = cfg_val["PG_SCHEMA"]
    pg_stage_schema = cfg_val["PG_STAGE_SCHEMA"]

    date_tbl_str = date_val.strftime("%Y%m%d")

    fm_tbl_str = "full_message_" + date_tbl_str
    pg_stage_fm_tbl = pg_stage_schema + "." + fm_tbl_str
    pg_fm_tbl = pg_schema + "." + fm_tbl_str

    logging.info("Postgres Stage Full Message Table: " + pg_stage_fm_tbl)
    logging.info("Postgres Full Message Table: " + pg_fm_tbl)

    pg_col_cursor = pg_conn.cursor()
    fm_cols_sql = """select c.column_name
                     from information_schema.columns c 
                          where  c.TABLE_SCHEMA = '{}'
                          and c.TABLE_NAME = '{}' 
                          and c.column_name not in ('pg_created_ts')
                    order by c.ORDINAL_POSITION"""

    logging.info("Retrieving Column names for the table: {}.{}".format(pg_schema, fm_tbl_str))
    fm_cols_sql = fm_cols_sql.format(pg_schema, fm_tbl_str)
    pg_col_cursor.execute(fm_cols_sql)
    fm_col_rlst = pg_col_cursor.fetchall()
    pg_col_cursor.close()
    fm_cols = [r[0] for r in fm_col_rlst]
    fm_col_lst = ",".join([c for c in fm_cols])

    m_fm_sql = "select " + fm_col_lst + " from " + pg_stage_fm_tbl + " where base_msg_id_seq = 1"

    pg_fm_col_lst = fm_col_lst
    pg_fm_val_str = "(" + ",".join(["%s" for _ in pg_fm_col_lst.split(',')]) + ")"
    pg_fm_sql = "insert into " + pg_fm_tbl + "(" + pg_fm_col_lst + ") values "

    pg_del_cursor = pg_conn.cursor()
    del_sql = "delete from " + pg_fm_tbl
    logging.info("Executing Delete SQL: " + del_sql)
    pg_del_cursor.execute(del_sql)
    pg_del_cursor.close()
    pg_cursor = pg_conn.cursor()

    fm_row_count = 0
    log_batch_count = 0
    logging.info("Insert SQL: " + pg_fm_sql)
    logging.info("Executing Stage Full Message SQL: " + m_fm_sql)
    #pg_stage_cursor = pg_conn.cursor()
    pg_stage_cursor = pg_conn.cursor('stage_' + date_tbl_str, withhold=True)
    pg_stage_cursor.execute(m_fm_sql)
    fm_rlst = pg_stage_cursor.fetchmany(fetch_limit)

    while fm_rlst:
        val_txt = b",".join(pg_cursor.mogrify(pg_fm_val_str, r) for r in fm_rlst).decode("utf-8")
        pg_cursor.execute(pg_fm_sql + val_txt)
        pg_conn.commit()
        ins_count = len(fm_rlst)
        fm_row_count += ins_count
        log_batch_count += ins_count
        if log_batch_count >= log_prog_count:
            logging.info("Full Message Row Count: " + str(fm_row_count))
            log_batch_count = 0

        fm_rlst = pg_stage_cursor.fetchmany(fetch_limit)

    pg_stage_cursor.close()
    pg_cursor.close()
    pg_conn.commit()
    logging.info("Full Message Total Record Count: " + str(fm_row_count))

    vacuum_table(cfg_val, pg_fm_tbl)


def insert_sub_del_log(date_val, cfg_val, pg_conn, m_col_lst, pg_col_lst, pg_val_str):

    fetch_limit = int(cfg_val["FETCH_LIMIT"])
    log_prog_count = int(cfg_val["LOG_PROG_COUNT"])
    pg_schema = cfg_val["PG_SCHEMA"]
    pg_stage_schema = cfg_val["PG_STAGE_SCHEMA"]

    date_str = date_val.strftime("%Y-%m-%d")
    date_tbl_str = date_val.strftime("%Y%m%d")

    pg_tbl = pg_schema + ".sub_del_log_" + date_tbl_str
    pg_stage_sub_tbl = pg_stage_schema + ".submission_" + date_tbl_str
    pg_stage_del_tbl = pg_stage_schema + ".deliveries_" + date_tbl_str
    logging.info("Processing Date: " + date_str)
    logging.info("Stage Submission Table: " + pg_stage_sub_tbl)
    logging.info("Stage Delivery Table: " + pg_stage_del_tbl)
    logging.info("Postgres Sub_Del_Log Table: " + pg_tbl)

    m_sql = "select " + m_col_lst
    m_sql += " from " + pg_stage_sub_tbl + " s left join " + pg_stage_del_tbl
    m_sql += " d on d.recv_date = s.recv_date and d.msg_id = s.msg_id and d.msg_id_seq = s.msg_id_seq "
    m_sql += " where s.msg_id_seq = 1"
    #m_sql += " limit 1000"

    pg_sql = "insert into " + pg_tbl + "(" + pg_col_lst + ") values "
    logging.info("Postgresql SQL: " + pg_sql)

    #batch_limit = int(cfg_val["BATCH_LIMIT"])

    row_count = 0
    log_batch_count = 0
    pg_del_cursor = pg_conn.cursor()
    del_sql = "delete from " + pg_tbl
    logging.info("Executing Delete SQL: " + del_sql)
    pg_del_cursor.execute(del_sql)
    pg_del_cursor.close()

    #pg_cursor = pg_conn.cursor()
    pg_cursor = pg_conn.cursor()
    logging.info("Executing Stage SQL: " + m_sql)
    #pg_stage_cursor = pg_conn.cursor()
    pg_stage_cursor = pg_conn.cursor('stage_' + date_tbl_str, withhold=True)
    pg_stage_cursor.execute(m_sql)
    rlst = pg_stage_cursor.fetchmany(fetch_limit)

    while rlst:
        #pg_cursor = pg_conn.cursor()
        val_txt = b",".join(pg_cursor.mogrify(pg_val_str, r) for r in rlst).decode("utf-8")
        pg_cursor.execute(pg_sql + val_txt)
        pg_conn.commit()
        #pg_cursor.close()
        ins_count = len(rlst)
        row_count += ins_count
        log_batch_count += ins_count
        if log_batch_count >= log_prog_count:
            logging.info("Row Count: " + str(row_count))
            log_batch_count = 0

        rlst = pg_stage_cursor.fetchmany(fetch_limit)

    pg_stage_cursor.close()
    pg_cursor.close()
    pg_conn.commit()
    logging.info("Total Record Count: " + str(row_count))

    vacuum_table(cfg_val, pg_tbl)


def main():
    cfg_fn = sys.argv[1]
    cfg_val = configobj.ConfigObj(cfg_fn)
    log_fn_dtsfx = datetime.now().strftime("%Y%m%d_%H%M%S")

    log_fn = "./log/pg_stage_to_billing_data_" + log_fn_dtsfx + ".log"
    logging.basicConfig(filename=log_fn, level=logging.INFO,
                        format='%(asctime)s %(levelname)-8s %(funcName)-20s %(message)s')
    logging.info("Script to migrate billing data from MariaDB to Postgresql started")

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

    pg_host = cfg_val['PG_HOST']
    pg_port = cfg_val['PG_PORT']
    pg_db = cfg_val['PG_DB']
    pg_user = cfg_val['PG_USER']
    pg_pass = cfg_val['PG_PASS']

    pg_con_tmplt = "host='{host}' port='{port}' dbname='{db}' user='{user}' password='{password}'"
    pg_con_str = pg_con_tmplt.format(host=pg_host, port=pg_port, db=pg_db, user=pg_user, password=pg_pass)
    logging.info("Connecting to Postgres DB: " + pg_host + "@" + pg_db)
    pg_conn = psycopg2.connect(pg_con_str)

    cfg_proc_mode = cfg_val['PROC_MODE'].strip().upper()
    logging.info("Mode: " + cfg_proc_mode)
    if cfg_proc_mode == "DATE":
        #DATE_FROM ==> DEFAULT or YYYY-mm-dd
        cfg_date_from = cfg_val['DATE_FROM'].strip()
        logging.info("Config Date From: " + cfg_date_from)
        if cfg_date_from == "DEFAULT":
            date_from = (datetime.now() - timedelta(days=2))
            date_to = (datetime.now() - timedelta(days=1))
        else:
            date_from = datetime.strptime(cfg_date_from, "%Y-%m-%d")
            date_to = datetime.strptime(cfg_val['DATE_TO'].strip(), "%Y-%m-%d")

    elif cfg_proc_mode == "MONTH":
        # MONTH ==> DEFAULT or YYYY-mm
        cfg_month = cfg_val['MONTH']
        logging.info("Config Month: " + cfg_month)
        if cfg_month == "DEFAULT":
            date_from = datetime.now().replace(day=1, hour=0, minute=0, second=0, microsecond=0)
            date_to = (datetime.now() - timedelta(days=1))
        else:
            date_from = datetime.strptime(cfg_month + "-01", "%Y-%m-%d")
            date_to = (date_from + relativedelta(months=1)) - timedelta(days=1)

    logging.info("Date From: " + date_from.strftime("%Y-%m-%d") + ", To: " + date_to.strftime("%Y-%m-%d"))

    m_cols = ["s.msg_id", "s.base_msg_id"]
    pg_cols = ["msg_id", "base_msg_id"]

    cols_sql = """select concat('{}',mdb_column_name) m_column_name, 
                     pg_column_name 
                     from mdb_to_pg_log_col_map m inner join information_schema.`COLUMNS` c 
                          on c.TABLE_SCHEMA = '{}'
                          and c.TABLE_NAME = '{}' 
                          and m.mdb_column_name = c.COLUMN_NAME 
                         where m.mdb_table_name = '{}'
                         and m.mdb_column_name not in ('msg_id', 'base_msg_id')
                    order by c.ORDINAL_POSITION"""

    sub_tbl_prefix = "submission"
    del_tbl_prefix = "deliveries"

    mdb_name = 'billing'
    mdb_sub_tbl_name = sub_tbl_prefix
    mdb_del_tbl_name = del_tbl_prefix

    db_month_suffix = cfg_val["DB_MONTH_SUFFIX"].lower()
    if db_month_suffix == "true":
        mdb_suffix = date_from.strftime("%Y%m")
        mdb_tbl_suffix = date_from.strftime("%Y%m%d")
        mdb_name = 'billing_' + mdb_suffix
        mdb_sub_tbl_name = sub_tbl_prefix + '_' + mdb_tbl_suffix
        mdb_del_tbl_name = del_tbl_prefix + '_' + mdb_tbl_suffix

    logging.info("Retrieving Column map for the table: {}.{}".format(mdb_name, mdb_sub_tbl_name))
    sub_tbl_prefix = "submission"
    sub_cols_sql = cols_sql.format('s.', mdb_name, mdb_sub_tbl_name, sub_tbl_prefix)
    logging.info(sub_cols_sql)
    my_cursor.execute(sub_cols_sql)
    sub_col_rlst = my_cursor.fetchall()
    m_cols.extend([r[0] for r in sub_col_rlst])
    pg_cols.extend([r[1] for r in sub_col_rlst])

    logging.info("Retrieving Column map for the table: {}.{}".format(mdb_name, mdb_del_tbl_name))
    del_cols_sql = cols_sql.format('d.', mdb_name, mdb_del_tbl_name, del_tbl_prefix)
    logging.info(del_cols_sql)
    my_cursor.execute(del_cols_sql)
    del_col_rlst = my_cursor.fetchall()
    m_cols.extend([r[0] for r in del_col_rlst])
    pg_cols.extend([r[1] for r in del_col_rlst])
    my_cursor.close()

    m_col_lst = ",".join([mc for mc in m_cols])
    pg_col_lst = ",".join([pc for pc in pg_cols])
    pg_val_str = "(" + ",".join(["%s" for _ in pg_cols]) + ")"

    if date_from == date_to:
        proc_date(date_from, cfg_val, pg_conn, m_col_lst, pg_col_lst, pg_val_str)
    else:
        dt_lst = list(rrule(DAILY, interval=1, dtstart=date_from, until=date_to))
        for dt in dt_lst:
            proc_date(dt, cfg_val, pg_conn, m_col_lst, pg_col_lst, pg_val_str)

    pg_conn.close()
    my_conn.close()
    logging.info("Script to migrate billing data from MariaDB to Postgresql completed")
    logging.shutdown()

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("Config file is missing")
        print("Usage : " + sys.argv[0] + " <CONFIG file>")
        sys.exit(1)
    main()
