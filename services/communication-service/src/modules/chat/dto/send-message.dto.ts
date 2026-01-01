import { IsString, IsOptional, IsArray, ValidateNested } from 'class-validator';
import { Type } from 'class-transformer';

class SharedPostDto {
  @IsString()
  postId: string;

  @IsString()
  title: string;

  @IsString()
  @IsOptional()
  image?: string;

  @IsString()
  link: string;

  @IsString()
  @IsOptional()
  authorName?: string;

  @IsString()
  @IsOptional()
  authorAvatar?: string;
}

export class SendMessageDto {
  @IsString()
  senderId: string;

  @IsString()
  roomId: string;

  @IsString()
  @IsOptional()
  content?: string;

  @IsString()
  @IsOptional()
  fileUrl?: string;

  @IsString()
  @IsOptional()
  imageUrl?: string;

  @ValidateNested()
  @Type(() => SharedPostDto)
  @IsOptional()
  sharedPost?: SharedPostDto;

  @IsString()
  @IsOptional()
  replyToMessageId?: string;

  @IsArray()
  @IsOptional()
  attachments?: { url: string; type: 'image' | 'file'; fileName?: string }[];
}
