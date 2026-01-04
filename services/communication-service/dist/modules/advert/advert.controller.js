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
exports.AdvertController = void 0;
const common_1 = require("@nestjs/common");
const swagger_1 = require("@nestjs/swagger");
const advert_service_1 = require("./advert.service");
const create_advert_request_dto_1 = require("./dto/create-advert-request.dto");
const update_advert_request_status_1 = require("./dto/update-advert-request-status");
const advert_request_entity_1 = require("../../entities/advert_request.entity");
const filter_advert_dto_1 = require("./dto/filter-advert.dto");
const get_my_request_dto_1 = require("./dto/get-my-request.dto");
let AdvertController = class AdvertController {
    advertService;
    constructor(advertService) {
        this.advertService = advertService;
    }
    create(req, dto) {
        return this.advertService.create(dto.userId, dto);
    }
    getAllForAdmin(query) {
        return this.advertService.findAllForAdmin(query);
    }
    getActiveAdverts() {
        return this.advertService.findActiveAdverts();
    }
    async getMyRequests(query) {
        console.log('userId', query.userId);
        return this.advertService.findMyRequests(query.userId ?? 0);
    }
    getAll() {
        return this.advertService.findAllRequests();
    }
    updateStatus(id, dto) {
        return this.advertService.updateStatus(+id, dto.status);
    }
    getById(id) {
        return this.advertService.findById(id);
    }
};
exports.AdvertController = AdvertController;
__decorate([
    (0, common_1.Post)(),
    (0, swagger_1.ApiOperation)({ summary: 'User tạo request quảng cáo' }),
    (0, swagger_1.ApiBody)({ type: create_advert_request_dto_1.CreateAdvertRequestDto }),
    (0, swagger_1.ApiResponse)({
        status: 201,
        description: 'Advert request được tạo thành công',
        type: advert_request_entity_1.AdvertRequest,
    }),
    __param(0, (0, common_1.Req)()),
    __param(1, (0, common_1.Body)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, create_advert_request_dto_1.CreateAdvertRequestDto]),
    __metadata("design:returntype", void 0)
], AdvertController.prototype, "create", null);
__decorate([
    (0, common_1.Get)('admin'),
    (0, swagger_1.ApiOperation)({ summary: 'Admin lấy danh sách advert (filter + pagination)' }),
    (0, swagger_1.ApiResponse)({ status: 200 }),
    __param(0, (0, common_1.Query)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [filter_advert_dto_1.FilterAdvertDto]),
    __metadata("design:returntype", void 0)
], AdvertController.prototype, "getAllForAdmin", null);
__decorate([
    (0, common_1.Get)('active'),
    (0, swagger_1.ApiOperation)({ summary: 'Lấy danh sách quảng cáo đang chạy ngoài web' }),
    (0, swagger_1.ApiResponse)({ status: 200, type: [advert_request_entity_1.AdvertRequest] }),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", []),
    __metadata("design:returntype", void 0)
], AdvertController.prototype, "getActiveAdverts", null);
__decorate([
    (0, common_1.Get)('me'),
    (0, swagger_1.ApiOperation)({
        summary: 'Lấy danh sách request của user hiện tại hoặc theo userId query param',
    }),
    (0, swagger_1.ApiResponse)({
        status: 200,
        description: 'Danh sách request',
        type: [advert_request_entity_1.AdvertRequest],
    }),
    __param(0, (0, common_1.Query)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [get_my_request_dto_1.GetMyRequestsQuery]),
    __metadata("design:returntype", Promise)
], AdvertController.prototype, "getMyRequests", null);
__decorate([
    (0, common_1.Get)(),
    (0, swagger_1.ApiOperation)({ summary: 'Admin lấy tất cả request quảng cáo' }),
    (0, swagger_1.ApiResponse)({
        status: 200,
        description: 'Danh sách tất cả request quảng cáo',
        type: [advert_request_entity_1.AdvertRequest],
    }),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", []),
    __metadata("design:returntype", void 0)
], AdvertController.prototype, "getAll", null);
__decorate([
    (0, common_1.Patch)(':id/status'),
    (0, swagger_1.ApiOperation)({ summary: 'Admin cập nhật trạng thái request quảng cáo' }),
    (0, swagger_1.ApiParam)({
        name: 'id',
        type: Number,
        description: 'ID của advert request',
    }),
    (0, swagger_1.ApiBody)({ type: update_advert_request_status_1.UpdateAdvertRequestStatusDto }),
    (0, swagger_1.ApiResponse)({
        status: 200,
        description: 'Advert request đã được cập nhật status',
        type: advert_request_entity_1.AdvertRequest,
    }),
    __param(0, (0, common_1.Param)('id')),
    __param(1, (0, common_1.Body)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, update_advert_request_status_1.UpdateAdvertRequestStatusDto]),
    __metadata("design:returntype", void 0)
], AdvertController.prototype, "updateStatus", null);
__decorate([
    (0, common_1.Get)(':id'),
    (0, swagger_1.ApiOperation)({ summary: 'Lấy chi tiết advert request theo ID' }),
    (0, swagger_1.ApiParam)({
        name: 'id',
        type: Number,
        description: 'ID của advert request',
    }),
    (0, swagger_1.ApiResponse)({
        status: 200,
        description: 'Chi tiết advert request',
        type: advert_request_entity_1.AdvertRequest,
    }),
    __param(0, (0, common_1.Param)('id', common_1.ParseIntPipe)),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Number]),
    __metadata("design:returntype", void 0)
], AdvertController.prototype, "getById", null);
exports.AdvertController = AdvertController = __decorate([
    (0, swagger_1.ApiTags)('Advert'),
    (0, common_1.Controller)('advert'),
    __metadata("design:paramtypes", [advert_service_1.AdvertService])
], AdvertController);
//# sourceMappingURL=advert.controller.js.map