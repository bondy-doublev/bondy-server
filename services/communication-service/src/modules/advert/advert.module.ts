import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { AdvertMedia } from 'src/entities/advert_media.entity';
import { AdvertRequest } from 'src/entities/advert_request.entity';
import { AdvertService } from './advert.service';
import { AdvertController } from './advert.controller';

@Module({
  imports: [TypeOrmModule.forFeature([AdvertRequest, AdvertMedia])],
  providers: [AdvertService],
  controllers: [AdvertController],
})
export class AdvertModule {}
