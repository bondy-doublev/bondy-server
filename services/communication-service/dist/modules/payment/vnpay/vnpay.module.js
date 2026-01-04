"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.BondyVnpayModule = void 0;
const common_1 = require("@nestjs/common");
const config_1 = require("@nestjs/config");
const nestjs_vnpay_1 = require("nestjs-vnpay");
const vnpay_1 = require("vnpay");
const vnpay_controller_1 = require("./vnpay.controller");
const vnpay_service_1 = require("./vnpay.service");
let BondyVnpayModule = class BondyVnpayModule {
};
exports.BondyVnpayModule = BondyVnpayModule;
exports.BondyVnpayModule = BondyVnpayModule = __decorate([
    (0, common_1.Module)({
        imports: [
            config_1.ConfigModule.forRoot({
                isGlobal: true,
            }),
            nestjs_vnpay_1.VnpayModule.registerAsync({
                imports: [config_1.ConfigModule],
                inject: [config_1.ConfigService],
                useFactory: (configService) => ({
                    tmnCode: configService.get('VNPAY_TMN_CODE', '123456'),
                    secureSecret: configService.get('VNPAY_SECURE_SECRET', 'hello'),
                    vnpayHost: 'https://sandbox.vnpayment.vn',
                    testMode: true,
                    hashAlgorithm: vnpay_1.HashAlgorithm.SHA512,
                    enableLog: true,
                    loggerFn: vnpay_1.ignoreLogger,
                }),
            }),
        ],
        controllers: [vnpay_controller_1.VnpayController],
        providers: [vnpay_service_1.BookoraVnpayService],
        exports: [vnpay_service_1.BookoraVnpayService],
    })
], BondyVnpayModule);
//# sourceMappingURL=vnpay.module.js.map