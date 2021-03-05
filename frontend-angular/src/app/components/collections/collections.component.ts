import {Component, Input, Output} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {CollectionEntity} from '../../model/collection.entity';
import {UserEntity} from '../../model/user.entity';
import {ToastrService} from 'ngx-toastr';

@Component({
  selector: 'app-collections',
  templateUrl: './collections.component.html',
  styleUrls: ['./collections.component.scss']
})
export class CollectionsComponent {
  category = '';
  public = false;
  @Input()
  userId: UserEntity | null = null;
  @Output()
  collections: CollectionEntity[] | null = null;

  constructor(
    private readonly httpClient: HttpClient,
    private readonly toastr: ToastrService
  ) {
    this.loadCollections();
  }

  createCollection(): void {
    if (!this.userId) {
      this.toastr.error('Not authorised!');
    } else {
      this.httpClient.post<CollectionEntity>('http://localhost:8080/api/collection', {
        category: this.category,
        public: this.public,
        creatorId: this.userId.id
      }, {withCredentials: true}).subscribe(u => {
        this.toastr.success(`Collection ${u.category} was created`);
      }, error => {
        console.error(error);
        this.toastr.error('Collection creation went wrong');
      });
    }
  }

  private loadCollections(): void {
    this.httpClient.get<CollectionEntity[]>('http://localhost:8080/api/collection')
      .subscribe(u => {
        this.collections = u;
      }, error => {
        console.error(error);
        this.toastr.error('Something went wrong while getting collections', error.status);
      });
  }
}
