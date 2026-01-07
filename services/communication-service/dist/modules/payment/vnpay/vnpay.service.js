"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.BookoraVnpayService = void 0;
const common_1 = require("@nestjs/common");
const nestjs_vnpay_1 = require("nestjs-vnpay");
const vnpay_1 = require("vnpay");
const crypto_1 = require("crypto");
let BookoraVnpayService = class BookoraVnpayService {
    vnpay;
    constructor(vnpay) {
        this.vnpay = vnpay;
    }
    createPaymentUrl(amount, ipAddr, redirectUrl) {
        const txnRef = (0, crypto_1.randomUUID)().replace(/-/g, '').slice(0, 8);
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        const url = this.vnpay.buildPaymentUrl({
            vnp_Amount: amount,
            vnp_IpAddr: ipAddr,
            vnp_TxnRef: txnRef,
            vnp_OrderInfo: `Thanh toán đơn hàng ${txnRef}`,
            vnp_OrderType: vnpay_1.ProductCode.Other,
            vnp_ReturnUrl: redirectUrl || process.env.VNPAY_RETURN_URL || 'return url',
            vnp_Locale: vnpay_1.VnpLocale.VN,
            vnp_CreateDate: (0, vnpay_1.dateFormat)(new Date()),
            vnp_ExpireDate: (0, vnpay_1.dateFormat)(tomorrow),
        });
        return { paymentUrl: url, txnRef };
    }
    async verifyReturnQuery(query) {
        return await this.vnpay.verifyReturnUrl(query);
    }
};
exports.BookoraVnpayService = BookoraVnpayService;
exports.BookoraVnpayService = BookoraVnpayService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [nestjs_vnpay_1.VnpayService])
], BookoraVnpayService);
//# sourceMappingURL=vnpay.service.js.map