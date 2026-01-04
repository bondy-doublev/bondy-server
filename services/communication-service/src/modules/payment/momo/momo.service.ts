import { Injectable } from '@nestjs/common';
import * as crypto from 'crypto';
import * as https from 'https';

export interface MomoResponse {
  partnerCode: string;
  requestId: string;
  orderId: string;
  amount: string;
  responseTime: number;
  message: string;
  resultCode: number;
  payUrl?: string;
  deeplink?: string;
  qrCodeUrl?: string;
  [key: string]: any;
}

@Injectable()
export class MomoService {
  async createPayment(
    amount: number,
    redirectUrl?: string,
  ): Promise<MomoResponse> {
    const partnerCode = 'MOMO';
    const accessKey = 'F8BBA842ECF85';
    const secretKey = 'K951B6PE1waDMi640xX08PD3vg6EkVlz';
    const requestId = partnerCode + new Date().getTime();
    const orderId = requestId;
    const orderInfo = 'Pay with MoMo';
    const defaultRedirectUrl = 'https://momo.vn/return';
    const ipnUrl = 'https://callback.url/notify';
    const requestType = 'captureWallet';
    const extraData = '';

    // Dùng redirectUrl do frontend gửi lên hoặc mặc định
    const finalRedirectUrl = redirectUrl || defaultRedirectUrl;

    const rawSignature = `accessKey=${accessKey}&amount=${amount}&extraData=${extraData}&ipnUrl=${ipnUrl}&orderId=${orderId}&orderInfo=${orderInfo}&partnerCode=${partnerCode}&redirectUrl=${finalRedirectUrl}&requestId=${requestId}&requestType=${requestType}`;

    const signature = crypto
      .createHmac('sha256', secretKey)
      .update(rawSignature)
      .digest('hex');

    const requestBody = JSON.stringify({
      partnerCode,
      accessKey,
      requestId,
      amount,
      orderId,
      orderInfo,
      redirectUrl: finalRedirectUrl,
      ipnUrl,
      extraData,
      requestType,
      signature,
      lang: 'en',
    });

    const options = {
      hostname: 'test-payment.momo.vn',
      port: 443,
      path: '/v2/gateway/api/create',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(requestBody),
      },
    };

    return new Promise<MomoResponse>((resolve, reject) => {
      const req = https.request(options, (res) => {
        let data = '';
        res.on('data', (chunk) => (data += chunk));
        res.on('end', () => {
          try {
            const parsed: MomoResponse = JSON.parse(data);
            resolve(parsed);
          } catch (err) {
            reject(err);
          }
        });
      });

      req.on('error', (e) => reject(e));
      req.write(requestBody);
      req.end();
    });
  }
}
