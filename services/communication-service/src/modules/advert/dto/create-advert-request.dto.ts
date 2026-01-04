import {
  IsArray,
  IsDateString,
  IsNotEmpty,
  ValidateNested,
  IsOptional,
  IsInt,
  IsEnum,
  IsString,
  IsEmail,
} from 'class-validator';
import { Type } from 'class-transformer';
import { AdvertMediaType } from 'src/entities/advert_media.entity';

class AdvertMediaDto {
  @IsNotEmpty()
  url: string;

  @IsEnum(AdvertMediaType)
  type: AdvertMediaType;
}

export class CreateAdvertRequestDto {
  @IsNotEmpty()
  userId: number;

  @IsEmail()
  @IsOptional()
  userEmail?: string;

  @IsString()
  @IsOptional()
  userAvatar?: string;

  @IsNotEmpty()
  accountName: string;

  @IsNotEmpty()
  title: string;

  @IsOptional()
  @IsInt()
  postId?: number;

  @IsDateString()
  startDate: string;

  @IsDateString()
  endDate: string;

  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => AdvertMediaDto)
  media: AdvertMediaDto[];
}
