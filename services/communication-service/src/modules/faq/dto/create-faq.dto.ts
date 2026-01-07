import { ApiProperty } from '@nestjs/swagger';
import { IsString } from 'class-validator';

export class CreateFaqDto {
  @IsString()
  @ApiProperty({
    example: 'Làm sao để đăng ký tài khoản?',
    description: 'Câu hỏi FAQ',
  })
  question: string;

  @IsString()
  @ApiProperty({
    example: 'Bạn bấm nút đăng ký và nhập email, mật khẩu.',
    description: 'Câu trả lời FAQ',
  })
  answer: string;
}
