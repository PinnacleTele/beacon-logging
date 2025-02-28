const { response400, response403, response500 } = require('./generic-schema');

const unameuniqueSchema = {
    summary: 'Checks if the username is unique',
    tags: ['Account'],
    querystring: {
        type: 'object',
        required: ['uname'],
        properties: { uname: { type: 'string', description: 'username' } },
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

const countriesSchema = {
    summary: 'Get all the countries',
    tags: ['Account'],
    querystring: {},
    response: {
        200: {
            type: 'array',
            items: {
                type: 'object',
                properties: {
                    country_code_iso_3: { type: 'string', example: 'IND', description: '3 digit country code' },
                    country: { type: 'string', example: 'India', description: 'Country name' },
                },
                description: 'Successful response',
            },
        },
        400: response400,
        500: response500,
    },
};

const tgroupsSchema = {
    summary: 'Get all the template groups for the user',
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

const assignedSubServicesSchema = {
    summary: 'Get all the assigned sub services for the user',
    tags: ['Account'],
    querystring: {},
    response: {
        200: {
            type: 'array',
            items: {
                type: 'object',
                properties: {
                    sub_service_name: { type: 'string', example: 'API', description: 'sub service name for display' },
                    sub_service: { type: 'string', example: 'api', description: 'sub service id' },
                    service: { type: 'string', example: 'sms', description: 'service id' },
                    sub_service_desc: {
                        type: 'string',
                        example: 'Messaging over HTTPS Protocol',
                        description: 'description of the sub service',
                    },
                },
                description: 'Successful response',
            },
        },
        400: response400,
        500: response500,
    },
};

const ainfoSchema = {
    summary: 'Get account info',
    tags: ['Account'],
    querystring: {
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
                total_admins: { type: 'integer', example: 12, description: 'total admin accounts under him' },
                total_users: { type: 'integer', example: 2, description: 'total user accounts under him' },
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
                allocated_tgroups: {
                    type: 'array',
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
                smpp_charset: { type: 'string', example: 'UTF-8', description: 'Charset for SMPP' },
                two_level_auth: { type: 'integer', example: '0', description: '0- notenabled 1-enabled' },
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
                encrypt_mobile_yn: { type: 'integer', example: '0', description: '0-no 1-yes' },
                encrypt_message_yn: { type: 'integer', example: '0', description: '0-no 1-yes' },
                assigned_groups: {
                    type: 'array',
                    items: {
                        type: 'object',
                        example: { g_id: 'v54l2ewcsjdel119oipfl008c4d9q5a0ga99', g_name: 'group name1', g_type: 'normal' },
                        description: 'ids of the shared group(s) for this user',
                        properties: {
                            g_id: {
                                type: 'string',
                                example: 'v54l2ewcsjdel119oipfl008c4d9q5a0ga99',
                                description: 'group identifier',
                            },
                            g_name: { type: 'string', example: 'group name1', description: 'group name' },
                            g_type: { type: 'string', example: 'normal', description: 'type of group normal/exclude' },
                        },
                    },
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
                has_row_yn: { type: 'integer', example: 0, description: 'to show/hide ROW rate section when intl service is enabled. 0-no 1-yes' },
                created_ts: { type: 'string', example: 'Created date & time e.g 30-06-2021 17:44:29 IST' },
                modified_ts: { type: 'string', example: 'Last modified e.g 30-06-2021 17:44:29 IST' },
                created_ts_unix: { type: 'integer', example: 1318874398 },
                modified_ts_unix: { type: 'integer', example: 1318874398 },
                autogen_cname_yn: { type: 'integer', example: '0', description: '0-no 1-yes' },
                subusers_reports_yn: { type: 'integer', example: '0', description: '0-no 1-yes' },
                full_message_yn: { type: 'integer', example: '0', description: '0-no 1-yes' },
                platform_cluster: { type: 'string', example: 'BULK', description: 'accounts cluster type' },
                is_16bit_udh: { type: 'integer', enum: [0, 1], example: 0, description: '8bitUDH(0), 16bitUDH(1)' },
                ip_validation: { type: 'integer', enum: [0, 1], example: 0, description: 'Disabled(0), Enabled(1)' },
                ip_list: { type: 'string', example: '192.168.1.101,192.168.1.102', description: 'IP list to be whitelisted' },
                expiry_date: { type: 'string', example: '2024-03-30 17:44:29', description: 'account expiry date in user timezone' },
                expiry_ts: { type: 'string', example: 'Mar 30, 2024 17:44:29 IST', description: 'account expiry date in user timezone' },
                expiry_ts_unix: { type: 'integer', example: 1318874398 },
                inactive_login: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' }
            },
            description: 'Successful response',
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

const accountsSchema = {
    summary: 'Get all accounts for a user',
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
                        description: '0 for active, 1 for deactivated and 2 for suspended and 3 for expired',
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
                    wallet_bal: { type: 'number', example: 53345.0234, description: 'Wallet amount. Upto 6 decimal places' },
                    sms_left_india: { type: 'integer', example: 1034, description: 'total sms that can be sent with the wallet balance' },
                    billing_currency: { type: 'string', example: 'INR', description: 'currency assigned for the account' },
                    expiry_date: { type: 'string', example: '2024-03-30 17:44:29', description: 'account expiry date in loggedinuser timezone' },
                    expiry_ts_unix: { type: 'integer', example: 1318874398 },
                    acc_status_desc: { type: 'string', example: 'Active/Deactivated/Suspended/Expired' },
                },
                description: 'Successful response',
            },
        },
        400: response400,
        500: response500,
    },
};

const anewSchema = {
    summary: 'Create new account',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['user_type', 'firstname', 'company', 'username', 'email', 'mobile', 'zone_name', 'offset', 'allocated_tgroup_ids', 'assigned_tgroup_id', 'services', 'smpp_charset', 'billing_currency', 'intl_rates',
                    'is_16bit_udh','ip_validation', 'full_message', 'camp_name_auto_gen', 'expiry_date', 'twofa_yn', 'inactive_login'],
        properties: {
            user_type: { type: 'integer', enum: [1, 2], example: '1', description: '1-admin 2-user' },
            firstname: { type: 'string', example: 'Firstname', description: 'first name' },
            lastname: { type: 'string', example: 'Lastname', description: 'last name' },
            company: { type: 'string', example: 'Mannaran & Co', description: 'company name' },
            username: { type: 'string', example: 'user1', description: 'login username' },
            email: { type: 'string', example: 'user1@mail.com', description: 'email' },
            mobile: { type: 'string', example: '9465644565', description: 'mobile number without any spl chars.' },
            zone_name: { type: 'string', example: 'Asia/Calcutta', description: 'zone name of the selected tz' },
            offset: { type: 'string', example: '+05:30', description: 'offset value for the selected tz' },
            allocated_tgroup_ids: {
                type: 'array',
                items: {
                    type: 'integer',
                    example: 14542255,
                    description: 'id of the allocated template group',
                    minItems: 1,
                },
            },
            assigned_tgroup_id: { type: 'integer', example: 14542255, description: 'assigned template group id' },
            smpp_charset: { type: 'string', example: 'UTF-8', description: 'Charset for SMPP' },
            address: { type: 'string', example: '123, 1 St, MA', description: 'Company address' },
            twofa_yn: { type: 'integer', example: 0, enum: [0, 1], description: '0- disabled 1-enabled' },
            wallet: { type: 'number', example: 53345.0234, description: 'Wallet amount. Upto 6 decimal places' },
            smsrate: { type: 'number', example: 1.0234, description: 'SMS Rate. Upto 6 decimal places' },
            dltrate: { type: 'number', example: 0.0025, description: 'DLT Rate. Upto 6 decimal places' },
            billing_currency: { type: 'string', example: 'INR', enum: ['AUD', 'CAD', 'CHF', 'EUR', 'GBP', 'INR', 'JPY', 'NZD', 'USD'], description: 'currency code' },
            newline_chars: {
                type: 'string',
                example: '##',
                description: 'Char(s) that represents newline in the message',
            },
            encrypt_mobile_yn: { type: 'integer', example: 0, enum: [0, 1], description: '0-no 1-yes' },
            encrypt_message_yn: { type: 'integer', example: 0, enum: [0, 1], description: '0-no 1-yes' },
            assigned_groups: {
                type: 'array',
                items: {
                    type: 'string',
                    example: '19qlry9xjzlpblxnhea54obd4aqctu0bqrdv',
                    description: 'id of the shared group(s) selected',
                },
            },
            services: {
                type: 'array',
                items: {
                    type: 'object',
                    required: ['service', 'sub_service'],
                    properties: {
                        service: { type: 'string', example: 'sms', description: 'the main service' },
                        sub_service: { type: 'string', example: 'api', description: 'sub service under the service' },
                    },
                },
            },
            intl_rates: {
                type: 'array',
                items: {
                    type: 'object',
                    properties: {
                        country: { type: 'string', example: 'Australia', description: 'the country name' },
                        smsrate: { type: 'number', example: 0.04563, description: 'the rate in his billing currency' },
                    },
                },
            },
            is_16bit_udh: { type: 'integer', enum: [0, 1], example: 0, description: '8bitUDH(0), 16bitUDH(1)' },
            ip_validation: { type: 'integer', enum: [0, 1], example: 0, description: 'Disabled(0), Enabled(1)' },
            ip_list: { type: 'string', example: '192.168.1.101,192.168.1.102', description: 'IP list to be whitelisted' },
            full_message: { type: 'integer', enum: [0, 1], example: 0, description: 'Full message in report download. Disabled(0), Enabled(1)' },
            camp_name_auto_gen: { type: 'integer', enum: [0, 1], example: 0, description: 'Auto generation of campaign name. Disabled(0), Enabled(1)' },
            subusers_reports: { type: 'integer', enum: [0, 1], example: 0, description: 'Subusers report viewing. Disabled(0), Enabled(1)' },
            expiry_date: { type: 'string', example: '2023-12-31', description: 'account expiry date' },
            //two_level_auth: { type: 'integer', enum: [0, 1], example: 0, description: 'Two Level Authentication Disable(0), Enabled(1)' },
            inactive_login: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' }
        },
        dependencies: {
            wallet: ['smsrate', 'dltrate'],
            smsrate: ['wallet', 'dltrate'],
            dltrate: ['wallet', 'smsrate'],
            ip_list: ['ip_validation']
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

const aupdatePISchema = {
    summary: 'Update Account Info',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['cli_id', 'firstname', 'company', 'address'],
        properties: {
            cli_id: {
                type: 'integer',
                example: 6000000200000021,
                description: 'client id of the account',
            },
            firstname: { type: 'string', example: 'Firstname', description: 'first name' },
            lastname: { type: 'string', example: 'Lastname', description: 'last name' },
            company: { type: 'string', example: 'Mannaran & Co', description: 'company name' },
            address: { type: 'string', example: 'Group1', description: 'group name' },
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

const aupdateEncryptionSchema = {
    summary: 'Update Encryption',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['cli_id', 'encrypt_mobile_yn', 'encrypt_message_yn'],
        properties: {
            cli_id: {
                type: 'integer',
                example: 6000000200000021,
                description: 'client id of the account',
            },
            encrypt_mobile_yn: { type: 'integer', example: 0, enum: [0, 1], description: '0-no 1-yes' },
            encrypt_message_yn: { type: 'integer', example: 0, enum: [0, 1], description: '0-no 1-yes' },
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

const aupdateTGroups = {
    summary: 'Update Accounts DLT Template Group Allocation & Assignment',
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
                    description: 'id of the allocated template group',
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

const aupdate2FASchema = {
    summary: 'Update 2FA',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['cli_id', 'twofa_yn'],
        properties: {
            cli_id: {
                type: 'integer',
                example: 6000000200000021,
                description: 'client id of the account',
            },
            twofa_yn: { type: 'integer', example: 0, enum: [0, 1], description: '0- notenabled 1-enabled' },
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

const aupdatepasswordSchema = {
    summary: 'Update Password',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['newpass'],
        properties: { newpass: { type: 'string', example: 'newpassword', description: 'new password to be updated' } },
    },
    response: {
        200: {
            type: 'object',
            properties: {
                statusCode: { type: 'integer', example: 200, description: 'Update Successful' },
                message: { type: 'string', example: 'Account password has been updated successfully' },
            },
            description: 'Update Successful',
        },
        400: response400,
        403: response403,
        500: response500,

    },
};

const aupdateMSSchema = {
    summary: 'Update Messaging Settings',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['cli_id', 'zone_name', 'offset', 'newline_chars', 'is_16bit_udh', 'ip_validation', 'full_message', 'camp_name_auto_gen' ],
        properties: {
            cli_id: {
                type: 'integer',
                example: 6000000200000021,
                description: 'client id of the account',
            },
            zone_name: { type: 'string', example: 'Asia/Calcutta', description: 'zone name of the selected tz' },
            offset: { type: 'string', example: '+05:30', description: 'offset value for the selected tz' },
            newline_chars: {
                type: 'string',
                example: '##',
                description: 'Char(s) that represents newline in the message',
            },
            is_16bit_udh: { type: 'integer', enum: [0, 1], example: 0, description: '8bitUDH(0), 16bitUDH(1)' },
            ip_validation: { type: 'integer', enum: [0, 1], example: 0, description: 'Disabled(0), Enabled(1)' },
            ip_list: { type: 'string', example: '192.168.1.101,192.168.1.102', description: 'IP list to be whitelisted' },
            full_message: { type: 'integer', enum: [0, 1], example: 0, description: 'Full message in report download. Disabled(0), Enabled(1)' },
            camp_name_auto_gen: { type: 'integer', enum: [0, 1], example: 0, description: 'Auto generation of campaign name. Disabled(0), Enabled(1)' },
            subusers_reports: { type: 'integer', enum: [0, 1], example: 0, description: 'Subusers report viewing. Disabled(0), Enabled(1)' },
            expiry_date: { type: 'string', example: '2023-12-31', description: 'account expiry date' },
            inactive_login: { type: 'integer', enum: [0, 1], example: 0, description: 'Disable(0), Enabled(1)' }
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

const aupdateServicesSchema = {
    summary: 'Update Services',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['cli_id', 'services', 'smpp_charset', 'row_rate'],
        properties: {
            cli_id: {
                type: 'integer',
                example: 6000000200000021,
                description: 'client id of the account',
            },
            smpp_charset: { type: 'string', example: 'UTF-8', description: 'Charset for SMPP' },
            services: {
                type: 'array',
                items: {
                    type: 'object',
                    required: ['service', 'sub_service'],
                    properties: {
                        service: { type: 'string', example: 'sms', description: 'the main service' },
                        sub_service: { type: 'string', example: 'api', description: 'sub service under the service' },
                    },
                },
            },
            row_rate: { type: 'number', example: 1.0234, description: 'rest of world rate' },
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

const aupdateGroupsSchema = {
    summary: 'Update Shared Groups',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['cli_id', 'assigned_groups'],
        properties: {
            cli_id: {
                type: 'integer',
                example: 6000000200000021,
                description: 'client id of the account',
            },
            assigned_groups: {
                type: 'array',
                items: {
                    type: 'string',
                    example: '19qlry9xjzlpblxnhea54obd4aqctu0bqrdv',
                    description: 'id of the shared group(s) selected',
                },
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
            description: 'Update Successful',
        },
        400: response400,
        403: response403,
        500: response500,

    },
};

const aupdateWRateSchema = {
    summary: 'Update Wallet rates',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['cli_id', 'smsrate', 'dltrate', 'smsrate_old', 'dltrate_old'],
        properties: {
            cli_id: {
                type: 'integer',
                example: 6000000200000021,
                description: 'client id of the account',
            },
            smsrate: { type: 'number', example: 1.0234, description: 'SMS Rate. Upto 6 decimal places' },
            dltrate: { type: 'number', example: 0.0025, description: 'DLT Rate. Upto 6 decimal places' },
            smsrate_old: { type: 'number', example: 1.0234, description: 'Previous SMS Rate. Upto 6 decimal places' },
            dltrate_old: { type: 'number', example: 0.0025, description: 'Previous DLT Rate. Upto 6 decimal places' },
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

const aupdateWAmountSchema = {
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
            amount: { type: 'number', example: 1.0234, description: 'wallet amount to be added/deducted' },
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

const abalSchema = {
    summary: 'Get Wallet balance',
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
                wallet_bal: {
                    type: 'number',
                    example: 53345.0234,
                    description: 'Wallet balance for this user',
                },
                sms_left: { type: 'integer', example: 1034, description: 'total sms that can be sent for the wallet balance' },
            },
            description: 'Successful',
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

const ausersmwSchema = {
    summary: 'Get users matching wallet action and amount',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['amount', 'action'],
        properties: {
            amount: { type: 'number', example: 1.0234, description: 'wallet amount to be added/deducted' },
            action: { type: 'string', enum: ['add', 'deduct'], example: 'add', description: 'Add/Deduct' },
        },
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
                    wallet_bal: { type: 'number', example: 53345.0234, description: 'Wallet balance for this user' },
                    billing_currency: { type: 'string', example: 'INR', description: 'currency assigned for the account' },
                },
            },
            description: 'Successful',
        },
        400: response400,
        403: response403,
        500: response500,

    },
};

const aupdatewamountmultiSchema = {
    summary: 'Update wallet amount to the selected users',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['amount', 'action', 'cli_ids', 'comments'],
        properties: {
            amount: { type: 'number', example: 1.0234, description: 'wallet amount to be added/deducted' },
            action: { type: 'string', enum: ['add', 'deduct'], example: 'add', description: 'Add/Deduct' },
            cli_ids: {
                type: 'array',
                items: {
                    type: 'integer',
                    example: [6000000200000029],
                    description: 'cli_id of selected users',
                    minItems: 1,
                },
            },
            comments: { type: 'string', example: 'some description', description: 'Description of the transaction' },
        },
    },
    response: {
        200: {
            type: 'object',
            properties: {
                statusCode: {
                    type: 'integer',
                    example: '200/-602',
                    description: 'Update Successful/ Not enough balance',
                },
                message: { type: 'string', example: 'Wallet amount has been updated successfully' },
            },
            description: 'Successful',
        },
        400: response400,
        403: response403,
        500: response500,

    },
};

const aquicklinkssettingsSchema = {
    summary: 'get all the quick link settings',
    tags: ['Account'],
    querystring: {},
    response: {
        200: {
            type: 'object',
            properties: {
                quicklinks: {
                    type: 'object',
                    properties: {
                        campaign: {
                            type: 'object',
                            properties: {
                                cq: { type: 'boolean', example: false },
                                cotm: { type: 'boolean', example: false },
                                cmtm: { type: 'boolean', example: false },
                                cg: { type: 'boolean', example: false },
                                ct: { type: 'boolean', example: false },

                            },
                        },
                        createnew: {
                            type: 'object',
                            properties: {
                                account: { type: 'boolean', example: false },
                                template: { type: 'boolean', example: false },
                                group: { type: 'boolean', example: false },
                            },
                        },
                        myaccount: {
                            type: 'object',
                            properties: {
                                mysettings: { type: 'boolean', example: false },
                                wallet: { type: 'boolean', example: false },
                            },
                        },
                        report: {
                            type: 'object',
                            properties: {
                                summary: { type: 'boolean', example: false },
                                detailed: { type: 'boolean', example: false },
                                search: { type: 'boolean', example: false },
                            },
                        },
                    },
                },
            },
            description: 'Successful response',
        },
        400: response400,
        500: response500,
    },
};

const aquicklinksSchema = {
    summary: 'get all the configured quick links',
    tags: ['Account'],
    querystring: {},
    response: {
        200: {
            type: 'array',
            items: {
                type: 'object',
                properties: {
                    quicklink: {
                        type: 'string',
                        example: 'cq',
                        description: 'the quick link',
                    },
                    group: {
                        type: 'string',
                        example: 'campaign',
                        description: 'the group the link belongs to',
                    },
                },
            },

            description: 'Successful response',
        },
        400: response400,
        500: response500,
    },
};

const aupdatequicklinksettingsSchema = {
    summary: 'Update quick link settings',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['selected_quicklinks'],
        properties: {
            selected_quicklinks: {
                type: 'array',
                items: {
                    type: 'object',
                    required: ['ql', 'group'],
                    properties: {
                        ql: {
                            type: 'string',
                            enum: ['cq', 'cotm', 'cmtm', 'cg', 'ct', 'account', 'group', 'template', 'mysettings', 'wallet', 'summary', 'detailed', 'search'],
                            example: 'cq',
                            description: 'the quick link',
                        },
                        group: {
                            type: 'string',
                            enum: ['campaign', 'createnew', 'myaccount', 'report'],
                            example: 'campaign',
                            description: 'the group the link belongs to',
                        },
                    },
                },
            },
        },
    },
    response: {
        200: {
            type: 'object',
            properties: {
                statusCode: { type: 'integer', example: 200, description: 'Update Successful' },
                message: { type: 'string', example: 'Quick link settings updated successfully' },
            },
            description: 'Update Successful',
        },
        400: response400,
        403: response403,
        500: response500,

    },
};

const awtSchema = {
    summary: 'Get wallet transaction',
    tags: ['Account'],
    body: {
        type: 'object',
        required: ['dateselectiontype', 'q'],
        properties: {
            dateselectiontype: {
                type: 'string',
                enum: ['custom range', 'yesterday', 'today', 'last 7 days', 'last 15 days', 'last 30 days', 'this week', 'this month'],
                example: 'last 7 days',
                description: 'the selected date type',
            },
            q: {
                type: 'string',
                enum: ['lu', 'u'],
                example: 'lu',
                description: 'get the data for. lu - logged in user, u-user',
            },
            fdate: { type: 'string', example: '2021-09-09', description: 'From date in yyyy-MM-dd format' },
            tdate: { type: 'string', example: '2021-09-09', description: 'From date in yyyy-MM-dd format' },
        },
    },
    response: {
        200: {
            type: 'array',
            items: {
                type: 'object',
                properties: {
                    username: { type: 'string', example: 'Username', description: 'username' },
                    amount: { type: 'number', example: 10.0025, description: 'Amount added / deducted' },
                    loggedin_bal_after: { type: 'number', example: 10.0025, description: 'wallet balance of logged in user after the action' },
                    new_bal: { type: 'number', example: 10.0025, description: 'wallet balance of the user after the action' },
                    old_bal: { type: 'number', example: 11.0025, description: 'wallet balance of the user before the action' },
                    created_ts: { type: 'string', example: 'Created date & time e.g Jun 20 2021 14:20 IST' },
                    created_ts_unix: { type: 'integer', example: 1318874398 },
                    user_type: { type: 'integer', example: '1', description: '0-superadmin 1-admin 2-user' },
                    billing_currency: { type: 'string', example: 'INR', description: 'currency assigned for this account' },
                    action: { type: 'string', example: 'add', description: 'action that was done add/deduct' },
                    description: { type: 'string', example: 'details of transaction', description: 'details of the transaction' },
                },
            },
            description: 'Successful',
        },
        400: response400,
        500: response500,

    },
};

const parentbrrateintlSchema = {
    summary: 'get all the configured rates for intl countries',
    tags: ['Account'],
    querystring: {},
    response: {
        200: {
            type: 'array',
            items: {
                type: 'object',
                properties: {
                    country: {
                        type: 'string',
                        example: 'USA',
                        description: 'country name',
                    },
                    smsrate: {
                        type: 'number',
                        example: 0.023,
                        description: 'the rate',
                    },
                    sms_left: { type: 'integer', example: 1034, description: 'total sms that can be sent for the wallet balance' },
                },
            },

            description: 'Successful response',
        },
        400: response400,
        500: response500,
    },
};

const brrateintlSchema = {
    summary: 'get all the configured rates for intl countries',
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
                conv_rate: {
                    type: 'number',
                    example: 0.023,
                    description: 'conversion rate for billing currency to euro',
                },

                rates_arr: {
                    type: 'array',
                    items: {
                        type: 'object',
                        properties: {
                            country: {
                                type: 'string',
                                example: 'USA',
                                description: 'country name',
                            },
                            sms_rate: {
                                type: 'number',
                                example: 0.023,
                                description: 'the rate',
                            },
                            base_sms_rate: {
                                type: 'number',
                                example: 0.023,
                                description: 'the rate',
                            },
                            billing_currency: { type: 'string', example: 'INR', description: 'currency assigned for this account' },
                        },
                    },
                },
            },

            description: 'Successful response',
        },
        400: response400,
        500: response500,
    },
};

module.exports = {
    unameuniqueSchema,
    countriesSchema,
    tgroupsSchema,
    assignedSubServicesSchema,
    anewSchema,
    ainfoSchema,
    accountsSchema,
    astatsSchema,
    aupdatePISchema,
    aupdateEncryptionSchema,
    aupdateTGroups,
    aupdate2FASchema,
    aupdatepasswordSchema,
    aupdateMSSchema,
    aupdateServicesSchema,
    aupdateGroupsSchema,
    aupdateWRateSchema,
    aupdateWAmountSchema,
    abalSchema,
    updateaccstatusSchema,
    ausersmwSchema,
    aupdatewamountmultiSchema,
    aquicklinkssettingsSchema,
    aupdatequicklinksettingsSchema,
    aquicklinksSchema,
    awtSchema,
    parentbrrateintlSchema,
    brrateintlSchema,
};
