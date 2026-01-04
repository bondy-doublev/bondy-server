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
exports.CreateVnpayDto = void 0;
const swagger_1 = require("@nestjs/swagger");
const class_validator_1 = require("class-validator");
class CreateVnpayDto {
    amount;
    redirectUrl;
}
exports.CreateVnpayDto = CreateVnpayDto;
__decorate([
    (0, swagger_1.ApiProperty)({
        description: 'Số tiền cần thanh toán (đơn vị: VND)',
        example: 100000,
    }),
    (0, class_validator_1.IsNumber)({}, { message: 'amount phải là số' }),
    (0, class_validator_1.Min)(1000, { message: 'Số tiền tối thiểu là 1,000 VND' }),
    __metadata("design:type", Number)
], CreateVnpayDto.prototype, "amount", void 0);
__decorate([
    (0, swagger_1.ApiPropertyOptional)({
        description: 'URL để VNPay redirect về sau khi thanh toán',
        example: 'https://myfrontend.com/payment/result',
    }),
    (0, class_validator_1.IsOptional)(),
    (0, class_validator_1.IsString)(),
    __metadata("design:type", String)
], CreateVnpayDto.prototype, "redirectUrl", void 0);
//# sourceMappingURL=create-vnpay.dto.js.map