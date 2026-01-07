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
exports.AdvertMedia = exports.AdvertMediaType = void 0;
const typeorm_1 = require("typeorm");
const advert_request_entity_1 = require("./advert_request.entity");
var AdvertMediaType;
(function (AdvertMediaType) {
    AdvertMediaType["IMAGE"] = "IMAGE";
    AdvertMediaType["VIDEO"] = "VIDEO";
})(AdvertMediaType || (exports.AdvertMediaType = AdvertMediaType = {}));
let AdvertMedia = class AdvertMedia {
    id;
    advertId;
    url;
    type;
    advert;
};
exports.AdvertMedia = AdvertMedia;
__decorate([
    (0, typeorm_1.PrimaryGeneratedColumn)(),
    __metadata("design:type", Number)
], AdvertMedia.prototype, "id", void 0);
__decorate([
    (0, typeorm_1.Column)(),
    __metadata("design:type", Number)
], AdvertMedia.prototype, "advertId", void 0);
__decorate([
    (0, typeorm_1.Column)({ length: 500 }),
    __metadata("design:type", String)
], AdvertMedia.prototype, "url", void 0);
__decorate([
    (0, typeorm_1.Column)({
        type: 'enum',
        enum: AdvertMediaType,
    }),
    __metadata("design:type", String)
], AdvertMedia.prototype, "type", void 0);
__decorate([
    (0, typeorm_1.ManyToOne)(() => advert_request_entity_1.AdvertRequest, (advert) => advert.media, {
        onDelete: 'CASCADE',
    }),
    (0, typeorm_1.JoinColumn)({ name: 'advertId' }),
    __metadata("design:type", advert_request_entity_1.AdvertRequest)
], AdvertMedia.prototype, "advert", void 0);
exports.AdvertMedia = AdvertMedia = __decorate([
    (0, typeorm_1.Entity)('advert_media')
], AdvertMedia);
//# sourceMappingURL=advert_media.entity.js.map