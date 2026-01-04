import { Controller, Post, Body } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse } from '@nestjs/swagger';
import { MomoService, MomoResponse } from './momo.service';
import { CreateMomoPaymentDto } from './dto/create-momo-payment.dto';

@ApiTags('MoMo Payment')
@Controller('advert/payment/momo')
export class MomoController {
  constructor(private readonly momoService: MomoService) {}

  @Post()
  @ApiOperation({ summary: 'Tạo liên kết thanh toán MoMo' })
  @ApiResponse({
    status: 200,
    description: 'Tạo link thanh toán thành công',
    schema: {
      example: {
        message: 'Payment link created successfully',
        payUrl:
          'https://test-payment.momo.vn/v2/gateway/pay?t=TU9NT3xNT01PMTc1OTg1MDcxMjk1OA&s=abcd...',
      },
    },
  })
  async createPayment(
    @Body() dto: CreateMomoPaymentDto,
  ): Promise<{ message: string; data: MomoResponse; payUrl?: string }> {
    const { amount, redirectUrl } = dto;
    const result = await this.momoService.createPayment(amount, redirectUrl);

    return {
      message: 'Payment link created successfully',
      data: result,
      payUrl: result.payUrl,
    };
  }
}
