// src/modules/chat/dto/edit-message.dto.ts
import { IsNumber, IsString } from 'class-validator';

export class EditMessageDto {
  @IsNumber()
  userId: number;

  @IsString()
  content: string;
}
