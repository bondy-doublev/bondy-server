"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.AdvertModule = void 0;
const common_1 = require("@nestjs/common");
const typeorm_1 = require("@nestjs/typeorm");
const advert_media_entity_1 = require("../../entities/advert_media.entity");
const advert_request_entity_1 = require("../../entities/advert_request.entity");
const advert_service_1 = require("./advert.service");
const advert_controller_1 = require("./advert.controller");
let AdvertModule = class AdvertModule {
};
exports.AdvertModule = AdvertModule;
exports.AdvertModule = AdvertModule = __decorate([
    (0, common_1.Module)({
        imports: [typeorm_1.TypeOrmModule.forFeature([advert_request_entity_1.AdvertRequest, advert_media_entity_1.AdvertMedia])],
        providers: [advert_service_1.AdvertService],
        controllers: [advert_controller_1.AdvertController],
    })
], AdvertModule);
//# sourceMappingURL=advert.module.js.map