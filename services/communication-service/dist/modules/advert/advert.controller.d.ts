import { AdvertService } from './advert.service';
import { CreateAdvertRequestDto } from './dto/create-advert-request.dto';
import { UpdateAdvertRequestStatusDto } from './dto/update-advert-request-status';
import { AdvertRequest } from 'src/entities/advert_request.entity';
import { FilterAdvertDto } from './dto/filter-advert.dto';
import { GetMyRequestsQuery } from './dto/get-my-request.dto';
export declare class AdvertController {
    private readonly advertService;
    constructor(advertService: AdvertService);
    create(req: any, dto: CreateAdvertRequestDto): Promise<AdvertRequest>;
    getAllForAdmin(query: FilterAdvertDto): Promise<{
        items: AdvertRequest[];
        pagination: {
            page: number;
            limit: number;
            total: number;
            totalPages: number;
        };
    }>;
    getActiveAdverts(): Promise<AdvertRequest[]>;
    getMyRequests(query: GetMyRequestsQuery): Promise<AdvertRequest[]>;
    getAll(): Promise<AdvertRequest[]>;
    updateStatus(id: string, dto: UpdateAdvertRequestStatusDto): Promise<AdvertRequest>;
    getById(id: number): Promise<AdvertRequest>;
}
