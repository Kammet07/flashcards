import {UserEntity} from './user.entity';

export interface CollectionEntity {
  id: string;
  category: string;
  public: string;
  creator: UserEntity;
}
