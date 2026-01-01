import { ChatService } from './chat.service';
import { CreateRoomDto } from './dto/create-room.dto';
import { SendMessageDto } from './dto/send-message.dto';
import { EditMessageDto } from './dto/edit-message.dto';
import { DeleteMessageDto } from './dto/delete-message.dto';
import { UpdateGroupDto } from './dto/update-group.dto';
export declare class ChatController {
    private chatService;
    constructor(chatService: ChatService);
    getMessages(roomId: string, page: string, limit: string): Promise<import("../../entities/message.entity").Message[]>;
    getPrivateRooms(userId: string): Promise<{
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
    getPublicRooms(userId: string): Promise<{
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
    createRoom(body: CreateRoomDto): Promise<{
        name: string;
        isGroup: boolean;
    } & import("../../entities/chat-room.entity").ChatRoom>;
    sendMessage(body: SendMessageDto): Promise<import("../../entities/message.entity").Message>;
    editMessage(id: string, body: EditMessageDto): Promise<import("../../entities/message.entity").Message>;
    deleteMessage(id: string, body: DeleteMessageDto): Promise<import("../../entities/message.entity").Message>;
    getTotalUnread(userId: number): Promise<{
        total: number;
    }>;
    getRoomMembers(roomId: string): Promise<import("../../entities/chat-room.entity").ChatRoom>;
    getRoomFiles(roomId: string): Promise<{
        files: {
            url: string;
            type: "image" | "file";
            fileName?: string;
        }[];
    }>;
    updateGroup(roomId: string, body: UpdateGroupDto): Promise<import("../../entities/chat-room.entity").ChatRoom>;
    getPersonalRoom(userId1: string, userId2: string): Promise<import("../../entities/chat-room.entity").ChatRoom | null>;
}
