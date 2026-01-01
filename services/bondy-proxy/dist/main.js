"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const core_1 = require("@nestjs/core");
const app_module_1 = require("./app.module");
async function bootstrap() {
    const app = await core_1.NestFactory.create(app_module_1.AppModule);
    const allowedOrigins = Object.keys(process.env)
        .filter(key => /^API\d+_CLIENT_URL$/.test(key))
        .map(key => process.env[key])
        .filter(Boolean);
    app.enableCors({
        origin: (origin, callback) => {
            if (!origin || allowedOrigins.includes(origin))
                callback(null, true);
            else
                callback(new Error('Not allowed by CORS'));
        },
        methods: 'GET,HEAD,PUT,PATCH,POST,DELETE,OPTIONS',
        allowedHeaders: ['Content-Type', 'Authorization', 'x-api-key'],
        credentials: true,
    });
    app.getHttpAdapter().get('/health', (req, res) => {
        res.json({ status: 'UP', name: 'proxy-server' });
    });
    await app.listen(process.env.PORT || 3333, '0.0.0.0');
    console.log(`Proxy server running on :${process.env.PORT || 3333}`);
}
bootstrap();
//# sourceMappingURL=main.js.map