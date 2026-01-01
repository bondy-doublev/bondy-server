import {
  IsString,
  IsBoolean,
  IsArray,
  ArrayNotEmpty,
  IsNumber,
} from 'class-validator';

export class CreateRoomDto {
  @IsString()
  name: string;

  @IsBoolean()
  isGroup: boolean;

  @IsArray()
  @ArrayNotEmpty()
  @IsNumber({}, { each: true }) // mỗi phần tử phải là number
  memberIds: number[];
}
