import { ApiProperty } from '@nestjs/swagger';
import { IsString } from 'class-validator';

export class ChatDto {
  @IsString()
  @ApiProperty({
    example: 'Làm sao để đăng ký tài khoản?',
    description: 'Câu hỏi của người dùng gửi tới chatbot',
  })
  message: string;
}
