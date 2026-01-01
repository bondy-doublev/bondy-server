import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  // Dynamic CORS
  const allowedOrigins = Object.keys(process.env)
    .filter(key => /^API\d+_CLIENT_URL$/.test(key))
    .map(key => process.env[key]!)
    .filter(Boolean);

  app.enableCors({
    origin: (origin, callback) => {
      if (!origin || allowedOrigins.includes(origin)) callback(null, true);
      else callback(new Error('Not allowed by CORS'));
    },
    methods: 'GET,HEAD,PUT,PATCH,POST,DELETE,OPTIONS',
    allowedHeaders: ['Content-Type', 'Authorization', 'x-api-key'],
    credentials: true,
  });

  // ⚡ HEALTH CHECK — MUST for proxy
  app.getHttpAdapter().get('/health', (req, res) => {
    res.json({ status: 'UP', name: 'proxy-server' });
  });

  await app.listen(process.env.PORT || 3333, '0.0.0.0');
  console.log(`Proxy server running on :${process.env.PORT || 3333}`);
}

bootstrap();
