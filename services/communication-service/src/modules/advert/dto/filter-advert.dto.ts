import { IsEnum, IsOptional, IsInt, Min } from 'class-validator';
import { Type } from 'class-transformer';
import { AdvertRequestStatus } from 'src/entities/advert_request.entity';

export class FilterAdvertDto {
  @IsOptional()
  @IsEnum(AdvertRequestStatus)
  status?: AdvertRequestStatus;

  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  page?: number = 1;

  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  limit?: number = 10;
}
