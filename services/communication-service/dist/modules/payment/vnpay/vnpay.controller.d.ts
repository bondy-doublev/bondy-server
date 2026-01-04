import { BookoraVnpayService } from './vnpay.service';
import { CreateVnpayDto } from './dto/create-vnpay.dto';
import type { Request } from 'express';
export declare class VnpayController {
    private readonly vnpayService;
    constructor(vnpayService: BookoraVnpayService);
    createPayment(dto: CreateVnpayDto, req: Request): {
        paymentUrl: string;
        txnRef: string;
    };
    checkPayment(query: Record<string, string>): Promise<{
        message: string;
        data: {
            vnp_Amount: number;
        };
        success: boolean;
    } | {
        message: string;
        data: Record<string, string>;
        success: boolean;
    }>;
}
