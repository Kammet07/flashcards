import {Component, Input} from '@angular/core';
import {CollectionEntity} from '../../model/collection.entity';
import {HttpClient} from '@angular/common/http';
import {ToastrService} from 'ngx-toastr';
import {UserEntity} from '../../model/user.entity';

@Component({
  selector: 'app-collection-edit',
  templateUrl: './collection-edit.component.html',
  styleUrls: ['./collection-edit.component.scss']
})
export class CollectionEditComponent {
  @Input()
  collectionEdit: CollectionEntity | null = null;
  @Input()
  user: UserEntity | null = null;

  constructor(
    private readonly httpClient: HttpClient,
    private readonly toastr: ToastrService
  ) {
  }

  editCollection(): void {
    if (!this.user) {
      this.toastr.error('Not authorised!');
    } else if (!this.collectionEdit) {
      this.toastr.error('Something went wrong');
    } else {
      this.httpClient.put<CollectionEntity>(`http://192.168.0.165:8080/api/collection/${this.collectionEdit.id}`, {
        category: this.collectionEdit.category,
        public: this.collectionEdit.public,
        creatorId: this.user.id
      }, {withCredentials: true}).subscribe(response => {
        this.collectionEdit = response;
        this.toastr.success('Collection was successfully edited!');
      }, error => {
        console.error(error);
        this.toastr.error('Collection edit went wrong', error.status);
      });
    }
  }
}
