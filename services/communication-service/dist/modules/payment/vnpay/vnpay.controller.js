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
var __param = (this && this.__param) || function (paramIndex, decorator) {
    return function (target, key) { decorator(target, key, paramIndex); }
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.VnpayController = void 0;
const common_1 = require("@nestjs/common");
const swagger_1 = require("@nestjs/swagger");
const vnpay_service_1 = require("./vnpay.service");
const create_vnpay_dto_1 = require("./dto/create-vnpay.dto");
let VnpayController = class VnpayController {
    vnpayService;
    constructor(vnpayService) {
        this.vnpayService = vnpayService;
    }
    createPayment(dto, req) {
        const ipAddr = req.headers['x-forwarded-for'] || req.socket.remoteAddress || '127.0.0.1';
        return this.vnpayService.createPaymentUrl(dto.amount, ipAddr.toString(), dto.redirectUrl);
    }
    async checkPayment(query) {
        const verified = await this.vnpayService.verifyReturnQuery(query);
        if (verified) {
            const amount = Number(query.vnp_Amount) / 100;
            return {
                message: 'Thanh toán thành công 🎉',
                data: { ...query, vnp_Amount: amount },
                success: true,
            };
        }
        return { message: 'Xác thực thất bại ❌', data: query, success: false };
    }
};
exports.VnpayController = VnpayController;
__decorate([
    (0, common_1.Post)(),
    (0, swagger_1.ApiOperation)({ summary: 'Tạo URL thanh toán VNPay' }),
    (0, swagger_1.ApiResponse)({
        status: 201,
        description: 'Trả về URL thanh toán và mã giao dịch',
        schema: {
            example: {
                paymentUrl: 'https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?...',
                txnRef: 'a1b2c3d4',
            },
        },
    }),
    __param(0, (0, common_1.Body)()),
    __param(1, (0, common_1.Req)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [create_vnpay_dto_1.CreateVnpayDto, Object]),
    __metadata("design:returntype", void 0)
], VnpayController.prototype, "createPayment", null);
__decorate([
    (0, common_1.Get)(),
    (0, swagger_1.ApiOperation)({ summary: 'Kiểm tra kết quả thanh toán VNPay' }),
    (0, swagger_1.ApiQuery)({
        name: 'vnp_TxnRef',
        required: false,
        description: 'Mã giao dịch do hệ thống tạo',
        type: String,
    }),
    (0, swagger_1.ApiResponse)({
        status: 200,
        description: 'Kết quả xác thực giao dịch',
        schema: {
            example: {
                message: 'Thanh toán thành công 🎉',
                data: {
                    vnp_TxnRef: 'a1b2c3d4',
                    vnp_ResponseCode: '00',
                    vnp_Amount: '10000000',
                    vnp_OrderInfo: 'Thanh toán đơn hàng a1b2c3d4',
                },
            },
        },
    }),
    __param(0, (0, common_1.Query)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object]),
    __metadata("design:returntype", Promise)
], VnpayController.prototype, "checkPayment", null);
exports.VnpayController = VnpayController = __decorate([
    (0, swagger_1.ApiTags)('VNPay'),
    (0, common_1.Controller)('advert/payment/vnpay'),
    __metadata("design:paramtypes", [vnpay_service_1.BookoraVnpayService])
], VnpayController);
//# sourceMappingURL=vnpay.controller.js.map