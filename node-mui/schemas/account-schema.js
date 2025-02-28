const { response400, response403, response500 } = require('./generic');

const tgroupsSchema = {
    summary: 'Get all the DLT template groups ',
    tags: ['Account'],
    querystring: {},
    response: {
        200: {
            type: 'array',
            items: {
                type: 'object',
                properties: {
                    template_group_id: { type: 'integer', example: 1000000000, description: 'template group id' },
                    template_group_name: {
                        type: 'string',
                        example: 'Template Grp Name',
                        description: 'template group name',
                    },
                },
                description: 'Successful response',
            },
        },
        400: response400,
        500: response500,
    },
};

const subServicesSchema = {
    summary: 'Get all the Sub services',
    tags: ['Account'],
    querystring: {},
    response: {
        200: {
            type: 'array',
            items: {
                type: 'object',
                properties: {
                    sub_service_name: { type: 'string', example: 'Campaign UI', description: 'Service Name' },
                    sub_service: { type: 'string', example: 'cm', description: 'short form of the service name' },
                    service: { type: 'string', example: 'sms', description: 'The service' },
                    sub_service_desc: { type: 'string', example: 'Messaging over HTTPS Protocol', description: 'A short desc of the sub service' },
                    },
                },
                description: 'Successful response',
            },
        400: response400,
        500: response500,
    },
};

const anewSchema = {
    summary: 'Create new superadmin account',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['firstname', 'company', 'username', 'mobile', 'email', 'billing_currency', 'conv_type', 'zone_name', 'offset',
                   'bill_type', 'message_type', 'platform_cluster', 'services', 'smsrate', 'dltrate', 'allocated_tgroup_ids',
                   'assigned_tgroup_id', 'sms_priority', 'sms_retry', 'bill_encrypt_type',
                   'domestic_sms_blockout', 'blklist_chk', 'spam_chk', 'dup_chk_req', 'intl_sms_blockout', 'optin_chk_req', 'sales_id',
                   'domestic_special_series_allow', 'req_hex_msg', 'acc_type', 'invoice_based_on', 'is_ildo', 'cli_mid_tag', 'full_message',
                   'camp_name_auto_gen', 'is_16bit_udh', 'ip_validation', 'subusers_reports', 'dnd_chk', 'two_level_auth',
                   'mt_adjust', 'dn_adjust', 'dnd_reject_yn', 'vl_shortner', 'msg_replace_chk', 'is_schedule_allow', 'uc_iden_allow',
                   'is_remove_uc_chars', 'url_smartlink_enable', 'url_track_enabled', 'is_async',
                   'use_default_header', 'use_default_on_header_fail',
                   'acc_default_header', 'considerdefaultlength_as_domestic', 'domestic_tra_blockout_reject', 'timebound_chk_enable',
                   'force_dnd_check', 'msg_retry_available', 'capping_chk_enable', 'acc_route_id', 'expiry_date', 'inactive_login'],
        properties: {
            firstname: { type: 'string', example: 'Firstname', description: 'first name' },
            lastname: { type: 'string', example: 'Lastname', description: 'last name' },
            address: { type: 'string', example: '123, 1 St, MA', description: 'Company address' },
            company: { type: 'string', example: 'Mannaran & Co', description: 'company name' },
            username: { type: 'string', example: 'user1', description: 'login username' },
            mobile: { type: 'string', example: '9465644565', description: 'mobile number without any spl chars.' },
            email: { type: 'string', example: 'user1@mail.com', description: 'email' },
            billing_currency: { type: 'string', example: 'INR', enum: ['AUD', 'CAD', 'CHF', 'EUR', 'GBP', 'INR', 'JPY', 'NZD', 'USD'], description: 'currency code' },
            conv_type: { type: 'integer', enum: [1, 2], example: 1, description: 'accounts billing type Monthly(1), Daily(2)' },
            zone_name: { type: 'string', example: 'Asia/Calcutta', description: 'zone name of the selected tz' },
            offset: { type: 'string', example: '+05:30', description: 'offset value for the selected tz' },
            bill_type: { type: 'integer', enum: [0, 1], example: 0, description: 'accounts billing type Postpaid(0), Prepaid(1)' },
            message_type: { type: 'integer', enum: [0, 1], example: 1, description: 'accounts message type Promotional(0), Transactional(1)' },
            platform_cluster: { type: 'string', enum: ['TRANS', 'BULK', 'OTP'], example: 'BULK', description: 'accounts cluster type' },
            services: {
                type: 'array',
                items: {
                    type: 'object',
                    required: ['service', 'sub_service'],
                    properties: {
                        service: { type: 'string', example: 'sms', description: 'the main service' },
                        sub_service: { type: 'string', example: 'cm', description: 'sub service under the service' },
                    },
                },
            },
            bind_type: { type: 'string', enum: ['TX', 'RX', 'TRX', 'TX~RX', 'TX~RX~TRX'], example: 'TRX', description: 'accounts smpp bind type' },
            max_allowed_connections: { type: 'integer', minimum: 1, maximum: 30, example: 1, description: 'accounts smpp max allowed connections' },
            throttle_limit: { type: 'integer', minimum: 0, maximum: 1000, example: 0, description: 'accounts smpp throttle limit (TPS)' },
            smpp_charset: { type: 'string', example: 'UTF-8', description: 'Charset for SMPP' },
            dlt_entityid_tag: { type: 'integer', example: 1400, description: 'DLT Entity id tag' },
            dlt_templateid_tag: { type: 'integer', example: 1401, description: 'DLT template id tag' },
            dn_date_format: { type: 'string', enum: ['yyMMddHHmm', 'yyMMddHHmmss'], example: 'yyMMddHHmm', description: 'dn date format' },
            wallet: { type: 'number', example: 53345.023411, description: 'Wallet amount. Upto 6 decimal places' },
            smsrate: { type: 'number', example: 1.023456, description: 'SMS Rate(india). Upto 6 decimal places' },
            dltrate: { type: 'number', example: 0.000007, description: 'DLT Rate(india). Upto 6 decimal places' },
            intl_rates: {
                type: 'array',
                items: {
                    type: 'object',
                    required: ['country', 'smsrate'],
                    properties: {
                        country: { type: 'string', example: 'Australia', description: 'the country name' },
                        smsrate: { type: 'number', example: 0.04563, description: 'the rate in his billing currency' },
                    },
                },
            },
            allocated_tgroup_ids: {
                type: 'array',
                items: {
                    type: 'integer',
                    example: 14542255,
                    description: 'id of the allocated dlt template group',
                    minItems: 1,
                },
            },
            assigned_tgroup_id: { type: 'integer', example: 14542255, description: 'assigned template group id' },
            newline_chars: {
                type: 'string',
                example: '##',
                description: 'Char(s) that represents newline in the message',
            },
            sms_priority: { type: 'integer', enum: [0, 1, 2, 4, 5], example: 5, description: 'sms_priority OTP Only(0), High(1), Medium(2), Low(4), Very Low(5)' },
            sms_retry: { type: 'integer', enum: [0, 1, 2, 3], example: 0, description: 'sms_retry_available  No Retry(0), Global(1), Same Route(2), Custom (3)' },
            // client_encrypt: { type: 'integer', enum: [0, 1], example: 0, description: 'client_encrypt   Disable(0), Enabled(1)' },
            bill_encrypt_type: { type: 'integer', enum: [0, 1, 2, 3], example: 0, description: 'bill_encrypt_type  Disable(0), Encrypt Message Only(1), Encrypt Mobile Only(2), Encrypt Both(3)' },
            trai_blockout: { type: 'integer', enum: [0, 1], example: 0, description: 'domestic_promo_trai_blockout_purge  Process next day(0), Discard the Request(1)' },
            domestic_sms_blockout: { type: 'integer', enum: [0, 1, 2], example: 0, description: 'domestic_sms_blockout  Disable(0), Process the message after blockout time(1), Discard the Request(2)' },
            domestic_sms_blockout_start: { type: 'string', example: '22:00', description: 'domestic_sms_blockout_start value in 24Hrs format HH:mm' },
            domestic_sms_blockout_stop: { type: 'string', example: '07:00', description: 'domestic_sms_blockout_stop value in 24Hrs format HH:mm' },
            dnd_chk: { type: 'integer', enum: [0, 1], example: 0, description: 'dnd_chk Disable(0), Enabled(1)' },
            blklist_chk: { type: 'integer', enum: [0, 1, 2, 3, 4, 5, 6, 7], example: 0, description: 'blklist_chk 0 - Disabled, 1 - Gobal Level Only, 2 - User Level Only, 3 - User then Global only, 4 - User then Admin then Super Admin only, 5 - User then Admin then Super Admin then Global, 6 - User then Parent User then Grand Parent User only, 7 - User then Parent User then Grand Parent User then Global' },
            spam_chk: { type: 'integer', enum: [0, 1, 2, 3, 4], example: 2, description: 'spam_chk  Disable(0), Global Only(1), MsgType Only(2), Client Level Only(3), ALL (Client Level, MsgType, Global) (4)' },
            dup_chk_req: { type: 'integer', enum: [0, 1, 2], example: 0, description: 'dup_chk_req  Disable(0), Cust Mid(1), Mobile & Message(2)' },
            dup_chk_interval: { type: 'integer', example: 10, description: 'dup_chk_interval value in minutes' },
            intl_sms_blockout: { type: 'integer', enum: [0, 1, 2], example: 0, description: 'intl_sms_blockout  Disable(0), Process the message after blockout time(1), Discard the Request(2)' },
            intl_sms_blockout_start: { type: 'string', example: '23:30', description: 'intl_sms_blockout_start value in 24Hrs format HH:mm' },
            intl_sms_blockout_stop: { type: 'string', example: '06:00', description: 'intl_sms_blockout_stop value in 24Hrs format HH:mm' },
            optin_chk_req: { type: 'integer', enum: [0, 1], example: 0, description: 'optin_chk_req  Disable(0), Enabled(1)' },
            sales_id: { type: 'string', example: '1234', description: 'sales person id' },
            domestic_special_series_allow: { type: 'integer', enum: [0, 1], example: 0, description: 'domestic_special_series_allow  Disable(0), Enabled(1)' },
            req_hex_msg: { type: 'integer', enum: [0, 1], example: 0, description: 'req_hex_msg  Disable(0), Enabled(1)' },
            acc_type: { type: 'integer', enum: [1, 2, 0], example: 1, description: 'acc_type  Testing(1), Demo(2), Production(0)' },
            invoice_based_on: { type: 'integer', enum: [0, 1], example: 0, description: 'invoice_based_on  Submission(0), Deliveries(1)' },
            is_ildo: { type: 'integer', enum: [0, 1], example: 0, description: 'is_ildo  Non ILDO(0), ILDO(1)' },
            cli_mid_tag: { type: 'integer', example: 1402, description: 'Client Message Id Tag ' },
            is_16bit_udh: { type: 'integer', enum: [0, 1], example: 0, description: '8bitUDH(0), 16bitUDH(1)' },
            ip_validation: { type: 'integer', enum: [0, 1], example: 0, description: 'Disabled(0), Enabled(1)' },
            ip_list: { type: 'string', example: '192.168.1.101,192.168.1.102', description: 'IP list to be whitelisted' },
            full_message: { type: 'integer', enum: [0, 1], example: 0, description: 'Full message in report download. Disabled(0), Enabled(1)' },
            camp_name_auto_gen: { type: 'integer', enum: [0, 1], example: 0, description: 'Auto generation of campaign name. Disabled(0), Enabled(1)' },
            subusers_reports: { type: 'integer', enum: [0, 1], example: 0, description: 'Subusers report viewing. Disabled(0), Enabled(1)' },
            two_level_auth: { type: 'integer', enum: [0, 1], example: 0, description: 'Two Level Authentication Disable(0), Enabled(1)' },
            mt_adjust: { type: 'integer', enum: [0, 1], example: 0, description: 'mt_adjust Disable(0), Enabled(1)' },
            dn_adjust: { type: 'integer', enum: [0, 1], example: 0, description: 'dn_adjust Disable(0), Enabled(1)' },
            dnd_reject_yn: { type: 'integer', enum: [0, 1], example: 0, description: 'dnd_reject_yn Disable(0), Enabled(1)' },
            vl_shortner: { type: 'integer', enum: [0, 1], example: 0, description: 'vl_shortner Disable(0), Enabled(1)' },
            msg_replace_chk: { type: 'integer', enum: [0, 1], example: 0, description: 'msg_replace_chk Disable(0), Enabled(1)' },
            is_schedule_allow: { type: 'integer', enum: [0, 1], example: 0, description: 'is_schedule_allow Disable(0), Enabled(1)' },
            uc_iden_allow: { type: 'integer', enum: [0, 1], example: 0, description: 'uc_iden_allow Disable(0), Enabled(1)' },
            uc_iden_char_len: { type: 'integer', example: 2, description: 'UC identification char length' },
            uc_iden_occur: { type: 'integer', example: 2, description: 'UC identification occurence' },
            is_remove_uc_chars: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
            url_smartlink_enable: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
            url_track_enabled: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
            vl_shortcode_len: { type: 'integer', enum: [5, 6], example: 5, description: '5/6 char length short code' },
            is_async: { type: 'integer', enum: [0, 1], example: 1, description: 'Sync(0), Async(1)' },
            use_default_header: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
            use_default_on_header_fail: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
            acc_default_header: { type: 'string', example: 'DEVTST', description: 'account default header' },
            considerdefaultlength_as_domestic: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
            domestic_tra_blockout_reject: { type: 'integer', enum: [0, 1], example: 1, description: 'Accept During TRAI Blockout at Interface level(0), Reject During TRAI Blockout  at Interface level(1)' },
            timebound_chk_enable: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
            timebound_interval: { type: 'integer', example: 0, description: 'timebound interval' },
            timebound_max_count_allow: { type: 'integer', example: 0, description: 'timebound max count allow' },
            dnd_pref: { type: 'integer', example: 0, description: 'dnd preferences' },
            force_dnd_check: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
            msg_retry_available: { type: 'integer', enum: [0, 1, 2, 3], example: 2, description: 'No Retry(0), Single Part Only(1), Single and Multipart(2) Partial Failed retry(3)' },
            capping_chk_enable: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
            capping_interval_type: { type: 'integer', enum: [0, 1, 2, 3, 4, 5, 6], example: 0, description: 'None(0), Minute(1), Hour(2), Day(3), Week(4), Month(5), Year(6)' },
            capping_interval: { type: 'integer', example: 0, description: 'capping interval' },
            capping_max_count_allow: { type: 'integer', example: 0, description: 'capping max count allow' },
            acc_route_id: { type: 'string', example: 'WW', description: 'account default route id' },
            credit_check: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
            credit_limit: { type: 'number', example: 123456.0234, description: 'Max credit limit for postpaid customers. Upto 4 decimal places' },
            expiry_date: { type: 'string', example: '2023-12-31', description: 'account expiry date' },
            inactive_login: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' }
        },
        dependencies: {
            domestic_sms_blockout_start: ['domestic_sms_blockout', 'domestic_sms_blockout_stop'],
            domestic_sms_blockout_stop: ['domestic_sms_blockout', 'domestic_sms_blockout_start'],
            intl_sms_blockout_start: ['intl_sms_blockout', 'intl_sms_blockout_stop'],
            intl_sms_blockout_stop: ['intl_sms_blockout', 'intl_sms_blockout_start'],
            dup_chk_interval: ['dup_chk_req'],
            ip_list: ['ip_validation'],
        },
    },
    response: {
        200: {
            type: 'object',
            properties: {
                statusCode: {
                    type: 'integer',
                    enum: [200, -600, -601],
                    example: '200 / -600 / -601',
                    description: '+ve statuscode-> success, -ve statuscode-> failed',
                },
                message: {
                    type: 'string',
                    example: 'Account has been created successfully / Username is not unique / Not enough wallet balance',
                },
            },
            description: 'Check for statuscode',
        },
        400: response400,
        403: response403,
        500: response500,
    },
};

const ainfoSchema = {
    summary: 'Get account info for a particular cli id',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['cli_id'],
        properties: {
            cli_id: {
                type: 'integer',
                example: 6000000200000021,
                description: 'client id of the account',
            },
        },
    },
    response: {
        200: {
            type: 'object',
            properties: {

                total_admins: { type: 'integer', default: 0, example: 12, description: 'ideally this is zero for an admin account' },
                total_users: { type: 'integer', default: 0, example: 2, description: 'total user accounts under him' },
                firstname: { type: 'string', example: 'Firstname', description: 'first name' },
                lastname: { type: 'string', example: 'Lastname', description: 'last name' },
                user_type: { type: 'integer', example: '1', description: '0-superadmin 1-admin 2-user' },
                bill_type: { type: 'integer', example: '1', description: '0-Postpaid, 1-Prepaid' },
                msg_type: { type: 'integer', example: '1', description: '0-Promotional, 1-Tansactional' },
                acc_status: {
                    type: 'integer',
                    example: '1',
                    description: '0 for active, 1 for suspended and 2 for deactivated',
                },
                company: { type: 'string', example: 'Mannaran & Co', description: 'company name' },
                address: { type: 'string', example: 'address 1', description: 'address of the company' },
                user: { type: 'string', example: 'user1', description: 'login username' },
                email: { type: 'string', example: 'user1@mail.com', description: 'email' },
                mobile: { type: 'string', example: '9465644565', description: 'mobile number without any spl chars.' },
                time_zone: { type: 'string', example: 'Asia/Calcutta', description: 'zone name of the selected tz' },
                time_zone_abbr: {
                    type: 'string',
                    example: 'IST',
                    description: 'zone name abbreviation of the selected tz',
                },
                time_offset: { type: 'string', example: '+05:30', description: 'offset value for the selected tz' },
                message_type: { type: 'integer', enum: [0, 1], example: 1, description: 'accounts message type Promotional(0), Transactional(1)' },
                platform_cluster: { type: 'string', enum: ['TRANS', 'BULK', 'OTP'], example: 'BULK', description: 'accounts cluster type' },
                allocated_tgroups: {
                    type: 'array',
                    description: 'List of DLT template groups to be shown under allocated section',
                    items: {
                        type: 'object',
                        example: { template_group_id: 1000000000, template_group_name: 'Briant Template Grou' },
                        description: 'Allocated dlt template groups',
                        properties: {
                            template_group_id: {
                                type: 'integer',
                                example: 1000000000,
                                description: 'template group id',
                            },
                            template_group_name: {
                                type: 'string',
                                example: 'Briant Template Group',
                                description: 'template group name',
                            },
                        },
                    },
                },
                dlt_templ_grp_id: { type: 'integer', example: 14542255, description: 'assigned template group id' },
                dlt_templ_grp_name: { type: 'string', example: 14542255, description: 'assigned template group name' },
                bind_type: { type: 'string', default: process.env.BIND_TYPE, enum: ['TX', 'RX', 'TRX', 'TX~RX', 'TX~RX~TRX'], example: 'TRX', description: 'accounts smpp bind type' },
                max_allowed_connections: { type: 'integer', default: process.env.MAX_ALLOWED_CONNECTIONS, minimum: 1, maximum: 30, example: 1, description: 'accounts smpp max allowed connections' },
                throttle_limit: { type: 'integer', default: process.env.THROTTLE_LIMIT, minimum: 0, maximum: 1000, example: 0, description: 'accounts smpp throttle limit (TPS)' },
                smpp_charset: { type: 'string', default: process.env.SMPP_CHARSET, example: 'UTF-8', description: 'Charset for SMPP' },
                dlt_entityid_tag: { type: 'integer', default: process.env.DLT_ENTITYID_TAG, example: 1400, description: 'DLT Entity id tag' },
                dlt_templateid_tag: { type: 'integer', default: process.env.DLT_TEMPLATEID_TAG, example: 1401, description: 'DLT template id tag' },
                wallet: { type: 'number', example: 53345.0234, description: 'Wallet amount. Upto 6 decimal places' },
                smsrate: { type: 'number', example: 1.0234, description: 'SMS Rate. Upto 6 decimal places' },
                dltrate: { type: 'number', example: 0.0025, description: 'DLT Rate. Upto 6 decimal places' },
                billing_currency: { type: 'string', example: 'INR', description: 'currency assigned for the account' },
                sms_left: { type: 'integer', example: 1034, description: 'total sms that can be sent for the wallet balance' },
                newline_replace_char: {
                    type: 'string',
                    example: '##',
                    description: 'Char(s) that represents newline in the message',
                },
                services: {
                    type: 'array',
                    items: {
                        type: 'object',
                        properties: {
                            service: { type: 'string', example: 'sms', description: 'the main service' },
                            sub_service: {
                                type: 'string',
                                example: 'api',
                                description: 'sub service under the service',
                            },
                            sub_service_name: {
                                type: 'string',
                                example: 'API',
                                description: 'user readable sub service name',
                            },
                            sub_service_desc: {
                                type: 'string',
                                example: 'Messaging over HTTPS Protocol',
                                description: 'description of the sub service',
                            },
                            enabled_yn: { type: 'integer', example: 0, description: '0-no 1-yes' },
                        },
                    },
                },
                intl_rates: {
                    type: 'array',
                    items: {
                        type: 'object',
                        properties: {
                            country: { type: 'string', example: 'India', description: 'name of the country' },
                            smsrate: { type: 'number', example: '123.3435', description: 'intl sms rate defined for the country' },

                        },
                    },
                },
                has_row_yn: { type: 'integer', example: 0, description: 'to show/hide ROW rate section when intl service is enabled. 0-no 1-yes' },
                created_ts: { type: 'string', example: 'Created date & time e.g 30-06-2021 17:44:29 IST' },
                modified_ts: { type: 'string', example: 'Last modified e.g 30-06-2021 17:44:29 IST' },
                created_ts_unix: { type: 'integer', example: 1318874398 },
                modified_ts_unix: { type: 'integer', example: 1318874398 },
                sms_priority: { type: 'integer', enum: [0, 1, 2, 4, 5], description: '0 - OTP, 1 - High, 2 - Medium , 4-Low, 5- Very Low' },
                sms_retry_available: { type: 'integer', enum: [0, 1, 2, 3], description: '0 - No retry, 1 - Global , 2-Same route, 3- Custom' },
                bill_encrypt_type: { type: 'integer', enum: [0, 1, 2, 3], description: '0 - Disable, 1 - Encrypt Message Only , 2-Encrypt Mobile Only, 3-Encrypt both' },
                trai_blockout: { type: 'integer', enum: [0, 1], description: '0 - Process next day, 1 - Discard the request' },
                domestic_sms_blockout: { type: 'integer', enum: [0, 1, 2], description: '0 - Disable, 1 - Process the message after blockout time ,2 - Discard the request' },
                domestic_sms_blockout_start: { type: 'string', example: '23:59', description: 'hh:mm 24 hour format separated by :' },
                domestic_sms_blockout_stop: { type: 'string', example: '23:59', description: 'hh:mm 24 hour format separated by :' },
                dnd_pref: { type: 'integer', enum: [0, 1], description: '0 - Disable, 1 - Enabled' },
                blklist_chk: { type: 'integer', enum: [0, 1], description: '0 - Disable ,1 - Enabled' },
                spam_chk: { type: 'integer', description: 'spam check selection' },
                dup_chk_req: { type: 'integer', enum: [0, 1, 2], description: '0 - Disable, 1 - Cust Mid ,2 - Mobile & Message' },
                dup_chk_interval: { type: 'integer', example: 59, description: 'interval in minutes' },
                intl_sms_blockout: { type: 'integer', enum: [0, 1, 2], description: '0 - Disable, 1 - Process the message after blockout time, 2 - Discard the request' },
                intl_sms_blockout_start: { type: 'string', example: '23:59', description: 'hh:mm 24 hour format separated by :' },
                intl_sms_blockout_stop: { type: 'string', example: '23:59', description: 'hh:mm 24 hour format separated by :' },
                optin_chk_req: { type: 'integer', default: 0, enum: [0, 1], description: '0 - Disable, 1 - Enable' },
                sales_id: { type: 'integer', default: null, example: 1 },
                sales_name: { type: 'string', default: null, example: 'Johnathan' },
                domestic_special_series_allow: { type: 'integer', enum: [0, 1], example: 0, description: 'domestic_special_series_allow  Disable(0), Enabled(1)' },
                req_hex_msg: { type: 'integer', enum: [0, 1], example: 0, description: 'req_hex_msg  Disable(0), Enabled(1)' },
                acc_type: { type: 'integer', enum: [1, 2, 0], example: 1, description: 'acc_type  Testing(1), Demo(2), Production(0)' },
                invoice_based_on: { type: 'integer', enum: [0, 1], example: 0, description: 'invoice_based_on  Submission(0), Deliveries(1)' },
                is_ildo: { type: 'integer', enum: [0, 1], example: 0, description: 'is_ildo  Non ILDO(0), ILDO(1)' },
                cli_mid_tag: { type: 'integer', default: 1402, example: 1402, description: 'Client Message Id Tag ' },
                ip_validation: { type: 'integer', enum: [0, 1], example: 0, description: 'Disabled(0), Enabled(1)' },
                ip_list: { type: 'string', example: '192.168.1.101,192.168.1.102', description: 'IP list to be whitelisted' },
                is_16bit_udh: { type: 'integer', enum: [0, 1], example: 0, description: 'Disabled(0), Enabled(1)' },
                considerdefaultlength_as_domestic: { type: 'integer', enum: [0, 1], example: 0, description: 'Disabled(0), Enabled(1)' },
                full_message: { type: 'integer', enum: [0, 1], example: 0, description: 'Full message in report download. Disabled(0), Enabled(1)' },
                camp_name_auto_gen: { type: 'integer', enum: [0, 1], example: 0, description: 'Auto generation of campaign name. Disabled(0), Enabled(1)' },
                billing_currency_conv_type: { type: 'integer', enum: [1, 2], example: 1, description: 'accounts billing type Monthly(1), Daily(2)' },
                subusers_reports: { type: 'integer', enum: [0, 1], example: 0, description: 'Subusers report viewing. Disabled(0), Enabled(1)' },
                two_level_auth: { type: 'integer', enum: [0, 1], example: 0, description: 'Two Level Authentication Disable(0), Enabled(1)' },
                mt_adjust: { type: 'integer', enum: [0, 1], example: 0, description: 'mt_adjust Disable(0), Enabled(1)' },
                dn_adjust: { type: 'integer', enum: [0, 1], example: 0, description: 'dn_adjust Disable(0), Enabled(1)' },
                dnd_reject_yn: { type: 'integer', enum: [0, 1], example: 0, description: 'dnd_reject_yn Disable(0), Enabled(1)' },
                vl_shortner: { type: 'integer', enum: [0, 1], example: 0, description: 'vl_shortner Disable(0), Enabled(1)' },
                msg_replace_chk: { type: 'integer', enum: [0, 1], example: 0, description: 'msg_replace_chk Disable(0), Enabled(1)' },
                is_schedule_allow: { type: 'integer', enum: [0, 1], example: 0, description: 'is_schedule_allow Disable(0), Enabled(1)' },
                uc_iden_allow: { type: 'integer', enum: [0, 1], example: 0, description: 'uc_iden_allow Disable(0), Enabled(1)' },
                uc_iden_char_len: { type: 'integer', example: 2, description: 'UC identification char length' },
                uc_iden_occur: { type: 'integer', example: 2, description: 'UC identification occurence' },
                is_remove_uc_chars: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
                url_smartlink_enable: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
                url_track_enabled: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
                vl_shortcode_len: { type: 'integer', enum: [5, 6], example: 0, description: '5/6 char length short code' },
                is_async: { type: 'integer', enum: [0, 1], example: 1, description: 'Sync(0), Async(1)' },
                use_default_header: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
                use_default_on_header_fail: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
                acc_default_header: { type: 'string', example: 'DEVTST', description: 'account default header' },
                domestic_tra_blockout_reject: { type: 'integer', enum: [0, 1], example: 1, description: 'Accept During TRAI Blockout at Interface level(0), Reject During TRAI Blockout  at Interface level(1)' },
                timebound_chk_enable: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
                timebound_interval: { type: 'integer', example: 0, description: 'timebound interval' },
                timebound_max_count_allow: { type: 'integer', example: 0, description: 'timebound max count allow' },
                force_dnd_check: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
                msg_retry_available: { type: 'integer', enum: [0, 1, 2, 3], example: 2, description: 'No Retry(0), Single Part Only(1), Single and Multipart(2) Partial Failed retry(3)' },
                capping_chk_enable: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
                capping_interval_type: { type: 'integer', enum: [0, 1, 2, 3, 4, 5, 6], example: 0, description: 'None(0), Minute(1), Hour(2), Day(3), Week(4), Month(5), Year(6)' },
                capping_interval: { type: 'integer', example: 0, description: 'capping interval' },
                capping_max_count_allow: { type: 'integer', example: 0, description: 'capping max count allow' },
                acc_route_id: { type: 'string', example: 'WW', description: 'account default route id' },
                credit_check: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
                credit_limit: { type: 'number', example: 123456.0234, description: 'Max credit limit for postpaid customers. Upto 4 decimal places' },
                dnd_chk: { type: 'integer', enum: [0, 1], example: 0, description: 'dnd_chk Disable(0), Enabled(1)' },
                dn_date_format: { type: 'string', enum: ['yyMMddHHmm', 'yyMMddHHmmss'], example: 'yyMMddHHmm', description: 'dn date format' },
                dn_expiry_in_sec: { type: 'number', example: 21600 },
                credit_limit_available: { type: 'integer', example: 100, description: 'amount available out of credit limit' },
                expiry_date: { type: 'string', example: '2024-03-30 17:44:29', description: 'account expiry date in account timezone' },
                expiry_ts_unix: { type: 'integer', example: 1318874398 },
                expiry_ts: { type: 'string', example: 'Expiry date & time e.g Mar 30, 2024 17:44:29 IST' },
                inactive_login: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' }
            },
            description: 'Successful response',
        },
        400: response400,
        500: response500,
    },
};

const accountsSchema = {
    summary: 'Get all accounts in the system',
    tags: ['Account'],
    querystring: {
        type: 'object',
        required: [],
        properties: {},
    },
    response: {
        200: {
            type: 'array',
            items: {
                type: 'object',
                properties: {
                    cli_id: { type: 'integer', example: 6000000200000029, description: 'id for this user' },
                    firstname: { type: 'string', example: 'Firstname', description: 'first name' },
                    lastname: { type: 'string', example: 'Lastname', description: 'last name' },
                    user_type: { type: 'integer', example: '1', description: '0-superadmin 1-admin 2-user' },
                    acc_status: {
                        type: 'integer',
                        example: '1',
                        description: '0 for active, 1 for suspended and 2 for deactivated',
                    },
                    user: { type: 'string', example: 'user1', description: 'login username' },
                    email: { type: 'string', example: 'user1@mail.com', description: 'email' },
                    mobile: {
                        type: 'string',
                        example: '9465644565',
                        description: 'mobile number without any spl chars.',
                    },
                    created_ts: { type: 'string', example: 'Created date & time e.g 30-06-2021 17:44:29 IST' },
                    created_ts_unix: { type: 'integer', example: 1318874398 },
                    bill_type: { type: 'string', example: 'Prepaid', description: 'Prepaid' },
                    wallet: { type: 'string', example: '123456.123456', description: 'Wallet balance' },
                    billing_currency: { type: 'string', example: 'INR', description: 'billing_currency' },
                    expiry_date: { type: 'string', example: 'Expiry date & time e.g Mar 30, 2024 17:44:29', description: 'account expiry date in IST' },
                    expiry_ts_unix: { type: 'integer', example: 1318874398 },
                    acc_status_desc: { type: 'string', example: 'Active/Deactivated/Suspended/Expired' }
                },
                description: 'Successful response',
            },
        },
        400: response400,
        500: response500,
    },
};

const astatsSchema = {
    summary: 'Get account statistics',
    tags: ['Account'],
    querystring: {
        type: 'object',
        required: [],
        properties: {},
    },
    response: {
        200: {
            type: 'object',
            properties: {

                total_accounts: { type: 'integer', example: 14, description: 'total accounts under him' },
                total_sa: { type: 'integer', example: 14, description: 'total super admin accounts under him' },
                total_admins: { type: 'integer', example: 12, description: 'total admin accounts under him' },
                total_users: { type: 'integer', example: 2, description: 'total user accounts under him' },
                total_active: { type: 'integer', example: 1, description: 'total accounts which are active' },
                total_inactive: { type: 'integer', example: 2, description: 'total accounts which are deactivated' },
                total_groups: { type: 'integer', example: 2, description: 'total groups created by this user' },
                total_templates: { type: 'integer', example: 2, description: 'total templates created by this user' },
            },
            description: 'Successful response',
        },
        400: response400,
        500: response500,
    },
};

const unameuniqueSchema = {
    summary: 'Checks if the username is unique',
    tags: ['Account'],
    querystring: {
        type: 'object',
        required: ['uname'],
        properties: { uname: { type: 'string', maxLength: 50, minLength: 3, description: 'username' } },
    },
    response: {
        200: {
            type: 'object',
            properties: { isUnique: { type: 'boolean', example: true, description: 'false-not unique true-unique' } },
            description: 'Successful response',
        },
        400: response400,
        500: response500,
    },
};

const accountProfileInfoSchema = {
    summary: 'Update profile info',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['cli_id', 'firstname', 'lastname', 'company'],
        properties: {
            cli_id: {
                type: 'integer',
                example: 6000000200000021,
                description: 'client id of the account being edited',
            },
            firstname: {
                type: 'string',
                example: 'Firstname',
                description: 'first name',
            },
            lastname: {
                type: 'string',
                example: 'Lastname',
                description: 'last name',
            },
            address: {
                type: 'string',
                example: '123, 1 St, MA',
                description: 'company address',
            },
            company: {
                type: 'string',
                example: 'Mannaran & Co',
                description: 'company name',
            },
        },
    },
    response: {
        200: {
            type: 'object',
            properties: {
                statusCode: { type: 'integer', example: 200, description: 'Update Successful' },
                message: { type: 'string', example: 'Account has been updated successfully' },
            },
            description: 'Successful',
        },
        400: response400,
        403: response403,
        500: response500,
    },
};

const accountSettingsSchema = {
    summary: 'Update account settings',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['cli_id', 'mobile', 'email', 'zone_name', 'offset', 'message_type', 'platform_cluster', 'sms_priority', 'acc_type', 'invoice_based_on', 'use_default_header', 'use_default_on_header_fail', 'acc_route_id',
        'acc_default_header', 'expiry_date'],
        properties: {
            cli_id: { type: 'integer', example: 6000000200000021, description: 'client id of the account being edited' },
            mobile: { type: 'string', example: '9465644565', description: 'mobile number without any spl chars.' },
            email: { type: 'string', example: 'user1@mail.com', description: 'email' },
            zone_name: { type: 'string', example: 'Asia/Calcutta', description: 'zone name of the selected tz' },
            offset: { type: 'string', example: '+05:30', description: 'offset value for the selected tz' },
            bill_type: { type: 'integer', enum: [0, 1], example: 0, description: 'accounts billing type Postpaid(0), Prepaid(1)' },
            message_type: { type: 'integer', enum: [0, 1], example: 1, description: 'accounts message type Promotional(0), Transactional(1)' },
            platform_cluster: { type: 'string', enum: ['TRANS', 'BULK', 'OTP'], example: 'BULK', description: 'accounts cluster type' },
            sms_priority: { type: 'integer', enum: [0, 1, 2, 4, 5], example: 5, description: 'sms_priority OTP(0), High(1), Medium(2), Low(4), Very Low(5)' },
            acc_type: { type: 'integer', enum: [1, 2, 0], example: 1, description: 'acc_type  Testing(1), Demo(2), Production(0)' },
            invoice_based_on: { type: 'integer', enum: [0, 1], example: 0, description: 'invoice_based_on  Submission(0), Deliveries(1)' },
            use_default_header: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
            use_default_on_header_fail: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
            acc_default_header: { type: 'string', example: 'DEVTST', description: 'account default header' },
            acc_route_id: { type: 'string', example: 'WW', description: 'account default route id' },
            expiry_date: { type: 'string', example: '2023-12-31', description: 'account expiry date' }
        },
    },
    response: {
        200: {
            type: 'object',
            properties: {
                statusCode: { type: 'integer', example: 200, description: 'Update Successful' },
                message: { type: 'string', example: 'Account has been updated successfully' },
            },
            description: 'Successful',
        },
        400: response400,
        403: response403,
        500: response500,
    },
};

const accountServicesSchema = {
    summary: 'Update services',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['cli_id', 'services'],
        properties: {
            cli_id: {
                type: 'integer',
                example: 6000000200000021,
                description: 'client id of the account',
            },
            services: {
                type: 'array',
                items: {
                    type: 'object',
                    required: ['service', 'sub_service'],
                    properties: {
                        service: { type: 'string', example: 'sms', description: 'the main service' },
                        sub_service: { type: 'string', example: 'cm', description: 'sub service under the service' },
                    },
                },
            },
            row_sms_rate: { type: 'number', example: 0.023011, description: 'sms rate' },
        },
    },
    response: {
        200: {
            type: 'object',
            properties: {
                statusCode: { type: 'integer', example: 200, description: 'Update Successful' },
                message: { type: 'string', example: 'Account has been updated successfully' },
            },
            description: 'Update Successful',
        },
        400: response400,
        403: response403,
        500: response500,

    },
};

const accountSMPPSettingsSchema = {
    summary: 'Update smpp settings',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['cli_id', 'bind_type', 'max_allowed_connections', 'throttle_limit', 'smpp_charset', 'dlt_entityid_tag', 'dlt_templateid_tag', 'cli_mid_tag', 'dn_date_format'],
        properties: {
            cli_id: {
                type: 'integer',
                example: 6000000200000021,
                description: 'client id of the account',
            },
            bind_type: { type: 'string', enum: ['TX', 'RX', 'TRX', 'TX~RX', 'TX~RX~TRX'], example: 'TRX', description: 'accounts smpp bind type' },
            max_allowed_connections: { type: 'integer', minimum: 1, maximum: 30, example: 1, description: 'accounts smpp max allowed connections' },
            throttle_limit: { type: 'integer', minimum: 0, maximum: 1000, example: 0, description: 'accounts smpp throttle limit (TPS)' },
            smpp_charset: { type: 'string', example: 'UTF-8', description: 'Charset for SMPP' },
            dlt_entityid_tag: { type: 'integer', example: 1400, description: 'DLT Entity id tag' },
            dlt_templateid_tag: { type: 'integer', example: 1401, description: 'DLT template id tag' },
            cli_mid_tag: { type: 'integer', example: 1402, description: 'Client Message Id Tag ' },
            dn_date_format: { type: 'string', enum: ['yyMMddHHmm', 'yyMMddHHmmss'], example: 'yyMMddHHmm', description: 'dn date format' },
        },
    },
    response: {
        200: {
            type: 'object',
            properties: {
                statusCode: { type: 'integer', example: 200, description: 'Update Successful' },
                message: { type: 'string', example: 'Account has been updated successfully' },
            },
            description: 'Update Successful',
        },
        400: response400,
        403: response403,
        500: response500,
    },
};

const accountDLTGroupsSchema = {
    summary: 'Update dlt template groups',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['cli_id', 'allocated_tgroup_ids', 'assigned_tgroup_id'],
        properties: {
            cli_id: {
                type: 'integer',
                example: 6000000200000021,
                description: 'client id of the account',
            },
            allocated_tgroup_ids: {
                type: 'array',
                items: {
                    type: 'integer',
                    example: 14542255,
                    description: 'id of the allocated dlt template group',
                    minItems: 1,
                },
            },
            assigned_tgroup_id: { type: 'integer', example: 14542255, description: 'assigned template group id' },
        },
    },
    response: {
        200: {
            type: 'object',
            properties: {
                statusCode: { type: 'integer', example: 200, description: 'Update Successful' },
                message: { type: 'string', example: 'Account has been updated successfully' },
            },
            description: 'Update Successful',
        },
        400: response400,
        403: response403,
        500: response500,
    },
};

const accountOtherSettingsSchema = {
    summary: 'Update other settings',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['cli_id', 'newline_chars', 'sms_retry', 'bill_encrypt_type',
        'domestic_sms_blockout', 'blklist_chk', 'spam_chk', 'dup_chk_req', 'intl_sms_blockout', 'optin_chk_req', 'sales_id',
        'domestic_special_series_allow', 'req_hex_msg', 'is_ildo', 'is_16bit_udh', 'ip_validation', 'full_message', 'camp_name_auto_gen', 'subusers_reports',
        'dnd_chk', 'msg_retry_available',
        'uc_iden_allow', 'uc_iden_char_len', 'uc_iden_occur', 'is_remove_uc_chars',
        'timebound_chk_enable', 'timebound_interval', 'timebound_max_count_allow',
        'vl_shortner', 'url_smartlink_enable', 'url_track_enabled',
        'domestic_tra_blockout_reject', 'is_async', 'considerdefaultlength_as_domestic',
        'mt_adjust', 'dn_adjust', 'msg_replace_chk', 'is_schedule_allow', 'inactive_login'],
        properties: {
            cli_id: {
                type: 'integer',
                example: 6000000200000021,
                description: 'client id of the account',
            },
            newline_chars: {
                type: 'string',
                example: '##',
                description: 'Char(s) that represents newline in the message',
            },
            sms_retry: { type: 'integer', enum: [0, 1, 2, 3], example: 0, description: 'sms_retry_available  No Retry(0), Global(1), Same Route(2), Custom (3)' },
            bill_encrypt_type: { type: 'integer', enum: [0, 1, 2, 3], example: 0, description: 'bill_encrypt_type  Disable(0), Encrypt Message Only(1), Encrypt Mobile Only(2), Encrypt Both(3)' },
            trai_blockout: { type: 'integer', enum: [0, 1], example: 0, description: 'domestic_promo_trai_blockout_purge  Process next day(0), Discard the Request(1)' },
            domestic_sms_blockout: { type: 'integer', enum: [0, 1, 2], example: 0, description: 'domestic_sms_blockout  Disable(0), Process the message after blockout time(1), Discard the Request(2)' },
            domestic_sms_blockout_start: { type: 'string', example: '22:00', description: 'domestic_sms_blockout_start value in 24Hrs format HH:mm' },
            domestic_sms_blockout_stop: { type: 'string', example: '07:00', description: 'domestic_sms_blockout_stop value in 24Hrs format HH:mm' },
            dnd_chk: { type: 'integer', enum: [0, 1], example: 0, description: 'dnd_chk Disable(0), Enabled(1)' },
            blklist_chk: { type: 'integer', enum: [0, 1, 2, 3, 4, 5, 6, 7], example: 0, description: 'blklist_chk 0 - Disabled, 1 - Gobal Level Only, 2 - User Level Only, 3 - User then Global only, 4 - User then Admin then Super Admin only, 5 - User then Admin then Super Admin then Global, 6 - User then Parent User then Grand Parent User only, 7 - User then Parent User then Grand Parent User then Global' },
            spam_chk: { type: 'integer', enum: [0, 1, 2, 3, 4], example: 2, description: 'spam_chk  Disable(0), Global Only(1), MsgType Only(2), Client Level Only(3), ALL (Client Level, MsgType, Global) (4)' },
            dup_chk_req: { type: 'integer', enum: [0, 1, 2], example: 0, description: 'dup_chk_req  Disable(0), Cust Mid(1), Mobile & Message(2)' },
            dup_chk_interval: { type: 'integer', example: 10, description: 'dup_chk_interval value in minutes' },
            intl_sms_blockout: { type: 'integer', enum: [0, 1, 2], example: 0, description: 'intl_sms_blockout  Disable(0), Process the message after blockout time(1), Discard the Request(2)' },
            intl_sms_blockout_start: { type: 'string', example: '23:30', description: 'intl_sms_blockout_start value in 24Hrs format HH:mm' },
            intl_sms_blockout_stop: { type: 'string', example: '06:00', description: 'intl_sms_blockout_stop value in 24Hrs format HH:mm' },
            optin_chk_req: { type: 'integer', enum: [0, 1], example: 0, description: 'optin_chk_req  Disable(0), Enabled(1)' },
            sales_id: { type: 'string', example: '1234', description: 'sales person id' },
            domestic_special_series_allow: { type: 'integer', enum: [0, 1], example: 0, description: 'domestic_special_series_allow  Disable(0), Enabled(1)' },
            req_hex_msg: { type: 'integer', enum: [0, 1], example: 0, description: 'req_hex_msg  Disable(0), Enabled(1)' },
            is_ildo: { type: 'integer', enum: [0, 1], example: 0, description: 'is_ildo  Non ILDO(0), ILDO(1)' },
            is_16bit_udh: { type: 'integer', enum: [0, 1], example: 0, description: ' 8bitUDH(0), 16bitUDH(1)' },
            ip_validation: { type: 'integer', enum: [0, 1], example: 0, description: 'Disabled(0), Enabled(1)' },
            ip_list: { type: 'string', example: '192.168.1.101,192.168.1.102', description: 'IP list to be whitelisted' },
            full_message: { type: 'integer', enum: [0, 1], example: 0, description: 'Full message in report download. Disabled(0), Enabled(1)' },
            camp_name_auto_gen: { type: 'integer', enum: [0, 1], example: 0, description: 'Auto generation of campaign name. Disabled(0), Enabled(1)' },
            subusers_reports: { type: 'integer', enum: [0, 1], example: 0, description: 'Subusers report viewing. Disabled(0), Enabled(1)' },
            //two_level_auth: { type: 'integer', enum: [0, 1], example: 0, description: 'Two Level Authentication Disable(0), Enabled(1)' },
            mt_adjust: { type: 'integer', enum: [0, 1], example: 0, description: 'mt_adjust Disable(0), Enabled(1)' },
            dn_adjust: { type: 'integer', enum: [0, 1], example: 0, description: 'dn_adjust Disable(0), Enabled(1)' },
            dnd_reject_yn: { type: 'integer', enum: [0, 1], example: 0, description: 'dnd_reject_yn Disable(0), Enabled(1)' },
            vl_shortner: { type: 'integer', enum: [0, 1], example: 0, description: 'vl_shortner Disable(0), Enabled(1)' },
            msg_replace_chk: { type: 'integer', enum: [0, 1], example: 0, description: 'msg_replace_chk Disable(0), Enabled(1)' },
            is_schedule_allow: { type: 'integer', enum: [0, 1], example: 0, description: 'is_schedule_allow Disable(0), Enabled(1)' },
            uc_iden_allow: { type: 'integer', enum: [0, 1], example: 0, description: 'uc_iden_allow Disable(0), Enabled(1)' },
            uc_iden_char_len: { type: 'integer', example: 2, description: 'UC identification char length' },
            uc_iden_occur: { type: 'integer', example: 2, description: 'UC identification occurence' },
            is_remove_uc_chars: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
            url_smartlink_enable: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
            url_track_enabled: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
            vl_shortcode_len: { type: 'integer', enum: [5, 6], example: 6, description: '5/6 char length short code' },
            is_async: { type: 'integer', enum: [0, 1], example: 1, description: 'Sync(0), Async(1)' },
            use_default_header: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
            use_default_on_header_fail: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
            acc_default_header: { type: 'string', example: 'DEVTST', description: 'account default header' },
            considerdefaultlength_as_domestic: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
            domestic_tra_blockout_reject: { type: 'integer', enum: [0, 1], example: 1, description: 'Accept During TRAI Blockout at Interface level(0), Reject During TRAI Blockout  at Interface level(1)' },
            timebound_chk_enable: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
            timebound_interval: { type: 'integer', example: 0, description: 'timebound interval' },
            timebound_max_count_allow: { type: 'integer', example: 0, description: 'timebound max count allow' },
            dnd_pref: { type: 'integer', example: 0, description: 'dnd preferences' },
            force_dnd_check: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
            msg_retry_available: { type: 'integer', enum: [0, 1, 2, 3], example: 2, description: 'No Retry(0), Single Part Only(1), Single and Multipart(2) Partial Failed retry(3)' },
            inactive_login: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
        },
        dependencies: {
            domestic_sms_blockout_start: ['domestic_sms_blockout', 'domestic_sms_blockout_stop'],
            domestic_sms_blockout_stop: ['domestic_sms_blockout', 'domestic_sms_blockout_start'],
            intl_sms_blockout_start: ['intl_sms_blockout', 'intl_sms_blockout_stop'],
            intl_sms_blockout_stop: ['intl_sms_blockout', 'intl_sms_blockout_start'],
            dup_chk_interval: ['dup_chk_req'],
            ip_list: ['ip_validation'],
            dnd_reject_yn: ['dnd_chk'],
            dnd_pref: ['dnd_chk'],

        },
    },
    response: {
        200: {
            type: 'object',
            properties: {
                statusCode: { type: 'integer', example: 200, description: 'Update Successful' },
                message: { type: 'string', example: 'Account has been updated successfully' },
            },
            description: 'Update Successful',
        },
        400: response400,
        403: response403,
        500: response500,
    },
};

const smsDomesticRatesSchema = {
    summary: 'Update accounts sms & dlt rates',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['cli_id', 'smsrate', 'dltrate'],
        properties: {
            cli_id: {
                type: 'integer',
                example: 6000000200000021,
                description: 'client id of the account',
            },
            smsrate: { type: 'number', example: 1.023456, description: 'SMS Rate(india). Upto 6 decimal places' },
            dltrate: { type: 'number', example: 0.000007, description: 'DLT Rate(india). Upto 6 decimal places' },
        },
    },
    response: {
        200: {
            type: 'object',
            properties: {
                statusCode: { type: 'integer', example: 200, description: 'Update Successful' },
                message: { type: 'string', example: 'Account has been updated successfully' },
            },
            description: 'Update Successful',
        },
        400: response400,
        403: response403,
        500: response500,
    },
};

const smsInternationalRatesSchema = {
    summary: 'Update accounts international sms rates',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['cli_id', 'add_arr', 'update_arr', 'delete_arr'],
        properties: {
            cli_id: {
                type: 'integer',
                example: 6000000200000021,
                description: 'client id of the account',
            },
            add_arr: {
                type: 'array',
                items: {
                    type: 'object',
                    required: ['country', 'sms_rate'],
                    properties: {
                        country: { type: 'string', example: 'USA', description: 'country name' },
                        sms_rate: { type: 'number', example: 0.023, description: 'the rate' },
                    },
                },
            },
            update_arr: {
                type: 'array',
                items: {
                    type: 'object',
                    required: ['country', 'sms_rate', 'sms_rate_old'],
                    properties: {
                        country: { type: 'string', example: 'UAE', description: 'country name' },
                        sms_rate: { type: 'number', example: 0.023, description: 'updated rate' },
                        sms_rate_old: { type: 'number', example: 0.023, description: 'old rate' },
                    },
                },
            },
            delete_arr: {
                type: 'array',
                items: {
                    type: 'string',
                    properties: { country: { type: 'string', example: 'UK', description: 'country name' } },
                },
            },
        },
    },
    response: {
        200: {
            type: 'object',
            properties: {
                statusCode: { type: 'integer', example: 200, description: 'Update Successful' },
                message: { type: 'string', example: 'International sms rates have been updated successfully' },
            },
            description: 'Update Successful',
        },
        400: response400,
        500: response500,

    },
};

const smsRateRowSchema = {
    summary: 'Update accounts sms rates for row',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['cli_id', 'sms_rate', 'sms_rate_old'],
        properties: {
            cli_id: {
                type: 'integer',
                example: 6000000200000021,
                description: 'client id of the account',
            },
            sms_rate: { type: 'number', example: 0.023, description: 'new rate' },
            sms_rate_old: { type: 'number', example: 0.023, description: 'old rate' },
        },
    },
    response: {
        200: {
            type: 'object',
            properties: {
                statusCode: { type: 'integer', example: 200, description: 'Update Successful' },
                message: { type: 'string', example: 'ROW Rates has been updated successfully' },
            },
            description: 'Update Successful',
        },
        400: response400,
        500: response500,

    },
};

const walletUpdateSchema = {
    summary: 'Update Wallet amount',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['cli_id', 'amount', 'action', 'comments'],
        properties: {
            cli_id: {
                type: 'integer',
                example: 6000000200000021,
                description: 'client id of the account',
            },
            amount: { type: 'number', example: 1.0234, description: 'wallet amount to be adjusted' },
            action: { type: 'string', enum: ['add', 'deduct'], example: 'add', description: 'Add/Deduct' },
            comments: { type: 'string', example: 'adding amount', description: 'Description of the transaction' },
        },
    },
    response: {
        200: {
            type: 'object',
            properties: {
                statusCode: { type: 'integer', example: 200, description: 'Update Successful' },
                message: { type: 'string', example: 'Wallet has been updated successfully' },
                wallet_bal: { type: 'number', example: 53345.0234, description: 'Wallet balance for this user' },
            },
            description: 'Update Successful',
        },
        400: response400,
        403: response403,
        500: response500,

    },
};

const updateaccstatusSchema = {
    summary: 'Update account status',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['cli_id', 'astatus'],
        properties: {
            cli_id: {
                type: 'integer',
                example: 6000000200000021,
                description: 'client id of the account',
            },
            astatus: {
                type: 'string',
                example: 'activate',
                enum: ['activate', 'deactivate', 'suspend'],
                description: 'activate/deactivate/suspend an account',
            },
        },
    },
    response: {
        200: {
            type: 'object',
            properties: {
                statusCode: { type: 'integer', example: 200, description: 'Update Successful' },
                message: { type: 'string', example: 'Account status has been updated successfully' },
            },
            description: 'Successful',
        },
        400: response400,
        403: response403,
        500: response500,

    },
};

const salesPersonsSchema = {
    summary: 'Get all the Salespersons details',
    tags: ['Account'],
    querystring: {},
    response: {
        200: {
            type: 'array',
            items: {
                type: 'object',
                properties: {
                    id: { type: 'number', example: 1, description: 'primary key value' },
                    name: { type: 'string', example: 'Shan', description: 'Name' },
                    },
                },
                description: 'Successful response',
            },
        400: response400,
        500: response500,
    },
};

const creditsSchema = {
    summary: 'Update credit check and limit',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['cli_id', 'credit_check'],
        properties: {
            cli_id: {
                type: 'integer',
                example: 7000008400000000,
                description: 'client id of the account',
            },
            credit_check: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
            action: { type: 'string', enum: ['add', 'deduct'], example: 'add', description: 'Add/Deduct' },
            amount: { type: 'number', example: 123456.0234, description: 'amount to be credit/debited from/to credit_limit. Upto 4 decimal places' },
        },
    },
    response: {
        200: {
            type: 'object',
            properties: {
                statusCode: { type: 'integer', example: 200, description: 'Update Successful' },
                message: { type: 'string', example: 'Account has been updated successfully' },
                credit_limit: { type: 'integer', example: 123488.0234, description: 'latest credit limit' },
                credit_limit_available: { type: 'integer', example: 123.1, description: 'latest credit limit available' }
            },
            description: 'Update Successful',
        },
        400: response400,
        403: response403,
        500: response500,
    },
};

const cappingSchema = {
    summary: 'Update capping settings',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['cli_id', 'capping_chk_enable'],
        properties: {
            cli_id: {
                type: 'integer',
                example: 6000000200000021,
                description: 'client id of the account',
            },
            capping_chk_enable: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' },
            capping_interval_type: { type: 'integer', enum: [0, 1, 2, 3, 4, 5, 6], example: 0, description: 'None(0), Minute(1), Hour(2), Day(3), Week(4), Month(5), Year(6)' },
            capping_interval: { type: 'integer', example: 0, description: 'capping interval' },
            capping_max_count_allow: { type: 'integer', example: 0, description: 'capping max count allow' },
        },
    },
    response: {
        200: {
            type: 'object',
            properties: {
                statusCode: { type: 'integer', example: 200, description: 'Update Successful' },
                message: { type: 'string', example: 'Account has been updated successfully' },
            },
            description: 'Update Successful',
        },
        400: response400,
        403: response403,
        500: response500,
    },
};

const twoLevelAuthUpdationSchema = {
    summary: 'Update account two level auth (2factor) status',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['cli_id', 'two_level_auth'],
        properties: {
            cli_id: {
                type: 'integer',
                example: 6000000200000021,
                description: 'client id of the account',
            },
            two_level_auth: {
                type: 'integer',
                example: 1,
                enum: [0,1],
                description: 'Enable(1), Disable(0) 2factor auth for account',
            },
        },
    },
    response: {
        200: {
            type: 'object',
            properties: {
                statusCode: { type: 'integer', example: 200, description: 'Update Successful' },
                message: { type: 'string', example: 'Account status has been updated successfully' },
            },
            description: 'Successful',
        },
        400: response400,
        403: response403,
        500: response500,

    },
};

module.exports = {
tgroupsSchema,
subServicesSchema,
anewSchema,
ainfoSchema,
unameuniqueSchema,
accountsSchema,
astatsSchema,
accountProfileInfoSchema,
accountSettingsSchema,
accountServicesSchema,
accountSMPPSettingsSchema,
accountDLTGroupsSchema,
accountOtherSettingsSchema,
smsDomesticRatesSchema,
smsInternationalRatesSchema,
smsRateRowSchema,
walletUpdateSchema,
updateaccstatusSchema,
salesPersonsSchema,
creditsSchema,
cappingSchema,
twoLevelAuthUpdationSchema
};
