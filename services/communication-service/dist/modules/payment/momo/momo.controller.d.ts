import { MomoService, MomoResponse } from './momo.service';
import { CreateMomoPaymentDto } from './dto/create-momo-payment.dto';
export declare class MomoController {
    private readonly momoService;
    constructor(momoService: MomoService);
    createPayment(dto: CreateMomoPaymentDto): Promise<{
        message: string;
        data: MomoResponse;
        payUrl?: string;
    }>;
}
