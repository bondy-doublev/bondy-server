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
exports.AdvertService = void 0;
const common_1 = require("@nestjs/common");
const typeorm_1 = require("@nestjs/typeorm");
const typeorm_2 = require("typeorm");
const advert_request_entity_1 = require("../../entities/advert_request.entity");
let AdvertService = class AdvertService {
    advertRepo;
    PRICE_PER_DAY = 20000;
    constructor(advertRepo) {
        this.advertRepo = advertRepo;
    }
    async create(userId, dto) {
        const start = new Date(dto.startDate);
        const end = new Date(dto.endDate);
        const totalDays = Math.floor((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)) + 1;
        if (totalDays <= 0) {
            throw new common_1.BadRequestException('Thời gian quảng cáo không hợp lệ');
        }
        const advert = this.advertRepo.create({
            userId,
            userEmail: dto.userEmail,
            userAvatar: dto.userAvatar,
            accountName: dto.accountName,
            title: dto.title,
            postId: dto.postId,
            pricePerDay: this.PRICE_PER_DAY,
            totalDays,
            startDate: dto.startDate,
            endDate: dto.endDate,
            totalPrice: totalDays * this.PRICE_PER_DAY,
            status: advert_request_entity_1.AdvertRequestStatus.PENDING,
            media: dto.media,
        });
        return this.advertRepo.save(advert);
    }
    async findMyRequests(userId) {
        return this.advertRepo.find({
            where: { userId },
            order: { createdAt: 'DESC' },
        });
    }
    async findAllForAdmin(filter) {
        const { status, page = 1, limit = 10 } = filter;
        const qb = this.advertRepo
            .createQueryBuilder('advert')
            .leftJoinAndSelect('advert.media', 'media')
            .orderBy('advert.createdAt', 'DESC');
        if (status) {
            qb.andWhere('advert.status = :status', { status });
        }
        qb.skip((page - 1) * limit).take(limit);
        const [items, total] = await qb.getManyAndCount();
        return {
            items,
            pagination: {
                page,
                limit,
                total,
                totalPages: Math.ceil(total / limit),
            },
        };
    }
    async findActiveAdverts() {
        const today = new Date().toISOString().split('T')[0];
        return this.advertRepo
            .createQueryBuilder('advert')
            .leftJoinAndSelect('advert.media', 'media')
            .where('advert.status = :status', {
            status: advert_request_entity_1.AdvertRequestStatus.RUNNING,
        })
            .andWhere('advert.startDate <= :today', { today })
            .andWhere('advert.endDate >= :today', { today })
            .orderBy('advert.createdAt', 'DESC')
            .getMany();
    }
    async findById(id) {
        const advert = await this.advertRepo.findOne({
            where: { id },
            relations: ['media'],
        });
        if (!advert) {
            throw new common_1.NotFoundException('Advert request not found');
        }
        return advert;
    }
    async findAllRequests() {
        return this.advertRepo.find({
            order: { createdAt: 'DESC' },
        });
    }
    async updateStatus(id, status) {
        const advert = await this.advertRepo.findOne({ where: { id } });
        if (!advert)
            throw new common_1.NotFoundException('Advert request not found');
        advert.status = status;
        return this.advertRepo.save(advert);
    }
};
exports.AdvertService = AdvertService;
exports.AdvertService = AdvertService = __decorate([
    (0, common_1.Injectable)(),
    __param(0, (0, typeorm_1.InjectRepository)(advert_request_entity_1.AdvertRequest)),
    __metadata("design:paramtypes", [typeorm_2.Repository])
], AdvertService);
//# sourceMappingURL=advert.service.js.map