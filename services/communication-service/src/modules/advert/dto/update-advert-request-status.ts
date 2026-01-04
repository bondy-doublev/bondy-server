import { IsEnum } from 'class-validator';
import { AdvertRequestStatus } from 'src/entities/advert_request.entity';

export class UpdateAdvertRequestStatusDto {
  @IsEnum(AdvertRequestStatus)
  status: AdvertRequestStatus;
}
