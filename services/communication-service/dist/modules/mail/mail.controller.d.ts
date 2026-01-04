import { MailService } from './mail.service';
export declare class MailController {
    private readonly mailService;
    constructor(mailService: MailService);
    paymentRequest(body: {
        to: string;
        userName: string;
        advertTitle: string;
        amount: number;
        dueDate: string;
    }): Promise<{
        success: boolean;
        message: string;
    }>;
    paymentSuccess(body: {
        to: string;
        userName: string;
        advertTitle: string;
        amount: number;
    }): Promise<{
        success: boolean;
        message: string;
    }>;
}
