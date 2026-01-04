import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ChatModule } from './modules/chat/chat.module';
import { AppController } from './app.controller';
import { AdvertModule } from './modules/advert/advert.module';
import { MailModule } from './modules/mail/mail.module';
import { MomoModule } from './modules/payment/momo/momo.module';
import { BondyVnpayModule } from './modules/payment/vnpay/vnpay.module';
import { ConfigModule } from '@nestjs/config';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true, // cho phép dùng env ở mọi module
    }),

    TypeOrmModule.forRoot({
      type: 'postgres',
      host: process.env.DB_HOST,
      port: parseInt(process.env.DB_PORT!),
      username: process.env.DB_USER,
      password: process.env.DB_PASSWORD,
      database: process.env.DB_NAME,
      autoLoadEntities: true,
      synchronize: true,
    }),
    ChatModule,
    AdvertModule,
    MailModule,
    MomoModule,
    BondyVnpayModule,
  ],
  controllers: [AppController],
})
export class AppModule {}
