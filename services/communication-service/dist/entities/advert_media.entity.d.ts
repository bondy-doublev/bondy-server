import { AdvertRequest } from './advert_request.entity';
export declare enum AdvertMediaType {
    IMAGE = "IMAGE",
    VIDEO = "VIDEO"
}
export declare class AdvertMedia {
    id: number;
    advertId: number;
    url: string;
    type: AdvertMediaType;
    advert: AdvertRequest;
}
