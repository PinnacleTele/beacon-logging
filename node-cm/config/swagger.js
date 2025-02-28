exports.options = {
    routePrefix: '/doc',
    exposeRoute: true,
    swagger: {
        info: {
            title: 'Campaign Manager API',
            description:
                'Backend API\'s for campaign manager ui',
            version: '1.0.0',
        },
        externalDocs: {
            url: 'https://swagger.io',
            description: 'Find more info here',
        },
        host: '127.0.0.1:3000',
        schemes: ['http'],
        consumes: ['application/json'],
        produces: ['application/json'],
        securityDefinitions: {

            api_key: {
                type: 'apiKey',
                in: 'header',
                name: 'x-api-key',
            },

        },
        tags: [
            { name: 'Auth', description: 'API related to authentication and so' },
            { name: 'Utils', description: 'List of generic APIs used across the app' },
            { name: 'Quick SMS', description: 'API for Quick SMS Campaign' },
            { name: 'Campaign', description: 'API for Campaign List & Details' },
            { name: 'Campaign OTM', description: 'API for One To Many Campaign' },
            { name: 'Campaign MTM', description: 'API for Many To Many Campaign' },
            { name: 'Campaign Group', description: 'API for Group Campaign' },
            { name: 'Campaign Template', description: 'API for Template Campaign' },
            { name: 'Template', description: 'API for Template related functionality' },
            { name: 'Group', description: 'API for Group related functionality' },
            { name: 'Contact', description: 'API for Contacts related functionality' },
            { name: 'Account', description: 'API for Account related functionality' },
            { name: 'Report', description: 'API for common report related functionality' },
            { name: 'Report Summary', description: 'API for summary report related functionality' },
            { name: 'DLT', description: 'API for DLT related functionality' },
            { name: 'Download', description: 'API for download related functionality' },
        ],
    },
};
