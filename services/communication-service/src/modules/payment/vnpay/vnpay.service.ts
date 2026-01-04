import { Injectable } from '@nestjs/common';
import { VnpayService } from 'nestjs-vnpay';
import {
  ProductCode,
  VnpLocale,
  dateFormat,
  ReturnQueryFromVNPay,
} from 'vnpay';
import { randomUUID } from 'crypto';

@Injectable()
export class BookoraVnpayService {
  constructor(private readonly vnpay: VnpayService) {}

  createPaymentUrl(amount: number, ipAddr: string, redirectUrl?: string) {
    const txnRef = randomUUID().replace(/-/g, '').slice(0, 8);

    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);

    const url = this.vnpay.buildPaymentUrl({
      vnp_Amount: amount,
      vnp_IpAddr: ipAddr,
      vnp_TxnRef: txnRef,
      vnp_OrderInfo: `Thanh toán đơn hàng ${txnRef}`,
      vnp_OrderType: ProductCode.Other,
      vnp_ReturnUrl:
        redirectUrl || process.env.VNPAY_RETURN_URL || 'return url',
      vnp_Locale: VnpLocale.VN,
      vnp_CreateDate: dateFormat(new Date()),
      vnp_ExpireDate: dateFormat(tomorrow),
    });

    return { paymentUrl: url, txnRef };
  }

  async verifyReturnQuery(query: Record<string, string>) {
    return await this.vnpay.verifyReturnUrl(
      query as unknown as ReturnQueryFromVNPay,
    );
  }
}
