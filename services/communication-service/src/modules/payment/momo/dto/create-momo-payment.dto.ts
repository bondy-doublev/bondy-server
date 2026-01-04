import { ApiProperty } from '@nestjs/swagger';
import { IsNotEmpty, IsNumber, IsOptional, IsString } from 'class-validator';

export class CreateMomoPaymentDto {
  @ApiProperty({
    example: '50000',
    description: 'Số tiền cần thanh toán (VNĐ)',
  })
  @IsNumber()
  @IsNotEmpty()
  amount: number;

  @ApiProperty({
    example: 'https://myshop.com/payment/success',
    description: 'URL để MoMo redirect sau khi thanh toán thành công',
    required: false,
  })
  @IsOptional()
  @IsString()
  redirectUrl?: string;
}
