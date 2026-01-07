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
exports.AdvertRequest = exports.AdvertRequestStatus = void 0;
const typeorm_1 = require("typeorm");
const advert_media_entity_1 = require("./advert_media.entity");
var AdvertRequestStatus;
(function (AdvertRequestStatus) {
    AdvertRequestStatus["PENDING"] = "pending";
    AdvertRequestStatus["RUNNING"] = "running";
    AdvertRequestStatus["DONE"] = "done";
    AdvertRequestStatus["REJECTED"] = "rejected";
    AdvertRequestStatus["CANCELLED"] = "cancelled";
    AdvertRequestStatus["ACCEPTED"] = "accepted";
})(AdvertRequestStatus || (exports.AdvertRequestStatus = AdvertRequestStatus = {}));
let AdvertRequest = class AdvertRequest {
    id;
    userId;
    userAvatar;
    userEmail;
    accountName;
    title;
    postId;
    pricePerDay;
    totalDays;
    startDate;
    endDate;
    totalPrice;
    status;
    media;
    createdAt;
};
exports.AdvertRequest = AdvertRequest;
__decorate([
    (0, typeorm_1.PrimaryGeneratedColumn)(),
    __metadata("design:type", Number)
], AdvertRequest.prototype, "id", void 0);
__decorate([
    (0, typeorm_1.Column)(),
    __metadata("design:type", Number)
], AdvertRequest.prototype, "userId", void 0);
__decorate([
    (0, typeorm_1.Column)({ nullable: true }),
    __metadata("design:type", String)
], AdvertRequest.prototype, "userAvatar", void 0);
__decorate([
    (0, typeorm_1.Column)({ nullable: true }),
    __metadata("design:type", String)
], AdvertRequest.prototype, "userEmail", void 0);
__decorate([
    (0, typeorm_1.Column)({ length: 255 }),
    __metadata("design:type", String)
], AdvertRequest.prototype, "accountName", void 0);
__decorate([
    (0, typeorm_1.Column)({ length: 255 }),
    __metadata("design:type", String)
], AdvertRequest.prototype, "title", void 0);
__decorate([
    (0, typeorm_1.Column)({ nullable: true }),
    __metadata("design:type", Number)
], AdvertRequest.prototype, "postId", void 0);
__decorate([
    (0, typeorm_1.Column)({ type: 'int' }),
    __metadata("design:type", Number)
], AdvertRequest.prototype, "pricePerDay", void 0);
__decorate([
    (0, typeorm_1.Column)({ type: 'int' }),
    __metadata("design:type", Number)
], AdvertRequest.prototype, "totalDays", void 0);
__decorate([
    (0, typeorm_1.Column)({ type: 'date' }),
    __metadata("design:type", String)
], AdvertRequest.prototype, "startDate", void 0);
__decorate([
    (0, typeorm_1.Column)({ type: 'date' }),
    __metadata("design:type", String)
], AdvertRequest.prototype, "endDate", void 0);
__decorate([
    (0, typeorm_1.Column)({ type: 'int' }),
    __metadata("design:type", Number)
], AdvertRequest.prototype, "totalPrice", void 0);
__decorate([
    (0, typeorm_1.Column)({
        type: 'enum',
        enum: AdvertRequestStatus,
        default: AdvertRequestStatus.PENDING,
    }),
    __metadata("design:type", String)
], AdvertRequest.prototype, "status", void 0);
__decorate([
    (0, typeorm_1.OneToMany)(() => advert_media_entity_1.AdvertMedia, (media) => media.advert, {
        cascade: true,
        eager: true,
    }),
    __metadata("design:type", Array)
], AdvertRequest.prototype, "media", void 0);
__decorate([
    (0, typeorm_1.CreateDateColumn)(),
    __metadata("design:type", Date)
], AdvertRequest.prototype, "createdAt", void 0);
exports.AdvertRequest = AdvertRequest = __decorate([
    (0, typeorm_1.Entity)('advert_requests')
], AdvertRequest);
//# sourceMappingURL=advert_request.entity.js.map