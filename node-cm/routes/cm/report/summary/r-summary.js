/* eslint-disable no-restricted-syntax */
const _ = require('lodash');
const mtz = require('moment-timezone');
const moment = require('moment');

const { rsummarySchema, rsummarydownloadSchema } = require('../../../../schema/report-summary-schema');
const { reject } = require('lodash');

/**
 * setup some routes
 * @param {import("fastify").FastifyInstance} fastify
 * @param {*} opts
 */
async function rsummary(fastify, opts) {
    fastify.addHook('preValidation', async (req, reply) => {
        console.log('preValidation hook called');
        return req.jwtVerify();
    });

    async function processTodaysCount(momentInUserTZ, rToday, rTodayMTSuccess, rTodayMTRejected, rTodayDNSuccess, rTodayDNFailed, reportfor) {
        const result = [];
        const tBucket = _.get(rToday, 'aggregations.genric_and_cli_id.buckets', []);
        const sBucket = _.get(rTodayMTSuccess, 'aggregations.genric_and_cli_id.buckets', []);
        const rBucket = _.get(rTodayMTRejected, 'aggregations.genric_and_cli_id.buckets', []);
        const dsBucket = _.get(rTodayDNSuccess, 'aggregations.genric_and_cli_id.buckets', []);
        const dfBucket = _.get(rTodayDNFailed, 'aggregations.genric_and_cli_id.buckets', []);

        /* const tBucket = _.get(rToday, 'aggregations.groupby.buckets', []);
        const sBucket = _.get(rTodayMTSuccess, 'aggregations.groupby.buckets', []);
        const rBucket = _.get(rTodayMTRejected, 'aggregations.groupby.buckets', []);
        const dsBucket = _.get(rTodayDNSuccess, 'aggregations.groupby.buckets', []);
        const dfBucket = _.get(rTodayDNFailed, 'aggregations.groupby.buckets', []);
 */
        const sGroupby = _.groupBy(sBucket, 'key_as_string');
        const rGroupby = _.groupBy(rBucket, 'key_as_string');
        const dsGroupby = _.groupBy(dsBucket, 'key_as_string');
        const dfGroupby = _.groupBy(dfBucket, 'key_as_string');

        for (const obj of tBucket) {
            let tmts = 0;
            let tmtr = 0;
            let tdns = 0;
            let tdnf = 0;

            const key = _.get(obj, 'key_as_string');

            // get the mt success counts for this campaign
            let o = _.get(sGroupby, key, []);
            if (o.length > 0) tmts = Number(_.get(o[0], 'doc_count', 0));

            // get the rejected counts for this campaign
            o = _.get(rGroupby, key, []);
            if (o.length > 0) tmtr = Number(_.get(o[0], 'doc_count', 0));

            // get the dn success counts for this campaign
            o = _.get(dsGroupby, key, {});
            if (o.length > 0) tdns = Number(_.get(o[0], 'doc_count', 0));

            // get the dn failed counts for this campaign
            o = _.get(dfGroupby, key, {});
            if (o.length > 0) tdnf = Number(_.get(o[0], 'doc_count', 0));

            const ttotal = tmts + tmtr;
            const tdnpending = tmts - (tdns + tdnf);

            const mtsuccess = tmts;
            const dnsuccess = tdns;
            const dnfailed = tdnf;
            const mtrejected = tmtr;
            const dnpending = tdnpending;
            const total = ttotal;
            const obj1 = {
                recv_date: momentInUserTZ.format('YYYY-MM-DD'),
                totalrecieved: total,
                mtsuccess,
                mtrejected,
                dnsuccess,
                dnfailed,
                dnpending,
            };
            const cliAndVal = key.split('|')
            
            _.set(obj1, reportfor, _.eq(reportfor, 'source') ? _.toUpper(cliAndVal[0]) : cliAndVal[0]);
            _.set(obj1, "cli_id", cliAndVal[1])
            result.push(obj1);
        }
        return result;
    }
 
    async function processSummary(req) {
        const { dateselectiontype, fdate, tdate, source, campaign_id, senderid, reportby } = req.body;
        const { tz } = req.user;
        let cli_id = [];
        const cli_id_req = req.body.cli_id;
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
        let total = 0;
        let mtsuccess = 0;
        let dnsuccess = 0;
        let dnfailed = 0;
        let mtrejected = 0;
        let dnpending = 0;

        //console.log('/rsummary incoming params => ', [dateselectiontype, fdate, tdate, source, campaign_id, senderid, reportby]);

        // TODO: validate custom range date. shud not be less than the cur date

        // current date in users tz
        const momentInUserTZ = mtz().tz(tz);
       // console.log('/rsummary current date in users tz ', momentInUserTZ.format('YYYY-MM-DD'), tz);

        // timezone is not supported for T-1 data
        const { fromdateStrInIST, todateStrInIST, typeofdate } = await fastify.getFromAndToDate(process.env.IST_ZONE_NAME, dateselectiontype, fdate, tdate, true);
        // for es
        const { fromdatetimeStrInIST: todayfts, todatetimeStrInIST: todaytts } = await fastify.getFromAndToDate(tz, 'today', null, null, true);
        console.log('/rsummary from and to date => ', fromdateStrInIST, todateStrInIST, todayfts, todaytts, typeofdate);

        // get the intf_type for the source
        const result1 = await fastify.getInterfaceTypes(source);
        let interfaceArr = [];

        if (result1.length === 0) {
            console.log('/rsummary *** Could not find mapping in interface_type_mapping table for source. setting source as all  => ', source);
        }

        interfaceArr = _.compact(_.map(result1, 'intf_type'));
        
        // TODO: implementation based on typeofdate is pending
        let result = [];
        if (_.eq(reportby, 'date')) {
            if (_.eq(typeofdate, 'onlytoday') || _.eq(typeofdate, 'both')) {
                
                const [rTodayMTSuccess, rTodayMTRejected, rTodayDNSuccess, rTodayDNFailed] = await Promise.all([fastify.getTodaysCountOverall(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'mtsuccess'), fastify.getTodaysCountOverall(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'mtrejected'), fastify.getTodaysCountOverall(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'dnsuccess'), fastify.getTodaysCountOverall(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'dnfailed')]);
        
                const successObj = _.get(rTodayMTSuccess, 'aggregations.byCli.buckets', 0);
                const rejObj = _.get(rTodayMTRejected, 'aggregations.byCli.buckets', 0);
                const dnSuccessObj = _.get(rTodayDNSuccess, 'aggregations.byCli.buckets', 0);
                const dnfailedObj = _.get(rTodayDNFailed, 'aggregations.byCli.buckets', 0);
               // console.log("bucketes....",rejObj)
                //let mtsuccess = 0, mtrejected = 0;
                _.forEach(cli_id, (o)=>{
		    mtsuccess = 0, mtrejected = 0, dnsuccess = 0, dnfailed = 0;
                    if(_.findIndex(successObj ,(x) => x.key == o) != -1){
                         mtsuccess = Number(_.find(successObj , (x) => x.key == o).doc_count, 0);
                    
                    }
                    
                    if(_.findIndex(rejObj ,(x) => x.key == o) != -1){
                         mtrejected = Number(_.find(rejObj, (x) => x.key == o).doc_count, 0);
                      
                    }

                    const total = mtsuccess + mtrejected;
                    if(+total >0){
                     
                        if(_.findIndex(dnSuccessObj ,(x) => x.key == o) != -1){
                             dnsuccess = Number(_.find(dnSuccessObj,(x) => x.key == o).doc_count, 0);
                            }
        
                            if(_.findIndex(dnfailedObj ,(x) => x.key == o) != -1){
                             dnfailed = Number(_.find(dnfailedObj, (x) => x.key == o).doc_count, 0);
                            }
                            
                            const dnpending = mtsuccess - (dnsuccess + dnfailed);
                            result.push({
                                totalrecieved: +total,
                                mtsuccess,
                                mtrejected,
                                dnsuccess,
                                dnfailed,
                                dnpending,
                                cli_id: o
                            })
                    }
                   
                    
                })
                                
             }
            if (_.eq(typeofdate, 'both')) {

                // get the date for prev days
                const r1 = await fastify.getRSDatewise(fromdateStrInIST, todateStrInIST, interfaceArr, campaign_id, senderid, cli_id);
                // todays is already added above
                result.push(...r1);
                //console.log(`both /rsummary [${typeofdate}] final summary count => `, result);
            }
            if (_.eq(typeofdate, 'onlyprev')) {
                // get the date for prev days
                const r1 = await fastify.getRSDatewise(fromdateStrInIST, todateStrInIST, interfaceArr, campaign_id, senderid, cli_id);
                result = r1;
                console.log(`only prev /rsummary [${typeofdate}] final summary count => `, result);
            }
        } else if (_.eq(reportby, 'campaign')) {
            // get the interface types for this interface group (UI)
            const rtypes = await fastify.getAllInterfaceTypesForGroup('UI');
            interfaceArr = _.compact(_.map(rtypes, 'intf_type'));

            if (_.eq(typeofdate, 'onlytoday') || _.eq(typeofdate, 'both')) {
                const [rToday, rTodayMTSuccess, rTodayMTRejected, rTodayDNSuccess, rTodayDNFailed] = await Promise.all([fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'total', 'campaign'), fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'mtsuccess', 'campaign'), fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'mtrejected', 'campaign'), fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'dnsuccess', 'campaign'), fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'dnfailed', 'campaign')]);
                result = await processTodaysCount(momentInUserTZ, rToday, rTodayMTSuccess, rTodayMTRejected, rTodayDNSuccess, rTodayDNFailed, reportby);
              //  console.log(`/rsummary [${typeofdate}] Todays count => `, result);
                
            }
            if (_.eq(typeofdate, 'both')) {
                // get the date for prev days
                
                const r1 = await fastify.getRSCampaignwise(fromdateStrInIST, todateStrInIST, interfaceArr, campaign_id, senderid, cli_id);
                // todays is already added above
                
                
                result.push(...r1);
                //console.log(`/rsummary [${typeofdate}] final summary count => `, result);
            }
            if (_.eq(typeofdate, 'onlyprev')) {
                // get the date for prev days
                const r1 = await fastify.getRSCampaignwise(fromdateStrInIST, todateStrInIST, interfaceArr, campaign_id, senderid, cli_id);
               
                result = r1;
                //console.log(`/rsummary [${typeofdate}] final summary count => `, result);
            }
        } else if (_.eq(reportby, 'source')) {
            if (_.eq(typeofdate, 'onlytoday') || _.eq(typeofdate, 'both')) {
                const [rToday, rTodayMTSuccess, rTodayMTRejected, rTodayDNSuccess, rTodayDNFailed] = await Promise.all([fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'total', 'source'), fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'mtsuccess', 'source'), fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'mtrejected', 'source'), fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'dnsuccess', 'source'), fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'dnfailed', 'source')]);
               result = await processTodaysCount(momentInUserTZ, rToday, rTodayMTSuccess, rTodayMTRejected, rTodayDNSuccess, rTodayDNFailed, reportby);

                console.log(`/rsummary [${typeofdate}] Todays count..  source=> `, result);
            }
            if (_.eq(typeofdate, 'both')) {
                // get the date for prev days
                const r1 = await fastify.getRSSourcewise(fromdateStrInIST, todateStrInIST, interfaceArr, campaign_id, senderid, cli_id);
                // todays is already added above
                
                result.push(...r1);
                console.log(`/rsummary [${typeofdate}] final summary count => `, result);
            }
            if (_.eq(typeofdate, 'onlyprev')) {
                // get the date for prev days
                const r1 = await fastify.getRSSourcewise(fromdateStrInIST, todateStrInIST, interfaceArr, campaign_id, senderid, cli_id);
                result = r1;
            }
        } else if (_.eq(reportby, 'senderid')) {
            if (_.eq(typeofdate, 'onlytoday') || _.eq(typeofdate, 'both')) {
               const [rToday, rTodayMTSuccess, rTodayMTRejected, rTodayDNSuccess, rTodayDNFailed] = await Promise.all([fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'total', 'senderid'), fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'mtsuccess', 'senderid'), fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'mtrejected', 'senderid'), fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'dnsuccess', 'senderid'), fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'dnfailed', 'senderid')]);
              
               result = await processTodaysCount(momentInUserTZ, rToday, rTodayMTSuccess, rTodayMTRejected, rTodayDNSuccess, rTodayDNFailed, reportby);
                console.log(`/rsummary [${typeofdate}] senderid Todays count => `, result);
            }
            if (_.eq(typeofdate, 'both')) {
                // get the date for prev days
                const r1 = await fastify.getRSSenderidwise(fromdateStrInIST, todateStrInIST, interfaceArr, campaign_id, senderid, cli_id);
                // todays is already added above
                result.push(...r1);
                console.log(`/rsummary [${typeofdate}] final summary count => `, result);
            }
            if (_.eq(typeofdate, 'onlyprev')) {
                // get the date for prev days
                const r1 = await fastify.getRSSenderidwise(fromdateStrInIST, todateStrInIST, interfaceArr, campaign_id, senderid, cli_id);
                //console.log('>>> resp from db', r1);
                result = r1;
            }
        } else {
            if (_.eq(typeofdate, 'onlytoday') || _.eq(typeofdate, 'both')) {
                
                const [rTodayMTSuccess, rTodayMTRejected, rTodayDNSuccess, rTodayDNFailed] = await Promise.all([fastify.getTodaysCountOverall(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'mtsuccess'), fastify.getTodaysCountOverall(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'mtrejected'), fastify.getTodaysCountOverall(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'dnsuccess'), fastify.getTodaysCountOverall(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'dnfailed')]);
                const successObj = _.get(rTodayMTSuccess, 'aggregations.byCli.buckets', 0);
                const rejObj = _.get(rTodayMTRejected, 'aggregations.byCli.buckets', 0);
                const dnSuccessObj = _.get(rTodayDNSuccess, 'aggregations.byCli.buckets', 0);
                const dnfailedObj = _.get(rTodayDNFailed, 'aggregations.byCli.buckets', 0);
                //console.log("1st If only today or both",successObj,rejObj,dnSuccessObj,dnfailedObj)
               
                
                _.forEach(cli_id, (o)=>{
                   // let mtsuccess = 0, mtrejected = 0;
		    mtsuccess = 0, mtrejected = 0, dnsuccess = 0, dnfailed = 0;
                    if(_.findIndex(successObj ,(x) => x.key == o) != -1){
                         mtsuccess = Number(_.find(successObj , (x) => x.key == o).doc_count, 0);
                    
                    }
                    
                    if(_.findIndex(rejObj ,(x) => x.key == o) != -1){
                         mtrejected = Number(_.find(rejObj, (x) => x.key == o).doc_count, 0);
                    }

                    const total = mtsuccess + mtrejected;
                    if(+total >0){
                        
                        if(_.findIndex(dnSuccessObj ,(x) => x.key == o) != -1){
                             dnsuccess = Number(_.filter(dnSuccessObj,(x) => x.key == o).doc_count, 0);
                            }
        
                            if(_.findIndex(dnfailedObj ,(x) => x.key == o) != -1){
                             dnfailed = Number(_.filter(dnfailedObj, (x) => x.key == o).doc_count, 0);
                            }
                            
                            const dnpending = mtsuccess - (dnsuccess + dnfailed);
                            result.push({
                                totalrecieved: +total,
                                mtsuccess,
                                mtrejected,
                                dnsuccess,
                                dnfailed,
                                dnpending,
                                cli_id: o
                            })
                    }
                   
                    
                })
            }
            if (_.eq(typeofdate, 'both')) {
                // get the date for prev days
                
                
                const r1 = await fastify.getRSOverall(fromdateStrInIST, todateStrInIST, interfaceArr, campaign_id, senderid, cli_id);
                // todays is already added above
               _.forEach(r1, (record =>{
                    
                    result.push( {
                        totalrecieved: +record.totalrecieved,
                        mtsuccess: +record.mtsuccess,
                        mtrejected: +record.mtrejected,
                        dnsuccess: +record.dnsuccess,
                        dnfailed: +record.dnfailed,
                        dnpending: +record.dnpending,
                        cli_id:record.cli_id
                        
                    })

                }))
               //  const grouped= _.groupBy(result,"cli_id")
              
                const ans = _(result)
                            .groupBy('cli_id')
                            .map((platform, id) => ({
                                cli_id: id,
                                totalrecieved: _.sumBy(platform, 'totalrecieved'),
                                mtsuccess: _.sumBy(platform, 'mtsuccess'),
                                mtrejected: _.sumBy(platform, 'mtrejected'),
                                dnsuccess: _.sumBy(platform, 'dnsuccess'),
                                dnfailed: _.sumBy(platform, 'dnfailed'),
                                dnpending: _.sumBy(platform, 'dnpending'),

                            }))
                            .value()
                result = ans;
                
               
            //  console.log(`/rsummary 2nd if [${typeofdate}] final summary count => `, result);
            }
            if (_.eq(typeofdate, 'onlyprev')) {
                console.log("3rd if only prev")
                // get the date for prev days
                const r1 = await fastify.getRSOverall(fromdateStrInIST, todateStrInIST, interfaceArr, campaign_id, senderid, cli_id);
                result = r1;
            }
        }

       
       
        for (const r of result) {
            // recv_date will not be present for Overall count.
            
            const recv_date = moment(r.recv_date).format('DD-MMM-YYYY');
            
            total = Number(_.isNull(r.totalrecieved) ? 0 : r.totalrecieved);

            // ignore if total is zero
            if (total === 0) continue;

            mtsuccess = Number(_.isNull(r.mtsuccess) ? 0 : r.mtsuccess);
            dnsuccess = Number(_.isNull(r.dnsuccess) ? 0 : r.dnsuccess);
            dnfailed = Number(_.isNull(r.dnfailed) ? 0 : r.dnfailed) + Number(_.isNull(_.get(r, 'expired', 0)) ? 0 : _.get(r, 'expired', 0));
            mtrejected = Number(_.isNull(r.mtrejected) ? 0 : r.mtrejected);
            dnpending = Number(_.isNull(r.dnpending) ? 0 : r.dnpending);
            dnpending = (dnpending < 0) ? 0 : dnpending;

            let dnsuccesspercentage = _.round((dnsuccess / mtsuccess) * 100, 2);
            let mtsuccesspercentage = _.round((mtsuccess / total) * 100, 2);
            let dnfailedpercentage = _.round((dnfailed / mtsuccess) * 100, 2);
            let mtrejectedpercentage = _.round((mtrejected / total) * 100, 2);
            let dnpendingpercentage = _.round((dnpending / mtsuccess) * 100, 2);
            dnfailedpercentage = _.isFinite(dnfailedpercentage) ? dnfailedpercentage : 0;
            mtsuccesspercentage = _.isFinite(mtsuccesspercentage) ? mtsuccesspercentage : 0;
            dnsuccesspercentage = _.isFinite(dnsuccesspercentage) ? dnsuccesspercentage : 0;
            mtrejectedpercentage = _.isFinite(mtrejectedpercentage) ? mtrejectedpercentage : 0;
            dnpendingpercentage = _.isFinite(dnpendingpercentage) ? dnpendingpercentage : 0;

            dnsuccesspercentage = _.isNaN(dnsuccesspercentage) ? '0' : dnsuccesspercentage;
            mtsuccesspercentage = _.isNaN(mtsuccesspercentage) ? '0' : mtsuccesspercentage;
            dnfailedpercentage = _.isNaN(dnfailedpercentage) ? '0' : dnfailedpercentage;
            mtrejectedpercentage = _.isNaN(mtrejectedpercentage) ? '0' : mtrejectedpercentage;
            dnpendingpercentage = _.isNaN(dnpendingpercentage) ? '0' : dnpendingpercentage;

            const obj = {
                total,
                total_human: fastify.coolFormat(total, 0),
                mtsuccess,
                mtsuccess_human: fastify.coolFormat(mtsuccess, 0),
                dnsuccess,
                dnsuccess_human: fastify.coolFormat(dnsuccess, 0),
                dnfailed,
                dnfailed_human: fastify.coolFormat(dnfailed, 0),
                mtrejected,
                mtrejected_human: fastify.coolFormat(mtrejected, 0),
                dnpending,
                dnpending_human: fastify.coolFormat(dnpending, 0),
                dnsuccesspercentage,
                mtsuccesspercentage,
                dnfailedpercentage,
                mtrejectedpercentage,
                dnpendingpercentage,
                username: _.get(cli_id_wise_name, r.cli_id, '')
                
            };

            // set the date
            if (!_.eq(reportby, 'overall')) {
                _.set(obj, 'recv_date', recv_date);
            }
            // set campaign
            if (_.eq(reportby, 'campaign')) {
                _.set(obj, 'campaign_name', r.campaign);
                // _.set(obj, 'campaign_id', r.campaign_id);
            }
            // set source
            if (_.eq(reportby, 'source')) {
                _.set(obj, 'source', r.source);
            }
            // set senderid
            if (_.eq(reportby, 'senderid')) {
                _.set(obj, 'senderid', r.senderid);
            }

                

            // TODO: Sorting
            payload.push(obj);
        }

        //console.log('payload => ', payload);
        return payload;
    }

    fastify.post('/rsummary', { preValidation: [], schema: rsummarySchema }, async (req, reply) => {
        try {
            return await processSummary(req);
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not get summary data. Please try again', { err });

            return e;
        }
    });

    fastify.post('/rsummarydownload', { preValidation: [], schema: rsummarydownloadSchema }, async (req, reply) => {
        try {
            const { reportby, dateselectiontype } = req.body;
            let header = '';

            // construct file headers
            if (_.eq(reportby, 'date')) header = 'Date,Username,Total,Submitted to Telco,Rejected,Delivered,Failed,Pending,Submitted %,Rejected %,Delivered %,Failed %,Pending %';
            if (_.eq(reportby, 'campaign')) header = 'Date,Username,Campaign,Total,Submitted to Telco,Rejected,Delivered,Failed,Pending,Submitted %,Rejected %,Delivered %,Failed %,Pending %';
            if (_.eq(reportby, 'source')) header = 'Date,Username,Source,Total,Submitted to Telco,Rejected,Delivered,Failed,Pending,Submitted %,Rejected %,Delivered %,Failed %,Pending %';
            if (_.eq(reportby, 'senderid')) header = 'Date,Username,SenderId,Total,Submitted to Telco,Rejected,Delivered,Failed,Pending,Submitted %,Rejected %,Delivered %,Failed %,Pending %';
            if (_.eq(reportby, 'overall')) header = 'Username,Total,Submitted to Telco,Rejected,Delivered,Failed,Pending,Submitted %,Rejected %,Delivered %,Failed %,Pending %';

            const payload = await processSummary(req);

            reply.type('text/csv');
            reply.header('Content-Disposition', `attachment;filename=${reportby}-${dateselectiontype}.csv`); // prevent ie from opening in app and shows save as dialog instead
            const arr = [];

            arr.push(header);
            for (const o of payload) {
                
                if (_.eq(reportby, 'date')) arr.push(`${o.recv_date},${o.username},${o.total},${o.mtsuccess},${o.mtrejected},${o.dnsuccess},${o.dnfailed},${o.dnpending},${o.mtsuccesspercentage},${o.mtrejectedpercentage},${o.dnsuccesspercentage},${o.dnfailedpercentage},${o.dnpendingpercentage}`);
                if (_.eq(reportby, 'campaign')) arr.push(`${o.recv_date},${o.username},${(o.campaign_name) ? o.campaign_name : ''},${o.total},${o.mtsuccess},${o.mtrejected},${o.dnsuccess},${o.dnfailed},${o.dnpending},${o.mtsuccesspercentage},${o.mtrejectedpercentage},${o.dnsuccesspercentage},${o.dnfailedpercentage},${o.dnpendingpercentage}`);
                if (_.eq(reportby, 'source')) arr.push(`${o.recv_date},${o.username},${o.source},${o.total},${o.mtsuccess},${o.mtrejected},${o.dnsuccess},${o.dnfailed},${o.dnpending},${o.mtsuccesspercentage},${o.mtrejectedpercentage},${o.dnsuccesspercentage},${o.dnfailedpercentage},${o.dnpendingpercentage}`);
                if (_.eq(reportby, 'senderid')) arr.push(`${o.recv_date},${o.username},${o.senderid},${o.total},${o.mtsuccess},${o.mtrejected},${o.dnsuccess},${o.dnfailed},${o.dnpending},${o.mtsuccesspercentage},${o.mtrejectedpercentage},${o.dnsuccesspercentage},${o.dnfailedpercentage},${o.dnpendingpercentage}`);
                if (_.eq(reportby, 'overall')) arr.push(`${o.username},${o.total},${o.mtsuccess},${o.mtrejected},${o.dnsuccess},${o.dnfailed},${o.dnpending},${o.mtsuccesspercentage},${o.mtrejectedpercentage},${o.dnsuccesspercentage},${o.dnfailedpercentage},${o.dnpendingpercentage}`);
            }
            reply.send(_.join(arr, '\n'));
        } catch
        (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not get data for source filter. Please try again', [req.id]);

            return e;
        }
    });
}

module.exports = rsummary;
