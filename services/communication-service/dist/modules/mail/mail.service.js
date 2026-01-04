"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.MailService = void 0;
const common_1 = require("@nestjs/common");
const nodemailer = __importStar(require("nodemailer"));
let MailService = class MailService {
    transporter;
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
    async sendPaymentRequest(params) {
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
        }
        catch (err) {
            console.error(err);
            throw new common_1.InternalServerErrorException('Failed to send payment request email');
        }
    }
    async sendPaymentSuccess(params) {
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
        }
        catch (err) {
            console.error(err);
            throw new common_1.InternalServerErrorException('Failed to send payment success email');
        }
    }
};
exports.MailService = MailService;
exports.MailService = MailService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [])
], MailService);
//# sourceMappingURL=mail.service.js.map