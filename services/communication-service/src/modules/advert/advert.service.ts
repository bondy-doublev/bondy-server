import {
  Injectable,
  BadRequestException,
  NotFoundException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import {
  AdvertRequest,
  AdvertRequestStatus,
} from 'src/entities/advert_request.entity';
import { CreateAdvertRequestDto } from './dto/create-advert-request.dto';

@Injectable()
export class AdvertService {
  private readonly PRICE_PER_DAY = 20000;

  constructor(
    @InjectRepository(AdvertRequest)
    private readonly advertRepo: Repository<AdvertRequest>,
  ) {}

  async create(userId: number, dto: CreateAdvertRequestDto) {
    const start = new Date(dto.startDate);
    const end = new Date(dto.endDate);

    const totalDays =
      Math.floor((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)) + 1;

    if (totalDays <= 0) {
      throw new BadRequestException('Thời gian quảng cáo không hợp lệ');
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
      status: AdvertRequestStatus.PENDING,
      media: dto.media,
    });

    return this.advertRepo.save(advert);
  }

  async findMyRequests(userId: number) {
    return this.advertRepo.find({
      where: { userId },
      order: { createdAt: 'DESC' },
    });
  }

  async findAllForAdmin(filter: {
    status?: AdvertRequestStatus;
    page?: number;
    limit?: number;
  }) {
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
        status: AdvertRequestStatus.RUNNING,
      })
      .andWhere('advert.startDate <= :today', { today })
      .andWhere('advert.endDate >= :today', { today })
      .orderBy('advert.createdAt', 'DESC')
      .getMany();
  }

  async findById(id: number) {
    const advert = await this.advertRepo.findOne({
      where: { id },
      relations: ['media'],
    });

    if (!advert) {
      throw new NotFoundException('Advert request not found');
    }

    return advert;
  }

  async findAllRequests() {
    return this.advertRepo.find({
      order: { createdAt: 'DESC' },
    });
  }

  async updateStatus(id: number, status: AdvertRequestStatus) {
    const advert = await this.advertRepo.findOne({ where: { id } });
    if (!advert) throw new NotFoundException('Advert request not found');

    advert.status = status;
    return this.advertRepo.save(advert);
  }
}
