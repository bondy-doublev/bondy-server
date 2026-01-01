import { ChatRoom } from './chat-room.entity';
export declare class Message {
    id: string;
    roomId: string;
    room: ChatRoom;
    senderId: string;
    replyToMessageId?: string;
    content?: string;
    fileUrl?: string;
    imageUrl?: string;
    attachments?: {
        url: string;
        type: 'image' | 'file';
        fileName?: string;
    }[];
    sharedPost?: {
        postId: string;
        title: string;
        image?: string;
        link: string;
        authorName?: string;
        authorAvatar?: string;
    };
    isEdited: boolean;
    isDeleted: boolean;
    readBy: string[];
    createdAt: Date;
    updatedAt: Date;
}
