"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const core_1 = require("@nestjs/core");
const dotenv = __importStar(require("dotenv"));
const app_module_1 = require("./app.module");
const eureka_1 = require("./configs/eureka");
const swagger_1 = require("@nestjs/swagger");
const express_1 = __importDefault(require("express"));
const common_1 = require("@nestjs/common");
const process = __importStar(require("node:process"));
dotenv.config();
async function bootstrap() {
    const app = await core_1.NestFactory.create(app_module_1.AppModule);
    app.use(express_1.default.json());
    app.useGlobalPipes(new common_1.ValidationPipe({
        whitelist: true,
        forbidNonWhitelisted: false,
        transform: true,
        transformOptions: { enableImplicitConversion: true },
    }));
    app.enableCors({
        origin: 'http://localhost:3000',
        credentials: true,
    });
    const port = Number(process.env.SERVER_PORT);
    const config = new swagger_1.DocumentBuilder()
        .setTitle('Communication Service API')
        .setDescription('REST API for Bondy')
        .setVersion('v1')
        .addBearerAuth({ type: 'http', scheme: 'bearer', bearerFormat: 'JWT', in: 'header' }, 'Bearer')
        .addApiKey({ type: 'apiKey', name: 'X-API-KEY', in: 'header' }, 'API Key')
        .addServer('/api/v1', 'Via Gateway')
        .addServer('/', 'Direct')
        .build();
    const document = swagger_1.SwaggerModule.createDocument(app, config);
    swagger_1.SwaggerModule.setup('docs/communication', app, document, {
        swaggerOptions: {
            persistAuthorization: true,
            docExpansion: 'none',
        },
    });
    app.use('/v3/api-docs', express_1.default.json(), (req, res) => {
        res.json(document);
    });
    await app.listen(port);
    console.log(`🚀 Communication Service running on port ${port}`);
    const actuatorApp = (0, express_1.default)();
    actuatorApp.get('/actuator/health', (_req, res) => {
        res.json({ status: 'UP' });
    });
    actuatorApp.listen(Number(process.env.ACTUATOR_PORT), process.env.HOST || 'localhost', () => {
        console.log(`Actuator running on port ${process.env.ACTUATOR_PORT}`);
    });
    eureka_1.eurekaClient.start((error) => {
        if (error)
            console.error('❌ Eureka registration failed:', error);
        else
            console.log('✅ Registered to Eureka');
    });
}
bootstrap();
//# sourceMappingURL=main.js.map