import { Message } from './message.entity';
import { RoomMember } from './room-member.entity';
export declare class ChatRoom {
    id: string;
    name: string;
    avatar: string;
    isGroup: boolean;
    createdAt: Date;
    messages: Message[];
    members: RoomMember[];
}
