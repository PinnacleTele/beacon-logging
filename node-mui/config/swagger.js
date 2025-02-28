exports.options = {
    routePrefix: '/doc',
    exposeRoute: true,
    swagger: {
        info: {
            title: 'API - MUI',
            description:
                'API\'s for management ui',
            version: '1.0.0',
        },
        externalDocs: {
            url: 'https://swagger.io',
            description: 'Find more info here',
        },
        host: '127.0.0.1:3003',
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
        ],
    },
};
