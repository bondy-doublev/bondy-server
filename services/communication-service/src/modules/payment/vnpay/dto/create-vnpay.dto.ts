import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsNumber, Min, IsOptional, IsString } from 'class-validator';

export class CreateVnpayDto {
  @ApiProperty({
    description: 'Số tiền cần thanh toán (đơn vị: VND)',
    example: 100000,
  })
  @IsNumber({}, { message: 'amount phải là số' })
  @Min(1000, { message: 'Số tiền tối thiểu là 1,000 VND' })
  amount: number;

  @ApiPropertyOptional({
    description: 'URL để VNPay redirect về sau khi thanh toán',
    example: 'https://myfrontend.com/payment/result',
  })
  @IsOptional()
  @IsString()
  redirectUrl?: string;
}
