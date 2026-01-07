export declare class MailService {
    private transporter;
    constructor();
    sendPaymentRequest(params: {
        to: string;
        userName: string;
        advertTitle: string;
        amount: number;
        dueDate: string;
    }): Promise<{
        success: boolean;
        message: string;
    }>;
    sendPaymentSuccess(params: {
        to: string;
        userName: string;
        advertTitle: string;
        amount: number;
    }): Promise<{
        success: boolean;
        message: string;
    }>;
}
