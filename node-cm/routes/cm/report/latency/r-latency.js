/* eslint-disable no-restricted-syntax */
const _ = require('lodash');
const mtz = require('moment-timezone');
const moment = require('moment');

const { rLatencyTableSchema, rLatencyPieChartSchema, rLatencyStackChartSchema } = require('../../../../schema/report-latency-schema');
const { reject } = require('lodash');

/**
 * setup some routes
 * @param {import("fastify").FastifyInstance} fastify
 * @param {*} opts
 */
async function rlatency(fastify, opts) {
    fastify.addHook('preValidation', async (req, reply) => {
        //console.log('preValidation hook called');
        return req.jwtVerify();
    });

    async function latencyReportTable(req, path, table) {
        const { dateselectiontype, fdate, tdate } = req.body;
        const { tz } = req.user;
        let cli_id = [];
        const cli_id_req = req.body.cli_id;

        req.log.debug(`latencyReportTable ${path} - inputs cli_id ${cli_id_req}  - dateselectiontype ${dateselectiontype} - fdate ${fdate} - tdate ${tdate}`);

        const cli_id_wise_name = {};
        if(_.isUndefined(cli_id_req) || _.isNull(cli_id_req) || _.isEmpty(_.trim(cli_id_req))){
            cli_id.push(req.user.cli_id);
            cli_id_wise_name[req.user.cli_id] = req.user.user;
        }else if(_.isEqual(_.toLower(_.trim(cli_id_req)), 'all')){
            // add login user
            cli_id.push(req.user.cli_id);
            cli_id_wise_name[req.user.cli_id] = req.user.user;
            // add subusers under login user
            const result1 = await fastify.getAllUsersForId(req.user.cli_id);
            for (const row of result1) {
                cli_id.push(row.cli_id);
                cli_id_wise_name[row.cli_id] = row.user;
            }
        }else{
            cli_id.push(Number(cli_id_req));
            const result1 = await fastify.findUserById(cli_id);
            cli_id_wise_name[cli_id] = _.get(result1[0], 'user', '');
        }

        const payload = [];

        // timezone is not supported for T-1 data
        const { fromdateStrInIST, todateStrInIST, typeofdate } = await fastify.getFromAndToDate(process.env.IST_ZONE_NAME, dateselectiontype, fdate, tdate, true);
        // for es
        const { fromdatetimeStrInIST: todayfts, todatetimeStrInIST: todaytts } = await fastify.getFromAndToDate(tz, 'today', null, null, true);
        req.log.debug(`latencyReportTable ${path} - from and to date => ${fromdateStrInIST}, ${todateStrInIST}, ${todayfts}, ${todaytts}, ${typeofdate}`);
        
        let result = [];
        if (_.eq(typeofdate, 'onlytoday') || _.eq(typeofdate, 'both')) {
            console.log("today§§§---",todayfts, fromdateStrInIST)
            
            const todayCnt = await fastify.getTodaysLatencyForTable(cli_id, todayfts, todaytts, table); 
            
            const sBucket = _.get(todayCnt, 'aggregations.latency.buckets', []);
            for (let i = 0; i < sBucket.length; i++) {
                const timeRangeMilliSecs = _.get(sBucket[i].tel_count, 'buckets', []);

                if(sBucket[i].doc_count > 0){

                    const perCli = {
                        recv_date : todayfts ,
                        cli_id: sBucket[i].key,
                        lat_0_5 : _.filter(timeRangeMilliSecs,(x) => x.key == "*-5500.0")[0].doc_count,
                        lat_6_10 : _.filter(timeRangeMilliSecs,(x) => x.key == "5500.0-10500.0")[0].doc_count,
                        lat_11_15 : _.filter(timeRangeMilliSecs,(x) => x.key == "10500.0-15500.0")[0].doc_count,
                        lat_16_30: _.filter(timeRangeMilliSecs,(x) => x.key == "15500.0-30500.0")[0].doc_count,
                        lat_31_45: _.filter(timeRangeMilliSecs,(x) => x.key == "30500.0-45500.0")[0].doc_count,
                        lat_46_60 : _.filter(timeRangeMilliSecs,(x) => x.key == "45500.0-60500.0")[0].doc_count,
                        lat_61_120 : _.filter(timeRangeMilliSecs,(x) => x.key == "60500.0-120500.0")[0].doc_count,
                        lat_gt_120 : _.filter(timeRangeMilliSecs,(x) => x.key == "120500.0-*")[0].doc_count,
                        tot_cnt: sBucket[i].doc_count
                    };
                  
                    result.push(perCli)
                }
              }
        }
        if (_.eq(typeofdate, 'both')) {
            // get the date for prev days
            const r1 = await fastify.getLatencyForTable(fromdateStrInIST, todateStrInIST, cli_id, table);
            // todays is already added above
            result.push(...r1);
            
                
        }
        if (_.eq(typeofdate, 'onlyprev')) {
            // get the date for prev days
            const r1 = await fastify.getLatencyForTable(fromdateStrInIST, todateStrInIST, cli_id, table);
            result = r1;
        }
       
        for (const r of result) {            
            const recv_date = moment(r.recv_date).format('DD-MMM-YYYY');          
            const lat_0_5 = Number(_.isNull(r.lat_0_5) ? 0 : r.lat_0_5);
            const lat_6_10 = Number(_.isNull(r.lat_6_10) ? 0 : r.lat_6_10);
            const lat_11_15 = Number(_.isNull(r.lat_11_15) ? 0 : r.lat_11_15);
            const lat_16_30 = Number(_.isNull(r.lat_16_30) ? 0 : r.lat_16_30);
            const lat_31_45 = Number(_.isNull(r.lat_31_45) ? 0 : r.lat_31_45);
            const lat_46_60 = Number(_.isNull(r.lat_46_60) ? 0 : r.lat_46_60);
            const lat_61_120 = Number(_.isNull(r.lat_61_120) ? 0 : r.lat_61_120);
            const lat_gt_120 = Number(_.isNull(r.lat_gt_120) ? 0 : r.lat_gt_120);
            const total = Number(_.isNull(r.tot_cnt) ? 0 : r.tot_cnt);

            const obj = {
                recv_date,
                username: _.get(cli_id_wise_name, r.cli_id, ''),
                lat_0_5,
                lat_6_10,
                lat_11_15,
                lat_16_30,
                lat_31_45,
                lat_46_60,
                lat_61_120,
                lat_gt_120,
                total
            };
            
            // TODO: Sorting
            payload.push(obj);
        }
        
        req.log.debug(`latencyReportTable ${path} - response - ${JSON.stringify(payload)}`);
        return payload;
    }

    fastify.post('/platform/latencytable', { preValidation: [], schema: rLatencyTableSchema }, async (req, reply) => {
        try {
            const table = 'summary.ui_platform_latency_report';
            return await latencyReportTable(req, '/platform/latencytable', table);
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not get platform latency data. Please try again', { err });
            return e;
        }
    });

    fastify.post('/telco/latencytable', { preValidation: [], schema: rLatencyTableSchema }, async (req, reply) => {
        try {
            const table = 'summary.ui_telco_latency_report';
            return await latencyReportTable(req, '/telco/latencytable', table);
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not get telco latency data. Please try again', { err });
            return e;
        }
    });

    latencyReportPieChart = async (req, path, table) => {
        const { dateselectiontype, fdate, tdate } = req.body;
        const { tz } = req.user;
        let cli_id = [];
        const cli_id_req = req.body.cli_id;

        req.log.debug(`latencyReportPieChart ${path} - inputs cli_id ${cli_id_req}  - dateselectiontype ${dateselectiontype} - fdate ${fdate} - tdate ${tdate}`);

        const cli_id_wise_name = {};
        if(_.isUndefined(cli_id_req) || _.isNull(cli_id_req) || _.isEmpty(_.trim(cli_id_req))){
            cli_id.push(req.user.cli_id);
            cli_id_wise_name[req.user.cli_id] = req.user.user;
        }else if(_.isEqual(_.toLower(_.trim(cli_id_req)), 'all')){
            // add login user
            cli_id.push(req.user.cli_id);
            cli_id_wise_name[req.user.cli_id] = req.user.user;
            // add subusers under login user
            const result1 = await fastify.getAllUsersForId(req.user.cli_id);
            for (const row of result1) {
                cli_id.push(row.cli_id);
                cli_id_wise_name[row.cli_id] = row.user;
            }
        }else{
            cli_id.push(Number(cli_id_req));
            const result1 = await fastify.findUserById(cli_id);
            cli_id_wise_name[cli_id] = _.get(result1[0], 'user', '');
        }

        // timezone is not supported for T-1 data
        const { fromdateStrInIST, todateStrInIST, typeofdate } = await fastify.getFromAndToDate(process.env.IST_ZONE_NAME, dateselectiontype, fdate, tdate, true);
        // for es
        const { fromdatetimeStrInIST: todayfts, todatetimeStrInIST: todaytts } = await fastify.getFromAndToDate(tz, 'today', null, null, true);
        req.log.debug(`latencyReportPieChart ${path} - from and to date => ${fromdateStrInIST}, ${todateStrInIST}, ${todayfts}, ${todaytts}, ${typeofdate}`);
        
        const finalObj = { table: [], data: [], colors: [], labels:[] };
        let result = [];
        try {
            // get the data from datastore
            if (_.eq(typeofdate, 'onlytoday') || _.eq(typeofdate, 'both')) {
                const perCli = await fastify.getTodaysLatencyOnlyDateWise(cli_id, todayfts, todaytts, table); 
                result.push(perCli);
            }
            if (_.eq(typeofdate, 'both')) {
                // get the date for prev days
                const r1 = await fastify.getLatencyForPieChart(fromdateStrInIST, todateStrInIST, cli_id, table);
                // todays is already added above
                result.push(...r1);
            }
            if (_.eq(typeofdate, 'onlyprev')) {
                // get the date for prev days
                const r1 = await fastify.getLatencyForPieChart(fromdateStrInIST, todateStrInIST, cli_id, table);
                result = r1;
            }
            //const result = await fastify.getLatencyForPieChart(fromdateStrInIST, todateStrInIST, cli_id, table);
          
            
            if (result.length > 0) {
                
                
                const sum_lat_0_5 = _.sumBy(result,(o) => Number(o.lat_0_5) ) ;
                const sum_lat_6_10 = _.sumBy(result,(o) =>  Number(o.lat_6_10) ) ;
                const sum_lat_11_15 = _.sumBy(result,(o) => Number(o.lat_11_15 ) );
                const sum_lat_16_30 = _.sumBy(result,(o) => Number(o.lat_16_30) );
                const sum_lat_31_45 = _.sumBy(result,(o) => Number(o.lat_31_45) );
                const sum_lat_46_60 = _.sumBy(result,(o) => Number(o.lat_46_60) );
                const sum_lat_61_120 = _.sumBy(result,(o) => Number(o.lat_61_120)) ;
                const sum_lat_gt_120 = _.sumBy(result,(o) => Number(o.lat_gt_120)) ;
                const sum_tot_cnt = _.sumBy(result,(o) => Number(o.tot_cnt) );
                
                finalObj.table.push({
                    "latency":"0-5 sec",
                    "delivered":sum_lat_0_5,
                    "rate": Number(sum_lat_0_5 /sum_tot_cnt *100).toFixed(2),
                    "color":"#5E548E"
                });
                finalObj.table.push({
                    "latency":"6-10 sec",
                    "delivered":sum_lat_6_10,
                    "rate": Number(sum_lat_6_10 /sum_tot_cnt *100).toFixed(2),
                    "color":"#B8BEDD"
                });
                finalObj.table.push({
                    "latency":"11-15 sec",
                    "delivered":sum_lat_11_15,
                    "rate": Number(sum_lat_11_15 /sum_tot_cnt *100).toFixed(2),
                    "color":"#B5F0EE"
                });
                finalObj.table.push({
                    "latency":"16-30 sec",
                    "delivered":sum_lat_16_30,
                    "rate": Number(sum_lat_16_30 /sum_tot_cnt *100).toFixed(2),
                    "color":"#EFC3E6"
                });
                finalObj.table.push({
                    "latency":"31-45 sec",
                    "delivered":sum_lat_31_45,
                    "rate": Number(sum_lat_31_45 /sum_tot_cnt *100).toFixed(2),
                    "color":"#F0A6CA"
                });
                finalObj.table.push({
                    "latency":"46-60 sec",
                    "delivered":sum_lat_46_60,
                    "rate": Number(sum_lat_46_60 /sum_tot_cnt *100).toFixed(2),
                    "color":"#BE95C4"
                });
                finalObj.table.push({
                    "latency":"61-120 sec",
                    "delivered":sum_lat_61_120,
                    "rate": Number(sum_lat_61_120 /sum_tot_cnt *100).toFixed(2),
                    "color":"#9F86C0"
                });
                finalObj.table.push({
                    "latency":"> 120 sec",
                    "delivered":sum_lat_gt_120,
                    "rate": Number(sum_lat_gt_120 /sum_tot_cnt *100).toFixed(2),
                    "color":"#76427A"
                });
                finalObj.delivered = sum_tot_cnt;
                finalObj.data = [];
                if (Number(sum_lat_0_5) !== 0) {
                    finalObj.data.push(sum_lat_0_5);
                    finalObj.colors.push('#5E548E');
                }
                if (Number(sum_lat_6_10) !== 0) {
                    finalObj.data.push(sum_lat_6_10);
                    finalObj.colors.push('#B8BEDD');
                }
                if (Number(sum_lat_11_15) !== 0) {
                    finalObj.data.push(sum_lat_11_15);
                    finalObj.colors.push('#B5F0EE');
                }
                if (Number(sum_lat_16_30) !== 0) {
                    finalObj.data.push(sum_lat_16_30);
                    finalObj.colors.push('#EFC3E6');
                }
                if (Number(sum_lat_31_45) !== 0) {
                    finalObj.data.push(sum_lat_31_45);
                    finalObj.colors.push('#F0A6CA');
                }
                if (Number(sum_lat_46_60) !== 0) {
                    finalObj.data.push(sum_lat_46_60);
                    finalObj.colors.push('#BE95C4');
                }
                if (Number(sum_lat_61_120) !== 0) {
                    finalObj.data.push(sum_lat_61_120);
                    finalObj.colors.push('#9F86C0');
                }
                if (Number(sum_lat_gt_120) !== 0) {
                    finalObj.data.push(sum_lat_gt_120);
                    finalObj.colors.push('#76427A');
                }
            }
            return finalObj;
        } catch (e) {
            req.log.error(`latencyReportPieChart ${path} - error stack =>`, e);
            throw e;
        }
    };

    fastify.post('/platform/latencypiechart', { preValidation: [], schema: rLatencyPieChartSchema }, async (req, reply) => {
        try {
            const table = 'summary.ui_platform_latency_report';
            return await latencyReportPieChart(req, '/platform/latencypiechart', table);
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not get platform latency data. Please try again', { err });
            return e;
        }
    });

    fastify.post('/telco/latencypiechart', { preValidation: [], schema: rLatencyPieChartSchema }, async (req, reply) => {
        try {
            const table = 'summary.ui_telco_latency_report';
            return await latencyReportPieChart(req, '/telco/latencypiechart', table);
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not get telco latency data. Please try again', { err });
            return e;
        }
    });

    latencyReportStackedChart = async (req, path, table) => {
        const { dateselectiontype, fdate, tdate } = req.body;
        const { tz } = req.user;
        let cli_id = [];
        const cli_id_req = req.body.cli_id;
        let latency = req.body.latency;
        const latency_ranges = ['all', 'lat_0_5','lat_6_10','lat_11_15', 'lat_16_30', 'lat_31_45', 'lat_46_60', 'lat_61_120', 'lat_gt_120'];

        req.log.debug(`latencyReportStackedChart ${path} - inputs cli_id ${cli_id_req}  - dateselectiontype ${dateselectiontype} - fdate ${fdate} - tdate ${tdate}  - latency ${latency}`);

        if(_.isUndefined(latency) || _.isNull(latency) || _.isEmpty(_.trim(latency))){
            latency = 'all';
        } else{
            latency = _.trim(latency);
        }
        
        if(_.indexOf(latency_ranges, latency) == -1){
            return fastify.httpErrors.badRequest('Invalid latency range');
        }

        const cli_id_wise_name = {};
        if(_.isUndefined(cli_id_req) || _.isNull(cli_id_req) || _.isEmpty(_.trim(cli_id_req))){
            cli_id.push(req.user.cli_id);
            cli_id_wise_name[req.user.cli_id] = req.user.user;
        }else if(_.isEqual(_.toLower(_.trim(cli_id_req)), 'all')){
            // add login user
            cli_id.push(req.user.cli_id);
            cli_id_wise_name[req.user.cli_id] = req.user.user;
            // add subusers under login user
            const result1 = await fastify.getAllUsersForId(req.user.cli_id);
            for (const row of result1) {
                cli_id.push(row.cli_id);
                cli_id_wise_name[row.cli_id] = row.user;
            }
        }else{
            cli_id.push(Number(cli_id_req));
            const result1 = await fastify.findUserById(cli_id);
            cli_id_wise_name[cli_id] = _.get(result1[0], 'user', '');
        }

        // timezone is not supported for T-1 data
        const { fromdateStrInIST, todateStrInIST, typeofdate } = await fastify.getFromAndToDate(process.env.IST_ZONE_NAME, dateselectiontype, fdate, tdate, true);
        // for es
        const { fromdatetimeStrInIST: todayfts, todatetimeStrInIST: todaytts } = await fastify.getFromAndToDate(tz, 'today', null, null, true);
        req.log.debug(`latencyReportStackedChart ${path} - from and to date => ${fromdateStrInIST}, ${todateStrInIST}, ${todayfts}, ${todaytts}, ${typeofdate}`);
        
        let result = [];
        if (_.eq(typeofdate, 'onlytoday') || _.eq(typeofdate, 'both')) {
            const perCli = await fastify.getTodaysLatencyOnlyDateWise(cli_id, todayfts, todaytts, table); 
          
        
        result.push(perCli);

        }
        if (_.eq(typeofdate, 'both')) {
            // get the date for prev days
            const r1 = await fastify.getLatencyForStackedChart(fromdateStrInIST, todateStrInIST, cli_id, table);
            // todays is already added above
            
            result.push(...r1);
            //console.log(`both /rsummary [${typeofdate}] final summary count => `, result);
        }
        if (_.eq(typeofdate, 'onlyprev')) {
            // get the date for prev days
            const r1 = await fastify.getLatencyForStackedChart(fromdateStrInIST, todateStrInIST, cli_id, table);
            result = r1;
           // console.log(`only prev /rlatency/platform/lattencytable [${typeofdate}] final summary count => `, result);
        }

        let lat_0_5_arr =[], lat_6_10_arr=[], lat_11_15_arr=[], lat_16_30_arr=[], lat_31_45_arr=[], lat_46_60_arr=[], lat_61_120_arr=[], lat_gt_120_arr = [];
        let finalObj = { series: [], label:[] };

        for (const r of result) {            
            const recv_date = moment(r.recv_date).format('DD MMM');          
            const lat_0_5 = Number(_.isNull(r.lat_0_5) ? 0 : r.lat_0_5);
            const lat_6_10 = Number(_.isNull(r.lat_6_10) ? 0 : r.lat_6_10);
            const lat_11_15 = Number(_.isNull(r.lat_11_15) ? 0 : r.lat_11_15);
            const lat_16_30 = Number(_.isNull(r.lat_16_30) ? 0 : r.lat_16_30);
            const lat_31_45 = Number(_.isNull(r.lat_31_45) ? 0 : r.lat_31_45);
            const lat_46_60 = Number(_.isNull(r.lat_46_60) ? 0 : r.lat_46_60);
            const lat_61_120 = Number(_.isNull(r.lat_61_120) ? 0 : r.lat_61_120);
            const lat_gt_120 = Number(_.isNull(r.lat_gt_120) ? 0 : r.lat_gt_120);

            lat_0_5_arr.push(lat_0_5);
            lat_6_10_arr.push(lat_6_10);
            lat_11_15_arr.push(lat_11_15);
            lat_16_30_arr.push(lat_16_30);
            lat_31_45_arr.push(lat_31_45);
            lat_46_60_arr.push(lat_46_60);
            lat_61_120_arr.push(lat_61_120);
            lat_gt_120_arr.push(lat_gt_120);
            finalObj.label.push(recv_date);
        }

        if(_.isEqual(dateselectiontype, 'this year')){
            const monthWiseData = {};
            for (const r of result) {   
                if(_.isEmpty(r)){
                    continue;
                }         
                const recv_month_string = moment(r.recv_date).format('MMM');    
                const recv_month_number = moment(r.recv_date).format('MM');          
                const lat_0_5 = Number(_.isNull(r.lat_0_5) ? 0 : r.lat_0_5);
                const lat_6_10 = Number(_.isNull(r.lat_6_10) ? 0 : r.lat_6_10);
                const lat_11_15 = Number(_.isNull(r.lat_11_15) ? 0 : r.lat_11_15);
                const lat_16_30 = Number(_.isNull(r.lat_16_30) ? 0 : r.lat_16_30);
                const lat_31_45 = Number(_.isNull(r.lat_31_45) ? 0 : r.lat_31_45);
                const lat_46_60 = Number(_.isNull(r.lat_46_60) ? 0 : r.lat_46_60);
                const lat_61_120 = Number(_.isNull(r.lat_61_120) ? 0 : r.lat_61_120);
                const lat_gt_120 = Number(_.isNull(r.lat_gt_120) ? 0 : r.lat_gt_120);

                const data = {recv_month_string, recv_month_number, lat_0_5, lat_6_10, lat_11_15, lat_16_30, lat_31_45, lat_46_60, lat_61_120, lat_gt_120};
                if(_.has(monthWiseData, recv_month_number)){
                    const existingData = monthWiseData[recv_month_number];
                    existingData.lat_0_5 += data.lat_0_5;
                    existingData.lat_6_10 += data.lat_6_10;
                    existingData.lat_11_15 += data.lat_11_15;
                    existingData.lat_16_30 += data.lat_16_30;
                    existingData.lat_31_45 += data.lat_31_45;
                    existingData.lat_46_60 += data.lat_46_60;
                    existingData.lat_61_120 += data.lat_61_120;
                    existingData.lat_gt_120 += data.lat_gt_120;
                    monthWiseData[recv_month_number] = existingData;
                }else{
                    monthWiseData[recv_month_number] = data;
                }
            }
            let monthsNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec' ];
            let monthsNumber = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12'];
            const currentMonth = Number(moment().format('MM'));
            monthsNumber =  _.slice(monthsNumber, 0, currentMonth);
            monthsNames = _.slice(monthsNames, 0, currentMonth);
            lat_0_5_arr =[], lat_6_10_arr=[], lat_11_15_arr=[], lat_16_30_arr=[], lat_31_45_arr=[], lat_46_60_arr=[], lat_61_120_arr=[], lat_gt_120_arr = [];
            finalObj = { series: [], label:[] };
            _.forEach(monthsNumber, (month) =>{
                const data = monthWiseData[month];
                let recv_date = monthsNames[+month - 1], lat_0_5 = 0, lat_6_10= 0, lat_11_15 = 0, lat_16_30 = 0, lat_31_45 = 0, lat_46_60 = 0, lat_61_120 = 0, lat_gt_120 = 0;
                if(!_.isUndefined(data) && !_.isNull(data)){
                    recv_date = data.recv_month_string;
                    lat_0_5 = Number(_.isNull(data.lat_0_5) ? 0 : data.lat_0_5);
                    lat_6_10 = Number(_.isNull(data.lat_6_10) ? 0 : data.lat_6_10);
                    lat_11_15 = Number(_.isNull(data.lat_11_15) ? 0 : data.lat_11_15);
                    lat_16_30 = Number(_.isNull(data.lat_16_30) ? 0 : data.lat_16_30);
                    lat_31_45 = Number(_.isNull(data.lat_31_45) ? 0 : data.lat_31_45);
                    lat_46_60 = Number(_.isNull(data.lat_46_60) ? 0 : data.lat_46_60);
                    lat_61_120 = Number(_.isNull(data.lat_61_120) ? 0 : data.lat_61_120);
                    lat_gt_120 = Number(_.isNull(data.lat_gt_120) ? 0 : data.lat_gt_120);
                }      

                lat_0_5_arr.push(lat_0_5);
                lat_6_10_arr.push(lat_6_10);
                lat_11_15_arr.push(lat_11_15);
                lat_16_30_arr.push(lat_16_30);
                lat_31_45_arr.push(lat_31_45);
                lat_46_60_arr.push(lat_46_60);
                lat_61_120_arr.push(lat_61_120);
                lat_gt_120_arr.push(lat_gt_120);
                finalObj.label.push(recv_date);
            });
        }

        if (_.eq(latency, 'all') || _.eq(latency, 'lat_0_5')){
            finalObj.series.push({
                "name":"0-5 sec",
                "data":lat_0_5_arr,
                "color":"#5E548E"
            });
        }

        if (_.eq(latency, 'all') || _.eq(latency, 'lat_6_10')){
            finalObj.series.push({
                "name":"6-10 sec",
                "data":lat_6_10_arr,
                "color":"#B8BEDD"
            });
        }

        if (_.eq(latency, 'all') || _.eq(latency, 'lat_11_15')){
            finalObj.series.push({
                "name":"11-15 sec",
                "data":lat_11_15_arr,
                "color":"#B5F0EE"
            });
        }

        if (_.eq(latency, 'all') || _.eq(latency, 'lat_16_30')){
            finalObj.series.push({
                "name":"16-30 sec",
                "data":lat_16_30_arr,
                "color":"#EFC3E6"
            });
        }
        
        if (_.eq(latency, 'all') || _.eq(latency, 'lat_31_45')){
            finalObj.series.push({
                "name":"31-45 sec",
                "data":lat_31_45_arr,
                "color":"#F0A6CA"
            });
        }
        
        if (_.eq(latency, 'all') || _.eq(latency, 'lat_46_60')){
            finalObj.series.push({
                "name":"46-60 sec",
                "data":lat_46_60_arr,
                "color":"#BE95C4"
            });
        }
        
        if (_.eq(latency, 'all') || _.eq(latency, 'lat_61_120')){
            finalObj.series.push({
                "name":"61-120 sec",
                "data":lat_61_120_arr,
                "color":"#9F86C0"
            });
        }

        if (_.eq(latency, 'all') || _.eq(latency, 'lat_gt_120')){
            finalObj.series.push({
                "name":"> 120 sec",
                "data":lat_gt_120_arr,
                "color":"#76427A"
            });
        }      
        
        req.log.debug(`latencyReportStackedChart ${path} - response - ${JSON.stringify(finalObj)}`);
        return finalObj;
    }

    fastify.post('/platform/latencystackchart', { preValidation: [], schema: rLatencyStackChartSchema }, async (req, reply) => {
        try {
            const table = 'summary.ui_platform_latency_report';
            return await latencyReportStackedChart(req, '/platform/latencystackchart', table);
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not get platform latency data. Please try again', { err });
            return e;
        }
    });

    fastify.post('/telco/latencystackchart', { preValidation: [], schema: rLatencyStackChartSchema }, async (req, reply) => {
        try {
            const table = 'summary.ui_telco_latency_report';
            return await latencyReportStackedChart(req, '/telco/latencystackchart', table);
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not get telco latency data. Please try again', { err });
            return e;
        }
    });

}

module.exports = rlatency;
