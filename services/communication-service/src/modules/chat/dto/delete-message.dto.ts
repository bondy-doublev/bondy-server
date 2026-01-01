// src/modules/chat/dto/delete-message.dto.ts
import { IsNumber } from 'class-validator';

export class DeleteMessageDto {
  @IsNumber()
  userId: number;
}
