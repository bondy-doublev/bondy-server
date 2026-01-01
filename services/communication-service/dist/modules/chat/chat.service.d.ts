import { ChatRoom } from 'src/entities/chat-room.entity';
import { Message } from 'src/entities/message.entity';
import { RoomMember } from 'src/entities/room-member.entity';
import { Repository } from 'typeorm';
export declare class ChatService {
    private roomRepo;
    memberRepo: Repository<RoomMember>;
    private msgRepo;
    constructor(roomRepo: Repository<ChatRoom>, memberRepo: Repository<RoomMember>, msgRepo: Repository<Message>);
    createRoom(name: string, isGroup: boolean, memberIds: number[]): Promise<{
        name: string;
        isGroup: boolean;
    } & ChatRoom>;
    getRooms(userId: number, isGroup: boolean): Promise<{
        id: string;
        name: string;
        isGroup: boolean;
        createdAt: Date;
        members: {
            id: number;
        }[];
        latestUnreadMessage: {
            id: string;
            content: string;
            senderId: string;
            createdAt: Date;
            isUnread: boolean;
        } | null;
    }[]>;
    sendMessage(senderId: string, roomId: string, dto: any): Promise<Message>;
    editMessage(id: string, userId: number, content: string): Promise<Message>;
    deleteMessage(id: string, userId: number): Promise<Message>;
    markAsRead(messageId: string, userId: string): Promise<Message | null>;
    getRoomMessages(roomId: string, page?: number, limit?: number): Promise<Message[]>;
    getRoomInformation(roomId: string): Promise<ChatRoom>;
    getRoomFiles(roomId: string): Promise<{
        url: string;
        type: "image" | "file";
        fileName?: string;
    }[]>;
    getUnreadMessageCount(userId: number, roomId?: string): Promise<number>;
    markAllAsRead(userId: string, roomId: string): Promise<number>;
    markAllMessagesAsRead(userId: string): Promise<number>;
    updateGroup(roomId: string, dto: {
        name?: string;
        avatarUrl?: string;
    }): Promise<ChatRoom>;
    getPersonalRoom(userId1: number, userId2: number): Promise<ChatRoom | null>;
}
