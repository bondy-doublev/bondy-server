import { AdvertRequestStatus } from 'src/entities/advert_request.entity';
export declare class FilterAdvertDto {
    status?: AdvertRequestStatus;
    page?: number;
    limit?: number;
}
