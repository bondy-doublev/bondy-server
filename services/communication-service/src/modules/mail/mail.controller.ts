import { Controller, Post, Body } from '@nestjs/common';
import { ApiTags, ApiOperation } from '@nestjs/swagger';
import { MailService } from './mail.service';

@ApiTags('Mail')
@Controller('advert/mail')
export class MailController {
  constructor(private readonly mailService: MailService) {}

  @Post('payment-request')
  @ApiOperation({ summary: 'Send payment request email to user' })
  async paymentRequest(
    @Body()
    body: {
      to: string;
      userName: string;
      advertTitle: string;
      amount: number;
      dueDate: string;
    },
  ) {
    return this.mailService.sendPaymentRequest(body);
  }

  @Post('payment-success')
  @ApiOperation({ summary: 'Send payment success email to user' })
  async paymentSuccess(
    @Body()
    body: {
      to: string;
      userName: string;
      advertTitle: string;
      amount: number;
    },
  ) {
    return this.mailService.sendPaymentSuccess(body);
  }
}
