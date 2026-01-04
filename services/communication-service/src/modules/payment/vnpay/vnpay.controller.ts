import { Controller, Post, Get, Body, Req, Query } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiQuery, ApiResponse } from '@nestjs/swagger';
import { BookoraVnpayService } from './vnpay.service';
import { CreateVnpayDto } from './dto/create-vnpay.dto';
import type { Request } from 'express';

@ApiTags('VNPay')
@Controller('advert/payment/vnpay')
export class VnpayController {
  constructor(private readonly vnpayService: BookoraVnpayService) {}

  @Post()
  @ApiOperation({ summary: 'Tạo URL thanh toán VNPay' })
  @ApiResponse({
    status: 201,
    description: 'Trả về URL thanh toán và mã giao dịch',
    schema: {
      example: {
        paymentUrl: 'https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?...',
        txnRef: 'a1b2c3d4',
      },
    },
  })
  createPayment(@Body() dto: CreateVnpayDto, @Req() req: Request) {
    const ipAddr =
      req.headers['x-forwarded-for'] || req.socket.remoteAddress || '127.0.0.1';

    return this.vnpayService.createPaymentUrl(
      dto.amount,
      ipAddr.toString(),
      dto.redirectUrl,
    );
  }

  @Get()
  @ApiOperation({ summary: 'Kiểm tra kết quả thanh toán VNPay' })
  @ApiQuery({
    name: 'vnp_TxnRef',
    required: false,
    description: 'Mã giao dịch do hệ thống tạo',
    type: String,
  })
  @ApiResponse({
    status: 200,
    description: 'Kết quả xác thực giao dịch',
    schema: {
      example: {
        message: 'Thanh toán thành công 🎉',
        data: {
          vnp_TxnRef: 'a1b2c3d4',
          vnp_ResponseCode: '00',
          vnp_Amount: '10000000',
          vnp_OrderInfo: 'Thanh toán đơn hàng a1b2c3d4',
        },
      },
    },
  })
  async checkPayment(@Query() query: Record<string, string>) {
    const verified = await this.vnpayService.verifyReturnQuery(query);

    if (verified) {
      // ✅ chia lại 100 để hiển thị đúng giá trị tiền
      const amount = Number(query.vnp_Amount) / 100;

      return {
        message: 'Thanh toán thành công 🎉',
        data: { ...query, vnp_Amount: amount },
        success: true,
      };
    }

    return { message: 'Xác thực thất bại ❌', data: query, success: false };
  }
}
