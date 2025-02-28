import sys
import mysql.connector
import psycopg2
from datetime import datetime, timedelta
from dateutil.rrule import *
from dateutil.relativedelta import *

from collections import Counter

import configobj
import logging
#import Util

def proc_date(date_val, cfg_val, my_conn, pg_conn,
              m_sub_cols, pg_sub_cols,
              m_del_cols, pg_del_cols,
              m_fmsg_cols, pg_fmsg_cols,
              m_dn_plog_cols, pg_dn_plog_cols):

    insert_stage_submission(date_val, cfg_val, my_conn, pg_conn, m_sub_cols, pg_sub_cols)
    insert_stage_deliveries(date_val, cfg_val, my_conn, pg_conn, m_del_cols, pg_del_cols)
    insert_stage_full_msg(date_val, cfg_val, my_conn, pg_conn, m_fmsg_cols, pg_fmsg_cols)
    insert_smslog_dn_post_log(date_val, cfg_val, my_conn, pg_conn, m_dn_plog_cols, pg_dn_plog_cols)

def insert_stage_submission(date_val, cfg_val, my_conn, pg_conn, m_sub_cols, pg_sub_cols):

    fetch_limit = int(cfg_val["FETCH_LIMIT"])
    log_prog_count = int(cfg_val["LOG_PROG_COUNT"])
    pg_schema = cfg_val["PG_SCHEMA"]
    db_month_suffix = cfg_val["DB_MONTH_SUFFIX"].lower()

    m_col_lst = ",".join([mc for mc in m_sub_cols])
    pg_col_lst = ",".join([pc for pc in pg_sub_cols])
    pg_val_str = "(" + ",".join(["%s" for _ in pg_sub_cols]) + ")"

    date_str = date_val.strftime("%Y-%m-%d")
    date_db_str = date_val.strftime("%Y%m")
    date_tbl_str = date_val.strftime("%Y%m%d")
    sub_tbl_n = 'submission'

    m_db = 'billing'
    m_sub_tbl = m_db + '.' + sub_tbl_n

    if db_month_suffix == "true":
        m_db = 'billing_' + date_db_str
        m_sub_tbl = m_db + '.' + sub_tbl_n + '_' + date_tbl_str

    pg_tbl = pg_schema + '.' + sub_tbl_n + '_' + date_tbl_str
    logging.info("Processing Date: " + date_str)
    logging.info("MariaDB Submission Table: " + m_sub_tbl)
    logging.info("Postgres Submission Table: " + pg_tbl)

    m_sql = "select " + m_col_lst + " from " + m_sub_tbl
    m_sql += " where recv_date = %s "
    #m_sql += " limit 1000"

    pg_sql = "insert into " + pg_tbl + "(" + pg_col_lst + ") values "
    logging.info("Postgresql SQL: " + pg_sql)
    #pg_cursor = pg_conn.cursor('sub_del_log_' + date_str)

    #batch_limit = int(cfg_val["BATCH_LIMIT"])

    row_count = 0
    log_batch_count = 0
    msg_id_counter = Counter()
    pg_cursor = pg_conn.cursor()
    trunc_sql = "truncate table " + pg_tbl
    logging.info("Executing Truncate SQL: " + trunc_sql)
    pg_cursor.execute(trunc_sql)

    logging.info("Executing MariaDB SQL: " + m_sql)
    my_cursor = my_conn.cursor()
    my_cursor.execute(m_sql, (date_str,))
    rlst = my_cursor.fetchmany(fetch_limit)

    while rlst:
        pg_data = []
        for r in rlst:
            pg_row = []
            # msg_id_seq = 1
            msg_id = r[0]
            pg_row.append(msg_id)
            # if msg_id in msg_id_counter:
            #     msg_id_seq = msg_id_counter[msg_id]
            #     msg_id_seq += 1
            msg_id_counter.update([msg_id])
            msg_id_seq = msg_id_counter[msg_id]
            pg_row.append(msg_id_seq)
            msg = r[1].replace("\x00", "\uFFFD")
            pg_row.append(msg)
            pg_row.extend(r[2:])
            pg_data.append(tuple(pg_row))

        val_txt = b",".join(pg_cursor.mogrify(pg_val_str, r) for r in pg_data).decode("utf-8")
        pg_cursor.execute(pg_sql + val_txt)
        pg_conn.commit()
        ins_count = len(rlst)
        row_count += ins_count
        log_batch_count += ins_count
        if log_batch_count >= log_prog_count:
            logging.info("Row Count: " + str(row_count))
            log_batch_count = 0

        rlst = my_cursor.fetchmany(fetch_limit)

    pg_conn.commit()
    my_cursor.close()
    pg_cursor.close()
    logging.info("Total Record Count: " + str(row_count))

def insert_stage_deliveries(date_val, cfg_val, my_conn, pg_conn, m_del_cols, pg_del_cols):

    fetch_limit = int(cfg_val["FETCH_LIMIT"])
    log_prog_count = int(cfg_val["LOG_PROG_COUNT"])
    pg_schema = cfg_val["PG_SCHEMA"]
    db_month_suffix = cfg_val["DB_MONTH_SUFFIX"].lower()

    m_col_lst = ",".join([mc for mc in m_del_cols])
    pg_col_lst = ",".join([pc for pc in pg_del_cols])
    pg_val_str = "(" + ",".join(["%s" for _ in pg_del_cols]) + ")"

    date_str = date_val.strftime("%Y-%m-%d")
    date_db_str = date_val.strftime("%Y%m")
    date_tbl_str = date_val.strftime("%Y%m%d")
    del_tbl_n = 'deliveries'

    m_db = 'billing'
    m_del_tbl = m_db + '.' + del_tbl_n

    if db_month_suffix == "true":
        m_db = m_db + '_' + date_db_str
        m_del_tbl = m_db + '.' + del_tbl_n + '_' + date_tbl_str

    pg_tbl = pg_schema + '.' + del_tbl_n + '_' + date_tbl_str
    logging.info("Processing Date: " + date_str)
    logging.info("MariaDB Deliveries Table: " + m_del_tbl)
    logging.info("Postgres Deliveries Table: " + pg_tbl)

    m_sql = "select " + m_col_lst + " from " + m_del_tbl
    m_sql += " where recv_date = %s "
    #m_sql += " limit 1000"

    pg_sql = "insert into " + pg_tbl + "(" + pg_col_lst + ") values "
    logging.info("Postgresql SQL: " + pg_sql)
    #pg_cursor = pg_conn.cursor('sub_del_log_' + date_str)

    #batch_limit = int(cfg_val["BATCH_LIMIT"])

    row_count = 0
    log_batch_count = 0
    msg_id_counter = Counter()
    pg_cursor = pg_conn.cursor()

    trunc_sql = "truncate table " + pg_tbl
    logging.info("Executing Truncate SQL: " + trunc_sql)
    pg_cursor.execute(trunc_sql)

    logging.info("Executing MariaDB SQL: " + m_sql)
    my_cursor = my_conn.cursor()
    my_cursor.execute(m_sql, (date_str,))
    rlst = my_cursor.fetchmany(fetch_limit)

    while rlst:
        pg_data = []
        for r in rlst:
            pg_row = []
            # msg_id_seq = 1
            msg_id = r[0]
            pg_row.append(msg_id)
            # if msg_id in msg_id_counter:
            #     msg_id_seq = msg_id_counter[msg_id]
            #     msg_id_seq += 1
            msg_id_counter.update([msg_id])
            msg_id_seq = msg_id_counter[msg_id]
            pg_row.append(msg_id_seq)
            carr_full_dn = r[1]
            if carr_full_dn is not None:
                carr_full_dn = carr_full_dn.replace("\x00", "\uFFFD")
            pg_row.append(carr_full_dn)
            pg_row.extend(r[2:])
            pg_data.append(tuple(pg_row))

        val_txt = b",".join(pg_cursor.mogrify(pg_val_str, r) for r in pg_data).decode("utf-8")
        pg_cursor.execute(pg_sql + val_txt)
        pg_conn.commit()
        ins_count = len(rlst)
        row_count += ins_count
        log_batch_count += ins_count
        if log_batch_count >= log_prog_count:
            logging.info("Row Count: " + str(row_count))
            log_batch_count = 0

        rlst = my_cursor.fetchmany(fetch_limit)

    pg_conn.commit()
    my_cursor.close()
    pg_cursor.close()
    logging.info("Total Record Count: " + str(row_count))

def insert_stage_full_msg(date_val, cfg_val, my_conn, pg_conn, m_fmsg_cols, pg_fmsg_cols):
    fetch_limit = int(cfg_val["FETCH_LIMIT"])
    log_prog_count = int(cfg_val["LOG_PROG_COUNT"])
    pg_schema = cfg_val["PG_SCHEMA"]
    db_month_suffix = cfg_val["DB_MONTH_SUFFIX"].lower()

    m_col_lst = ",".join([mc for mc in m_fmsg_cols])
    pg_col_lst = ",".join([pc for pc in pg_fmsg_cols])
    pg_val_str = "(" + ",".join(["%s" for _ in pg_fmsg_cols]) + ")"

    date_str = date_val.strftime("%Y-%m-%d")
    date_db_str = date_val.strftime("%Y%m")
    date_tbl_str = date_val.strftime("%Y%m%d")
    fmsg_tbl_n = 'full_message'

    m_db = 'billing'
    m_fmsg_tbl = m_db + '.' + fmsg_tbl_n

    if db_month_suffix == "true":
        m_db = m_db + '_' + date_db_str
        m_fmsg_tbl = m_db + '.' + fmsg_tbl_n + '_' + date_tbl_str

    pg_tbl = pg_schema + '.' + fmsg_tbl_n + '_' + date_tbl_str
    logging.info("Processing Date: " + date_str)
    logging.info("MariaDB Full Message Table: " + m_fmsg_tbl)
    logging.info("Postgres Full Message Table: " + pg_tbl)

    m_sql = "select " + m_col_lst + " from " + m_fmsg_tbl
    m_sql += " where recv_date = %s "
    #m_sql += " limit 1000"

    pg_sql = "insert into " + pg_tbl + "(" + pg_col_lst + ") values "
    logging.info("Postgresql SQL: " + pg_sql)
    #pg_cursor = pg_conn.cursor('sub_del_log_' + date_str)

    #batch_limit = int(cfg_val["BATCH_LIMIT"])

    row_count = 0
    log_batch_count = 0
    msg_id_counter = Counter()
    pg_cursor = pg_conn.cursor()

    trunc_sql = "truncate table " + pg_tbl
    logging.info("Executing Truncate SQL: " + trunc_sql)
    pg_cursor.execute(trunc_sql)

    logging.info("Executing MariaDB SQL: " + m_sql)
    my_cursor = my_conn.cursor()
    my_cursor.execute(m_sql, (date_str,))
    rlst = my_cursor.fetchmany(fetch_limit)

    while rlst:
        pg_data = []
        for r in rlst:
            pg_row = []
            # msg_id_seq = 1
            msg_id = r[0]
            pg_row.append(msg_id)
            # if msg_id in msg_id_counter:
            #     msg_id_seq = msg_id_counter[msg_id]
            #     msg_id_seq += 1
            msg_id_counter.update([msg_id])
            msg_id_seq = msg_id_counter[msg_id]
            pg_row.append(msg_id_seq)
            long_msg = r[1].replace("\x00", "\uFFFD")
            pg_row.append(long_msg)
            pg_row.extend(r[2:])
            pg_data.append(tuple(pg_row))

        #val_txt = b",".join(pg_cursor.mogrify(pg_val_str, r) for r in pg_data).decode("utf-8")
        val_txt = b",".join(pg_cursor.mogrify(pg_val_str, r) for r in pg_data).decode("utf-8")
        pg_cursor.execute(pg_sql + val_txt)
        pg_conn.commit()
        ins_count = len(rlst)
        row_count += ins_count
        log_batch_count += ins_count
        if log_batch_count >= log_prog_count:
            logging.info("Row Count: " + str(row_count))
            log_batch_count = 0

        rlst = my_cursor.fetchmany(fetch_limit)

    pg_conn.commit()
    my_cursor.close()
    pg_cursor.close()
    logging.info("Total Record Count: " + str(row_count))

def insert_smslog_dn_post_log(date_val, cfg_val, my_conn, pg_conn, m_dn_plog_cols, pg_dn_plog_cols):
    fetch_limit = int(cfg_val["FETCH_LIMIT"])
    log_prog_count = int(cfg_val["LOG_PROG_COUNT"])
    pg_schema = "smslog"
    db_month_suffix = cfg_val["DB_MONTH_SUFFIX"].lower()

    m_col_lst = ",".join([mc for mc in m_dn_plog_cols])
    pg_col_lst = ",".join([pc for pc in pg_dn_plog_cols])
    pg_val_str = "(" + ",".join(["%s" for _ in pg_dn_plog_cols]) + ")"

    date_str = date_val.strftime("%Y-%m-%d")
    date_db_str = date_val.strftime("%Y%m")
    date_tbl_str = date_val.strftime("%Y%m%d")
    dn_plog_tbl_n = 'dn_post_log'

    m_db = 'billing'
    m_dn_plog_tbl = m_db + '.' + dn_plog_tbl_n

    if db_month_suffix == "true":
        m_db = m_db + '_' + date_db_str
        m_dn_plog_tbl = m_db + '.' + dn_plog_tbl_n + '_' + date_tbl_str

    pg_tbl = pg_schema + '.' + dn_plog_tbl_n + '_' + date_tbl_str
    logging.info("Processing Date: " + date_str)
    logging.info("MariaDB DN Post Log Table: " + m_dn_plog_tbl)
    logging.info("Postgres DN Post Log Table: " + pg_tbl)

    m_sql = "select date(recv_time) as recv_date, " + m_col_lst + " from " + m_dn_plog_tbl
    #m_sql += " where recv_date = %s "
    #m_sql += " limit 1000"

    pg_sql = "insert into " + pg_tbl + "(" + pg_col_lst + ") values "
    logging.info("Postgresql SQL: " + pg_sql)

    row_count = 0
    log_batch_count = 0
    pg_cursor = pg_conn.cursor()

    trunc_sql = "truncate table " + pg_tbl
    logging.info("Executing Truncate SQL: " + trunc_sql)
    pg_cursor.execute(trunc_sql)

    logging.info("Executing MariaDB SQL: " + m_sql)
    my_cursor = my_conn.cursor()
    my_cursor.execute(m_sql)
    rlst = my_cursor.fetchmany(fetch_limit)

    while rlst:
        pg_data = []
        for r in rlst:
            pg_row = []
            pg_row.extend(r[:11])
            reason = r[11].replace("\x00", "\uFFFD")
            pg_row.append(reason)
            pg_row.extend(r[12:])
            pg_data.append(tuple(pg_row))

        val_txt = b",".join(pg_cursor.mogrify(pg_val_str, r) for r in pg_data).decode("utf-8")
        pg_cursor.execute(pg_sql + val_txt)
        pg_conn.commit()
        ins_count = len(rlst)
        row_count += ins_count
        log_batch_count += ins_count
        if log_batch_count >= log_prog_count:
            logging.info("Row Count: " + str(row_count))
            log_batch_count = 0

        rlst = my_cursor.fetchmany(fetch_limit)

    pg_conn.commit()
    my_cursor.close()
    pg_cursor.close()
    logging.info("Total Record Count: " + str(row_count))

def main():
    cfg_fn = sys.argv[1]
    cfg_val = configobj.ConfigObj(cfg_fn)
    log_fn_dtsfx = datetime.now().strftime("%Y%m%d_%H%M%S")

    log_fn = "./log/mariadb_to_pg_stage_" + log_fn_dtsfx + ".log"
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

    pg_host = cfg_val['PG_HOST']
    pg_port = cfg_val['PG_PORT']
    pg_db = cfg_val['PG_DB']
    pg_user = cfg_val['PG_USER']
    pg_pass = cfg_val['PG_PASS']
    pg_schema = cfg_val['PG_SCHEMA']

    pg_con_tmplt = "host='{host}' port='{port}' dbname='{db}' user='{user}' password='{password}'"
    pg_con_str = pg_con_tmplt.format(host=pg_host, port=pg_port, db=pg_db, user=pg_user, password=pg_pass)
    logging.info("Connecting to Postgres DB: " + pg_host + "@" + pg_db)
    pg_conn = psycopg2.connect(pg_con_str)
    pg_cursor = pg_conn.cursor()

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

    cols_sql = """select c.column_name
                     from information_schema.columns c 
                          where  c.TABLE_SCHEMA = '{}'
                          and c.TABLE_NAME = '{}' 
                          and c.column_name not in 
                          ('msg_id', 'msg_id_seq', 'msg', 'carrier_full_dn', 'pg_created_ts')
                    order by c.ORDINAL_POSITION"""

    tbl_suffix = date_from.strftime("%Y%m%d")
    sub_tbl_prefix = "submission"
    sub_tbl_name = sub_tbl_prefix + "_" + tbl_suffix

    logging.info("Retrieving Column map for the table: {}.{}".format(pg_schema, sub_tbl_name))

    sub_cols_sql = cols_sql.format(pg_schema, sub_tbl_name)
    logging.info(sub_cols_sql)
    pg_cursor.execute(sub_cols_sql)
    sub_col_rlst = pg_cursor.fetchall()

    m_sub_cols = ["msg_id", "msg"]
    pg_sub_cols = ["msg_id", "msg_id_seq", "msg"]
    m_sub_cols.extend([r[0] for r in sub_col_rlst])
    pg_sub_cols.extend([r[0] for r in sub_col_rlst])

    del_tbl_prefix = "deliveries"
    del_tbl_name = del_tbl_prefix + "_" + tbl_suffix

    logging.info("Retrieving Column map for the table: {}.{}".format(pg_schema, del_tbl_name))
    del_cols_sql = cols_sql.format(pg_schema, del_tbl_name)
    logging.info(del_cols_sql)
    pg_cursor.execute(del_cols_sql)
    del_col_rlst = pg_cursor.fetchall()

    m_del_cols = ["msg_id", "carrier_full_dn"]
    pg_del_cols = ["msg_id", "msg_id_seq", "carrier_full_dn"]
    m_del_cols.extend([r[0] for r in del_col_rlst])
    pg_del_cols.extend([r[0] for r in del_col_rlst])

    fmsg_cols_sql = """select c.column_name
                     from information_schema.columns c 
                          where  c.TABLE_SCHEMA = '{}'
                          and c.TABLE_NAME = '{}' 
                          and c.column_name not in ('base_msg_id', 'base_msg_id_seq', 
                                                    'long_msg', 'pg_created_ts')
                    order by c.ORDINAL_POSITION"""

    fmsg_tbl_prefix = "full_message"
    fmsg_tbl_name = fmsg_tbl_prefix + "_" + tbl_suffix

    logging.info("Retrieving Column map for the table: {}.{}".format(pg_schema, fmsg_tbl_name))
    fmsg_cols_sql = fmsg_cols_sql.format(pg_schema, fmsg_tbl_name)
    logging.info(fmsg_cols_sql)
    pg_cursor.execute(fmsg_cols_sql)
    fmsg_col_rlst = pg_cursor.fetchall()
    m_fmsg_cols = ["base_msg_id", "long_msg"]
    pg_fmsg_cols = ["base_msg_id", "base_msg_id_seq", "long_msg"]
    m_fmsg_cols.extend([r[0] for r in fmsg_col_rlst])
    pg_fmsg_cols.extend([r[0] for r in fmsg_col_rlst])

    dn_plog_cols_sql = """select c.column_name
                     from information_schema.columns c 
                          where  c.TABLE_SCHEMA = '{}'
                          and c.TABLE_NAME = '{}' 
                          and c.column_name not in ('recv_date', 'pg_created_ts')
                    order by c.ORDINAL_POSITION"""

    dn_plog_tbl_prefix = "dn_post_log"
    dn_plog_tbl_name = dn_plog_tbl_prefix + "_" + tbl_suffix
    pg_slog_schema = "smslog"
    logging.info("Retrieving Column map for the table: {}.{}".format(pg_slog_schema, dn_plog_tbl_name))
    dn_plog_cols_sql = dn_plog_cols_sql.format(pg_slog_schema, dn_plog_tbl_name)
    logging.info(dn_plog_cols_sql)
    pg_cursor.execute(dn_plog_cols_sql)
    dn_post_col_rlst = pg_cursor.fetchall()
    m_dn_plog_cols = [r[0] for r in dn_post_col_rlst]
    pg_dn_plog_cols = ["recv_date"]
    pg_dn_plog_cols.extend([r[0] for r in dn_post_col_rlst])

    pg_cursor.close()

    if date_from == date_to:
        proc_date(date_from, cfg_val, my_conn, pg_conn,
                  m_sub_cols, pg_sub_cols,
                  m_del_cols, pg_del_cols,
                  m_fmsg_cols, pg_fmsg_cols,
                  m_dn_plog_cols, pg_dn_plog_cols)
    else:
        dt_lst = list(rrule(DAILY, interval=1, dtstart=date_from, until=date_to))
        for dt in dt_lst:
            proc_date(dt, cfg_val, my_conn, pg_conn,
                      m_sub_cols, pg_sub_cols,
                      m_del_cols, pg_del_cols,
                      m_fmsg_cols, pg_fmsg_cols,
                      m_dn_plog_cols, pg_dn_plog_cols)

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
