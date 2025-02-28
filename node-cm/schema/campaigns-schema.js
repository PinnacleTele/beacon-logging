const { response400, response500 } = require('./generic-schema');

const msginfoSchema = {
  summary: 'Gets the info on the message like no of parts, total char length, is it a unicode message ',
  tags: ['Campaign'],
  body: {
    type: 'object',
    required: ['msg'],
    properties: { msg: { type: 'string', example: '<message entered>' } },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        chars: { type: 'integer', example: '21 <total characters>' },
        parts: { type: 'integer', example: '2 <no of parts>' },
        isUnicode: { type: 'boolean', example: true },
        msg: { type: 'string', example: 'this is a message', description: 'the actual msg that will be sent to the handset' },
      },
      description: 'Successful response',
    },
    400: response400,
    500: response500,
  },
};

const cnameuniqueSchema = {
  summary: 'Checks if the campaign name is unique for this user',
  tags: ['Quick SMS'],
  querystring: {
    type: 'object',
    required: ['cname'],
    properties: { cname: { type: 'string', description: 'campaign name' } },
  },
  response: {
    200: {
      type: 'object',
      properties: { isUnique: { type: 'boolean', example: true } },
      description: 'Successful response',
    },
    400: response400,
    500: response500,
  },
};

const cqSendSchema = {
  summary: 'Send Quick Campaign',
  tags: ['Quick SMS'],
  body: {
    type: 'object',
    required: ['c_name', 'mobile_list', 'remove_dupe_yn', 'msg', 'c_type', 'c_lang_type', 'c_lang', 'valid', 'traffic_to'],
    properties: {
      c_name: { type: 'string', example: 'campaign 1', description: 'campaign name' },
      mobile_list: {
        type: 'array',
        items: { type: 'integer' },
        example: '[9728722445, 9728722443]',
        description: 'list of valid mobile numbers',
      },
      remove_dupe_yn: { type: 'integer', enum: [0, 1], example: 1, description: '1-remove duplicate' },
      shorten_url_yn: { type: 'integer', enum: [0, 1], example: 1, description: '1 - enabled, 0- disabled' },
      msg: {
        type: 'string',
        example: 'this is a message',
        description: 'message',
      },
      c_type: { type: 'string', enum: ['quick'], example: 'quick', description: 'the type of the campaign' },
      c_lang_type: { type: 'string', enum: ['english', 'unicode'], example: 'unicode', description: 'is message unicode or english' },
      c_lang: { type: 'string', example: '', description: 'empty for now' },
      valid: { type: 'integer', example: '4645435', description: 'the total valid mobile count' },
      template_id: { type: 'string', example: 'w99ldvffgai7vinx20bexmnq3u5eaywcp3c9' },
      dlt_entity_id: { type: 'string', example: '110100001352' },
      header: { type: 'string', example: 'Sender id' },
      dlt_template_id: { type: 'string', example: '1000000000000000626', description: 'DLT template id' },
      intl_header: { type: 'string', example: 'Intl Sender id' },
      traffic_to: { type: 'string', enum: ['india', 'other', 'both'], example: 'india', description: 'preference where the campaign is targetted. india-only india, other-all countries except india, both-worldwide' },
    },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        statusCode: { type: 'integer', example: '200/-602', description: 'Campaign Sent Successfully/No wallet balance' },
        message: { type: 'string', example: 'Campaign sent for processing ' },
      },
      description: 'Sent Successfully',
    },
    400: response400,
    500: response500,

  },
};

const cqScheduleSchema = {
  summary: 'Schedule Quick Campaign',
  tags: ['Quick SMS'],
  body: {
    type: 'object',
    required: ['c_name', 'mobile_list', 'remove_dupe_yn', 'msg', 'c_type', 'c_lang_type', 'c_lang', 'valid', 'dlt_entity_id', 'schedule_list'],
    properties: {
      c_name: { type: 'string', example: 'campaign 1', description: 'campaign name' },
      mobile_list: {
        type: 'array',
        items: { type: 'integer' },
        example: '[9728722445, 9728722443]',
        description: 'list of valid mobile numbers',
      },
      remove_dupe_yn: { type: 'integer', enum: [0, 1], example: '1', description: 'remove duplicate' },
      shorten_url_yn: { type: 'integer', enum: [0, 1], example: 1, description: '1 - enabled, 0- disabled' },
      msg: {
        type: 'string',
        example: 'this is a message',
        description: 'message',
      },
      c_type: { type: 'string', enum: ['quick'], example: 'quick', description: 'the type of the campaign' },
      c_lang_type: { type: 'string', enum: ['english', 'unicode'], example: 'unicode', description: 'is message unicode or english' },
      c_lang: { type: 'string', example: '', description: 'empty for now' },
      valid: { type: 'integer', example: '4645435', description: 'the total valid mobile count' },
      template_id: { type: 'string', example: 'w99ldvffgai7vinx20bexmnq3u5eaywcp3c9' },
      dlt_entity_id: { type: 'string', example: '110100001352' },
      header: { type: 'string', example: 'Sender id' },
      schedule_list: {
        type: 'array',
        items: {
          type: 'object',
          required: ['dt', 'time', 'zone_name'],
          properties: {
            dt: { type: 'string', example: '2021-07-16', description: 'Schedule date selected (no conversion). Format => YYYY-MM-DD' },
            time: { type: 'string', example: '13:01:00', description: 'Schedule time selected (no conversion). Format => HH24:mm:ss' },
            zone_name: { type: 'string', example: 'America/Los_Angeles', description: 'Zone name of the selected timezone' },
          },
        },
      },
      dlt_template_id: { type: 'string', example: '1000000000000000626', description: 'DLT template id' },
      intl_header: { type: 'string', example: 'Intl Sender id' },
      traffic_to: { type: 'string', enum: ['india', 'other', 'both'], example: 'india', description: 'preference where the campaign is targetted. india-only india, other-all countries except india, both-worldwide' },
    },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        statusCode: { type: 'integer', example: 201, description: 'Campaign Scheduled Successfully' },
        message: { type: 'string', example: 'Campaign Scheduled Successfully' },
      },
      description: 'Scheduled Successfully',
    },
    400: response400,
    500: response500,

  },
};

const cotmSendSchema = {
  summary: 'Send OTM Campaign',
  tags: ['Campaign OTM'],
  body: {
    type: 'object',
    required: ['c_name', 'remove_dupe_yn', 'msg', 'c_type', 'c_lang_type', 'c_lang', 'files', 'traffic_to'],
    properties: {
      c_name: { type: 'string', example: 'campaign 1', description: 'campaign name' },
      remove_dupe_yn: { type: 'integer', enum: [0, 1], example: '1', description: 'remove duplicate' },
      shorten_url_yn: { type: 'integer', enum: [0, 1], example: 1, description: '1 - enabled, 0- disabled' },
      msg: {
        type: 'string',
        example: 'this is a message',
        description: 'message',
      },
      c_type: { type: 'string', enum: ['otm'], example: 'otm', description: 'the type of the campaign' },
      c_lang_type: { type: 'string', enum: ['english', 'unicode'], example: 'unicode', description: 'is message unicode or english' },
      c_lang: { type: 'string', example: '', description: 'empty for now' },
      template_id: { type: 'string', example: 'w99ldvffgai7vinx20bexmnq3u5eaywcp3c9' },
      dlt_entity_id: { type: 'string', example: '110100001352' },
      header: { type: 'string', example: 'Sender id' },
      files: {
        type: 'array',
        items: {
          type: 'object',
          required: ['filename', 'r_filename', 'count'],
          properties: {
            filename: { type: 'string', example: 'file1.xls', description: 'file uploaded by the user' },
            r_filename: { type: 'string', example: 'cotm_file1.xls', description: 'remote file name' },
            count: { type: 'integer', example: 3456, description: 'total count from the upload api' },
          },
        },
      },
      dlt_template_id: { type: 'string', example: '1000000000000000626', description: 'DLT template id' },
      intl_header: { type: 'string', example: 'Intl Sender id' },
      traffic_to: { type: 'string', enum: ['india', 'other', 'both'], example: 'india', description: 'preference where the campaign is targetted. india-only india, other-all countries except india, both-worldwide' },
    },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        statusCode: { type: 'integer', example: '200/-602', description: 'Campaign Sent Successfully/No wallet balance' },
        message: { type: 'string', example: 'Campaign sent for processing ' },
      },
      description: 'Sent Successfully',
    },
    400: response400,
    500: response500,

  },
};

const cotmScheduleSchema = {
  summary: 'Schedule OTM Campaign',
  tags: ['Campaign OTM'],
  body: {
    type: 'object',
    required: ['c_name', 'remove_dupe_yn', 'msg', 'c_type', 'c_lang_type', 'c_lang', 'files', 'schedule_list', 'traffic_to'],
    properties: {
      c_name: { type: 'string', example: 'campaign 1', description: 'campaign name' },
      remove_dupe_yn: { type: 'integer', enum: [0, 1], example: '1', description: 'remove duplicate' },
      shorten_url_yn: { type: 'integer', enum: [0, 1], example: 1, description: '1 - enabled, 0- disabled' },
      msg: {
        type: 'string',
        example: 'this is a message',
        description: 'message',
      },
      c_type: { type: 'string', enum: ['otm'], example: 'otm', description: 'the type of the campaign' },
      c_lang_type: { type: 'string', enum: ['english', 'unicode'], example: 'unicode', description: 'is message unicode or english' },
      c_lang: { type: 'string', example: '', description: 'empty for now' },
      template_id: { type: 'string', example: 'w99ldvffgai7vinx20bexmnq3u5eaywcp3c9' },
      dlt_entity_id: { type: 'string', example: '110100001352' },
      header: { type: 'string', example: 'Sender id' },
      files: {
        type: 'array',
        items: {
          type: 'object',
          required: ['filename', 'r_filename', 'count'],
          properties: {
            filename: { type: 'string', example: 'file1.xls', description: 'file uploaded by the user' },
            r_filename: { type: 'string', example: 'cotm_file1.xls', description: 'remote file name' },
            count: { type: 'integer', example: 3456, description: 'total count from the upload api' },
          },
        },
      },
      schedule_list: {
        type: 'array',
        items: {
          type: 'object',
          required: ['dt', 'time', 'zone_name'],
          properties: {
            dt: { type: 'string', example: '2021-07-16', description: 'Schedule date selected (no conversion). Format => YYYY-MM-DD' },
            time: { type: 'string', example: '13:01:00', description: 'Schedule time selected (no conversion). Format => HH24:mm:ss' },
            zone_name: { type: 'string', example: 'America/Los_Angeles', description: 'Zone name of the selected timezone' },
          },
        },
      },
      dlt_template_id: { type: 'string', example: '1000000000000000626', description: 'DLT template id' },
      intl_header: { type: 'string', example: 'Intl Sender id' },
      traffic_to: { type: 'string', enum: ['india', 'other', 'both'], example: 'india', description: 'preference where the campaign is targetted. india-only india, other-all countries except india, both-worldwide' },
    },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        statusCode: { type: 'integer', example: 201, description: 'Campaign Scheduled Successfully' },
        message: { type: 'string', example: 'Campaign Scheduled Successfully' },
      },
      description: 'Scheduled Successfully',
    },
    400: response400,
    500: response500,

  },
};

const cmtmSendSchema = {
  summary: 'Send MTM Campaign',
  tags: ['Campaign MTM'],
  body: {
    type: 'object',
    required: ['c_name', 'remove_dupe_yn', 'c_type', 'c_lang_type', 'c_lang', 'files', 'traffic_to'],
    properties: {
      c_name: { type: 'string', example: 'campaign 1', description: 'campaign name' },
      remove_dupe_yn: { type: 'integer', enum: [0, 1], example: '1', description: 'remove duplicate' },
      shorten_url_yn: { type: 'integer', enum: [0, 1], example: 1, description: '1 - enabled, 0- disabled' },
      c_type: { type: 'string', enum: ['mtm'], example: 'otm', description: 'the type of the campaign' },
      c_lang_type: { type: 'string', enum: ['english', 'unicode'], example: 'unicode', description: 'is message unicode or english' },
      c_lang: { type: 'string', example: '', description: 'empty for now' },
      dlt_entity_id: { type: 'string', example: '110100001352' },
      header: { type: 'string', example: 'Sender id' },
      files: {
        type: 'array',
        items: {
          type: 'object',
          required: ['filename', 'r_filename', 'count'],
          properties: {
            filename: { type: 'string', example: 'file1.xls', description: 'file uploaded by the user' },
            r_filename: { type: 'string', example: 'cmtm_file1.xls', description: 'remote file name' },
            count: { type: 'integer', example: 3456, description: 'total count from the upload api' },
          },
        },
      },
      dlt_template_id: { type: 'string', example: '1000000000000000626', description: 'DLT template id' },
      intl_header: { type: 'string', example: 'Intl Sender id' },
      traffic_to: { type: 'string', enum: ['india', 'other', 'both'], example: 'india', description: 'preference where the campaign is targetted. india-only india, other-all countries except india, both-worldwide' },
    },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        statusCode: { type: 'integer', example: '200/-602', description: 'Campaign Sent Successfully/No wallet balance' },
        message: { type: 'string', example: 'Campaign sent for processing ' },
      },
      description: 'Sent Successfully',
    },
    400: response400,
    500: response500,

  },
};

const cmtmScheduleSchema = {
  summary: 'Schedule MTM Campaign',
  tags: ['Campaign MTM'],
  body: {
    type: 'object',
    required: ['c_name', 'remove_dupe_yn', 'c_type', 'c_lang_type', 'c_lang', 'files', 'schedule_list', 'traffic_to'],
    properties: {
      c_name: { type: 'string', example: 'campaign 1', description: 'campaign name' },
      remove_dupe_yn: { type: 'integer', enum: [0, 1], example: '1', description: 'remove duplicate' },
      shorten_url_yn: { type: 'integer', enum: [0, 1], example: 1, description: '1 - enabled, 0- disabled' },
      c_type: { type: 'string', enum: ['mtm'], example: 'otm', description: 'the type of the campaign' },
      c_lang_type: { type: 'string', enum: ['english', 'unicode'], example: 'unicode', description: 'is message unicode or english' },
      c_lang: { type: 'string', example: '', description: 'empty for now' },
      dlt_entity_id: { type: 'string', example: '110100001352' },
      header: { type: 'string', example: 'Sender id' },
      files: {
        type: 'array',
        items: {
          type: 'object',
          required: ['filename', 'r_filename', 'count'],
          properties: {
            filename: { type: 'string', example: 'file1.xls', description: 'file uploaded by the user' },
            r_filename: { type: 'string', example: 'cmtm_file1.xls', description: 'remote file name' },
            count: { type: 'integer', example: 3456, description: 'total count from the upload api' },
          },
        },
      },
      schedule_list: {
        type: 'array',
        items: {
          type: 'object',
          required: ['dt', 'time', 'zone_name'],
          properties: {
            dt: { type: 'string', example: '2021-07-16', description: 'Schedule date selected (no conversion). Format => YYYY-MM-DD' },
            time: { type: 'string', example: '13:01:00', description: 'Schedule time selected (no conversion). Format => HH24:mm:ss' },
            zone_name: { type: 'string', example: 'America/Los_Angeles', description: 'Zone name of the selected timezone' },
          },
        },
      },
      dlt_template_id: { type: 'string', example: '1000000000000000626', description: 'DLT template id' },
      intl_header: { type: 'string', example: 'Intl Sender id' },
      traffic_to: { type: 'string', enum: ['india', 'other', 'both'], example: 'india', description: 'preference where the campaign is targetted. india-only india, other-all countries except india, both-worldwide' },
    },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        statusCode: { type: 'integer', example: 201, description: 'Campaign Scheduled Successfully' },
        message: { type: 'string', example: 'Campaign Scheduled Successfully' },
      },
      description: 'Scheduled Successfully',
    },
    400: response400,
    500: response500,

  },
};

const ctSendSchema = {
  summary: 'Send Template Campaign',
  tags: ['Campaign Template'],
  body: {
    type: 'object',
    required: ['c_name', 'remove_dupe_yn', 'c_type', 'c_lang_type', 'c_lang', 'files', 'traffic_to', 'save_template_yn', 'is_unicode'],
    properties: {
      c_name: { type: 'string', example: 'campaign 1', description: 'campaign name' },
      shorten_url_yn: { type: 'integer', enum: [0, 1], example: 1, description: '1 - enabled, 0- disabled' },
      remove_dupe_yn: { type: 'integer', enum: [0, 1], example: '1', description: 'remove duplicate' },
      c_type: { type: 'string', enum: ['template'], example: 'template', description: 'the type of the campaign' },
      c_lang_type: { type: 'string', enum: ['english', 'unicode'], example: 'unicode', description: 'is message unicode or english' },
      c_lang: { type: 'string', example: '', description: 'empty for now' },
      template_id: { type: 'string', example: 'w99ldvffgai7vinx20bexmnq3u5eaywcp3c9' },
      dlt_entity_id: { type: 'string', example: '110100001352' },
      header: { type: 'string', example: 'Sender id' },
      files: {
        type: 'array',
        items: {
          type: 'object',
          required: ['filename', 'r_filename', 'count'],
          properties: {
            filename: { type: 'string', example: 'file1.xls', description: 'file uploaded by the user' },
            r_filename: { type: 'string', example: 'cotm_file1.xls', description: 'remote file name' },
            count: { type: 'integer', example: 3456, description: 'total count from the upload api' },
          },
        },
      },
      dlt_template_id: { type: 'string', example: '1000000000000000626', description: 'DLT template id' },
      intl_header: { type: 'string', example: 'Intl Sender id' },
      traffic_to: { type: 'string', enum: ['india', 'other', 'both'], example: 'india', description: 'preference where the campaign is targetted. india-only india, other-all countries except india, both-worldwide' },
      msg: {
        type: 'string',
        example: 'this is a message',
        description: 'message',
      },
      save_template_yn: { type: 'integer', example: 1, description: 'save this in saved template. 1-yes 0-no ' },
      is_static: { type: 'integer', enum: [0, 1, null], example: 1, description: 'is the message static or dynamic 0-dynamic, 1-static' },
      is_unicode: { type: 'integer', enum: [0, 1], example: 1, description: 'is the template content 0-english, 1-unicode' },
      t_name: { type: 'string', example: 'Template Name' },
      t_type: { type: 'string', example: 'Template Type <column/index>' },
      t_mobile_column: { type: 'string', example: 'Mobile column. value may be a column name or index' },
    },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        statusCode: { type: 'integer', example: '200/-602', description: 'Campaign Sent Successfully/No wallet balance' },
        message: { type: 'string', example: 'Campaign sent for processing ' },
      },
      description: 'Sent Successfully',
    },
    400: response400,
    500: response500,

  },
};

const ctScheduleSchema = {
  summary: 'Schedule Template Campaign',
  tags: ['Campaign Template'],
  body: {
    type: 'object',
    required: ['c_name', 'remove_dupe_yn', 'c_type', 'c_lang_type', 'c_lang', 'files', 'schedule_list', 'traffic_to', 'save_template_yn', 'is_unicode'],
    properties: {
      c_name: { type: 'string', example: 'campaign 1', description: 'campaign name' },
      shorten_url_yn: { type: 'integer', enum: [0, 1], example: 1, description: '1 - enabled, 0- disabled' },
      remove_dupe_yn: { type: 'integer', enum: [0, 1], example: '1', description: 'remove duplicate' },
      c_type: { type: 'string', enum: ['template'], example: 'template', description: 'the type of the campaign' },
      c_lang_type: { type: 'string', enum: ['english', 'unicode'], example: 'unicode', description: 'is message unicode or english' },
      c_lang: { type: 'string', example: '', description: 'empty for now' },
      template_id: { type: 'string', example: 'w99ldvffgai7vinx20bexmnq3u5eaywcp3c9' },
      dlt_entity_id: { type: 'string', example: '110100001352' },
      header: { type: 'string', example: 'Sender id' },
      files: {
        type: 'array',
        items: {
          type: 'object',
          required: ['filename', 'r_filename', 'count'],
          properties: {
            filename: { type: 'string', example: 'file1.xls', description: 'file uploaded by the user' },
            r_filename: { type: 'string', example: 'cotm_file1.xls', description: 'remote file name' },
            count: { type: 'integer', example: 3456, description: 'total count from the upload api' },
          },
        },
      },
      schedule_list: {
        type: 'array',
        items: {
          type: 'object',
          required: ['dt', 'time', 'zone_name'],
          properties: {
            dt: { type: 'string', example: '2021-07-16', description: 'Schedule date selected (no conversion). Format => YYYY-MM-DD' },
            time: { type: 'string', example: '13:01:00', description: 'Schedule time selected (no conversion). Format => HH24:mm:ss' },
            zone_name: { type: 'string', example: 'America/Los_Angeles', description: 'Zone name of the selected timezone' },
          },
        },
      },
      dlt_template_id: { type: 'string', example: '1000000000000000626', description: 'DLT template id' },
      intl_header: { type: 'string', example: 'Intl Sender id' },
      traffic_to: { type: 'string', enum: ['india', 'other', 'both'], example: 'india', description: 'preference where the campaign is targetted. india-only india, other-all countries except india, both-worldwide' },
      msg: {
        type: 'string',
        example: 'this is a message',
        description: 'message',
      },
      save_template_yn: { type: 'integer', example: 1, description: 'save this in saved template. 1-yes 0-no ' },
      is_static: { type: 'integer', enum: [0, 1, null], example: 1, description: 'is the message static or dynamic 0-dynamic, 1-static' },
      is_unicode: { type: 'integer', enum: [0, 1], example: 1, description: 'is the template content 0-english, 1-unicode' },
      t_name: { type: 'string', example: 'Template Name' },
      t_type: { type: 'string', example: 'Template Type <column/index>' },
      t_mobile_column: { type: 'string', example: 'Mobile column. value may be a column name or index' },
    },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        statusCode: { type: 'integer', example: 201, description: 'Campaign Scheduled Successfully' },
        message: { type: 'string', example: 'Campaign Scheduled Successfully' },
      },
      description: 'Scheduled Successfully',
    },
    400: response400,
    500: response500,

  },
};

const cgSendSchema = {
  summary: 'Send Group Campaign',
  tags: ['Campaign Group'],
  body: {
    type: 'object',
    required: ['c_name', 'remove_dupe_yn', 'msg', 'c_type', 'c_lang_type', 'c_lang', 'group_ids', 'traffic_to'],
    properties: {
      c_name: { type: 'string', example: 'campaign 1', description: 'campaign name' },
      shorten_url_yn: { type: 'integer', enum: [0, 1], example: 1, description: '1 - enabled, 0- disabled' },
      remove_dupe_yn: { type: 'integer', enum: [0, 1], example: '1', description: 'remove duplicate' },
      msg: { type: 'string', example: 'this is a message', description: 'message content' },
      c_type: { type: 'string', enum: ['group'], example: 'group', description: 'the type of the campaign' },
      c_lang_type: { type: 'string', enum: ['english', 'unicode'], example: 'unicode', description: 'is message unicode or english' },
      c_lang: { type: 'string', example: '', description: 'empty for now' },
      template_id: { type: 'string', example: 'w99ldvffgai7vinx20bexmnq3u5eaywcp3c9' },
      dlt_entity_id: { type: 'string', example: '110100001352' },
      header: { type: 'string', example: 'Sender id' },
      group_ids: {
        type: 'array',
        items: { type: 'string' },
        minItems: 1,
        uniqueItems: true,
      },
      exclude_group_ids: {
        type: 'array',
        items: { type: 'string' },
      },
      dlt_template_id: { type: 'string', example: '1000000000000000626', description: 'DLT template id' },
      intl_header: { type: 'string', example: 'Intl Sender id' },
      traffic_to: { type: 'string', enum: ['india', 'other', 'both'], example: 'india', description: 'preference where the campaign is targetted. india-only india, other-all countries except india, both-worldwide' },
    },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        statusCode: { type: 'integer', example: '200/-602', description: 'Campaign Sent Successfully/No wallet balance' },
        message: { type: 'string', example: 'Campaign sent for processing ' },
      },
      description: 'Sent Successfully',
    },
    400: response400,
    500: response500,

  },
};

const cgScheduleSchema = {
  summary: 'Schedule Group Campaign',
  tags: ['Campaign Group'],
  body: {
    type: 'object',
    required: ['c_name', 'remove_dupe_yn', 'msg', 'c_type', 'c_lang_type', 'c_lang', 'dlt_entity_id', 'header', 'group_ids', 'schedule_list', 'traffic_to'],
    properties: {
      c_name: { type: 'string', example: 'campaign 1', description: 'campaign name' },
      shorten_url_yn: { type: 'integer', enum: [0, 1], example: 1, description: '1 - enabled, 0- disabled' },
      remove_dupe_yn: { type: 'integer', enum: [0, 1], example: '1', description: 'remove duplicate' },
      msg: { type: 'string', example: 'this is a message', description: 'message content' },
      c_type: { type: 'string', enum: ['group'], example: 'group', description: 'the type of the campaign' },
      c_lang_type: { type: 'string', enum: ['english', 'unicode'], example: 'unicode', description: 'is message unicode or english' },
      c_lang: { type: 'string', example: '', description: 'empty for now' },
      template_id: { type: 'string', example: 'w99ldvffgai7vinx20bexmnq3u5eaywcp3c9' },
      dlt_entity_id: { type: 'string', example: '110100001352' },
      header: { type: 'string', example: 'Sender id' },
      group_ids: {
        type: 'array',
        items: { type: 'string' },
        minItems: 1,
        uniqueItems: true,
      },
      exclude_group_ids: {
        type: 'array',
        items: { type: 'string' },
      },
      schedule_list: {
        type: 'array',
        items: {
          type: 'object',
          required: ['dt', 'time', 'zone_name'],
          properties: {
            dt: { type: 'string', example: '2021-07-16', description: 'Schedule date selected (no conversion). Format => YYYY-MM-DD' },
            time: { type: 'string', example: '13:01:00', description: 'Schedule time selected (no conversion). Format => HH24:mm:ss' },
            zone_name: { type: 'string', example: 'America/Los_Angeles', description: 'Zone name of the selected timezone' },
          },
        },
      },
      dlt_template_id: { type: 'string', example: '1000000000000000626', description: 'DLT template id' },
      intl_header: { type: 'string', example: 'Intl Sender id' },
      traffic_to: { type: 'string', enum: ['india', 'other', 'both'], example: 'india', description: 'preference where the campaign is targetted. india-only india, other-all countries except india, both-worldwide' },
    },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        statusCode: { type: 'integer', example: 201, description: 'Campaign Scheduled Successfully' },
        message: { type: 'string', example: 'Campaign Scheduled Successfully' },
      },
      description: 'Scheduled Successfully',
    },
    400: response400,
    500: response500,

  },
};

const clistSchema = {
  summary: 'List Campaigns',
  tags: ['Campaign'],
  body: {
    type: 'object',
    required: ['dateselectiontype'],
    properties: {
      dateselectiontype: { type: 'string', enum: ['custom range', 'yesterday', 'today', 'last 7 days', 'last 15 days', 'last 30 days', 'this week', 'this month'], example: 'last 7 days', description: 'the selected date type' },
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
          id: { type: 'string', example: '707l86ag4xazet7n9b5hhxi2vren4h7821lm', description: 'campaign id' },
          c_name: { type: 'string', example: 'campaign 1', description: 'campaign name' },
          msg: { type: 'string', example: 'this is a message', description: 'message content' },
          header: { type: 'string', example: 'Sender id' },
          intl_header: { type: 'string', example: 'Intl Sender id' },
          template_id: { type: 'string', example: 'w99ldvffgai7vinx20bexmnq3u5eaywcp3c9' },
          dlt_entity_id: { type: 'string', example: '110100001352', description: 'DLT Entity id' },
          dlt_template_id: { type: 'string', example: '1000000000000000626', description: 'DLT template id' },
          c_type: { type: 'string', example: 'group', description: 'the type of the campaign' },
          c_lang_type: { type: 'string', enum: ['english', 'unicode'], example: 'unicode', description: 'is message unicode or english' },
          remove_dupe_yn: { type: 'integer', enum: [0, 1], example: '1', description: 'remove duplicate' },
          status: { type: 'string', example: 'completed', description: 'Current state of the campaign' },
          total: { type: 'integer', example: '34543', description: 'Total count' },
          total_human: { type: 'string', example: '34K', description: 'Total count in human readable form' },
          excluded: { type: 'integer', example: '34543', description: 'Total excluded count' },
          excluded_human: {
            type: 'string',
            example: '34K',
            description: 'Total excluded count in human readable form',
          },
          count: { type: 'integer', example: '3', description: 'Total File/Group uploaded' },
          exclude_group_count: { type: 'integer', example: '3', description: 'Total exclude groups used for the campaign' },
          created_ts: { type: 'string', example: 'Created date & time e.g Jun 20 2021 14:20 IST' },
          created_ts_unix: { type: 'integer', example: 1318874398 },
          time_from_now: { type: 'string', example: '2 month ago', description: 'human readable form of timeago or relative time' },
        },
        description: 'Successful response',
      },
    },
    400: response400,
    500: response500,

  },
};

const ctodaysstatsSchema = {
  summary: 'Todays campaign stats',
  tags: ['Campaign'],
  querystring: {
    type: 'object',
    required: [],
    properties: {},
  },
  response: {
    200: {
      type: 'object',
      properties: {
        total: { type: 'integer', example: '4', description: 'Todays Total count' },
        completed: { type: 'integer', example: '1', description: 'Todays Total completed campaigns at platform' },
        running: { type: 'integer', example: '2', description: 'Todays Total running campaigns at platform' },
        failed: { type: 'integer', example: '1', description: 'Todays Total failed campaigns' },

      },
      description: 'Successful response',
    },
    400: response400,
    500: response500,

  },
};

const cdetailsSchema = {
  summary: 'Campaign details',
  tags: ['Campaign'],
  querystring: {
    type: 'object',
    required: ['c_id'],
    properties: { c_id: { type: 'string', example: '707l86ag4xazet7n9b5hhxi2vren4h7821lm', description: 'campaign id' } },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        id: { type: 'string', example: '707l86ag4xazet7n9b5hhxi2vren4h7821lm', description: 'campaign id' },
        c_name: { type: 'string', example: 'campaign 1', description: 'campaign name' },
        msg: { type: 'string', example: 'this is a message', description: 'message content' },
        header: { type: 'string', example: 'Sender id' },
        intl_header: { type: 'string', example: 'Intl Sender id' },
        template_id: { type: 'string', example: 'w99ldvffgai7vinx20bexmnq3u5eaywcp3c9' },
        dlt_entity_id: { type: 'string', example: '110100001352', description: 'DLT Entity id' },
        dlt_template_id: { type: 'string', example: '1000000000000000626', description: 'DLT template id' },
        c_type: { type: 'string', example: 'group', description: 'the type of the campaign' },
        c_lang_type: { type: 'string', enum: ['english', 'unicode'], example: 'unicode', description: 'is message unicode or english' },
        remove_dupe_yn: { type: 'integer', enum: [0, 1], example: 1, description: 'remove duplicate' },
        shorten_url: { type: 'integer', enum: [0, 1], example: 1, description: '0-Disable, 1-Enable' },
        status: { type: 'string', example: 'completed', description: 'Current state of the campaign' },
        total: { type: 'integer', example: '34543', description: 'Total count' },
        total_human: { type: 'string', example: '34K', description: 'Total count in human readable form' },
        count: { type: 'integer', example: '3', description: 'Total File/Group uploaded' },
        exclude_group_count: { type: 'integer', example: '3', description: 'Total exclude groups used for the campaign' },
        created_ts: { type: 'string', example: 'Created date & time e.g Jun 20 2021 14:20 IST' },
        created_ts_unix: { type: 'integer', example: 1318874398 },
        time_from_now: { type: 'string', example: '2 month ago', description: 'human readable form of timeago or relative time' },
      },
      description: 'Successful response',
    },
    400: response400,
    500: response500,

  },
};

const cdetailsbyfileSchema = {
  summary: 'Campaign details by files/groups',
  tags: ['Campaign'],
  querystring: {
    type: 'object',
    required: ['c_id'],
    properties: { c_id: { type: 'string', example: '707l86ag4xazet7n9b5hhxi2vren4h7821lm', description: 'campaign id' } },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        c_type: { type: 'string', example: 'group', description: 'campaign type' },
        processed_count: { type: 'integer', example: 3, description: 'Total processed count' },
        processed_count_human: { type: 'string', example: '3K', description: 'Total processed count in human readable form' },
        asof_ts: { type: 'string', example: 'As of date & time e.g Jun 20 2021 14:20 IST' },
        count: { type: 'integer', example: 3, description: 'Total file/group counts' },
        completed_count: { type: 'integer', example: 1, description: 'Total file/group that has completed status' },
        completion_percentage: { type: 'integer', example: 17, description: 'File/Group completion percentage' },
        data: {
          type: 'array',
          items: {
            type: 'object',
            properties: {
              g_name: { type: 'string', example: 'Group Name 1', description: 'Group name' },
              filetype: { type: 'string', example: '.xlsx', description: 'File type (.xlsx/.xls/.csv)' },
              filename_ori: { type: 'string', example: 'XLSX 10K fmm.xlsx', description: 'File name uploaded by user' },
              status: { type: 'string', example: 'completed', description: 'Current state of the campaign files' },
              total: { type: 'integer', example: '34543', description: 'Total count' },
              total_human: { type: 'string', example: '34K', description: 'Total count in human readable form' },
              valid: { type: 'integer', example: '34543', description: 'Total valid count' },
              valid_human: { type: 'string', example: '34K', description: 'Total valid count in human readable form' },
              invalid: { type: 'integer', example: '34543', description: 'Total invalid count' },
              invalid_human: {
                type: 'string',
                example: '34K',
                description: 'Total invalid count in human readable form',
              },
              duplicate: { type: 'integer', example: '34543', description: 'Total duplicate count' },
              duplicate_human: {
                type: 'string',
                example: '34K',
                description: 'Total duplicate count in human readable form',
              },
              excluded: { type: 'integer', example: '34543', description: 'Total excluded count' },
              excluded_human: {
                type: 'string',
                example: '34K',
                description: 'Total excluded count in human readable form',
              },
              failed: { type: 'integer', example: '34543', description: 'Total failed count' },
              failed_human: {
                type: 'string',
                example: '34K',
                description: 'Total  failed count in human readable form',
              },
              processed_count: { type: 'integer', example: '3', description: 'Total processed count' },
              processed_count_human: { type: 'string', example: '3K', description: 'Total processed count in human readable form' },
              exclude_group_count: {
                type: 'integer',
                example: '3',
                description: 'Total exclude groups used for the campaign',
              },
            },
            description: 'Successful response',
          },
        },
      },
    },
    400: response400,
    500: response500,

  },
};

const cprocessedstatsSchema = {
  summary: 'Processed count',
  tags: ['Campaign'],
  querystring: {
    type: 'object',
    required: ['c_id'],
    properties: { c_id: { type: 'string', example: 'd34r2dfdfewgf34t3453f34', description: 'campaign id' } },
  },
  response: {
    // 200: {
    //     type: 'object',
    //     properties: {
    //         today: { type: 'integer', example: 4, description: 'Total received today' },
    //         thisweek: { type: 'integer', example: 1, description: 'Total received this week' },
    //         thismonth: { type: 'integer', example: 2, description: 'Total received this month' },
    //         lastmonth: { type: 'integer', example: 1, description: 'Total received last month' },
    //         today_human: { type: 'string', example: '4', description: 'Total received today in human form' },
    //         thisweek_human: { type: 'string', example: '1', description: 'Total received this week in human form' },
    //         thismonth_human: { type: 'string', example: '2', description: 'Total received this month in human form' },
    //         lastmonth_human: { type: 'string', example: '1', description: 'Total received last month in human form' },
    //
    //     },
    //     description: 'Successful response',
    // },
    400: response400,
    500: response500,

  },
};

const cschedstatsSchema = {
  summary: 'Scheduled campaign stats',
  tags: ['Campaign'],
  querystring: {
    type: 'object',
    required: [],
    properties: {},
  },
  response: {
    200: {
      type: 'object',
      properties: {
        today: { type: 'integer', example: '4', description: 'Total scheduled today' },
        thisweek: { type: 'integer', example: '1', description: 'Scheduled for this week' },
        thismonth: { type: 'integer', example: '2', description: 'Scheduled for this month' },
        nextmonth: { type: 'integer', example: '1', description: 'Scheduled for next month' },

      },
      description: 'Successful response',
    },
    400: response400,
    500: response500,

  },
};

const cslistSchema = {
  summary: 'List Scheduled Campaigns',
  tags: ['Campaign'],
  body: {
    type: 'object',
    required: ['dateselectiontype'],
    properties: {
      dateselectiontype: { type: 'string', enum: ['custom range', 'today', 'next 7 days', 'next 15 days', 'next 30 days', 'this week', 'this month'], example: 'last 7 days', description: 'the selected date type' },
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
          id: { type: 'string', example: '8t28z62sccbbhkr4y9lf4xctwlaj023tju3e', description: 'campaign id' },
          at_id: { type: 'string', example: 'r1znjbyldi03rvo7tap85uviqe20pn4mr884', description: 'sched at id' },
          c_name: { type: 'string', example: 'campaign 1', description: 'campaign name' },
          msg: { type: 'string', example: 'this is a message', description: 'message content' },
          header: { type: 'string', example: 'Sender id' },
          intl_header: { type: 'string', example: 'Intl Sender id' },
          template_id: { type: 'string', example: 'w99ldvffgai7vinx20bexmnq3u5eaywcp3c9' },
          dlt_entity_id: { type: 'string', example: '110100001352', description: 'DLT Entity id' },
          dlt_template_id: { type: 'string', example: '1000000000000000626', description: 'DLT template id' },
          c_type: { type: 'string', example: 'group', description: 'the type of the campaign' },
          c_lang_type: { type: 'string', enum: ['english', 'unicode'], example: 'unicode', description: 'is message unicode or english' },
          remove_dupe_yn: { type: 'integer', enum: [0, 1], example: '1', description: 'remove duplicate' },
          status: { type: 'string', example: 'completed', description: 'Current state of the campaign' },
          total: { type: 'integer', example: '34543', description: 'Total count' },
          total_human: { type: 'string', example: '34K', description: 'Total count in human readable form' },
          count: { type: 'integer', example: '3', description: 'Total File/Group uploaded' },
          exclude_group_count: { type: 'integer', example: '3', description: 'Total exclude groups used for the campaign' },
          created_ts: { type: 'string', example: 'Created date & time e.g Jun 20 2021 14:20 IST' },
          created_ts_unix: { type: 'integer', example: 1318874398 },
          scheduled_ts: { type: 'string', example: 'Created date & time e.g Jun 20 2021 14:20 IST' },
          scheduled_ts_unix: { type: 'integer', example: 1318874398 },
          selected_zone: { type: 'string', example: 'Asia/Calcutta', description: 'the selected tz of the campaign' },
          selected_dt: { type: 'string', example: '', description: 'the selected tz of the campaign' },
        },
        description: 'Successful response',
      },
    },
    400: response400,
    500: response500,

  },
};

const csdetailsSchema = {
  summary: 'Scheduled Campaign details',
  tags: ['Campaign'],
  querystring: {
    type: 'object',
    required: ['c_id', 'at_id'],
    properties: {
      c_id: { type: 'string', example: '8t28z62sccbbhkr4y9lf4xctwlaj023tju3e', description: 'campaign id' },
      at_id: { type: 'string', example: 'r1znjbyldi03rvo7tap85uviqe20pn4mr884', description: 'sched at id' },
    },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        c_id: { type: 'string', example: '8t28z62sccbbhkr4y9lf4xctwlaj023tju3e', description: 'campaign id' },
        at_id: { type: 'string', example: 'r1znjbyldi03rvo7tap85uviqe20pn4mr884', description: 'sched at id' },
        c_name: { type: 'string', example: 'campaign 1', description: 'campaign name' },
        msg: { type: 'string', example: 'this is a message', description: 'message content' },
        header: { type: 'string', example: 'Sender id' },
        intl_header: { type: 'string', example: 'Intl Sender id' },
        template_id: { type: 'string', example: 'w99ldvffgai7vinx20bexmnq3u5eaywcp3c9' },
        dlt_entity_id: { type: 'string', example: '110100001352', description: 'DLT Entity id' },
        dlt_template_id: { type: 'string', example: '1000000000000000626', description: 'DLT template id' },
        c_type: { type: 'string', example: 'group', description: 'the type of the campaign' },
        c_lang_type: { type: 'string', enum: ['english', 'unicode'], example: 'unicode', description: 'is message unicode or english' },
        remove_dupe_yn: { type: 'integer', enum: [0, 1], example: '1', description: 'remove duplicate' },
        status: { type: 'string', example: 'completed', description: 'Current state of the campaign' },
        total: { type: 'integer', example: '34543', description: 'Total count' },
        total_human: { type: 'string', example: '34K', description: 'Total count in human readable form' },
        count: { type: 'integer', example: '3', description: 'Total File/Group uploaded' },
        exclude_group_count: { type: 'integer', example: '3', description: 'Total exclude groups used for the campaign' },
        created_ts: { type: 'string', example: 'Created date & time e.g Jun 20 2021 14:20 IST' },
        created_ts_unix: { type: 'integer', example: 1318874398 },
        time_from_now: { type: 'string', example: 'in 2 days', description: 'human readable form of timeago or relative time' },
        scheduled_ts: { type: 'string', example: 'Created date & time e.g Jun 20 2021 14:20 IST' },
        scheduled_ts_unix: { type: 'integer', example: 1318874398 },
        scheduled_date: { type: 'string', example: '2021-08-31', description: 'scheduled date in yyyy-MM-dd format' },
        scheduled_time: { type: 'string', example: '14:30', description: 'scheduled time in HH24:mm format' },
        selected_zone: { type: 'string', example: 'Asia/Calcutta', description: 'zone name of the selected tz' },
        selected_dt: { type: 'string', example: '', description: 'the selected tz of the campaign' },
        files: {
          type: 'array',
          items: {
            type: 'object',
            properties: {
              r_filename: { type: 'string', example: 'cotm_file1.xls', description: 'remote file name' },
              count: { type: 'integer', example: 3456, description: 'total count from the upload api' },
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

const csdeleteSchema = {
  summary: 'Delete a particular sched campaign',
  tags: ['Campaign'],
  body: {
    type: 'object',
    required: ['c_id', 'at_id'],
    properties: {
      c_id: { type: 'string', example: '8t28z62sccbbhkr4y9lf4xctwlaj023tju3e', description: 'campaign id' },
      at_id: { type: 'string', example: 'r1znjbyldi03rvo7tap85uviqe20pn4mr884', description: 'sched at id' },
    },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        statusCode: { type: 'integer', example: 200, description: 'Successfully deleted' },
        message: { type: 'string', example: 'Campaign has been successfully deleted' },
      },
      description: 'Successful deletion',
    },
    400: response400,
    500: response500,
  },
};

const csupdateSchema = {
  summary: 'Update a particular sched campaign',
  tags: ['Campaign'],
  body: {
    type: 'object',
    required: ['c_id', 'at_id', 'scheduled_date', 'scheduled_time', 'scheduled_zone'],
    properties: {
      c_id: { type: 'string', example: '8t28z62sccbbhkr4y9lf4xctwlaj023tju3e', description: 'campaign id' },
      at_id: { type: 'string', example: 'r1znjbyldi03rvo7tap85uviqe20pn4mr884', description: 'sched at id' },
      scheduled_date: { type: 'string', example: '2021-07-16', description: 'Schedule date selected (no conversion). Format => YYYY-MM-DD' },
      scheduled_time: { type: 'string', example: '13:01:00', description: 'Schedule time selected (no conversion). Format => HH24:mm:ss' },
      scheduled_zone: { type: 'string', example: 'America/Los_Angeles', description: 'Zone name of the selected timezone' },
    },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        statusCode: { type: 'integer', example: 200, description: 'Successfully deleted' },
        message: { type: 'string', example: 'Campaign has been successfully updated' },
      },
      description: 'Successful updation',
    },
    400: response400,
    500: response500,
  },
};

module.exports = {
  msginfoSchema,
  cnameuniqueSchema,
  cqSendSchema,
  cotmSendSchema,
  cmtmSendSchema,
  ctSendSchema,
  cgSendSchema,
  cqScheduleSchema,
  cotmScheduleSchema,
  cmtmScheduleSchema,
  cgScheduleSchema,
  ctScheduleSchema,
  clistSchema,
  ctodaysstatsSchema,
  cdetailsSchema,
  cdetailsbyfileSchema,
  cschedstatsSchema,
  cslistSchema,
  csdetailsSchema,
  csdeleteSchema,
  csupdateSchema,
  cprocessedstatsSchema,
};
