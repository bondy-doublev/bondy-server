import { Module } from '@nestjs/common';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { VnpayModule } from 'nestjs-vnpay';
import { ignoreLogger, HashAlgorithm } from 'vnpay';
import { VnpayController } from './vnpay.controller';
import { BookoraVnpayService } from './vnpay.service';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
    }),
    VnpayModule.registerAsync({
      imports: [ConfigModule],
      inject: [ConfigService],
      useFactory: (configService: ConfigService) => ({
        tmnCode: configService.get<string>('VNPAY_TMN_CODE', '123456'),
        secureSecret: configService.get<string>('VNPAY_SECURE_SECRET', 'hello'),
        vnpayHost: 'https://sandbox.vnpayment.vn',
        testMode: true,
        hashAlgorithm: HashAlgorithm.SHA512,
        enableLog: true,
        loggerFn: ignoreLogger,
      }),
    }),
  ],
  controllers: [VnpayController],
  providers: [BookoraVnpayService],
  exports: [BookoraVnpayService],
})
export class BondyVnpayModule {}
