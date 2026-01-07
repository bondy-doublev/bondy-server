import { Repository } from 'typeorm';
import { AdvertRequest, AdvertRequestStatus } from 'src/entities/advert_request.entity';
import { CreateAdvertRequestDto } from './dto/create-advert-request.dto';
export declare class AdvertService {
    private readonly advertRepo;
    private readonly PRICE_PER_DAY;
    constructor(advertRepo: Repository<AdvertRequest>);
    create(userId: number, dto: CreateAdvertRequestDto): Promise<AdvertRequest>;
    findMyRequests(userId: number): Promise<AdvertRequest[]>;
    findAllForAdmin(filter: {
        status?: AdvertRequestStatus;
        page?: number;
        limit?: number;
    }): Promise<{
        items: AdvertRequest[];
        pagination: {
            page: number;
            limit: number;
            total: number;
            totalPages: number;
        };
    }>;
    findActiveAdverts(): Promise<AdvertRequest[]>;
    findById(id: number): Promise<AdvertRequest>;
    findAllRequests(): Promise<AdvertRequest[]>;
    updateStatus(id: number, status: AdvertRequestStatus): Promise<AdvertRequest>;
}
