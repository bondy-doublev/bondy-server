import { Injectable, InternalServerErrorException } from '@nestjs/common';
import * as nodemailer from 'nodemailer';

@Injectable()
export class MailService {
  private transporter: nodemailer.Transporter;

  constructor() {
    this.transporter = nodemailer.createTransport({
      host: process.env.SMTP_HOST,
      port: Number(process.env.SMTP_PORT) || 587,
      secure: false,
      auth: {
        user: process.env.SMTP_USER,
        pass: process.env.SMTP_PASS,
      },
    });
  }

  // ---------------- Mail nhắc thanh toán ----------------
  async sendPaymentRequest(params: {
    to: string;
    userName: string;
    advertTitle: string;
    amount: number;
    dueDate: string;
  }) {
    const { to, userName, advertTitle, amount, dueDate } = params;

    const mailOptions = {
      from: `"Bondy Ads" <${process.env.SMTP_USER}>`,
      to,
      subject: `Advert Approved: Please complete your payment`,
      html: `
        <p>Hi ${userName},</p>
        <p>Your advert <b>"${advertTitle}"</b> has been approved by admin.</p>
        <p>Please complete the payment to finalize your advert.</p>
        <p><b>Amount:</b> ${amount.toLocaleString()} VND</p>
        <p><b>Due date:</b> ${dueDate}</p>
        <br/>
        <p>Thanks,<br/>Bondy Ads Team</p>
      `,
    };

    try {
      await this.transporter.sendMail(mailOptions);
      return { success: true, message: 'Payment request email sent' };
    } catch (err) {
      console.error(err);
      throw new InternalServerErrorException(
        'Failed to send payment request email',
      );
    }
  }

  // ---------------- Mail thanh toán thành công ----------------
  async sendPaymentSuccess(params: {
    to: string;
    userName: string;
    advertTitle: string;
    amount: number;
  }) {
    const { to, userName, advertTitle, amount } = params;

    const mailOptions = {
      from: `"Bondy Ads Team" <${process.env.SMTP_USER}>`,
      to,
      subject: `Payment Successful: Advert "${advertTitle}" is live`,
      html: `
        <p>Hi ${userName},</p>
        <p>We have received your payment of <b>${amount.toLocaleString()} VND</b> for advert <b>"${advertTitle}"</b>.</p>
        <p>Your advert is now active and running.</p>
        <br/>
        <p>Thanks,<br/>Bondy Ads Team</p>
      `,
    };

    try {
      await this.transporter.sendMail(mailOptions);
      return { success: true, message: 'Payment success email sent' };
    } catch (err) {
      console.error(err);
      throw new InternalServerErrorException(
        'Failed to send payment success email',
      );
    }
  }
}
