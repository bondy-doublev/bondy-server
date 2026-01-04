import { NestFactory } from '@nestjs/core';
import * as dotenv from 'dotenv';
import { AppModule } from './app.module';
import { eurekaClient } from './configs/eureka';
import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';
import { ValidationPipe } from '@nestjs/common';
import express from 'express';
import * as process from 'node:process';

dotenv.config();

async function bootstrap() {
  const SERVER_PORT = Number(process.env.SERVER_PORT) || 8080;
  const ACTUATOR_PORT = Number(process.env.ACTUATOR_PORT) || 9086;
  const HOST = process.env.HOST || '0.0.0.0';

  // ==== NestJS App ====
  const app = await NestFactory.create(AppModule);

  // Body parser
  app.use(express.json());

  // Validation pipe
  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,
      forbidNonWhitelisted: false,
      transform: true,
      transformOptions: { enableImplicitConversion: true },
    }),
  );

  // CORS
  const corsOrigins = (process.env.CORS_ORIGINS || '')
    .split(',')
    .map((o) => o.trim())
    .filter(Boolean);

  app.enableCors({
    origin: (origin, callback) => {
      if (!origin) return callback(null, true); // curl, healthcheck
      if (corsOrigins.includes(origin)) return callback(null, true);
      return callback(new Error(`CORS blocked for origin: ${origin}`), false);
    },
    credentials: process.env.CORS_CREDENTIALS === 'true',
  });

  // ==== Swagger setup ====
  const config = new DocumentBuilder()
    .setTitle('Communication Service API')
    .setDescription('REST API for Bondy')
    .setVersion('v1')
    .addBearerAuth(
      { type: 'http', scheme: 'bearer', bearerFormat: 'JWT', in: 'header' },
      'Bearer',
    )
    .addApiKey({ type: 'apiKey', name: 'X-API-KEY', in: 'header' }, 'API Key')
    .addServer('/api/v1', 'Via Gateway')
    .addServer('/', 'Direct')
    .build();

  const document = SwaggerModule.createDocument(app, config);

  // Swagger UI
  SwaggerModule.setup('docs/communication', app, document, {
    swaggerOptions: {
      persistAuthorization: true,
      docExpansion: 'none',
    },
  });

  // JSON docs (Gateway hoặc client có thể lấy)
  app.use('/v3/api-docs', (_req, res) => {
    res.json(document);
  });

  // ==== Start NestJS App ====
  await app.listen(SERVER_PORT, HOST);
  console.log(`🚀 Communication Service running on port ${SERVER_PORT}`);

  // ==== Actuator (Express) ====
  const actuatorApp = express();
  actuatorApp.get('/actuator/health', (_req, res) => {
    res.json({ status: 'UP' });
  });

  actuatorApp.listen(ACTUATOR_PORT, HOST, () => {
    console.log(`Actuator running on port ${ACTUATOR_PORT}`);
  });

  // ==== Register Eureka ====
  eurekaClient.start((error) => {
    if (error) console.error('❌ Eureka registration failed:', error);
    else console.log('✅ Registered to Eureka');
  });
}

bootstrap();
