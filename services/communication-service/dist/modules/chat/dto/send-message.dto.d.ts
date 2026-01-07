declare class SharedPostDto {
    postId: string;
    title: string;
    image?: string;
    link: string;
    authorName?: string;
    authorAvatar?: string;
}
export declare class SendMessageDto {
    senderId: string;
    roomId: string;
    content?: string;
    fileUrl?: string;
    imageUrl?: string;
    sharedPost?: SharedPostDto;
    replyToMessageId?: string;
    attachments?: {
        url: string;
        type: 'image' | 'file';
        fileName?: string;
    }[];
}
export {};
