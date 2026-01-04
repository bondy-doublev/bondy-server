import { NestFactory } from '@nestjs/core';
import * as dotenv from 'dotenv';
import { AppModule } from './app.module';
import { eurekaClient } from './configs/eureka';
import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';
import express from 'express';
import { ValidationPipe } from '@nestjs/common';
import * as process from 'node:process';

dotenv.config();

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  // Body parser
  app.use(express.json());

  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,
      forbidNonWhitelisted: false,
      transform: true,
      transformOptions: { enableImplicitConversion: true },
    }),
  );

  const corsOrigins = (process.env.CORS_ORIGINS || '')
    .split(',')
    .map((o) => o.trim())
    .filter(Boolean);

  app.enableCors({
    origin: (origin, callback) => {
      // Cho phép request không có origin (curl, server-to-server, healthcheck)
      if (!origin) return callback(null, true);

      if (corsOrigins.includes(origin)) {
        return callback(null, true);
      }

      return callback(new Error(`CORS blocked for origin: ${origin}`), false);
    },
    credentials: process.env.CORS_CREDENTIALS === 'true',
  });

  const port = Number(process.env.SERVER_PORT);

  // ===== Swagger setup =====
  const config = new DocumentBuilder()
    .setTitle('Communication Service API')
    .setDescription('REST API for Bondy')
    .setVersion('v1')
    .addBearerAuth(
      { type: 'http', scheme: 'bearer', bearerFormat: 'JWT', in: 'header' },
      'Bearer', // ← định danh của auth
    )
    .addApiKey({ type: 'apiKey', name: 'X-API-KEY', in: 'header' }, 'API Key')
    .addServer('/api/v1', 'Via Gateway')
    .addServer('/', 'Direct')
    .build();

  const document = SwaggerModule.createDocument(app, config);

  // ✅ Cấu hình Swagger UI
  SwaggerModule.setup('docs/communication', app, document, {
    swaggerOptions: {
      persistAuthorization: true, // nhớ token sau khi reload
      docExpansion: 'none', // thu gọn các endpoint
    },
  });

  // ✅ JSON docs (Gateway lấy để tổng hợp)
  app.use('/v3/api-docs', express.json(), (req, res) => {
    res.json(document);
  });

  await app.listen(port);
  console.log(`🚀 Communication Service running on port ${port}`);

  const actuatorApp = express();
  actuatorApp.get('/actuator/health', (_req, res) => {
    res.json({ status: 'UP' });
  });

  actuatorApp.listen(
    Number(process.env.ACTUATOR_PORT),
    process.env.HOST || 'localhost',
    () => {
      console.log(`Actuator running on port ${process.env.ACTUATOR_PORT}`);
    },
  );

  // ===== Register Eureka =====
  eurekaClient.start((error) => {
    if (error) console.error('❌ Eureka registration failed:', error);
    else console.log('✅ Registered to Eureka');
  });
}

bootstrap();
