import { AdvertMediaType } from 'src/entities/advert_media.entity';
declare class AdvertMediaDto {
    url: string;
    type: AdvertMediaType;
}
export declare class CreateAdvertRequestDto {
    userId: number;
    userEmail?: string;
    userAvatar?: string;
    accountName: string;
    title: string;
    postId?: number;
    startDate: string;
    endDate: string;
    media: AdvertMediaDto[];
}
export {};
