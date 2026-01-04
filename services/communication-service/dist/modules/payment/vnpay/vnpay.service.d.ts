import { VnpayService } from 'nestjs-vnpay';
export declare class BookoraVnpayService {
    private readonly vnpay;
    constructor(vnpay: VnpayService);
    createPaymentUrl(amount: number, ipAddr: string, redirectUrl?: string): {
        paymentUrl: string;
        txnRef: string;
    };
    verifyReturnQuery(query: Record<string, string>): Promise<import("vnpay", { with: { "resolution-mode": "import" } }).VerifyReturnUrl>;
}
