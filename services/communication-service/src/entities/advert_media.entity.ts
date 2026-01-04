import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  JoinColumn,
} from 'typeorm';
import { AdvertRequest } from './advert_request.entity';

export enum AdvertMediaType {
  IMAGE = 'IMAGE',
  VIDEO = 'VIDEO',
}

@Entity('advert_media')
export class AdvertMedia {
  @PrimaryGeneratedColumn()
  id: number;

  @Column()
  advertId: number;

  @Column({ length: 500 })
  url: string;

  @Column({
    type: 'enum',
    enum: AdvertMediaType,
  })
  type: AdvertMediaType;

  @ManyToOne(() => AdvertRequest, (advert: any) => advert.media, {
    onDelete: 'CASCADE',
  })
  @JoinColumn({ name: 'advertId' })
  advert: AdvertRequest;
}
