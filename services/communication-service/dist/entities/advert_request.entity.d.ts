import { AdvertMedia } from './advert_media.entity';
export declare enum AdvertRequestStatus {
    PENDING = "pending",
    RUNNING = "running",
    DONE = "done",
    REJECTED = "rejected",
    CANCELLED = "cancelled",
    ACCEPTED = "accepted"
}
export declare class AdvertRequest {
    id: number;
    userId: number;
    userAvatar?: string;
    userEmail?: string;
    accountName: string;
    title: string;
    postId?: number;
    pricePerDay: number;
    totalDays: number;
    startDate: string;
    endDate: string;
    totalPrice: number;
    status: AdvertRequestStatus;
    media: AdvertMedia[];
    createdAt: Date;
}
