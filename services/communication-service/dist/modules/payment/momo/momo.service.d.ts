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
export declare class MomoService {
    createPayment(amount: number, redirectUrl?: string): Promise<MomoResponse>;
}
